package com.xxxxx.mj.tools;

import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;

public class InfoControl {
	private Context context;
	private SharedPreferences sp = null;
	private SharedPreferences.Editor editor = null;

	public InfoControl(Context context) {
		this.context = context;
	}

	// 参数表示含义，上下文对象、所存储的值的名称、所存储的值、存储的文件
	public void setContent(String key, String value, String fileName) {
		if (null == sp) {
			sp = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
		}
		if (null == editor) {
			editor = sp.edit();
		}
		editor.putString(key, value);
		editor.commit();
	}

	public void deleteContent(String key, String filename) {
		if (null == sp) {
			sp = context.getSharedPreferences(filename, Context.MODE_PRIVATE);
		}
		if (null == editor) {
			editor = sp.edit();
		}
		editor.remove(key);
		editor.commit();
	}

	public void setContent(String key, Boolean value, String fileName) {
		if (null == sp) {
			sp = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
		}
		if (null == editor) {
			editor = sp.edit();
		}
		editor.putBoolean(key, value);
		editor.commit();
	}

	public String getValue(String key, String fileName) {
		if (null == sp) {
			sp = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
		}
		String str = sp.getString(key, null);
		return str;
	}

	public Map<String, ?> getName(String filename) {
		if (null == sp) {
			sp = context.getSharedPreferences(filename, Context.MODE_PRIVATE);
		}
		return (Map<String, ?>) sp.getAll();
	}
}
