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
 * 封装音乐相关业务  
 */
public class MusicModel {
	
	/**
	 * 异步 搜索音乐列表
	 * @param keyword  关键字  
	 * @param callback  当音乐搜索结果加载完毕后执行的回调
	 */
	public void searchMusicList(final String keyword, final MusicListCallback callback){
		AsyncTask<String, String, List<Music>> task = new AsyncTask<String, String, List<Music>>(){
			//异步 查询搜索结果列表 封装成List
			protected List<Music> doInBackground(String... params) {
				try {
					String path = UrlFactory.getSearchMusicUrl(keyword);
					InputStream is = HttpUtils.get(path);
					String json = HttpUtils.isToString(is);
					//解析json
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
	 * 通过歌词路径  加载歌词 并 解析歌词
	 * 把一整篇歌词内容都封装到HashMap
	 * @param lrcPath
	 * @param callback
	 */
	public void loadLrc(final String lrcPath, final LrcCallback callback){
		AsyncTask<String, String, HashMap<String, String>> task = new AsyncTask<String, String, HashMap<String,String>>(){
			/** 发送http请求 下载歌词 */
			protected HashMap<String, String> doInBackground(String... params) {
				try {
					if(lrcPath==null || lrcPath.equals("")){
						//歌词不存在  
						return null;
					}
					//声明缓存文件File对象
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
							//每读取1行 向文件中写入一行
							out.println(line);
							out.flush();
						}
						//line:
						//line: [title]微微一笑很倾城
						//line: [00:00.90]微微一笑很倾城
						if("".equals(line.trim())){
							continue;  //继续循环下一行
						}
						if(! line.contains(".")){ //不包含.  格式不对
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
	 * 通过songId 查询 音乐路径和基本信息
	 * @param songId
	 * @param callback
	 */
	public void loadSongInfoBySongId(final String songId, final SongInfoCallback callback){
		AsyncTask<String, String, Music> task = new AsyncTask<String, String, Music>(){
			/** 发送http请求 通过songId 获取基本信息 */
			public Music doInBackground(String... params) {
				Music m = new Music();
				try {
					String path = UrlFactory.getSongInfoUrl(songId);
					InputStream is = HttpUtils.get(path);
					//把输入流中的数据解析为json字符串
					String json=HttpUtils.isToString(is);
					//解析json字符串
					JSONObject obj = new JSONObject(json);
					JSONArray urlAry = obj.getJSONObject("songurl").getJSONArray("url");
					//把urlAry解析成 List<SongUrl>
					List<SongUrl> urls = JSONParser.parseUrls(urlAry);
					JSONObject infoObj = obj.getJSONObject("songinfo");
					//把infoObj解析成SongInfo
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
	 * 获取新歌榜列表  需要发送http请求
	 * 该操作需要在工作线程中执行 
	 * @param offset  起始位置
	 * @param size	   查询数量
	 */
	public void getNewMusicList(final int offset, final int size, final MusicListCallback callback) {
		AsyncTask<String, String, List<Music>> task = new AsyncTask<String, String, List<Music>>(){
			/** 子线程中执行 可以直接发送http请求 */
			public List<Music> doInBackground(String... params) {
				try {
					//获取地址
					String url =UrlFactory.getNewMusicListUrl(offset, size);
					//发送http请求
					InputStream is=HttpUtils.get(url);
					//解析is中的xml数据 获取List<Music>
					List<Music> musics = XmlParser.parseMusicList(is);
					return musics;
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}
			/** 当doInBackground方法执行完毕后 
			 * 将会在主线程中执行该方法 */
			public void onPostExecute(List<Music> musics) {
				//Log.i("info", ""+musics.toString());
				callback.onMusicListLoaded(musics);
			}
		};
		task.execute();
	}

	/**
	 * 获取热歌榜列表  需要发送http请求
	 * 该操作需要在工作线程中执行 
	 * @param offset  起始位置
	 * @param size	   查询数量
	 */
	public void getHotMusicList(final int offset, final int size, final MusicListCallback callback) {
		AsyncTask<String, String, List<Music>> task = new AsyncTask<String, String, List<Music>>(){
			/** 子线程中执行 可以直接发送http请求 */
			public List<Music> doInBackground(String... params) {
				try {
					//获取地址
					String url =UrlFactory.getHotMusicListUrl(offset, size);
					//发送http请求
					InputStream is=HttpUtils.get(url);
					//解析is中的xml数据 获取List<Music>
					List<Music> musics = XmlParser.parseMusicList(is);
					return musics;
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}
			/** 当doInBackground方法执行完毕后 
			 * 将会在主线程中执行该方法 */
			public void onPostExecute(List<Music> musics) {
				//Log.i("info", ""+musics.toString());
				callback.onMusicListLoaded(musics);
			}
		};
		task.execute();
	}

	/**
	 * 回调接口
	 */
	public interface MusicListCallback{
		/**
		 * 回调方法  当音乐列表加载完毕后
		 * 将会调用该回调方法 
		 * 把得到的音乐列表结果交给调用者
		 * 执行后续业务。
		 */
		void onMusicListLoaded(List<Music> musics);
	}
	
	
	public interface SongInfoCallback {
		/**
		 * 当音乐基本信息加载完毕后执行
		 * @param urls  封装了url列表
		 * @param info  封装了音乐详细信息
		 */
		void onSongInfoLoaded(List<SongUrl> urls, SongInfo info);
	}
	
	public interface LrcCallback {
		/**
		 * 当音乐文件下载并解析完毕后执行
		 * @param lrc
		 */
		void onLrcLoaded(HashMap<String, String> lrc);
	}
	
}



