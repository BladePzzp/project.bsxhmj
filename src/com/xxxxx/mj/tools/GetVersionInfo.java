package com.xxxxx.mj.tools;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

public class GetVersionInfo {
	
	public String getInfo(){
		try{
			String strResult = null;
			HttpParams params = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(params, 10 * 1000);
			HttpConnectionParams.setSoTimeout(params, 10 * 1000);
			HttpGet httpRequest = new HttpGet(ConstVar.VERSION_CHECK);
			HttpResponse response = new DefaultHttpClient(params).execute(httpRequest);
			if(HttpStatus.SC_OK != response.getStatusLine().getStatusCode()){
				httpRequest.abort();
			}
			strResult = EntityUtils.toString(response.getEntity());
			return strResult;
		}catch(Exception e){
			Debugs.debug("err in getInfo :" + e.toString());
			return null;
		}
	}
}
