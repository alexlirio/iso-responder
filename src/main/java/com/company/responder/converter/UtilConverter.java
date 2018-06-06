package com.company.responder.converter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

import javax.xml.bind.DatatypeConverter;

import org.apache.log4j.Logger;
import org.jpos.iso.ISOBinaryField;
import org.jpos.iso.ISOBitMap;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOField;
import org.jpos.iso.ISOMsg;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class UtilConverter {
	
	private final static Logger log = Logger.getLogger(UtilConverter.class);
	
	public final static String ISO_REPONSES = "responses";
	public final static String ISO_FIELD_EQUALS = "equals";
	public final static String ISO_CONFIG_SLEEP = "CONFIG_SLEEP";
	public final static String ISO_CONFIG_FILTER = "CONFIG_FILTER";
	
	public static ISOMsg getISO(JSONObject json) {
		
		ISOMsg isoMsg = new ISOMsg();
		
		Iterator<?> iterator = json.keySet().iterator();
		while (iterator.hasNext()) {
			String key = (String)iterator.next();
			String value = json.get(key).toString();

			if (Pattern.compile("^([\\d]{1,3})([\\.]{1}[\\d]{1,3})*$").matcher(key).matches()) {
				//Key valid to ISO field
				try {
					isoMsg.set(key, value);
				} catch (ISOException e) {
					log.error(e.getClass().getSimpleName() + " in " + UtilConverter.class.getClass().getName() , e);
					e.printStackTrace();
				}
			} else {
				log. error(String.format("Invalid ISO Key(bit) format. Key(bit)=%s", key));
				new ISOException(String.format("Invalid ISO Key(bit) format. Key(bit)=%s", key)).printStackTrace();
			}
		}
		
		return isoMsg;
	}
	
	public static JSONObject getJSON(String filename) {
		JSONObject json = new JSONObject();
		try {
			json = new JSONObject(new String(Files.readAllBytes(Paths.get(filename))));
		} catch (JSONException | IOException e) {
			log.error(e.getClass().getSimpleName() + " in " + UtilConverter.class.getClass().getName() , e);
			e.printStackTrace();
		}
		return json;
	}

	public static JSONObject getJSON(ISOMsg isoMsg) {
		Map<String, String> fieldsMap = getIsoFieldsMap(isoMsg);
		return new JSONObject(fieldsMap);
	}

	@SuppressWarnings("rawtypes")
	private static Map<String, String> getIsoFieldsMap(ISOMsg isoMsg) {
		
		Map<String, String> fields = new TreeMap<String, String>();
		
		Iterator fieldsInterator = isoMsg.getChildren().entrySet().iterator();
		while (fieldsInterator.hasNext()) {
			Map.Entry me = (Map.Entry)fieldsInterator.next();
			if (me.getValue() instanceof ISOBitMap) {
				//BITMAP
				continue;
			} else if (me.getValue() instanceof ISOField) {
				//FIELD
				fields.put(String.valueOf(me.getKey()), String.valueOf(((ISOField)me.getValue()).getValue()));
			} else if (me.getValue() instanceof ISOBinaryField) {
				//BINARY FIELD
				fields.put(String.valueOf(me.getKey()), new String(DatatypeConverter.parseHexBinary(((ISOBinaryField)me.getValue()).toString())));
			} else if (me.getValue() instanceof ISOMsg) {
				//SUBFIELD
				fields.putAll(getIsoSubFieldsMap(String.valueOf(me.getKey()), ((ISOMsg)me.getValue()), fields));
			} else {
				log.error("ERROR: Field " + me.getKey() + " has a class not implemented. Class=" + me.getValue().getClass().getName());
			}
		}
		
		return fields;
	}
	
	@SuppressWarnings("rawtypes")
	private static Map<String, String> getIsoSubFieldsMap(String idField, ISOMsg isoMsg, Map<String, String> fields) {
		
		Iterator fieldsInterator = isoMsg.getChildren().entrySet().iterator();
		while (fieldsInterator.hasNext()) {
			Map.Entry me = (Map.Entry)fieldsInterator.next();
			if (me.getValue() instanceof ISOBitMap) {
				//BITMAP
				continue;
			} else if (me.getValue() instanceof ISOField) {
				//FIELD
				fields.put(idField + "." + String.valueOf(me.getKey()), String.valueOf(((ISOField)me.getValue()).getValue()));
			} else if (me.getValue() instanceof ISOBinaryField) {
				//BINARY FIELD
				fields.put(idField + "." + String.valueOf(me.getKey()), new String(DatatypeConverter.parseHexBinary(((ISOBinaryField)me.getValue()).toString())));
			} else if (me.getValue() instanceof ISOMsg) {
				//SUBFIELD
				fields.putAll(getIsoSubFieldsMap(idField + "." + String.valueOf(me.getKey()), ((ISOMsg)me.getValue()), fields));
			} else {
				log.error("ERROR: Field " + idField + "." + String.valueOf(me.getKey()) + " has a class not implemented. Class=" + me.getValue().getClass().getName());
			}
		}
		
		return fields;
	}
	
	public static JSONObject mergeJSONs(JSONObject jsonRequest, JSONObject jsonResponse) {
		ArrayList<String> keysToRemove = new ArrayList<String>(); 
		Iterator<?> iterator = jsonResponse.keys();
		while (iterator.hasNext()) {
			String key = (String)iterator.next();
			String value = jsonResponse.get(key).toString();
			if (value.equalsIgnoreCase(ISO_FIELD_EQUALS)) {
				if (jsonRequest.has(key)) {
					jsonResponse.put(key, jsonRequest.get(key));
				} else {
					keysToRemove.add(key);
				}
			}
		}
		for (String key : keysToRemove) {
			jsonResponse.remove(key);
		}
		return jsonResponse;
	}
	
	public static JSONObject getJSON(JSONObject jsonResponse, JSONObject jsonRequest) {
		JSONObject ret = null;
		Iterator<?> i = ((JSONArray)jsonResponse.get(UtilConverter.ISO_REPONSES)).iterator(); 
		while (i.hasNext()) {
			jsonResponse = (JSONObject)i.next();
			if (!jsonResponse.has(UtilConverter.ISO_CONFIG_FILTER)) {
				ret = jsonResponse;
			} else if (isJSONRequestLikePattern(jsonRequest, (JSONObject)jsonResponse.get(UtilConverter.ISO_CONFIG_FILTER))) {
				jsonResponse.remove(UtilConverter.ISO_CONFIG_FILTER);
				ret = jsonResponse;
			}
		}
		return ret;
	}
	
	private static boolean isJSONRequestLikePattern(JSONObject jsonRequest, JSONObject jsonRequestPattern) {
		boolean ret = false;
		Iterator<?> iterator = jsonRequestPattern.keySet().iterator();
		while (iterator.hasNext()) {
			String key = (String)iterator.next();
			String value = jsonRequestPattern.get(key).toString();
			if (jsonRequest.has(key) && jsonRequest.get(key).toString().matches(value)) {
				ret = true;
			} else {
				return false;
			}
		}
		return ret;
	}
	
}
