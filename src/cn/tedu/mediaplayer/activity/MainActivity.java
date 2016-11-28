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
		//�ؼ���ʼ��
		setViews();
		//ΪviewPager����������
		setPagerAdapter();
		//���ü�����
		setListeners();
		//��Service
		bindMusicService();
		//ע��������
		registComponents();
	}
	
	/**
	 * ע��������
	 */
	private void registComponents() {
		receiver = new MusicStateReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(GlobalConsts.ACTION_MUSIC_STARTED);
		filter.addAction(GlobalConsts.ACTION_UPDATE_MUSIC_PROGRESS);
		this.registerReceiver(receiver, filter);
	}

	/**
	 * ��PlayMusicService��� 
	 */
	private void bindMusicService() {
		Intent intent = new Intent(this, PlayMusicService.class);
		conn = new ServiceConnection() {
			//����service�������쳣�Ͽ�ʱִ��
			public void onServiceDisconnected(ComponentName name) {
			}
			//����service���ӽ����ɹ���ִ��
			public void onServiceConnected(ComponentName name, IBinder service) {
				binder = (MusicBinder) service;
				//��binder���󴫵ݸ�����Fragment
				NewMusicListFragment f1=(NewMusicListFragment) fragments.get(0);
				f1.setBinder(binder);
				HotMusicListFragment f2 = (HotMusicListFragment) fragments.get(1);
				f2.setBinder(binder);
			}
		};
		this.bindService(intent, conn, Service.BIND_AUTO_CREATE);
	}

	/**
	 * ���ü�����
	 */
	private void setListeners() {
		//�����Ž����е�ר��ͼƬ��Ӽ��� 
		//��������ṩ���ذ汾��AlertDialog
		ivPMAlbum.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				//��ȡ��ǰ���ֶ���
				MusicApplication app = MusicApplication.getApp();
				final Music m = app.getCurrentMusic();
				final List<SongUrl> urls = m.getUrls();
				//����alertDialog �ṩ���ذ汾
				AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
				String[] items = new String[urls.size()];
				for(int i=0; i<items.length; i++){
					SongUrl url = urls.get(i);
					items[i] = Math.floor(100.0*url.getFile_size()/1024/1024)/100+"M";
				}
				builder.setTitle("ѡ�����ذ汾")
					.setItems(items, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							//Log.i("info", "ѡ��İ汾��"+which);
							SongUrl url = urls.get(which);
							String title = m.getTitle();
							int bitrate = url.getFile_bitrate();
							String path = url.getFile_link();
							if(path.equals("")){
								path = url.getShow_link();
							}
							int filesize = url.getFile_size();
							//����Service
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
		
		//����������б���Ӽ���
		lvSearchMusic.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				//�ѵ�ǰ�����б�List �� position ����application
				MusicApplication app = MusicApplication.getApp();
				app.setMusics(searchMusicList);
				app.setPosition(position);
				//��ȡ��ǰ��Ҫ���ŵ�����
				final Music m = searchMusicList.get(position);
				//ͨ��songId ��ȡ������Ϣ
				model.loadSongInfoBySongId(m.getSong_id(), new SongInfoCallback() {
					public void onSongInfoLoaded(List<SongUrl> urls, SongInfo info) {
						m.setUrls(urls);
						m.setInfo(info);
						String url = urls.get(0).getFile_link();
						if(url.equals("")){
							url = urls.get(0).getShow_link();
						}
						//��������
						binder.playMusic(url);
					}
				});
			}
		});
		
		
		//rlPlayMusic�������touch�¼�
		rlPlayMusic.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				return true;
			}
		});
		
		//seekBar��Ӽ���
		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if(fromUser){ //���ȸı������û������
					//��λ����Ӧ��λ�� ��������
					binder.seekTo(progress);
				}
			}
		});
		//RadioGroup����ViewPager
		radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId) {
				case R.id.radioNew: //ѡ�����¸��
					viewPager.setCurrentItem(0);
					break;
				case R.id.radioHot: //ѡ�����ȸ��
					viewPager.setCurrentItem(1);
					break;
				}
			}
		});
		
		//ViewPager����RadioGroup
		viewPager.setOnPageChangeListener(new OnPageChangeListener() {
			public void onPageSelected(int position) {
				switch (position) {
				case 0: //�������¸��
					radioNew.setChecked(true);
					break;
				case 1: //�������ȸ��
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
	 * ΪviewPager����������
	 */
	private void setPagerAdapter() {
		//����Fragment���� ��Ϊviewpager������Դ
		fragments = new ArrayList<Fragment>();
		//��ӵ�һҳ
		fragments.add(new NewMusicListFragment());
		fragments.add(new HotMusicListFragment());
		pagerAdapter = new MainPagerAdapter(getSupportFragmentManager());
		viewPager.setAdapter(pagerAdapter);
	}

	/**
	 * �ؼ���ʼ��
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
		//��service�����
		this.unbindService(conn);
		//ȡ��ע��㲥������
		this.unregisterReceiver(receiver);
		super.onDestroy();
	}

	/**
	 * ����
	 */
	public void doClick(View view){
		switch (view.getId()) {
		case R.id.btnToSearch: //��ʾ��������
			rlSearchMusic.setVisibility(View.VISIBLE);
			Animation anim = new TranslateAnimation(0, 0, -rlSearchMusic.getHeight(), 0);
			anim.setDuration(400);
			rlSearchMusic.startAnimation(anim);
			break;
		case R.id.btnSearch:	//��������
			searchMusic();
			break;
		case R.id.btnCancel:	//������������
			rlSearchMusic.setVisibility(View.INVISIBLE);
			anim = new TranslateAnimation(0, 0, 0, -rlSearchMusic.getHeight());
			anim.setDuration(400);
			rlSearchMusic.startAnimation(anim);
			break;
		case R.id.ivCMAlbum:  //��ʾ��rlPlayMusic����
			rlPlayMusic.setVisibility(View.VISIBLE);
			anim = new ScaleAnimation(0, 1, 0, 1, 0, rlPlayMusic.getHeight());
			anim.setDuration(400);
			rlPlayMusic.startAnimation(anim);
			break;
		}
	}
	
	/**
	 * ��������
	 */
	private void searchMusic() {
		String keyword = etKeyword.getText().toString();
		if("".equals(keyword.trim())){
			return;
		}
		//ִ������ҵ��
		model.searchMusicList(keyword, new MusicListCallback() {
			public void onMusicListLoaded(List<Music> musics) {
				//��musics����ȫ��  �Ժ�Ҫ��
				searchMusicList = musics;
				//���������б��ListView
				searchAdapter = new SearchMusicAdapter(MainActivity.this, musics);
				lvSearchMusic.setAdapter(searchAdapter);
			}
		});
	}

	@Override
	public void onBackPressed() {
		if(rlPlayMusic.getVisibility() == View.VISIBLE){
			//����rlPlayMusic
			rlPlayMusic.setVisibility(View.INVISIBLE);
			ScaleAnimation anim = new ScaleAnimation(1, 0, 1, 0, 0, rlPlayMusic.getHeight());
			anim.setDuration(400);
			rlPlayMusic.startAnimation(anim);
		}else{
			super.onBackPressed();
		}
	}
	
	/**
	 * �������ֲ���
	 */
	public void controllMusic(View view){
		MusicApplication app = MusicApplication.getApp();
		switch (view.getId()) {
		case R.id.ivPMPre:  //��һ��
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
		case R.id.ivPMNext:	//��һ��
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
		case R.id.ivPMPause: //�������ͣ
			binder.startOrPause();
			break;
		}
	}
	
	/**
	 * �㲥������ ��������״̬��صĹ㲥
	 */
	class MusicStateReceiver extends BroadcastReceiver{
		public void onReceive(Context context, Intent intent) {
			String action=intent.getAction();
			//��ȡ��ǰ���ڲ��ŵ����ֶ��� ȥapp����
			MusicApplication app = MusicApplication.getApp();
			final Music music = app.getCurrentMusic();
			if(action.equals(GlobalConsts.ACTION_UPDATE_MUSIC_PROGRESS)){
				//�������ֽ���
				int total = intent.getIntExtra("total", 0);
				int current = intent.getIntExtra("current", 0);
				//���¿ؼ�������
				seekBar.setMax(total);
				seekBar.setProgress(current);
				SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
				String ct = sdf.format(new Date(current));
				tvPMCurrentTime.setText(ct);
				tvPMTotalTime.setText(sdf.format(new Date(total)));
				//���¸��
				HashMap<String, String> lrc = music.getLrc();
				if(lrc!=null){ //����Ѿ��������
					String content=lrc.get(ct);
					if(content!=null){ //�и���뵱ǰʱ��ƥ��
						tvPMLrc.setText(content);
					}
				}
				
			}else if(action.equals(GlobalConsts.ACTION_MUSIC_STARTED)){
				//���ֿ�ʼ����
				//���ø���
				String title = music.getTitle();
				tvCMTitle.setText(Html.fromHtml(title));
				tvPMTitle.setText(Html.fromHtml(title));
				//���ø�����
				String singer = music.getAuthor();
				tvCMSinger.setText(Html.fromHtml(singer));
				tvPMSinger.setText(Html.fromHtml(singer));
				//����Բ��ͼƬ
				String path = music.getInfo().getPic_small();
				BitmapUtils.loadBitmap(path, new BitmapCallback(){
					public void onBitmapLoaded(Bitmap bitmap){
						if(bitmap!=null){
							ivCMAlbum.setImageBitmap(bitmap);
							RotateAnimation anim = new RotateAnimation(0, 360, ivCMAlbum.getWidth()/2, ivCMAlbum.getHeight()/2);
							anim.setDuration(10000);
							//�����˶�
							anim.setInterpolator(new LinearInterpolator());
							anim.setRepeatCount(RotateAnimation.INFINITE);
							ivCMAlbum.startAnimation(anim);
						}else{
							ivCMAlbum.setImageResource(R.drawable.ic_launcher);
						}
					}
				});
				//���ò��Ž����е�ר��ͼƬ
				String albumPath=music.getInfo().getAlbum_500_500();
				BitmapUtils.loadBitmap(albumPath, new BitmapCallback() {
					public void onBitmapLoaded(Bitmap bitmap) {
						if(bitmap!=null){
							ivPMAlbum.setImageBitmap(bitmap);
						}
					}
				});
				//���Ž����еı���ͼƬ
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
							//������������ͼƬ����ģ������
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
				//���ص�ǰ���ֵĸ��
				String lrcPath = music.getInfo().getLrclink();
				model.loadLrc(lrcPath, new LrcCallback() {
					public void onLrcLoaded(HashMap<String, String> lrc) {
						//����ǰmusic�������ø��
						music.setLrc(lrc);
					}
				});
				
			}
		}
	}
	
	/**
	 * viewpager��������
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




