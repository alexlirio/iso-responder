package com.company.responder.servlet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONObject;

@SuppressWarnings("serial")
public class FileServlet extends HttpServlet {

	private final static Logger log = Logger.getLogger(FileServlet.class);
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String fileParam = request.getParameter("file");
		log.info(String.format("FileServlet - GET - JSON File Response = %s", fileParam));
		
		try {
			String jsonResponse = new String(Files.readAllBytes(Paths.get(fileParam)));
			log.info(String.format("JSON File Response content = %s", new JSONObject(jsonResponse)));
			
			response.setContentType("application/json");
			response.setStatus(HttpServletResponse.SC_OK);
			response.getWriter().write(jsonResponse);
		} catch (NullPointerException | NoSuchFileException e) {
			log.error(e.getClass().getSimpleName() + " in " + this.getClass().getSimpleName(), e);
			response.setContentType("application/json");
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().write(String.format("{\"msg\":\"%s\"}", String.format(e.getClass().getSimpleName() + " in " + this.getClass().getSimpleName())));
		}
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String fileParam = request.getParameter("file");
		log.info(String.format("FileServlet - POST - JSON File Response = %s", fileParam));
		
		try {
			String jsonResponse = new String(Files.readAllBytes(Paths.get(fileParam)));
			log.info(String.format("JSON File Response old content = %s", new JSONObject(jsonResponse)));
			
			String bodyRequest = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
			log.info(String.format("JSON File Response new content = %s", new JSONObject(bodyRequest)));
			Files.write(Paths.get(fileParam), bodyRequest.getBytes());
			
			response.setContentType("application/json");
			response.setStatus(HttpServletResponse.SC_CREATED);
			response.getWriter().write("{\"msg\":\"JSON File Response saved.\"}");
		} catch (NullPointerException | NoSuchFileException e) {
			log.error(e.getClass().getSimpleName() + " in " + this.getClass().getSimpleName(), e);
			response.setContentType("application/json");
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().write(String.format("{\"msg\":\"File '%s' not found.\"}", fileParam));
		}
	}
	
}

