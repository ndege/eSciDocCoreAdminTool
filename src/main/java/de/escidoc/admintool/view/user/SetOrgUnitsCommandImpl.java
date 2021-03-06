/**
 * CDDL HEADER START
 *
 * The contents of this file are subject to the terms of the
 * Common Development and Distribution License, Version 1.0 only
 * (the "License").  You may not use this file except in compliance
 * with the License.
 *
 * You can obtain a copy of the license at license/ESCIDOC.LICENSE
 * or https://www.escidoc.org/license/ESCIDOC.LICENSE .
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each
 * file and include the License file at license/ESCIDOC.LICENSE.
 * If applicable, add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your own identifying
 * information: Portions Copyright [yyyy] [name of copyright owner]
 *
 * CDDL HEADER END
 *
 *
 *
 * Copyright 2011 Fachinformationszentrum Karlsruhe Gesellschaft
 * fuer wissenschaftlich-technische Information mbH and Max-Planck-
 * Gesellschaft zur Foerderung der Wissenschaft e.V.
 * All rights reserved.  Use is subject to license terms.
 */
package de.escidoc.admintool.view.user;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.vaadin.ui.AbstractSelect;

import de.escidoc.admintool.app.AppConstants;
import de.escidoc.admintool.service.internal.UserService;
import de.escidoc.admintool.view.resource.ResourceRefDisplay;
import de.escidoc.core.client.exceptions.EscidocClientException;
import de.escidoc.core.resources.aa.useraccount.Attribute;
import de.escidoc.core.resources.aa.useraccount.Attributes;

public class SetOrgUnitsCommandImpl implements SetOrgUnitsCommand {

    private final UserService userService;

    private String objectId;

    private Set<ResourceRefDisplay> orgUnitIds;

    public SetOrgUnitsCommandImpl(final UserService userService) {
        this.userService = userService;
    }

    @Override
    public void setSelectedUserId(final String objid) {
        objectId = objid;
    }

    @Override
    public void setSeletectedOrgUnit(final Set<ResourceRefDisplay> orgUnitIds) {
        this.orgUnitIds = orgUnitIds;
    }

    @Override
    public void execute(final AbstractSelect select) throws EscidocClientException {
        Preconditions.checkNotNull(select, "orgUnitWidget is null: %s", select);
        Preconditions.checkNotNull(orgUnitIds, "orgUnitIds is null: %s", orgUnitIds);
        Preconditions.checkNotNull(userService, "userService is null: %s", userService);

        if (shouldSetOrgUnits(select)) {
            for (final ResourceRefDisplay resourceRefDisplay : orgUnitIds.toArray(new ResourceRefDisplay[orgUnitIds
                .size()])) {
                userService.assign(objectId, new Attribute(AppConstants.DEFAULT_ORG_UNIT_ATTRIBUTE_NAME,
                    resourceRefDisplay.getObjectId()));
            }
        }
    }

    private boolean shouldSetOrgUnits(final AbstractSelect select) {
        return select.size() > 0;
    }

    @Override
    public void update(final List<String> oldOrgUnits) throws EscidocClientException {
        if (updateNeeded(toCreate(oldOrgUnits), toRemove(oldOrgUnits))) {
            addOrgUnit(toCreate(oldOrgUnits));
            removeOrgUnit(toRemove(oldOrgUnits));
        }
    }

    private Set<String> toRemove(final Collection<? extends String> oldOrgUnits) {
        final Set<String> toRemove = Sets.newHashSet(oldOrgUnits);
        toRemove.removeAll(getSelectedOrgUnitId());
        return toRemove;
    }

    private Set<String> toCreate(final Collection<? extends String> oldOrgUnits) {
        final Set<String> toCreate = Sets.newHashSet(getSelectedOrgUnitId());
        toCreate.removeAll(new HashSet<String>(oldOrgUnits));
        return toCreate;

    }

    private Set<String> getSelectedOrgUnitId() {
        final Set<String> newOrgUnits = new HashSet<String>();
        for (final ResourceRefDisplay resourceRefDisplay : orgUnitIds
            .toArray(new ResourceRefDisplay[orgUnitIds.size()])) {
            newOrgUnits.add(resourceRefDisplay.getObjectId());
        }
        return newOrgUnits;
    }

    private boolean updateNeeded(final Set<String> toCreate, final Set<String> toRemove) {
        return !toCreate.equals(toRemove);
    }

    private void addOrgUnit(final Set<String> toCreate) throws EscidocClientException {
        for (final String string : toCreate) {
            userService.assign(objectId, new Attribute(AppConstants.DEFAULT_ORG_UNIT_ATTRIBUTE_NAME, string));
        }
    }

    private void removeOrgUnit(final Set<String> toRemove) throws EscidocClientException {
        final Attributes allAttributes = userService.retrieveAttributes(objectId);

        for (final Attribute attribute : allAttributes) {
            if (isOrgUnit(attribute)) {
                final String value = attribute.getValue();
                if (toRemove.contains(value)) {
                    userService.removeAttribute(objectId, attribute);
                }
            }
        }
    }

    private boolean isOrgUnit(final Attribute attribute) {
        return "o".equals(attribute.getName());
    }
}
