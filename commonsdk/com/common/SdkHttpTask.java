package com.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.NameValuePair;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class SdkHttpTask extends AsyncTask<String, Void, String> {
    private static final String TAG = "SdkHttpTask";
    
    private static final int MAX_RETRY_TIME = 3;
    
    private int mRetryCount;

    private SdkHttpListener mListener;

    private ArrayList<NameValuePair> mKeyValueArray;

    private boolean mIsHttpPost;

    private Context mContext;

    public SdkHttpTask(Context context) {
    	Log.d("SdkHttpTask", "init context");
        mContext = context;
    }

    public void doPost(SdkHttpListener listener, ArrayList<NameValuePair> keyValueArray,
            String url) {
        this.mListener = listener;
        this.mIsHttpPost = true;
        this.mKeyValueArray = keyValueArray;
        this.mRetryCount = 0;

        execute(url);
    }

    public void doGet(SdkHttpListener listener, String url) {
        this.mListener = listener;
        this.mIsHttpPost = false;
        this.mRetryCount = 0;

        execute(url);
    }

     
    protected String doInBackground(String... params) {
        
        String response = null;
        while (response == null && mRetryCount < MAX_RETRY_TIME) {

            if (isCancelled())
                return null;

                String uri = params[0];
                
                Log.d("SdkHttpTask", this.toString() + "||mRetryCount=" + mRetryCount);
                Log.d("SdkHttpTask", this.toString() + "||request=" + uri);
                //HttpResponse httpResp = executeHttp(mContext, uri);
                InputStream content = SdkTool.getHttpResponse(uri, "UTF-8");
                Log.d("SdkHttpTask", "content = " + content);
                if (content != null && !isCancelled()) {
                	response = convertStreamToString(content);
                }
            
            Log.d("SdkHttpTask", this.toString() + "||response=" + response);
            
            mRetryCount++;
        }

        return response;
    }

     
    protected void onCancelled() {
        super.onCancelled();

        if (mListener != null) {
            Log.d("SdkHttpTask", this.toString() + "||onCancelled");
            mListener.onCancelled();
            mListener = null;
        }
    }

     
    protected void onPostExecute(String response) {
        super.onPostExecute(response);

        if (mListener != null && !isCancelled()) {
            Log.d("SdkHttpTask", this.toString() + "||onResponse");
            mListener.onResponse(response);
            mListener = null;
        }
    }


    private static String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
}
