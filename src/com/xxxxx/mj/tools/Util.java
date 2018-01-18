package com.xxxxx.mj.tools;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import com.xxxxx.xinhe.EntryActivity;

import junit.framework.Assert;

import android.R.integer;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;

public class Util {
	
	private static final String TAG = "SDK_Sample.Util";
	
	private static final int IMAGE_SIZE = 3276800;
	
	public static Bitmap compressImage(Bitmap image){
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
		Debugs.debug("compressImage baos start length:" + baos.toByteArray().length + " options = " +100);
		int options = IMAGE_SIZE / baos.toByteArray().length;
		if(options < 100){
			baos.reset();
			image.compress(Bitmap.CompressFormat.JPEG, options, baos);
		}
		Debugs.debug("compressImage baos end length:" + baos.toByteArray().length + " options = " + options);
		ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
		Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);
		return bitmap;
	}
	
	public static Bitmap getSmallBitmap(String filePath){
		Debugs.debug("getSmallBitmap filePath:" + filePath);
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filePath, options);
		options.inSampleSize = calculateInsampleSize(options,480,800);
		options.inJustDecodeBounds = false;
		Debugs.debug("compressImage inSampleSize:" + options.inSampleSize);
		Bitmap bm = BitmapFactory.decodeFile(filePath, options);
//		return compressImage(bm);
		return bm;
	}
	
	public static Bitmap adjustImage(String absolutePath){
		Bitmap bm = null;
		BitmapFactory.Options opt = new BitmapFactory.Options();
		opt.inJustDecodeBounds = true;
		bm = BitmapFactory.decodeFile(absolutePath, opt);
		int picWidth = opt.outWidth;
		int picHeight = opt.outHeight;
		
		DisplayMetrics dm = new DisplayMetrics();    //获取屏幕分辨率
  		
		EntryActivity._instance.getWindowManager().getDefaultDisplay().getMetrics(dm);
  		// 得到屏幕的长和宽
  		int screenWidth = dm.widthPixels; // 水平分辨率
  		int screenHeight = dm.heightPixels; // 垂直分辨率
		
  		opt.inSampleSize = 1;
  		if (picWidth > picHeight) {
  			if (picWidth > screenWidth) {
  				opt.inSampleSize = picWidth / screenWidth;
  			}
  		} else {
  			if (picHeight > screenHeight) {
  				opt.inSampleSize = picHeight / screenHeight;
  			}
  		}
  		
  		opt.inJustDecodeBounds = false;
  		
  		File file = new File(absolutePath);
  		try {
			FileInputStream fs = new FileInputStream(file);
			byte[] bt = inputStreamToByte(fs);
			bm = BitmapFactory.decodeByteArray(bt, 0, bt.length, opt);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
  		return bm;
	}
	
	public static int calculateInsampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight){
		int height = options.outHeight;
		int width = options.outWidth;
		int inSampleSize = 1;
		if(height > reqHeight || width > reqWidth){
			int heightRatio = Math.round((float)height / (float)reqHeight);
			int widthRatio = Math.round((float)width / (float)reqWidth);
			
			inSampleSize = heightRatio < widthRatio ? widthRatio : heightRatio;
		}
		return inSampleSize;
	}
	
	public static byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		bmp.compress(CompressFormat.JPEG, 100, output);
		Debugs.debug("bmpToByteArray output start length:" + output.toByteArray().length + " options = " +100);
		int options = IMAGE_SIZE / output.toByteArray().length;
		if(options < 100){
			output.reset();
			bmp.compress(Bitmap.CompressFormat.JPEG, options, output);
		}
		
		Debugs.debug("bmpToByteArray baos end length:" + output.toByteArray().length + " options = " + options);
		if (needRecycle) {
			bmp.recycle();
		}
		
		byte[] result = output.toByteArray();
		try {
			output.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	public static byte[] getHtmlByteArray(final String url) {
		 URL htmlUrl = null;     
		 InputStream inStream = null;     
		 try {         
			 htmlUrl = new URL(url);         
			 URLConnection connection = htmlUrl.openConnection();         
			 HttpURLConnection httpConnection = (HttpURLConnection)connection;         
			 int responseCode = httpConnection.getResponseCode();         
			 if(responseCode == HttpURLConnection.HTTP_OK){             
				 inStream = httpConnection.getInputStream();         
			  }     
			 } catch (MalformedURLException e) {               
				 e.printStackTrace();     
			 } catch (IOException e) {              
				e.printStackTrace();    
		  } 
		byte[] data = inputStreamToByte(inStream);

		return data;
	}
	
	public static byte[] inputStreamToByte(InputStream is) {
		try{
			ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
			int ch;
			while ((ch = is.read()) != -1) {
				bytestream.write(ch);
			}
			byte imgdata[] = bytestream.toByteArray();
			bytestream.close();
			return imgdata;
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static byte[] readFromFile(String fileName, int offset, int len) {
		if (fileName == null) {
			return null;
		}

		File file = new File(fileName);
		if (!file.exists()) {
			Log.i(TAG, "readFromFile: file not found");
			return null;
		}

		if (len == -1) {
			len = (int) file.length();
		}

		Log.d(TAG, "readFromFile : offset = " + offset + " len = " + len + " offset + len = " + (offset + len));

		if(offset <0){
			Log.e(TAG, "readFromFile invalid offset:" + offset);
			return null;
		}
		if(len <=0 ){
			Log.e(TAG, "readFromFile invalid len:" + len);
			return null;
		}
		if(offset + len > (int) file.length()){
			Log.e(TAG, "readFromFile invalid file len:" + file.length());
			return null;
		}

		byte[] b = null;
		try {
			RandomAccessFile in = new RandomAccessFile(fileName, "r");
			b = new byte[len]; // ���������ļ���С������
			in.seek(offset);
			in.readFully(b);
			in.close();

		} catch (Exception e) {
			Log.e(TAG, "readFromFile : errMsg = " + e.getMessage());
			e.printStackTrace();
		}
		return b;
	}
	
	private static final int MAX_DECODE_PICTURE_SIZE = 1920 * 1440;
	public static Bitmap extractThumbNail(final String path, final int height, final int width, final boolean crop) {
		Assert.assertTrue(path != null && !path.equals("") && height > 0 && width > 0);

		BitmapFactory.Options options = new BitmapFactory.Options();

		try {
			options.inJustDecodeBounds = true;
			Bitmap tmp = BitmapFactory.decodeFile(path, options);
			if (tmp != null) {
				tmp.recycle();
				tmp = null;
			}

			Log.d(TAG, "extractThumbNail: round=" + width + "x" + height + ", crop=" + crop);
			final double beY = options.outHeight * 1.0 / height;
			final double beX = options.outWidth * 1.0 / width;
			Log.d(TAG, "extractThumbNail: extract beX = " + beX + ", beY = " + beY);
			options.inSampleSize = (int) (crop ? (beY > beX ? beX : beY) : (beY < beX ? beX : beY));
			if (options.inSampleSize <= 1) {
				options.inSampleSize = 1;
			}

			// NOTE: out of memory error
			while (options.outHeight * options.outWidth / options.inSampleSize > MAX_DECODE_PICTURE_SIZE) {
				options.inSampleSize++;
			}

			int newHeight = height;
			int newWidth = width;
			if (crop) {
				if (beY > beX) {
					newHeight = (int) (newWidth * 1.0 * options.outHeight / options.outWidth);
				} else {
					newWidth = (int) (newHeight * 1.0 * options.outWidth / options.outHeight);
				}
			} else {
				if (beY < beX) {
					newHeight = (int) (newWidth * 1.0 * options.outHeight / options.outWidth);
				} else {
					newWidth = (int) (newHeight * 1.0 * options.outWidth / options.outHeight);
				}
			}

			options.inJustDecodeBounds = false;

			Log.i(TAG, "bitmap required size=" + newWidth + "x" + newHeight + ", orig=" + options.outWidth + "x" + options.outHeight + ", sample=" + options.inSampleSize);
			Bitmap bm = BitmapFactory.decodeFile(path, options);
			if (bm == null) {
				Log.e(TAG, "bitmap decode failed");
				return null;
			}

			Log.i(TAG, "bitmap decoded size=" + bm.getWidth() + "x" + bm.getHeight());
			final Bitmap scale = Bitmap.createScaledBitmap(bm, newWidth, newHeight, true);
			if (scale != null) {
				bm.recycle();
				bm = scale;
			}

			if (crop) {
				final Bitmap cropped = Bitmap.createBitmap(bm, (bm.getWidth() - width) >> 1, (bm.getHeight() - height) >> 1, width, height);
				if (cropped == null) {
					return bm;
				}

				bm.recycle();
				bm = cropped;
				Log.i(TAG, "bitmap croped size=" + bm.getWidth() + "x" + bm.getHeight());
			}
			return bm;

		} catch (final OutOfMemoryError e) {
			Log.e(TAG, "decode bitmap failed: " + e.getMessage());
			options = null;
		}

		return null;
	}
	
	public static void makeToast(Context context, String msg, int duration,
			boolean gravity) {
		Toast toast = Toast.makeText(context, msg, duration);
		if (gravity) {
			toast.setGravity(17, 0, 0);
		}
		toast.show();
	}
	/**
	 * 获取当前的网络状态	   -1：没有网络 1：WIFI网络 2：wap网络 3：net网络
	 * @param context
	 * @return
	 */
	public static int getAPNType(Context context) {
		int netType = -1;
		ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		
		if(networkInfo == null) {
			return netType;
		}
		
		int nType = networkInfo.getType();
		if(nType == ConnectivityManager.TYPE_MOBILE) {
			Debugs.debug("networkInfo.getExtraInfo() is " + networkInfo.getExtraInfo());
			if(networkInfo.getExtraInfo().toLowerCase().equals("cmnet")) {
				netType = 3;
			} else {
				netType = 2;
			}
		} else if(nType == ConnectivityManager.TYPE_WIFI) {
			netType = 1;
		}
		return netType;
	}
}
