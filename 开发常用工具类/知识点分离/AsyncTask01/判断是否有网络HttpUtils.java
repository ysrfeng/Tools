package com.qianfeng.day22_service1.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class HttpUtils {
	/**
	 * 判断是否有网络
	 * @param context
	 * @return
	 */
	public static boolean isNetWork(Context context){
		//得到网络的管理者
		ConnectivityManager manager = (ConnectivityManager) 
				context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = manager.getActiveNetworkInfo();
		
		if(info!=null){
			return true;
					
		}else{
			return false;
		}
		
	}

	
	/**
	 * 获取数据
	 * @param path
	 * @return
	 */
	public static byte[] getData(String path) {
		HttpClient httpClient = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(path);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try {
			HttpResponse response = httpClient.execute(httpGet);
			if (response.getStatusLine().getStatusCode() == 200) {
				InputStream inputStream = response.getEntity().getContent();
				byte[] buffer = new byte[1024];
				int temp = 0;
				while ((temp = inputStream.read(buffer)) != -1) {
					outputStream.write(buffer, 0, temp);
					outputStream.flush();
				}

			}
			return outputStream.toByteArray();

		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;

	}
	/**
在相应的module下的build.gradle中加入：
android {
    useLibrary 'org.apache.http.legacy'
}
注
*/
}
