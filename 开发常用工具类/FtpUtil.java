package com.zeone.inspection.ftp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import com.zeone.inspection.R;
import com.zeone.inspection.util.CommonFun;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;

/**
 * Ftp访问工具
 */
public class FtpUtil {

	public static final int Ftp_Upload_Progress = 10000001;
	public static final int Ftp_Upload_Complete = 10000002;
	public static final int Ftp_Upload_Failure = 10000003;
	public static final int Ftp_Upload_Error = 10000004;
	public static final int Ftp_Download_Progress = 10000005;
	public static final int Ftp_Download_Complete = 10000006;
	public static final int Ftp_Download_Failure = 10000007;
	public static final int Ftp_Download_Error = 10000008;
	public static final int Ftp_DisConnect = 10000009;
	/** 是否强制停止文件上传 */
	public static boolean stopUpload = false;
	/** 是否强制停止文件上传 */
	public static boolean stopSingleUpload = false;

	/**
	 * 连接FTP服务器
	 * @param serverUrl
	 * @param port
	 * @param username
	 * @param password
	 * @return FTPClient对象
	 */
	public static FTPClient connectFtp(String serverUrl, int port,
									   String username, String password) {

		// serverUrl = EncryptUtil.decode(serverUrl);
		// username = EncryptUtil.decode(username);
		// password =EncryptUtil.decode(password);

		FTPClient ftp = new FTPClient();
		ftp.setControlEncoding("GBK");
		try {
			if (port == 0) {
				ftp.connect(serverUrl);
			} else {
				ftp.connect(serverUrl, port);
			}
			if (ftp.login(username, password)) {
				FTPClientConfig conf = new FTPClientConfig(getSystemKey(ftp.getSystemName()));
				conf.setServerLanguageCode("zh");
				ftp.configure(conf);
				int reply = ftp.getReplyCode();
				if (!FTPReply.isPositiveCompletion(reply)) {
					ftp.disconnect();
					ftp.logout();
				} else {
					ftp.enterLocalPassiveMode();
					ftp.setFileType(FTP.BINARY_FILE_TYPE);
					return ftp;
				}
			} else {
				return null;
			}
		} catch (IOException e) {

			return null;
		}
		return null;
	}

	public static void uploadFile(FTPClient ftp, String serverDir,
								  String localFile, Handler handler, String message) {
		uploadFile(ftp, serverDir, new File(localFile), handler, message);
	}

	/**
	 * 上载本地文件到FTP目录
	 * @param ftp
	 * @param serverDir FTP目录 根目录留空
	 * @param localFile 本地文件
	 * @param handler 回发消息
	 *
	 */
	public static void uploadFile(FTPClient ftp, String serverDir,
								  File localFile, Handler handler, String message) {
		if (ftp == null)
			return;
		try {
			ftp.initiateListParsing();
			FTPFile[] remoteFiles = ftp.listFiles();
			if (localFile.isFile()) {
				String name = localFile.getName();

				// 假如服务器不存在这个目录，则创建它
				// (只支持mkdir,不支持mkdirs的多级目录创建)
				if (!serverDir.equals("")) {
					if (ftp.listFiles(serverDir) == null
							|| ftp.listFiles(serverDir).length == 0) {
						ftp.makeDirectory(serverDir);
					}
				}

				OutputStream os = ftp.storeFileStream(serverDir + name);

				if (os != null) {

					// 打开本地文件供读写
					RandomAccessFile raf = new RandomAccessFile(localFile, "rw");

					// 如果远程文件存在,并且小于当前文件大小
					FTPFile remoteFile = existsFile(remoteFiles, localFile);
					if (remoteFile != null
							&& raf.length() >= remoteFile.getSize()) {
						raf.seek(remoteFile.getSize());
					}

					// 写入远程文件
					long upLoadFileSize = 0;
					byte[] buf = new byte[1024];
					do {
						// 循环读取,每次读取1kb
						int numread = raf.read(buf);
						if (numread == -1) {
							break;
						}
						os.write(buf, 0, numread);
						os.flush();
						upLoadFileSize += numread;

						// 上传进度
						if (message != null && handler != null) {
							// HandlerUtil.updateTitle(handler, message
							// + (upLoadFileSize / 1024) + "kb");
							Message msg = handler.obtainMessage(
									Ftp_Upload_Progress, message
											+ (upLoadFileSize / 1024) + "kb");
							handler.sendMessage(msg);
						}
					} while (true);

					// 关闭数据流
					raf.close();
					os.close();

					if (ftp.completePendingCommand()) {
						System.out.println("ftp: upload file completed!");
						if (handler != null) {
							Message msg = handler
									.obtainMessage(Ftp_Upload_Complete);
							handler.sendMessage(msg);
						}
					} else {
						System.out.println("ftp:can't put file:" + name
								+ ",completePendingCommand fail");
						if (handler != null) {
							Message msg = handler.obtainMessage(
									Ftp_Upload_Failure, "无法完成文件写入操作");
							handler.sendMessage(msg);
						}
					}
				} else {
					System.out.println("ftp:can't put file:" + name
							+ ",OutputStream is null");
					if (handler != null) {
						Message msg = handler.obtainMessage(Ftp_Upload_Failure,
								"无法向Ftp服务器写入文件");
						handler.sendMessage(msg);
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			if (handler != null) {
				Message msg = handler.obtainMessage(Ftp_Upload_Error,
						e.getMessage());
				handler.sendMessage(msg);
			}

		} catch (Exception e) {
			e.printStackTrace();
			if (handler != null) {
				Message msg = handler.obtainMessage(Ftp_Upload_Error,
						e.getMessage());
				handler.sendMessage(msg);
			}

		}
	}

	/**
	 * 上传多个图片到ftp
	 */
	public static void uploadPolicePics(FTPClient ftp, String serverDir,
										String[] serverName, File[] localFiles, Handler handler) {
		boolean uploadRes = false;
		if (ftp == null) {
			Message msg = handler.obtainMessage(Ftp_Upload_Failure,
					"连接失败,请查看手机网络状况");
			handler.sendMessage(msg);
			return;
		}
		try {
			for (int i = 0; i < localFiles.length; i++) {
				File file = localFiles[i];
				if (file.isFile()) {
					boolean result = uploadPolicePicOne(ftp, serverDir,
							serverName[i], file);
					if (result == false) {
						uploadRes = false;
						Message msg = handler.obtainMessage(Ftp_Upload_Failure);
						handler.sendMessage(msg);
						break;
					} else {
						uploadRes = true;
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		if (uploadRes) {
			Message msg = handler.obtainMessage(Ftp_Upload_Complete);
			handler.sendMessage(msg);
		} else {
			Message msg = handler.obtainMessage(Ftp_Upload_Failure);
			handler.sendMessage(msg);
		}

	}

	/**
	 * 简单上传,上传单个文件
	 * @param ftp ftp客户端对象
	 * @param serverDir 服务端存储目录 如/sbxj/
	 * @param serverName 服务端保存文件名
	 * @param localFile 本地文件
	 * @return
	 */
	public static boolean uploadPolicePicOne(FTPClient ftp, String serverDir,
											 String serverName, File localFile) {
		try {
			ftp.initiateListParsing();
			FTPFile[] remoteFiles = ftp.listFiles();
			if (localFile.isFile()) {
				String name = localFile.getName();

				// 假如服务器不存在这个目录，则创建它
				// (只支持mkdir,不支持mkdirs的多级目录创建)
				if (!serverDir.equals("")) {
					if (ftp.listFiles(serverDir) == null
							|| ftp.listFiles(serverDir).length == 0) {
						ftp.makeDirectory(serverDir);
						System.out.println("ftp: upload file,create directory!");
					}
				}
				Thread.sleep(100);
				OutputStream os = ftp.storeFileStream(serverDir + serverName);

				if (os != null) {
					// 打开本地文件供读写
					RandomAccessFile raf = new RandomAccessFile(localFile, "rw");

//					// 如果远程文件存在,并且小于当前文件大小
//					FTPFile remoteFile = existsFile(remoteFiles, localFile);
//					if (remoteFile != null
//							&& raf.length() >= remoteFile.getSize()) {
//						raf.seek(remoteFile.getSize());
//					}

					// 写入远程文件
					long upLoadFileSize = 0;
					byte[] buf = new byte[1024 * 320];
					do {
						// 循环读取,每次读取1kb
						int numread = raf.read(buf);
						if (numread == -1) {
							break;
						}
						os.write(buf, 0, numread);
						os.flush();
						upLoadFileSize += numread;

						// 上传进度
					} while (!stopUpload);

					// 关闭数据流
					raf.close();
					os.close();

					if (ftp.completePendingCommand() && !stopUpload) {
						System.out.println("ftp: upload file completed!");
						return true;
					} else {
						System.out.println("ftp:can't put file:" + name+ ",completePendingCommand fail");
						return false;
					}
				} else {
					System.out.println("ftp:can't put file:" + name+ ",OutputStream is null");
					return false;
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return false;
	}


	/**
	 * 使用ftp上传单个文件
	 * @param ftp ftp对象,必须先调用connectFtp方法
	 * @param serverDir 服务端存储目录,如"/sbxj/"
	 * @param serverName 服务端文件名，如"test.jpg"
	 * @param file 上传的文件对象
	 * @param listener 上传过程监听
	 */
	public static void uploadSingleFile(FTPClient ftp, String serverDir,
										String serverName,UploadFtpFile file, UploadFileListener listener) {
		if (ftp == null) {
			listener.onDisConnected(file, "连接失败,请查看网络状况");
			return;
		}
		try {
			// ftp.initiateListParsing();
			// FTPFile[] remoteFiles = ftp.listFiles();
			if (file != null) {
				File localFile = new File(file.getLocalpath());
				String name = localFile.getName();
				// 假如服务器不存在这个目录，则创建它
				// (只支持mkdir,不支持mkdirs的多级目录创建)
				FTPFile[] listfs = null;
				if (!"".equals(serverDir)) {
					listfs = ftp.listFiles(serverDir);
					if ( listfs == null|| listfs.length == 0) {
						ftp.makeDirectory(serverDir);
						System.out.println("ftp: upload file,create directory!");
					}

					String lastchar = serverDir.substring(serverDir.length() -1 );
					if(!"\\".equals(lastchar) && !"/".equals(lastchar)){
						serverDir += "/";
					}
				}

				// 已写入远程文件的字节数
				long upLoadFileSize = 0;
				// 断点续传,如果远程文件存在,并且小于当前文件大小===================================================
				FTPFile remoteFile = existsFile(listfs, localFile);
				if (remoteFile != null
						&& localFile.length() == remoteFile.getSize()) {
					//raf.seek(remoteFile.getSize());
					upLoadFileSize = remoteFile.getSize();
					// 断点续传不成功,但是可以文件名相同尺寸相同的文件不重复上传
					listener.onProgress(file, localFile.length(),upLoadFileSize);
					Thread.sleep(500);
					listener.onCompleted(file);
					return;
				}
				//=============================================================================

				OutputStream os = ftp.storeFileStream(serverDir + serverName);

				if (os != null) {

					// 打开本地文件供读写
					RandomAccessFile raf = new RandomAccessFile(localFile, "rw");

					byte[] buf = new byte[1024*320];
					do {
						// 循环读取,每次读取320kb
						int numread = raf.read(buf);
						if (numread == -1) {
							break;
						}
						os.write(buf, 0, numread);
						os.flush();
						upLoadFileSize += numread;

						listener.onProgress(file, localFile.length(),upLoadFileSize);
					} while (!stopSingleUpload);

					// 关闭数据流
					raf.close();
					os.close();

					if (ftp.completePendingCommand() && !stopUpload) {
						System.out.println("ftp: upload file completed!");
						listener.onCompleted(file);
					} else {
						System.out.println("ftp:can't put file:" + name+ ",completePendingCommand fail");
						listener.onFailure(file, "向FTP服务器存储文件失败");
					}
				} else {
					System.out.println("ftp:can't put file:" + name+ ",OutputStream is null");

					listener.onFailure(file, "无法向FTP服务器写入文件");
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			listener.onError(file, e.getMessage());

		} catch (Exception e) {
			e.printStackTrace();
			listener.onError(file, e.getMessage());
		}
	}

	/**
	 *
	 * 上载本地目录到FTP目录
	 *
	 * @param ftp
	 * @param serverDir
	 *            FTP目录 根目录留空
	 * @param localDir
	 *            本地目录
	 * @throws IOException
	 */
	private static void uploadFiles(FTPClient ftp, String serverDir,
									File localDir) {
		if (ftp == null)
			return;
		try {
			ftp.initiateListParsing();

			FTPFile[] remoteFiles = ftp.listFiles();

			File[] localFiles = localDir.listFiles();

			for (File file : localFiles) {
				if (file.isFile()) {
					String name = file.getName();
					int count = ftp.list(serverDir);
					if (count == 0) {
						ftp.makeDirectory(serverDir);
					}

					OutputStream os = ftp.storeFileStream(serverDir + name);

					if (os != null) {
						RandomAccessFile raf = new RandomAccessFile(file, "rw");
						// 如果远程文件存在,并且小于当前文件大小
						FTPFile remoteFile = existsFile(remoteFiles, file);
						if (remoteFile != null
								&& raf.length() >= remoteFile.getSize()) {
							raf.seek(remoteFile.getSize());
						}
						byte[] buf = new byte[1024];
						do {
							// 循环读取,每次读取1kb
							int numread = raf.read(buf);
							if (numread == -1) {
								break;
							}
							os.write(buf, 0, numread);
							os.flush();
						} while (true);

						raf.close();
						os.close();
						if (ftp.completePendingCommand()) {
							System.out.println("done!");
						} else {
							System.out.println("can't put file:" + name);
						}
					} else {
						System.out.println("can't put file:" + name);
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
			// Message msg =
			// handler.obtainMessage(Ftp_Upload_Error,e.getMessage());
			// handler.sendMessage(msg);
		}
	}

	/**
	 * 测试ftp是否连接成功
	 * @param ip
	 * @param port
	 * @param user
	 * @param pwd
	 * @param path
	 * @return
	 */
	public static boolean TestFtp(String ip,int port,String user,String pwd,String path){
		FTPClient ftp = FtpUtil.connectFtp(ip, port, user, pwd);
		if(ftp==null){
			return false;
		}
		if(!path.equals("")){
			FTPFile[] files = null;
			try {
				files = ftp.listFiles(path);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				return false;
			}
		}
		try {
			ftp.logout();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;

	}

	/**
	 *
	 * 下载FTP文件到本地文件,2012-02-24修改支持进度条
	 *
	 * @param ftp
	 *            ftp连接对象
	 * @param serverFile
	 *            服务端文件路径名称
	 * @param localFile
	 *            本地保存路径名称
	 * @param handler
	 *            回发处理
	 * @param message
	 *            下载进度提示消息, message="*%*"表示Handler将返回 long数组{文件总大小,已下载,完成百分比}。
	 *            message是其他字符,Handler只返回一段话
	 */
	public static void downloadFile(FTPClient ftp, String serverFile,
									File localFile, Handler handler, String message) {
		long totalSize = 0;
		String logs = "";
		if (ftp == null) {
			if (handler != null) {
				Message msg = handler.obtainMessage(Ftp_Download_Error,
						"无法连接FTP服务器");
				handler.sendMessage(msg);
			}
			return;
		}
		try {
			// 获取文件尺寸------------------------------------
			serverFile = serverFile.replace("\\", "/");
			int idx = serverFile.lastIndexOf("/");
			String serverPath = serverFile.substring(0, idx);
			String fileName = serverFile.substring(idx + 1);

			System.out.println("ftp:try to get file,fullpath:" + serverFile
					+ ",path:" + serverPath + ",filename:" + fileName);
			FTPFile[] files = null;
			try {
				files = ftp.listFiles(serverPath);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (files != null && files.length > 0) {
				for (FTPFile ftpFile : files) {
					if (fileName.equals(ftpFile.getName())) {
						totalSize = ftpFile.getSize();
						System.out.println("ftp:file total size:" + totalSize);
						break;
					}
				}
			}// ------------------------------------------------

			if (!localFile.exists()) {
				localFile.createNewFile();
			} else {
				localFile.delete();
				localFile.createNewFile();
			}
			long pos = localFile.length();
			RandomAccessFile raf = new RandomAccessFile(localFile, "rw");
			raf.seek(pos);
			ftp.setRestartOffset(pos);

			// ftp.changeWorkingDirectory("/");
			InputStream is = ftp.retrieveFileStream(serverFile);
			if (is == null) {
				System.out.println("ftp:no such file:" + serverFile);
				localFile.delete();
				if (handler != null) {
					Message msg = handler.obtainMessage(Ftp_Download_Failure,
							"Ftp服务器中文件不存在");
					handler.sendMessage(msg);
				}
			} else {
				long downLoadFileSize = 0;
				byte[] buf = new byte[1024 * 25];// 速度快慢根据网络调整
				do {
					// 循环读取,每次读取1kb
					int numread = is.read(buf);
					if (numread == -1) {
						break;
					}
					raf.write(buf, 0, numread);
					downLoadFileSize += numread;

					if (message != null && handler != null) {

						if (message.equals("*%*")) {// 计算下载完成百分比
							long ywc = downLoadFileSize;
							long zcd = totalSize;
							long percent = 0;
							if (zcd <= 0 || ywc <= 0) {
								percent = 0;
							} else {
								long t = ywc * 100 / zcd;
								percent = t;
							}
							if (percent > 100)
								percent = 100;
							Message msg = handler.obtainMessage(
									Ftp_Download_Progress, new long[] { zcd,
											ywc, percent });
							handler.sendMessage(msg);
						} else {// 直接提示一段话
							Message msg = handler.obtainMessage(
									Ftp_Download_Progress, message
											+ (downLoadFileSize / 1024) + "KB");
							handler.sendMessage(msg);
						}
					}
					try {
						Thread.sleep(50);
					} catch (Exception e) {
					}
				} while (true);

				is.close();
				if (ftp.completePendingCommand()) {
					System.out.println("ftp:download file completed!");
					if (handler != null) {
						Message msg = handler.obtainMessage(
								Ftp_Download_Complete, localFile);
						handler.sendMessage(msg);
					}
				} else {
					System.out.println("ftp:can't get file:" + serverFile);
					localFile.delete();
					if (handler != null) {
						Message msg = handler.obtainMessage(
								Ftp_Download_Failure, "无法下载文件");
						handler.sendMessage(msg);
					}
				}
			}
			raf.close();
		} catch (IOException e) {
			e.printStackTrace();
			if (!localFile.exists())
				localFile.delete();
			if (handler != null) {
				Message msg = handler.obtainMessage(Ftp_Download_Error,
						e.getMessage());
				handler.sendMessage(msg);
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (!localFile.exists())
				localFile.delete();
			if (handler != null) {
				Message msg = handler.obtainMessage(Ftp_Download_Error,
						e.getMessage());
				handler.sendMessage(msg);
			}
		}
	}

	/**
	 * 下载ftp文件 不带进度条
	 * @param ftp
	 * @param serverFile 服务端路径
	 * @param localFile 本地文件
	 * @return true 下载成功  false下载失败
	 */
	public static boolean downloadFile(FTPClient ftp, String serverFile,File localFile ) {
		long totalSize = 0;
		String ftps = "";
		if (ftp == null) {
			return false;
		}
		try {
			// 获取文件尺寸------------------------------------
			serverFile = serverFile.replace("\\", "/");
			int idx = serverFile.lastIndexOf("/");
			String serverPath = serverFile.substring(0, idx);
			String fileName = serverFile.substring(idx + 1);

			// 本地路径不存在创建
			String localpath = localFile.getAbsolutePath().replace("\\", "/");
			int idx1 = localpath.lastIndexOf("/");
			String localpath1 = localpath.substring(0, idx1);
			File dir = new File(localpath1);
			if(!dir.exists()){
				dir.mkdirs();
			}
			ftps = "ftp:try to get file,fullpath:" + serverFile+ ",path:" + serverPath + ",filename:" + fileName;
			System.out.println("ftp:try to get file,fullpath:" + serverFile+ ",path:" + serverPath + ",filename:" + fileName);
//			FTPFile[] files = null;
//			try {
//				files = ftp.listFiles(serverPath);
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//				return false;
//			}
//			if (files != null && files.length > 0) {
//				for (FTPFile ftpFile : files) {
//					if (fileName.equals(ftpFile.getName())) {
//						totalSize = ftpFile.getSize();
//						System.out.println("ftp:file total size:" + totalSize);
//						break;
//					}
//				}
//			}// ------------------------------------------------

			// 此行删除 可能导致下载失败抛出 open failed: EBUSY (Device or resource busy)异常
//			if (!localFile.exists()) {
//				localFile.createNewFile();
//			} else {
//				localFile.delete();
//				localFile.createNewFile();
//			}
			//--------------------------------------------------------------------

//			long pos = localFile.length();
			RandomAccessFile raf = new RandomAccessFile(localFile, "rw");
			ftps += "\t\n ===RandomAccessFile ok!";
//			raf.seek(pos);
//			ftp.setRestartOffset(pos);

			// ftp.changeWorkingDirectory("/");
			InputStream is = ftp.retrieveFileStream(serverFile);

			ftps += "\t\n ===ftp.retrieveFileStream ok!";

			if (is == null) {
				System.out.println("ftp:no such file:" + serverFile);
				ftps += "\t\n===FTP服务器上没有此文件:" + serverFile;
				CommonFun.WriteJson2TxtFile(ftps,
						Environment.getExternalStorageDirectory()+"/.adinspection/logs/ftp下载失败1.txt");
				localFile.delete();
				raf.close();
				return false;
			} else {
				ftps += "\t\n ===InputStream is not null!";
				long downLoadFileSize = 0;
				byte[] buf = new byte[1024 * 320];// 速度快慢根据网络调整
				do {
					// 循环读取,每次读取1kb
					int numread = is.read(buf);
					if (numread == -1) {
						break;
					}
					raf.write(buf, 0, numread);
					downLoadFileSize += numread;

//					try {
//						Thread.sleep(50);
//					} catch (Exception e) {
//					}
				} while (true);

				is.close();
				ftps += "\t\n ===InputStream is close ok!";
				raf.close();
				ftps += "\t\n ===RandomAccessFile is close ok!";
				if (ftp.completePendingCommand()) {
					System.out.println("ftp:download file completed!");
					CommonFun.WriteJson2TxtFile(ftps+"\t\ftp.completePendingCommand ok===>>>下载完成！",
							Environment.getExternalStorageDirectory()+"/.adinspection/logs/ftp下载成功0.txt");
					return true;
				} else {
					System.out.println("ftp:can't get file:" + serverFile);
					localFile.delete();
					return false;
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
			CommonFun.WriteJson2TxtFile(ftps+"\t\nftp下载失败："+e.getMessage(),
					Environment.getExternalStorageDirectory()+"/.adinspection/logs/ftp下载失败2.txt");
			if (!localFile.exists())localFile.delete();
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			CommonFun.WriteJson2TxtFile(ftps+"\t\nftp下载失败："+e.getMessage(),
					Environment.getExternalStorageDirectory()+"/.adinspection/logs/ftp下载失败3.txt");
			if (!localFile.exists())localFile.delete();
			return false;
		}
	}

	/**
	 * 判断FTP服务器上是否存在某文件
	 *
	 * @param ftp
	 * @param serverDir
	 * @param serverFile
	 * @return
	 *
	 *
	 */
	public static boolean checkFileExist(FTPClient ftp, String serverDir,
										 String serverFile) {
		FTPFile[] files = null;
		try {
			files = ftp.listFiles(serverDir);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (files != null && files.length > 0) {
			for (FTPFile ftpFile : files) {
				if (serverFile.equals(ftpFile.getName())) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 找到FTP目录下某种类型文件
	 *
	 * @param ftp
	 * @param serverDir
	 * @param fileType
	 *            如：zip类型
	 * @return
	 *
	 *
	 */
	public static String[] findSDCardFile(FTPClient ftp, String serverDir,
										  final String fileType) {
		FTPFile[] files = null;
		try {
			files = ftp.listFiles(serverDir);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		List<String> list = new ArrayList<String>();
		if (files != null && files.length > 0) {
			for (FTPFile ftpFile : files) {
				if (ftpFile.getName().endsWith(fileType)) {
					list.add(ftpFile.getName());
				}
			}
		}
		Collections.sort(list, new Comparator<String>() {
			@Override
			public int compare(String str1, String str2) {
				return str2.compareTo(str1);
			}
		});
		String[] ret = new String[list.size()];
		return list.toArray(ret);
	}

	/**
	 * 创建FTP上目录
	 *
	 * @param serverDir
	 * @return
	 *
	 *
	 */
	public static boolean createDir(FTPClient ftp, String serverDir) {
		FTPFile[] files = null;
		try {
			files = ftp.listFiles(serverDir);
			if (files == null || files.length == 0) {
				ftp.makeDirectory(serverDir);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

	/**
	 * 退出FTP服务器
	 *
	 * @param ftp
	 *
	 */
	public static void logout(FTPClient ftp) {
		try {
			if (ftp != null && ftp.isConnected()) {
				ftp.logout();
				ftp.disconnect();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 *
	 * @Function:
	 *
	 * @param systemName
	 * @return
	 *
	 * @author Louis Feb 26, 2009
	 *
	 */
	private static String getSystemKey(String systemName) {
		String[] values = systemName.split(" ");
		if (values != null && values.length > 0) {
			return values[0];
		} else {
			return null;
		}
	}

	/**
	 *
	 * 文件是否存在ftp目录中
	 *
	 * @param remoteFiles
	 * @param file
	 * @return
	 *
	 * @author Louis Feb 26, 2009
	 *
	 */
	private static FTPFile existsFile(FTPFile[] remoteFiles, File file) {
		for (FTPFile remoteFile : remoteFiles) {
			if (file.getName().equals(remoteFile.getName())) {
				return remoteFile;
			}
		}
		return null;
	}

	/**
	 * 删除FTP上某个文件
	 *
	 * @param ftp
	 * @param serverDir
	 * @param serverFile
	 *
	 * @author Louis on 2010-12-17 & memoryCat V1.0
	 *
	 */
	public static void deleteFile4FTP(FTPClient ftp, String serverDir,
									  String serverFile) {

		try {
			ftp.deleteFile(serverDir + "/" + serverFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void setStopFtp(boolean tag) {
		FtpUtil.stopUpload = tag;
	}

	/**
	 * 文件尺寸单位转换
	 * @param lsize 字节数
	 * @return
	 */
	public static String GetRealSize(long lsize) {
		double size = (double) lsize;
		double kb = 1024; // Kilobyte
		double mb = 1024 * kb; // Megabyte
		double gb = 1024 * mb; // Gigabyte
		double tb = 1024 * gb; // Terabyte

		DecimalFormat dcf = new DecimalFormat("0.00");
		double c = 0.00;
		String s = "";
		if (size < kb) {
			s = size + " B";
		} else if (size < mb) {
			c = size / kb;
			s = dcf.format(c) + " KB";
		} else if (size < gb) {
			c = size / mb;
			s = dcf.format(c) + " MB";
		} else if (size < tb) {
			c = size / gb;
			s = dcf.format(c) + " GB";
		} else {
			c = size / tb;
			s = dcf.format(c) + " TB";
		}
		return s;
	}
}