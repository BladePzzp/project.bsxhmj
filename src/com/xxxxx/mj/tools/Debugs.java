package com.xxxxx.mj.tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.util.Log;

public class Debugs {
	public static boolean LogFlag = true;
	public static String LogTag = ConstVar.CAPTION;
	public static SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public static boolean createLogFlag = false;
	public static BufferedWriter bw;
	public static FileWriter fw;
	
	private static void createLog(){
		
		//add by zp ,由于每一次都会生成程序的日志，为了使以前的日志删掉，只保留一个当前运行的日志
		//		File dir = new File(ConstVar.filePath);
//		File[] files = dir.listFiles();
//
//		for(File f : files)
//		{
//			f.delete();
//		}
		
		//创建本地日志文件
		Date date = new Date();
		SimpleDateFormat sd1 = new SimpleDateFormat("HH_mm_ss");
		String str1 = sd1.format(date);
		
		final String savePath = ConstVar.filePath + str1 +  "_log.txt";
		System.out.println("文件名为:" + savePath);
		File file = new File(savePath);
		if(!file.exists()){
			try {
				file.createNewFile();
			} catch (IOException e) {
				System.out.println("createLog 1111:" + e.toString());
			}
		}
		try {
			fw = new FileWriter(savePath);
			bw = new BufferedWriter(fw);
		} catch (IOException e) {
			System.out.println("createLog 2222:" + e.toString());
		}
	}
	
	public static void closeFile(){
		try {
			if(LogFlag){
				createLogFlag = false;
				if(bw != null){
					bw.flush();
					bw.close();
				}
				if(fw != null){
					fw.close();
				}
			}
		} catch (IOException e){
			e.printStackTrace();
		}
	}
	public static void debug(String msg){
		if(LogFlag){
			if(!createLogFlag){
				createLog();
				createLogFlag = true;
			}
			Date date1 = new Date();
			String str1 = sd.format(date1);
			Log.v(LogTag, msg + "   time = " + str1);
			try {
				bw.write(msg + "  time = " + str1);
				bw.newLine();
				bw.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}catch(Exception e){
				
			}
		}
	}

}
