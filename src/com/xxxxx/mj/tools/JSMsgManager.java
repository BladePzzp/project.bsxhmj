package com.xxxxx.mj.tools;

import org.egret.egretframeworknative.engine.EgretGameEngine;

public class JSMsgManager {
	private static JSMsgManager instance = null;
	public static synchronized JSMsgManager getInstance() {
		if(null == instance) {
			instance = new JSMsgManager();
		}
		return instance;
	}
	
	public void sendMsg(EgretGameEngine gameEngine, String sendString){
		gameEngine.callEgretInterface("sendToJS", sendString);
	}
}
