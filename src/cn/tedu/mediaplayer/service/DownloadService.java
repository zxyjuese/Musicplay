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
	 * �÷����ڹ����߳���ִ��  ����ֱ�ӱ�д��ʱ����
	 * ������Ҫ�ڸ÷����з���http����
	 * �������ҵ�� (�߶�ȡ�߱���)
	 */
	protected void onHandleIntent(Intent intent) {
		//��ȡActivity���ݹ����Ĳ�����
		String path = intent.getStringExtra("path");
		String title = intent.getStringExtra("title");
		int bitrate = intent.getIntExtra("bitrate", 0);
		int filesize = intent.getIntExtra("filesize", 0);
		try {
			if(path==null || path.equals("")){ //û��·��
				return;
			}
			File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), "_"+bitrate+"/"+title+".mp3");
			if(!file.getParentFile().exists()){
				file.getParentFile().mkdirs();
			}
			FileOutputStream fos = new FileOutputStream(file);
			//���ز� �߶�ȡ �߱��浽SD��
			InputStream is = HttpUtils.get(path);
			byte[] buffer = new byte[1024*200];
			int length=0;
			//׼����ʼ����  ����֪ͨ
			sendNotification("��������", "���ֿ�ʼ����", "���ֿ�ʼ����");
			int progress = 0;
			while((length=is.read(buffer))!=-1){
				fos.write(buffer, 0, length);
				fos.flush();
				progress += length;
				//����Ĺ�����  ��֪ͨ ��ʾ����
				String jindu = Math.floor(100.0*progress / filesize) +"%";
				sendNotification("��������", "���ֿ�ʼ����", "���ؽ��ȣ�"+jindu);
			}
			fos.close();
			//�������  ����֪ͨ
			clearNotification();
			sendNotification("��������", "�����������", "�����������");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/** ��֪ͨ */
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
	
	/** ���֪ͨ */
	public void clearNotification(){
		NotificationManager manager = (NotificationManager)
				getSystemService(NOTIFICATION_SERVICE);
		manager.cancel(NOTIFICATION_ID);
	}
	
}



