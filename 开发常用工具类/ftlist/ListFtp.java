package com.cniia.web.util;

import java.io.IOException;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

/*
 * 此类是直接可以运行，然后扫描
 * 我这扫描的ftp站点是自己电脑上建立的，外网扫描不到。
 * 如果要用此代码只需要改一下main函数里的ftp站点就能直接运行
 * */

public class ListFtp {

	public static boolean FtpRead(String url,int port,String username, String password, String path){
		 boolean success = false;  
	     FTPClient ftp = new FTPClient();  
	     //解码不然中文乱码
	     ftp.setControlEncoding("GBK");
	    try {  
	        int reply;  
	        ftp.connect(url);//连接FTP服务器  
	        //如果采用默认端口，可以使用ftp.connect(url)的方式直接连接FTP服务器  
	        ftp.login(username, password);//登录  

//这个方法的意思就是每次数据连接之前，ftp client告诉ftp server开通一个端口来传输数据。
//为什么要这样做呢，因为ftp server可能每次开启不同的端口来传输数据
			 ftp.enterLocalPassiveMode();//必须加入否则返回null


	        reply = ftp.getReplyCode();  
	        if (!FTPReply.isPositiveCompletion(reply)) {  
	            ftp.disconnect();  
	            return success;  
	        }  
	        ftp.changeWorkingDirectory(path);  
	  
	        FTPFile [] files = ftp.listFiles(path);      
	        for (int i = 0; i < files.length; i++) {      
	                if (files[i].isFile()) {      
	                    System.out.println(files[i].getName());      
	                } else if (files[i].isDirectory()) {    
	                    String currDirPath = path + files[i].getName() + "/";  
	                    ftp.changeWorkingDirectory(currDirPath);     
	                    listAllFiles(ftp,currDirPath);     
//	        input.close();  
	       // ftp.logout();  
	        success = true;  
	    } 
	  }
	    }catch (IOException e) {  
	        e.printStackTrace();  
	    } finally {  
	        if (ftp.isConnected()) {  
	            try {  
	                ftp.disconnect();  
	            } catch (IOException ioe) {  
	            }  
	        }  
	    }  
	    return success;  
		}
	        
	      public static void listAllFiles(FTPClient ftp,String remotePath) {         
	       try{ 
	        	if (true) {      
	                 if (remotePath.startsWith("/") && remotePath.endsWith("/")) {     
	                     FTPFile[] files = ftp.listFiles(remotePath);      
	                     for (int i = 0; i < files.length; i++) {      
	                             if (files[i].isFile()) { 
	                            	
	                            	 java.sql.Date date2 = new java.sql.Date(files[i].getTimestamp().getTime().getTime());
	         						//System.out.println("文件名:" + files[i].getName());
	         						System.out.println("文件路径:" +"ftp://172.29.250.21" + ftp.printWorkingDirectory()+"/"+files[i].getName());
	       				
	       					     	System.out.println("最后修改日期:" + date2);
//	         						System.out
//	         								.println("-----------------------------------");
	                            	 
	                                 System.out.println(files[i].getName());      
	                             } else if (files[i].isDirectory()) {  
	                            	 if (!files[i].getName().equals(".")
	             							&& !files[i].getName().equals("..")) {
	                            	 
	                                 String currDirPath = remotePath + files[i].getName() + "/";  
	                                 ftp.changeWorkingDirectory(currDirPath);     
	                                 listAllFiles(ftp,currDirPath);     
	                                 // System.out.println("监控工作目录切换:" + ftp.printWorkingDirectory());  
	                            	 }
	                             }      
	                     }      
	                 }       
	             }      
	        } catch (Exception e) {
				e.printStackTrace();
			 }
	        }
	      

	  	//与ftp断开连接
	  	public void ftpClose(FTPClient ftp) {
	  		try {
	  			ftp.logout();
	  		} catch (IOException e) {
	  			e.printStackTrace();
	  		}
	  		if (ftp.isConnected()) {
	  			try {
	  				ftp.disconnect();
	  			} catch (IOException ioe) {
	  				ioe.printStackTrace();
	  			}
	  		}
	  		System.out.println("与ftp断开连接!");
	  	} 
	      public static void main(String[] args) {
	    	  FtpRead("172.29.250.21", 23, "eee", "eee","/");
	    	
			
		}
	
}