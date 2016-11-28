package cn.tedu.mediaplayer.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * url������
 * ��������url��ַ�ַ���
 */
public class UrlFactory {
	/**
	 * ��ȡ�ȸ�������ַ
	 * @param offset   ��ʼλ��
	 * @param size		���ָ���
	 * @return
	 */
	public static String getHotMusicListUrl(int offset, int size){
		String url = "http://tingapi.ting.baidu.com/v1/restserver/ting?from=qianqian&version=2.1.0&method=baidu.ting.billboard.billList&format=xml&type=2&offset="+offset+"&size="+size;
		return url;
	}

	
	/**
	 * ��ȡ�¸�������ַ
	 * @param offset   ��ʼλ��
	 * @param size		���ָ���
	 * @return
	 */
	public static String getNewMusicListUrl(int offset, int size){
		String url = "http://tingapi.ting.baidu.com/v1/restserver/ting?from=qianqian&version=2.1.0&method=baidu.ting.billboard.billList&format=xml&type=1&offset="+offset+"&size="+size;
		return url;
	}

	/**
	 * ����ͨ��songid  ��ȡsonginfo��url��ַ
	 * @param songId
	 * @return
	 */
	public static String getSongInfoUrl(String songId) {
		String url = "http://tingapi.ting.baidu.com/v1/restserver/ting?from=qianqian&version=2.1.0&method=baidu.ting.song.getInfos&format=json&songid="+songId+"&ts=1408284347323&e=JoN56kTXnnbEpd9MVczkYJCSx%2FE1mkLx%2BPMIkTcOEu4%3D&nw=2&ucf=1&res=1";
		return url;
	}

	public static String getSearchMusicUrl(String keyword) {
		try {
			keyword = URLEncoder.encode(keyword, "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		String url = "http://tingapi.ting.baidu.com/v1/restserver/ting?from=qianqian&version=2.1.0&method=baidu.ting.search.common&format=json&query="+keyword+"&page_no=1&page_size=100";
		return url;
	}
}
