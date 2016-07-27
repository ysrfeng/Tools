

   <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />





    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />


















public class MyService extends Service{
	
	private static final String TAG = "==MyService==";

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		Log.i(TAG, "==onBind==");
		return null;
	}
	

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		final String imagPath = intent.getStringExtra("imagPath");
		
		//开启线程下载图片  
		new Thread(){
			public void run() {
				if(HttpUtils.isNetWork(MyService.this)){
					//有网状态
					byte[] buffer = HttpUtils.getData(imagPath);
					
					if(buffer!= null&&buffer.length>0){
						if(FileUtils.isConnSdCard()){
							
							boolean bl = FileUtils.writeToSdcard(buffer, imagPath);
							if(bl){
								System.out.println("成功写入");
								
								stopSelf();//关闭服务
							}else{
								System.out.println("写入失败");
							}
							
						}else{
							System.out.println("sd卡不可用");
						}
						
						
					}else{
						System.out.println("下载图片异常");
					}
				}else{
					System.out.println("网络异常");
				}
			};
		}.start();
		
		
		return START_NOT_STICKY;
	
	}
	


}
