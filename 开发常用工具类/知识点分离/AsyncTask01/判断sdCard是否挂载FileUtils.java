package com.qianfeng.day22_service1.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.os.Environment;

public class FileUtils {
	
	/**
	 * 判断sdCard是否挂载
	 * @return
	 */
	public static boolean isConnSdCard(){
		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
			return true;
		}
		return false;
	}
	/**
	 * 将图片的字节数组保存到  sd卡上
	 * @return
	 */
	public static boolean writeToSdcard(byte[] buffer ,String imagName){
		FileOutputStream outputStream =null;
		
		boolean flag = false;
		
		String fileName = imagName.substring(imagName.lastIndexOf("/")+1);
		File file = new File(Environment.
				getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
				fileName);
		try {
			
			outputStream = new FileOutputStream(file);
			outputStream.write(buffer);
			flag = true;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if(outputStream!=null){
				try {
					outputStream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		return flag;
		
	}

}
