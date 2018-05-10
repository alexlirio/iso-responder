package com.company.responder.rest;

import org.jpos.iso.ISOPackager;
import org.jpos.util.LogSource;
import org.jpos.util.Logger;

public class RestListener implements LogSource {
	protected ISOPackager packager;
	protected Logger logger;
	protected String realm;

	public RestListener(ISOPackager packager) {
		this.packager = packager;
	}

	@Override
	public void setLogger(Logger logger, String realm) {
		this.logger = logger;
		this.realm = realm;
	}

	@Override
	public String getRealm() {
		return this.realm;
	}

	@Override
	public Logger getLogger() {
		return this.logger;
	}

}