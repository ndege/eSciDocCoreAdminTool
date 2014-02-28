package de.uni_leipzig.ubl.admintool.service.internal;

import de.escidoc.admintool.service.internal.ServiceFactory;
import de.escidoc.admintool.view.EscidocServiceLocation;
import de.escidoc.core.client.exceptions.InternalClientException;

public class UBLServiceFactory extends ServiceFactory {

    private final String serviceUri;

    private final String token;
    
    public UBLServiceFactory(final EscidocServiceLocation escidocServiceLocation, final String token) {
		super(escidocServiceLocation, token);
		// TODO Auto-generated constructor stub
		this.serviceUri = escidocServiceLocation.getUri();
		this.token = token;
	}
	
	public GroupService createGroupService() throws InternalClientException {
		return new GroupService(serviceUri, token);
	}

}