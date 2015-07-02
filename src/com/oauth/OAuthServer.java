package com.oauth;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.common.collect.ImmutableMap;

public class OAuthServer {

	private Server server = new Server(8089);

	private String clientId="4U9LMyxNql8uwVusWytHfw";
	private String clientSecret="3wGDnQpuz6Auu3ZHHXYxUMmD8W6NrxoCsXLLfaxdZg";
	private String callbackUri="http://localhost:8089/callback";
	private String accessToken = null;
	
	public static void main(String[] args) throws Exception {
		new OAuthServer().startJetty();
	}
	
	public void startJetty() throws Exception {

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);
 
        // map servlets to endpoints
        context.addServlet(new ServletHolder(new SigninServlet()),"/signin");        
        context.addServlet(new ServletHolder(new CallbackServlet()),"/callback");        
        //initialize();
        server.start();
        server.join();
	}
	
	private void initialize() {
		String propFileName = "config.properties";
		Properties prop = new Properties();
		InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);

		try {
			if (inputStream != null) {
				prop.load(inputStream);
				inputStream.close();
			}
		}catch (IOException e) {
			e.printStackTrace();
		}
		clientId = prop.getProperty("client_id");
		clientSecret = prop.getProperty("client_secret");
		callbackUri = prop.getProperty("callback_uri");
	}

	class SigninServlet extends HttpServlet {
		@Override
		protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException,IOException {
			
			StringBuilder oauthUrl = new StringBuilder().append("https://www.yammer.com/oauth2/authorize")
			.append("?client_id=").append(clientId) 
			.append("&response_type=code")
			.append("&redirect_uri=").append(callbackUri); 
			response.sendRedirect(oauthUrl.toString());
		}	
	}
	
	class CallbackServlet extends HttpServlet {
		
		HttpUtils httpUtils = new HttpUtils();
		@Override
		protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException,IOException {
			
			// if the user denied access, we get back an error, ex
			// error=access_denied&state=session%3Dpotatoes			
			if (request.getParameter("error") != null) {
				response.getWriter().println(request.getParameter("error"));
				return;
			}
			
			// get the code that can be exchanged for a access token
			String code = request.getParameter("code");
			
			// get the access token by post to Yammer
			String body = httpUtils.post("https://www.yammer.com/oauth2/access_token", ImmutableMap.<String,String>builder()
					.put("code", code)
					.put("client_id", clientId)
					.put("client_secret", clientSecret)
					.put("grant_type", "authorization_code").build(),accessToken);
			JSONObject jsonObject = null;
			
			try {
				jsonObject = (JSONObject) new JSONParser().parse(body);
			} catch (ParseException e) {
				throw new RuntimeException("Unable to parse json " + body);
			}
			
			
			//JSONObject accessTokenObj = (JSONObject)jsonObject.get("access_token");
			//String accessToken = (String)accessTokenObj.get("token");
			
			// you may want to store the access token in session
			//req.getSession().setAttribute("access_token", accessToken);
			
			// get some info about the user with the access token
			/*String json = get(new StringBuilder
					("https://www.yammer.com/api/v1/users/current.json")
					.toString(),accessToken);*/
			
			response.getWriter().println(jsonObject);
		}	
	}
	
}
