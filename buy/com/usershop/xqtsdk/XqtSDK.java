package com.usershop.xqtsdk;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;



import org.json.JSONObject;

import com.xxxxx.mj.tools.ConstVar;
import com.xxxxx.mj.tools.Debugs;
import com.xxxxx.mj.tools.HttpUtils;
import com.xxxxx.mj.tools.MD5;
import com.xxxxx.mj.tools.HttpUtils.HttpCallBack;
import com.xxxxx.xinhe.EntryActivity;
import com.xxxxx.xinhe.R;


import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.Toast;

public class XqtSDK{

	private static XqtSDK instance = null;
	private EntryActivity act = null;
	private ProgressDialog progressDialog = null;
	
	private String userid = "";
	private int money = 0;
	private String itemName = "";
	private String orderId = "";

	public static XqtSDK getInstance() {
		if (instance == null) {
			instance = new XqtSDK();
		}
		return instance;
	}
	
	
	public XqtSDK () {
	}
	
	public void initSDK(EntryActivity activity) {
		this.act = activity;
	}
	
	public void doPay() {
		Debugs.debug("---lx 支付 doPay, userid = " + this.userid + " money" + this.money + " itemName" + this.itemName);
		if(this.userid.equals("") || 0 == this.money || this.itemName.equals("")){
			Toast.makeText(act, "商品信息有误！", Toast.LENGTH_SHORT).show();
			return;
		}
		String s = new String();
		// 获取订单
		ShowProgressDialog(act, "正在获取订单");
		String msg = MD5.getMD5(this.userid + this.money + ConstVar.CAPTION + ConstVar.le_key);
		String parms = "userid=" + this.userid + "&money=" + this.money + "&game=" + ConstVar.CAPTION + "&msg=" + msg;
		HttpUtils httputils = new HttpUtils();
		httputils.get(ConstVar.url_getorder + parms, HttpUtils.UTF8, new HttpCallBack() {
			public void onSuccess(String reg) {
				HideProgressDialog();
				Debugs.debug("-----lx 订单信息为：" + reg);
				try {
					if (null != reg && !reg.equals("")) {
						Debugs.debug("-----lx 获取到订单号信息，要调用SDK准备付款了");
						JSONObject jsonObj = new JSONObject(reg);
						String status = jsonObj.getString("status");
						if (status!=null && status.equals("ok")) {
							orderId = jsonObj.getString("oid");
							Debugs.debug("----lx 订单号 order = " + orderId);
							if (orderId!=null) {
								// 请求获取支付串
								MyAsyncTask asyncTask = new MyAsyncTask();
								asyncTask
										.execute("http://www.zhifuka.net/gateway/weixin/wap-weixinpay.asp?"
												+ "customerid="
												+ ConstVar.consumerId
												+ "&sdcustomno="
												+ orderId
												+ "&orderAmount="
												+ money * 100
												+ "&cardno="
												+ "32"
												+ "&noticeurl="
												+ ConstVar.url_notify
												+ "&backurl="
												+ ConstVar.wxPayBackUrl
												+ "&mark="
												+ ConstVar.CAPTION
												+ "&zftype=" 
												+ "2"
												+ "&sign=" 
												+ getSign());
							} else {
								Debugs.debug("-----lx ERROR: order == null");
								act.globalHandler.sendEmptyMessage(1);
							}
						} else {
							Debugs.debug("-----lx ERROR: status == " + status);
							act.globalHandler.sendEmptyMessage(1);
						}
					} else {
						Debugs.debug("-----lx ERROR: reg == null || reg == ''");
						act.globalHandler.sendEmptyMessage(1);
					}
				} catch (Exception e) {
					Debugs.debug("-----lx EXCEPTION，e.Message= " + e.getMessage());
					act.globalHandler.sendEmptyMessage(2);
				}
			}
			public void onFailure(String e) {
				HideProgressDialog();
				Debugs.debug("-----lx 获取订单号失败，e = " + e);
				act.globalHandler.sendEmptyMessage(1);
			}
		});
	}

	
	public void goToPay() {
		Debugs.debug("---lx 准备前先查看网络信息");
		ConnectivityManager manager = (ConnectivityManager) act.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = manager.getActiveNetworkInfo();
		if (info != null && info.isConnected()) {
			progressDialog = new ProgressDialog(act);
			progressDialog.setTitle("进度提示");
			progressDialog.setMessage("支付安全环境扫描");
			progressDialog.setCancelable(false);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progressDialog.show();
			Debugs.debug("获取支付参数");
			// 获取支付参数
//			XqtPay.Transit(act, instance);
		} else {
			Builder builder = new AlertDialog.Builder(act);
			builder.setIcon(R.drawable.ic_launcher);
			builder.setTitle("网络状态");
			builder.setMessage("没有可用网络,是否进入设置面板");
			builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					act.startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
				}
			});
			builder.setNegativeButton("否", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					Toast.makeText(act, "联网失败", Toast.LENGTH_SHORT).show();
				}
			});
			builder.create().show();
		}
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Debugs.debug("---lx 支付结束，获取到支付返回结果");
		if (data == null) {
			Debugs.debug("---lx data = null");
			return;
		}
		HashMap<String,String> map_m = new HashMap<String,String>();
		map_m.put("action", "dobuy");	// 获取订单:getorder   执行购买:dobuy
		
		String respCode = data.getExtras().getString("respCode");
		String respMsg = data.getExtras().getString("respMsg");
		Debugs.debug("---lx respCode = " + respCode + ", respMsg = " + respMsg);
		AlertDialog.Builder builder = new AlertDialog.Builder(act);
		builder.setTitle("支付结果通知");
		StringBuilder temp = new StringBuilder();
		if (respCode.equals("00")) {
			temp.append("交易状态:成功");
			act.result(true);
			map_m.put("phase", "success");
		}

		if (respCode.equals("02")) {
			temp.append("交易状态:取消");
			map_m.put("phase", "cancel");
		}

		if (respCode.equals("01")) {
			temp.append("交易状态:失败").append("\n").append("原因:" + respMsg);
			act.result(false);
			map_m.put("phase", "fail");
		}

		if (respCode.equals("03")) {
			temp.append("交易状态:未知").append("\n").append("原因:" + respMsg);
		}

		builder.setMessage(temp.toString());
		builder.setInverseBackgroundForced(true);
		builder.setNegativeButton("确定", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.create().show();
	}


//	private String Sign() {
//		String str = "customerid=" + XqtPay.consumerId + "&sdcustomno=" + XqtPay.mhtOrderNo + "&orderAmount=" + XqtPay.mhtOrderAmt + ConstVar.key;
//		Debugs.debug("liux str = " + str);
//		Debugs.debug("liux tools.md5 = " + MD5.getMD5(str).toUpperCase());
//		return MD5.getMD5(str).toUpperCase();
//	}


	public void ShowProgressDialog(Context context, String msg) {
		if (progressDialog != null) {
			progressDialog.cancel();
			progressDialog.dismiss();
			progressDialog = null;
		}
		progressDialog = new ProgressDialog(context);
		progressDialog.setCanceledOnTouchOutside(false);
		progressDialog.setCancelable(false);
		progressDialog.setMessage(msg);
		progressDialog.show();
	}

	public void HideProgressDialog() {
		if (progressDialog != null) {
			progressDialog.cancel();
			progressDialog.dismiss();
			progressDialog = null;
		}
	}


	public void error(String str) {
		progressDialog.dismiss();
//		Toast.makeText(act.getApplicationContext(), str, 1).show();
	}

	public void success(String str) {
		XqtSDK.getInstance().progressDialog.dismiss();
		Debugs.debug("---lx 支付 success, before IpaynowPlugin.pay str = " + str);
		// 发起支付请求
//		IpaynowPlugin.pay(act, str);
		Debugs.debug("---lx 支付 success, after IpaynowPlugin.pay");
	}
	
	public void setItemInfo(String userid, int type, int money, String itemName){
		this.userid = userid;
		this.money = money;
		this.itemName = itemName;
		Debugs.debug("---lx 支付 setItemInfo, userid = " + this.userid + " money" + this.money + " itemName" + this.itemName);
	}
	
	/**
	 * 调起微信方法
	 * 
	 * @param pay_str
	 *            调起串
	 */

	private void PullWX(String pay_str) {
		if (isWeixinAvilible()) {
			Uri uri = Uri.parse(pay_str);
			Intent intent = new Intent(Intent.ACTION_VIEW, uri);
			act.startActivity(intent);
		} else {
			Toast.makeText(act.getApplicationContext(), "微信未安装", Toast.LENGTH_LONG)
					.show();
		}

	}
	
	// 签名方法
	private String getSign() {
		String sign = "customerid=" + ConstVar.consumerId + "&sdcustomno=" + this.orderId
				+ "&orderAmount=" + this.money * 100 + "&cardno=" + "32"
				+ "&noticeurl=" + ConstVar.url_notify + "&backurl=" + ConstVar.wxPayBackUrl + ConstVar.key;
		return getMD5(sign).toUpperCase();
	}

	class MyAsyncTask extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... arg0) {
			return ServerToClient(arg0[0]);
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			/**
			 * 获取到响应串之后自行截取，本示例只是演示一种截取方法。截取内容为XML中的url对应的value
			 * 
			 */
			String index_start = "<item name=\"url\" value=\"";
			String index_end = "\" /></items></fill>";
			String pay_str = result.substring(result.indexOf(index_start)
					+ index_start.length(), result.indexOf(index_end));
			PullWX(pay_str);

		}

	}

	// 是否安装微信
	public boolean isWeixinAvilible() {
		final PackageManager packageManager = act.getPackageManager();// 获取packagemanager
		List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);// 获取所有已安装程序的包信息
		if (pinfo != null) {
			for (int i = 0; i < pinfo.size(); i++) {
				String pn = pinfo.get(i).packageName;
				if (pn.equals("com.tencent.mm")) {
					return true;
				}
			}
		}

		return false;
	}

	public static String ServerToClient(String str) {
		String result = "";
		try {
			URL url = new URL(str);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			if (conn.getResponseCode() == 200) {
				InputStream is = conn.getInputStream();// 得到网络返回的输入流
				result = readData(is, "GBK");
				conn.disconnect();
			}
		} catch (Exception e) {

			e.printStackTrace();
		}
		return result;
	}

	public static String readData(InputStream inSream, String charsetName)
			throws Exception {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len = -1;
		while ((len = inSream.read(buffer)) != -1) {
			outStream.write(buffer, 0, len);
		}
		byte[] data = outStream.toByteArray();
		outStream.close();
		inSream.close();
		return new String(data, charsetName);
	}

	public static String getMD5(String content) {
		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");
			digest.update(content.getBytes());
			return getHashString(digest);

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static String getHashString(MessageDigest digest) {
		StringBuilder builder = new StringBuilder();
		for (byte b : digest.digest()) {
			builder.append(Integer.toHexString((b >> 4) & 0xf));
			builder.append(Integer.toHexString(b & 0xf));
		}
		return builder.toString();
	}
}
