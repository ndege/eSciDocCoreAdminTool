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
package de.escidoc.admintool.view.admintask.loadexamples;

import com.google.common.base.Preconditions;

import com.vaadin.ui.Button;
import com.vaadin.ui.Window;

import java.util.Collection;

import de.escidoc.admintool.service.AdminService;
import de.escidoc.admintool.view.ViewConstants;
import de.escidoc.admintool.view.admintask.AbstractCustomView;

public class LoadExampleResourceViewImpl extends AbstractCustomView implements LoadExampleView {

    private static final long serialVersionUID = -2478541354753165293L;

    private final Button loadExampleButton = new Button(ViewConstants.LOAD_EXAMPLES);

    private OnLoadExampleClick listener;

    private final AdminService adminService;

    private final Window mainWindow;

    public LoadExampleResourceViewImpl(final Window mainWindow, final AdminService adminService) {

        checkPreconditions(mainWindow, adminService);
        this.mainWindow = mainWindow;
        this.adminService = adminService;
        init();
    }

    private void checkPreconditions(final Window mainWindow, final AdminService adminService) {
        Preconditions.checkNotNull(mainWindow, "mainWindow can not be null: %s", mainWindow);
        Preconditions.checkNotNull(adminService, "adminService can not be null: %s", adminService);
    }

    private void init() {
        addLoadExampleButton();
        createLoadExampleButtonListener();
        addListener();
    }

    private void addLoadExampleButton() {
        loadExampleButton.setWidth(150, UNITS_PIXELS);
        getViewLayout().addComponent(loadExampleButton);
    }

    private void addListener() {
        loadExampleButton.addListener(listener);
    }

    interface ShowResultCommand {
        void execute(Collection<?> entry);
    }

    private void createLoadExampleButtonListener() {
        listener = new OnLoadExampleClick(new ShowResultCommandImpl(this));
        listener.setAdminService(adminService);
        listener.setMainWindow(mainWindow);
    }
}