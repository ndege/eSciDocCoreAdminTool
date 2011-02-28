package de.escidoc.admintool.view.resource;

import java.util.Collection;

import com.vaadin.data.Container;
import com.vaadin.data.Item;

import de.escidoc.core.resources.Resource;
import de.escidoc.core.resources.oum.OrganizationalUnit;

public interface ResourceContainer {

    void addChildren(Resource parent, Collection<OrganizationalUnit> children);

    void addChild(final Resource parent, final Resource child);

    int size();

    Container getContainer();

    void updateParent(OrganizationalUnit child, OrganizationalUnit parent);

    void removeParent(OrganizationalUnit child);

    void remove(Resource resource);

    void add(Resource created);

    Item firstResourceAsItem();

    Object firstResource();

    boolean isEmpty();

    Item getItem(Resource resource);

}