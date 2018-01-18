package com.appserver;

import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

public class CommonUserInfo {
	private String userid;
	private String token;
	private String openid;

	public static CommonUserInfo parseJson(String jsonString) {
		CommonUserInfo userInfo = null;
		if (!TextUtils.isEmpty(jsonString)) {
			try {
				JSONObject jsonObj = new JSONObject(jsonString);
				if (jsonObj.has("errcode")) {
//					
				}else{
					String userid = "";
					String token = "";
					String openid = "";
					if(jsonObj.has("userid")){
						userid = jsonObj.getString("userid");
					}
				     
					if(jsonObj.has("token")){
						token = jsonObj.getString("token");
					}
					
					if(jsonObj.has("openid")){
						openid = jsonObj.getString("openid");
					}

					userInfo = new CommonUserInfo();
					userInfo.setUserid(userid);
					userInfo.setToken(token);
					userInfo.setOpenid(openid);
				}
				
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		return userInfo;
	}

	public boolean isValid() {
		return !TextUtils.isEmpty(userid);
	}
	
	public String getUserid() {
		return userid;
	}

	public void setUserid(String userid) {
		this.userid = userid;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
	
	public String getOpenid() {
		return openid;
	}

	public void setOpenid(String openid) {
		this.openid = openid;
	}

}
