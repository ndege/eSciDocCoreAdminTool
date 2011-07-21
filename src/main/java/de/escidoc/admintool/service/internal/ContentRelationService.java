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
package de.escidoc.admintool.service.internal;

import java.util.Collection;

import de.escidoc.core.client.exceptions.EscidocClientException;
import de.escidoc.core.client.exceptions.EscidocException;
import de.escidoc.core.client.exceptions.InternalClientException;
import de.escidoc.core.client.exceptions.TransportException;
import de.escidoc.core.client.interfaces.ContentRelationHandlerClientInterface;
import de.escidoc.core.client.interfaces.base.HandlerService;
import de.escidoc.core.resources.Resource;

public class ContentRelationService extends AbstractEscidocService<ContentRelationHandlerClientInterface> {

    public ContentRelationService(final HandlerService client) {
        super(client);
    }

    @Override
    public Resource create(final Resource resource) throws EscidocException, InternalClientException,
        TransportException {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Resource findById(final String objid) throws EscidocClientException {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void update(final Resource resource) throws EscidocClientException {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    ContentRelationHandlerClientInterface getClient() {
        return (ContentRelationHandlerClientInterface) client;
    }

    @Override
    Collection<? extends Resource> findPublicOrReleasedResources() throws EscidocException, InternalClientException,
        TransportException {
        return getClient().retrieveContentRelationsAsList(withEmptyFilter());
    }

    @Override
    public Collection<? extends Resource> filterUsingInput(final String query) throws EscidocException,
        InternalClientException, TransportException {
        return getClient().retrieveContentRelationsAsList(userInputToFilter(query));
    }
}