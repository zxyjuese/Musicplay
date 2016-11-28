package cn.tedu.mediaplayer.service;

import java.io.IOException;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Binder;
import android.os.IBinder;
import cn.tedu.mediaplayer.util.GlobalConsts;

/** 音乐播放服务 */
public class PlayMusicService extends Service{
	private MediaPlayer player = new MediaPlayer();
	private boolean isLoop = true;
	
	/**  当第一次绑定service时 执行一次  */
	public void onCreate() {
		super.onCreate();
		//为player注册监听
		player.setOnPreparedListener(new OnPreparedListener() {
			//当player准备完毕后执行该方法
			public void onPrepared(MediaPlayer mp) {
				player.start();
				//发送一个音乐已经开始播放广播
				Intent intent = new Intent(GlobalConsts.ACTION_MUSIC_STARTED);
				sendBroadcast(intent);
			}
		});
		//启动一个工作线程 每1s发一次广播
		new Thread(){
			public void run() {
				while(isLoop){
					try {
						Thread.sleep(1000);
						//发送广播
						if(player.isPlaying()){
							int total=player.getDuration();
							int current=player.getCurrentPosition();
							Intent intent = new Intent(GlobalConsts.ACTION_UPDATE_MUSIC_PROGRESS);
							intent.putExtra("total", total);
							intent.putExtra("current", current);
							sendBroadcast(intent);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}.start();
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return new MusicBinder();
	}
	
	@Override
	public void onDestroy() {
		//释放MediaPlayer
		player.release();
		//停止工作线程
		isLoop = false;
		super.onDestroy();
	}
	
	/**
	 * 在binder类型中声明供客户端调用的接口方法
	 */
	public class MusicBinder extends Binder{
		
		public void seekTo(int position){
			player.seekTo(position);
		}
		
		/** 暂停或播放 */
		public void startOrPause(){
			if(player.isPlaying()){
				player.pause();
			}else{
				player.start();
			}
		}
		
		/** 播放音乐 */
		public void playMusic(String url){
			try {
				player.reset();
				player.setDataSource(url);
				player.prepareAsync();
				//必须得等准备好了之后才可以start
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}



