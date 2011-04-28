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
package de.escidoc.admintool.view.role;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;

import de.escidoc.core.resources.Resource;

final class ResourceSelectionListener implements ValueChangeListener {

    private final RoleView roleView;

    ResourceSelectionListener(final RoleView roleView) {
        this.roleView = roleView;
    }

    private static final String EMPTY_STRING = "";

    private static final long serialVersionUID = -3079481037459553076L;

    @Override
    public void valueChange(final ValueChangeEvent event) {
        if (event.getProperty() != null && event.getProperty().getValue() != null
            && event.getProperty().getValue() instanceof Resource) {
            final Resource selected = (Resource) event.getProperty().getValue();
            roleView.searchBox.setValue(selected.getXLinkTitle());
        }
        else {
            roleView.searchBox.setValue(EMPTY_STRING);
        }
    }
}