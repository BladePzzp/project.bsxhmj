package com.xxxxx.mj.tools;


public class ConstVar{
	//app文件地址
	public static String filePath = "";
	//版本校验地址
	public static final String VERSION_CHECK 	= "http://mogumj.com/version.lua?game="+ConstVar.CAPTION;
	//版本号
	public static String VERSIONCODE 			= "1";
	//游戏名称
	public static final String CAPTION 			= "xinhe";
	//更新开关
	public static final boolean update 			= true;
	//egret 根目录
	public static final String EGRET_ROOT = "egret";
	//TODO: egret publish之后，修改以下常量为生成的game_code名(本地zip包)
	public static final String EGRET_PUBLISH_ZIP = "game_code_171219232259.zip";
	//若bUsingPlugin为true，开启插件
	public static final boolean bUsingPlugin = false;
	//egret gameId
	public static String gameId = "local";
	//热更新地址
	public static final String UPDATE_URL = "http://mogumj.com/update/update.php?" + "game=" + ConstVar.CAPTION + "&v=" + ConstVar.VERSIONCODE + "&platform=android";
	//自定义热更新开关
	public static final boolean UseCustomHotUpdate = false;
	//loading开关
	public static final boolean CustomLoadingFlag = false;
	//热更新模式
	public static enum HotUpdateType {
		LOCALMODE,
		NETZIPMODE,
		LOCALZIPMODE,
	}
	public static final HotUpdateType HOT_UPDATE_TYPE = HotUpdateType.NETZIPMODE;
//	//微信登陆获取用户信息
	public static final String APP_SERVER_URL_GET_USER = "http://api.0710mj.com/ytt/newlogin/weixinlogin.php?";
	//微信登陆获取用户信息(记住openid之后)
	public static final String APP_SERVER_URL_GET_USER_AUTO = "http://api.0710mj.com/ytt/newlogin/weixinloginauto.php?";
	//微信登陆获取用户信息测试地址
//	public static final String APP_SERVER_URL_GET_USER = "http://test.0710mj.com/ytt/newlogin/weixinlogin.php?";
//	//微信登陆获取用户信息(记住openid之后)测试地址
//	public static final String APP_SERVER_URL_GET_USER_AUTO = "http://test.0710mj.com/ytt/newlogin/weixinloginauto.php?";
	//游客登陆获取用户信息
	public static final String APP_SERVER_URL_GET_USER_VISITOR = "http://api.0710mj.com/ytt/login/loginvisitor.apk.php?";
	//登陆获取用户信息密钥
    public static final String KEY_LOGIN = "gghs837fdhfnytt88qijunchaodfgw";
    //微信登陆APP_ID
    public static String APP_ID = "wx375d209a67e6e5a0";
    
    // 第三方微信支付商户秘钥
    public static final String key = "b7cfbfeadc073dcc7984b6400c3562d6";
 	// 第三方微信支付获取订单、支付密钥
    public static final String le_key = "sajhdjgheuribvieue53467sdafejuh";
 	// 第三方微信支付获取订单地址
    public static final String url_getorder = "http://api.0710mj.com/ytt/chongzhi/weixin/getorder.php?";
 	// 第三方微信支付回调地址
    public static final String url_notify = "http://api.0710mj.com/ytt/chongzhi/weixin/notice_wxpay.php";
    // 第三方微信支付展示页面
    public static final String wxPayBackUrl = "http://api.0710mj.com/ytt/chongzhi/weixin/back.php";
 	// 第三方微信支付网关商户号 
    public static final String consumerId = "154717";
 	// 第三方微信支付上级带代理  默认为100000
    public static final String superid = "100000";
    
    //七牛获取上传token地址
    public static final String getUptokenUrl = "http://api.0710mj.com/ytt/GetToken/GetToken.php";
    //七牛资源下载地址
	public static final String downLoadUrl = "http://qiniu.0710mj.com/";
	
	//屏幕参考宽度
	public static final double defaultScreenWidth = 1136.0;
	//屏幕参考高度
	public static final double defaultScreenHeight = 640.0;
	
	//屏幕宽度
	public static int screenWidth = 1136;
	//屏幕高度
	public static int screenHeight = 640;
	//X-缩放比例
	public static float xZoom = 1.0f; 
	//Y-缩放比例
	public static float yZoom = 1.0f;
	//高德定位经纬度
	public static double LONGITUDE = 114.401294;	
	public static double LATITUDE = 30.441249;
	
	//浏览器参数
	public static String browserData = "";
 }
