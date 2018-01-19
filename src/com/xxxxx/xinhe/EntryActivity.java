package com.xxxxx.xinhe;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;



import org.egret.egretframeworknative.EgretRuntime;
import org.egret.egretframeworknative.engine.EgretGameEngine;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.czt.mp3recorder.util.MP3Recorder;
import com.qiniu.sdk.QiniuSDK;
import com.sdkinterface.CommonSDK;
import com.sdkinterface.CommonSDK.OnLoginStateListener;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXImageObject;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.umeng.analytics.MobclickAgent;
import com.umeng.analytics.MobclickAgent.EScenarioType;
import com.usershop.xqtsdk.XqtSDK;
import com.xxxxx.mj.tools.ConstVar;
import com.xxxxx.mj.tools.Debugs;
import com.xxxxx.mj.tools.GDLocation;
import com.xxxxx.mj.tools.GetVersionInfo;
import com.xxxxx.mj.tools.HotUpdate;
import com.xxxxx.mj.tools.InfoControl;
import com.xxxxx.mj.tools.UpdateManager;
import com.xxxxx.mj.tools.Util;
import com.xxxxx.mj.tools.ConstVar.HotUpdateType;
import com.xxxxx.mj.tools.UpdateManager.UpdateListener;
import com.xxxxx.xinhe.appupdate.utils.UpdateService;

public class EntryActivity extends Activity implements UpdateListener {
    private interface IRuntimeInterface {
        public void callback(String message);
        // 因为遗留问题 callBack 也是接受的
    }
    private boolean engineInited = false;
    private EgretGameEngine gameEngine;
    private String egretRoot;
    private String loaderUrl;
    private String updateUrl;
    
    private HotUpdate hotUpdate = null;
    
 // IWXAPI 是第三方app和微信通信的openapi接口
    private IWXAPI api;
    
    public static Activity _instance = null;
    
    private Handler handler;
    
    String _SDCard = null;
	String path = ConstVar.CAPTION;
	
	private InfoControl infoControl;
	private Map<String,?> user;
	
	private InfoControl infoControl2;
	private Map<String,?> open;
	
	private String openid = "";
	private String userid = "";
	
	private int lgtype = 0; //0:微信登陆1:游客登陆
	
	private static final int THUMB_SIZE = 150;
	private static final String SDCARD_ROOT = Environment.getExternalStorageDirectory().getAbsolutePath();
	
	private ProgressDialog tip;
	private MP3Recorder mRecorder;
	private long startTime;
    private long endTime;
    
    private UpdateManager mUpdateManager;//更新管理器
    private byte loginState = 2;	//登陆状态:1--正在检测更新 2--正常状态 3--正在更新 4--预更新 5--下载完成
    private String server_version;		//服务器版本号
	private String low_version;			//最低版本号
	
	private int deskId = -1;
	
	//微信登陆标志，防止连点
	private boolean wxLoginFlag = true;
	//初始化数据完成标志
	private boolean initDataFlag = false;
	//加载login界面完成
	private boolean loadLoginFlag = false;
	//登陆大厅成功标志
	private boolean loginHallFlag = false;
	private ViewGroup viewGroup;
	private FrameLayout fLayout_load;
	//剪切板
	private ClipboardManager mClipboard;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Make_path();
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        getAppInfo();
        initActivity();
    }
    
    @Override
	protected void onNewIntent(Intent intent) {
    	Debugs.debug("onNewIntent");
		super.onNewIntent(intent);
//		initDataFromBrowser(intent);
		decodeScheme(intent);
	}
    
    private void getAppInfo() {
    	try {
			String pkName = this.getPackageName();
			ConstVar.VERSIONCODE = this.getPackageManager().getPackageInfo(pkName, 0).versionCode + "";
			Debugs.debug("getAppInfo ,versioncode = " + ConstVar.VERSIONCODE);
		} catch (Exception e) {
			// TODO: handle exception
			Debugs.debug("getAppInfo exception, e = " +  egretRoot.toString());
		}
    }
    
    //初始化Activity
    private void initActivity() {
    	initPm();
        //初始化获取浏览器打开app数据
        Intent intent = getIntent();
//	    initDataFromBrowser(intent);
	    decodeScheme(intent);
	    //友盟初始化
	    initUMeng();
	    _instance = this;
	    initHandler();
	    initLocalData();
		//初始化xqt SDK
		XqtSDK.getInstance().initSDK(EntryActivity.this);
        //初始化录音
		initAudioRecord();
		//初始化七牛SDKcontext
		QiniuSDK.getInstance().initQnSDK(this.getApplicationContext(),EntryActivity.this);
		//初始化高德定位
		GDLocation.getInstance().initLocation(EntryActivity.this);
		// 初始化剪切板
		initClipboard();
		//初始化热更新
		initHotUpdate();
    }
    
    private void initClipboard() {
    	Debugs.debug("initClipboard");
    	if (null == mClipboard) {
    		Debugs.debug("initClipboard1");
    		try {
        		mClipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
    		} catch (Exception e) {
    			// TODO: handle exception
    			Debugs.debug("initClipboard err: " + e.toString());
    		}
    	}
    }
    
    private void sendClipDataToJS(){
    	try {
    		String clpData = getClipboard();
			Debugs.debug("sendClipDataToJS clpData: " + clpData);
			if(!clpData.equals("")){
				JSONObject jsonSendObj = new JSONObject();
				jsonSendObj.put("a", 1);
				jsonSendObj.put("t", 17);
				JSONObject dValue = new JSONObject();
				dValue.put("str", clpData);
				jsonSendObj.put("d", dValue);
				String sendString = jsonSendObj.toString();
				sendMsgToJS(sendString);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Debugs.debug("Exception in sendClipDataToJS " + e.toString());
		}
    }
    
    private int setClipboard(String des) {
    	Debugs.debug("setClipboard");
    	if (mClipboard != null) {
    		Debugs.debug("setClipboard1");
    		int sdk = android.os.Build.VERSION.SDK_INT;
    		if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
    			mClipboard.setText(des);
    			Debugs.debug("setClipboard2");
    		} else {
    			ClipData clip = ClipData.newPlainText("simple text", des);
    			Debugs.debug("setClipboard3");
    			mClipboard.setPrimaryClip(clip);
    			Debugs.debug("setClipboard4");
    		}
    	}
    	return 0;
    }
    
    private String getClipboard() {
    	String retStr = "";
    	if (mClipboard != null) {
    		if (mClipboard.hasPrimaryClip()) {
    			ClipData clipData = mClipboard.getPrimaryClip();
    			int count = clipData.getItemCount();
    			if (count > 0) {
    				ClipData.Item item = clipData.getItemAt(0);
    				retStr = item.getText().toString();
    			}
    		}
    	}
    	return retStr;
    }
    
    private void initLocalData() {
    	//保存注册参数
  		if(null == infoControl){
  			 infoControl = new InfoControl(EntryActivity.this);
      	}
	    
	    user = infoControl.getName("user"); 
		if(user.containsKey("userid")){
			userid = infoControl.getValue("userid", "user");
		}
		
		Debugs.debug("initLocalData userid:" + userid);
		
		//保存注册参数
  		if(null == infoControl2){
  			 infoControl2 = new InfoControl(EntryActivity.this);
      	}
	    
	    open = infoControl2.getName("open"); 
		if(open.containsKey("openid")){
			openid = infoControl2.getValue("openid", "open");
		}
		
		Debugs.debug("initLocalData openid:" + openid);
    }
    private void initGameEngine(){
    	egretRoot = new File(getFilesDir(), ConstVar.EGRET_ROOT).getAbsolutePath();
        gameEngine = new EgretGameEngine();
    }
    //初始化热更新
    private void initHotUpdate() {
    	initGameEngine();
         if (!ConstVar.UseCustomHotUpdate) {
        	//初始化默认热更新
        	 initDefaultHotUpdate();
         }
         else {
         	//初始化自定义热更新
         	initCustomHotUpdate();
         }
    }
    
    //初始化默认热更新
    private void initDefaultHotUpdate() {
    	//TODO: DEBUG 使用 2
        setLoaderUrl(ConstVar.HOT_UPDATE_TYPE);
        // 设置游戏的选项  (set game options)
        HashMap<String, Object> options = getGameOptions();
        gameEngine.game_engine_set_options(options);
        // 设置加载进度条  (set loading progress bar)
        gameEngine.game_engine_set_loading_view(new GameLoadingView(this));
        // 创建Egret<->Runtime的通讯 (create pipe between Egret and Runtime)
        setInterfaces();
        // 初始化并获得渲染视图 (initialize game engine and obtain rendering view)
        gameEngine.game_engine_init(this);
        engineInited = true;
        View gameEngineView = gameEngine.game_engine_get_view();
        setContentView(gameEngineView);
        startGame();
    }
    
    /**
     * 启动游戏的时候在最顶层添加图层（原因启动游戏的过程中有段时间是黑屏）
     * */
    public void startGame(){
    	RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
    			RelativeLayout.LayoutParams.WRAP_CONTENT);
    	fLayout_load = new GameLoadingView(this);
    	//添加控件    
    	addContentView(fLayout_load, params); 
    }
    
    /**
     * 游戏启动结束
     * 隐藏最上层的图片，显示游戏
     * */
    public void gameStartEnd(){
    	if(!ConstVar.UseCustomHotUpdate) {
    		if (fLayout_load != null) fLayout_load.setVisibility(View.GONE);
    	}
    }
    
    //初始化自定义热更新
    private void initCustomHotUpdate() {
    	viewGroup = new FrameLayout(this);
    	fLayout_load = new GameLoadingView(this);
    	viewGroup.addView(fLayout_load);
    	setContentView(viewGroup);
    	
        hotUpdate = new HotUpdate(this, ConstVar.gameId);
        hotUpdate.doLoadGame();
    }
    
    //移除loadingView
    private void removeLoadingView() {
    	if(!ConstVar.UseCustomHotUpdate) {
    		return;
    	} else if (null == fLayout_load || null == viewGroup || 1 == viewGroup.getChildCount()) {
    		return;
    	}
    	viewGroup.removeView(fLayout_load);
    }
    
    //初始化微信api
    private void initWxAPI() {
    	Debugs.debug("initWxAPI APP_ID = " + ConstVar.APP_ID);
    	// 通过WXAPIFactory工厂，获取IWXAPI的实例
    	api = WXAPIFactory.createWXAPI(EntryActivity.this, ConstVar.APP_ID, true);
    	// 将该app注册到微信
	    api.registerApp(ConstVar.APP_ID); 
    }
    
    // 友盟初始化
    private void initUMeng() {
    	// 设置调试模式开关
    	MobclickAgent.setDebugMode(true);
  	    // SDK在统计Fragment时，需要关闭Activity自带的页面统计，
        // 然后在每个页面中重新集成页面统计的代码(包括调用了 onResume 和 onPause 的Activity)。
  	    MobclickAgent.openActivityDurationTrack(false);
		// 场景类型设置接口。
  	    MobclickAgent.setScenarioType(EntryActivity.this, EScenarioType.E_UM_NORMAL);
    }
    
    //获取屏幕长和宽，以及缩放比率
  	private void initPm(){
  		DisplayMetrics dm = new DisplayMetrics();    //获取屏幕分辨率
  		
  		this.getWindowManager().getDefaultDisplay().getMetrics(dm);
  		// 得到屏幕的长和宽
  		int screenWidth = dm.widthPixels; // 水平分辨率
  		int screenHeight = dm.heightPixels; // 垂直分辨率
  		
  		int tempHeight = screenHeight;
  		int tempWidth = screenWidth;
  		
  		if(tempHeight < tempWidth){       //若是横屏，则仍未横屏
  			screenHeight = tempHeight;
  			screenWidth = tempWidth;
  		}else{
  			screenHeight = tempWidth;     //否则将当前屏幕转为竖屏
  			screenWidth = tempHeight;
  		}
  		//得到缩放比率	
  		ConstVar.xZoom = (float) (screenWidth / ConstVar.defaultScreenWidth);
  		ConstVar.yZoom = (float) (screenHeight / ConstVar.defaultScreenHeight);
  		Debugs.debug("initPm screenWidth = " + screenWidth + " screenHeight = " + screenHeight);
  		Debugs.debug("initPm xZoom = " + ConstVar.xZoom + " yZoom = " + ConstVar.yZoom);
  		
  		ConstVar.screenWidth = screenWidth;
		ConstVar.screenHeight = screenHeight;
  	}
    
    private void Make_path() {
		try {
			File SDFile = android.os.Environment.getExternalStorageDirectory();	            
	        Log.v("loc","SDFile.getAbsolutePath():" + SDFile.getAbsolutePath());	            
	        
	        _SDCard = SDFile.getAbsolutePath() + File.separator ;
	        ConstVar.filePath = _SDCard + path + File.separator;
	        _SDCard = _SDCard + path + File.separator;
	        File f = new File(_SDCard);
			if (!f.exists()) {
				if (f.mkdirs()) {
					Log.v("loc", "In Make_path 创建目录成功    目录:" + _SDCard);
				}
			}
		} catch (Exception e) {
			Log.v("loc", "In Service Make_Path err:" + e.toString());
		}
	}
    
    public void runGameAfterHotUpdate(String _updateUrl, boolean cnsvrFlag) {
    	if(cnsvrFlag) {
    		loaderUrl = "";
            updateUrl = _updateUrl;
            HashMap<String, Object> options = getGameOptions();
            gameEngine.game_engine_set_options(options);
            setInterfaces();

            gameEngine.game_engine_init(this);
            engineInited = true;
            View gameEngineView = gameEngine.game_engine_get_view();
            viewGroup.addView(gameEngineView, 0);
    	} else {
    		Debugs.debug("无法连接服务器，请检查你的网络重试");
    		handler.sendEmptyMessage(101);
    	}
    }
    
    private void setInterfaces() {
        // Egret（TypeScript）－Runtime（Java）通讯
        // setRuntimeInterface(String name, IRuntimeInterface interface) 用于设置一个runtime的目标接口
        // callEgretInterface(String name, String message) 用于调用Egret的接口，并传递消息
    	gameEngine.setRuntimeInterface("sendToNative", new IRuntimeInterface() {
            @Override
             public void callback(String message) {
                 try{
                 	JSONObject jsonObj = new JSONObject(message);
                 	dealMsgFromJS(jsonObj);
                 }catch(Exception e) {
                 	e.printStackTrace();
                 }
             }
         });
    }

    private HashMap<String, Object> getGameOptions() {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(EgretRuntime.OPTION_EGRET_GAME_ROOT, egretRoot);
        options.put(EgretRuntime.OPTION_GAME_ID, ConstVar.gameId);
        options.put(EgretRuntime.OPTION_GAME_LOADER_URL, loaderUrl);
        options.put(EgretRuntime.OPTION_GAME_UPDATE_URL, updateUrl);
//        // 避免第一次启动一定会进行热更新的方法
//        options.put(EgretRuntime.OPTION_PUBLISH_ZIP, EGRET_PUBLISH_ZIP);
        if(ConstVar.CustomLoadingFlag) {
        	//添加此options 说明要手动关闭GameLoadingView
            options.put("customLoading",true);
        }
        if(ConstVar.bUsingPlugin){
        	String pluginConf = "{'plugins':[{'name':'androidca','class':'org.egret.egretframeworknative.CameraAudio','types':'jar,so'}]}";
			options.put(EgretRuntime.OPTION_GAME_GLVIEW_TRANSPARENT, "true");
	        options.put(EgretRuntime.OPTION_EGRET_PLUGIN_CONF, pluginConf);
        }
        return options;
    }

    private void setLoaderUrl(HotUpdateType mode) {
        switch (mode) {
        case LOCALMODE:
            // local DEBUG mode
            // 本地DEBUG模式，发布请使用0本地zip，或者1网络获取zip
            loaderUrl = "";
            updateUrl = "";
            break;
        case NETZIPMODE:
            // http request zip RELEASE mode, use permission INTERNET
            // 请求网络zip包发布模式，需要权限 INTERNET
//            loaderUrl = "http://www.example.com/" + EGRET_PUBLISH_ZIP;
//            updateUrl = "http://www.example.com/";
        	loaderUrl = ConstVar.UPDATE_URL;
        	updateUrl = "";
            break;
        default:
            // local zip RELEASE mode, default mode, `egret publish -compile --runtime native`
            // 私有空间zip包发布模式, 默认模式, `egret publish -compile --runtime native`
            loaderUrl = ConstVar.EGRET_PUBLISH_ZIP;
            updateUrl = "";
            break;
        }
    }
    
    private void sendMsgToJS(String sendString){
    	Debugs.debug("sendMsgToJS sendString = " + sendString);
		gameEngine.callEgretInterface("sendToJS", sendString);
	}
	
    private void dealMsgFromJS(JSONObject jsonObj){
		try {
			int action = jsonObj.getInt("a");
			int aType = jsonObj.getInt("t");
			JSONObject dValue = jsonObj.getJSONObject("d");
			Debugs.debug("dealMsgFromJS jsonStr:" + jsonObj.toString());
			Debugs.debug("dealMsgFromJS action:" + action + " aType:" + aType);
			switch (action) {
			case 1:
				switch (aType) {
				case 1:
					lgtype = dValue.getInt("lt");
					Debugs.debug("dealMsgFromJS loginType:" + lgtype);
					clickLoginGame(lgtype);
					break;
				case 4:
					String shareFileName = dValue.getString("n");
					Debugs.debug("dealMsgFromJS shareFileName:" + shareFileName);
					shareImageToWx(shareFileName);
					break;
				case 5:
					String swUrl = dValue.getString("url");
					int swType = 0;
					if (dValue.has("ty")) {
						swType = dValue.getInt("ty");
					}
					String swTitle = dValue.getString("t");
					String swDes = dValue.getString("s");
					Debugs.debug("dealMsgFromJS shareWeb: swUrl:" + swUrl + " swType:" + swType + " swTitle:" + swTitle + " swDes:" + swDes);
					shareWebToWx(swUrl, swTitle, swDes, swType);
					break;
				case 6:
					String userid = dValue.getString("u");
					int type = dValue.getInt("id");
					int money = dValue.getInt("m");
					String itemName = dValue.getString("n");
					Debugs.debug("dealMsgFromJS userid:" + userid + " money:" + money + " itemName:" + itemName);
					XqtSDK.getInstance().setItemInfo(userid, type, money, itemName);
					XqtSDK.getInstance().doPay();
					break;
				case 10:
					int recordAction = dValue.getInt("s");
					Debugs.debug("dealMsgFromJS recordAction:" + recordAction);
					if(0 == recordAction || 2 == recordAction){
						endTime = System.currentTimeMillis();
					}else if(1 == recordAction){
						startTime = System.currentTimeMillis();
					}
					if(1 == recordAction){
						if(startTime >= endTime && (startTime - endTime <= 1000)){
							Debugs.debug("录音操作频繁！");
							break;
						}else{
							Debugs.debug("正常录音操作！");
						}
					}
					
					switch (recordAction) {
					case 0:
						Debugs.debug("录音取消");
//						mAudioRecoderUtils.cancelRecord();//录音取消
						
//						mRecorder.stop();
						mRecorder.cancel();
						break;
					
					case 1:
						Debugs.debug("开始录音");
						try {
							mRecorder.start();
						} catch (IOException e) {
							Debugs.debug("开始录音 IOException e:" + e.getMessage());
						}
//						mAudioRecoderUtils.startRecord();//开始录音
						break;
						
					case 2:
						Debugs.debug("停止录音");
//						mAudioRecoderUtils.stopRecord();//停止录音
						
						mRecorder.stop();
						break;

					default:
						break;
					}
					
					break;
				case 11:
					String url = dValue.getString("k");
					Debugs.debug("dealMsgFromJS audioUrl:" + url);
//					soundPlayerInstance.playSoundByUrl(url);
					break;
				case 12:
					String eventID = dValue.getString("i");
					Debugs.debug("dealMsgFromJS eventID:" + eventID);
					if(eventID.equals("logout")){
						loginHallFlag = false;
						clearData();
					}
					MobclickAgent.onEvent(EntryActivity.this, eventID);	// umeng统计
					break;
				case 13:
					String loadLoginOver = dValue.getString("l");
					Debugs.debug("dealMsgFromJS loadLoginOver:" + loadLoginOver);
					loadLoginFlag = true;
					loginHallFlag = false;
					if(2 == loginState){
						autoWeixinLogin();
					}
					//检测更新
				    initCheckUpdate();
				    removeLoadingView();
				    gameStartEnd();
					break;
				case 14:
					//检测微信是否安装通知JS
					initDataFlag = true;
					sendBrowserDataToJS();
					if (dValue.has("id")) {
						ConstVar.APP_ID = dValue.getString("id");
					}
					//初始化微信api
					initWxAPI();
					break;
				case 15:
					loginHallFlag = true;
//					checkVersion();
					break;
				case 16:
					GDLocation.getInstance().startLocation();
					break;
				case 17:
					String clpType = dValue.getString("type");
					if (clpType.endsWith("get")) {
						sendClipDataToJS();
					} else if (clpType.endsWith("set")) {
						String clpStr = dValue.getString("str");
						setClipboard(clpStr);
					}
					break;
				default:
					break;
				}
				break;
			case 2:
				switch (aType) {
				case 1:
		        	if (engineInited) {
		                gameEngine.game_engine_onStop();
		            }
		        	this.finish();
					break;

				default:
					break;
				}

			default:
				break;
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Debugs.debug("Exception in dealMsgFromJS" + e.toString());
		}
	}
 
    // 有用户信息时自动登陆
    private void autoWeixinLogin() {
    	Debugs.debug("autoLogin pre openid:" + this.openid);
    	if(this.wxLoginFlag && !this.openid.equals("")){
    		Debugs.debug("autoLogin act");
			this.wxLoginFlag = false;
			doLogin(this.openid,2);
    	}
    }
    
  //请求Code
    private void reqCode() {
    	// send oauth request
		final SendAuth.Req req = new SendAuth.Req();
		req.scope = "snsapi_userinfo";
		req.state = ConstVar.CAPTION;
		api.sendReq(req);  	
    }
    
    //分享网页到微信好友
    private void shareWebToWx(String url, String title, String des, int type){
		WXWebpageObject webpage = new WXWebpageObject();
		webpage.webpageUrl = url;
		WXMediaMessage msg = new WXMediaMessage(webpage);
		msg.title = title;
		msg.description = des;
		Bitmap thumb = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
		msg.thumbData = Util.bmpToByteArray(thumb, true);
		
		SendMessageToWX.Req req = new SendMessageToWX.Req();
		req.transaction = buildTransaction("webpage");
		req.message = msg;
//		req.scene = SendMessageToWX.Req.WXSceneSession;
		req.scene = (1 == type) ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
//		req.scene = isTimelineCb.isChecked() ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
		api.sendReq(req);
    }
    
    private String buildTransaction(final String type) {
		return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
	}
    
    //分享图片到微信好友
    private void shareImageToWx(String fileName){
    	Debugs.debug("shareImageToWx fileName:" + fileName);
    	String path = SDCARD_ROOT + "/" + fileName;
		File file = new File(path);
		if (!file.exists()) {
			String tip = EntryActivity.this.getString(R.string.send_img_file_not_exist);
			Toast.makeText(EntryActivity.this, tip + " path = " + path, Toast.LENGTH_LONG).show();
			return;
		}
//		Bitmap bm = Util.getSmallBitmap(path);
		Bitmap thumb = Util.adjustImage(path);
		WXImageObject imgObj = new WXImageObject(thumb);
//		imgObj.setImagePath(path);
		
		WXMediaMessage msg = new WXMediaMessage();
		msg.mediaObject = imgObj;
		
//		Bitmap bmp = BitmapFactory.decodeFile(path);
		Bitmap thumbBmp = Bitmap.createScaledBitmap(thumb, 160, 90, true);
		thumb.recycle();
		msg.thumbData = Util.bmpToByteArray(thumbBmp, true);
		
		SendMessageToWX.Req req = new SendMessageToWX.Req();
		req.transaction = buildTransaction("img");
		req.message = msg;
		req.scene = SendMessageToWX.Req.WXSceneSession;
//		req.scene = isTimelineCb.isChecked() ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
		api.sendReq(req);
		
		Debugs.debug("shareImageToWx end");
    }
    
    //清除数据
    private void clearData() {
    	if(0 == this.lgtype){
			this.openid = "";
			infoControl2.setContent("openid", "", "open");
		}	
    }
    
    //保存数据
    private void saveData(String userid, String openid) {
    	if(0 == this.lgtype){
    		this.openid = openid;
    		infoControl2.setContent("openid", openid, "open");
		}else if(1 == this.lgtype){
			this.userid = userid;
			infoControl.setContent("userid", userid, "user");
		}	
    }
    
    private void initHandler(){
    	handler = new Handler(){

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				
				switch (msg.what) {
				case 1:
					clearData();
					wxLoginFlag = true;
					Toast.makeText(EntryActivity.this, "登陆失败，请重试", Toast.LENGTH_SHORT).show();
					break;
				case 2:
					Toast.makeText(EntryActivity.this, "登陆正在执行中", Toast.LENGTH_SHORT).show();
					break;
				case 3:
					wxLoginFlag = true;
					Toast.makeText(EntryActivity.this, "获取密码失败，请重试", Toast.LENGTH_SHORT).show();
					break;
					
				case 401:
					Toast.makeText(EntryActivity.this, "录音时长太短", Toast.LENGTH_SHORT).show();
					break;
				case 402:
					Toast.makeText(EntryActivity.this, "录音取消！", Toast.LENGTH_SHORT).show();
					break;
					
				case 5:
					if(mUpdateManager == null){
						mUpdateManager = new UpdateManager(EntryActivity.this,(String)msg.obj,server_version);
						mUpdateManager.setListener(EntryActivity.this);
						loginState = 4;		//预更新状态
						mUpdateManager.checkUpdateInfo();
					}
					break;
				case 6:
					Util.makeToast(EntryActivity.this, "检测更新失败!", Toast.LENGTH_SHORT, true);
					break;
				case 101:
					Util.makeToast(EntryActivity.this, "无法连接服务器，请检查你的网络重试", Toast.LENGTH_SHORT, true);
					break;
				default:
					break;
				}
			}
    	};
    }
    
    private void initAudioRecord(){
    	mRecorder = new MP3Recorder();
    	//录音回调
    	mRecorder.setOnAudioStatusUpdateListener(new MP3Recorder.OnAudioStatusUpdateListener() {
    		//录音结束，filePath为保存路径
            @Override
            public void onStop(String filePath) {
            	Debugs.debug("initAudioRecord onStop 录音保存在 = " + filePath);
            	QiniuSDK.getInstance().doGetUpToken(filePath);
            }

			@Override
			public void onFailed() {
				// TODO Auto-generated method stub
				Debugs.debug("initAudioRecord onFailed");
				handler.sendEmptyMessage(401);
			}
			@Override
			public void onCancel() {
				// TODO Auto-generated method stub
				Debugs.debug("initAudioRecord onCancel");
				handler.sendEmptyMessage(402);
			}
    	});
    }
    
    private void initCheckUpdate(){
    	try {
    		Debugs.debug("initCheckUpdate");
    		if(ConstVar.update){
    			ShowProgressDialog("正在检测更新!");
    			new Thread(){
    				public void run(){
    					checkUpdate();		//放外网打开
    				}
    			}.start();
    		}
		} catch (Exception e) {
			// TODO: handle exception
			Debugs.debug("Exception in initCheckUpdate err: " + e.toString());
		}
    }
    
    private void checkVersion(){
        Intent intent = new Intent(EntryActivity.this, UpdateService.class);
        intent.putExtra("apkUrl", "http://ver.0710mj.com/ver/apk/down/lzkwx.apk");
        startService(intent);
    }

    
    private void checkUpdate(){
    	try{
    		loginState = 1;	//正在检测更新状态
        	GetVersionInfo getVersion = new GetVersionInfo();
        	String versionInfo = getVersion.getInfo();
        	Debugs.debug("versionInfo = " + versionInfo);
    		
        	if(versionInfo != null){
        		String info[] = versionInfo.split(" ");
        		if(info.length > 1){
        			Debugs.debug("info[0] = " + info[0] + " info[1] = " + info[1] + "current_version = " + ConstVar.VERSIONCODE);
        			info[0] = info[0].trim();
            		info[1] = info[1].trim();
            		server_version = info[0];
            		low_version = info[1];
            		if((0 == Integer.valueOf(server_version)) || (0 == Integer.valueOf(low_version))){
            			Debugs.debug("获取更新内容不合法!");
            			getVersion = null;
                    	versionInfo = null;
                    	loginState = 2;
                    	if(tip != null){
                    		tip.cancel();
                    		tip.dismiss();
                    		tip = null;
                    	}
                    	return;
            		}
            		if(info[0] != null && (Integer.valueOf(ConstVar.VERSIONCODE) >= Integer.valueOf(server_version))){
            			//不需要更新
            			loginState = 2;
            		}else{
            			if(null != info[2]){
            				Message msg = new Message();
            				msg.what = 5;
            				msg.obj = info[2];
            				handler.sendMessage(msg);
            			}
            		}
        		}else{
        			handler.sendEmptyMessage(6);
            		loginState = 2;
        		}
        		info = null;
        	}else{	//检测更新失败  提示
        		handler.sendEmptyMessage(6);
        		loginState = 2;
        	}
        	getVersion = null;
        	versionInfo = null;
        	if(tip != null){
        		tip.cancel();
        		tip.dismiss();
        		tip = null;
        	}
    	}catch(Exception e){
    		Debugs.debug("Exception in checkUpdate err: " + e.toString());
    	}
    }
    
    private void clickLoginGame(int lType){
		if(1 == loginState){
			Util.makeToast(this, "正在检测更新!", Toast.LENGTH_LONG, true);
			return;
		}else if(3 == loginState){
			Util.makeToast(this, "正在更新!", Toast.LENGTH_LONG, true);
			if(mUpdateManager != null){
				mUpdateManager.showDownload();
			}
			return;
		}else if(4 == loginState){
			Util.makeToast(this, "请选择是否更新!", Toast.LENGTH_LONG, true);
			if(mUpdateManager != null){
				mUpdateManager.shownoticDialog();
			}
			return;
		}else if(5 == loginState){
			Util.makeToast(this, "请安装更新包后重新登陆.安装包在sd卡"+ ConstVar.CAPTION + "文件夹里面,如不能安装请先卸载本版本", Toast.LENGTH_LONG, true);
			return;
		}
		Debugs.debug("blade checkWXAppInstalled");
    	if(!api.isWXAppInstalled()){
    		Debugs.debug("blade WXApp not Installed");
    		try {
    			JSONObject jsonSendObj = new JSONObject();
    			jsonSendObj.put("a", 1);
    			jsonSendObj.put("t", 2);
    			String sendString = jsonSendObj.toString();
    			sendMsgToJS(sendString);
    		} catch (JSONException e) {
    			// TODO Auto-generated catch block
    			Debugs.debug("Exception in checkWXAppInstalled " + e.toString());
    		}
    		return;
    	}
		if(this.wxLoginFlag){
			this.wxLoginFlag = false;
			if (0 == lType)
			{
				Debugs.debug("clickLoginGame openid:" + this.openid);
				if(!this.openid.equals("")){
					doLogin(this.openid,2);
				}else{
					//微信登陆
					reqCode();
				}
//				handler.sendEmptyMessage(2);
			}else if (1 == lType) {
				//游客登陆
				Debugs.debug("dealMsgFromJS userid:" + this.userid);
				if(!this.userid.equals("")){
					doLogin(this.userid,4);
				}else{
					//游客登陆
					Date date = new Date();
					SimpleDateFormat sd = new SimpleDateFormat("yyyyMMdd");
					String dateStr = sd.format(date);
					Debugs.debug("dealMsgFromJS dateStr:" + dateStr);
					doLogin(dateStr,3);
				}
			}
		}
    }
    public Handler globalHandler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			switch(msg.what){
				case 1:
					Toast.makeText(_instance, "获取订单失败", Toast.LENGTH_SHORT).show();
					break;
				case 2:
					Toast.makeText(_instance, "购买失败", Toast.LENGTH_SHORT).show();
					break;
				case 3:
					XqtSDK.getInstance().goToPay();
					break;
				case 601:
					sendLocationToJS(true);
					break;
				case 602:
					sendLocationToJS(false);
					break;
				case 701:
					Toast.makeText(_instance, "用户取消", Toast.LENGTH_SHORT).show();
					break;
				case 702:
					Toast.makeText(_instance, "用户拒绝授权", Toast.LENGTH_SHORT).show();
					break;
				case 703:
					Toast.makeText(_instance, "用户未知错误", Toast.LENGTH_SHORT).show();
					break;
				default:
					break;
			}
		}
	};
    
    private void doLogin(String code, int type) {
		try {
			CommonSDK.getInstance().doLoginTask(EntryActivity.this, code, type, new OnLoginStateListener() {
				
				@Override
				public void onLoginSuccess(String userid,String token,String openid) {
					if(!token.equals("")){
						wxLoginFlag = true;
						Debugs.debug("doLogin userid = " + userid +"  token = " + token + " openid = " + openid);
		                try {
		 					JSONObject jsonSendObj = new JSONObject();
		 					jsonSendObj.put("a", 1);
		 					jsonSendObj.put("t", 1);
		 					JSONObject dValue = new JSONObject();
		 					dValue.put("u", userid);
		 					dValue.put("t", token);
		 					jsonSendObj.put("d", dValue);
		 					String sendString = jsonSendObj.toString();
		 					sendMsgToJS(sendString);
		 					
		 					saveData(userid,openid);
		 					
		 				} catch (JSONException e) {
		 					// TODO Auto-generated catch block
		 					Debugs.debug("Exception in doLogin parsejson" + e.toString());
		 				}
					}else{
						handler.sendEmptyMessage(3);
					}
				}
				@Override
				public void onLoginFailed() {
					// TODO Auto-generated method stub
					handler.sendEmptyMessage(1);
				}
			});
		} catch (Exception e) {
			// TODO: handle exception
			Debugs.debug("Exception in doLogin" + e.toString());
		}
		
	}
    
    public void getCodeFromWX(String code){
    	Debugs.debug("blade getCodeFromWX code = " + code);
//    	doLogin(code,1);
    	sendCodeToJs(code);
    }
    
    public void setWXLgFlg(Boolean flag) {
    	wxLoginFlag = flag;
    }
    
    private void sendCodeToJs(String code){
    	 try {
				JSONObject jsonSendObj = new JSONObject();
				jsonSendObj.put("a", 1);
				jsonSendObj.put("t", 1);
				JSONObject dValue = new JSONObject();
				dValue.put("code", code);
				jsonSendObj.put("d", dValue);
				String sendString = jsonSendObj.toString();
				sendMsgToJS(sendString);
				
//				saveData(userid,openid);
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				Debugs.debug("Exception in sendCodeToJs parsejson" + e.toString());
			}
    }
    
    public void result(boolean res){
		if(res){
			Debugs.debug("购买成功");
			Toast.makeText(this, "购买成功,正在给您领取房卡,如果没有领取成功,请重新登陆领取", Toast.LENGTH_LONG).show();
			sendCheckBuyMoney();
		}else{
			Debugs.debug("购买失败");
		}
	}
    
    public void sendUpAudioUrl(String key){
    	Debugs.debug("sendUpAudioKey key=" + key);
		try {
				JSONObject jsonSendObj = new JSONObject();
				jsonSendObj.put("a", 1);
				jsonSendObj.put("t", 10);
				JSONObject dValue = new JSONObject();
				dValue.put("k", key);
				jsonSendObj.put("d", dValue);
				String sendString = jsonSendObj.toString();
				sendMsgToJS(sendString);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Debugs.debug("Exception in checkWXAppInstalled " + e.toString());
		}
    }
    private void ShowProgressDialog(String msg){
    	if(tip != null){
    		tip.cancel();
    		tip.dismiss();
    		tip = null;
    	}
		tip = new ProgressDialog(this);
		tip.setMessage(msg);
		tip.setCanceledOnTouchOutside(false);
		tip.show();
	}
    private void sendCheckBuyMoney(){
    	try {
			JSONObject jsonSendObj = new JSONObject();
			jsonSendObj.put("a", 1);
			jsonSendObj.put("t", 6);
			String sendString = jsonSendObj.toString();
			sendMsgToJS(sendString);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Debugs.debug("Exception in sendCheckBuyMoney " + e.toString());
		}
    }
    protected void onRestart(){
		super.onRestart();
		//重新启动
		initLocalData();
	}
    @Override
    public void onPause() {
    	Debugs.debug("onPause");
        super.onPause();
        if (engineInited) {
        	gameEngine.game_engine_onPause();
        }
        MobclickAgent.onPause(this);
    }

    @Override
    public void onResume() {
    	Debugs.debug("onResume");
        super.onResume();
        if (engineInited) {
        	gameEngine.game_engine_onResume();
        }
        MobclickAgent.onResume(this);
    }
    
    @Override
	protected void onDestroy(){
    	Debugs.debug("onDestroy");
		super.onDestroy();
		try {
			Debugs.closeFile();
			Debugs.debug("");
			if(null != hotUpdate) {
				hotUpdate = null;
			}
			if(null != _instance) {
				_instance = null;
			}
			if(null != _instance) {
				_instance = null;
			}
			if(null != handler) {
				handler = null;
			}
			if(null != infoControl) {
				infoControl = null;
			}
			if(null != infoControl2) {
				infoControl2 = null;
			}
			if(null != user) {
				user = null;
			}
			if(null != open) {
				open = null;
			}
			if(null != open) {
				open = null;
			}
			if(null != userid) {
				userid = null;
			}
			if(null != openid) {
				openid = null;
			}
			if(null != tip) {
				tip.cancel();
        		tip.dismiss();
        		tip = null;
			}
			if(null != mRecorder) {
				mRecorder.release();
				mRecorder = null;
			}
			if(null != mUpdateManager) {
				mUpdateManager = null;
			}
			if(null != server_version) {
				server_version = null;
			}
			if(null != low_version) {
				server_version = null;
			}
			if(null != viewGroup) {
				viewGroup = null;
			}
			if(null != fLayout_load) {
				fLayout_load = null;
			}
			if(null != mClipboard) {
				mClipboard = null;
			}
			GDLocation.getInstance().destroyLocation();
		} catch (Exception e) {
			// TODO: handle exception
			Debugs.debug("onDestroy Exception：" + e.getMessage());
		}
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
        case KeyEvent.KEYCODE_BACK:
//        	if (engineInited) {
//                gameEngine.game_engine_onStop();
//            }
//        	this.finish();
        	moveTaskToBack(true);
            return true;
        default:
            return super.onKeyDown(keyCode, event);
        }
    }
    
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void closeLogin() {
		// TODO Auto-generated method stub
		//判断是不是必须更新的
		try{
			Debugs.debug("low_version = " + low_version + " ConstVar.Version + " + ConstVar.VERSIONCODE);
			if(Float.valueOf(low_version) > Float.valueOf(ConstVar.VERSIONCODE)){
				Util.makeToast(this, "本地版本号低于当前最低版本号,必须更新客户端", Toast.LENGTH_LONG, true);
				Thread.sleep(2000);
				//关闭登陆窗体
				this.finish();
			}else{
				loginState = 2;			//修改客户端状态为可登陆状态
			}
		}catch(Exception e){
			Debugs.debug("Exception in closeLogin err: " + e.toString());
			loginState = 2;
		}
	}
	
	//初始化获取浏览器打开app数据
	private void initDataFromBrowser(Intent intent){
		try {
			Uri uri = intent.getData();
			if(null != uri){
				String data = uri.getQueryParameter("openapp");
				if(null != data && data.equals("open")){
					Debugs.debug("initDataFromBrowser data: " + data);
				}
				deskId = Integer.parseInt(uri.getQueryParameter("deskid"));
				Debugs.debug("initDataFromBrowser deskId: " + deskId);
				if(initDataFlag){
					sendDeskIdToJS();
				}
				if(2 == loginState && loadLoginFlag && !loginHallFlag){
					//微信登陆
					Debugs.debug("initDataFromBrowser openid:" + this.openid);
					autoWeixinLogin();
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			Debugs.debug("Exception in initDataFromBrowser err: " + e.toString());
		}
	}
	
	private void decodeScheme(Intent intent) {
		try {
			String scheme = intent.getScheme();
			Uri uri = intent.getData();
			if(null != scheme && null != uri){
				String data = uri.getQuery();
				if(null != data){
					Debugs.debug("decodeScheme data: " + data);
					ConstVar.browserData = data;
				}
				if(initDataFlag){
					sendBrowserDataToJS();
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			Debugs.debug("Exception in decodeScheme err: " + e.toString());
		}
	}

	private void sendDeskIdToJS(){
    	try {
			Debugs.debug("sendDeskId deskId: " + deskId);
			if(-1 != deskId){
				JSONObject jsonSendObj = new JSONObject();
				jsonSendObj.put("a", 1);
				jsonSendObj.put("t", 14);
				JSONObject dValue = new JSONObject();
				dValue.put("i", deskId);
				jsonSendObj.put("d", dValue);
				String sendString = jsonSendObj.toString();
				sendMsgToJS(sendString);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Debugs.debug("Exception in sendDeskId " + e.toString());
		}
    }
	
	private void sendBrowserDataToJS(){
    	try {
			Debugs.debug("sendBrowserDataToJS data: " + ConstVar.browserData);
			if(!ConstVar.browserData.equals("")){
				JSONObject jsonSendObj = new JSONObject();
				jsonSendObj.put("a", 1);
				jsonSendObj.put("t", 14);
				JSONObject dValue = new JSONObject();
				dValue.put("param", ConstVar.browserData);
				jsonSendObj.put("d", dValue);
				String sendString = jsonSendObj.toString();
				sendMsgToJS(sendString);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Debugs.debug("Exception in sendBrowserDataToJS " + e.toString());
		}
    }
	
	private void sendLocationToJS(boolean flag){
//		if(!loginHallFlag) return;
    	try {
			Debugs.debug("sendLocationToJS 经度: " + ConstVar.LONGITUDE + "  纬度：" + ConstVar.LATITUDE);
			JSONObject jsonSendObj = new JSONObject();
			jsonSendObj.put("a", 1);
			jsonSendObj.put("t", 16);
			JSONObject dValue = new JSONObject();
			if (flag) {
				dValue.put("lg", ConstVar.LONGITUDE);
				dValue.put("la", ConstVar.LATITUDE);
			}
			jsonSendObj.put("d", dValue);
			String sendString = jsonSendObj.toString();
			sendMsgToJS(sendString);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Debugs.debug("Exception in sendDeskId " + e.toString());
		}
    }

	@Override
	public void downLoading() {
		// TODO Auto-generated method stub
		loginState = 3;	//正在下载
	}

	@Override
	public void downLoadOver() {
		// TODO Auto-generated method stub
		loginState = 5;	//更新下载完成
	}
}
