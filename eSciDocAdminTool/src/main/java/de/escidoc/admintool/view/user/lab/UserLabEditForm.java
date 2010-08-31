package de.escidoc.admintool.view.user.lab;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.data.util.POJOContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;

import de.escidoc.admintool.app.AdminToolApplication;
import de.escidoc.admintool.app.PropertyId;
import de.escidoc.admintool.service.RoleService;
import de.escidoc.admintool.service.UserService;
import de.escidoc.admintool.view.ViewConstants;
import de.escidoc.admintool.view.validator.EmptyFieldValidator;
import de.escidoc.core.client.exceptions.EscidocClientException;
import de.escidoc.core.client.exceptions.EscidocException;
import de.escidoc.core.client.exceptions.InternalClientException;
import de.escidoc.core.client.exceptions.TransportException;
import de.escidoc.core.resources.aa.role.Role;
import de.escidoc.core.resources.aa.useraccount.Grant;
import de.escidoc.core.resources.aa.useraccount.UserAccount;
import de.escidoc.vaadin.dialog.ErrorDialog;
import de.escidoc.vaadin.utilities.Converter;
import de.escidoc.vaadin.utilities.LayoutHelper;

@SuppressWarnings("serial")
public class UserLabEditForm extends CustomComponent implements ClickListener {

    private static final String EDIT_USER_ACCOUNT = "Edit User Account";

    private static final Logger log =
        LoggerFactory.getLogger(UserLabEditForm.class);

    private static final int ROLE_LIST_HEIGHT = 100;

    private static final String ROLE_LIST_WIDTH = "400px";

    private static final String ROLES_LABEL = "Roles";

    private static final int NUMBER_OF_ROLE_TO_SHOW = 5;

    private static final int LABEL_WIDTH = 100;

    private final Panel panel = new Panel();

    private final FormLayout form = new FormLayout();

    private final ListSelect roleList = new ListSelect();

    private final Button save = new Button("Save", this);

    private final Button cancel = new Button("Cancel", this);

    private final Label objIdField = new Label();

    private final AdminToolApplication app;

    private final UserService userService;

    private final RoleService roleService;

    private HorizontalLayout footer;

    private TextField nameField;

    private TextField loginNameField;

    private Label modifiedOn;

    private Label modifiedBy;

    private Label createdOn;

    private Label createdBy;

    private CheckBox state;

    private Item item;

    private String userObjectId;

    public UserLabEditForm(final AdminToolApplication app,
        final UserService userService, final RoleService roleService) {
        this.app = app;
        this.userService = userService;
        this.roleService = roleService;
        init();
    }

    public void init() {
        panel.setContent(form);
        panel.setCaption(EDIT_USER_ACCOUNT);

        form.setSpacing(false);

        nameField = new TextField();
        nameField.setWidth(ROLE_LIST_WIDTH);
        nameField.setWriteThrough(false);

        final int height = 15;
        panel.addComponent(LayoutHelper.create(ViewConstants.NAME_LABEL,
            nameField, LABEL_WIDTH, true));
        loginNameField = new TextField();
        loginNameField.setWidth(ROLE_LIST_WIDTH);
        loginNameField.setWriteThrough(false);
        loginNameField.setReadOnly(true);
        panel.addComponent(LayoutHelper.create(ViewConstants.LOGIN_NAME_LABEL,
            loginNameField, LABEL_WIDTH, false));

        panel.addComponent(LayoutHelper.create(ViewConstants.OBJECT_ID_LABEL,
            objIdField, LABEL_WIDTH, false));

        modifiedOn = new Label();
        modifiedBy = new Label();
        panel.addComponent(LayoutHelper.create("Modified", "by", modifiedOn,
            modifiedBy, LABEL_WIDTH, height, false));

        createdOn = new Label();
        createdBy = new Label();
        panel.addComponent(LayoutHelper.create("Created", "by", createdOn,
            createdBy, LABEL_WIDTH, height, false));

        state = new CheckBox();
        state.setWriteThrough(false);
        panel.addComponent(LayoutHelper.create("Active status", state,
            LABEL_WIDTH, false));

        initRoleListSelect();

        panel.addComponent(addFooter());
        setCompositionRoot(panel);
    }

    private void initRoleListSelect() {
        roleList.setRows(NUMBER_OF_ROLE_TO_SHOW);
        roleList.setWidth(ROLE_LIST_WIDTH);
        roleList.setNullSelectionAllowed(true);
        roleList.setMultiSelect(true);
        roleList.setImmediate(true);

        // TODO add, edit and remove buttons.
        panel.addComponent(LayoutHelper.create(ROLES_LABEL, roleList,
            LABEL_WIDTH, ROLE_LIST_HEIGHT, false, new Button[] {}));
    }

    private List<Role> getRoles() {
        try {
            return getRolesFor(retrieveGrantsFor(userObjectId));
        }
        catch (final EscidocException e) {
            app.getMainWindow().addWindow(
                new ErrorDialog(app.getMainWindow(), "Error",
                    "An unexpected error occured! See log for details."));
            log.error("An unexpected error occured! See log for details.", e);
            e.printStackTrace();
        }
        catch (final InternalClientException e) {
            app.getMainWindow().addWindow(
                new ErrorDialog(app.getMainWindow(), "Error",
                    "An unexpected error occured! See log for details."));
            log.error("An unexpected error occured! See log for details.", e);
            e.printStackTrace();
        }
        catch (final TransportException e) {
            app.getMainWindow().addWindow(
                new ErrorDialog(app.getMainWindow(), "Error",
                    "An unexpected error occured! See log for details."));
            log.error("An unexpected error occured! See log for details.", e);
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    private List<Role> getRolesFor(final Collection<Grant> userGrants)
        throws EscidocException, InternalClientException, TransportException {
        final List<Role> roles = new ArrayList<Role>();
        for (final Grant grant : userGrants) {
            roles.add(getRoleFor(grant));
        }
        return roles;
    }

    private Role getRoleFor(final Grant grant) throws EscidocException,
        InternalClientException, TransportException {
        return roleService.retrieve(grant
            .getGrantProperties().getRole().getObjid());
    }

    private Collection<Grant> retrieveGrantsFor(final String userObjectId) {
        try {
            return userService.retrieveCurrentGrants(userObjectId);
        }
        catch (final InternalClientException e) {
            app.getMainWindow().addWindow(
                new ErrorDialog(app.getMainWindow(), "Error",
                    "An unexpected error occured! See log for details."));
            log.error("An unexpected error occured! See log for details.", e);
            e.printStackTrace();
        }
        catch (final TransportException e) {
            app.getMainWindow().addWindow(
                new ErrorDialog(app.getMainWindow(), "Error",
                    "An unexpected error occured! See log for details."));
            log.error("An unexpected error occured! See log for details.", e);
            e.printStackTrace();
        }
        catch (final EscidocClientException e) {
            app.getMainWindow().addWindow(
                new ErrorDialog(app.getMainWindow(), "Error",
                    "An unexpected error occured! See log for details."));
            log.error("An unexpected error occured! See log for details.", e);
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    private HorizontalLayout addFooter() {
        footer = new HorizontalLayout();
        footer.setSpacing(true);

        footer.addComponent(save);
        footer.addComponent(cancel);
        return footer;
    }

    public void buttonClick(final ClickEvent event) {
        final Button source = event.getButton();
        if (source == cancel) {
            nameField.setValue("");
            loginNameField.setValue("");
        }
        else if (source == save) {
            try {
                boolean valid = true;
                valid =
                    EmptyFieldValidator.isValid(nameField, "Please enter a "
                        + ViewConstants.NAME_ID);
                valid &=
                    (EmptyFieldValidator.isValid(loginNameField,
                        "Please enter a " + ViewConstants.LOGIN_NAME_ID));
                if (valid) {
                    userService.update(getSelectedItemId(), (String) item
                        .getItemProperty(ViewConstants.NAME_ID).getValue());
                    if (state.isModified()) {
                        changeState();
                    }
                    nameField.setComponentError(null);
                    loginNameField.setComponentError(null);
                    nameField.commit();
                    loginNameField.commit();
                }

            }
            catch (final EscidocException e) {
                log.error("An unexpected error occured! See log for details.",
                    e);
                e.printStackTrace();
            }
            catch (final InternalClientException e) {
                log.error("An unexpected error occured! See log for details.",
                    e);
                e.printStackTrace();
            }
            catch (final TransportException e) {
                log.error("An unexpected error occured! See log for details.",
                    e);
                e.printStackTrace();
            }
            catch (final EscidocClientException e) {
                log.error("An unexpected error occured! See log for details.",
                    e);
                e.printStackTrace();
            }
        }
    }

    public void setSelected(final Item item) {
        this.item = item;
        if (item != null) {
            userObjectId = (String) item.getItemProperty("objid").getValue();

            nameField.setPropertyDataSource(item
                .getItemProperty(ViewConstants.NAME_ID));
            loginNameField.setPropertyDataSource(item
                .getItemProperty("properties.loginName"));
            objIdField.setPropertyDataSource(item
                .getItemProperty(PropertyId.OBJECT_ID));
            modifiedOn.setCaption(Converter
                .dateTimeToString((org.joda.time.DateTime) item
                    .getItemProperty("lastModificationDate").getValue()));
            modifiedBy.setPropertyDataSource(item
                .getItemProperty("properties.modifiedBy.objid"));
            state.setPropertyDataSource(item
                .getItemProperty("properties.active"));
            createdOn.setCaption(Converter
                .dateTimeToString((org.joda.time.DateTime) item
                    .getItemProperty("properties.creationDate").getValue()));
            createdBy.setPropertyDataSource(item
                .getItemProperty("properties.createdBy.objid"));

            bindRolesWithView();
        }
    }

    private void bindRolesWithView() {
        final List<Role> userRoles = getRoles();
        if (userRoles.size() > 0) {
            final POJOContainer<Role> roleContainer =
                new POJOContainer<Role>(userRoles, PropertyId.OBJECT_ID,
                    PropertyId.NAME);
            roleList.setContainerDataSource(roleContainer);
            roleList.setItemCaptionPropertyId(PropertyId.NAME);
        }
    }

    private String getSelectedItemId() {
        if (item == null) {
            return "";
        }
        return (String) item.getItemProperty("objid").getValue();
    }

    public UserAccount deleteUser() throws EscidocException,
        InternalClientException, TransportException {
        return userService.delete(getSelectedItemId());
    }

    public void changeState() throws InternalClientException,
        TransportException, EscidocClientException {
        if (!(Boolean) state.getPropertyDataSource().getValue()) {
            userService.activate(getSelectedItemId());
        }
        else {
            userService.deactivate(getSelectedItemId());
        }
    }
}
