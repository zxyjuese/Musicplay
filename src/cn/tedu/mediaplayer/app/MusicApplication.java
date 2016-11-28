package cn.tedu.mediaplayer.app;

import java.util.List;

import android.app.Application;
import cn.tedu.mediaplayer.entity.Music;

public class MusicApplication extends Application {
	private List<Music> musics;
	private int position;
	private static MusicApplication app;
	
	@Override
	public void onCreate() {
		super.onCreate();
		app = this; //�ѵ�ǰ������뾲̬����app
	}
	
	public static MusicApplication getApp(){
		return app;
	}

	public void setMusics(List<Music> musics) {
		this.musics = musics;
	}

	public void setPosition(int position) {
		this.position = position;
	}
	
	/** ��ȡ��ǰ���ڲ��ŵ����ֶ��� */
	public Music getCurrentMusic(){
		return this.musics.get(position);
	}

	/**
	 * ��ת����һ�׸�
	 */
	public void preMusic() {
		position = position == 0 ? 0 : position-1;
	}

	/** ��ת����һ�׸� */
	public void nextMusic(){
		position = position == musics.size()-1 ? 0 : position+1;
	}
	
}


