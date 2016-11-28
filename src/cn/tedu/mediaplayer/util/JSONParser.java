package cn.tedu.mediaplayer.util;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.tedu.mediaplayer.entity.Music;
import cn.tedu.mediaplayer.entity.SongInfo;
import cn.tedu.mediaplayer.entity.SongUrl;

/**
 * ���ڽ���json�ַ����Ĺ�����
 */
public class JSONParser {

	/**
	 * ����url����
	 * 
	 * @param urlAry
	 *            [{},{},{},{}]
	 * @return
	 * @throws JSONException
	 */
	public static List<SongUrl> parseUrls(JSONArray urlAry) throws JSONException {
		List<SongUrl> urls = new ArrayList<SongUrl>();
		for (int i = 0; i < urlAry.length(); i++) {
			JSONObject o = urlAry.getJSONObject(i);
			SongUrl url = new SongUrl(o.getInt("song_file_id"), o.getInt("file_size"), o.getInt("file_duration"),
					o.getInt("file_bitrate"), o.getString("show_link"), o.getString("file_extension"),
					o.getString("file_link"));
			urls.add(url);
		}
		return urls;
	}

	/**
	 * ����songinfo����
	 * 
	 * @param infoObj
	 *            {......}
	 * @return
	 * @throws JSONException
	 */
	public static SongInfo parseSongInfo(JSONObject infoObj) throws JSONException {
		SongInfo info = new SongInfo(infoObj.getString("pic_huge"), infoObj.getString("album_1000_1000"),
				infoObj.getString("pic_singer"), infoObj.getString("album_500_500"), infoObj.getString("compose"),
				infoObj.getString("artist_500_500"), infoObj.getString("file_duration"),
				infoObj.getString("album_title"), infoObj.getString("title"), infoObj.getString("pic_radio"),
				infoObj.getString("language"), infoObj.getString("lrclink"), infoObj.getString("pic_big"),
				infoObj.getString("pic_premium"), infoObj.getString("artist_480_800"), infoObj.getString("artist_id"),
				infoObj.getString("album_id"), infoObj.getString("artist_1000_1000"),
				infoObj.getString("all_artist_id"), infoObj.getString("artist_640_1136"),
				infoObj.getString("publishtime"), infoObj.getString("share_url"), infoObj.getString("author"),
				infoObj.getString("pic_small"), infoObj.getString("song_id"));
		return info;
	}

	/**
	 * ����������������б�json
	 * 
	 * @param ary
	 *            [{},{},{},{}]
	 * @return
	 * @throws JSONException 
	 */
	public static List<Music> parseSearchResult(JSONArray ary) throws JSONException {
		List<Music> musics = new ArrayList<Music>();
		for(int i=0; i<ary.length(); i++){
			JSONObject o = ary.getJSONObject(i);
			Music m = new Music();
			m.setTitle(o.getString("title"));
			m.setSong_id(o.getString("song_id"));
			m.setAuthor(o.getString("author"));
			m.setArtist_id(o.getString("artist_id"));
			m.setAlbum_title(o.getString("album_title"));
			musics.add(m);
		}
		return musics;
	}

}
