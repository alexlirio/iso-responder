package com.company.responder.context;

/**
 * Context abstraction, containing the properties being held during a single
 * transaction processing.
 */
public class CONTEXT {

	/** The ISO message. */
	public static final String ISOMSG = "ISOMSG";

	/** Original ISO to be used on abort flow. */
	public static final String ISOSOURCE = "ISOSOURCE";

}
