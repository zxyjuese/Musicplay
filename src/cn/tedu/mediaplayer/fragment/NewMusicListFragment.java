package cn.tedu.mediaplayer.fragment;

import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;
import cn.tedu.mediaplayer.R;
import cn.tedu.mediaplayer.adapter.MusicAdapter;
import cn.tedu.mediaplayer.app.MusicApplication;
import cn.tedu.mediaplayer.entity.Music;
import cn.tedu.mediaplayer.entity.SongInfo;
import cn.tedu.mediaplayer.entity.SongUrl;
import cn.tedu.mediaplayer.model.MusicModel;
import cn.tedu.mediaplayer.model.MusicModel.MusicListCallback;
import cn.tedu.mediaplayer.model.MusicModel.SongInfoCallback;
import cn.tedu.mediaplayer.service.PlayMusicService.MusicBinder;

/**
 * �����¸���б����  Fragment
 */
public class NewMusicListFragment extends Fragment{
	private ListView listView;
	private MusicAdapter adapter;
	private List<Music> musics;
	private MusicModel model;
	private MusicBinder binder;
	
	/**
	 * ���������ڷ����������Զ�����
	 * ��viewpager��Ҫ��ȡFragment��view����ʱ
	 */
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_music_list, null);
		//��ʼ��Fragment�еĿؼ�
		listView = (ListView) view.findViewById(R.id.listView);
		//���ü���
		setListeners();
		//����ҵ������  �����¸���б�
		model = new MusicModel();
		model.getNewMusicList(0, 20, new MusicListCallback() {
			public void onMusicListLoaded(List<Music> musics) {
				NewMusicListFragment.this.musics = musics;
				adapter = new MusicAdapter(getActivity(), musics, listView);
				listView.setAdapter(adapter);
			}
		});
		return view;
	}
	
	/**
	 * ���ü���
	 */
	public void setListeners(){
		//����ListVIewʱ
		listView.setOnScrollListener(new OnScrollListener() {
			private boolean isBottom = false;
			private boolean requesting = false;
			//����״̬�ı�ʱִ�� 
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				switch (scrollState) {
				case OnScrollListener.SCROLL_STATE_IDLE:
					//Log.i("info", "SCROLL_STATE_IDLE");
					if(isBottom && !requesting){
						//Log.i("info", "�������ǵ�����....");
						//������һҳ����
						requesting = true;
						model.getNewMusicList(musics.size(), 20, new MusicListCallback() {
							public void onMusicListLoaded(List<Music> musics) {
								if(musics.isEmpty()){
									Toast.makeText(getActivity(), "T-T  û����", Toast.LENGTH_SHORT).show();
									return;
								}
								//�ѷ���˷��ص���һҳ������ 
								//����ӵ���ǰ����ʹ�õ�musics������
								NewMusicListFragment.this.musics.addAll(musics);
								//����adapter
								adapter.notifyDataSetChanged();
								requesting = false;
							}
						});
						
					}
					break;
				case OnScrollListener.SCROLL_STATE_FLING:
					//Log.i("info", "SCROLL_STATE_FLING");
					break;
				case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
					//Log.i("info", "SCROLL_STATE_TOUCH_SCROLL");
					break;
				}
			}
			//����ʱִ��  ִ��Ƶ�ʷǳ���
			public void onScroll(AbsListView view, 
					int firstVisibleItem, 
					int visibleItemCount, 
					int totalItemCount) {
				if(firstVisibleItem + visibleItemCount == totalItemCount){
					isBottom = true;
				}else{
					isBottom = false;
				}
			}
		});
		
		//���listView��itemʱ
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				//�Ѳ����б���position����Application
				MusicApplication app = MusicApplication.getApp();
				app.setMusics(musics);
				app.setPosition(position);
				//����ҵ���  ��ȡ���ֵĻ�����Ϣ
				final Music m=musics.get(position);
				String songId = m.getSong_id();
				model.loadSongInfoBySongId(songId, new SongInfoCallback() {
					public void onSongInfoLoaded(List<SongUrl> urls, SongInfo info) {
						//�ѻ�ȡ����urls������songInfo������뵱ǰmusic��
						//�Ժ�Ҫ��
						m.setUrls(urls);
						m.setInfo(info);
						//��������
						String url=urls.get(0).getFile_link();
						binder.playMusic(url);
					}
				});
			}
		});
	}
	
	public void setBinder(MusicBinder binder){
		this.binder = binder;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		//����adapter�е����߳�
		adapter.stopThread();
	}
	
}






