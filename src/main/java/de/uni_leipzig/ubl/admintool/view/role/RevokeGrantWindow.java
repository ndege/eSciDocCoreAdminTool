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
package de.uni_leipzig.ubl.admintool.view.role;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.util.POJOContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import de.escidoc.admintool.app.Command;
import de.escidoc.core.client.exceptions.EscidocClientException;
import de.escidoc.core.resources.aa.useraccount.Grant;

public class RevokeGrantWindow extends CustomComponent {

    private static final Logger LOG = LoggerFactory.getLogger(RevokeGrantWindow.class);

    private static final String COMMENT_FIELD_WIDTH = "350px";

    private static final int COMMENT_ROW = 3;

    private static final String CANCEL_LABEL = "Cancel";

    private static final String OK_LABEL = "Ok";

    private static final String WINDOW_HEIGHT = "225px";

    private static final String WINDOW_WIDTH = "400px";

    private static final String REVOKE_GRANT_CAPTION = "Revoke Grant";

    private final Window subwindow = new Window(REVOKE_GRANT_CAPTION);

    private final Panel panel = new Panel();

    private final TextField commentField = new TextField("Comment");

    private final Command command;

    private final POJOContainer<Grant> grants;

    private final Grant grant;

    public RevokeGrantWindow(final Command command, final Grant grant, final POJOContainer<Grant> grants) {
        this.command = command;
        this.grant = grant;
        this.grants = grants;
        setCompositionRoot(panel);
        initWindow();
        initField();
        initFooter();
    }

    public Window getModalWindow() {
        return subwindow;
    }

    private void initWindow() {
        subwindow.setSizeUndefined();
        subwindow.setModal(true);
        subwindow.setWidth(WINDOW_WIDTH);
        subwindow.setHeight(WINDOW_HEIGHT);
        mainLayout = (VerticalLayout) subwindow.getContent();
    }

    private void initField() {
        commentField.setWidth(COMMENT_FIELD_WIDTH);
        commentField.setRows(COMMENT_ROW);
        subwindow.addComponent(commentField);
    }

    // TODO move in extra class
    private final HorizontalLayout footer = new HorizontalLayout();

    private final Button okButton = new Button(OK_LABEL);

    private final Button cancelButton = new Button(CANCEL_LABEL);

    private VerticalLayout mainLayout;

    private void initFooter() {
        mainLayout.addComponent(footer);
        footer.addComponent(okButton);
        footer.addComponent(cancelButton);
        addButtonListeners();
    }

    private void addButtonListeners() {
        addOkButtonListener();
        addCancelButtonListener();
    }

    private void addOkButtonListener() {
        okButton.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(final ClickEvent event) {
                revokeGrant();
                closeWindow();
            }

            private void revokeGrant() {
                try {
                    command.execute();
                    grants.removeItem(grant);
                }
                catch (final EscidocClientException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        });
    }

    private void closeWindow() {
        (subwindow.getParent()).removeWindow(subwindow);
    }

    private void addCancelButtonListener() {
        cancelButton.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(final ClickEvent event) {
                closeWindow();
            }
        });
    }
}