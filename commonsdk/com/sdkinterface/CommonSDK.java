package com.sdkinterface;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.appserver.CommonUserInfo;
import com.appserver.CommonUserInfoListener;
import com.appserver.CommonUserInfoTask;
import com.xxxxx.mj.tools.Debugs;

public class CommonSDK implements CommonUserInfoListener{
	public static final String MSGTAG = "GameSDKCommon";
	
	private static CommonSDK _instance = null;
	private ProgressDialog tip;
	private CommonUserInfo mUserInfo = null;
	private CommonUserInfoTask mUserInfoTask;
	
	
	private Context mLoginContext;
	private OnLoginStateListener loginListener;
	
	private Context mPayContext;
	private OnPayStateListener payListener;
	public  OnLoginToHallListener listener = null;
	public  OnPayToHallListener listener2 = null;
	
	private String mCode = "";
	private int mType;
	private Handler handler;
	
	public interface OnLoginToHallListener{
	public void onLoginSuccess(String uName,String Uid,String psw );
	public void onLoginFail();
    };
    public interface OnPayToHallListener{
    	public void onPaySuccess();
    	public void onPayFail();
        };
	
	public static CommonSDK getInstance(){
		if(null == _instance){
			_instance = new CommonSDK();
		}
		return _instance;
	}
	
	public CommonSDK(){
		initHandler();
	}
	public interface OnLoginStateListener{
		public void onLoginSuccess(String userid, String token, String openid);
		public void onLoginFailed();
	};
	
	public interface OnPayStateListener{
		public void onPaySuccess(String order);
		public void onPayFailed();
	};
	
	
	/**
	 * 执行登陆
	 */
	public void doLoginTask(Context context, String code, int type,OnLoginStateListener listener){
		Debugs.debug("blade doLoginTask code = " + code);
		this.mLoginContext = context;
		this.mCode = code;
		this.mType = type;
		this.loginListener = listener;
		
		handler.sendEmptyMessage(1000);
	}
	
	
	private void getUidSession(Context context, String code){
		
		Debugs.debug("blade getUidSession code = "+ code);
		if(code.equals("")){
			loginListener.onLoginFailed();
			Debugs.debug("blade getUidSession code err");
		}else{
			Debugs.debug("blade getUidSession code right");
			tip = new ProgressDialog(context);
			tip.setCanceledOnTouchOutside(false);
			tip.setMessage("登陆游戏中,请稍后...");
			tip.show();
			
			_instance.mUserInfoTask = CommonUserInfoTask.newInstance();
			Debugs.debug("blade getUidSession 1");
			
			Log.d(MSGTAG, "GetUidSession mUserInfoTask =" + mUserInfoTask);
			_instance.mUserInfoTask.doRequest(context, code, mType,this);
			Debugs.debug("blade getUidSession 2");
		}
	}
	
	 
	public void onGotUserInfo(CommonUserInfo userInfo) {
		// TODO Auto-generated method stub
		if (userInfo != null && userInfo.isValid()) {
			Debugs.debug("登陆成功");
			if(tip!=null){
				tip.dismiss();
				tip = null;
			}
			Debugs.debug("blade onGotUserInfo success");
            this.mUserInfo = userInfo;
            String userid = this.mUserInfo.getUserid();
            String token = this.mUserInfo.getToken();
            String openid = this.mUserInfo.getOpenid();
            this.loginListener.onLoginSuccess(userid, token, openid);
        } else {
        	Debugs.debug("blade onGotUserInfo failed");
        	if(tip!=null){
    			tip.dismiss();
    			tip = null;
    		}
        	Debugs.debug("登陆失败");
        	this.loginListener.onLoginFailed();
        }
	}

	 
	public void initLoginContext(Context context)
	{
		mLoginContext = context;
		
		
		Log.d(MSGTAG, "initLoginContext context = " + context);
	}
	public void initHandler(){
		
		handler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				
				
				switch (msg.what) {
				case 1000:
					getUidSession(mLoginContext,mCode);
					break;
				
				default:
					break;
				}
			}
		};
	}
}
