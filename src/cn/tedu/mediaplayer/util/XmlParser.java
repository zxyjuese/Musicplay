package cn.tedu.mediaplayer.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Xml;
import cn.tedu.mediaplayer.entity.Music;

/**
 * 解析XML的工具类
 */
public class XmlParser {
	/**
	 * 解析音乐列表
	 * @param is  输入流
	 * @return  音乐集合 List<Music>
	 * @throws Exception 
	 */
	public static List<Music> parseMusicList(InputStream is) throws Exception {
		List<Music> musics = new ArrayList<Music>();
		XmlPullParser parser=Xml.newPullParser();
		parser.setInput(is, "utf-8");
		int event = parser.getEventType();
		Music music = null;
		while(event != XmlPullParser.END_DOCUMENT){
			switch (event) {
			case XmlPullParser.START_TAG: //开始标签
				String tagName = parser.getName();
				if(tagName.equals("song")){
					music = new Music();
					musics.add(music);
				}else if(tagName.equals("artist_id")){
					music.setArtist_id(parser.nextText());
				}else if(tagName.equals("language")){
					music.setLanguage(parser.nextText());
				}else if(tagName.equals("pic_big")){
					music.setPic_big(parser.nextText());
				}else if(tagName.equals("pic_small")){
					music.setPic_small(parser.nextText());
				}else if(tagName.equals("lrclink")){
					music.setLrclink(parser.nextText());
				}else if(tagName.equals("all_artist_id")){
					music.setAll_artist_id(parser.nextText());
				}else if(tagName.equals("file_duration")){
					music.setFile_duration(parser.nextText());
				}else if(tagName.equals("song_id")){
					music.setSong_id(parser.nextText());
				}else if(tagName.equals("title")){
					music.setTitle(parser.nextText());
				}else if(tagName.equals("author")){
					music.setAuthor(parser.nextText());
				}else if(tagName.equals("album_id")){
					music.setAlbum_id(parser.nextText());
				}else if(tagName.equals("album_title")){
					music.setAlbum_title(parser.nextText());
				}else if(tagName.equals("artist_name")){
					music.setArtist_name(parser.nextText());
				}
				
				break;
			}
			event = parser.next();
		}
		return musics;
	}
}



