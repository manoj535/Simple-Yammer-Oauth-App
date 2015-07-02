package com.oauth;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

public class HttpUtils {
	public String get(String url, String accessToken) throws ClientProtocolException, IOException {
		return execute(new HttpGet(url), accessToken);
	}
	
	public String post(String url, Map<String,String> formParameters,String accessToken) 
							throws ClientProtocolException, IOException {	
		HttpPost request = new HttpPost(url);
			
		List <NameValuePair> nvps = new ArrayList <NameValuePair>();
		
		for (String key : formParameters.keySet()) {
			nvps.add(new BasicNameValuePair(key, formParameters.get(key)));	
		}

		request.setEntity(new UrlEncodedFormEntity(nvps));
		
		return execute(request, accessToken);
	}
	
	private String execute(HttpRequestBase request, String accessToken) throws ClientProtocolException, IOException {
		HttpClient httpClient = new DefaultHttpClient();
		HttpResponse response = httpClient.execute(request);
		Header authorizationHeader=null;
		authorizationHeader=new BasicHeader("Authorization","Bearer " + accessToken  );
		request.addHeader(authorizationHeader);		
		HttpEntity entity = response.getEntity();
	    String body = EntityUtils.toString(entity);

		if (response.getStatusLine().getStatusCode() != 200) {
			throw new RuntimeException("Expected 200 but got " + response.getStatusLine().getStatusCode() + ", with body " + body);
		}

	    return body;
	}
}
