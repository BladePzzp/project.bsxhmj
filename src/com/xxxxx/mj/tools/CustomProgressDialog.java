package com.xxxxx.mj.tools;

import java.text.NumberFormat;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ProgressBar;
import android.widget.TextView;

public class CustomProgressDialog extends AlertDialog {
	private ProgressBar mProgressBar;
	private TextView mProgressNumber;
	private TextView mProgressPercent;
	private TextView mProgressMessage;
	private Handler mViewUpdateHandler;
	private int mMax;
	private CharSequence mMessage;
	private boolean mHasStarted;
	private int mProgressVal;
	private String TAG = "CustomProgressDialog";
	private String mProgressNumberFormat;
	private NumberFormat mProgressPercentFormat;
	
	protected CustomProgressDialog(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		initFormats();
	}
	
	private void initFormats() {
		mProgressNumberFormat = "%1.2fM/%2.2fM";
		mProgressPercentFormat = NumberFormat.getPercentInstance();
		mProgressPercentFormat.setMaximumFractionDigits(0);
	}
}
