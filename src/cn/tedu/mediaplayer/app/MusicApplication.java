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
		app = this; //把当前对象存入静态变量app
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
	
	/** 获取当前正在播放的音乐对象 */
	public Music getCurrentMusic(){
		return this.musics.get(position);
	}

	/**
	 * 跳转到上一首歌
	 */
	public void preMusic() {
		position = position == 0 ? 0 : position-1;
	}

	/** 跳转到下一首歌 */
	public void nextMusic(){
		position = position == musics.size()-1 ? 0 : position+1;
	}
	
}


