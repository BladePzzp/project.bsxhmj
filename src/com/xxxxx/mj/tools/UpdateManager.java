package com.xxxxx.mj.tools;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageButton;
import android.widget.TextView;

public class UpdateManager {
	private Context mContext;
	//提示语
	private String updateMsg = "当前不是最新版本,是否下载？";
	//返回的安装包url
	public String apkUrl = "";
	public String apkMD5 = "";
	private AlertDialog noticeDialog;
	private TextView txt_tip;
	private ImageButton bt_ok;
	private ImageButton bt_cancel;
	private ProgressDialog downloadDialog;
	 /* 下载包安装路径 */
    private static String saveFileName = ConstVar.filePath;
    /* 进度条与通知ui刷新的handler和msg常量 */
    private static final int DOWN_UPDATE = 1;
    private static final int DOWN_OVER = 2;
    private byte progress;
    
    private Thread downLoadThread;
    private boolean interceptFlag = false;
    private Handler mHandler;
    
    public interface UpdateListener{
    	public void closeLogin();			//通知登陆窗体  关闭游戏
    	public void downLoading();
    	public void downLoadOver();			//下载完成
    }
    private UpdateListener listener;
    
    public void setListener(UpdateListener thelistener){
    	this.listener = thelistener;
    }
    
	public UpdateManager(Context context,String url,String server_version) {
		this.mContext = context;
		this.apkUrl = url;
		saveFileName = ConstVar.filePath + ConstVar.CAPTION + "_" + server_version  + ".apk";
		Debugs.debug("this.apkUrl = " + this.apkUrl);
		mHandler = new Handler(){
	    	public void handleMessage(Message msg){
	    		switch (msg.what) {
					case DOWN_UPDATE:
						downloadDialog.setProgress(progress);
						break;
					case DOWN_OVER:
						Debugs.debug("DOWN_OVER progress = " + progress);
						if(downloadDialog != null){
							downloadDialog.dismiss();
							downloadDialog = null;
						}
						listener.downLoadOver();
						installApk();
						break;
					default:
						break;
				}
	    	};
	    };
	}
	
	//外部接口让主Activity调用
	public void checkUpdateInfo(){
		showNoticeDialog();
	}
	
	private void showNoticeDialog(){
//		if(noticeDialog == null){
//			noticeDialog = new AlertDialog.Builder(mContext).create();
//		}
//		noticeDialog.setCanceledOnTouchOutside(false);
//		noticeDialog.show();
//   	 	Window window = noticeDialog.getWindow();
//   	 	window.setContentView(R.layout.custom_dialog);
//   	 	window.setLayout((int)(846*ConstVar.xZoom), (int)(432 * ConstVar.yZoom));   
//   	 	window.setGravity(Gravity.CENTER);
//   	 
//   	 	txt_tip = (TextView)window.findViewById(R.id.txt_tip);
//   	 	if(1.0 == ConstVar.xZoom){
//   	 		txt_tip.setTextSize(TypedValue.COMPLEX_UNIT_PX, ConstVar.xZoom * (1.0f * 42 + 0.5f)); 
//   	 	}else{
//   	 		txt_tip.setTextSize(TypedValue.COMPLEX_UNIT_PX, ConstVar.xZoom * (mContext.getResources().getDisplayMetrics().density * 42 + 0.5f)); 
//   	 	}
//   	 	txt_tip.setText("软件版本更新");
//   	 	bt_ok = (ImageButton) window.findViewById(R.id.dialog_ok);
//   	 	bt_cancel = (ImageButton) window.findViewById(R.id.dialog_cancel);
//   	 	bt_ok.setOnClickListener(new View.OnClickListener() {
//    	 	public void onClick(View v) {
////    	 		listener.downLoading();
//    	 		noticeDialog.dismiss();
//				showDownloadDialog();
//    	 	}
//    	 });
//   	 	bt_cancel.setOnClickListener(new View.OnClickListener() {
//	 	 	public void onClick(View v) {
//	 	 		noticeDialog.dismiss();	
////				listener.closeLogin();
//	 	 	}
//	 	 });
		AlertDialog.Builder builder = new Builder(mContext);
		builder.setTitle("软件版本更新");
		builder.setMessage(updateMsg);
		builder.setPositiveButton("立即更新", new OnClickListener() {			
			 
			public void onClick(DialogInterface dialog, int which) {
				listener.downLoading();
				dialog.dismiss();
				showDownloadDialog();			
			}
		});
		builder.setNegativeButton("稍后再说", new OnClickListener() {			
			 
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();	
				listener.closeLogin();
			}
		});
		noticeDialog = builder.create();
		noticeDialog.setCanceledOnTouchOutside(false);
		noticeDialog.show();
	}
	public void shownoticDialog(){
		if(noticeDialog != null){
			noticeDialog.show();
		}
	}
	public void showDownload(){
		if(downloadDialog != null){
			downloadDialog.show();
		}else{
			showDownloadDialog();
		}
	}
	private void showDownloadDialog(){
		downloadDialog = new ProgressDialog(mContext);
		downloadDialog.setTitle("软件版本更新");
		downloadDialog.setMax(100);
		downloadDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		downloadDialog.setCancelable(true);
		downloadDialog.setButton("取消", new OnClickListener(){
			 
			public void onClick(DialogInterface arg0, int arg1) {
				downloadDialog.dismiss();
				interceptFlag = true;
				listener.closeLogin();
			}
			
		});
		downloadDialog.setCanceledOnTouchOutside(false);
		downloadDialog.show();
		downloadApk();
	}
	
	private Runnable mdownApkRunnable = new Runnable() {	
		 
		public void run() {
			try {
				File file = new File(ConstVar.filePath);
				if(!file.exists()){
					file.mkdir();
				}
				int size = getApkFileSize();
				String apkFile = saveFileName;
				File ApkFile = new File(apkFile);
				//下载前先删除之前下载的旧文件
				if(ApkFile.exists() && ApkFile.isFile()){
					ApkFile.delete();
				}
				int saveFileLength = (int) ApkFile.length();
				if(saveFileLength < size){


					URL url = new URL(apkUrl);
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
					conn.setRequestMethod("GET");
					conn.setConnectTimeout(5000);
					conn.setRequestProperty("Connection", "Keep-Alive");
					conn.connect();

//					URL url = new URL(apkUrl);
//					HttpURLConnection conn = (HttpURLConnection)url.openConnection();
//					conn.setRequestMethod("GET");
//					conn.setDoInput(true);
//					conn.setDoOutput(true);
//					conn.setUseCaches(false);
//					conn.setRequestProperty("Accept-Encoding", "identity");
//					conn.setRequestProperty("Range", "bytes=" + saveFileLength + "-" + (size - 1));
//					conn.setConnectTimeout(10000);
//					conn.setReadTimeout(5000);
//					conn.connect();
					
					int length = conn.getContentLength();
					Debugs.debug("文件大小是:" + length + " saveFileLength: " + saveFileLength);
					if(-1 != length){
						InputStream is = conn.getInputStream();
						RandomAccessFile fos = new RandomAccessFile(ApkFile,"rwd");
						fos.seek(saveFileLength);
						
						int count = saveFileLength;
						byte buf[] = new byte[1024];
						
						do{   		   		
				    		int numread = is.read(buf);
				    		if(numread < 0){
				    			break;
				    		}
				    		count += numread;
//				    		Debugs.debug("count = " + count + " numread = " + numread);
				    	    progress =(byte)(((float)count / (length + saveFileLength)) * 100);
//				    	    Debugs.debug("progress = " + progress);
				    	    //更新进度
				    	    mHandler.sendEmptyMessage(DOWN_UPDATE);
				    		fos.write(buf,0,numread);
				    	}while(!interceptFlag);//点击取消就停止下载.
						fos.close();
						is.close();
					}
				}
				
				if(!interceptFlag){
					Debugs.debug("file md5:" + MD5.getFileMD5(apkFile));
					//检测MD5是不是对的  如果不是对的 就提示下载失败  到指定 的网址那去下载
					
		    		//下载完成通知安装
		    		mHandler.sendEmptyMessage(DOWN_OVER);
		    	}
				
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch(IOException e){
				e.printStackTrace();
			}
			
		}
	};
	
	
	 /**
     * 下载apk
     * @param url
     */
	
	private void downloadApk(){
		downLoadThread = new Thread(mdownApkRunnable);
		downLoadThread.start();
	}
	 /**
     * 安装apk
     * @param url
     */
	private void installApk(){
		File apkfile = new File(saveFileName);
        if (!apkfile.exists()) {
            return;
        }    
        Intent i = new Intent(Intent.ACTION_VIEW);
        Debugs.debug("filename = " + "file://" + apkfile.toString());
        //没有这句 安装好了，点打开，是不会打开新版应用的
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.setDataAndType(Uri.parse("file://" + apkfile.toString()), "application/vnd.android.package-archive");
        mContext.startActivity(i);
        //没有这句 最后不会提示完成,打开
        android.os.Process.killProcess(android.os.Process.myPid());
	}
	private int getApkFileSize(){
		HttpURLConnection conn = null;
		try{
			conn = (HttpURLConnection)(new URL(apkUrl)).openConnection();
			conn.setConnectTimeout(3000);
			conn .setRequestProperty("Accept-Encoding", "identity"); 	//设置不做gzip压缩  免得获取的大小为-1
			conn.setRequestMethod("GET");
			int size = 0;
			if(HttpURLConnection.HTTP_OK == conn.getResponseCode()){
				size = conn.getContentLength();
			}
			conn.disconnect();
			conn = null;
			return size;
		}catch(Exception e){
			return 0;
		}finally{
			if(conn != null){
				conn.disconnect();
				conn = null;
			}
		}
	}
}
