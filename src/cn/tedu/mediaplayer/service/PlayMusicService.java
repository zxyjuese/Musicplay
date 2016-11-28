package cn.tedu.mediaplayer.service;

import java.io.IOException;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Binder;
import android.os.IBinder;
import cn.tedu.mediaplayer.util.GlobalConsts;

/** ���ֲ��ŷ��� */
public class PlayMusicService extends Service{
	private MediaPlayer player = new MediaPlayer();
	private boolean isLoop = true;
	
	/**  ����һ�ΰ�serviceʱ ִ��һ��  */
	public void onCreate() {
		super.onCreate();
		//Ϊplayerע�����
		player.setOnPreparedListener(new OnPreparedListener() {
			//��player׼����Ϻ�ִ�и÷���
			public void onPrepared(MediaPlayer mp) {
				player.start();
				//����һ�������Ѿ���ʼ���Ź㲥
				Intent intent = new Intent(GlobalConsts.ACTION_MUSIC_STARTED);
				sendBroadcast(intent);
			}
		});
		//����һ�������߳� ÿ1s��һ�ι㲥
		new Thread(){
			public void run() {
				while(isLoop){
					try {
						Thread.sleep(1000);
						//���͹㲥
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
		//�ͷ�MediaPlayer
		player.release();
		//ֹͣ�����߳�
		isLoop = false;
		super.onDestroy();
	}
	
	/**
	 * ��binder�������������ͻ��˵��õĽӿڷ���
	 */
	public class MusicBinder extends Binder{
		
		public void seekTo(int position){
			player.seekTo(position);
		}
		
		/** ��ͣ�򲥷� */
		public void startOrPause(){
			if(player.isPlaying()){
				player.pause();
			}else{
				player.start();
			}
		}
		
		/** �������� */
		public void playMusic(String url){
			try {
				player.reset();
				player.setDataSource(url);
				player.prepareAsync();
				//����õ�׼������֮��ſ���start
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}



