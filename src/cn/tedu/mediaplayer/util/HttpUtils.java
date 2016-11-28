package cn.tedu.mediaplayer.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * ���ڷ���http����Ĺ�����
 */
public class HttpUtils {
	/**
	 * ��path��ַ����httpget����
	 * @param path  ������Դ·��
	 * @return
	 * @throws Exception 
	 */
	public static InputStream get(String path) throws Exception{
		URL url = new URL(path);
		HttpURLConnection conn=(HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		InputStream is = conn.getInputStream();
		return is;
	}

	/**
	 * ������������Ϊ�ַ���
	 * @param is
	 * @return
	 */
	public static String isToString(InputStream is) throws IOException{
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		String line = null;
		StringBuffer sb = new StringBuffer();
		while((line=reader.readLine())!=null){
			sb.append(line);
		}
		return sb.toString();
	}
}





