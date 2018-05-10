package com.company.responder.rest.listener;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.jpos.iso.ISOPackager;

import com.company.responder.converter.UtilConverter;
import com.company.responder.rest.RestListener;


@Path("/responder-rest-listener")
public class ResponderRestListener extends RestListener {
	
	private final static Logger log = Logger.getLogger(ResponderRestListener.class);
	
	public ResponderRestListener(ISOPackager packager) {
		super(packager);
	}

	@GET
    @Path("/response-file")
    public Response getResponseFile() {
		
		log.info("HTTP getResponseFile method.");
		try {
			log.info(String.format("JSON File Response = %s", UtilConverter.JSON_FILE_RESPONSE));
			String jsonResponse = new String(Files.readAllBytes(Paths.get(UtilConverter.JSON_FILE_RESPONSE)));
			log.info(String.format("JSON File Response Content = %s", jsonResponse));
			return Response.status(200).entity(new String(jsonResponse)).build();
		} catch (IOException e) {
			log.error(e.getClass().getSimpleName() + " in " + this.getClass().getSimpleName() , e);
			e.printStackTrace();
    		return Response.status(500)
    				.entity(e.getMessage())
    				.build();
		}
    }

	@POST
    @Path("/response-file")
    public Response setResponseFile(final String httpBodyRequest) {
		
		log.info("HTTP setResponseFile method.");
		try {
			log.info(String.format("JSON File Response = %s", UtilConverter.JSON_FILE_RESPONSE));
			String jsonResponse = new String(Files.readAllBytes(Paths.get(UtilConverter.JSON_FILE_RESPONSE)));
			log.info(String.format("Old JSON File Response Content = %s", jsonResponse));
			log.info(String.format("New JSON File Response Content = %s", httpBodyRequest));
			Files.write(Paths.get(UtilConverter.JSON_FILE_RESPONSE), httpBodyRequest.getBytes());
			return Response.status(201).entity(String.format("New JSON File Response '%s' saved.", UtilConverter.JSON_FILE_RESPONSE)).build();
		} catch (IOException e) {
			log.error(e.getClass().getSimpleName() + " in " + this.getClass().getSimpleName() , e);
			e.printStackTrace();
    		return Response.status(500)
    				.entity(e.getMessage())
    				.build();
		}
    }
	
}
