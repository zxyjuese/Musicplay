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
 * ͼƬ������ع�����
 */
public class BitmapUtils {
	
	/**
	 * �첽ģ��������ͼƬ
	 * @param bitmap  ԭʼͼƬ
	 * @param radius   ģ���뾶  �뾶Խ��  Խģ��
	 * @param callback  ��ģ��������Ϻ󽫻᷵��bitmap
	 */
	public static void loadBlurBitmap(final Bitmap bitmap, final int radius, final BitmapCallback callback){
		AsyncTask<String, String, Bitmap> task = new AsyncTask<String, String, Bitmap>(){
			//�����߳���ģ��������ͼƬ  ��ʱ
			protected Bitmap doInBackground(String... params) {
				Bitmap b =createBlurBitmap(bitmap, radius);
				return b;
			}
			//����callback�ķ��� �Ѵ��������ظ�������
			protected void onPostExecute(Bitmap result) {
				callback.onBitmapLoaded(result);
			}
		};
		task.execute();
	}
	
	/**
	 * ����bitmap ����ģ���뾶 ����һ����ģ����bitmap
	 * �ȽϺ�ʱ
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
	 * ���ļ��ж�ȡһ��Bitmap
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
	 * ��bitmapѹ����jpg��ʽ���浽File�ļ���
	 * @param bitmap
	 * @param file
	 * @throws FileNotFoundException 
	 */
	public static void save(Bitmap bitmap, File file) throws FileNotFoundException{
		if(!file.getParentFile().exists()){
			file.getParentFile().mkdirs(); //��Ŀ¼�������򴴽� 
		}
		FileOutputStream fos = new FileOutputStream(file);
		bitmap.compress(CompressFormat.JPEG, 100, fos);
	}
	

	/**
	 * ͨ��һ��������  �����û���Ҫ�Ĵ�С����ͼƬѹ���� ����Bitmap����
	 * @param is   ����Դ
	 * @param width   ͼƬ��Ŀ����
	 * @param height   ͼƬ��Ŀ��߶�
	 * @return  ѹ�������Bitmap
	 */
	public static Bitmap loadBitmap(InputStream is, int width, int height) throws IOException {
		//1. ���������� ��ȡ��byte[] 
		//���������е����� д�� �ֽ��������
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		byte[] buffer = new byte[10*1024];
		int length = 0;
		while((length=is.read(buffer)) != -1){
			bos.write(buffer, 0, length);
			bos.flush();
		}
		//��������еõ�byte[]  ����һ������Bitmap����
		byte[] bytes = bos.toByteArray();
		bos.close();
		//2. ����byte[]  ��ȡͼƬ��ԭʼ�����
		Options opts = new Options();
		//��������ͼƬ��bounds����
		opts.inJustDecodeBounds = true;
		BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opts);
		int w = opts.outWidth / width; //��ȵ�ѹ������
		int h = opts.outHeight / height; //�߶ȵ�ѹ������
		//3. �����û������� �����ѹ������
		int scale = w > h ? w : h;
		//4. �ٴν���byte[] ��ȡѹ�������ͼƬ
		opts.inJustDecodeBounds = false;
		opts.inSampleSize = scale;
		Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opts);
		return bitmap;
	}

	/**
	 * ͨ��һ�������ַ  �첽����һ��ͼƬ
	 * @param path  ͼƬ·��
	 * @param bitmapCallback   �ص�����
	 */
	public static void loadBitmap(final String path, final BitmapCallback bitmapCallback) {
		AsyncTask<String, String, Bitmap> task = new AsyncTask<String, String, Bitmap>(){
			//�����߳���ִ��  ������ ��ȡBitmap
			protected Bitmap doInBackground(String... params) {
				try {
					//http://xxxx/x/x/xxxx/xxxx/xx.jpg
					String filename = path.substring(path.lastIndexOf("/"));
					File file = new File(MusicApplication.getApp().getCacheDir(), "images"+filename);
					Bitmap b =loadBitmap(file);
					if(b!=null){ //�ļ��Ѿ����ع� ֱ��ʹ��
						return b;
					}
					InputStream is = HttpUtils.get(path);
					b=BitmapFactory.decodeStream(is);
					//��bitmap�����ļ����� ��ʡ����
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
	 * ͨ��һ�������ַ  �첽����һ��ͼƬ ���Ұ���ѹ����������ѹ������
	 * @param path  ͼƬ·��
	 * @param scale ѹ������
	 * @param bitmapCallback   �ص�����
	 */
	public static void loadBitmap(final String path, final int scale, final BitmapCallback bitmapCallback) {
		AsyncTask<String, String, Bitmap> task = new AsyncTask<String, String, Bitmap>(){
			//�����߳���ִ��  ������ ��ȡBitmap
			protected Bitmap doInBackground(String... params) {
				try {
					//��ȡ����ͼƬĿ¼
					String filename = path.substring(path.lastIndexOf("/"));
					File file = new File(MusicApplication.getApp().getCacheDir(), "images"+filename);
					Options opts = new Options();
					opts.inSampleSize = scale;
					if(file.exists()){ //�ļ��Ѵ���
						return BitmapFactory.decodeFile(file.getAbsolutePath(), opts);
					}
					InputStream is = HttpUtils.get(path);
					//�ӷ��������ԭʼͼƬ
					Bitmap b=BitmapFactory.decodeStream(is);
					//��ԭʼͼƬ�����ļ�
					save(b, file);
					//���ļ�����ѹ���ķ�ʽ ��ȡͼƬ
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
		/** ��ͼƬ������Ϻ�ִ�� */
		void onBitmapLoaded(Bitmap bitmap);
	}
	
}
