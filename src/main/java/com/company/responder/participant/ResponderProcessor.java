package com.company.responder.participant;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Properties;
import java.util.UUID;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOFilter.VetoException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOSource;
import org.jpos.iso.ISOUtil;
import org.jpos.q2.QBeanSupport;
import org.jpos.transaction.Context;
import org.jpos.transaction.ContextRecovery;
import org.jpos.transaction.TransactionParticipant;
import org.json.simple.JSONObject;

import com.company.responder.context.CONTEXT;
import com.company.responder.converter.UtilConverter;

public class ResponderProcessor extends QBeanSupport implements TransactionParticipant, ContextRecovery {
	
	private boolean hasJsonFileResponse;
	private String jsonFileResponse;
	
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
		
		String transact_id = UUID.randomUUID().toString();
		log.info(String.format("status=parser_init transaction=%s", transact_id));
		
		Properties prop = new Properties();
		InputStream input = null;
		
		try {
			input = new FileInputStream("cfg/config.properties");
			prop.load(input);
			jsonFileResponse = prop.getProperty("json_file_response");
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
		
		hasJsonFileResponse = jsonFileResponse != null && jsonFileResponse.trim().length() > 0;
		
		if (hasJsonFileResponse) {
			log.info(String.format("JSON File Response = %s", jsonFileResponse));
		}
		
		log.info(String.format("JSON REQUESTED: %s", UtilConverter.getJSON(isoRequest).toString()));
		
		if (hasJsonFileResponse) {
			String isoHeader = null;
			JSONObject jsonResponse = UtilConverter.getJSON(jsonFileResponse);
			
			if (jsonResponse.containsKey("header")) {
				isoHeader = (String)jsonResponse.get("header");
				jsonResponse.remove("header");
			}
			
			isoResponse = UtilConverter.getISO(jsonResponse);
			
			if (isoHeader != null) {
				if (isoRequest.getHeader() == null || ISOUtil.str2bcd(isoHeader, true).length != isoRequest.getHeader().length) {
					log.warn("JSON File Response with 'header' length different of ISO Request. ISO Header from ISO Request used for ISO Response.");
					isoResponse.setHeader(isoRequest.getHeader());
				} else {
					isoResponse.setHeader(ISOUtil.str2bcd(isoHeader, true));
				}
			} else {
				log.info("JSON File Response without 'header'. ISO Header from ISO Request used for ISO Response.");
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
		
		log.info(String.format("JSON RESPONSED: %s", UtilConverter.getJSON(isoResponse).toString()));
		
		try {
			isoSource.send(isoResponse);
			((Context) context).checkPoint("INI - SendResponse");
		} catch (VetoException e) {
			e.printStackTrace();
			return ABORTED;
		} catch (IOException e) {
			e.printStackTrace();
			return ABORTED;
		} catch (ISOException e) {
			e.printStackTrace();
			return ABORTED;
		}

		return PREPARED;
		
	}

}
