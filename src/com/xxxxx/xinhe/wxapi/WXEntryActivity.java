package com.xxxxx.xinhe.wxapi;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.tencent.mm.sdk.constants.ConstantsAPI;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.xxxxx.mj.tools.ConstVar;
import com.xxxxx.mj.tools.Debugs;
import com.xxxxx.xinhe.EntryActivity;

public class WXEntryActivity extends Activity implements IWXAPIEventHandler{
	
	// IWXAPI 是第三方app和微信通信的openapi接口
    private IWXAPI api;
    
 // 用户换取access_token的code，仅在ErrCode为0时有效
    private String code = "";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        api = WXAPIFactory.createWXAPI(this, ConstVar.APP_ID);
        
        api.handleIntent(getIntent(), this);
    }

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		
		setIntent(intent);
        api.handleIntent(intent, this);
	}

	// 微信发送请求到第三方应用时，会回调到该方法
	@Override
	public void onReq(BaseReq req) {
		switch (req.getType()) {
		case ConstantsAPI.COMMAND_GETMESSAGE_FROM_WX:
			break;
		case ConstantsAPI.COMMAND_SHOWMESSAGE_FROM_WX:
			break;
		default:
			break;
		}
	}

	// 第三方应用发送到微信的请求处理后的响应结果，会回调到该方法
	@Override
	public void onResp(BaseResp resp) {
		Debugs.debug("onResp type = " + resp.getType() + " errCode = " + resp.errCode);
		if (!(resp instanceof SendAuth.Resp)) {
			finish();
			return;
		}
		if (resp.getType() == ConstantsAPI.COMMAND_SENDAUTH) {
			((EntryActivity) EntryActivity._instance).setWXLgFlg(true);
			switch (resp.errCode) {
				case BaseResp.ErrCode.ERR_OK:
					SendAuth.Resp sendResp = new SendAuth.Resp();
		 			sendResp = (SendAuth.Resp) resp;
		 			if (!sendResp.code.equals("")){
		 				code = sendResp.code;
		 				if(null != EntryActivity._instance){
		 					((EntryActivity) EntryActivity._instance).getCodeFromWX(code);
		 				}
		 				Debugs.debug("onResp get code：" + code);
		 			}
					break;
				case BaseResp.ErrCode.ERR_USER_CANCEL:
					((EntryActivity) EntryActivity._instance).globalHandler.sendEmptyMessage(701);
					break;
				case BaseResp.ErrCode.ERR_AUTH_DENIED:
					((EntryActivity) EntryActivity._instance).globalHandler.sendEmptyMessage(702);
					break;
				default:
					((EntryActivity) EntryActivity._instance).globalHandler.sendEmptyMessage(703);
					break;
			}
		} else if (resp.getType() == ConstantsAPI.COMMAND_SENDMESSAGE_TO_WX) {
			Debugs.debug("send msg to wx errCode = " + resp.errCode);
			switch (resp.errCode) {
			case BaseResp.ErrCode.ERR_OK:
	 			Debugs.debug("send msg to wx ok");
				break;
			case BaseResp.ErrCode.ERR_USER_CANCEL:
				Debugs.debug("send msg to wx cacel");
				break;
			default:
				break;
		}
		}
		finish();
//		int result = 0;
//		
//		switch (resp.errCode) {
//		case BaseResp.ErrCode.ERR_OK:
//			if (resp.getType() == ConstantsAPI.COMMAND_SENDAUTH) {
//				SendAuth.Resp sendResp = new SendAuth.Resp();
//	 			sendResp = (SendAuth.Resp) resp;
//	 			if (!sendResp.code.equals("")){
//	 				code = sendResp.code;
//	 				if(null != EntryActivity._instance){
//	 					((EntryActivity) EntryActivity._instance).getCodeFromWX(code);
//	 				}
//	 				Debugs.debug("onResp get code：" + code);
//	 			}
//			}
//			break;
//		case BaseResp.ErrCode.ERR_USER_CANCEL:
//			break;
//		case BaseResp.ErrCode.ERR_AUTH_DENIED:
//			break;
//		default:
//			break;
//		}
//		this.finish();
//		Toast.makeText(this, result, Toast.LENGTH_LONG).show();
	}
}