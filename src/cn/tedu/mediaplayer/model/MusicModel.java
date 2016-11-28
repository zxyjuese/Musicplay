package cn.tedu.mediaplayer.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;
import cn.tedu.mediaplayer.app.MusicApplication;
import cn.tedu.mediaplayer.entity.Music;
import cn.tedu.mediaplayer.entity.SongInfo;
import cn.tedu.mediaplayer.entity.SongUrl;
import cn.tedu.mediaplayer.util.HttpUtils;
import cn.tedu.mediaplayer.util.JSONParser;
import cn.tedu.mediaplayer.util.UrlFactory;
import cn.tedu.mediaplayer.util.XmlParser;

/**
 * ��װ�������ҵ��  
 */
public class MusicModel {
	
	/**
	 * �첽 ���������б�
	 * @param keyword  �ؼ���  
	 * @param callback  �������������������Ϻ�ִ�еĻص�
	 */
	public void searchMusicList(final String keyword, final MusicListCallback callback){
		AsyncTask<String, String, List<Music>> task = new AsyncTask<String, String, List<Music>>(){
			//�첽 ��ѯ��������б� ��װ��List
			protected List<Music> doInBackground(String... params) {
				try {
					String path = UrlFactory.getSearchMusicUrl(keyword);
					InputStream is = HttpUtils.get(path);
					String json = HttpUtils.isToString(is);
					//����json
					JSONObject obj = new JSONObject(json);
					JSONArray ary=obj.getJSONArray("song_list");
					List<Music> musics=JSONParser.parseSearchResult(ary);
					return musics;
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}
			protected void onPostExecute(List<Music> result) {
				callback.onMusicListLoaded(result);
			}
		};
		task.execute();
	}
	
	
	/**
	 * ͨ�����·��  ���ظ�� �� �������
	 * ��һ��ƪ������ݶ���װ��HashMap
	 * @param lrcPath
	 * @param callback
	 */
	public void loadLrc(final String lrcPath, final LrcCallback callback){
		AsyncTask<String, String, HashMap<String, String>> task = new AsyncTask<String, String, HashMap<String,String>>(){
			/** ����http���� ���ظ�� */
			protected HashMap<String, String> doInBackground(String... params) {
				try {
					if(lrcPath==null || lrcPath.equals("")){
						//��ʲ�����  
						return null;
					}
					//���������ļ�File����
					String filename=lrcPath.substring(lrcPath.lastIndexOf("/"));
					File file = new File(MusicApplication.getApp().getCacheDir(), "lrc"+filename);
					InputStream is = null;
					PrintWriter out = null;
					boolean isNewFile = true;
					if(file.exists()){
						 is = new FileInputStream(file);
						 isNewFile = false;
					}else{
						if(!file.getParentFile().exists()){
							file.getParentFile().mkdirs();
						}
						out = new PrintWriter(file);
						is = HttpUtils.get(lrcPath);
						isNewFile = true;
					}
					HashMap<String, String> map = new HashMap<String, String>();
					BufferedReader reader = new BufferedReader(new InputStreamReader(is));
					String line = null;
					while((line=reader.readLine()) != null){
						if(isNewFile){
							//ÿ��ȡ1�� ���ļ���д��һ��
							out.println(line);
							out.flush();
						}
						//line:
						//line: [title]΢΢һЦ�����
						//line: [00:00.90]΢΢һЦ�����
						if("".equals(line.trim())){
							continue;  //����ѭ����һ��
						}
						if(! line.contains(".")){ //������.  ��ʽ����
							continue; 
						}
						String time = line.substring(1, 6);
						String content = line.substring(10);
						map.put(time, content);
					}
					if(out!=null){
						out.close();
					}
					return map;
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}
			protected void onPostExecute(HashMap<String, String> lrc) {
				callback.onLrcLoaded(lrc);
			}
		};
		task.execute();
	}
	
	/**
	 * ͨ��songId ��ѯ ����·���ͻ�����Ϣ
	 * @param songId
	 * @param callback
	 */
	public void loadSongInfoBySongId(final String songId, final SongInfoCallback callback){
		AsyncTask<String, String, Music> task = new AsyncTask<String, String, Music>(){
			/** ����http���� ͨ��songId ��ȡ������Ϣ */
			public Music doInBackground(String... params) {
				Music m = new Music();
				try {
					String path = UrlFactory.getSongInfoUrl(songId);
					InputStream is = HttpUtils.get(path);
					//���������е����ݽ���Ϊjson�ַ���
					String json=HttpUtils.isToString(is);
					//����json�ַ���
					JSONObject obj = new JSONObject(json);
					JSONArray urlAry = obj.getJSONObject("songurl").getJSONArray("url");
					//��urlAry������ List<SongUrl>
					List<SongUrl> urls = JSONParser.parseUrls(urlAry);
					JSONObject infoObj = obj.getJSONObject("songinfo");
					//��infoObj������SongInfo
					SongInfo info = JSONParser.parseSongInfo(infoObj);
					m.setUrls(urls);
					m.setInfo(info);
				} catch (Exception e) {
					e.printStackTrace();
				}
				return m;
			}
			protected void onPostExecute(Music music) {
				callback.onSongInfoLoaded(music.getUrls(), music.getInfo());
			}
		};
		task.execute();
	}
	
	/**
	 * ��ȡ�¸���б�  ��Ҫ����http����
	 * �ò�����Ҫ�ڹ����߳���ִ�� 
	 * @param offset  ��ʼλ��
	 * @param size	   ��ѯ����
	 */
	public void getNewMusicList(final int offset, final int size, final MusicListCallback callback) {
		AsyncTask<String, String, List<Music>> task = new AsyncTask<String, String, List<Music>>(){
			/** ���߳���ִ�� ����ֱ�ӷ���http���� */
			public List<Music> doInBackground(String... params) {
				try {
					//��ȡ��ַ
					String url =UrlFactory.getNewMusicListUrl(offset, size);
					//����http����
					InputStream is=HttpUtils.get(url);
					//����is�е�xml���� ��ȡList<Music>
					List<Music> musics = XmlParser.parseMusicList(is);
					return musics;
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}
			/** ��doInBackground����ִ����Ϻ� 
			 * ���������߳���ִ�и÷��� */
			public void onPostExecute(List<Music> musics) {
				//Log.i("info", ""+musics.toString());
				callback.onMusicListLoaded(musics);
			}
		};
		task.execute();
	}

	/**
	 * ��ȡ�ȸ���б�  ��Ҫ����http����
	 * �ò�����Ҫ�ڹ����߳���ִ�� 
	 * @param offset  ��ʼλ��
	 * @param size	   ��ѯ����
	 */
	public void getHotMusicList(final int offset, final int size, final MusicListCallback callback) {
		AsyncTask<String, String, List<Music>> task = new AsyncTask<String, String, List<Music>>(){
			/** ���߳���ִ�� ����ֱ�ӷ���http���� */
			public List<Music> doInBackground(String... params) {
				try {
					//��ȡ��ַ
					String url =UrlFactory.getHotMusicListUrl(offset, size);
					//����http����
					InputStream is=HttpUtils.get(url);
					//����is�е�xml���� ��ȡList<Music>
					List<Music> musics = XmlParser.parseMusicList(is);
					return musics;
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}
			/** ��doInBackground����ִ����Ϻ� 
			 * ���������߳���ִ�и÷��� */
			public void onPostExecute(List<Music> musics) {
				//Log.i("info", ""+musics.toString());
				callback.onMusicListLoaded(musics);
			}
		};
		task.execute();
	}

	/**
	 * �ص��ӿ�
	 */
	public interface MusicListCallback{
		/**
		 * �ص�����  �������б������Ϻ�
		 * ������øûص����� 
		 * �ѵõ��������б�������������
		 * ִ�к���ҵ��
		 */
		void onMusicListLoaded(List<Music> musics);
	}
	
	
	public interface SongInfoCallback {
		/**
		 * �����ֻ�����Ϣ������Ϻ�ִ��
		 * @param urls  ��װ��url�б�
		 * @param info  ��װ��������ϸ��Ϣ
		 */
		void onSongInfoLoaded(List<SongUrl> urls, SongInfo info);
	}
	
	public interface LrcCallback {
		/**
		 * �������ļ����ز�������Ϻ�ִ��
		 * @param lrc
		 */
		void onLrcLoaded(HashMap<String, String> lrc);
	}
	
}



