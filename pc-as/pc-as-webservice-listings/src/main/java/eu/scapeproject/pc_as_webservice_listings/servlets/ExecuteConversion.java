package eu.scapeproject.pc_as_webservice_listings.servlets;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ExecuteConversion extends HttpServlet {
	private static final long serialVersionUID = -5496168853778502358L;

	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		String serviceURL = request.getParameter("serviceURL"), fileURL = request.getParameter("fileURL");
		// System.out.println(serviceURL + "\n" + fileURL);
		if (serviceURL == null || fileURL == null) {
			try {
				response.sendError(400, "Must provide a web service url and a file url!");
			} catch (IOException e) {
			}
		} else {
			if (serviceURL.endsWith("/")) {
				serviceURL = serviceURL.substring(0, serviceURL.length() - 1);
			}
			int slashIndex = serviceURL.lastIndexOf("/");
			String serviceName = serviceURL.substring(slashIndex + 1);
			serviceName = serviceName.replaceFirst("^scape-", "");
			serviceName = serviceName.replaceFirst("-service$", "");
			response.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
			String redirectTo = serviceURL + "/services/" + serviceName + "/convert?input=" + fileURL;
			// System.out.println(redirectTo);
			response.setHeader("Location", redirectTo);
		}
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) {
		doGet(request, response);
	}
}
