package cn.tedu.mediaplayer.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.os.AsyncTask;
import cn.tedu.mediaplayer.app.MusicApplication;

/**
 * 图片操作相关工具类
 */
public class BitmapUtils {
	
	/**
	 * 异步模糊化处理图片
	 * @param bitmap  原始图片
	 * @param radius   模糊半径  半径越大  越模糊
	 * @param callback  当模糊处理完毕后将会返回bitmap
	 */
	public static void loadBlurBitmap(final Bitmap bitmap, final int radius, final BitmapCallback callback){
		AsyncTask<String, String, Bitmap> task = new AsyncTask<String, String, Bitmap>(){
			//工作线程中模糊化处理图片  耗时
			protected Bitmap doInBackground(String... params) {
				Bitmap b =createBlurBitmap(bitmap, radius);
				return b;
			}
			//调用callback的方法 把处理结果返回给调用者
			protected void onPostExecute(Bitmap result) {
				callback.onBitmapLoaded(result);
			}
		};
		task.execute();
	}
	
	/**
	 * 传递bitmap 传递模糊半径 返回一个被模糊的bitmap
	 * 比较耗时
	 * @param sentBitmap
	 * @param radius
	 * @return
	 */
	private static Bitmap createBlurBitmap(Bitmap sentBitmap, int radius) {
		Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);
		if (radius < 1) {
			return (null);
		}
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		int[] pix = new int[w * h];
		bitmap.getPixels(pix, 0, w, 0, 0, w, h);
		int wm = w - 1;
		int hm = h - 1;
		int wh = w * h;
		int div = radius + radius + 1;
		int r[] = new int[wh];
		int g[] = new int[wh];
		int b[] = new int[wh];
		int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
		int vmin[] = new int[Math.max(w, h)];
		int divsum = (div + 1) >> 1;
		divsum *= divsum;
		int dv[] = new int[256 * divsum];
		for (i = 0; i < 256 * divsum; i++) {
			dv[i] = (i / divsum);

		}
		yw = yi = 0;
		int[][] stack = new int[div][3];
		int stackpointer;
		int stackstart;
		int[] sir;
		int rbs;
		int r1 = radius + 1;
		int routsum, goutsum, boutsum;
		int rinsum, ginsum, binsum;
		for (y = 0; y < h; y++) {
			rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
			for (i = -radius; i <= radius; i++) {
				p = pix[yi + Math.min(wm, Math.max(i, 0))];
				sir = stack[i + radius];
				sir[0] = (p & 0xff0000) >> 16;
				sir[1] = (p & 0x00ff00) >> 8;
				sir[2] = (p & 0x0000ff);
				rbs = r1 - Math.abs(i);
				rsum += sir[0] * rbs;
				gsum += sir[1] * rbs;
				bsum += sir[2] * rbs;
				if (i > 0) {
					rinsum += sir[0];
					ginsum += sir[1];
					binsum += sir[2];

				} else {
					routsum += sir[0];
					goutsum += sir[1];
					boutsum += sir[2];

				}

			}
			stackpointer = radius;
			for (x = 0; x < w; x++) {
				r[yi] = dv[rsum];
				g[yi] = dv[gsum];
				b[yi] = dv[bsum];
				rsum -= routsum;
				gsum -= goutsum;
				bsum -= boutsum;
				stackstart = stackpointer - radius + div;
				sir = stack[stackstart % div];
				routsum -= sir[0];
				goutsum -= sir[1];
				boutsum -= sir[2];
				if (y == 0) {
					vmin[x] = Math.min(x + radius + 1, wm);

				}
				p = pix[yw + vmin[x]];
				sir[0] = (p & 0xff0000) >> 16;
				sir[1] = (p & 0x00ff00) >> 8;
				sir[2] = (p & 0x0000ff);
				rinsum += sir[0];
				ginsum += sir[1];
				binsum += sir[2];
				rsum += rinsum;
				gsum += ginsum;
				bsum += binsum;
				stackpointer = (stackpointer + 1) % div;
				sir = stack[(stackpointer) % div];
				routsum += sir[0];
				goutsum += sir[1];
				boutsum += sir[2];
				rinsum -= sir[0];
				ginsum -= sir[1];
				binsum -= sir[2];
				yi++;

			}
			yw += w;

		}
		for (x = 0; x < w; x++) {
			rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
			yp = -radius * w;
			for (i = -radius; i <= radius; i++) {
				yi = Math.max(0, yp) + x;
				sir = stack[i + radius];
				sir[0] = r[yi];
				sir[1] = g[yi];
				sir[2] = b[yi];
				rbs = r1 - Math.abs(i);
				rsum += r[yi] * rbs;
				gsum += g[yi] * rbs;
				bsum += b[yi] * rbs;
				if (i > 0) {
					rinsum += sir[0];
					ginsum += sir[1];
					binsum += sir[2];

				} else {
					routsum += sir[0];
					goutsum += sir[1];
					boutsum += sir[2];

				}
				if (i < hm) {
					yp += w;
				}
			}
			yi = x;
			stackpointer = radius;
			for (y = 0; y < h; y++) {
				pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16)
						| (dv[gsum] << 8) | dv[bsum];
				rsum -= routsum;
				gsum -= goutsum;
				bsum -= boutsum;
				stackstart = stackpointer - radius + div;
				sir = stack[stackstart % div];
				routsum -= sir[0];
				goutsum -= sir[1];
				boutsum -= sir[2];
				if (x == 0) {
					vmin[y] = Math.min(y + r1, hm) * w;

				}
				p = x + vmin[y];
				sir[0] = r[p];
				sir[1] = g[p];
				sir[2] = b[p];
				rinsum += sir[0];
				ginsum += sir[1];
				binsum += sir[2];
				rsum += rinsum;
				gsum += ginsum;
				bsum += binsum;
				stackpointer = (stackpointer + 1) % div;
				sir = stack[stackpointer];
				routsum += sir[0];
				goutsum += sir[1];
				boutsum += sir[2];
				rinsum -= sir[0];
				ginsum -= sir[1];
				binsum -= sir[2];
				yi += w;
			}
		}
		bitmap.setPixels(pix, 0, w, 0, 0, w, h);
		return (bitmap);
	}

	
	
	/**
	 * 从文件中读取一个Bitmap
	 * @param file
	 * @return
	 */
	public static Bitmap loadBitmap(File file){
		if(!file.exists()){ 
			return null;
		}
		Bitmap b = BitmapFactory.decodeFile(file.getAbsolutePath());
		return b;
	}
	
	/**
	 * 把bitmap压缩成jpg格式保存到File文件中
	 * @param bitmap
	 * @param file
	 * @throws FileNotFoundException 
	 */
	public static void save(Bitmap bitmap, File file) throws FileNotFoundException{
		if(!file.getParentFile().exists()){
			file.getParentFile().mkdirs(); //父目录不存在则创建 
		}
		FileOutputStream fos = new FileOutputStream(file);
		bitmap.compress(CompressFormat.JPEG, 100, fos);
	}
	

	/**
	 * 通过一个输入流  按照用户需要的大小进行图片压缩后 返回Bitmap对象
	 * @param is   数据源
	 * @param width   图片的目标宽度
	 * @param height   图片的目标高度
	 * @return  压缩过后的Bitmap
	 */
	public static Bitmap loadBitmap(InputStream is, int width, int height) throws IOException {
		//1. 从输入流中 读取出byte[] 
		//把输入流中的数据 写到 字节输出流中
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		byte[] buffer = new byte[10*1024];
		int length = 0;
		while((length=is.read(buffer)) != -1){
			bos.write(buffer, 0, length);
			bos.flush();
		}
		//从输出流中得到byte[]  描述一个完整Bitmap数据
		byte[] bytes = bos.toByteArray();
		bos.close();
		//2. 解析byte[]  获取图片的原始宽与高
		Options opts = new Options();
		//仅仅加载图片的bounds属性
		opts.inJustDecodeBounds = true;
		BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opts);
		int w = opts.outWidth / width; //宽度的压缩比例
		int h = opts.outHeight / height; //高度的压缩比例
		//3. 根据用户的需求 计算出压缩比例
		int scale = w > h ? w : h;
		//4. 再次解析byte[] 获取压缩过后的图片
		opts.inJustDecodeBounds = false;
		opts.inSampleSize = scale;
		Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opts);
		return bitmap;
	}

	/**
	 * 通过一个网络地址  异步加载一个图片
	 * @param path  图片路径
	 * @param bitmapCallback   回调对象
	 */
	public static void loadBitmap(final String path, final BitmapCallback bitmapCallback) {
		AsyncTask<String, String, Bitmap> task = new AsyncTask<String, String, Bitmap>(){
			//工作线程中执行  发请求 获取Bitmap
			protected Bitmap doInBackground(String... params) {
				try {
					//http://xxxx/x/x/xxxx/xxxx/xx.jpg
					String filename = path.substring(path.lastIndexOf("/"));
					File file = new File(MusicApplication.getApp().getCacheDir(), "images"+filename);
					Bitmap b =loadBitmap(file);
					if(b!=null){ //文件已经下载过 直接使用
						return b;
					}
					InputStream is = HttpUtils.get(path);
					b=BitmapFactory.decodeStream(is);
					//把bitmap存入文件缓存 节省流量
					save(b, file);
					return b;
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}
			protected void onPostExecute(Bitmap b) {
				bitmapCallback.onBitmapLoaded(b);
			}
		};
		task.execute();
	}

	
	/**
	 * 通过一个网络地址  异步加载一个图片 并且按照压缩比例进行压缩处理
	 * @param path  图片路径
	 * @param scale 压缩比例
	 * @param bitmapCallback   回调对象
	 */
	public static void loadBitmap(final String path, final int scale, final BitmapCallback bitmapCallback) {
		AsyncTask<String, String, Bitmap> task = new AsyncTask<String, String, Bitmap>(){
			//工作线程中执行  发请求 获取Bitmap
			protected Bitmap doInBackground(String... params) {
				try {
					//获取缓存图片目录
					String filename = path.substring(path.lastIndexOf("/"));
					File file = new File(MusicApplication.getApp().getCacheDir(), "images"+filename);
					Options opts = new Options();
					opts.inSampleSize = scale;
					if(file.exists()){ //文件已存在
						return BitmapFactory.decodeFile(file.getAbsolutePath(), opts);
					}
					InputStream is = HttpUtils.get(path);
					//从服务端下载原始图片
					Bitmap b=BitmapFactory.decodeStream(is);
					//把原始图片存入文件
					save(b, file);
					//从文件中以压缩的方式 读取图片
					return BitmapFactory.decodeFile(file.getAbsolutePath(), opts);
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}
			protected void onPostExecute(Bitmap b) {
				bitmapCallback.onBitmapLoaded(b);
			}
		};
		task.execute();
	}

	
	public interface BitmapCallback{
		/** 当图片加载完毕后执行 */
		void onBitmapLoaded(Bitmap bitmap);
	}
	
}
