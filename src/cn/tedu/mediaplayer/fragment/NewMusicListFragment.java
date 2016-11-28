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
 * 描述新歌榜列表界面  Fragment
 */
public class NewMusicListFragment extends Fragment{
	private ListView listView;
	private MusicAdapter adapter;
	private List<Music> musics;
	private MusicModel model;
	private MusicBinder binder;
	
	/**
	 * 该生命周期方法由容器自动调用
	 * 当viewpager需要获取Fragment的view对象时
	 */
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_music_list, null);
		//初始化Fragment中的控件
		listView = (ListView) view.findViewById(R.id.listView);
		//设置监听
		setListeners();
		//调用业务层代码  访问新歌榜列表
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
	 * 设置监听
	 */
	public void setListeners(){
		//滚动ListVIew时
		listView.setOnScrollListener(new OnScrollListener() {
			private boolean isBottom = false;
			private boolean requesting = false;
			//滚动状态改变时执行 
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				switch (scrollState) {
				case OnScrollListener.SCROLL_STATE_IDLE:
					//Log.i("info", "SCROLL_STATE_IDLE");
					if(isBottom && !requesting){
						//Log.i("info", "这次真的是到底了....");
						//加载下一页数据
						requesting = true;
						model.getNewMusicList(musics.size(), 20, new MusicListCallback() {
							public void onMusicListLoaded(List<Music> musics) {
								if(musics.isEmpty()){
									Toast.makeText(getActivity(), "T-T  没有了", Toast.LENGTH_SHORT).show();
									return;
								}
								//把服务端返回的下一页的数据 
								//都添加到当前正在使用的musics集合中
								NewMusicListFragment.this.musics.addAll(musics);
								//更新adapter
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
			//滚动时执行  执行频率非常高
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
		
		//点击listView的item时
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				//把播放列表与position存入Application
				MusicApplication app = MusicApplication.getApp();
				app.setMusics(musics);
				app.setPosition(position);
				//调用业务层  获取音乐的基本信息
				final Music m=musics.get(position);
				String songId = m.getSong_id();
				model.loadSongInfoBySongId(songId, new SongInfoCallback() {
					public void onSongInfoLoaded(List<SongUrl> urls, SongInfo info) {
						//把获取到的urls集合与songInfo对象存入当前music中
						//以后要用
						m.setUrls(urls);
						m.setInfo(info);
						//播放音乐
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
		//销毁adapter中的子线程
		adapter.stopThread();
	}
	
}






