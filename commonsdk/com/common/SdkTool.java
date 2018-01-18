package com.common;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import android.util.Log;



public class SdkTool {
	public static InputStream getHttpResponse(String urlPath,String encoding){
		try{
			StringBuffer sb = new StringBuffer();
			
			String data = sb.toString();
			URL url;
			url = new URL(urlPath);
			HttpURLConnection conn;
			conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setRequestMethod("GET");
			conn.setUseCaches(false);
			conn.setInstanceFollowRedirects(true);
			conn.setConnectTimeout(6 * 1000);
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			conn.setRequestProperty("Connection", "Keep-Alive");
			conn.setRequestProperty("Charset", "UTF-8");
			DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
			dos.writeBytes(data);
			dos.flush();
			dos.close();
			if(200 == conn.getResponseCode()){
				return conn.getInputStream();
			}
		}catch (UnsupportedEncodingException e){
		}catch (MalformedURLException e) {
		}catch (ProtocolException e) {
		}catch (IOException e) {
		}
		return null;
	}
}
