package cn.tedu.mediaplayer.util;

import java.io.File;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;
import cn.tedu.mediaplayer.R;

/**
 * �첽��������ͼƬ�Ĺ�����
 */
public class ImageLoader {
	//��������
	private Map<String, SoftReference<Bitmap>> cache = new HashMap<String, SoftReference<Bitmap>>();
	private Context context;
	//����������ѭ�����񼯺�
	private List<ImageLoadTask> tasks = new ArrayList<ImageLoadTask>();
	//����һ�������߳�  ������ѭ���񼯺�
	private Thread workThread;
	private boolean isLoop = true;
	private AbsListView listView;
	//����Handler
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case HANDLER_IMAGE_LOADED:
				//��imageView����Bitmap
				ImageLoadTask task = (ImageLoadTask) msg.obj;
				ImageView ivAlbum = (ImageView) listView.findViewWithTag(task.path);
				if(ivAlbum!=null){ //�ҵ��˶�Ӧ��imageView�ؼ�
					Bitmap b = task.bitmap;
					if(b!=null){  //ͼƬ���سɹ���
						ivAlbum.setImageBitmap(b);
					}else{ //ͼƬ����ʧ����
						ivAlbum.setImageResource(R.drawable.ic_launcher);
					}
				}
				break;
			}
		}
	};
	
	public static final int HANDLER_IMAGE_LOADED = 100;

	public ImageLoader(Context context, AbsListView listView) {
		this.context = context;
		this.listView = listView;
		//��ʼ�������߳�
		workThread = new Thread(){
			/**
			 * ������ѭ���񼯺� �Ӽ����л�ȡÿ������
			 * Ȼ��ִ�У�����ͼƬ��
			 */
			public void run() {
				while(isLoop){
					if(!tasks.isEmpty()){ //����������
						ImageLoadTask task = tasks.remove(0);
						String path = task.path;
						Bitmap bitmap = loadBitmap(path);
						task.bitmap = bitmap;
						//���̣߳���bitmap���õ�ImageView��
						Message msg = new Message();
						msg.what = HANDLER_IMAGE_LOADED;
						msg.obj = task;
						handler.sendMessage(msg);
						
					}else{ //����û������   �̵߳ȴ�
						try {
							synchronized (workThread) {
								workThread.wait();
							}
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
		};
		workThread.start();
	}
	
	/**
	 * ͨ��·��  ����ͼƬ����
	 * @param path
	 * @return
	 */
	public Bitmap loadBitmap(String path){
		try {
			InputStream is = HttpUtils.get(path);
			//Bitmap b = BitmapFactory.decodeStream(is,);
			Bitmap b=BitmapUtils.loadBitmap(is, 50, 50);
			//��bitmap�����ڴ滺����
			cache.put(path, new SoftReference<Bitmap>(b));
			//���ļ������д�ͼƬ
			//��path�н�ȡ���һ�� ��ΪͼƬ�ļ���
			//path:  http://xxxx/xxx/x/xx/xxx.jpg
			String filename = path.substring(path.lastIndexOf("/"));
			// /data/data/cn.tedu.media/cache/images/xxx.jpg
			File file = new File(context.getCacheDir(), "images"+filename);
			BitmapUtils.save(b, file);
			return b;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	/**
	 * �첽��������ͼƬ��������ʾ��imageView��
	 * @param ivAlbum
	 * @param path
	 */
	public void display(ImageView ivAlbum, String path) {
		//��·�����ó�ImageView��tag   
		//��Ϊ��handler����Ҫͨ��tag�ҵ����ImageView
		ivAlbum.setTag(path);
		//ȥ�ڴ滺����Ѱ�� �Ƿ��Ѿ����
		SoftReference<Bitmap> ref = cache.get(path);
		if(ref!=null){ //��ǰ���
			Bitmap b=ref.get();
			if(b!=null){ //bitmap��û�б�����
				//Log.i("info", "���ڴ滺�����ҵ���ͼƬ..");
				ivAlbum.setImageBitmap(b);
				return;
			}
		}
		//ȥ�ļ�������Ѱ���Ƿ���ͼƬ
		String filename = path.substring(path.lastIndexOf("/"));
		File file = new File(context.getCacheDir(), "images"+filename);
		Bitmap b=BitmapUtils.loadBitmap(file);
		if(b != null){
			//Log.i("info", "���ļ��������ҵ���ͼƬ..");
			ivAlbum.setImageBitmap(b);
			//�Ѵ��ļ��ж�ȡ��bitmap �����ڴ滺��
			cache.put(path, new SoftReference<Bitmap>(b));
			return;
		}
		//����ImageLoadTask���� ��ӵ����񼯺���
		ImageLoadTask task = new ImageLoadTask();
		task.path = path;
		tasks.add(task);
		//���ѹ����߳�  �����ɻ�
		synchronized (workThread) {
			workThread.notify();
		}
	}
	
	//��װͼƬ��������
	class ImageLoadTask{
		String path;			//ͼƬ��ַ
		Bitmap bitmap;	//���ݵ�ַ���ص���ͼƬ
	}

	/**
	 * ֹͣ�����߳�
	 */
	public void stopThread() {
		isLoop = false;
		synchronized (workThread) {
			workThread.notify();
		}
	}
}



