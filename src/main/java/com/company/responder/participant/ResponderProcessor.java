package com.company.responder.participant;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Properties;
import java.util.UUID;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOSource;
import org.jpos.iso.ISOUtil;
import org.jpos.q2.QBeanSupport;
import org.jpos.transaction.Context;
import org.jpos.transaction.ContextRecovery;
import org.jpos.transaction.TransactionParticipant;
import org.json.JSONObject;

import com.company.responder.context.CONTEXT;
import com.company.responder.converter.UtilConverter;

public class ResponderProcessor extends QBeanSupport implements TransactionParticipant, ContextRecovery {
	
	private boolean useJsonFileResponse;
	
	public Serializable recover(long id, Serializable context, boolean commit) {
		log.info(" - - - - -  recover ResponderProcessor");
		return null;
	}

	public void commit(long id, Serializable context) {
		log.info(" - - - - -  commit ResponderProcessor");
	}
	
	public void abort(long id, Serializable context) {
		log.info(" - - - - -  abort ResponderProcessor");
	}
	
	public int prepare(long id, Serializable context) {
		
		log.info(" - - - - -  prepare ResponderProcessor");
		
		Context myContext = (Context)context;
		ISOMsg isoRequest = (ISOMsg)myContext.get(CONTEXT.ISOMSG);
		ISOSource isoSource = (ISOSource)myContext.get(CONTEXT.ISOSOURCE);
		ISOMsg isoResponse = null;
		
		//JSON response configurations
		Integer configDelayResponse = null;
		
		String transact_id = UUID.randomUUID().toString();
		log.info(String.format("status=parser_init transaction=%s", transact_id));
		
		JSONObject jsonRequest = UtilConverter.getJSON(isoRequest);
		log.info(String.format("JSON REQUESTED: %s", jsonRequest));
		
		Properties prop = new Properties();
		InputStream input = null;
		
		try {
			input = new FileInputStream("cfg/config.properties");
			prop.load(input);
			useJsonFileResponse = Boolean.parseBoolean(prop.getProperty("use_json_file_response"));
		} catch (IOException e) {
			log.error(e.getClass().getSimpleName() + " in " + this.getClass().getSimpleName() , e);
			e.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					log.error(e.getClass().getSimpleName() + " in " + this.getClass().getSimpleName() , e);
					e.printStackTrace();
				}
			}
		}
		log.info(String.format("Use JSON File Response = %s", useJsonFileResponse));
		
		if (useJsonFileResponse) {
			
			String isoHeader = null;
			
			JSONObject jsonResponse = UtilConverter.getJSON("cfg/responses.json");
			log.info(String.format("JSON File Response Content = %s", jsonResponse));
			
			//Get the correct "JSON Response" from "JSON Response File"
			jsonResponse = UtilConverter.getJSON(jsonResponse, jsonRequest);
			
			//Merge fields to response
			jsonResponse = UtilConverter.mergeJSONs(jsonRequest, jsonResponse);
			
			//Replace TAG fields 
			jsonResponse = UtilConverter.replaceTagFields(jsonRequest, jsonResponse);
			
			//Set JSON response configurations
			configDelayResponse = (Integer)jsonResponse.opt(UtilConverter.ISO_CONFIG_SLEEP) == null ? 0 : (Integer)jsonResponse.remove(UtilConverter.ISO_CONFIG_SLEEP);
			log.info(String.format("JSON Response Content = %s", jsonResponse));
			
			isoHeader = (String)jsonResponse.remove("header");
			
			isoResponse = UtilConverter.getISO(jsonResponse);
			
			//Set ISO Header response
			if (isoHeader != null) {
				if (isoRequest.getHeader() == null || ISOUtil.str2bcd(isoHeader, true).length != isoRequest.getHeader().length) {
					log.warn("JSON Response with 'header' length different of ISO Request. ISO Header from Request will be use.");
					isoResponse.setHeader(isoRequest.getHeader());
				} else {
					isoResponse.setHeader(ISOUtil.str2bcd(isoHeader, true));
				}
			} else {
				log.info("JSON Response without 'header'. ISO Header Request will be use.");
				isoResponse.setHeader(isoRequest.getHeader());
			}
			
		} else {
			isoResponse = isoRequest;
			try {
				isoResponse.setResponseMTI();
			} catch (ISOException e) {
				log.error(e.getClass().getSimpleName() + " in " + this.getClass().getSimpleName() , e);
				e.printStackTrace();
			}
		}
		
		log.info(String.format("JSON RESPOND: %s", UtilConverter.getJSON(isoResponse).toString()));
		
		try {
			//Verify "delayResponse"
			log.info(String.format("Respond delay is %s milliseconds.", configDelayResponse));
			Thread.sleep(configDelayResponse);
			
			isoSource.send(isoResponse);
			((Context) context).checkPoint("INI - SendResponse");
		} catch (IOException | ISOException | InterruptedException e) {
			log.error(e.getClass().getSimpleName() + " in " + this.getClass().getSimpleName() , e);
			e.printStackTrace();
			return ABORTED;
		}

		return PREPARED;
		
	}

}
