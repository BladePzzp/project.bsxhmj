package com.qiniu.sdk;

import java.util.HashMap;



import org.json.JSONObject;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.widget.Toast;

import com.qiniu.auth.Authorizer;
import com.qiniu.io.IO;
import com.qiniu.rs.CallBack;
import com.qiniu.rs.CallRet;
import com.qiniu.rs.PutExtra;
import com.qiniu.rs.UploadCallRet;
import com.xxxxx.mj.tools.ConstVar;
import com.xxxxx.mj.tools.Debugs;
import com.xxxxx.mj.tools.HttpUtils;
import com.xxxxx.mj.tools.HttpUtils.HttpCallBack;
import com.xxxxx.xinhe.EntryActivity;

public class QiniuSDK {
	public static final int PICK_PICTURE_RESUMABLE = 0;
	
	// upToken 这里需要自行获取. SDK 将不实现获取过程. 隔一段时间到业务服务器重新获取一次
	public static String uptoken = "anEC5u_72gw1kZPSy3Dsq1lo_DPXyvuPDaj4ePkN:zmaikrTu1lgLb8DTvKQbuFZ5ai0=:eyJzY29wZSI6ImFuZHJvaWRzZGsiLCJyZXR1cm5Cb2R5Ijoie1wiaGFzaFwiOlwiJChldGFnKVwiLFwia2V5XCI6XCIkKGtleSlcIixcImZuYW1lXCI6XCIgJChmbmFtZSkgXCIsXCJmc2l6ZVwiOlwiJChmc2l6ZSlcIixcIm1pbWVUeXBlXCI6XCIkKG1pbWVUeXBlKVwiLFwieDphXCI6XCIkKHg6YSlcIn0iLCJkZWFkbGluZSI6MTQ2NjIyMjcwMX0=";
	
	public static String bucketName;
	public static Authorizer auth = new Authorizer();
	private static QiniuSDK _instance = null;
	private Context mContext;
	private Handler handler;
	private EntryActivity act = null;
	public static QiniuSDK getInstance(){
		if(null == _instance){
			_instance = new QiniuSDK();
		}
		return _instance;
	}
	
	public QiniuSDK(){
		initHandler();
	}
	private void initHandler() {
		handler = new Handler(){

			@Override
			public void handleMessage(Message msg) {
				switch(msg.what){
				case 1:
					String str = (String) msg.obj;
					Toast.makeText(mContext, str, Toast.LENGTH_LONG).show();
					break;
				case 2:
					setUploadToken();
					Uri uri = Uri.parse((String) msg.obj);
					doUpload(uri);
					break;
				}
			}
			
		};
	}
	
	public void initQnSDK(Context context, EntryActivity activity)
	{
		Debugs.debug("initQnSDK context" + context);
		mContext = context;
		act = activity;
	}
	
	public void doGetUpToken(final String filePath) {
		Debugs.debug("doGetUpToken filePath" + filePath);
		HttpUtils httpUtils = new HttpUtils();
		httpUtils.get(ConstVar.getUptokenUrl, HttpUtils.UTF8, new HttpCallBack() {
			
			@Override
			public void onSuccess(String reg) {
				uptoken = reg;
				Debugs.debug("doGetUpToken onSuccess reg" + reg);
				Message msg = new Message();
				msg.what = 2;
				msg.obj = filePath;
				handler.sendMessage(msg);
			}
			
			@Override
			public void onFailure(String e) {
				Debugs.debug("doGetUpToken onFailure e" + e);
				Message msg = new Message();
				msg.what = 1;
				msg.obj = "网络有误,上传失败,请检查网络重试...";
				handler.sendMessage(msg);
			}
		});
	}

	// @gist upload
	volatile boolean uploading = false;
	/**
	 * 普通上传文件
	 * @param uri
	 */
	private void doUpload(Uri uri) {
		Debugs.debug("doUpload uri" + uri);
		if (uploading) {
			Debugs.debug("上传中，请稍后");
			return;
		}
		uploading = true;
		String key = IO.UNDEFINED_KEY; // 自动生成key
		PutExtra extra = new PutExtra();
		extra.params = new HashMap<String, String>();
		extra.params.put("x:a", "测试中文信息");
		Debugs.debug("上传中");
		// 返回 UploadTaskExecutor ，可执行cancel，见 MyResumableActivity
		IO.putFile(mContext, auth, key, uri, extra, new CallBack() {
			@Override
			public void onProcess(long current, long total) {
				int percent = (int)(current*100/total);
				Debugs.debug("上传中，current = " + current + " total=" + total + " percent = " + percent);
			}

			@Override
			public void onSuccess(UploadCallRet ret) {
				uploading = false;
				String key = ret.getKey();
				String redirect = ConstVar.downLoadUrl + key;
//				String redirect = "http://" + bucketName + ".qiniudn.com/" + key;
//				String redirect2 ="http://" + bucketName + ".u.qiniudn.com/" + key;
				Debugs.debug("上传成功，访问地址：redirect = " + redirect);
				act.sendUpAudioUrl(key);
			}

			@Override
			public void onFailure(CallRet ret) {
				uploading = false;
				Debugs.debug("上传失败，ret = " +  ret.toString());
			}
		});
	}
	// @endgist
	
	private void setUploadToken(){
		Debugs.debug("setUploadToken uptoken = " + uptoken);
		auth.setUploadToken(uptoken); 
		try {
			String str = uptoken.split(":")[2];
			String jsonStr = new String(Base64.decode(str, Base64.URL_SAFE | Base64.NO_WRAP), "utf-8");
			JSONObject json = new JSONObject(jsonStr);
			String scope = json.optString("scope");
			bucketName = scope.split(":")[0];
			Debugs.debug("QiniuSDK bucketName=" + bucketName);
		} catch (Exception e) {
			bucketName = "<bucketName>";
			e.printStackTrace();
		}
	}

}
