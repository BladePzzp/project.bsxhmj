package com.xxxxx.mj.tools;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;

public class SoundPlayer implements MediaPlayer.OnCompletionListener, OnPreparedListener{
	//声音资源编码 保存在本地记录
	private static SoundPlayer instance;
	private Context context;
	private MediaPlayer mp;			//背景音乐播放对象
	
	public static SoundPlayer getInstance(Context context){
		if(null == instance){
			instance = new SoundPlayer(context);
		}
		return instance;
	}
	
	public SoundPlayer(Context context){
		this.context = context;
	}
	
	public void playSoundByUrl(String url){
		try {
			Debugs.debug("playSoundByUrl url = " + url);
			if((null != this.mp) && (this.mp.isPlaying())){
				this.stopBackGroundSound();
			}
			if(null == this.mp){
//				Uri uri = Uri.parse(url);
				this.mp = new MediaPlayer();
				this.mp.reset();
				this.mp.setDataSource(url);
				this.mp.prepare();
				this.mp.setOnPreparedListener(this);
//				this.mp = MediaPlayer.create(this.context, uri);
				this.mp.setOnCompletionListener(this);
//				this.mp.start();
			}
		} catch (Exception e) {
			Debugs.debug("playSoundByUrl url err = " + e.toString());
		}
	}
	
	//停止背景音乐
	public void stopBackGroundSound(){
		Debugs.debug("stopBackGroundSound");
		if(null != this.mp){
			this.mp.stop();
			this.mp.release();
			this.mp = null;
		}
	}
	public void pauseBackGroundSound(){
		Debugs.debug("stopBackGroundSound");
		if(null != mp && mp.isPlaying()){
			mp.pause();
		}
	}
	public void resumeBackGroundSound(){
		try{
			Debugs.debug("resumeBackGroundSound");
			mp.stop();
			mp.prepare();
			mp.start();
		}catch(Exception e){
			Debugs.debug("resumeBackGroundSound err: " + e.toString());
		}
	}
	public void releaseResource(){
		this.stopBackGroundSound();
	}
	public void onCompletion(MediaPlayer arg0) {
		// TODO Auto-generated method stub
		Debugs.debug("onCompletion arg0：" + arg0);
		this.stopBackGroundSound();
	}

	@Override
	public void onPrepared(MediaPlayer arg0) {
		// TODO Auto-generated method stub
		Debugs.debug("onPrepared arg0：" + arg0);
		arg0.start();
	}
}
