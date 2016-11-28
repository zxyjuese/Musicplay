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
 * 异步批量加载图片的工具类
 */
public class ImageLoader {
	//声明缓存
	private Map<String, SoftReference<Bitmap>> cache = new HashMap<String, SoftReference<Bitmap>>();
	private Context context;
	//声明用于轮循的任务集合
	private List<ImageLoadTask> tasks = new ArrayList<ImageLoadTask>();
	//声明一个工作线程  用于轮循任务集合
	private Thread workThread;
	private boolean isLoop = true;
	private AbsListView listView;
	//声明Handler
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case HANDLER_IMAGE_LOADED:
				//给imageView设置Bitmap
				ImageLoadTask task = (ImageLoadTask) msg.obj;
				ImageView ivAlbum = (ImageView) listView.findViewWithTag(task.path);
				if(ivAlbum!=null){ //找到了对应的imageView控件
					Bitmap b = task.bitmap;
					if(b!=null){  //图片下载成功了
						ivAlbum.setImageBitmap(b);
					}else{ //图片下载失败了
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
		//初始化工作线程
		workThread = new Thread(){
			/**
			 * 不断轮循任务集合 从集合中获取每个任务
			 * 然后执行，下载图片。
			 */
			public void run() {
				while(isLoop){
					if(!tasks.isEmpty()){ //里面有任务
						ImageLoadTask task = tasks.remove(0);
						String path = task.path;
						Bitmap bitmap = loadBitmap(path);
						task.bitmap = bitmap;
						//主线程：把bitmap设置到ImageView中
						Message msg = new Message();
						msg.what = HANDLER_IMAGE_LOADED;
						msg.obj = task;
						handler.sendMessage(msg);
						
					}else{ //里面没有任务   线程等待
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
	 * 通过路径  访问图片数据
	 * @param path
	 * @return
	 */
	public Bitmap loadBitmap(String path){
		try {
			InputStream is = HttpUtils.get(path);
			//Bitmap b = BitmapFactory.decodeStream(is,);
			Bitmap b=BitmapUtils.loadBitmap(is, 50, 50);
			//把bitmap存入内存缓存中
			cache.put(path, new SoftReference<Bitmap>(b));
			//向文件缓存中存图片
			//从path中截取最后一段 作为图片文件名
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
	 * 异步加载网络图片，并且显示到imageView中
	 * @param ivAlbum
	 * @param path
	 */
	public void display(ImageView ivAlbum, String path) {
		//把路径设置成ImageView的tag   
		//因为在handler中需要通过tag找到这个ImageView
		ivAlbum.setTag(path);
		//去内存缓存中寻找 是否已经存过
		SoftReference<Bitmap> ref = cache.get(path);
		if(ref!=null){ //以前存过
			Bitmap b=ref.get();
			if(b!=null){ //bitmap还没有被销毁
				//Log.i("info", "从内存缓存中找到的图片..");
				ivAlbum.setImageBitmap(b);
				return;
			}
		}
		//去文件缓存中寻找是否有图片
		String filename = path.substring(path.lastIndexOf("/"));
		File file = new File(context.getCacheDir(), "images"+filename);
		Bitmap b=BitmapUtils.loadBitmap(file);
		if(b != null){
			//Log.i("info", "从文件缓存中找到的图片..");
			ivAlbum.setImageBitmap(b);
			//把从文件中读取的bitmap 存入内存缓存
			cache.put(path, new SoftReference<Bitmap>(b));
			return;
		}
		//创建ImageLoadTask对象 添加到任务集合中
		ImageLoadTask task = new ImageLoadTask();
		task.path = path;
		tasks.add(task);
		//唤醒工作线程  起来干活
		synchronized (workThread) {
			workThread.notify();
		}
	}
	
	//封装图片加载任务
	class ImageLoadTask{
		String path;			//图片地址
		Bitmap bitmap;	//根据地址下载到的图片
	}

	/**
	 * 停止工作线程
	 */
	public void stopThread() {
		isLoop = false;
		synchronized (workThread) {
			workThread.notify();
		}
	}
}



