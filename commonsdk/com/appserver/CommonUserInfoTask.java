package com.appserver;

import android.content.Context;
import android.util.Log;

import com.common.SdkHttpListener;
import com.common.SdkHttpTask;
import com.xxxxx.mj.tools.ConstVar;
import com.xxxxx.mj.tools.Debugs;
import com.xxxxx.mj.tools.MD5;

/***
 */
public class CommonUserInfoTask {

    private static final String TAG = "XMUserInfoTask";

    private SdkHttpTask sSdkHttpTask;
    
    public static CommonUserInfoTask newInstance(){
        return new CommonUserInfoTask();
     }

    public void doRequest(Context context, String code, int type, final CommonUserInfoListener listener) {
    	Debugs.debug("blade doRequest code ="+ code + " type = " + type);
    	String mstr = "";
    	if(1== type || 2 == type){
    		mstr = MD5.getMD5(ConstVar.APP_ID + code + ConstVar.KEY_LOGIN);
    	}else if (3== type || 4 == type){
    		mstr = MD5.getMD5(code + "once" + ConstVar.KEY_LOGIN);
    	}
    	String url = "";
    	switch (type) {
		case 1:
			url = ConstVar.APP_SERVER_URL_GET_USER + "appid=" + ConstVar.APP_ID + "&code=" + code +"&msg=" + mstr;
			break;
		case 2:
			url = ConstVar.APP_SERVER_URL_GET_USER_AUTO + "appid=" + ConstVar.APP_ID + "&openid=" + code +"&msg=" + mstr;
			break;
		case 3:
			url = ConstVar.APP_SERVER_URL_GET_USER_VISITOR + "msg=" + mstr;
			break;
		case 4:
			url = ConstVar.APP_SERVER_URL_GET_USER_VISITOR + "userid=" + code + "&msg=" + mstr;
			break;
		default:
			break;
		}

        if (sSdkHttpTask != null) {
        	Log.d(TAG, "cancel");
            sSdkHttpTask.cancel(true);
        }
        sSdkHttpTask = new SdkHttpTask(context);
        
        
        sSdkHttpTask.doGet(new SdkHttpListener() {
             public void onResponse(String response) {
                 Log.d(TAG, "onResponse=" + response);
                 Debugs.debug("blade doRequest onResponse = " + response);
                 CommonUserInfo userInfo = CommonUserInfo.parseJson(response);
                 listener.onGotUserInfo(userInfo);
                 sSdkHttpTask = null;
             }
             public void onCancelled() {
                 listener.onGotUserInfo(null);
                 sSdkHttpTask = null;
             }

        }, url);
        
        Debugs.debug("blade doRequest mstr = " + mstr + " url="+url);
    }

    public boolean doCancel() {
        return (sSdkHttpTask != null) ? sSdkHttpTask.cancel(true) : false;
    }

}
