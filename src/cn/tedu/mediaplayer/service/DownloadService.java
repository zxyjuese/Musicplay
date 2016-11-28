package cn.tedu.mediaplayer.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import android.app.IntentService;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Environment;
import cn.tedu.mediaplayer.R;
import cn.tedu.mediaplayer.util.HttpUtils;

public class DownloadService extends IntentService{

	private static final int NOTIFICATION_ID = 105;

	public DownloadService() {
		super("xiaoming");
	}

	/**
	 * 该方法在工作线程中执行  可以直接编写耗时代码
	 * 我们需要在该方法中发送http请求
	 * 完成下载业务 (边读取边保存)
	 */
	protected void onHandleIntent(Intent intent) {
		//获取Activity传递过来的参数：
		String path = intent.getStringExtra("path");
		String title = intent.getStringExtra("title");
		int bitrate = intent.getIntExtra("bitrate", 0);
		int filesize = intent.getIntExtra("filesize", 0);
		try {
			if(path==null || path.equals("")){ //没有路径
				return;
			}
			File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), "_"+bitrate+"/"+title+".mp3");
			if(!file.getParentFile().exists()){
				file.getParentFile().mkdirs();
			}
			FileOutputStream fos = new FileOutputStream(file);
			//下载并 边读取 边保存到SD卡
			InputStream is = HttpUtils.get(path);
			byte[] buffer = new byte[1024*200];
			int length=0;
			//准备开始下载  发送通知
			sendNotification("音乐下载", "音乐开始下载", "音乐开始下载");
			int progress = 0;
			while((length=is.read(buffer))!=-1){
				fos.write(buffer, 0, length);
				fos.flush();
				progress += length;
				//保存的过程中  发通知 提示进度
				String jindu = Math.floor(100.0*progress / filesize) +"%";
				sendNotification("音乐下载", "音乐开始下载", "下载进度："+jindu);
			}
			fos.close();
			//下载完毕  发送通知
			clearNotification();
			sendNotification("音乐下载", "音乐下载完毕", "音乐下载完成");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/** 发通知 */
	public void sendNotification(String title, String ticker, String text){
		NotificationManager manager = (NotificationManager)
				getSystemService(NOTIFICATION_SERVICE);
		Builder builder = new Builder(this);
		builder.setSmallIcon(R.drawable.ic_launcher)
			.setContentText(text)
			.setContentTitle(title)
			.setTicker(ticker);
		Notification n = builder.build();
		manager.notify(NOTIFICATION_ID, n);
	}
	
	/** 清除通知 */
	public void clearNotification(){
		NotificationManager manager = (NotificationManager)
				getSystemService(NOTIFICATION_SERVICE);
		manager.cancel(NOTIFICATION_ID);
	}
	
}



