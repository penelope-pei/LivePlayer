package com.activity;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import com.activity.adapter.MusicListAdapter;
import com.activity.dialog.CommonDialog;
import com.activity.dialog.DeleteDialog;
import com.activity.dialog.DeleteDialog.NoticeDialogListener;
import com.activity.info.CharacterParser;
import com.activity.info.MusicInfo;
import com.activity.info.PinyinComparator;
import com.activity.message.NotificationMsg;
import com.activity.service.GetHistoryService;
import com.activity.service.GetDataService;
import com.activity.service.PlayService;
import com.activity.utils.GestureListener;
import com.activity.utils.MusicUtils;
import com.activity.view.LyricView;
import com.activity.view.MarqueeTextView;
import com.activity.view.SideBar;
import com.activity.view.SideBar.OnTouchingLetterChangedListener;
import com.example.liveplayer.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.AlertDialog.Builder;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.BitmapFactory.Options;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

public class MusicActivity extends Activity implements OnClickListener, OnItemClickListener, 
													OnItemLongClickListener, OnTouchListener{

	public static int PLAY_MSG = 0;//播放
	public static int PAUSE_MSG = 1;//暂停
	public static int STOP_MSG = 2;//停止
	public static int CONTINUE_MSG = 3;//继续
	public static int PRIVIOUS_MSG = 4;//上一曲
	public static int NEXT_MSG = 5;//下一曲
	public static int PROGRESS_CHANGE = 6;//时间
	public static int PLAYING_MSG = 7;//
	public static int SEEKBAR_CHANGE = 8;//进度条
	public static int SHOW_NOTIFICATION = 9;  //显示通知栏
	
	public static String MUSIC_NAME = "music_name";
	public static String MUSIC_SINGER_NAME = "music_singer_name";
	public static String MUSIC_TOTAL_TIME = "music_total_time";
	public static String MUSIC_IS_PLAY = "isplay";
	public static String MUSIC_ID = "music_id";
	public static String IS_FIRST_CLICK = "is_first_click";
	public static String IS_FAVORITE = "isfavorite";
	public static String IS_SHOW_WORDS = "isshowwords";
	private String ACTION_TYPE = "action_type";
	private String DEL_DATA = "del_data";
	private String name = "";
	private String sname = "";
	private String totaltime = "";
	
	private int list_type = 0;   //显示列表内容类型   0、所有列表   1、喜爱列表   2、播放记录列表
	private int activity_type;
	public static int clickindex = 0;
	private Intent intent;
	public static boolean isPlay = false;
	public static boolean backfromplay = false;
	public static boolean isfavorite = false;
	private boolean firstclick = false;//是否第一次进来就点击播放按钮
	
	private TextView iv_back;
	private TextView textview_title;
	private TextView textview_music_resource;
	private TextView textview_no_data;
	private TextView textview_nums;
	private TextView textview_music_favorite;
	private TextView textview_music_history;
	private TextView textview_showlist_type_all;
	private TextView textview_showlist_type_fav;
	private TextView textview_showlist_type_history;
	private TextView textview_more_info;
	private ListView listview_music;
	public static LyricView mLyricView;//显示两句歌词，替换歌名和歌手
	public static RelativeLayout rl_show_music_info;
	
	public static TextView textview_music_singername;
	public static ImageView iv_paly_music;
	public static MarqueeTextView textview_music_name;
	public static Button btn_play_stop;
	private TextView textview_music_to_play;
	
	private RelativeLayout rl_music_about_layout;
	private Button btn_play_next;
	private Button btn_play_previous;
	private RelativeLayout rl_music;
	private RelativeLayout rl_music_list;
	
	
	public static List<MusicInfo> mymusiclist = null;
	public static List<MusicInfo> myfavmusiclist = null;
	private List<MusicInfo> bufferlist = null;
	public static List<MusicInfo> myhistorymusiclist = null;
	private MusicListAdapter musicListAdapter = null;
	
	private MusicInfo minfo;
	
	private Intent del_intent = null;
	private AlertDialog.Builder builder = null;  //长按ListView弹出对话框
	private String del_url = "";
	private long del_id = 0L;
	
	private int update_position = 0;
	private int update_position_in_history = 0;
	private long del_position_in_history = 0L;
	
	private Notification notification;
	
	private CommonDialog delete_dialog;
	private DeleteDialog delDialog;

	private CommonDialog show_more_dialog;
	
	private SideBar sideBar;  
    private TextView tv_dialog;//显示字母的TextView 
    private CharacterParser characterParser;//汉字转换成拼音的类
    private PinyinComparator pinyinComparator;//根据拼音来排列ListView里面的数据类 
	
    //用于开辟线程的Handler
  	private Handler threadHandler = new Handler();
  	private int common_del_position = 0;  //记录所有长按删除的音乐的id
    
    private Handler mHandler = new Handler()
    {
    	@Override
    	public void handleMessage(Message msg) {
    		// TODO Auto-generated method stub
    		super.handleMessage(msg);
    		switch(msg.what)
    		{
    		case 0:
    			delDialog.setContentView(R.layout.delete_dialog_deling);
    			delDialog.show();
    			break;
    			
    		case 1:
    			delDialog.setContentView(R.layout.finish_dialog);
    			delDialog.show();
    			break;
    			
    		case 2:
    			delDialog.setContentView(R.layout.delete_choise_dialog);
    			delDialog.show();
    			break;
    			
    		case 3:
    			getAllListfromdb();
    			if(list_type == 0)
    			{
    				updateOflocalmusic();
    			}
    			break;
    			
    		case 4:
    			getHistoryListfromdb();
    			break;
    			
    		case 5:
    			Toast.makeText(MusicActivity.this,"列表最新...", Toast.LENGTH_SHORT).show();
    			break;
    			
    		case 6:
    			Toast.makeText(MusicActivity.this,"正在获取数据,请耐心等待...", Toast.LENGTH_SHORT).show();
    			break;
    			
    		case 7:
    			//从服务中获取数据，并且把数据存到mymusiclist中
    			if(PlayService.mp3Infos != null)
    			{
    				
    				mymusiclist = PlayService.mp3Infos;
    				if(list_type == 0)
    				{
    					updateOflocalmusic();
    				}
    			}
    			break;
    			
    		case 8:
    			//如果PlayService中已经有了喜爱数据
    			if(PlayService.favmusiclist != null)
    			{
    				myfavmusiclist = PlayService.favmusiclist;
    				bufferlist = myfavmusiclist;
    				if(list_type == 1)
    				{
    					updateOffavmusic();
    				}
    			}
    			break;
    			
    		case 9:
    			getFavListfromdb();
    			if(list_type == 1)
    			{
    				updateOffavmusic();
    			}
    			break;
    			
    		case 10:
    			myhistorymusiclist = new ArrayList<MusicInfo>();
    			if(GetHistoryService.musiclist != null)
    			{
    				myhistorymusiclist = GetHistoryService.musiclist;
    			}
    			if(list_type == 2)
    			{
    				updateOfhistorymusic();
    			}
    			break;
    		default:
    			break;
    		}
    	}
    };
	//////////////////////////////////////////////////////////////////////////////////////////
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);		
		requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏
		// 启动后删除之前我们定义的通知   
        NotificationManager notificationManager = (NotificationManager) this  
                .getSystemService(NOTIFICATION_SERVICE);   
        notificationManager.cancel(0);  
		setContentView(R.layout.activity_music);
		backfromplay = false;
		
		PlayService.showPlay = false;
		//实例化汉字转拼音类
		characterParser = CharacterParser.getInstance();
		pinyinComparator = new PinyinComparator();
		
		//初始化控件
		initViews();
		//设置监听事件
		initListener();		
		//初始化列表数据
		initData();
		
		//启动获取播放历史记录服务
		getHistoryListfromdb();
	}
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		//传值，判断
		intent = this.getIntent();
		activity_type = intent.getIntExtra(MHomeActivity.TYPE, -1);
		if(activity_type == 0){
			textview_title.setText(this.getResources().getString(R.string.icon_music));
		}
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	protected void onResume()
	{		
		super.onResume();
		PlayService.showPlay = false;
		firstclick = GetDataService.firstclick;
		//注册一个广播，主要是用于通知MusicActivity更新界面
    	IntentFilter UpdateMusicFilter = new IntentFilter();  
    	UpdateMusicFilter.addAction(NotificationMsg.NOTIFICATION_MUSICACTIVITY_TO_UPDATE_LOCAL);
    	UpdateMusicFilter.addAction(NotificationMsg.NOTIFICATION_MUSICACTIVITY_TO_UPDATE_FAV);
    	UpdateMusicFilter.addAction(NotificationMsg.NOTIFICATION_MUSICACTIVITY_TO_UPDATE_HISTORY); 
        registerReceiver(updateReceiver,UpdateMusicFilter);
		
		//设置背景图片，根据GetDataService这个服务中的background_id来判断显示哪种背景
		if(GetDataService.background_id == -1)
		{
			rl_music.setBackgroundResource(BackgroundSettingActivity.imgs[0]);
		}
		else
		{
			rl_music.setBackgroundResource(BackgroundSettingActivity.imgs[GetDataService.background_id]);
		}
		
		list_type = PlayService.show_list_type;
		clickindex = PlayService.current;
		if(isPlay)
		{
			//默认播放图片,暂停键
			btn_play_stop.setBackgroundResource(R.drawable.player_play_normal);
		}else{
			btn_play_stop.setBackgroundResource(R.drawable.player_pause_normal);
		}
		
		if(backfromplay)
		{
			minfo = PlayService.mp3Infos.get(clickindex);
			name = minfo.getName();
			sname = minfo.getSinger();
			totaltime = MusicUtils.formatTime(minfo.getDuration());
			isfavorite = minfo.getFavorite();
			
			String str_current_name = name;
	    	if(str_current_name.contains("["))
			{
	    		str_current_name = str_current_name.replace("[", "=");
    			String name_str[] = str_current_name.split("=");
    			str_current_name = name_str[0];
				//str_current_name = str_current_name.split("[")[0];
			}
	    	if(str_current_name.contains(" - "))
	    	{
	    		String[] name_str = str_current_name.split(" - ");
	    		textview_music_name.setText(name_str[1]);
				textview_music_singername.setText(name_str[0]);
	    	}
	    	else
	    	{
	    		textview_music_name.setText(str_current_name);
	    		if(sname.equals("") || sname == null)
	    		{
	    			textview_music_singername.setText(R.string.unknown_singer);
	    		}
	    		else
	    		{
	    			textview_music_singername.setText(sname);
	    		}
	    	}
			
			//专辑图片覆盖
			iv_paly_music.setBackgroundResource(0);
			iv_paly_music.setImageBitmap(minfo.getThumbnail());
			if(minfo.getThumbnail() == null)
			{
				iv_paly_music.setBackgroundResource(R.drawable.default_album); 
			}
		}
		
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	@Override
	protected void onStop() {
		super.onStop();
		//释放图片资源，不然会内存泄露
		releaseImageViews();
		//启动服务，显示通知栏
		if(PlayService.mediaPlayer != null)
		{
			Intent noticationintent = new Intent();
			noticationintent.setClass(MusicActivity.this, PlayService.class);  
			noticationintent.putExtra("url", minfo.getUrl());
			noticationintent.putExtra("listPosition", clickindex);
			noticationintent.putExtra("MSG", SHOW_NOTIFICATION);
			startService(noticationintent);       //启动服务
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(updateReceiver);
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	//通过findViewById方法获取控件
	private void initViews() {
		// TODO Auto-generated method stub		
		iv_back = (TextView)findViewById(R.id.title_button_id);
		iv_paly_music = (ImageView)findViewById(R.id.paly_music_album);
		textview_more_info = (TextView)findViewById(R.id.page_title_more_info);
		textview_title = (TextView)findViewById(R.id.page_title_name);
		textview_music_resource = (TextView)findViewById(R.id.music_local_resource);
		textview_no_data = (TextView)findViewById(R.id.no_music_data);
		textview_nums = (TextView)findViewById(R.id.song_num);
		//初始化字幕滚动控件
		textview_music_name = (MarqueeTextView)findViewById(R.id.play_music_name);
		textview_music_singername = (TextView)findViewById(R.id.play_music_singer);
		textview_music_to_play = (TextView)findViewById(R.id.go_to_play_music);
		textview_music_favorite = (TextView)findViewById(R.id.music_favorite);
		textview_music_history = (TextView)findViewById(R.id.music_history);
		textview_showlist_type_all = (TextView)findViewById(R.id.music_songs_num_img);
		textview_showlist_type_fav = (TextView)findViewById(R.id.music_fav_img);
		textview_showlist_type_history = (TextView)findViewById(R.id.music_history_img);
		
		btn_play_stop = (Button)findViewById(R.id.play_on_music);
		btn_play_next = (Button)findViewById(R.id.play_next_music);
		btn_play_previous = (Button)findViewById(R.id.play_previous_music); 
		
		listview_music = (ListView)findViewById(R.id.music_list);
		mLyricView = (LyricView)findViewById(R.id.LyricShowOnMusic);
		rl_show_music_info = (RelativeLayout)findViewById(R.id.show_music_info);
		
		
		rl_music_about_layout = (RelativeLayout)findViewById(R.id.music_about_layout);
		rl_music = (RelativeLayout)findViewById(R.id.relative_music);
		rl_music_list = (RelativeLayout)findViewById(R.id.rl_music_list);
		
		sideBar = (SideBar)findViewById(R.id.music_sidebar);
		tv_dialog = (TextView)findViewById(R.id.music_sort_textview);
		sideBar.setTextView(tv_dialog);
		textview_title.setText(this.getResources().getString(R.string.icon_music));
		
		list_type = PlayService.show_list_type;
		
		//设置背景图片，根据GetDataService这个服务中的background_id来判断显示哪种背景
		if(GetDataService.background_id == -1)
		{
			rl_music.setBackgroundResource(BackgroundSettingActivity.imgs[0]);
		}
		else
		{
			rl_music.setBackgroundResource(BackgroundSettingActivity.imgs[GetDataService.background_id]);
		}
		
		mLyricView.setKLOK(true);
		if(PlayService.mediaPlayer != null && PlayService.mediaPlayer.isPlaying())
		{
			if(PlayService.LyricList != null && PlayService.LyricList.size() > 1)
			{
				//mLyricView.setSentenceEntities(PlayService.LyricList);//设置歌词资源，以显示
				mLyricView.setVisibility(4);//mLyricView.setVisibility(0);
				rl_show_music_info.setVisibility(0);//rl_show_music_info.setVisibility(4);
			}
			else
			{
				mLyricView.setVisibility(4);
				rl_show_music_info.setVisibility(0);
			}
		}
		else
		{
			mLyricView.setVisibility(4);
			rl_show_music_info.setVisibility(0);
		}
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	//添加各个控件的监听事件
	private void initListener()
	{
		listview_music.setOnItemClickListener(this);  //监听每个Item，点击歌曲进行播放
		listview_music.setOnItemLongClickListener(this);
		//左右滑动切换listview内容
		listview_music.setLongClickable(true);
		listview_music.setOnTouchListener(new MyGestureListener(this));
		
		iv_paly_music.setOnClickListener(this);
		rl_music_about_layout.setOnClickListener(this);
		mLyricView.setOnClickListener(this);
		//iv_back.setOnClickListener(this);//监听返回键		
		//textview_music_resource.setOnClickListener(this);
		//textview_more_info.setOnClickListener(this);
		//btn_play_stop.setOnClickListener(this);
		//btn_play_next.setOnClickListener(this);
		//btn_play_previous.setOnClickListener(this);
		//textview_music_favorite.setOnClickListener(this);
		//textview_music_history.setOnClickListener(this);
		
		//iv_paly_music.setOnTouchListener(this);
		//rl_music_about_layout.setOnTouchListener(this);	
		textview_music_resource.setOnTouchListener(this);
		textview_music_favorite.setOnTouchListener(this);
		textview_music_history.setOnTouchListener(this);
		iv_back.setOnTouchListener(this);
		textview_more_info.setOnTouchListener(this);
		btn_play_stop.setOnTouchListener(this);
		btn_play_next.setOnTouchListener(this);
		btn_play_previous.setOnTouchListener(this);
		//设置右侧触摸监听
		sideBar.setOnTouchingLetterChangedListener(new OnTouchingLetterChangedListener() {
			@Override
			public void onTouchingLetterChanged(String s) {
				//该字母首次出现的位置
				int position = musicListAdapter.getPositionForSection(s.charAt(0));
				if(position != -1){
					listview_music.setSelection(position);
				}
			}
		});
		
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	//开始进来，初始化数据
	private void initData()
	{
		mymusiclist = new ArrayList<MusicInfo>();
		myfavmusiclist = new ArrayList<MusicInfo>();
		myhistorymusiclist = new ArrayList<MusicInfo>();
		bufferlist = new ArrayList<MusicInfo>();
		
		musicListAdapter = new MusicListAdapter(this);
		listview_music.setAdapter(musicListAdapter);
		
		firstclick = GetDataService.firstclick;
		
		//如果正在拿数据,并且还没拿到数据，弹出提示框
		if(list_type == 0)
		{
			textview_showlist_type_all.setBackgroundResource(R.drawable.xia1);
			textview_showlist_type_fav.setBackgroundResource(R.drawable.xia);
			textview_showlist_type_history.setBackgroundResource(R.drawable.xia);
			if(!GetDataService.isfirsthasgetdata)
			{
				if(PlayService.mediaPlayer != null && PlayService.mediaPlayer.isPlaying())
				{
					PlayService.mediaPlayer.pause();
					btn_play_stop.setBackgroundResource(R.drawable.player_pause_normal);
					isPlay = false;
				}
				mHandler.sendEmptyMessage(6);
				mHandler.sendEmptyMessage(3);
			}
			else
			{
				mHandler.sendEmptyMessage(7);
				if(GetDataService.temp_numbers == GetDataService.numbers)
				{
					mHandler.sendEmptyMessage(5);
				}
			}
		}
		else if(list_type == 1)
		{
			textview_showlist_type_all.setBackgroundResource(R.drawable.xia);
			textview_showlist_type_fav.setBackgroundResource(R.drawable.xia1);
			textview_showlist_type_history.setBackgroundResource(R.drawable.xia);
			if(!GetDataService.isfirsthasgetfavdata)
			{
				mHandler.sendEmptyMessage(9);
			}
			else
			{
				mHandler.sendEmptyMessage(8);
			}
		}
		else if(list_type == 2)
		{
			textview_showlist_type_all.setBackgroundResource(R.drawable.xia);
			textview_showlist_type_fav.setBackgroundResource(R.drawable.xia);
			textview_showlist_type_history.setBackgroundResource(R.drawable.xia1);
			mHandler.sendEmptyMessage(10);
			/*if(!GetHistoryService.isfirsthasgethistorydata)
			{
				mHandler.sendEmptyMessage(4);
			}
			else 
			{
				mHandler.sendEmptyMessage(10);
			}*/
		}
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	//onClick操作
	@Override
	public void onClick(View view) {
		Intent intent = null;
		// TODO Auto-generated method stub
		switch(view.getId())
		{
		case R.id.title_button_id:
			finish();  //直接跳到上一层界面
			//设置切换动画，从左边进入，右边退出，此方法必须放在startActivity或者finish()之后
			overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
			break;
			
		case R.id.page_title_more_info:
			//弹出选择背景对话框
			showbackground_dialog();
			break;
		case R.id.music_local_resource:
			//显示所有音乐列表
			showmusiclist();
			break;
		case R.id.paly_music_album:
		case R.id.music_about_layout:
		case R.id.LyricShowOnMusic:
			//跳转到播放界面
			to_play_activity();
			break;
			
		case R.id.play_on_music:
			//暂停或播放
			play_pause_music();
			break;
			
		case R.id.play_next_music:
			//下一首
			play_next_music();
			break;
			
		case R.id.play_previous_music:
			//上一首
			play_previous_music();
			break;
			
		case R.id.music_favorite:
			//显示喜爱列表
			showfavlist();
			break;
			
		case R.id.music_history:
			//显示历史记录列表
			showhistorylist();
			break;
		}
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	//下一首操作
	private void play_next()
	{
		Intent play_next_intent = new Intent();
		clickindex++;
		if(clickindex > mymusiclist.size())
		{
			clickindex = 0;
		}
		minfo = mymusiclist.get(clickindex);
		//更新TextView内容
		updateViews();
		play_next_intent.putExtra("url", minfo.getUrl());
		play_next_intent.putExtra("MSG", NEXT_MSG);
		play_next_intent.putExtra("listPosition", clickindex);
		play_next_intent.setClass(MusicActivity.this, PlayService.class);       
		//启动服务，只在第一次启动的时候执行Service中的onCreate，后面不执行，但执行onStart，并且获取数据在onStart中获取
		startService(play_next_intent); 
		
		if(PlayService.mediaPlayer != null)
		{
			if(!PlayService.mediaPlayer.isPlaying())
			{
				mLyricView.setVisibility(4);
				rl_show_music_info.setVisibility(0);
			}
			else
			{
				if(PlayService.LyricList != null && PlayService.LyricList.size() > 1)
				{
					//mLyricView.setSentenceEntities(PlayService.LyricList);//设置歌词资源，以显示
					mLyricView.setVisibility(4);
					rl_show_music_info.setVisibility(0);
				}
				else
				{
					mLyricView.setVisibility(4);
					rl_show_music_info.setVisibility(0);
				}
			}
		}
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	//上一首操作
	private void play_previous()
	{
		Intent play_previous_intent = new Intent();
		clickindex--;
		if(clickindex < 0)
		{
			clickindex = mymusiclist.size() - 1;
		}
		minfo = mymusiclist.get(clickindex);
		//更新TextView内容
		updateViews();
		play_previous_intent.putExtra("url", minfo.getUrl());
		play_previous_intent.putExtra("MSG", PRIVIOUS_MSG);
		play_previous_intent.putExtra("listPosition", clickindex);
		play_previous_intent.setClass(MusicActivity.this, PlayService.class);  
        startService(play_previous_intent);       //启动服务
        
        if(PlayService.mediaPlayer != null)
		{
			if(!PlayService.mediaPlayer.isPlaying())
			{
				mLyricView.setVisibility(4);
				rl_show_music_info.setVisibility(0);
			}
			else
			{
				if(PlayService.LyricList != null && PlayService.LyricList.size() > 1)
				{
					//mLyricView.setSentenceEntities(PlayService.LyricList);//设置歌词资源，以显示
					mLyricView.setVisibility(4);
					rl_show_music_info.setVisibility(0);
				}
				else
				{
					mLyricView.setVisibility(4);
					rl_show_music_info.setVisibility(0);
				}
			}
		}
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	//更新列表
	private void updateViews()
	{
		btn_play_stop.setBackgroundResource(R.drawable.player_play_normal);
		//去掉歌曲名中的mp3字样
		name = minfo.getName();
		sname = minfo.getSinger();
		totaltime = MusicUtils.formatTime(minfo.getDuration());
		isfavorite = minfo.getFavorite();
		
		String str_current_name = name;
    	if(str_current_name.contains("["))
		{
    		str_current_name = str_current_name.replace("[", "=");
			String name_str[] = str_current_name.split("=");
			str_current_name = name_str[0];
			//str_current_name = str_current_name.split("[")[0];
		}
    	if(str_current_name.contains(" - "))
    	{
    		String[] name_str = str_current_name.split(" - ");
    		textview_music_name.setText(name_str[1]);
			textview_music_singername.setText(name_str[0]);
    	}
    	else
    	{
    		textview_music_name.setText(str_current_name);
    		if(sname.equals("") || sname == null)
    		{
    			textview_music_singername.setText(R.string.unknown_singer);
    		}
    		else
    		{
    			textview_music_singername.setText(sname);
    		}
    	}
		
		iv_paly_music.setBackgroundResource(0);
		iv_paly_music.setImageBitmap(minfo.getThumbnail());
		if(minfo.getThumbnail() == null)
		{
			iv_paly_music.setBackgroundResource(R.drawable.default_album); 
		}
		isPlay = true;
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	//删除之后更新控件
	private void updateViewsOfterDel()
	{
		name = minfo.getName();
		sname = minfo.getSinger();
		totaltime = MusicUtils.formatTime(minfo.getDuration());
		isfavorite = minfo.getFavorite();
		
		String str_current_name = name;
    	if(str_current_name.contains("["))
		{
    		str_current_name = str_current_name.replace("[", "=");
			String name_str[] = str_current_name.split("=");
			str_current_name = name_str[0];
			//str_current_name = str_current_name.split("[")[0];
		}
    	if(str_current_name.contains(" - "))
    	{
    		String[] name_str = str_current_name.split(" - ");
    		textview_music_name.setText(name_str[1]);
			textview_music_singername.setText(name_str[0]);
    	}
    	else
    	{
    		textview_music_name.setText(str_current_name);
    		if(sname.equals("") || sname == null)
    		{
    			textview_music_singername.setText(R.string.unknown_singer);
    		}
    		else
    		{
    			textview_music_singername.setText(sname);
    		}
    	}
		
		iv_paly_music.setBackgroundResource(0);
		iv_paly_music.setImageBitmap(minfo.getThumbnail());
		if(minfo.getThumbnail() == null)
		{
			iv_paly_music.setBackgroundResource(R.drawable.default_album); 
		}
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	private void showMusicList()
	{
		mymusiclist = new ArrayList<MusicInfo>();
		myfavmusiclist = new ArrayList<MusicInfo>();
		myhistorymusiclist = new ArrayList<MusicInfo>();
		
		//从服务中获取数据，并且把数据存到mymusiclist中
		if(PlayService.mp3Infos != null)
		{
			mymusiclist = PlayService.mp3Infos;
		}
		
		if(PlayService.favmusiclist != null)
		{
			myfavmusiclist = PlayService.favmusiclist;
		}
		
		if(GetHistoryService.musiclist != null)
		{
			myhistorymusiclist = GetHistoryService.musiclist;
		}
		
		if(list_type == 0)
		{
			textview_showlist_type_all.setBackgroundResource(R.drawable.xia1);
			textview_showlist_type_fav.setBackgroundResource(R.drawable.xia);
			textview_showlist_type_history.setBackgroundResource(R.drawable.xia);
			getAllListfromdb();
			updateOflocalmusic();
		}
		else if(list_type == 1)
		{
			textview_showlist_type_all.setBackgroundResource(R.drawable.xia);
			textview_showlist_type_fav.setBackgroundResource(R.drawable.xia1);
			textview_showlist_type_history.setBackgroundResource(R.drawable.xia);
			getFavListfromdb();
			updateOffavmusic();
		}
		else if(list_type == 2)
		{
			textview_showlist_type_all.setBackgroundResource(R.drawable.xia);
			textview_showlist_type_fav.setBackgroundResource(R.drawable.xia);
			textview_showlist_type_history.setBackgroundResource(R.drawable.xia1);
			//getHistoryListfromdb();
			//getDataFromHistorydb();
			mHandler.sendEmptyMessage(10);
			updateOfhistorymusic();
		}
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	//从数据库中获取所有音乐列表
	private void getAllListfromdb()
	{
		//防止统计列表个数出错，所以需要重新new一个mymusiclist
		mymusiclist = new ArrayList<MusicInfo>();
		PlayService.mp3Infos = new ArrayList<MusicInfo>();
		if(GetDataService.db != null)
		{
			//打开数据库
			GetDataService.db.open();
			//查找数据库内容
			Cursor c = GetDataService.db.getAllTitles();
			if (c != null && c.moveToFirst())
			{
				do{
					long music_id = c.getLong(0);
					String title = c.getString(1);  
					String artist = c.getString(2);  
					String url = c.getString(3);  
					long duration = c.getLong(4);  
					long size = c.getLong(5);
					String album = c.getString(6);  
					boolean favorite = c.getInt(7) == 1 ? true : false;
					String word_url = c.getString(8);
					String album_url = c.getString(9);
					
					MusicInfo musicInfo = new MusicInfo();  
					musicInfo.setAlbum(album);  
					musicInfo.setDuration(duration);  
					musicInfo.setSize(size);  
					musicInfo.setSinger(artist);  
					musicInfo.setUrl(url);
					//函数:getMusicName(String name)是指去掉歌曲名后面的.mp3
					musicInfo.setName(getMusicName(title));
					musicInfo.setId(music_id);
					musicInfo.setFavorite(favorite);
					musicInfo.setWordsUrl(word_url);
					musicInfo.setAlbumUrl(album_url);
					//获取专辑封面（如果数据量大的话，会很耗时——需要考虑如何开辟子线程加载）
					//url = /storage/emulated/0/Music/Download/Delacey - Dream It Possible/Dream It Possible.mp3
					Bitmap albumArt = createAlbumArt(album_url);//convertToBitmap(album_url,40,40);
					//判断歌曲是否有对于的专辑图片，没有时置空，防止专辑图片为空时出现错误，异常处理
					try {
						musicInfo.setThumbnail(albumArt);
					} catch (Exception e) {
						// TODO: handle exception
						musicInfo.setThumbnail(null);
					}
					//添加到list中
					mymusiclist.add(musicInfo);
				}while(c.moveToNext());
				mymusiclist = filledData(mymusiclist);
				// 根据a-z进行排序源数据
				Collections.sort(mymusiclist, pinyinComparator);
				PlayService.mp3Infos = mymusiclist;
				GetDataService.numbers = mymusiclist.size();
				GetDataService.temp_numbers = mymusiclist.size();
				GetDataService.isfirsthasgetdata = true;
				GetDataService.isgettingdata = false;
				GetDataService.db.close();
			}
		}
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	//从数据库中获取喜爱音乐列表
	private void getFavListfromdb()
	{
		//防止统计列表个数出错，所以需要重新new一个myfavmusiclist
		myfavmusiclist = new ArrayList<MusicInfo>();
		bufferlist = new ArrayList<MusicInfo>();
		
		if(GetDataService.db != null)
		{
			//打开数据库
			GetDataService.db.open();
			//查找数据库内容
			Cursor c = GetDataService.db.getAllTitles();
			if (c != null && c.moveToFirst())
			{
				do{
					long music_id = c.getLong(0);
					String title = c.getString(1);  
					String artist = c.getString(2);  
					String url = c.getString(3);  
					long duration = c.getLong(4);  
					long size = c.getLong(5);
					String album = c.getString(6);  
					boolean favorite = c.getInt(7) == 1 ? true : false;
					String word_url = c.getString(8);
					String album_url = c.getString(9);
					
					MusicInfo musicInfo = new MusicInfo();  
					musicInfo.setAlbum(album);  
					musicInfo.setDuration(duration);  
					musicInfo.setSize(size);  
					musicInfo.setSinger(artist);  
					musicInfo.setUrl(url);
					//函数:getMusicName(String name)是指去掉歌曲名后面的.mp3
					musicInfo.setName(getMusicName(title));
					musicInfo.setId(music_id);
					musicInfo.setFavorite(favorite);
					musicInfo.setWordsUrl(word_url);
					musicInfo.setAlbumUrl(album_url);
					
					Bitmap albumArt = createAlbumArt(album_url);
					//判断歌曲是否有对于的专辑图片，没有时置空，防止专辑图片为空时出现错误，异常处理
					try {
						musicInfo.setThumbnail(albumArt);
					} catch (Exception e) {
						// TODO: handle exception
						musicInfo.setThumbnail(null);
					}
					//添加到list中
					//mymusiclist.add(musicInfo);
					//添加到list中
					if(favorite)
					{
						myfavmusiclist.add(musicInfo);
					}
				}while(c.moveToNext());
				
				myfavmusiclist = filledData(myfavmusiclist);
				// 根据a-z进行排序源数据
				Collections.sort(myfavmusiclist, pinyinComparator);
				PlayService.favmusiclist = myfavmusiclist;
				bufferlist = myfavmusiclist;
				GetDataService.isfirsthasgetfavdata = true;
				GetDataService.db.close();
			}
		}
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	//从数据库中获取音乐播放历史列表
	private void getHistoryListfromdb()
	{
		//启动服务，获取播放历史记录
		Intent historyintent = new Intent();
	  	historyintent.putExtra("type", "toget");
	  	historyintent.setClass(MusicActivity.this, GetHistoryService.class);  
	  	startService(historyintent);
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	//从数据库获取数据
    private void getDataFromHistorydb()
    {
    	if(GetHistoryService.historydb != null)
    	{
    		GetHistoryService.historydb.open();
    		Cursor c = GetHistoryService.historydb.getAllTitles();
    		if (c != null && c.moveToFirst())
    		{
    			List<MusicInfo> musiclist = new ArrayList<MusicInfo>();
    			do{
    				long music_id = c.getLong(0);
    				String title = c.getString(1);  
    				String artist = c.getString(2);  
    				String url = c.getString(3);  
    				long duration = c.getLong(4);  
    				long size = c.getLong(5);
    				String album = c.getString(6);  
    				boolean favorite = c.getInt(7) == 1 ? true : false;
    				String word_url = c.getString(8);
    				String album_url = c.getString(9);
    				
    				MusicInfo musicInfo = new MusicInfo();  
    				musicInfo.setAlbum(album);  
    				musicInfo.setDuration(duration);  
    				musicInfo.setSize(size);  
    				musicInfo.setSinger(artist);  
    				musicInfo.setUrl(url);
    				//函数:getMusicName(String name)是指去掉歌曲名后面的.mp3
    				musicInfo.setName(getMusicName(title));
    				musicInfo.setId(music_id);
    				musicInfo.setFavorite(favorite);
    				musicInfo.setWordsUrl(word_url);
    				musicInfo.setAlbumUrl(album_url);
    				//获取专辑封面（如果数据量大的话，会很耗时——需要考虑如何开辟子线程加载）
    				//url = /storage/emulated/0/Music/Download/Delacey - Dream It Possible/Dream It Possible.mp3
    				Bitmap albumArt = createAlbumArt(album_url);//convertToBitmap(album_url,40,40);
    				//判断歌曲是否有对于的专辑图片，没有时置空，防止专辑图片为空时出现错误，异常处理
    				try {
    					musicInfo.setThumbnail(albumArt);
    				} catch (Exception e) {
    					// TODO: handle exception
    					musicInfo.setThumbnail(null);
    				}
    				musiclist.add(musicInfo);
    			}while(c.moveToNext());
    			GetHistoryService.isfirsthasgethistorydata = true;
    		}
    		GetHistoryService.historydb.close();
    	}
    }
    //////////////////////////////////////////////////////////////////////////////////////////
    //onItemClick操作
	@Override
	public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
		// TODO Auto-generated method stub
		if(GetDataService.isgettingdata)
		{
			mHandler.sendEmptyMessage(6);
		}
		else
		{
			if(!isPlay)
			{
				btn_play_stop.setBackgroundResource(R.drawable.player_play_normal);
				isPlay = true;
			}
			firstclick = false;
			GetDataService.firstclick = false;
			//先判断当前音乐在所有音乐列表的哪个位置
			if(mymusiclist != null && mymusiclist.size() > 0)
			{
				if(list_type == 0)
				{
					clickindex = position;
				}
				else if(list_type == 1)
				{
					clickindex = getpositionInalllist(myfavmusiclist.get(position).getUrl());
					//getpositionInalllist(myfavmusiclist.get(position).getName(),myfavmusiclist.get(position).getId());
				}
				else if(list_type == 2)
				{
					clickindex = getpositionInalllist(myhistorymusiclist.get(position).getUrl());
					//getpositionInalllist(myhistorymusiclist.get(position).getName(),myhistorymusiclist.get(position).getId());
				}
			}
			
			minfo = mymusiclist.get(clickindex);
			name = minfo.getName();
			sname = minfo.getSinger();
			totaltime = MusicUtils.formatTime(minfo.getDuration());
			isfavorite = minfo.getFavorite();
			
			String str_current_name = name;
	    	if(str_current_name.contains("["))
			{
	    		str_current_name = str_current_name.replace("[", "=");
    			String name_str[] = str_current_name.split("=");
    			str_current_name = name_str[0];
				//str_current_name = str_current_name.split("[")[0];
			}
	    	if(str_current_name.contains(" - "))
	    	{
	    		String[] name_str = str_current_name.split(" - ");
	    		textview_music_name.setText(name_str[1]);
				textview_music_singername.setText(name_str[0]);
	    	}
	    	else
	    	{
	    		textview_music_name.setText(str_current_name);
	    		if(sname.equals("") || sname == null)
	    		{
	    			textview_music_singername.setText(R.string.unknown_singer);
	    		}
	    		else
	    		{
	    			textview_music_singername.setText(sname);
	    		}
	    	}
			
			iv_paly_music.setBackgroundResource(0);
			iv_paly_music.setImageBitmap(minfo.getThumbnail());
			if(minfo.getThumbnail() == null)
			{
				iv_paly_music.setBackgroundResource(R.drawable.default_album); 
			}
			Intent intent = new Intent();  
			intent.putExtra("url", minfo.getUrl());    //路径
			intent.putExtra("MSG", PLAY_MSG);          //播放状态
			intent.putExtra("listPosition", clickindex);//位置，哪一首
			intent.setClass(MusicActivity.this, PlayService.class);  
			startService(intent);       //启动服务
			
			if(PlayService.mediaPlayer != null)
			{
				if(!PlayService.mediaPlayer.isPlaying())
				{
					mLyricView.setVisibility(4);
					rl_show_music_info.setVisibility(0);
				}
				else
				{
					if(PlayService.LyricList != null && PlayService.LyricList.size() > 1)
					{
						//mLyricView.setSentenceEntities(PlayService.LyricList);//设置歌词资源，以显示
						mLyricView.setVisibility(4);
						rl_show_music_info.setVisibility(0);
					}
					else
					{
						mLyricView.setVisibility(4);
						rl_show_music_info.setVisibility(0);
					}
				}
			}
		}
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	//得到音乐名
	private String getMusicName(String name)
	{
		//在正则表达式中,特殊符号（"."、"|"、"^"等字符）
		//必须用 \ 来进行转义，而在java字符串中，\ 也是个已经被使用的特殊符号，也需要使用 \ 来转义。
		//有些歌曲名字中带有符号'.'
		return name.substring(0, name.length() - 4);
		//不能用以下方法的原因是：有些歌曲名带有"."，故不能用split来分割
		//但是所有歌曲名都包含有.mp3字符，因此使用substring方法就绝对能保证得到正确的歌曲名
		/*String[] arrstr = name.split("\\.");
		return arrstr[0];*/
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * @Description 获取专辑封面
	 * @param filePath 文件路径，like XXX/XXX/XX.mp3
	 * 如果图片太大，会导致bitmap装不下，所以解决办法为把图片大小设置为原图的一半
	 * @return 专辑封面bitmap
	 */
	public Bitmap createAlbumArt(final String filePath) {
	    Bitmap bitmap = null;
	    //以下屏蔽的代码使根据图片的绝对路径来获取的，但是不同的手机，专辑图片保存的路径不一样
	    /*String[] albumstr = filePath.split("/");
	    String albumfile = "";
	    try {
	    	albumfile = albumstr[0]+"/"+albumstr[1]+"/"+albumstr[2]+"/"+albumstr[3]+"/"+albumstr[4]+"/"+"Cover"+"/"+albumstr[6]+".jpg";
		} catch (Exception e) {
			// TODO: handle exception
			albumfile = "";
		}
		File file = new File(albumfile); 
		*/
	    File file = new File(filePath);
        if (file.exists()) { 
        	bitmap = BitmapFactory.decodeFile(filePath,getBitmapOption(2));//从本地中读取图片,解码文件  albumfile
        }
	    return bitmap;
	}
	//将图片的长和宽缩小味原来的1/2
	private Options getBitmapOption(int inSampleSize){
		System.gc();
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPurgeable = true;
		options.inSampleSize = inSampleSize;
		return options;
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	//第二种方法获取bitmap,但是这种方法获取到的图片比较模糊
	public Bitmap convertToBitmap(String path, int w, int h) 
	{
		Bitmap bitmap = null;
		if(!path.equals("nothisfile"))
		{
			BitmapFactory.Options opts = new BitmapFactory.Options();
			// 设置为ture只获取图片大小
			opts.inJustDecodeBounds = true;
			opts.inPreferredConfig = Bitmap.Config.ARGB_4444;
			// 返回为空
			BitmapFactory.decodeFile(path, opts);
			int width = opts.outWidth;
			int height = opts.outHeight;
			float scaleWidth = 0.f, scaleHeight = 0.f;
			if (width > w || height > h) {
				// 缩放
				scaleWidth = ((float) width) / w;
				scaleHeight = ((float) height) / h;
			}
			opts.inJustDecodeBounds = false;
			float scale = Math.max(scaleWidth, scaleHeight);
			opts.inSampleSize = (int)scale;
			WeakReference<Bitmap> weak = new WeakReference<Bitmap>(BitmapFactory.decodeFile(path, opts));
			bitmap = Bitmap.createScaledBitmap(weak.get(), w, h, true);
		}
		return bitmap;
	}
	//////////////////////////////////////////////////////////////////////////////////////////
    //长按删除操作
	@Override
	public boolean onItemLongClick(AdapterView<?> adapter, View view, final int position,long arg3) {
		// TODO Auto-generated method stub
		if(GetDataService.isgettingdata)
		{
			mHandler.sendEmptyMessage(6);
		}
		else
		{
			if(list_type == 0)
			{
				name = mymusiclist.get(position).getName();
				sname = mymusiclist.get(position).getSinger();
				del_url = mymusiclist.get(position).getUrl();
				del_id = mymusiclist.get(position).getId();
			}
			else if(list_type == 1)
			{
				name = myfavmusiclist.get(position).getName();
				sname = myfavmusiclist.get(position).getSinger();
				del_url = myfavmusiclist.get(position).getUrl();
				del_id = myfavmusiclist.get(position).getId();
			}
			else if(list_type == 2)
			{
				name = myhistorymusiclist.get(position).getName();
				sname = myhistorymusiclist.get(position).getSinger();
				del_url = myhistorymusiclist.get(position).getUrl();
				del_id = myhistorymusiclist.get(position).getId();
			}
			
			del_intent = new Intent();
			builder = new Builder(MusicActivity.this);
			
			common_del_position = position;
			
			delete_dialog = new CommonDialog(this, getResources().getString(R.string.del_music), 0);
			delete_dialog.setCancelable(false);
			delete_dialog.show();
			delete_dialog.setClicklistener(new CommonDialog.ClickListenerInterface() {
				@Override
				public void dodeletelocalmusic() {
					// TODO Auto-generated method stub
					delete_dialog.dismiss();
					//以下弹出的对话框是系统自带的对话框
					if(list_type == 0)
					{
						showDialogDeletelocalmusic(position,name);
					}
					else if(list_type == 1)
					{
						showDialogDeleteallfav();
					}
					else
					{
						showDialogDeleteallhistory();
					}
				}
				@Override
				public void dodeletemusic() {
					// TODO Auto-generated method stub
					delete_dialog.dismiss();
					//以下弹出的对话框是系统自带的对话框
					showDialogDeletemusic(position,name);
					
				}
				@Override
				public void docancel() {
					// TODO Auto-generated method stub
					delete_dialog.dismiss();
				}
			});
		}
		return true;  //返回true，防止与onItemClick冲突
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	//仅删除列表及自创建的数据库中对应的数据
	private void showDialogDeletemusic(final int del_position,String del_name)
	{
		 builder.setTitle("删除歌曲")  //设置对话框标题
	     .setMessage("你确定要删除 "+"“"+name+"”"+ "这首歌吗?")//设置显示的内容 
	     .setCancelable(false)       //点击空白处，对话框不自动退出，点击返回键也不会退出
	     .setPositiveButton("确定",new DialogInterface.OnClickListener() {//添加确定按钮  
	         @Override  
	         public void onClick(DialogInterface dialog, int which) {//确定按钮的响应事件  
	        	 // TODO Auto-generated method stub
	        	 if(list_type == 0)
	        	 {
	        		 mymusiclist.remove(del_position);
	        		 //更新列表
	        		 updatelist(del_position,name,minfo.getName());
	        		 dialog.dismiss();
	        		 //弹出提示
	        		 Toast.makeText(MusicActivity.this, "删除成功", Toast.LENGTH_LONG).show();
	        		 //由于数据库的操作比较耗时，所以需要放在线程中执行
		        	 threadHandler.post(delmusicdataThread);
	        	 }
	        	 //这是在喜爱列表进行删除操作，不需要删除数据库内容，只需要更新数据库内容及列表内容
	        	 else if(list_type == 1)
	        	 {
	        		//更新列表
	        		myfavmusiclist.remove(del_position);
	        		updateOffavmusic();
	        		dialog.dismiss();
	        		//弹出提示
	        		Toast.makeText(MusicActivity.this, "删除成功", Toast.LENGTH_LONG).show();
	        		//由于数据库的操作比较耗时，所以需要放在线程中执行
	        		threadHandler.post(delonefavdataThread);
	        	 }
	        	 else if(list_type == 2)
	        	 {
	        		 myhistorymusiclist.remove(del_position);
    				 updateOfhistorymusic();
	 	        	 dialog.dismiss();
	 	        	 //弹出提示
	   				 Toast.makeText(MusicActivity.this, "删除成功", Toast.LENGTH_LONG).show();
	        		//由于数据库的操作比较耗时，所以需要放在线程中执行
	    			threadHandler.post(delonehistorydataThread);
	        	 }
	         }  
	  
	     }).setNegativeButton("取消",new DialogInterface.OnClickListener() {//添加返回按钮  
	         @Override  
	         public void onClick(DialogInterface dialog, int which) {//响应事件  
	             // TODO Auto-generated method stub 
	        	 dialog.dismiss();
	         }  
	     })
	     .create()
	     .show();//在按键响应事件中显示此对话框
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	//删除本地local音乐文件
	private void showDialogDeletelocalmusic(final int del_position,String del_name)
	{
		 builder.setTitle("删除歌曲")  //设置对话框标题
	     .setMessage("你确定要删除 "+"“"+name+"”"+ "这首歌吗?")//设置显示的内容 
	     .setCancelable(false)       //点击空白处，对话框不自动退出，点击返回键也不会退出
	     .setPositiveButton("确定",new DialogInterface.OnClickListener() {//添加确定按钮  
	         @Override  
	         public void onClick(DialogInterface dialog, int which) {//确定按钮的响应事件  
	        	 // TODO Auto-generated method stub
	        	 mymusiclist.remove(del_position);
        		 //更新列表
        		 updatelist(del_position,name,minfo.getName());
        		 dialog.dismiss();
        		 //弹出提示
        		 Toast.makeText(MusicActivity.this, "删除成功", Toast.LENGTH_LONG).show();
        		 //由于数据库的操作比较耗时，所以需要放在线程中执行
        		 threadHandler.post(dellocaldataThread);
	         }  
	  
	     }).setNegativeButton("取消",new DialogInterface.OnClickListener() {//添加返回按钮  
	         @Override  
	         public void onClick(DialogInterface dialog, int which) {//响应事件  
	             // TODO Auto-generated method stub 
	        	 dialog.dismiss();
	         }  
	     })
	     .create()
	     .show();//在按键响应事件中显示此对话框
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	//删除所有喜爱音乐
	private void showDialogDeleteallfav()
	{
		 builder.setTitle("删除全部歌曲")  //设置对话框标题
	     .setMessage("你确定要删除整个列表的喜爱歌曲吗?")//设置显示的内容 
	     .setCancelable(false)       //点击空白处，对话框不自动退出，点击返回键也不会退出
	     .setPositiveButton("确定",new DialogInterface.OnClickListener() {//添加确定按钮  
	         @Override  
	         public void onClick(DialogInterface dialog, int which) {//确定按钮的响应事件  
	        	 // TODO Auto-generated method stub
	        	 //更新列表
	        	 bufferlist = myfavmusiclist;
	        	 if(GetDataService.db != null)
	        	 {
	        		 GetDataService.db.open();
	        		 //更新数据库对应位置内容
	        		 for(MusicInfo mfavinfo : bufferlist)
	        		 {
	        			 updatedb(getpositionInalllist(mfavinfo.getUrl()));
	        		 }
	        		 GetDataService.db.close();
	        	 }
	        	 if(GetHistoryService.historydb != null)
	        	 {
	        		 GetHistoryService.historydb.open();
	        		 for(MusicInfo mfavinfo : bufferlist)
	        		 {
	        			 int need_to_update_position = getpositionInhistorylist(mfavinfo.getUrl());
	        			 if(need_to_update_position != -1)
	        			 {
	        				 //更新播放历史记录数据库对应位置内容
	        				 updatehistorydb(need_to_update_position);
	        			 }
	        		 }
	        		 GetHistoryService.historydb.close();
	        	 }
	        	 //threadHandler.post(delallfavdataThread);
	        	 myfavmusiclist.removeAll(myfavmusiclist);
	        	 updateOffavmusic();
	        	 dialog.dismiss();
	        	 //弹出提示
	        	 Toast.makeText(MusicActivity.this, "已经全部删喜爱歌单，请到播放界面继续添加", Toast.LENGTH_LONG).show();
	        	 //由于数据库的操作比较耗时，所以需要放在线程中执行
	        	 getAllListfromdb();
	        	 mHandler.sendEmptyMessage(10);
	         }  
	  
	     }).setNegativeButton("取消",new DialogInterface.OnClickListener() {//添加返回按钮  
	         @Override  
	         public void onClick(DialogInterface dialog, int which) {//响应事件  
	             // TODO Auto-generated method stub 
	        	 dialog.dismiss();
	         }  
	     })
	     .create()
	     .show();//在按键响应事件中显示此对话框
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	//删除所有历史记录
	private void showDialogDeleteallhistory()
	{
		 builder.setTitle("删除全部播放记录")  //设置对话框标题
	     .setMessage("你确定要清空播放记录列表吗?")//设置显示的内容 
	     .setCancelable(false)       //点击空白处，对话框不自动退出，点击返回键也不会退出
	     .setPositiveButton("确定",new DialogInterface.OnClickListener() {//添加确定按钮  
	         @Override  
	         public void onClick(DialogInterface dialog, int which) {//确定按钮的响应事件  
	        	 // TODO Auto-generated method stub
	        	 //更新播放历史记录列表
	        	 myhistorymusiclist.removeAll(myhistorymusiclist);
	        	 updateOfhistorymusic();
	        	 dialog.dismiss();
	        	 //弹出提示
	        	 Toast.makeText(MusicActivity.this, "已清空播放记录列表", Toast.LENGTH_LONG).show();
   				//由于数据库的操作比较耗时，所以需要放在线程中执行
   				threadHandler.post(delallhistorydataThread);
	         }  
	  
	     }).setNegativeButton("取消",new DialogInterface.OnClickListener() {//添加返回按钮  
	         @Override  
	         public void onClick(DialogInterface dialog, int which) {//响应事件  
	             // TODO Auto-generated method stub 
	        	 dialog.dismiss();
	         }  
	     })
	     .create()
	     .show();//在按键响应事件中显示此对话框
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	//删除本地某一音乐的操作,由于比较耗时，因此放在GerMusicDataService中处理
	private void del_from_local(String url)
	{
		Intent intent = new Intent();
 		intent.putExtra(ACTION_TYPE, DEL_DATA);
 		intent.putExtra("del_url", url);
 		intent.setClass(MusicActivity.this, GetDataService.class);  
 		startService(intent);
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	//删除某一音乐后更新列表的操作
	private void updatelist(int position,String del_name,String current_name)
	{
		 musicListAdapter.notifyDataSetChanged(mymusiclist);
		 textview_nums.setText(""+mymusiclist.size());
		 
		 if(position < clickindex)
		 {
			 clickindex -= 1;
		 }
		 else if(position == clickindex)
		 {
			 clickindex = position;
			 if(clickindex > mymusiclist.size() - 1)
			 {
				 clickindex = 0;
			 } 
		 }
		 minfo = mymusiclist.get(clickindex);
		 updateViewsOfterDel();
		//如果删除歌曲的名字与播放歌曲名字相同，则继续保存播放状态，播放下一首
		 if(del_name.equals(current_name))
		 {
			 if(PlayService.mediaPlayer != null && PlayService.mediaPlayer.isPlaying())
	    	 {
	    		 btn_play_stop.setBackgroundResource(R.drawable.player_play_normal);
				 isPlay = true;
				 del_intent.putExtra("url", minfo.getUrl());
				 del_intent.putExtra("listPosition", clickindex);
				 del_intent.putExtra("MSG", PLAY_MSG);
				 del_intent.setClass(MusicActivity.this, PlayService.class);  
				 startService(del_intent);       //启动服务
	    	 }
	    	 else if(PlayService.mediaPlayer != null && !PlayService.mediaPlayer.isPlaying())
	    	 {
	    		 PlayService.current = clickindex;
	    		 btn_play_stop.setBackgroundResource(R.drawable.player_pause_normal);
				 isPlay = false;
				 firstclick = true;
				 GetDataService.firstclick = true;
	    	 } 
		 }
		 else
		 {
			 PlayService.current = clickindex;
			 if(PlayService.mediaPlayer != null && PlayService.mediaPlayer.isPlaying())
	    	 {
	    		 btn_play_stop.setBackgroundResource(R.drawable.player_play_normal);
				 isPlay = true;
	    	 }
	    	 else if(PlayService.mediaPlayer != null && !PlayService.mediaPlayer.isPlaying())
	    	 {
	    		 btn_play_stop.setBackgroundResource(R.drawable.player_pause_normal);
				 isPlay = false;
				 firstclick = true;
				 GetDataService.firstclick = true;
	    	 } 
		 }
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	//更新所有音乐对应的数据库
	private void updatedb(int position)
	{
		String del_name = mymusiclist.get(position).getName()+".mp3";
		String del_sname = mymusiclist.get(position).getSinger();
		long rowId = mymusiclist.get(position).getId();
		String url = mymusiclist.get(position).getUrl();
		long duration = mymusiclist.get(position).getDuration();
		long size = mymusiclist.get(position).getSize();
		String album = mymusiclist.get(position).getAlbum();
		String word_url = mymusiclist.get(position).getWordsUrl();
		String album_url = mymusiclist.get(position).getAlbumUrl();
		GetDataService.db.updateTitle(rowId, del_name, del_sname, url, duration, size, album, 0, word_url, album_url);
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	//更新历史记录数据库
	private void updatehistorydb(int position)
	{
		String del_name = myhistorymusiclist.get(position).getName()+".mp3";
		String del_sname = myhistorymusiclist.get(position).getSinger();
		long rowId = del_position_in_history;
		String url = myhistorymusiclist.get(position).getUrl();
		long duration = myhistorymusiclist.get(position).getDuration();
		long size = myhistorymusiclist.get(position).getSize();
		String album = myhistorymusiclist.get(position).getAlbum();
		String word_url = myhistorymusiclist.get(position).getWordsUrl();
		String album_url = myhistorymusiclist.get(position).getAlbumUrl();
		GetHistoryService.historydb.updateTitle(rowId, del_name, del_sname, url, duration, size, album, 0, word_url, album_url);
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	//根据列表id判断是否在喜爱列表中
	private boolean isFav(String deleteUrl)
	{
		boolean isFav = false;
		if(myfavmusiclist != null)
		{
			for(int i = 0;i<myfavmusiclist.size();i++)
			{
				if(myfavmusiclist.get(i).getUrl().equals(deleteUrl))
				{
					isFav = true;
					update_position = i;
					break;
				}
			}
		}
		return isFav;
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	//根据列表id判断是否在历史记录中
	private boolean ishistory(String deleteUrl)
	{
		boolean ishistory = false;
		if(myhistorymusiclist != null)
		{
			for(int i = 0;i<myhistorymusiclist.size();i++)
			{
				if(myhistorymusiclist.get(i).getUrl().equals(deleteUrl))
				{
					ishistory = true;
					del_position_in_history = myhistorymusiclist.get(i).getId();
					update_position_in_history = i;
					break;
				}
			}
		}
		return ishistory;
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	//清除缓存的操作
	private void releaseImageViews() {
		releaseImageView(iv_paly_music);
	}
	private void releaseImageView(ImageView imageView) {
		Drawable d = imageView.getDrawable();
		if (d != null)
		{
			d.setCallback(null);
		}
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	//根据当前音乐的名字和id得到在所有音乐中的id
	private int getpositionInalllist(String tempName,long mId)
	{
		int temp_position = -1;
		for(int i = 0;i<mymusiclist.size();i++)
		{
			if(mymusiclist.get(i).getName().equals(tempName))
			{
				temp_position = i;
				break;
			}
		}
		if(temp_position == -1)  //说明根据名字判断拿不到，这时候需要根据music_id来判断
		{
			for(int i = 0;i<mymusiclist.size();i++)
			{
				if(mId == mymusiclist.get(i).getId())
				{
					temp_position = i;
					break;
				}
			}
		}
		return temp_position;
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	//根据音乐url得到当前音乐在所有音乐中的id
	private int getpositionInalllist(String del_current_url)
	{
		int temp_position = -1;
		for(int i = 0;i<mymusiclist.size();i++)
		{
			if(mymusiclist.get(i).getUrl().equals(del_current_url))
			{
				temp_position = i;
				break;
			}
		}
		return temp_position;
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	//根据音乐名以及音乐id得到当前音乐在历史记录音乐中的id
	private int getpositionInhistorylist(String tempName,long mId)
	{
		int temp_position = -1;
		if(myhistorymusiclist != null)
		{
			for(int i = 0;i<myhistorymusiclist.size();i++)
			{
				if(myhistorymusiclist.get(i).getName().equals(tempName))
				{
					temp_position = i;
					break;
				}
			}
			if(temp_position == -1)  //说明根据名字判断拿不到，这时候需要根据music_id来判断
			{
				for(int i = 0;i<myhistorymusiclist.size();i++)
				{
					if(mId == myhistorymusiclist.get(i).getId())
					{
						temp_position = i;
						break;
					}
				}
			}
		}
		return temp_position;
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	//根据音乐的url得到当前音乐在历史记录中的id
	private int getpositionInhistorylist(String del_current_url)
	{
		int temp_position = -1;
		if(myhistorymusiclist != null)
		{
			for(int i = 0;i<myhistorymusiclist.size();i++)
			{
				if(del_current_url.equals(myhistorymusiclist.get(i).getUrl()))
				{
					del_position_in_history = myhistorymusiclist.get(i).getId();
					temp_position = i;
					break;
				}
			}
		}
		return temp_position;
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	//更新本地列表数据
	private void updateOflocalmusic()
	{
		if(mymusiclist != null && mymusiclist.size() > 0)
		{
			textview_no_data.setVisibility(4);
			textview_music_to_play.setVisibility(4);
			//notifyDataSetChanged方法强制listview调用getView来刷新每个Item的内容
			
			mymusiclist = filledData(mymusiclist);
			// 根据a-z进行排序源数据
			Collections.sort(mymusiclist, pinyinComparator);
			PlayService.mp3Infos = mymusiclist;
			
			musicListAdapter.notifyDataSetChanged(mymusiclist);
			textview_nums.setText(""+mymusiclist.size());
			
			//对点击每个item做出相应的反应，这可能会出现异常，数组越界
			minfo = mymusiclist.get(clickindex);
			name = minfo.getName();
			sname = minfo.getSinger();
			totaltime = MusicUtils.formatTime(minfo.getDuration());
			isfavorite = minfo.getFavorite();
			
			String str_current_name = name;
	    	if(str_current_name.contains("["))
			{
	    		str_current_name = str_current_name.replace("[", "=");
    			String name_str[] = str_current_name.split("=");
    			str_current_name = name_str[0];
				//str_current_name = str_current_name.split("[")[0];
			}
	    	if(str_current_name.contains(" - "))
	    	{
	    		String[] name_str = str_current_name.split(" - ");
	    		textview_music_name.setText(name_str[1]);
				textview_music_singername.setText(name_str[0]);
	    	}
	    	else
	    	{
	    		textview_music_name.setText(str_current_name);
	    		if(sname.equals("") || sname == null)
	    		{
	    			textview_music_singername.setText(R.string.unknown_singer);
	    		}
	    		else
	    		{
	    			textview_music_singername.setText(sname);
	    		}
	    	}
			
			iv_paly_music.setBackgroundResource(0);
			iv_paly_music.setImageBitmap(minfo.getThumbnail());
			if(minfo.getThumbnail() == null)
			{
				iv_paly_music.setBackgroundResource(R.drawable.default_album); 
			}
		}
		else
		{
			textview_no_data.setVisibility(0);
			textview_music_to_play.setVisibility(4);
			textview_no_data.setText(R.string.no_data);
			musicListAdapter.notifyDataSetChanged(null);
			textview_nums.setText("0");
		}
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	//更新喜爱列表数据
	private void updateOffavmusic()
	{
		if(myfavmusiclist != null && myfavmusiclist.size() > 0)
		{
			textview_no_data.setVisibility(4);
			//notifyDataSetChanged方法强制listview调用getView来刷新每个Item的内容
			myfavmusiclist = filledData(myfavmusiclist);
			// 根据a-z进行排序源数据
			Collections.sort(myfavmusiclist, pinyinComparator);
			PlayService.favmusiclist = myfavmusiclist;
			bufferlist = myfavmusiclist;
			
			musicListAdapter.notifyDataSetChanged(myfavmusiclist);
			if(PlayService.mp3Infos != null && PlayService.mp3Infos.size() > 0)
			{
				textview_nums.setText(""+PlayService.mp3Infos.size());
				//对点击每个item做出相应的反应，这可能会出现异常，数组越界
				minfo = PlayService.mp3Infos.get(PlayService.current);
			}
			
			name = minfo.getName();
			sname = minfo.getSinger();
			totaltime = MusicUtils.formatTime(minfo.getDuration());
			isfavorite = minfo.getFavorite();
			
			String str_current_name = name;
	    	if(str_current_name.contains("["))
			{
	    		str_current_name = str_current_name.replace("[", "=");
    			String name_str[] = str_current_name.split("=");
    			str_current_name = name_str[0];
				//str_current_name = str_current_name.split("[")[0];
			}
	    	if(str_current_name.contains(" - "))
	    	{
	    		String[] name_str = str_current_name.split(" - ");
	    		textview_music_name.setText(name_str[1]);
				textview_music_singername.setText(name_str[0]);
	    	}
	    	else
	    	{
	    		textview_music_name.setText(str_current_name);
	    		if(sname.equals("") || sname == null)
	    		{
	    			textview_music_singername.setText(R.string.unknown_singer);
	    		}
	    		else
	    		{
	    			textview_music_singername.setText(sname);
	    		}
	    	}
			
			iv_paly_music.setBackgroundResource(0);
			iv_paly_music.setImageBitmap(minfo.getThumbnail());
			if(minfo.getThumbnail() == null)
			{
				iv_paly_music.setBackgroundResource(R.drawable.default_album); 
			}
		}
		else
		{
			textview_no_data.setVisibility(0);
			textview_no_data.setText(R.string.no_fav_data);
			musicListAdapter.notifyDataSetChanged(null);
		}
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	//更新播放记录列表数据
	private void updateOfhistorymusic()
	{
		if(myhistorymusiclist != null && myhistorymusiclist.size() > 0)
		{
			textview_no_data.setVisibility(4);
			//notifyDataSetChanged方法强制listview调用getView来刷新每个Item的内容
			musicListAdapter.notifyDataSetChanged(myhistorymusiclist);
			if(PlayService.mp3Infos != null && PlayService.mp3Infos.size() > 0)
			{
				textview_nums.setText(""+PlayService.mp3Infos.size());
				//对点击每个item做出相应的反应，这可能会出现异常，数组越界
				minfo = PlayService.mp3Infos.get(PlayService.current);
			}
			
			name = minfo.getName();
			sname = minfo.getSinger();
			totaltime = MusicUtils.formatTime(minfo.getDuration());
			isfavorite = minfo.getFavorite();
			
			String str_current_name = name;
	    	if(str_current_name.contains("["))
			{
	    		str_current_name = str_current_name.replace("[", "=");
    			String name_str[] = str_current_name.split("=");
    			str_current_name = name_str[0];
				//str_current_name = str_current_name.split("[")[0];
			}
	    	if(str_current_name.contains(" - "))
	    	{
	    		String[] name_str = str_current_name.split(" - ");
	    		textview_music_name.setText(name_str[1]);
				textview_music_singername.setText(name_str[0]);
	    	}
	    	else
	    	{
	    		textview_music_name.setText(str_current_name);
	    		if(sname.equals("") || sname == null)
	    		{
	    			textview_music_singername.setText(R.string.unknown_singer);
	    		}
	    		else
	    		{
	    			textview_music_singername.setText(sname);
	    		}
	    	}
			
			iv_paly_music.setBackgroundResource(0);
			iv_paly_music.setImageBitmap(minfo.getThumbnail());
			if(minfo.getThumbnail() == null)
			{
				iv_paly_music.setBackgroundResource(R.drawable.default_album); 
			}
		}
		else
		{
			textview_no_data.setVisibility(0);
			textview_no_data.setText(R.string.no_history_data);
			musicListAdapter.notifyDataSetChanged(null);
		}
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * 提取每首歌的首字母
	 * @param date
	 * @return
	 */
	private List<MusicInfo> filledData(List<MusicInfo> date){
		
		List<MusicInfo> mSortList = new ArrayList<MusicInfo>();
		for(int i = 0; i < date.size(); i++)
		{
			String sortStr = "";
	    	String str_name = date.get(i).getName();
			/*str_name = str_name.replace(" ", "");
			String[] name = str_name.split("-");
			try {
				sortStr = name[1];
			} catch (Exception e) {
				// TODO: handle exception
				//sortStr = name[0];
				sortStr = str_name;
			}*/
			
			if(str_name.contains(" - "))
	    	{
	    		String[] name = str_name.split(" - ");
	    		sortStr = name[1];
	    	}
	    	else
	    	{
	    		sortStr = str_name;
	    	}
			//汉字转换成拼音
			String pinyin = characterParser.getSelling(sortStr);
			String sortString = pinyin.substring(0, 1).toUpperCase();
			// 正则表达式，判断首字母是否是英文字母
			if(sortString.matches("[A-Z]")){
				date.get(i).setSortLetters(sortString.toUpperCase());
			}else{
				date.get(i).setSortLetters("#");
			}
			mSortList.add(date.get(i));
		}
		return mSortList;
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	// 显示Notification
	///////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean onTouch(View view, MotionEvent event) {
		// TODO Auto-generated method stub
		if(event.getAction() == MotionEvent.ACTION_DOWN)
		{
			switch(view.getId())
			{
			case R.id.music_local_resource:
				//显示所有音乐喜爱列表
        		showmusiclist();
				break;
				
			case R.id.music_favorite:
				//显示喜爱列表
        		showfavlist();
				break;
				
			case R.id.music_history:
				//显示历史记录列表
        		showhistorylist();
				break;
			case R.id.paly_music_album:
			case R.id.music_about_layout:
				//跳转到播放界面
        		to_play_activity();
				break;
				
			case R.id.play_on_music:
				//暂停或播放
				play_pause_music();
				break;
				
			case R.id.play_next_music:
				//下一首
				play_next_music();
				break;
				
			case R.id.play_previous_music:
				//上一首
				play_previous_music();
				break;
				
			case R.id.title_button_id:
				//直接跳到上一层界面
				//设置切换动画，从左边进入，右边退出，此方法必须放在startActivity或者finish()之后
				finish();  
				overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
				break;
				
			case R.id.page_title_more_info:
				//弹出选择背景对话框
				showbackground_dialog();
				break;
			}
			return false;
		}
		else
		{
			return true;
		}
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	//广播接收器，更新界面
	BroadcastReceiver updateReceiver = new BroadcastReceiver() {

        @Override  
        public void onReceive(Context context, Intent intent) {


            if(intent.getAction().equals(NotificationMsg.NOTIFICATION_MUSICACTIVITY_TO_UPDATE_LOCAL)){ 
            	if(list_type == 0)
            	{
            		mHandler.sendEmptyMessage(3);
            	}
            	else
            	{
                	getAllListfromdb();
            	}
            }
            
            if(intent.getAction().equals(NotificationMsg.NOTIFICATION_MUSICACTIVITY_TO_UPDATE_FAV))
            {
            	if(list_type == 1)
            	{
            		mHandler.sendEmptyMessage(9);
            	}
            	else
            	{
            		getFavListfromdb();
            	}
            	
            }
            
            if(intent.getAction().equals(NotificationMsg.NOTIFICATION_MUSICACTIVITY_TO_UPDATE_HISTORY))
            {
            	if(list_type == 2)
            	{
            		mHandler.sendEmptyMessage(10);
            	}
            	else
            	{
            		myhistorymusiclist = new ArrayList<MusicInfo>();
      	    	  if(GetHistoryService.musiclist != null)
      	    	  {
      	    		  myhistorymusiclist = GetHistoryService.musiclist;
      	    	  }
            	}
            	
            }
        }  
    };
	
    //////////////////////////////////////////////////////////////////////////////////////////
    /** 
     * 继承GestureListener，重写left和right方法 
     */  
    private class MyGestureListener extends GestureListener {  
        public MyGestureListener(Context context) {
            super(context);  
        }  
        @Override  
        public boolean left() {
        	Log.e("info", "------------------------------------1=====");
        	if(list_type == 0)
        	{
        		//显示喜爱列表
        		showfavlist();
        	}
        	else if(list_type == 1)
        	{
        		//显示历史记录列表
        		showhistorylist();
        	}
        	else if(list_type == 2)
        	{
        		//跳转到播放界面
        		to_play_activity();
        	}
            return super.left();  
        }  
  
        @Override  
        public boolean right() {
        	if(list_type == 0)
        	{
        		//跳转到Home界面
        		finish();  //直接跳到上一层界面
				//设置切换动画，从左边进入，右边退出，此方法必须放在startActivity或者finish()之后
				overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
        	}
        	else if(list_type == 1)
        	{
        		//显示音乐列表
        		showmusiclist();
        	}
        	else if(list_type == 2)
        	{
        		//显示喜爱列表
        		showfavlist();
        	}
            return super.right();  
        }  
    }
    
    ///////////////////////////////////////////////////////////////////////////////////////
    //显示喜爱列表的操作
    private void showfavlist()
    {
    	if(mymusiclist != null && mymusiclist.size() > 0)
		{
			if(GetDataService.isgettingdata)
			{
				mHandler.sendEmptyMessage(6);
			}
			else
			{
				PlayService.show_list_type = 1;
				list_type = PlayService.show_list_type;
				myfavmusiclist = new ArrayList<MusicInfo>();
				bufferlist = new ArrayList<MusicInfo>();
				textview_showlist_type_all.setBackgroundResource(R.drawable.xia);
				textview_showlist_type_fav.setBackgroundResource(R.drawable.xia1);
				textview_showlist_type_history.setBackgroundResource(R.drawable.xia);
				if(!GetDataService.isfirsthasgetfavdata)
				{
					mHandler.sendEmptyMessage(9);
				}
				else
				{
					
					mHandler.sendEmptyMessage(8);
				}
			}
		}
		else
		{
			mHandler.sendEmptyMessage(6);
		}
    }
    ///////////////////////////////////////////////////////////////////////////////////////
    //显示所有音乐列表的操作
    private void showmusiclist()
    {
    	PlayService.show_list_type = 0;
		list_type = PlayService.show_list_type;
		mymusiclist = new ArrayList<MusicInfo>();
		textview_showlist_type_all.setBackgroundResource(R.drawable.xia1);
		textview_showlist_type_fav.setBackgroundResource(R.drawable.xia);
		textview_showlist_type_history.setBackgroundResource(R.drawable.xia);
		if(!GetDataService.isfirsthasgetdata)
		{
			mHandler.sendEmptyMessage(6);
			mHandler.sendEmptyMessage(3);
		}
		else
		{
			mHandler.sendEmptyMessage(7);
			if(GetDataService.temp_numbers == GetDataService.numbers)
			{
				mHandler.sendEmptyMessage(5);
			}
		}
    }
    ///////////////////////////////////////////////////////////////////////////////////////
    //显示播放记录列表的操作
    private void showhistorylist()
    {
    	if(mymusiclist != null && mymusiclist.size() > 0)
		{
			if(GetDataService.isgettingdata)
			{
				mHandler.sendEmptyMessage(6);
			}
			else
			{
				PlayService.show_list_type = 2;
				list_type = PlayService.show_list_type;
				textview_showlist_type_all.setBackgroundResource(R.drawable.xia);
				textview_showlist_type_fav.setBackgroundResource(R.drawable.xia);
				textview_showlist_type_history.setBackgroundResource(R.drawable.xia1);
				mHandler.sendEmptyMessage(10);
			}
		}
		else
		{
			mHandler.sendEmptyMessage(6);
		}
    }
   /////////////////////////////////////////////////////////////////////////////////////
   //跳转到播放界面的操作
    private void to_play_activity()
    {
    	if(mymusiclist == null || mymusiclist.size() == 0)
		{
			Toast.makeText(MusicActivity.this,"暂无数据，请点击本地音乐添加歌曲！", Toast.LENGTH_SHORT).show();
		}
		else
		{
			if(GetDataService.isgettingdata)
			{
				mHandler.sendEmptyMessage(6);
			}
			else
			{
				intent = new Intent(MusicActivity.this,MusicPlay.class);
				intent.putExtra(MUSIC_NAME, name);
				intent.putExtra(MUSIC_SINGER_NAME, sname);
				intent.putExtra(MUSIC_TOTAL_TIME,totaltime);
				intent.putExtra(MUSIC_IS_PLAY, isPlay);
				intent.putExtra(MUSIC_ID, clickindex);
				intent.putExtra(IS_FIRST_CLICK,firstclick);
				intent.putExtra(IS_FAVORITE, isfavorite);
				intent.putExtra(IS_SHOW_WORDS, PlayService.showWords);
				startActivity(intent);
				//设置切换动画，从右边进入，左边退出，此方法必须放在startActivity或者finish()之后
				overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
			}
		}
    }
    //////////////////////////////////////////////////////////////////////////////////////////
    //暂停或恢复音乐播放的操作
    private void play_pause_music()
    {
    	if(mymusiclist == null || mymusiclist.size() == 0)
		{
			Toast.makeText(this,"暂无数据，请点击本地音乐添加歌曲！", Toast.LENGTH_SHORT).show();
		}
		else
		{
			if(GetDataService.isgettingdata)
			{
				mHandler.sendEmptyMessage(6);
			}
			else
			{
				Intent play_stop_intent = new Intent();  
				play_stop_intent.putExtra("url", minfo.getUrl());
				if(isPlay)
				{
					btn_play_stop.setBackgroundResource(R.drawable.player_pause_normal);
					isPlay = false;
					play_stop_intent.putExtra("MSG", PAUSE_MSG); 
				}
				else
				{
					Log.e("info", "------------------------------------123=====");
					btn_play_stop.setBackgroundResource(R.drawable.player_play_normal);
					isPlay = true;
					if(firstclick)
					{
						play_stop_intent.putExtra("MSG", PLAY_MSG);
						firstclick = false;
						GetDataService.firstclick = false;
					}
					else
					{
						Log.e("info", "------------------------------------456=====");
						play_stop_intent.putExtra("MSG", CONTINUE_MSG);
					}
				}
				play_stop_intent.putExtra("listPosition", clickindex);
				play_stop_intent.setClass(MusicActivity.this, PlayService.class);  
				startService(play_stop_intent);       //启动服务
				
				if(PlayService.mediaPlayer != null)
				{
					if(PlayService.mediaPlayer.isPlaying())
					{
						mLyricView.setVisibility(4);
						rl_show_music_info.setVisibility(0);
					}
					else
					{
						if(PlayService.LyricList != null && PlayService.LyricList.size() > 1)
						{
							//mLyricView.setSentenceEntities(PlayService.LyricList);//设置歌词资源，以显示
							mLyricView.setVisibility(4);
							rl_show_music_info.setVisibility(0);
						}
						else
						{
							mLyricView.setVisibility(4);
							rl_show_music_info.setVisibility(0);
						}
					}
				}
			}
		}
    }
    /////////////////////////////////////////////////////////////////////////////////////////
    //显示选择背景对话框的操作
    private void showbackground_dialog()
    {
    	show_more_dialog = new CommonDialog(this, getResources().getString(R.string.background_setting), 1);
		show_more_dialog.setCancelable(false);
		show_more_dialog.show();
		show_more_dialog.setuserClicklistener(new CommonDialog.UserClickListenerInterface() {
			@Override
			public void dobackgroundsettting() {
				// TODO Auto-generated method stub
				show_more_dialog.dismiss();
				//跳转到背景设置界面
				Intent intent = new Intent();
				intent.setClass(MusicActivity.this, BackgroundSettingActivity.class);
				startActivity(intent);
				//设置切换动画，从右边进入，左边退出，此方法必须放在startActivity或者finish()之后
				overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
			}
			@Override
			public void docancel() {
				// TODO Auto-generated method stub
				show_more_dialog.dismiss();
			}
		});
    }
    /////////////////////////////////////////////////////////////////////////////////////////
    //显示下一首歌的操作
    private void play_next_music()
    {
    	if(mymusiclist == null || mymusiclist.size() == 0)
		{
			Toast.makeText(this,"暂无数据，请点击本地音乐添加歌曲！", Toast.LENGTH_SHORT).show();
		}
		else
		{
			if(GetDataService.isgettingdata)
			{
				mHandler.sendEmptyMessage(6);
			}
			else
			{
				firstclick = false;
				GetDataService.firstclick = false;
				play_next();
			}
		}
    }
    /////////////////////////////////////////////////////////////////////////////////////////
    //显示上一首歌的操作
    private void play_previous_music()
    {
    	if(mymusiclist == null || mymusiclist.size() == 0)
		{
			Toast.makeText(this,"暂无数据，请点击本地音乐添加歌曲！", Toast.LENGTH_SHORT).show();
		}
		else
		{
			if(GetDataService.isgettingdata)
			{
				mHandler.sendEmptyMessage(6);
			}
			else
			{
				firstclick = false;
				GetDataService.firstclick = false;
				play_previous();
			}
		}
    }
    ///////////////////////////////////////////////////////////////////////////////////////////
    //线程:在所有列表中，删除单个列表音乐
    Runnable delmusicdataThread= new Runnable() {
    	public void run() {
    		//判断当前删除的音乐是否在喜爱列表或者播放记录列表
     		if(isFav(del_url))
     		{
     			myfavmusiclist.remove(update_position);
     		}
     		
     		if(ishistory(del_url))
     		{
     			//历史记录数据库删除
     			if(GetHistoryService.historydb != null)
     			{
     				GetHistoryService.historydb.open();
     				GetHistoryService.historydb.deleteTitle(del_position_in_history);
     				GetHistoryService.historydb.close();
     			}
     			//更新播放历史记录列表
     			if(GetHistoryService.musiclist != null)
     			{
     				GetHistoryService.musiclist.remove(update_position_in_history);
     			}
     			mHandler.sendEmptyMessage(10);
     		}
     		//执行删除单个数据SQL语句
    		if(GetDataService.db != null)
    		{
    			GetDataService.db.open();
    			GetDataService.db.deleteTitle(del_id);
    			GetDataService.db.close();
    		}
    		getAllListfromdb();
    	}
    };
    ///////////////////////////////////////////////////////////////////////////////////////////
    //线程:删除所有喜爱列表数据
    Runnable delallfavdataThread= new Runnable() {
    	public void run() {
    		
    		//如果当前删除的音乐也在播放记录里，则需要更新播放记录数据库对应内容
    		if(GetHistoryService.historydb != null)
    		{
    			GetHistoryService.historydb.open();
    			for(MusicInfo mfavinfo : bufferlist)
    			{
    				int need_to_update_position = getpositionInhistorylist(mfavinfo.getUrl());
    				if(need_to_update_position != -1)
    				{
    					//更新播放历史记录数据库对应位置内容
    					updatehistorydb(need_to_update_position);
    				}
    			}
    			GetHistoryService.historydb.close();
    		}
    		getAllListfromdb();
    		mHandler.sendEmptyMessage(10);
    	}
    };
    ///////////////////////////////////////////////////////////////////////////////////////////
    //线程:删除单个喜爱数据
    Runnable delonefavdataThread= new Runnable() {
    	public void run() {
    		//删除的时候需要判断当前删除的音乐是否在播放列表中也存在，如果存在，需要更新，同时需要更新所有列表中对应的数据
    		mHandler.sendEmptyMessage(10);
    		 if(GetDataService.db != null)
     		{
    			GetDataService.db.open();
     			//更新数据库对应位置内容
     			updatedb(getpositionInalllist(del_url));
     			GetDataService.db.close();
     		}
     		//如果当前删除的音乐也在播放记录里，则需要更新播放记录数据库对应内容
     		if(GetHistoryService.historydb != null)
     		{
     			GetHistoryService.historydb.open();
     			int need_to_update_position = getpositionInhistorylist(del_url);
     			if(need_to_update_position != -1)
     			{
     				//更新播放历史记录数据库对应位置内容
     				updatehistorydb(need_to_update_position);
     			}
     			GetHistoryService.historydb.close();
     		}
     		//更新之后重新拿数据
     		getAllListfromdb();
     		mHandler.sendEmptyMessage(10);
    	}
    };
    ///////////////////////////////////////////////////////////////////////////////////////////
    //线程:删除单个播放记录数据
    Runnable delonehistorydataThread= new Runnable() {
    	public void run() {
    		
    		if(GetHistoryService.historydb != null)
    		{
    			GetHistoryService.historydb.open();
    			GetHistoryService.historydb.deleteTitle(del_id);
    			GetHistoryService.historydb.close();
    		}
			mHandler.sendEmptyMessage(10);
    	}
    };
    ///////////////////////////////////////////////////////////////////////////////////////////
    //线程:所有历史记录数据
    Runnable delallhistorydataThread= new Runnable() {
    	public void run() {
    		
    		if(GetHistoryService.historydb != null)
    		{
    			GetHistoryService.historydb.open();
    			GetHistoryService.historydb.deleteTitle();
    			GetHistoryService.historydb.close();
    		}
    	}
    };
    ///////////////////////////////////////////////////////////////////////////////////////////
    //线程:删除单个本地音乐数据
    Runnable dellocaldataThread= new Runnable() {
    	public void run() {
    		//判断当前删除的音乐是否在喜爱列表或者播放记录列表
     		if(isFav(del_url))
     		{
     			myfavmusiclist.remove(update_position);
     		}
     		
     		if(ishistory(del_url))
     		{
     			//历史记录数据库删除
     			if(GetHistoryService.historydb != null)
     			{
     				GetHistoryService.historydb.open();
     				GetHistoryService.historydb.deleteTitle(del_position_in_history);
     				GetHistoryService.historydb.close();
     			}
     			//更新播放历史记录列表
     			if(GetHistoryService.musiclist != null)
     			{
     				GetHistoryService.musiclist.remove(update_position_in_history);
     			}
     			mHandler.sendEmptyMessage(10);
     		}
     		//执行删除单个数据SQL语句
    		if(GetDataService.db != null)
    		{
    			GetDataService.db.open();
    			GetDataService.db.deleteTitle(del_id);
    			Toast.makeText(MusicActivity.this, "真的删除成功了", Toast.LENGTH_LONG).show();
    			GetDataService.db.close();
    		}
    		getAllListfromdb();
	        //这是在本地音乐列表进行删除操作
	        del_from_local(del_url);
    	}
    };
    ///////////////////////////////////////////////////////////////////////////////////////////
}
