package de.uni_leipzig.ubl.admintool.view.group;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.data.util.POJOContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.themes.BaseTheme;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.VerticalSplitPanel;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;

import de.escidoc.admintool.app.AdminToolApplication;
import de.escidoc.admintool.app.PropertyId;
import de.escidoc.admintool.service.internal.UserService;
import de.escidoc.admintool.view.EscidocPagedTable;
import de.escidoc.admintool.view.ViewConstants;
import de.escidoc.core.client.exceptions.EscidocClientException;
import de.escidoc.core.client.exceptions.EscidocException;
import de.escidoc.core.client.exceptions.InternalClientException;
import de.escidoc.core.client.exceptions.TransportException;
import de.escidoc.core.resources.Resource;
import de.escidoc.core.resources.aa.useraccount.Grant;
import de.escidoc.core.resources.aa.useraccount.UserAccount;
import de.escidoc.core.resources.aa.usergroup.Selector;
import de.escidoc.core.resources.aa.usergroup.SelectorType;
import de.escidoc.core.resources.aa.usergroup.Selectors;
import de.escidoc.core.resources.aa.usergroup.UserGroup;
import de.uni_leipzig.ubl.admintool.service.internal.GroupService;
import de.uni_leipzig.ubl.admintool.view.group.selector.InternalSelectorName;

public class GroupSummaryView extends CustomComponent {

	private static final long serialVersionUID = 2107531184136070196L;
	
	private static final int WINDOW_HEIGHT_INT = 90;

	private static final int WINDOW_WIDTH_INT = 60;
	
	private static final String WINDOW_HEIGHT = WINDOW_HEIGHT_INT + "%";
	
	private static final String WINDOW_WIDTH = WINDOW_WIDTH_INT + "%";
	
	// app and services
	private final AdminToolApplication app;
	
	private final GroupService groupService;
	
	private final UserService userService;
	
	// data
	private UserGroup userGroup;
	
	private POJOContainer<UserAccount> allUserAccountsContainer;
	
	private HierarchicalContainer allUserGroupContainer;
	
	private POJOContainer<Grant> allGrantsContainer;
	
	private String groupTitle;
	
	private int numDirectUAs = 0;
	
	private int numGroupUAs = 0;
	
	private int numAttributeUAs = 0;
	
	private int numTotalUAs = 0;
	
	private int numDirectRoles = 0;
	
	private int numInheritedRoles = 0;
	
	private Set<Selector> deadSelectors= new HashSet<Selector>();
	
	// components
	private final Window modalWindow = new Window();
	
	private final VerticalLayout root = new VerticalLayout();
	
	private final HorizontalLayout header = new HorizontalLayout();
	
	private final HorizontalLayout footer = new HorizontalLayout();
	
	private final HorizontalSplitPanel splitPanel = new HorizontalSplitPanel();
	
	private final VerticalSplitPanel rightSplitPanel = new VerticalSplitPanel();
	
	private final VerticalLayout leftPanel = new VerticalLayout();
	
	private final VerticalLayout topRightPanel = new VerticalLayout();
	
	private final VerticalLayout bottomRightPanel = new VerticalLayout();
	
	private final Table allUserAccounts = new Table();
	
	private final Tree allUserGroups = new Tree();
	
	private final Table allGrants = new Table();
	
	private final Button closeButton = new Button(ViewConstants.CLOSE, new CloseButtonListener());
	
	private final GroupTreeSelectListener groupTreeSelectListener = new GroupTreeSelectListener();
	
	private final List<Button> parentButtons = new ArrayList<Button>();
	
	private final ParentButtonListener parentButtonListener = new ParentButtonListener();
	
	
	public GroupSummaryView(final AdminToolApplication app, final GroupService groupService, final UserService userService,
			final UserGroup userGroup) {
		Preconditions.checkNotNull(app, "app is null: %s", app);
		Preconditions.checkNotNull(groupService, "groupService is null: %s", groupService);
		Preconditions.checkNotNull(userService, "userService is null: %s", userService);
		Preconditions.checkNotNull(userGroup, "userGroup is null: %s", userGroup);
		this.app = app;
		this.groupService = groupService;
		this.userService = userService;
		this.userGroup = userGroup;
		bindData();
	}
	
	
	private void init() {
		configure();
		addHeader();
		addSplitPanel();
		addUserAccounts();
		addSummary();
		addParents();
		addGroupTree();
		addGrants();
		addFooter();
		addErrors();
	}
	
	
	private void configure() {
		modalWindow.setModal(true);
		modalWindow.setWidth(WINDOW_WIDTH);
//		modalWindow.setHeight(WINDOW_HEIGHT);
		modalWindow.setCaption("Summary Group View");
		modalWindow.setContent(root);
		
		root.setMargin(true);
	}
	
	
	private void addSpace() {
		final Label space = new Label("<br /><br />", Label.CONTENT_XHTML);
		root.addComponent(space);
	}
	
	private void addHeader() {
		Label description = new Label("This is the summary of the group <b>" + groupTitle + "</b>. All values are inherited and represent the final group settings for user accounts and assigned roles.", Label.CONTENT_XHTML);
		
		header.addComponent(description);
		header.setComponentAlignment(description, Alignment.BOTTOM_LEFT);
		
		root.addComponent(header);
	}
	
	
	private void addSplitPanel() {
		rightSplitPanel.setLocked(true);
		rightSplitPanel.setSplitPosition(70);
		rightSplitPanel.setFirstComponent(topRightPanel);
		rightSplitPanel.setSecondComponent(bottomRightPanel);
		
		leftPanel.setHeight(100, UNITS_PERCENTAGE);
		
		topRightPanel.setMargin(false, true, true, true);
		bottomRightPanel.setHeight(100, UNITS_PERCENTAGE);
		
		splitPanel.setLocked(true);
		splitPanel.setHeight(600, UNITS_PIXELS);
		splitPanel.setSplitPosition(40);
		splitPanel.setFirstComponent(leftPanel);
		splitPanel.setSecondComponent(rightSplitPanel);
		
		addSpace();
		root.addComponent(splitPanel);
	}
	
	
	private void addUserAccounts() {
		allUserAccounts.setContainerDataSource(allUserAccountsContainer);
		allUserAccounts.setReadOnly(true);
		allUserAccounts.setSelectable(false);
		allUserAccounts.setSizeFull();
		allUserAccounts.setVisibleColumns(new String [] { PropertyId.NAME });
		allUserAccounts.setColumnHeader(PropertyId.NAME, "Assigned and inherited user accounts …");
		
		leftPanel.addComponent(allUserAccounts);
		leftPanel.setExpandRatio(allUserAccounts, 1.0f);
	}
	
	
	private void addSummary() {
		final HorizontalLayout hl = new HorizontalLayout();
		addUserSummary(hl);
		addRoleSummary(hl);
		topRightPanel.addComponent(hl);
	}
	
	private void addUserSummary(final HorizontalLayout hl) {
		final VerticalLayout vl = new VerticalLayout();
		final Label totalUserAccountLabel = new Label("Total User Accounts: " + numTotalUAs);
		final Label directUserAccountLabel = new Label("User Accounts from user account selectors: " + numDirectUAs);
		final Label groupUserAccountLabel = new Label("User Accounts from user group selectors: " + numGroupUAs);
		final Label attributeAccountLabel = new Label("User Accounts from user attribute selectors: " + numAttributeUAs + "<br /><br />", Label.CONTENT_XHTML);
		
		vl.setMargin(false, true, false, false);
		vl.addComponent(totalUserAccountLabel);
		vl.addComponent(directUserAccountLabel);
		vl.addComponent(groupUserAccountLabel);
		vl.addComponent(attributeAccountLabel);
		hl.addComponent(vl);
	}
	
	
	private void addRoleSummary(final HorizontalLayout hl) {
		final VerticalLayout vl = new VerticalLayout();
		final Label totalRolesLabel = new Label("Total Roles: " + (numDirectRoles + numInheritedRoles));
		final Label directRolesLabel = new Label("Direct Roles: " + numDirectRoles);
		final Label inheritedRolesLabel = new Label("Inherited Roles: " + numInheritedRoles);
		
		vl.setMargin(false, false, false, true);
		vl.addComponent(totalRolesLabel);
		vl.addComponent(directRolesLabel);
		vl.addComponent(inheritedRolesLabel);
		hl.addComponent(vl);
	}
	
	
	private void addParents() {
		final HorizontalLayout hl = new HorizontalLayout();
		final Label parentLabel = new Label("Parent(s):&nbsp;", Label.CONTENT_XHTML);
		final Label delim = new Label(",&nbsp;", Label.CONTENT_XHTML);
		int i = 1;
		
		hl.addComponent(parentLabel);
		for (Button parentButton : parentButtons) {
			parentButton.setStyleName(BaseTheme.BUTTON_LINK);
			parentButton.addListener(parentButtonListener);
			if (i > 1) {
				hl.addComponent(delim);
			}
			hl.addComponent(parentButton);
			i++;
		}
		topRightPanel.addComponent(hl);
	}
	
	
	private void addGroupTree() {
		allUserGroups.setCaption("Group Structure:");
		allUserGroups.setContainerDataSource(allUserGroupContainer);
		allUserGroups.expandItemsRecursively(userGroup.getXLinkTitle());
		allUserGroups.select(userGroup.getXLinkTitle());
		allUserGroups.setMultiSelect(false);
		allUserGroups.setNullSelectionAllowed(false);
		allUserGroups.setImmediate(true);
		allUserGroups.addListener(groupTreeSelectListener);
		
		topRightPanel.addComponent(allUserGroups);
	}
	
	
	private void addGrants() {
		allGrants.setContainerDataSource(allGrantsContainer);
		allGrants.setReadOnly(true);
		allGrants.setSelectable(false);	
		allGrants.setSizeFull();
		allGrants.setColumnHeader(PropertyId.X_LINK_TITLE, "Assigned and inherited roles …");
		
		bottomRightPanel.addComponent(allGrants);
		bottomRightPanel.setExpandRatio(allGrants, 1.0f);
	}
	
	
	private void addFooter() {
		footer.addComponent(closeButton);
		footer.setComponentAlignment(closeButton, Alignment.BOTTOM_CENTER);
		
		addSpace();
		root.addComponent(footer);
	}
	
	
	private void addErrors() {
		if (deadSelectors.size() > 0) {
			String selectorIds = "";
			for (Selector selector : deadSelectors) {
				selectorIds += "<br />→ '"+ selector.getObjid() + "', '" + selector.getName() + "', '" + selector.getContent() + "'";
			}
			modalWindow.showNotification("info", "There are dead selectors. You should check selectors with id, name and content: " + selectorIds, Notification.TYPE_ERROR_MESSAGE);
		}
	}

	
	public void setGroup(final String groupID) {
		if (groupID == null) {
			throw new IllegalArgumentException("userGroup must not be null.");
		}
		try {
			close();
			app.getGroupView().showSummaryView(groupService.getGroupById(groupID));
		} catch (EscidocClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public void show() {
		init();
		app.getMainWindow().addWindow(modalWindow);
	}
	
	
	public void close() {
		app.getMainWindow().removeWindow(modalWindow);
	}
	
	
	@SuppressWarnings("unchecked")
	private List<UserAccount> getUserAccounts() {
		List<UserAccount> rawUsers = new ArrayList<UserAccount>();
		List<UserAccount> finalUsers = new ArrayList<UserAccount>();
		
		// add direct user account selectors
		List<UserAccount> directUAs = getDirectUserAccountsFromGroup(userGroup);
		rawUsers.addAll(directUAs);
		numDirectUAs = directUAs.size();
		
		// add user accounts from group selectors
		List<UserAccount> groupUAs = (List<UserAccount>) removeDuplicates(getGroupUserAccountsFromGroup(userGroup));
		rawUsers.addAll(groupUAs);
		numGroupUAs = groupUAs.size();
		
		// TODO add user accounts from attribute selectors
		
		
		finalUsers = (List<UserAccount>) removeDuplicates(rawUsers);
		numTotalUAs = finalUsers.size();
		return finalUsers;
	}
	
	
	private List<? extends Resource> removeDuplicates(List<? extends Resource> list) {
		List<String> ids = new ArrayList<String>();
		List<Resource> listWithoutDuplicates = new ArrayList<Resource>();
		for (final Resource resource : list) {
			if (!ids.contains(resource.getObjid())) {
				listWithoutDuplicates.add(resource);
				ids.add(resource.getObjid());
			}
		}
		return listWithoutDuplicates;
	}
	

	private List<UserAccount> getDirectUserAccountsFromGroup(final UserGroup group) {
		List<UserAccount> userAccounts = new ArrayList<UserAccount>();
		
		if (group != null) {
			userAccounts.addAll(getUserAccountsFromSelectors(group.getSelectors()));
		}
		
		return userAccounts;
	}
	
	
	private List<UserAccount> getGroupUserAccountsFromGroup(final UserGroup group) {
		List<UserAccount> userAccounts = new ArrayList<UserAccount>();
		List<Selector> userGroupSelectors = getUserGroupSelectors(group);
		
		for (final Selector userGroupSelector : userGroupSelectors) {
			UserGroup selectorGroup;
			try {
				// get group
				selectorGroup = groupService.getGroupById(userGroupSelector.getContent());
				
				if (selectorGroup != null) {
					// get direct UAs from group
					userAccounts.addAll(getDirectUserAccountsFromGroup(selectorGroup));
					
					// get group UAs from group
					userAccounts.addAll(getGroupUserAccountsFromGroup(selectorGroup));
				}
				else {
					// user group doesn't exist, so add selector to dead selectors
					deadSelectors.add(userGroupSelector);
				}
			} catch (EscidocClientException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return userAccounts;
	}

	
	private Collection<UserAccount> getUserAccountsFromSelectors(final Selectors selectors) {
		List<UserAccount> users = new ArrayList<UserAccount>();
		
		if (selectors instanceof Collection<?>) {
			for (final Selector selector : selectors) {
				if (selector.getType().equals(SelectorType.INTERNAL) && selector.getName().equals(InternalSelectorName.USER_ACCOUNT.getXmlValue())) {
					try {
						UserAccount user = userService.getUserById(selector.getContent()); 
						// check if user account exists, because it is possible that a selector references a deleted user account
						// TODO May notify the user that there are dead references
						if (user != null) {
							users.add(user);
						}
						else {
							// user account doesn't exist, so add selector to dead selectors
							deadSelectors.add(selector);
						}
					} catch (EscidocClientException e) {
						// TODO handle exception!
					}
				}
			}
		} 
		
		return users;
	}
	
	
	private List<Selector> getUserGroupSelectors(final UserGroup group) {
		List<Selector> groupSelectors = new ArrayList<Selector>();
		final List<Selector> selectors = group.getSelectors();
		
		if (selectors instanceof Collection<?>) {
			for (final Selector selector : selectors) {
				if (selector.getType().equals(SelectorType.INTERNAL) && selector.getName().equals(InternalSelectorName.USER_GROUP.getXmlValue())) {
						groupSelectors.add(selector);
				}
			}
		} 
		
		return groupSelectors;
	}
	
	
	private List<UserGroup> getParentUserGroups(final UserGroup group) {
		return getParentUserGroups(group, true);
	}
	
	
	private List<UserGroup> getParentUserGroups(final UserGroup group, final boolean allParents) {
		List<UserGroup> parentGroups = new ArrayList<UserGroup>();
		try {
			// iterate over all user groups and check if group exists as group selector
			final Collection<UserGroup> allGroups = groupService.findAll();
			for (final UserGroup parent : allGroups) {
				final List<Selector> selectors = getUserGroupSelectors(parent);
				for (final Selector selector : selectors) {
					if (selector.getContent().equals(group.getObjid())) {
						// parent found, add group to list
						parentGroups.add(parent);
						// check next parental level
						if (allParents) {
							final List<UserGroup> nextParentalGroups = getParentUserGroups(parent);
							if (!nextParentalGroups.isEmpty()) {
								parentGroups.addAll(nextParentalGroups);
							}
						}
						break;
					}
				}
			}
			
		} catch (EscidocClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return parentGroups;
	}
	
	
	private List<Grant> getAllGrants(final UserGroup group) throws EscidocException, InternalClientException, TransportException {
		final List<Grant> grants = new ArrayList<Grant>();
		// add direct assigned Grants to the list
		grants.addAll(groupService.retrieveCurrentGrants(group.getObjid()));
		// bind size information
		numDirectRoles = grants.size();
		// add grants from parental groups to the list
		for (final UserGroup parent : getParentUserGroups(group)) {
			grants.addAll(groupService.retrieveCurrentGrants(parent.getObjid()));
		}
		
		final List<Grant> filteredGrants = new ArrayList<Grant>();
		// check all grants back for duplicates
		for (final Grant uncheckedGrant : grants) {
			boolean addThis = true;
			for (final Grant filteredGrant : filteredGrants) {
				/*
				 * grant is equal if role equals and assigned to equals (grant retrieval ensures that they are granted to actual object)
				 * 
				 * unscoped grant equality
				 * A1=0 & A2=0 & R1=R2
				 * 
				 * scoped grant equality
				 * A1=1 & A2=1 & A1=A2 & R1=R2
				 * 
				 * combined condition
				 * ((A1=0 & A2=0) | (A1=1 & A2=1 & A1=A2)) & R1=R2
				 */
				if ( ((
							uncheckedGrant.getProperties().getAssignedOn() == null 
							&& filteredGrant.getProperties().getAssignedOn() == null
						) 
						|| 
						(
							uncheckedGrant.getProperties().getAssignedOn() != null
							&& filteredGrant.getProperties().getAssignedOn() != null
							&& uncheckedGrant.getProperties().getAssignedOn().equals(filteredGrant.getProperties().getAssignedOn())
						)) 
						&& uncheckedGrant.getProperties().getRole().equals(filteredGrant.getProperties().getRole())) {
					// duplicate detected, switch value of addThis and leave inner loop
					addThis = false;
					break;
				}
			}
			if (addThis) {
				// add grant to filtered list
				filteredGrants.add(uncheckedGrant);
			}
		}
		
		// bind size information
		numInheritedRoles = grants.size() - numDirectRoles;

		return filteredGrants;
	}
	
	
	private void bindData() {
		groupTitle = userGroup.getXLinkTitle();
		bindUserAccounts();
		bindParents();
		bindGroupTree();
		bindGrants();
	}
	
	
	private void bindUserAccounts() {
		List<UserAccount> users = getUserAccounts();
		allUserAccountsContainer = new POJOContainer<UserAccount>(UserAccount.class, PropertyId.NAME);
		for (final UserAccount user : users) {
			allUserAccountsContainer.addPOJO(user);
		}
		allUserAccountsContainer.sort(new String[] {PropertyId.NAME}, new boolean[] {true});
	}
	
	
	private void bindParents() {
		List<UserGroup> parents = getParentUserGroups(userGroup, false);
		for (UserGroup parent : parents) {
			Button parentButton = new Button();
			parentButton.setCaption(parent.getXLinkTitle());
			parentButton.setData(parent.getObjid());
			parentButtons.add(parentButton);
		}
	}
	
	
	private void bindGroupTree() {
		allUserGroupContainer = new HierarchicalContainer();
		allUserGroupContainer.addContainerProperty(PropertyId.NAME, String.class, "");
		allUserGroupContainer.addContainerProperty(PropertyId.OBJECT_ID, String.class, "");
		
		allUserGroupContainer.addItem(userGroup.getXLinkTitle());
		allUserGroupContainer.getItem(userGroup.getXLinkTitle()).getItemProperty(PropertyId.NAME).setValue(userGroup.getXLinkTitle());
		allUserGroupContainer.getItem(userGroup.getXLinkTitle()).getItemProperty(PropertyId.OBJECT_ID).setValue(userGroup.getObjid());
		createGroupTree(userGroup);
		allUserGroupContainer.sort(new Object[] {PropertyId.NAME}, new boolean[] {true});
	}
	
	
	private void createGroupTree(final UserGroup parent) {
		List<Selector> selectors = getUserGroupSelectors(parent);
		if (selectors.isEmpty()) {
			allUserGroupContainer.setChildrenAllowed(parent.getXLinkTitle(), false);
		}
		else {
			for (final Selector selector : selectors) {
				UserGroup group;
				try {
					group = groupService.getGroupById(selector.getContent());
					if (group != null) {
						allUserGroupContainer.addItem(group.getXLinkTitle());
						allUserGroupContainer.getItem(group.getXLinkTitle()).getItemProperty(PropertyId.NAME).setValue(group.getXLinkTitle());
						allUserGroupContainer.getItem(group.getXLinkTitle()).getItemProperty(PropertyId.OBJECT_ID).setValue(group.getObjid());
						allUserGroupContainer.setParent(group.getXLinkTitle(), parent.getXLinkTitle());
						createGroupTree(group);
					}
					else {
						// TODO handle dead selectors, group can be null !!!
						String deadGroupInfo = "refered user group doesn't exist anymore (" + selector.getContent() + ")";
						allUserGroupContainer.addItem(deadGroupInfo);
						allUserGroupContainer.setParent(deadGroupInfo, parent.getXLinkTitle());
						allUserGroupContainer.setChildrenAllowed(deadGroupInfo, false);
					}
				} catch (EscidocClientException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	
	private void bindGrants() {
			List<Grant> grants;
			allGrantsContainer = new POJOContainer<Grant>(Grant.class, PropertyId.X_LINK_TITLE);
			try {
				grants = (List<Grant>) getAllGrants(userGroup);
				for (final Grant grant : grants) {
					allGrantsContainer.addPOJO(grant);
				}
			} catch (EscidocException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InternalClientException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TransportException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	
	private class CloseButtonListener implements Button.ClickListener {

		private static final long serialVersionUID = 218805084731560485L;

		@Override
		public void buttonClick(ClickEvent event) {
			app.getMainWindow().removeWindow(modalWindow);
		}
		
	}
	
	
	private class GroupTreeSelectListener implements ItemClickListener {

		private static final long serialVersionUID = 8113178115382201649L;

		@Override
		public void itemClick(ItemClickEvent event) {
			String groupID = event.getItem().getItemProperty(PropertyId.OBJECT_ID).toString();
			if (groupID != null && !groupID.isEmpty() && !groupID.equals(userGroup.getObjid())) {
				setGroup(groupID);
			}
		}
		
	}
	
	
	private class ParentButtonListener implements Button.ClickListener {

		private static final long serialVersionUID = 5807978674342765306L;

		@Override
		public void buttonClick(ClickEvent event) {
			// TODO Auto-generated method stub
			String groupID = (String) event.getButton().getData();
			if (groupID != null && !groupID.isEmpty() && !groupID.equals(userGroup.getObjid())) {
				setGroup(groupID);
			}
		}
		
	}

}