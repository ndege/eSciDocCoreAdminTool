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
package de.escidoc.admintool.view.resource;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.vaadin.data.Item;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Field;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Reindeer;

import de.escidoc.admintool.app.AdminToolApplication;
import de.escidoc.admintool.domain.PdpRequest;
import de.escidoc.admintool.service.internal.OrgUnitServiceLab;
import de.escidoc.admintool.service.internal.ResourceService;
import de.escidoc.admintool.view.ViewConstants;

public class ResourceEditViewImpl extends CustomComponent implements ResourceEditView {

    private static final long serialVersionUID = -7860622778922198542L;

    private final VerticalLayout vLayout = new VerticalLayout();

    private final SaveAndCancelButtons footers = new SaveAndCancelButtons();

    private final Map<String, Field> fieldByName = new HashMap<String, Field>();

    private final Panel panel = new Panel(ViewConstants.EDIT_ORG_UNIT);

    private final FormLayout formLayout = FormLayoutFactory.create();

    private final PropertiesFields propertyFields;

    private final ResourceToolbar resourceToolbar;

    private final Window mainWindow;

    private final ResourceService orgUnitService;

    private final ResourceView resourceView;

    private ResourceBtnListener updateOrgUnitBtnListener;

    private OrgUnitSpecificView resourceSpecific;

    public ResourceEditViewImpl(final AdminToolApplication app, final Window mainWindow,
        final ResourceViewImpl resourceView, final ResourceService orgUnitService,
        final ResourceContainer resourceContainer, final PdpRequest pdpRequest) {

        checkPreconditions(mainWindow, resourceView, orgUnitService, resourceContainer, pdpRequest);

        this.mainWindow = mainWindow;
        this.resourceView = resourceView;
        this.orgUnitService = orgUnitService;
        formLayout.setWidth(75, UNITS_PERCENTAGE);

        resourceToolbar =
            new ResourceToolbar(app, resourceView, mainWindow, orgUnitService, resourceContainer, pdpRequest);

        propertyFields = new PropertiesFieldsImpl(app, vLayout, formLayout, fieldByName, pdpRequest);
        resourceSpecific = createOrgUnitSpecificView(mainWindow, orgUnitService, resourceContainer);

        buildView();
    }

    private void checkPreconditions(
        final Window mainWindow, final ResourceViewImpl resourceViewImpl, final ResourceService orgUnitService,
        final ResourceContainer resourceContainer, final PdpRequest pdpRequest) {
        Preconditions.checkNotNull(mainWindow, "mainWindow is null: %s", mainWindow);
        Preconditions.checkNotNull(resourceViewImpl, "resourceViewImpl is null: %s", resourceViewImpl);
        Preconditions.checkNotNull(orgUnitService, "orgUnitService is null: %s", orgUnitService);
        Preconditions.checkNotNull(resourceContainer, "resourceContainer is null: %s", resourceContainer);
        Preconditions.checkNotNull(pdpRequest, "pdpRequest is null: %s", pdpRequest);
    }

    private OrgUnitSpecificView createOrgUnitSpecificView(
        final Window mainWindow, final ResourceService orgUnitService, final ResourceContainer resourceContainer) {
        return new OrgUnitSpecificView(mainWindow, (OrgUnitServiceLab) orgUnitService, resourceContainer, formLayout,
            fieldByName);
    }

    private void buildView() {
        resourceSpecific.init();

        setCompositionRoot(panel);
        panel.setContent(vLayout);
        panel.setStyleName(Reindeer.PANEL_LIGHT);
        vLayout.setHeight(100, UNITS_PERCENTAGE);
        formLayout.setWidth(517, UNITS_PIXELS);
        vLayout.addComponent(resourceToolbar);
        vLayout.addComponent(propertyFields);
        addSpace();
        addSaveAndCancelButtons();
    }

    private void addSpace() {
        formLayout.addComponent(new Label("<br/>", Label.CONTENT_XHTML));
    }

    public void setResourceSpecificView(final OrgUnitSpecificView resourceSpecific) {
        this.resourceSpecific = resourceSpecific;
    }

    private void addSaveAndCancelButtons() {
        updateOrgUnitBtnListener =
            new UpdateOrgUnitBtnListener(propertyFields.getAllFields(), fieldByName, mainWindow, resourceView,
                orgUnitService);
        footers.setOkButtonListener(updateOrgUnitBtnListener);

        footers.getCancelBtn().addListener(new ClickListener() {
            private static final long serialVersionUID = 7587546491866882218L;

            @Override
            public void buttonClick(final ClickEvent event) {
                final Collection<Field> values = fieldByName.values();
                for (final Field field : values) {
                    field.discard();
                }
            }
        });
        formLayout.addComponent(footers);
        footers.setWidth(100, UNITS_PERCENTAGE);
    }

    @Override
    public void bind(final Item item) {
        resourceToolbar.bind(item);
        propertyFields.bind(item);
        resourceSpecific.bind(item);
        updateOrgUnitBtnListener.bind(item);
    }

    @Override
    public void setFormReadOnly(final boolean isReadOnly) {
        propertyFields.setNotEditable(isReadOnly);
        resourceSpecific.setNotEditable(isReadOnly);
    }

    @Override
    public void setFooterVisible(final boolean b) {
        footers.setVisible(b);
    }
}