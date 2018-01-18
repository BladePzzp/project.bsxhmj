package com.xxxxx.mj.tools;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
/**
 * 
 *md5���� ���μ��ܵ�ʱ��ע���С�?
 */
public class MD5 {
	public static String getMD5(String instr) {
		String s = null;
		
		char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9','a', 'b', 'c', 'd', 'e', 'f' };
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(instr.getBytes());
			byte tmp[] = md.digest(); 
										
			char str[] = new char[16 * 2]; 
											
			int k = 0; 
			for (int i = 0; i < 16; i++) { 
				byte byte0 = tmp[i]; 
				str[k++] = hexDigits[byte0 >>> 4 & 0xf];
				str[k++] = hexDigits[byte0 & 0xf]; 
			} 
			s = new String(str);
		} catch (Exception e) {

		}
		return s;
	}
	
	public static String getFileMD5(String filename){
		String s = null;
		char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9','A', 'B', 'C', 'D', 'E', 'F' };
		FileInputStream fis = null;
		try{
			MessageDigest md = MessageDigest.getInstance("MD5");
			fis = new FileInputStream(filename);
			byte[] buffer = new byte[2048];
			int length = -1;
			while((length = fis.read(buffer)) != -1){
				md.update(buffer, 0, length);
			}
			byte []b = md.digest();
			char str[] = new char[16 * 2]; 
			
			int k = 0; 
			for (int i = 0; i < 16; i++) { 
											
				byte byte0 = b[i]; 
				str[k++] = hexDigits[byte0 >>> 4 & 0xf];
				str[k++] = hexDigits[byte0 & 0xf]; 
			} 
			s = new String(str);
			return s;
		}catch(Exception e){
			return null;
		}finally{
			try {
				fis.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
