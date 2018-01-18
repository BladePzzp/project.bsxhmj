package com.xxxxx.mj.tools;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import android.R.integer;

public class HttpUtils {
	public interface HttpCallBack {
		public void onSuccess(String reg);
		public void onFailure(String e);
	}
	
	public final static String UTF8 = "UTF-8";
	public final static String GBK = "gb2312";
	
	private int connectTimeOut;
	public void setConnectTimeOut(int connectTimeOut) {
		this.connectTimeOut = connectTimeOut;
	}

	private int soTimeOut;
	public void setSoTimeOut(int soTimeOut) {
		this.soTimeOut = soTimeOut;
	}


	public HttpUtils() {
		// 默认超时时间为 3s
		connectTimeOut = 3 * 1000;
		soTimeOut = 3 * 1000;
	}
	

	public void get(final String url, String encoding, final HttpCallBack callback) {
		if(encoding == null){
			encoding = UTF8;
		}
		final String tempcode = encoding;
		new Thread() {
			@Override
			public void run() {
				String reg = Get(url, tempcode);
				int count = 0;
				while (reg == null) {
					if (count == 5) {
						break;
					}
					reg = Get(url, tempcode);
					count++;
				}
//				System.out.println(reg);
				if (reg == null) {
					callback.onFailure("time out");
				} else {
					callback.onSuccess(reg);
				}
			}
		}.start();
	}

	public void post(final String url, final Map<String, String> tmap, String encoding, final HttpCallBack callback) {
		if(encoding == null){
			encoding = UTF8;
		}
		final String tempcode = encoding;
		new Thread() {
			@Override
			public void run() {
				String reg = Post(url, tmap, tempcode);
				int count = 0;
				while (reg == null) {
					if (count == 5) {
						break;
					}
					reg = Post(url, tmap, tempcode);
					count++;
				}
				if (reg == null) {
					callback.onFailure("time out");
				} else {
					callback.onSuccess(reg);
				}
			}
		}.start();
	}

	private String Get(String urlPath, String encoding) {
		try {
			String strResult = null;
			HttpParams params = new BasicHttpParams();
//			HttpConnectionParams.setConnectionTimeout(params, 3 * 1000);
			HttpConnectionParams.setConnectionTimeout(params, connectTimeOut);
//			HttpConnectionParams.setSoTimeout(params, 3 * 1000);
			HttpConnectionParams.setSoTimeout(params, soTimeOut);
			
			HttpGet request = new HttpGet(urlPath);
			HttpClient client = new DefaultHttpClient(params);
			HttpResponse response = client.execute(request);
			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				request.abort();
			}
			strResult = EntityUtils.toString(response.getEntity(), encoding);
			return strResult;
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}

	private String Post(String urlPath, Map<String, String> params, String encoding) {
		try {
			String strResult = null;
			HttpParams hparams = new BasicHttpParams();
//			HttpConnectionParams.setConnectionTimeout(hparams, 3 * 1000);
			HttpConnectionParams.setConnectionTimeout(hparams, connectTimeOut);
//			HttpConnectionParams.setSoTimeout(hparams, 3 * 1000);
			HttpConnectionParams.setSoTimeout(hparams, soTimeOut);
			
			HttpPost request = new HttpPost(urlPath);
			List<NameValuePair> param = new ArrayList<NameValuePair>();
			for (Map.Entry<String, String> entry : params.entrySet()) {
				param.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
			}

			HttpEntity entity = new UrlEncodedFormEntity(param, "UTF-8");

			request.setEntity(entity);
			HttpClient client = new DefaultHttpClient(hparams);
			HttpResponse response = client.execute(request);
			System.out.println("HttpStatus = " + response.getStatusLine().getStatusCode());
//			SC_INTERNAL_SERVER_ERROR = 500;
			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				request.abort();
			}
			strResult = EntityUtils.toString(response.getEntity(), encoding);
//			System.out.println("strResult=" + strResult);
			return strResult;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
