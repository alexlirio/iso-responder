package com.company.responder.converter;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
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

import com.github.mustachejava.Code;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;


public class UtilConverter {
	
	private final static Logger log = Logger.getLogger(UtilConverter.class);
	
	public final static String ISO_REPONSES = "responses";
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
		HashMap<String, Object> scopes = new HashMap<String, Object>();
		StringWriter writer = new StringWriter();
		MustacheFactory mf = new DefaultMustacheFactory();
		Mustache mustache = mf.compile(new StringReader(jsonResponse.toString()), "mustacheResult");
		
		// Verify each 'mustache tag(example {{4}})' to replace value. 
		Code[] codes = mustache.getCodes();
		for (Code code : codes) {
		    if (code.getName() != null) {
	    		if (jsonRequest.has(code.getName())) {
	    			scopes.put(code.getName(), jsonRequest.get(code.getName()));
	    		}
		    }			
		}
		mustache.execute(writer, scopes);
		jsonResponse = new JSONObject(writer.toString());
        writer.flush();
		
		return jsonResponse;
	}
	
	public static JSONObject replaceTagFields(JSONObject jsonRequest, JSONObject jsonResponse) {
		Iterator<?> iterator = jsonResponse.keys();
		while (iterator.hasNext()) {
			String key = (String) iterator.next();
			String value = jsonResponse.get(key).toString();

			// TAG "<currency>"
			if (value.contains("<currency>")) {
				Pattern p = Pattern.compile("<currency>(.+?)</currency>");
				Matcher m = p.matcher(value);
				while (m.find()) {
					String tag = m.group(1);
					value = value.replace("<currency>" + tag + "</currency>", NumberFormat.getInstance(new Locale("pt", "BR")).format(Double.parseDouble(tag) / 100));
				}
				jsonResponse.put(key, value);
			}

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
