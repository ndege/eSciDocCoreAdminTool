package de.escidoc.admintool.view.orgunit;

import static de.escidoc.admintool.view.ViewConstants.VISIBLE_PARENTS;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.vaadin.data.Property;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;

import de.escidoc.admintool.app.AdminToolApplication;
import de.escidoc.admintool.service.OrgUnitService;
import de.escidoc.admintool.view.OrgUnitEditor;
import de.escidoc.admintool.view.ViewConstants;
import de.escidoc.core.client.exceptions.EscidocException;
import de.escidoc.core.client.exceptions.InternalClientException;
import de.escidoc.core.client.exceptions.TransportException;
import de.escidoc.core.resources.oum.OrganizationalUnit;
import de.escidoc.core.resources.oum.PredecessorForm;
import de.escidoc.vaadin.utilities.LayoutHelper;

@SuppressWarnings("serial")
public class OrgUnitAddForm extends CustomComponent
    implements ClickListener, Serializable {

    private final Button save = new Button("Save", this);

    private final Button cancel = new Button("Cancel", this);

    private final AdminToolApplication app;

    private final OrgUnitService service;

    private final Collection<OrganizationalUnit> allOrgUnits;

    private final OrgUnitList orgUnitList;

    private ListSelect predecessorsListSelect;

    private Button addPredecessorLink;

    private Button removePredecessor;

    private final FormLayout form = new FormLayout();

    private final Panel panel = new Panel();

    private final TextField titleField = new TextField();

    private final TextField descriptionField = new TextField();

    private final TextField alternativeField = new TextField();

    private final TextField identifierField = new TextField();

    private final TextField orgTypeField = new TextField();

    private final TextField cityField = new TextField();

    private final TextField countryField = new TextField();

    private final TextField coordinatesField = new TextField();

    private final ListSelect orgUnitListSelect = new ListSelect();

    private final Button addOrgUnitButton = new Button(ViewConstants.ADD_LABEL);

    private final Button removeOrgUnitButton = new Button(
        ViewConstants.REMOVE_LABEL);

    private final Button addPredecessorButton = new Button(
        ViewConstants.ADD_LABEL);

    private final Button removePredecessorButton = new Button(
        ViewConstants.REMOVE_LABEL);

    public OrgUnitAddForm(final AdminToolApplication app,
        final OrgUnitService service,
        final Collection<OrganizationalUnit> allOrgUnits,
        final OrgUnitList orgUnitList) {
        this.app = app;
        this.service = service;
        this.allOrgUnits = allOrgUnits;
        this.orgUnitList = orgUnitList;
        init();
    }

    private void init() {
        setCompositionRoot(panel);
        panel.setContent(form);
        int labelWidth = 100;

        // Title
        titleField.setWidth("400px");
        form.addComponent(LayoutHelper.create(ViewConstants.TITLE_LABEL,
            titleField, labelWidth, true));
        // titleProperty = mapBinding("", titleField);

        // Description
        descriptionField.setWidth("400px");
        descriptionField.setRows(5);
        form.addComponent(LayoutHelper.create(ViewConstants.DESCRIPTION_LABEL,
            descriptionField, labelWidth, true));

        // Alternative Title
        alternativeField.setWidth("400px");
        form.addComponent(LayoutHelper.create(ViewConstants.ALTERNATIVE_LABEL,
            alternativeField, labelWidth, false));

        // identifier
        identifierField.setWidth("400px");
        form.addComponent(LayoutHelper.create(ViewConstants.IDENTIFIER_LABEL,
            identifierField, labelWidth, false));

        // Org Type
        orgTypeField.setWidth("400px");
        form.addComponent(LayoutHelper.create(ViewConstants.ORGANIZATION_TYPE,
            orgTypeField, labelWidth, false));

        // city
        cityField.setWidth("400px");
        form.addComponent(LayoutHelper.create("City", cityField, labelWidth,
            false));

        // Country
        countryField.setWidth("400px");
        form.addComponent(LayoutHelper.create("Country", orgTypeField,
            labelWidth, false));

        // Org Unit
        coordinatesField.setWidth("400px");
        form.addComponent(LayoutHelper.create(ViewConstants.COORDINATES_LABEL,
            coordinatesField, labelWidth, false));

        // Parent
        orgUnitListSelect.setRows(5);
        orgUnitListSelect.setWidth("400px");
        orgUnitListSelect.setNullSelectionAllowed(true);
        orgUnitListSelect.setMultiSelect(true);
        orgUnitListSelect.setImmediate(true);

        form.addComponent(LayoutHelper.create(
            ViewConstants.ORGANIZATION_UNITS_LABEL, new OrgUnitEditor(
                orgUnitListSelect, addOrgUnitButton, removeOrgUnitButton),
            labelWidth, 140, false, new Button[] { addOrgUnitButton,
                removeOrgUnitButton }));

        // predecessor
        objIds = new ArrayList<String>();
        for (final OrganizationalUnit ou : allOrgUnits) {
            objIds.add(ou.getObjid());
        }

        ListSelect predecessorsListSelect = new ListSelect("", objIds);
        predecessorsListSelect.setRows(VISIBLE_PARENTS);
        predecessorsListSelect.setMultiSelect(true);
        predecessorsListSelect.setVisible(false);
        form.addComponent(LayoutHelper.create(ViewConstants.PREDECESSORS_LABEL,
            predecessorsListSelect, labelWidth, 140, false, new Button[] {
                addPredecessorButton, removePredecessorButton }));

        // panel.addComponent(form);
        addFooter();
    }

    // titleField.setRequiredError("Please enter a " + ViewConstants.TITLE_ID);
    // descriptionField.setRequiredError("Please enter a "
    // + ViewConstants.DESCRIPTION_ID);

    // setWriteThrough(false);
    // setInvalidCommitted(false);

    private OrgUnitAddForm addParentsListSelect() {
        // TODO move it to a data source class.
        final List<String> objIds = new ArrayList<String>();
        objIds.add("");
        for (final OrganizationalUnit ou : allOrgUnits) {
            objIds.add(ou.getObjid());
        }
        final ListSelect parentListSelect = new ListSelect("Parents", objIds);

        parentListSelect.setRows(VISIBLE_PARENTS);
        parentListSelect.setNullSelectionAllowed(true);
        parentListSelect.setMultiSelect(true);
        parentListSelect.setWidth("200px");
        // addField(PARENTS_ID, parentListSelect);

        return this;
    }

    private OrgUnitAddForm predessorAddButton() {
        addPredecessorLink = new Button("Add Predecessor");
        addPredecessorLink.setStyleName(Button.STYLE_LINK);
        // addField(ViewConstants.PREDECESSORS_TYPE_ID, addPredecessorLink);
        addPredecessorLink.addListener(new AddPredecessorListener());
        return this;
    }

    private ComboBox predecessorTypeComboBox;

    private OrgUnitAddForm predecessorTypeComboBox() {
        predecessorTypeComboBox =
            new ComboBox(ViewConstants.PREDECESSORS_TYPE_LABEL,
                Arrays.asList(PredecessorForm.values()));
        // OrgUnitAddForm.this.addField(ViewConstants.PREDECESSORS_TYPE_ID,
        // predecessorTypeComboBox);
        predecessorTypeComboBox.setImmediate(true);
        predecessorTypeComboBox.setVisible(false);
        predecessorTypeComboBox
            .addListener(new PredecessorTypeComboBoxListener());
        return this;
    }

    private List<String> objIds;

    private boolean addPredecessor = false;

    private OrgUnitAddForm removeAddPredecessor() {
        removePredecessor = new Button("Remove");
        removePredecessor.setStyleName(Button.STYLE_LINK);
        // OrgUnitAddForm.this.addField("removeAddPredecessor",
        // removePredecessor);
        removePredecessor.addListener(new RemoveAddPredecessorListener());
        removePredecessor.setVisible(false);
        addPredecessor = false;
        return this;
    }

    private class RemoveAddPredecessorListener implements ClickListener {

        public void buttonClick(final ClickEvent event) {
            System.out.println("discard all changes.");
            predecessorTypeComboBox.discard();
            predecessorTypeComboBox.setVisible(false);

            predecessorsListSelect.discard();
            predecessorsListSelect.setVisible(false);

            removePredecessor.setVisible(false);

            addPredecessorLink.addListener(new AddPredecessorListener());
        }
    }

    private class PredecessorTypeComboBoxListener
        implements Property.ValueChangeListener {

        public void valueChange(
            final com.vaadin.data.Property.ValueChangeEvent event) {

            final PredecessorForm selected =
                (PredecessorForm) event.getProperty().getValue();

            if (selected.equals(PredecessorForm.FUSION)) {
                showMultipleSelection();
            }
            else {
                showMultipleSelection();
            }
        }

        private void showMultipleSelection() {
            predecessorsListSelect.setVisible(true);
        }
    }

    private class AddPredecessorListener implements ClickListener {

        public void buttonClick(final ClickEvent event) {
            predecessorTypeComboBox.setVisible(true);
            removePredecessor.setVisible(true);
            addPredecessorLink.removeListener(this);
            addPredecessor = true;
        }

    }

    private OrgUnitAddForm addFooter() {
        final HorizontalLayout footer = new HorizontalLayout();
        footer.setSpacing(true);
        footer.addComponent(save);
        footer.addComponent(cancel);
        // setFooter(footer);
        return this;
    }

    public void buttonClick(final ClickEvent event) {
        final Button source = event.getButton();
        if (source == save) {
            // setValidationVisible(true);
            saveUserInput();
        }
        else if (source == cancel) {
            // discard();
            app.showOrganizationalUnitView();
        }
        else {
            throw new RuntimeException("Unknown button.");
        }
    }

    @SuppressWarnings("unchecked")
    private void saveUserInput() {
        // TODO this is crazy. Data binding to the rescue for later.
        // final String title = (String) getField(TITLE_ID).getValue();
        // final String description = (String)
        // getField(DESCRIPTION_ID).getValue();
        // final String identifier = (String)
        // getField(IDENTIFIER_ID).getValue();
        // final String alternative = (String)
        // getField(ALTERNATIVE_ID).getValue();
        // final String orgType = (String) getField(ORG_TYPE_ID).getValue();
        // final String country = (String) getField(COUNTRY_ID).getValue();
        // final String city = (String) getField(CITY_ID).getValue();
        // final String coordinates =
        // (String) getField(ViewConstants.COORDINATES_ID).getValue();
        // final Set<String> parents =
        // (Set<String>) getField(PARENTS_ID).getValue();

        Set<String> predecessors = null;
        PredecessorForm predecessorType = null;

        // TODO fix this! due possible bug in Vaadin.
        if (addPredecessor) {
            System.out.println("adding");
            // predecessors =
            // (Set<String>) getField(ViewConstants.PREDECESSORS_ID)
            // .getValue();
            // predecessorType =
            // (PredecessorForm) getField(ViewConstants.PREDECESSORS_TYPE_ID)
            // .getValue();

            System.out.println("type: " + predecessorType);

            for (final String predecessor : predecessors) {
                System.out.println("pre: " + predecessor);
            }
        }
        // try {
        // if (isValid()) {
        // final OrganizationalUnit createdOrgUnit =
        // storeInRepository(new OrgUnitFactory()
        // .create(title, description).parents(parents)
        // .predecessors(predecessors, predecessorType)
        // .identifier(identifier).alternative(alternative)
        // .orgType(orgType).country(country).city(city)
        // .coordinates(coordinates).build());
        // commit();
        // updateOrgUnitTableView(createdOrgUnit);
        // app.showOrganizationalUnitView();
        // }
        // }
        // TODO refactor exception handling
        // catch (final EscidocException e) {
        // if (e instanceof
        // de.escidoc.core.client.exceptions.application.invalid.InvalidStatusException)
        // {
        // ((ListSelect) getField(ViewConstants.PREDECESSORS_ID))
        // .setComponentError(new UserError(e.getHttpStatusMsg()));
        // System.out.println(e.getHttpStatusMsg());
        // }
        // e.printStackTrace();
        // }
        // catch (final InternalClientException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        // catch (final TransportException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        // catch (final ParserConfigurationException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        // catch (final SAXException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        // catch (final IOException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
    }

    private OrganizationalUnit storeInRepository(
        final OrganizationalUnit orgUnit) throws EscidocException,
        InternalClientException, TransportException {
        return service.create(orgUnit);
    }

    private void updateOrgUnitTableView(final OrganizationalUnit createdOrgUnit) {
        // TODO: fix me. Bad! everytime we do changes in the repository we have
        // to change model and the view
        // we should only have to change the model, and the view will get notify
        // and update it self.
        // Investigate how to use Vaadin's DataBinding
        allOrgUnits.add(createdOrgUnit);
        orgUnitList.addOrgUnit(createdOrgUnit);
    }

    @Override
    public void setReadOnly(final boolean readOnly) {
        super.setReadOnly(readOnly);
        save.setVisible(!readOnly);
        cancel.setVisible(!readOnly);
    }
}