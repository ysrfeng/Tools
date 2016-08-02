package com.zeone.inspection.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.zeone.inspection.InspectionAPP;
import com.zeone.inspection.R;
import com.zeone.inspection.adapter.TreeAdapter;
import com.zeone.inspection.comm.CommError;
import com.zeone.inspection.comm.TreeItemClickListener;
import com.zeone.inspection.po.MediaEntity;
import com.zeone.inspection.po.Node;
import com.zeone.inspection.ui.pop.BasePOP;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Bitmap.Config;
import android.media.ThumbnailUtils;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

/***
 * 常用方法集合
 * @author shaolong
 * @date 2014-01-03
 */
public class CommonFun {

	/**网络是否可用*/
	public static boolean isNetworkAvailable(Context context) {
		// 获得连接管理对象
		ConnectivityManager connectivity = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		if (connectivity == null) {
			return false;
		} else {

			// 获得所有联系信息
			NetworkInfo[] info = connectivity.getAllNetworkInfo();

			if (info != null) {
				for (int i = 0; i < info.length; i++) {
					NetworkInfo ni = info[i];
					if (ni.getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
				}
			}
		}
		return false;
	}
	/**
	 * 检查存储卡是否插入
	 * @return
	 */
	public static boolean isHasSdcard() {
		String status = Environment.getExternalStorageState();
		if (status.equals(Environment.MEDIA_MOUNTED)) {
			return true;
		} else {
			return false;
		}
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

	/**
	 * 提示
	 * @param msg 字符串
	 */
	public static void toastMsg(String msg) {
		Toast.makeText(InspectionAPP.getInstance(), msg, Toast.LENGTH_SHORT).show();
	}
	/**
	 * 提示
	 * @param resid string.xml资源id
	 */
	public static void toastMsg(int resid) {
		String msg = InspectionAPP.getInstance().getString(resid);
		Toast.makeText(InspectionAPP.getInstance(), msg, Toast.LENGTH_SHORT).show();
	}

	/**
	 * 根据指定的图像路径和大小来获取缩略图
	 * 此方法有两点好处：
	 *     1. 使用较小的内存空间，第一次获取的bitmap实际上为null，只是为了读取宽度和高度，
	 *        第二次读取的bitmap是根据比例压缩过的图像，第三次读取的bitmap是所要的缩略图。
	 *     2. 缩略图对于原图像来讲没有拉伸，这里使用了2.2版本的新工具ThumbnailUtils，使
	 *        用这个工具生成的图像不会被拉伸。
	 * @param imagePath 图像的路径
	 * @param width 指定输出图像的宽度
	 * @param height 指定输出图像的高度
	 * @return 生成的缩略图
	 */
	public static Bitmap getImageThumbnail(String imagePath, int width, int height) {
		Bitmap bitmap = null;
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		// 获取这个图片的宽和高，注意此处的bitmap为null
		bitmap = BitmapFactory.decodeFile(imagePath, options);
		options.inJustDecodeBounds = false; // 设为 false
		// 计算缩放比
		int h = options.outHeight;
		int w = options.outWidth;
		int beWidth = w / width;
		int beHeight = h / height;
		int be = 1;
		if (beWidth < beHeight) {
			be = beWidth;
		} else {
			be = beHeight;
		}
		if (be <= 0) {
			be = 1;
		}
		options.inSampleSize = be;
		// 重新读入图片，读取缩放后的bitmap，注意这次要把options.inJustDecodeBounds 设为 false
		bitmap = BitmapFactory.decodeFile(imagePath, options);
		// 利用ThumbnailUtils来创建缩略图，这里要指定要缩放哪个Bitmap对象
		bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
				ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		return bitmap;
	}

	/**
	 * 获取视频的缩略图
	 * 先通过ThumbnailUtils来创建一个视频的缩略图，然后再利用ThumbnailUtils来生成指定大小的缩略图。
	 * 如果想要的缩略图的宽和高都小于MICRO_KIND，则类型要使用MICRO_KIND作为kind的值，这样会节省内存。
	 * @param videoPath 视频的路径
	 * @param width 指定输出视频缩略图的宽度
	 * @param height 指定输出视频缩略图的高度度
	 * @param kind 参照MediaStore.Images.Thumbnails类中的常量MINI_KIND和MICRO_KIND。
	 *            其中，MINI_KIND: 512 x 384，MICRO_KIND: 96 x 96
	 * @return 指定大小的视频缩略图
	 */
	public static Bitmap getVideoThumbnail(String videoPath, int width, int height,
										   int kind) {
		try{
			Bitmap bitmap = null;
			// 获取视频的缩略图
			bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, kind);
//    	        System.out.println("w"+bitmap.getWidth());
//    	        System.out.println("h"+bitmap.getHeight());
			bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
					ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
			return bitmap;
		}catch(Exception e){
			return null;
		}

	}

	/***
	 * 指定路径的文件是否视频文件
	 * @param path
	 * @return
	 */
	public static boolean isVideo(String path){
		int idx = path.lastIndexOf(".");
		if(idx == -1){
			return false;
		}
		String ext = path.substring(idx+1).toLowerCase();
		if("mp4".equals(ext) || "3gp".equals(ext)){
			return true;
		}
		return false;
	}

	/**
	 * 绑定Spinner控件数据
	 * @param context
	 * @param list 列表
	 * @param sp 控件
	 * @param textColumn 文本域
	 * @param valueColumn  值域
	 */
	public static void setSpinner(Context context,
								  List<Map<String, Object>> list, Spinner sp, String textColumn,
								  String valueColumn) {
		if (list == null)
			return;
		try {
			String[] text = new String[list.size()];
			String[] value = new String[list.size()];
			Iterator<Map<String, Object>> iterator = list.listIterator();
			int i = 0;
			while (iterator.hasNext()) {
				Map<String, Object> obj = iterator.next();
				value[i] = obj.get(valueColumn).toString();
				text[i] = obj.get(textColumn).toString();
				i++;
			}
			ArrayAdapter<CharSequence> adapter = new ArrayAdapter(context,
					android.R.layout.simple_spinner_item, text);
//			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

			// ArrayAdapter<CharSequence> adapter = new ArrayAdapter(context,
			// android.R.layout.simple_spinner_item,
			// text);
			adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
			sp.setAdapter(adapter);
		} catch (Exception ex) {

		}
	}

	/**
	 * 设置Spinner默认值
	 * @param list 列表数据
	 * @param sp 控件
	 * @param valueColumn 文本域
	 * @param value 值域
	 */
	public static void setSelection(List<Map<String, Object>> list, Spinner sp,
									String valueColumn, String value) {
		if (valueColumn.equals(""))
			return;
		try {
			for (int i = 0; i < list.size(); i++) {
				if (list.get(i).get(valueColumn).toString().equals(value)) {
					sp.setSelection(i);
					break;
				}
			}
		} catch (Exception e) {
		}
	}

	private static Map<String, Node> nodeMap;//所有行政区划节点
	private static List<Node> rootNodeList;//行政区划根节点
	/**
	 * 绑定Button控件数据,实现无限级树
	 * @param context
	 * @param list 列表
	 * @param sp 控件
	 * @param textColumn 文本域
	 * @param valueColumn  值域
	 */
	public static void setButtonTree(Context context,
									 List<Map<String, Object>> list, Button sp, List<Object> params,
									 TreeItemClickListener itemClickListener) {
		if (list == null)
			return;
		try {
			setNodeData(list, params);
			setTreeNode();
			setTreeAdapter(context, sp, itemClickListener);
		} catch (Exception ex) {

		}
	}

	/**
	 * 绑定Button控件数据,实现无限级树, 上报事件中的工程地址
	 * @param context
	 * @param list 列表
	 * @param list1 列表
	 * @param sp 控件
	 * @param textColumn 文本域
	 * @param valueColumn  值域
	 */
	public static void setAddressTree(Context context,
									  List<Map<String, Object>> list, List<Map<String, Object>> list1, Button sp, List<Object> params,
									  TreeItemClickListener itemClickListener) {
		if (list == null)
			return;
		try {
			setNodeData(list, params);
			setTreeNode();
			setLeaf(list1, params);
			setTreeAdapter(context, sp, itemClickListener);
		} catch (Exception ex) {

		}
	}

	/**
	 * 设置节点数据,服务端下发的行政区划数据转成node节点
	 * @param list 列表
	 * @param params
	 */
	@SuppressWarnings("unused")
	private static void setNodeData(List<Map<String, Object>> list, List<Object> params){
		nodeMap = new HashMap<String, Node>();
		Iterator<Map<String, Object>> iterator = list.listIterator();
		while (iterator.hasNext()) {
			Map<String, Object> obj = iterator.next();
			String value = obj.get(params.get(0).toString()).toString();//districtcode
			String text = obj.get(params.get(1).toString()).toString();//districtname
			String parentValue = obj.get(params.get(2).toString()).toString();//parentcode
			Node node = new Node(text, value);
			node.setCheckBox(false);
			node.setExpanded(false);
			node.setParentValue(parentValue);
			nodeMap.put(value, node);
		}
	}

	/**
	 * 设置叶子节点数据
	 * @param list 列表
	 * @param params
	 */
	@SuppressWarnings("unused")
	private static void setLeaf(List<Map<String, Object>> list, List<Object> params){
		Iterator<Map<String, Object>> iterator = list.listIterator();
		while (iterator.hasNext()) {
			Map<String, Object> obj = iterator.next();
			String text = obj.get(params.get(3).toString()).toString();//address
			String parentValue = obj.get(params.get(4).toString()).toString();//districtcode
			Node node = new Node(text, parentValue);
			node.setCheckBox(false);
			node.setParentValue(parentValue);
			node.setTreeMap(obj);
			nodeMap.get(parentValue).add(node);
		}
	}

	/**
	 * 遍历节点，形成树状结构
	 */
	private static void setTreeNode(){
		rootNodeList = new ArrayList<Node>();
		Iterator<Entry<String, Node>> iter = nodeMap.entrySet().iterator();
		while(iter.hasNext()){
			Map.Entry<String, Node> entry = iter.next();
			Node node = (Node) entry.getValue();
			String parentValue = node.getParentValue();
			if(nodeMap.get(parentValue) == null){
				rootNodeList.add(node);
			} else {
				if(nodeMap.get(parentValue) != null){
					nodeMap.get(parentValue).add(node);
				}
			}
		}
	}

	/**
	 * 设置点击button跳出的树结构数据
	 * @param context
	 * @param sp
	 * @param itemClickListener
	 */
	private static void setTreeAdapter(Context context, Button sp, TreeItemClickListener itemClickListener){
		TreeAdapter adapter = new TreeAdapter(context, rootNodeList);
		adapter.setExpandLevel(1);
		adapter.setBtnDoexecute(false);
		adapter.setTree_Text_Style(R.style.Tree_Text_Style);
		final Dialog treeDialog = new Dialog(context);
		treeDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		treeDialog.setContentView(R.layout.activity_district_tree);
		ListView code_list =  (ListView) treeDialog.findViewById(R.id.code_list);
		code_list.setAdapter(adapter);
		itemClickListener.setTreeDialog(treeDialog);
		code_list.setOnItemClickListener(itemClickListener);
		sp.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				treeDialog.show();
			}

		});
	}

	/***
	 * http请求报错时提醒
	 * @param err 错误类型
	 * @args 可变参数
	 */
	public static void toastHttpError(CommError err,Object... args){
		if(err==CommError.HttpError){
			toastMsg(R.string.cannot_connect_server);
		}else if(err==CommError.ResponsNot200Error){
			toastMsg(R.string.responsnot200error);
		}else if(err==CommError.RequestError){
			String msg = "";
			if(args !=null && args.length>0){
				msg = " "+args[0].toString();
			}
			toastMsg(InspectionAPP.getInstance().getString(R.string.serverrequesterror)
					+msg);
		}else if(err==CommError.LogicError){
			toastMsg(R.string.clientlogicerror);
		}else if(err==CommError.JsonError){
			toastMsg(R.string.clientjsonerror);
		}else if(err==CommError.NoDataError){
			toastMsg(R.string.nodataerror);
		}else if(err==CommError.BusyError){
			toastMsg(R.string.busyrrror);
		}
	}

	/**
	 * 获得上传文件数组
	 * @param list 文件集合
	 * @return
	 */
	public static ArrayList<String> getUploadFiles( List<MediaEntity> list){
		ArrayList<String> al = new ArrayList<String>();
		for (MediaEntity me : list) {
			if (!me.getFilepath().equals("")) {
				al.add(me.getFilepath());
			}
		}
		return al;
	}
	/**
	 * 获得事件来源字典
	 * @return
	 */
	public static List<Map<String,Object>> genEventSource(){
		List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
		Map<String,Object> m1 = new HashMap<String,Object>();
		m1.put("text", "报警信息");m1.put("value", "1");
		list.add(m1);
		Map<String,Object> m2 = new HashMap<String,Object>();
		m2.put("text", "街道上报");m2.put("value", "2");
		list.add(m2);
		Map<String,Object> m3 = new HashMap<String,Object>();
		m3.put("text", "上级下发");m3.put("value", "3");
		list.add(m3);
		Map<String,Object> m4 = new HashMap<String,Object>();
		m4.put("text", "电话接收");m4.put("value", "4");
		list.add(m4);
		Map<String,Object> m5 = new HashMap<String,Object>();
		m5.put("text", "其它");m5.put("value", "5");
		list.add(m5);
		return list;
	}
	/**
	 * 获得事件等级字典
	 * @return
	 */
	public static List<Map<String,Object>> genEventLevel(){
		List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
		Map<String,Object> m1 = new HashMap<String,Object>();
		m1.put("text", "特别重大");m1.put("value", "1");
		list.add(m1);
		Map<String,Object> m2 = new HashMap<String,Object>();
		m2.put("text", "重大");m2.put("value", "2");
		list.add(m2);
		Map<String,Object> m3 = new HashMap<String,Object>();
		m3.put("text", "较大");m3.put("value", "3");
		list.add(m3);
		Map<String,Object> m4 = new HashMap<String,Object>();
		m4.put("text", "一般");m4.put("value", "4");
		list.add(m4);
//		Map<String,Object> m5 = new HashMap<String,Object>();
//		m5.put("text", "其它");m5.put("value", "5");
//		list.add(m5);
		return list;
	}
	/**
	 * 获得事件类型字典
	 * @return
	 */
	public static List<Map<String,Object>> genEventType(){
		List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
		Map<String,Object> m1 = new HashMap<String,Object>();
		m1.put("text", "人防工程坍塌事故");m1.put("value", "12W01");
		list.add(m1);
		Map<String,Object> m2 = new HashMap<String,Object>();
		m2.put("text", "人防工程渗漏或倒灌");m2.put("value", "12W02");
		list.add(m2);
		Map<String,Object> m3 = new HashMap<String,Object>();
		m3.put("text", "火灾事故");m3.put("value", "12W03");
		list.add(m3);
		Map<String,Object> m4 = new HashMap<String,Object>();
		m4.put("text", "人为破坏");m4.put("value", "12W04");
		list.add(m4);
		Map<String,Object> m5 = new HashMap<String,Object>();
		m5.put("text", "其他");m5.put("value", "12W99");
		list.add(m5);
		return list;
	}

	/**
	 * 获得安全巡查问题等级字典
	 * @return
	 */
	public static List<Map<String,Object>> genSafetyProblemLevel(){
		List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
		Map<String,Object> m1 = new HashMap<String,Object>();
		m1.put("text", "一级");m1.put("value", "1");
		list.add(m1);
		Map<String,Object> m2 = new HashMap<String,Object>();
		m2.put("text", "二级");m2.put("value", "2");
		list.add(m2);
		Map<String,Object> m3 = new HashMap<String,Object>();
		m3.put("text", "三级");m3.put("value", "3");
		list.add(m3);
		return list;
	}
	/**
	 * 获得使用情况
	 * @return
	 */
	public static List<Map<String,Object>> getUseStateList(){
		List<Map<String,Object>> listState  = new ArrayList<Map<String, Object>>();
		Map<String, Object> m00 = new HashMap<String, Object>();
		m00.put("text", "==选择使用情况==");
		m00.put("value", "");
		listState.add(m00);
		Map<String, Object> m10 = new HashMap<String, Object>();
		m10.put("text", "已用");
		m10.put("value", "1");
		listState.add(m10);
		Map<String, Object> m20 = new HashMap<String, Object>();
		m20.put("text", "未用");
		m20.put("value", "2");
		listState.add(m20);
		Map<String, Object> m30 = new HashMap<String, Object>();
		m30.put("text", "报废");
		m30.put("value", "3");
		listState.add(m30);
		Map<String, Object> m40 = new HashMap<String, Object>();
		m40.put("text", "未批准");
		m40.put("value", "4");
		listState.add(m40);
		return listState;
	}



	/**生成终端模块列表*/
	public static List<Map<String,Object>> genModuleList(boolean isOffline){
		List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
		Map<String,Object> m1 = new HashMap<String,Object>();
		m1.put("text", "设备巡检");m1.put("value", "1");list.add(m1);
		Map<String,Object> m2 = new HashMap<String,Object>();
		m2.put("text", "安全巡查");m2.put("value", "2");list.add(m2);
		Map<String,Object> m3 = new HashMap<String,Object>();
		m3.put("text", "维护维修");m3.put("value", "3");list.add(m3);
		Map<String,Object> m4 = new HashMap<String,Object>();

		if(!isOffline){
			m4.put("text", "报警处置");m4.put("value", "4");list.add(m4);
			Map<String,Object> m5 = new HashMap<String,Object>();
			m5.put("text", "事件处置");m5.put("value", "5");list.add(m5);
			Map<String,Object> m6 = new HashMap<String,Object>();
			m6.put("text", "设备设施");m6.put("value", "6");list.add(m6);
			Map<String,Object> m7 = new HashMap<String,Object>();
			m7.put("text", "工程档案");m7.put("value", "7");list.add(m7);
			Map<String,Object> m8 = new HashMap<String,Object>();
			m8.put("text", "预案查询");m8.put("value", "8");list.add(m8);
			Map<String,Object> m9 = new HashMap<String,Object>();
			m9.put("text", "案例查询");m9.put("value", "9");list.add(m9);
			Map<String,Object> m10 = new HashMap<String,Object>();
			m10.put("text", "知识查询");m10.put("value", "10");list.add(m10);
			Map<String,Object> m11 = new HashMap<String,Object>();
			m11.put("text", "通讯录");m11.put("value", "11");list.add(m11);
		}

		return list;
	}

	/***
	 * 文档打不开 推荐下载apk
	 * @param con
	 * @param filepath 文件路径
	 */
	public static void DownDocReader(final Context con,String filepath) {
		String ext = filepath.substring(filepath.lastIndexOf('.')+1);
		BasePOP.Builder customBuilder = new BasePOP.Builder(con);
		Dialog dialog = null;
		customBuilder
				.setTitle("未检测到 " + ext+ " 文件阅读器")
				.setMessage("推荐使用金山WPS查看文件,\n点击确认跳转到下载页!")
				.setNegativeButton(R.string.cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
												int which) {
								dialog.dismiss();
								dialog = null;
							}
						})
				.setPositiveButton(R.string.sure,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
												int which) {

								//下载apk
								try{
									con.startActivity(new Intent("android.intent.action.VIEW",
											Uri.parse("http://wdl.cache.ijinshan.com/wps/download/android/kingsoftoffice_2052/moffice_2052_wpscn.apk")));
								}catch(Exception e){

								}

								dialog.dismiss();
								dialog = null;
							}
						});
		dialog = customBuilder.create();

		dialog.show();
	}

	/**
	 * 压缩图片
	 * @param path
	 */
	public static void scalePicture(final String path){
		new Thread(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				Bitmap bp = null ;
				try{
					bp = scalePicture(path, (int)(640*1.3), (int)(480*1.3));
					FileOutputStream out=new FileOutputStream(new File(path));
					if(bp.compress(Bitmap.CompressFormat.JPEG, 100, out)){
						out.flush();
						out.close();
					}
				}catch(FileNotFoundException e){
					e.printStackTrace();
				}catch(IOException e){
					e.printStackTrace();
				}finally{
					if(bp != null && bp.isRecycled()==false){
						bp.recycle();
						bp = null;
					}
				}
			}

		}.start();

	}

	/**
	 * 压缩图片到指定大小
	 * @param filename 图片路径
	 * @param maxWidth 最大宽度
	 * @param maxHeight 最大高度
	 * @return
	 */
	public static Bitmap scalePicture(String filename, int maxWidth,int maxHeight) {
		Bitmap bitmap = null,remap=null;
		try {
			BitmapFactory.Options opts = new BitmapFactory.Options();
			bitmap = BitmapFactory.decodeFile(filename, opts);
			remap = PictureUtil.extractThumbnail(bitmap, maxWidth, maxHeight);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return remap;
	}
	/**
	 * 给图片添加水印
	 * @param src
	 * @return
	 */
	public static Bitmap addTimeBitmap(Bitmap src) {

		Calendar c = Calendar.getInstance();
		Date date = c.getTime();
		SimpleDateFormat simple = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		int w = src.getWidth();
		int h = src.getHeight();
		String mstrTitle = ""+simple.format(date).toString();
		Bitmap bmpTemp = Bitmap.createBitmap(w, h, Config.ARGB_8888);
		Canvas canvas = new Canvas(bmpTemp);
		Paint p = new Paint();
		p.setColor(Color.RED);
		p.setTextSize(22);
		canvas.drawBitmap(src, 0, 0, p);
		canvas.drawText(mstrTitle, 10, 30, p);
		canvas.save(Canvas.ALL_SAVE_FLAG);
		canvas.restore();
		return bmpTemp;
	}

	/**
	 * 返回MD5加密字符
	 * @param source 原字符串
	 * @return
	 */
	public static String getMD5String(String source){
		try{
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(source.getBytes());
			String rtn = new BigInteger(1,md.digest()).toString(16);
			return rtn;
		}catch(Exception e){
			return "";
		}

	}

	public static final String VIPARA = "0102030405060708";

	/**
	 * 返回AES加密字符
	 * @date 2014-09-01
	 * @param content 需要加密的内容  完美支持中文
	 * @param password  加密的密码  密码的长度为必须为16字节，否则会报错
	 * @return
	 */
	public static String encrypt(String context, String password) {
		IvParameterSpec zeroIv = new IvParameterSpec(VIPARA.getBytes());
		SecretKeySpec key = new SecretKeySpec(password.getBytes(), "AES");
		byte[] encryptedData = null;
		try {
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, key, zeroIv);
			encryptedData = cipher.doFinal(context.getBytes("GBK"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String result = new BigInteger(1, encryptedData).toString(16);

		return result;
	}

	/**AES加密不采用下面的方法，该方法导致Android 端和Java 控制台程序加密结果不一致**/
//	/**
//	 * 返回AES加密字符
//	 * @date 2014-09-01
//	 * @param content 需要加密的内容
//	 * @param password 加密的密码
//	 * @return
//	 */
//	public static String encrypt(String content, String password) {
//
//		try {
//
//			KeyGenerator kgen = KeyGenerator.getInstance("AES");
////			Provider pr=Security.getProvider("BC");
//	        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
////			SecureRandom sr =SecureRandom.getInstance("AES", pr);
//	        sr.setSeed(password.getBytes());
//	        kgen.init(128, sr); // 192 and 256 bits may not be available ,使用用户提供的随机源初始化此密钥生成器，使其具有确定的密钥大小。
//	        SecretKey secretKey = kgen.generateKey();
//			byte[] enCodeFormat = secretKey.getEncoded();
//			// 使用 SecretKeySpec 类来根据一个字节数组构造一个SecretKey
//			SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
//			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");// 创建密码器
//			byte[] byteContent = content.getBytes("utf-8");
//			cipher.init(Cipher.ENCRYPT_MODE, key);// 初始化
//			byte[] resultbyte = cipher.doFinal(byteContent); // 执行操作
//			String result = new BigInteger(1, resultbyte).toString(16);
//	        return result;
////	        byte[] rawKey  = skey.getEncoded();
////	        SecretKeySpec skeySpec = new SecretKeySpec(rawKey, "AES");
////	        Cipher cipher = Cipher.getInstance("AES");
////	        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
////	        byte[] encrypted = cipher.doFinal(content.getBytes());
////	        String result = new BigInteger(1, encrypted).toString(16);
////	        return result;
//		} catch (Exception e) {
//
//			e.printStackTrace();
//
//		}
//		return null;
//
//	}


	/***
	 * 将json字符串写入文本文件 测试时使用
	 * @param content 字符串内容
	 * @param strFilePath 保存文件路径
	 */
	public static void WriteJson2TxtFile(String content,String strFilePath){
		//每次写入时，都换行写
		String strContent= content+"\n";
		try {
			File file = new File(strFilePath);
			if(file.exists())file.delete();
			if (!file.exists()) {
				Log.d("CommonFun", "Create the file:" + strFilePath);
				file.createNewFile();
			}
			RandomAccessFile raf = new RandomAccessFile(file, "rw");
			raf.seek(file.length());
			raf.write(strContent.getBytes());
			raf.close();
		} catch (Exception e) {
			Log.e("CommonFun", "Error on write File.");
		}
	}

	/***
	 * 判断类是否实现指定接口
	 * @param c 需要判断的类
	 * @param szInterface 接口名称
	 * @return
	 */
	public static boolean isInterface(Class c, String szInterface) {
		Class[] face = c.getInterfaces();
		for (int i = 0, j = face.length; i < j; i++) {
			if (face[i].getName().equals(szInterface)) {
				return true;
			} else {
				Class[] face1 = face[i].getInterfaces();
				for (int x = 0; x < face1.length; x++) {
					if (face1[x].getName().equals(szInterface)) {
						return true;
					} else if (isInterface(face1[x], szInterface)) {
						return true;
					}
				}
			}
		}
		if (null != c.getSuperclass()) {
			return isInterface(c.getSuperclass(), szInterface);
		}
		return false;
	}


	/**
	 * 根据路径创建目录
	 * @param path
	 */
	public static void createFold(String path){
		File f = new File(path);
		if (!f.exists()) {
			f.mkdirs();
		}
	}

	/***
	 * 获取软件版本号
	 * @param con
	 * @return
	 */
	public static String getVersionName(Context con) {
		try{
			// 获取packagemanager的实例
			PackageManager packageManager = con.getPackageManager();
			// getPackageName()是你当前类的包名，0代表是获取版本信息
			PackageInfo packInfo = packageManager.getPackageInfo(con.getPackageName(),0);
			String version = packInfo.versionName;
			return version;
		}catch(Exception e){
			return "";
		}
	}

	/**
	 * 关闭输入法
	 * @param context
	 * @param view 输入框控件
	 */
	public static void closeInputMethod(Context context,View view) {
		InputMethodManager imm = (InputMethodManager) context
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		boolean isOpen = imm.isActive();
		if (isOpen) {
			// imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);//没有显示则显示
			imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}

	/**
	 * 生成当前时间
	 * @return
	 */
	public static String genNowTime() {
		Date nowTime = new Date();
		SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return time.format(nowTime);
	}
}
