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
import java.util.Collections;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.util.HierarchicalContainer;

import de.escidoc.admintool.app.PropertyId;
import de.escidoc.admintool.domain.PublicStatus;
import de.escidoc.admintool.view.ViewConstants;
import de.escidoc.core.resources.Resource;
import de.escidoc.core.resources.common.reference.UserAccountRef;
import de.escidoc.core.resources.oum.OrganizationalUnit;
import de.escidoc.core.resources.oum.Parents;
import de.escidoc.core.resources.oum.Predecessors;

public class ResourceContainerImpl implements ResourceContainer {

    private static final Logger LOG = LoggerFactory.getLogger(ResourceContainerImpl.class);

    private final Collection<? extends Resource> topLevelResources;

    private HierarchicalContainer container;

    public ResourceContainerImpl(final Collection<? extends Resource> topLevelResources) {
        Preconditions.checkNotNull(topLevelResources, "topLevelResources is null: %s", topLevelResources);

        this.topLevelResources = topLevelResources;
        toHierarchicalContainer();
    }

    private Container toHierarchicalContainer() {
        Preconditions.checkNotNull(topLevelResources, "topLevelResources is null: %s", topLevelResources);

        if (topLevelResources.isEmpty()) {
            return createHierarchicalContainer();
        }
        else {
            createHierarchicalContainer();
            addContainerProperties();
            addTopLevel();
            sortByLatestModificationDate();
            return container;
        }
    }

    private void sortByLatestModificationDate() {
        final boolean[] sort = new boolean[1];
        sort[0] = false;
        container.sort(new String[] { PropertyId.LAST_MODIFICATION_DATE }, sort);
    }

    @Override
    public void addChildren(final Resource parent, final Collection<OrganizationalUnit> children) {
        Preconditions.checkNotNull(parent, "parent is null: %s", parent);
        Preconditions.checkNotNull(children, "children is null: %s", children);

        for (final OrganizationalUnit child : children) {
            addChild(parent, child);
        }
    }

    @Override
    public void addChild(final Resource parent, final Resource child) {
        Preconditions.checkNotNull(parent, "parent is null: %s", parent);
        Preconditions.checkNotNull(child, "child is null: %s", child);
        final Item item = container.addItem(child);

        if (alreadyInTree(item)) {
            // child has more that one parents, not yet supported.
            LOG.warn("Organizational Unit has more than one parent. Currently not supported.");
        }
        else {
            bind(item, child);
            markAsLeaf(child);
            setParentIfAny(parent, child);
        }
    }

    private boolean alreadyInTree(final Item item) {
        return item == null;
    }

    private void setParentIfAny(final Resource parent, final Resource child) {
        if (parent != null) {
            container.setChildrenAllowed(parent, true);
            container.setParent(child, parent);
        }
        else if (hasParent(child)) {
            throw new UnsupportedOperationException("Not yet implemented");
        }
    }

    private boolean hasParent(final Resource child) {
        if (child instanceof OrganizationalUnit) {
            return ((OrganizationalUnit) child).getParents() != null
                && ((OrganizationalUnit) child).getParents() != null
                && !((OrganizationalUnit) child).getParents().isEmpty();
        }
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private Container createHierarchicalContainer() {
        container = new HierarchicalContainer();
        addContainerProperties();
        return container;
    }

    private void addContainerProperties() {
        addGeneralProperties();
        if (isOrgUnit()) {
            addOrgUnitProperties();
        }
    }

    private void addGeneralProperties() {
        addObjectIdProperty(container);
        container.addContainerProperty(PropertyId.OBJECT_ID, String.class, ViewConstants.EMPTY_STRING);
        container.addContainerProperty(PropertyId.NAME, String.class, ViewConstants.EMPTY_STRING);
        container.addContainerProperty(PropertyId.DESCRIPTION, String.class, ViewConstants.EMPTY_STRING);
        container.addContainerProperty(PropertyId.CREATED_ON, DateTime.class, new DateTime());
        container.addContainerProperty(PropertyId.CREATED_BY, UserAccountRef.class, new UserAccountRef(""));
        container.addContainerProperty(PropertyId.LAST_MODIFICATION_DATE, DateTime.class, new DateTime());
        container.addContainerProperty(PropertyId.MODIFIED_BY, UserAccountRef.class, new UserAccountRef(""));
        addPublicStatusProperty();
        container.addContainerProperty(PropertyId.PUBLIC_STATUS_COMMENT, String.class, ViewConstants.EMPTY_STRING);
    }

    private boolean addPublicStatusProperty() {
        return container.addContainerProperty(PropertyId.PUBLIC_STATUS, PublicStatus.class, ViewConstants.EMPTY_STRING);
    }

    private void addObjectIdProperty(final Container container) {
        container.addContainerProperty(PropertyId.OBJECT_ID, String.class, ViewConstants.EMPTY_STRING);
    }

    private void addOrgUnitProperties() {
        container.addContainerProperty(PropertyId.PARENTS, Parents.class, new Parents());
        container.addContainerProperty(PropertyId.PREDECESSORS, Predecessors.class, Collections.EMPTY_SET);
    }

    private boolean isOrgUnit() {
        return true;
    }

    private void addTopLevel() {
        assert (topLevelResources != null) : "top level resources is null";

        for (final Resource topLevel : topLevelResources) {
            add(topLevel);
        }
    }

    @Override
    public void add(final Resource resource) {
        Preconditions.checkNotNull(resource, "resource is null: %s", resource);
        final Item item = container.addItem(resource);
        Preconditions.checkNotNull(item, "item is null: %s", item);

        markAsLeaf(resource);
        bind(item, resource);
    }

    private void bind(final Item item, final Resource resource) {
        Preconditions.checkNotNull(item, "item is null: %s", item);
        Preconditions.checkNotNull(resource, "resource is null: %s", resource);
        final OrganizationalUnit orgUnit = (OrganizationalUnit) resource;
        item.getItemProperty(PropertyId.OBJECT_ID).setValue(resource.getObjid());
        item.getItemProperty(PropertyId.NAME).setValue(orgUnit.getProperties().getName());
        item.getItemProperty(PropertyId.DESCRIPTION).setValue(orgUnit.getProperties().getDescription());
        item.getItemProperty(PropertyId.CREATED_ON).setValue(orgUnit.getProperties().getCreationDate());
        item.getItemProperty(PropertyId.CREATED_BY).setValue(orgUnit.getProperties().getCreatedBy());
        item.getItemProperty(PropertyId.LAST_MODIFICATION_DATE).setValue(orgUnit.getLastModificationDate());
        item.getItemProperty(PropertyId.MODIFIED_BY).setValue(orgUnit.getProperties().getModifiedBy());
        item.getItemProperty(PropertyId.PUBLIC_STATUS).setValue(
            PublicStatus.from(orgUnit.getProperties().getPublicStatus()));
        item.getItemProperty(PropertyId.PUBLIC_STATUS_COMMENT).setValue(
            orgUnit.getProperties().getPublicStatusComment());
        item.getItemProperty(PropertyId.PARENTS).setValue(orgUnit.getParents());
        item.getItemProperty(PropertyId.PREDECESSORS).setValue(orgUnit.getPredecessors());
    }

    private void markAsLeaf(final Resource topLevel) {
        final boolean hasChildren = hasChildren(topLevel);
        container.setChildrenAllowed(topLevel, hasChildren);
    }

    @SuppressWarnings("boxing")
    private boolean hasChildren(final Resource resource) {
        return ((OrganizationalUnit) resource).getProperties().getHasChildren();
    }

    @Override
    public int size() {
        return container.size();
    }

    @Override
    public Container getContainer() {
        return container;
    }

    @Override
    public void updateParent(final OrganizationalUnit child, final OrganizationalUnit newParent) {

        preconditions(child);

        if (isNotNull(newParent)) {
            final Resource oldParent = (Resource) container.getParent(child);

            container.setChildrenAllowed(newParent, true);
            container.setParent(child, newParent);
            if (!container.hasChildren(oldParent) || container.getChildren(oldParent) == null) {
                container.setChildrenAllowed(oldParent, false);
            }

        }
        else if (hasParent(child)) {
            throw new UnsupportedOperationException("Not yet implemented");
        }
    }

    private void preconditions(final Object child) {
        Preconditions.checkNotNull(child, "child is null: %s", child);
    }

    private boolean isNotNull(final OrganizationalUnit parent) {
        return parent != null;
    }

    @Override
    public void removeParent(final OrganizationalUnit child) {
        Preconditions.checkNotNull(child, "child is null: %s", child);

        final OrganizationalUnit parent = (OrganizationalUnit) container.getParent(child);

        container.setParent(child, null);
        if (!container.hasChildren(parent)) {
            container.setChildrenAllowed(parent, false);
        }
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("ResourceContainerImpl [");
        if (topLevelResources != null) {
            builder.append("topLevelResources=").append(topLevelResources).append(", ");
        }
        if (container != null) {
            builder.append("container=").append(container);
        }
        builder.append("]");
        return builder.toString();
    }

    @Override
    public void remove(final Resource resource) {
        Preconditions.checkNotNull(resource, "resource is null: %s", resource);

        final Object parent = container.getParent(resource);

        container.removeItem(resource);

        if (!container.hasChildren(parent) || container.getChildren(parent) == null) {
            container.setChildrenAllowed(parent, false);
        }
    }

    @Override
    public Item firstResourceAsItem() {
        return container.getItem(container.firstItemId());
    }

    @Override
    public Object firstResource() {
        return container.getIdByIndex(0);
    }

    @Override
    public boolean isEmpty() {
        return container.rootItemIds() == null || container.rootItemIds().isEmpty();
    }

    @Override
    public Item getItem(final Resource resource) {
        return container.getItem(resource);
    }

}