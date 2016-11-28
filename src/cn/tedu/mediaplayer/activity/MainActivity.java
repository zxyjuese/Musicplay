package cn.tedu.mediaplayer.activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.app.AlertDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Html;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import cn.tedu.mediaplayer.R;
import cn.tedu.mediaplayer.adapter.SearchMusicAdapter;
import cn.tedu.mediaplayer.app.MusicApplication;
import cn.tedu.mediaplayer.entity.Music;
import cn.tedu.mediaplayer.entity.SongInfo;
import cn.tedu.mediaplayer.entity.SongUrl;
import cn.tedu.mediaplayer.fragment.HotMusicListFragment;
import cn.tedu.mediaplayer.fragment.NewMusicListFragment;
import cn.tedu.mediaplayer.model.MusicModel;
import cn.tedu.mediaplayer.model.MusicModel.LrcCallback;
import cn.tedu.mediaplayer.model.MusicModel.MusicListCallback;
import cn.tedu.mediaplayer.model.MusicModel.SongInfoCallback;
import cn.tedu.mediaplayer.service.DownloadService;
import cn.tedu.mediaplayer.service.PlayMusicService;
import cn.tedu.mediaplayer.service.PlayMusicService.MusicBinder;
import cn.tedu.mediaplayer.util.BitmapUtils;
import cn.tedu.mediaplayer.util.BitmapUtils.BitmapCallback;
import cn.tedu.mediaplayer.util.GlobalConsts;

public class MainActivity extends FragmentActivity {
	private RadioGroup radioGroup;
	private RadioButton radioNew;
	private RadioButton radioHot;
	private ViewPager viewPager;
	private ArrayList<Fragment> fragments;
	private MainPagerAdapter pagerAdapter;
	private ServiceConnection conn;
	
	private ImageView ivCMAlbum;
	private TextView tvCMTitle;
	private TextView tvCMSinger;
	private MusicStateReceiver receiver;
	
	private RelativeLayout rlPlayMusic;
	private ImageView ivPMBackground, ivPMAlbum;
	private TextView tvPMTitle, tvPMSinger, tvPMLrc, tvPMCurrentTime, tvPMTotalTime;
	private SeekBar seekBar;
	protected MusicBinder binder;
	
	private RelativeLayout rlSearchMusic;
	private Button btnSearch, btnToSearch, btnCancel;
	private EditText etKeyword;
	private ListView lvSearchMusic;
	
	
	private MusicModel model = new MusicModel();
	protected SearchMusicAdapter searchAdapter;
	protected List<Music> searchMusicList;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//控件初始化
		setViews();
		//为viewPager设置适配器
		setPagerAdapter();
		//设置监听器
		setListeners();
		//绑定Service
		bindMusicService();
		//注册各种组件
		registComponents();
	}
	
	/**
	 * 注册各种组件
	 */
	private void registComponents() {
		receiver = new MusicStateReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(GlobalConsts.ACTION_MUSIC_STARTED);
		filter.addAction(GlobalConsts.ACTION_UPDATE_MUSIC_PROGRESS);
		this.registerReceiver(receiver, filter);
	}

	/**
	 * 绑定PlayMusicService组件 
	 */
	private void bindMusicService() {
		Intent intent = new Intent(this, PlayMusicService.class);
		conn = new ServiceConnection() {
			//当与service的连接异常断开时执行
			public void onServiceDisconnected(ComponentName name) {
			}
			//当与service连接建立成功后执行
			public void onServiceConnected(ComponentName name, IBinder service) {
				binder = (MusicBinder) service;
				//把binder对象传递给两个Fragment
				NewMusicListFragment f1=(NewMusicListFragment) fragments.get(0);
				f1.setBinder(binder);
				HotMusicListFragment f2 = (HotMusicListFragment) fragments.get(1);
				f2.setBinder(binder);
			}
		};
		this.bindService(intent, conn, Service.BIND_AUTO_CREATE);
	}

	/**
	 * 设置监听器
	 */
	private void setListeners() {
		//给播放界面中的专辑图片添加监听 
		//点击弹出提供下载版本的AlertDialog
		ivPMAlbum.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				//获取当前音乐对象
				MusicApplication app = MusicApplication.getApp();
				final Music m = app.getCurrentMusic();
				final List<SongUrl> urls = m.getUrls();
				//弹出alertDialog 提供下载版本
				AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
				String[] items = new String[urls.size()];
				for(int i=0; i<items.length; i++){
					SongUrl url = urls.get(i);
					items[i] = Math.floor(100.0*url.getFile_size()/1024/1024)/100+"M";
				}
				builder.setTitle("选择下载版本")
					.setItems(items, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							//Log.i("info", "选择的版本："+which);
							SongUrl url = urls.get(which);
							String title = m.getTitle();
							int bitrate = url.getFile_bitrate();
							String path = url.getFile_link();
							if(path.equals("")){
								path = url.getShow_link();
							}
							int filesize = url.getFile_size();
							//启动Service
							Intent intent = new Intent(MainActivity.this, DownloadService.class);
							intent.putExtra("title", title);
							intent.putExtra("bitrate", bitrate);
							intent.putExtra("path", path);
							intent.putExtra("filesize", filesize);
							startService(intent);
						}
					});
				builder.create().show();
			}
		});
		
		//给搜索结果列表添加监听
		lvSearchMusic.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				//把当前搜索列表List 与 position 存入application
				MusicApplication app = MusicApplication.getApp();
				app.setMusics(searchMusicList);
				app.setPosition(position);
				//获取当前需要播放的音乐
				final Music m = searchMusicList.get(position);
				//通过songId 获取音乐信息
				model.loadSongInfoBySongId(m.getSong_id(), new SongInfoCallback() {
					public void onSongInfoLoaded(List<SongUrl> urls, SongInfo info) {
						m.setUrls(urls);
						m.setInfo(info);
						String url = urls.get(0).getFile_link();
						if(url.equals("")){
							url = urls.get(0).getShow_link();
						}
						//播放音乐
						binder.playMusic(url);
					}
				});
			}
		});
		
		
		//rlPlayMusic界面添加touch事件
		rlPlayMusic.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				return true;
			}
		});
		
		//seekBar添加监听
		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if(fromUser){ //进度改变是由用户引起的
					//定位到相应的位置 继续播放
					binder.seekTo(progress);
				}
			}
		});
		//RadioGroup操作ViewPager
		radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId) {
				case R.id.radioNew: //选择了新歌榜
					viewPager.setCurrentItem(0);
					break;
				case R.id.radioHot: //选择了热歌榜
					viewPager.setCurrentItem(1);
					break;
				}
			}
		});
		
		//ViewPager操作RadioGroup
		viewPager.setOnPageChangeListener(new OnPageChangeListener() {
			public void onPageSelected(int position) {
				switch (position) {
				case 0: //滑到了新歌榜
					radioNew.setChecked(true);
					break;
				case 1: //滑到了热歌榜
					radioHot.setChecked(true);
					break;
				}
			}
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}
			public void onPageScrollStateChanged(int arg0) {
			}
		});
		
	}

	/**
	 * 为viewPager设置适配器
	 */
	private void setPagerAdapter() {
		//构建Fragment集合 作为viewpager的数据源
		fragments = new ArrayList<Fragment>();
		//添加第一页
		fragments.add(new NewMusicListFragment());
		fragments.add(new HotMusicListFragment());
		pagerAdapter = new MainPagerAdapter(getSupportFragmentManager());
		viewPager.setAdapter(pagerAdapter);
	}

	/**
	 * 控件初始化
	 */
	private void setViews() {
		
		rlPlayMusic = (RelativeLayout) findViewById(R.id.rlPlayMusic);
		ivPMBackground = (ImageView) findViewById(R.id.ivPMBackground);
		ivPMAlbum = (ImageView) findViewById(R.id.ivPMAlbum);
		tvPMTitle = (TextView) findViewById(R.id.tvPMTitle);
		tvPMSinger = (TextView) findViewById(R.id.tvPMSinger);
		tvPMLrc = (TextView) findViewById(R.id.tvPMLrc);
		tvPMCurrentTime = (TextView) findViewById(R.id.tvPMCurrentTime);
		tvPMTotalTime = (TextView) findViewById(R.id.tvPMTotalTime);
		seekBar = (SeekBar) findViewById(R.id.seekBar);
		
		radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
		radioNew = (RadioButton) findViewById(R.id.radioNew);
		radioHot = (RadioButton) findViewById(R.id.radioHot);
		viewPager = (ViewPager) findViewById(R.id.viewPager);
		
		ivCMAlbum = (ImageView) findViewById(R.id.ivCMAlbum);
		tvCMTitle = (TextView) findViewById(R.id.tvCMTitle);
		tvCMSinger = (TextView) findViewById(R.id.tvCMSinger);
	
		rlSearchMusic = (RelativeLayout) findViewById(R.id.rlSearchMusic);
		btnToSearch = (Button) findViewById(R.id.btnToSearch);
		btnSearch = (Button) findViewById(R.id.btnSearch);
		btnCancel = (Button) findViewById(R.id.btnCancel);
		etKeyword = (EditText) findViewById(R.id.etSMKeyword);
		lvSearchMusic = (ListView) findViewById(R.id.lvSearchMusic);
	}
	
	@Override
	protected void onDestroy() {
		//与service解除绑定
		this.unbindService(conn);
		//取消注册广播接收器
		this.unregisterReceiver(receiver);
		super.onDestroy();
	}

	/**
	 * 监听
	 */
	public void doClick(View view){
		switch (view.getId()) {
		case R.id.btnToSearch: //显示搜索界面
			rlSearchMusic.setVisibility(View.VISIBLE);
			Animation anim = new TranslateAnimation(0, 0, -rlSearchMusic.getHeight(), 0);
			anim.setDuration(400);
			rlSearchMusic.startAnimation(anim);
			break;
		case R.id.btnSearch:	//搜索音乐
			searchMusic();
			break;
		case R.id.btnCancel:	//隐藏搜索界面
			rlSearchMusic.setVisibility(View.INVISIBLE);
			anim = new TranslateAnimation(0, 0, 0, -rlSearchMusic.getHeight());
			anim.setDuration(400);
			rlSearchMusic.startAnimation(anim);
			break;
		case R.id.ivCMAlbum:  //显示出rlPlayMusic界面
			rlPlayMusic.setVisibility(View.VISIBLE);
			anim = new ScaleAnimation(0, 1, 0, 1, 0, rlPlayMusic.getHeight());
			anim.setDuration(400);
			rlPlayMusic.startAnimation(anim);
			break;
		}
	}
	
	/**
	 * 搜索音乐
	 */
	private void searchMusic() {
		String keyword = etKeyword.getText().toString();
		if("".equals(keyword.trim())){
			return;
		}
		//执行搜索业务
		model.searchMusicList(keyword, new MusicListCallback() {
			public void onMusicListLoaded(List<Music> musics) {
				//把musics存入全局  以后要用
				searchMusicList = musics;
				//更新搜索列表的ListView
				searchAdapter = new SearchMusicAdapter(MainActivity.this, musics);
				lvSearchMusic.setAdapter(searchAdapter);
			}
		});
	}

	@Override
	public void onBackPressed() {
		if(rlPlayMusic.getVisibility() == View.VISIBLE){
			//隐藏rlPlayMusic
			rlPlayMusic.setVisibility(View.INVISIBLE);
			ScaleAnimation anim = new ScaleAnimation(1, 0, 1, 0, 0, rlPlayMusic.getHeight());
			anim.setDuration(400);
			rlPlayMusic.startAnimation(anim);
		}else{
			super.onBackPressed();
		}
	}
	
	/**
	 * 控制音乐播放
	 */
	public void controllMusic(View view){
		MusicApplication app = MusicApplication.getApp();
		switch (view.getId()) {
		case R.id.ivPMPre:  //上一曲
			app.preMusic();
			final Music m = app.getCurrentMusic();
			model.loadSongInfoBySongId(m.getSong_id(), new SongInfoCallback() {
				public void onSongInfoLoaded(List<SongUrl> urls, SongInfo info) {
					m.setUrls(urls);
					m.setInfo(info);
					String url = urls.get(0).getFile_link();
					binder.playMusic(url);
				}
			});
			break;
		case R.id.ivPMNext:	//下一曲
			app.nextMusic();
			final Music m2 = app.getCurrentMusic();
			model.loadSongInfoBySongId(m2.getSong_id(), new SongInfoCallback() {
				public void onSongInfoLoaded(List<SongUrl> urls, SongInfo info) {
					m2.setUrls(urls);
					m2.setInfo(info);
					String url=urls.get(0).getFile_link();
					binder.playMusic(url);
				}
			});
			
			break;
		case R.id.ivPMPause: //点击了暂停
			binder.startOrPause();
			break;
		}
	}
	
	/**
	 * 广播接收器 接收音乐状态相关的广播
	 */
	class MusicStateReceiver extends BroadcastReceiver{
		public void onReceive(Context context, Intent intent) {
			String action=intent.getAction();
			//获取当前正在播放的音乐对象 去app中拿
			MusicApplication app = MusicApplication.getApp();
			final Music music = app.getCurrentMusic();
			if(action.equals(GlobalConsts.ACTION_UPDATE_MUSIC_PROGRESS)){
				//更新音乐进度
				int total = intent.getIntExtra("total", 0);
				int current = intent.getIntExtra("current", 0);
				//更新控件的内容
				seekBar.setMax(total);
				seekBar.setProgress(current);
				SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
				String ct = sdf.format(new Date(current));
				tvPMCurrentTime.setText(ct);
				tvPMTotalTime.setText(sdf.format(new Date(total)));
				//更新歌词
				HashMap<String, String> lrc = music.getLrc();
				if(lrc!=null){ //歌词已经加载完成
					String content=lrc.get(ct);
					if(content!=null){ //有歌词与当前时间匹配
						tvPMLrc.setText(content);
					}
				}
				
			}else if(action.equals(GlobalConsts.ACTION_MUSIC_STARTED)){
				//音乐开始播放
				//设置歌名
				String title = music.getTitle();
				tvCMTitle.setText(Html.fromHtml(title));
				tvPMTitle.setText(Html.fromHtml(title));
				//设置歌手名
				String singer = music.getAuthor();
				tvCMSinger.setText(Html.fromHtml(singer));
				tvPMSinger.setText(Html.fromHtml(singer));
				//设置圆形图片
				String path = music.getInfo().getPic_small();
				BitmapUtils.loadBitmap(path, new BitmapCallback(){
					public void onBitmapLoaded(Bitmap bitmap){
						if(bitmap!=null){
							ivCMAlbum.setImageBitmap(bitmap);
							RotateAnimation anim = new RotateAnimation(0, 360, ivCMAlbum.getWidth()/2, ivCMAlbum.getHeight()/2);
							anim.setDuration(10000);
							//匀速运动
							anim.setInterpolator(new LinearInterpolator());
							anim.setRepeatCount(RotateAnimation.INFINITE);
							ivCMAlbum.startAnimation(anim);
						}else{
							ivCMAlbum.setImageResource(R.drawable.ic_launcher);
						}
					}
				});
				//设置播放界面中的专辑图片
				String albumPath=music.getInfo().getAlbum_500_500();
				BitmapUtils.loadBitmap(albumPath, new BitmapCallback() {
					public void onBitmapLoaded(Bitmap bitmap) {
						if(bitmap!=null){
							ivPMAlbum.setImageBitmap(bitmap);
						}
					}
				});
				//播放界面中的背景图片
				String bgPath = music.getInfo().getArtist_480_800();
				if(bgPath.equals("")){
					bgPath = music.getInfo().getArtist_640_1136();
				}
				if(bgPath.equals("")){
					bgPath = music.getInfo().getArtist_500_500();
				}
				if(bgPath.equals("")){
					bgPath = music.getInfo().getAlbum_500_500();
				}
				
				BitmapUtils.loadBitmap(bgPath, 4, new BitmapCallback() {
					public void onBitmapLoaded(Bitmap bitmap) {
						if(bitmap!=null){
							//对下载下来的图片进行模糊处理
							BitmapUtils.loadBlurBitmap(bitmap, 10, new BitmapCallback() {
								public void onBitmapLoaded(Bitmap bitmap) {
									if(bitmap!=null){
										ivPMBackground.setImageBitmap(bitmap);
									}
								}
							});
						}
					}
				});
				//加载当前音乐的歌词
				String lrcPath = music.getInfo().getLrclink();
				model.loadLrc(lrcPath, new LrcCallback() {
					public void onLrcLoaded(HashMap<String, String> lrc) {
						//给当前music对象设置歌词
						music.setLrc(lrc);
					}
				});
				
			}
		}
	}
	
	/**
	 * viewpager的适配器
	 */
	class MainPagerAdapter extends FragmentPagerAdapter{

		public MainPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			return fragments.get(position);
		}

		@Override
		public int getCount() {
			return fragments.size();
		}
		
	}
	
}




