package com.activity;


import java.util.Timer;
import java.util.TimerTask;

import com.activity.adapter.BackGroundAdapter;
import com.activity.adapter.MusicPlayModeAdapter;
import com.activity.dialog.CommonDialog;
import com.activity.info.MusicInfo;
import com.activity.message.NotificationMsg;
import com.activity.service.GetDataService;
import com.activity.service.PlayService;
import com.activity.utils.GestureListener;
import com.activity.utils.MusicUtils;
import com.activity.view.LyricView;
import com.activity.view.VisualizerView;
import com.example.liveplayer.R;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.Animation.AnimationListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class MusicPlay extends Activity implements OnClickListener, OnSeekBarChangeListener, 
													OnTouchListener, OnItemClickListener{

	private String name = "";
	private String sname = "";
	private String total_time = "";
	public static boolean isplay = false;
	private boolean firstclick = false;
	private boolean isshowwords = false;//专辑图片是否第一次点击
	public static int clickid = 0;
	public static boolean isfavorite = false;
	
	public static TextView tv_play_name;
	public static TextView tv_play_sname;
	private TextView tv_play_pre;
	private TextView tv_play_next;
	public static TextView tv_play_stop;
	public static TextView tv_play_current_time;
	public static TextView tv_play_total_time;
	private TextView tv_play_mode;
	private TextView tv_first_cicle_point;
	private TextView tv_second_cicle_point;
	public static TextView tv_music_play_favorite;
	private TextView tv_music_play_top_id;
	private TextView tv_music_play_more_info;
	public static ImageView iv_music_play_change;  
	public static SeekBar time_seekbar;
	public static LyricView mLyricView;
	private RelativeLayout rl_music_play;
	
	private LinearLayout ll_show_music_info;
	private TextView tv_music_info_name;
	private TextView tv_music_info_sname;
	private TextView tv_music_info_duration;
	private ImageView iv_music_info_album;
	private TextView tv_music_info_path;
	private TextView tv_music_info;
	private ImageView musicfavorite;
	public static VisualizerView musicvisualizer;// 均衡器视图
	
	private ListView lv_play_mode;
	
	private MusicInfo mminfo;
    private Context mcontext;
    public static Matrix matrix; //矩阵
    private Bitmap bitmap;
    private Bitmap round_bitmp;  //圆形图
    private Bitmap rotation_bitmap;//旋转图片
	
    private CommonDialog show_user_dialog;
	//定义播放模式,1、单曲循环  2、全部循环  3、顺序  4、随机播放
	private int play_type = 0;
	private int[] modes = {1,2,3,4};
	
	private MusicActivity mActivity;
	
	//定义一个播放模式图片数组
	private int[] images = {R.drawable.player_mode_repeat_one_normal,R.drawable.player_mode_repeat_all_normal,
							R.drawable.player_mode_normal_normal,R.drawable.player_mode_random_normal};
	
	private MusicPlayModeAdapter mMusicPlayModeAdapter;
	
	private AudioManager am;        //音频管理引用，提供对音频的控制  
	private LinearLayout ll_player_voice; //音量控制面板布局  
	private boolean clickvoice = false;
	private boolean clickinfo = false;
	private boolean clickmode = false;
	private int currentVolume;              //当前音量  
	private int maxVolume;                  //最大音量  
	private TextView tv_player_voice;  //显示音量控制面板的按钮  
	private SeekBar sb_player_voice;        //控制音量大小 
	private TextView tv_voice_add,tv_voice_reduce;
	//旋转图片
	private Animation operatingAnim = null;  //动画操作
	
	//用于开辟线程的Handler
	private Handler threadHandler = new Handler();
	
	private Handler myHandler = new Handler()
	{
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch(msg.what)
			{
			case 1:
				Toast.makeText(MusicPlay.this,"取消喜爱成功！", Toast.LENGTH_SHORT).show();
				break;
			case 2:
				Toast.makeText(MusicPlay.this,"添加喜爱成功！", Toast.LENGTH_SHORT).show();
				break;
			case 3:
				Toast.makeText(MusicPlay.this,"abs ==== ", Toast.LENGTH_SHORT).show();
				NetworkInfo networkinfo = ((ConnectivityManager) MusicPlay.this.getSystemService("connectivity")).getActiveNetworkInfo();
		    	if (networkinfo != null)
		    	{
		    		if(networkinfo.isAvailable())
		    		{
		    			
		    		}
		    	}
		    	else
		    	{
		    		
		    	}
				break;
			}
			super.handleMessage(msg);
		}
		
	};
	//////////////////////////////////////////////////////////////////////////////////////////
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏
		// 启动后删除之前我们定义的通知   
        //NotificationManager notificationManager = (NotificationManager) this  
        //        .getSystemService(NOTIFICATION_SERVICE);   
        //notificationManager.cancel(0); 
		setContentView(R.layout.music_play);
		
		PlayService.showPlay = true;
		mcontext = getBaseContext();//
		matrix = new Matrix();
		clickvoice = false;
		clickinfo = false;
		clickmode = false;
		//获得系统音频管理服务对象  
		am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
		
		//初始化数据
		initData();
		//初始化控件
		initView();
		
		//监听事件
		initListener();
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		PlayService.showPlay = true;
		if(GetDataService.background_id == -1)
		{
			rl_music_play.setBackgroundResource(BackgroundSettingActivity.imgs[0]);
		}
		else
		{
			rl_music_play.setBackgroundResource(BackgroundSettingActivity.imgs[GetDataService.background_id]);
		}
	}

    @Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		//释放图片资源和动画资源，不然会内存泄露
		if (operatingAnim != null) 
		{  
			iv_music_play_change.clearAnimation(); 
		}
		releaseImageViews();
		//发送通知
		//if(PlayService.mediaPlayer != null)
		//{
		//	MusicActivity.showNotification(PlayService.name,PlayService.bitmap);  //
		//}
	}
    //////////////////////////////////////////////////////////////////////////////////////////
    //onKeyDown
	//响应系统返回键，同时传递相关值
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if(event.getAction() == KeyEvent.ACTION_DOWN)
		{
			switch(keyCode)
			{
			case KeyEvent.KEYCODE_BACK:
				//如果返回上一activity的时候，先把view隐藏再操作
				hideView();
				//避免从播放界面回到列表界面时，获取歌曲名，歌手名等信息执行两次，(在onCreat()与onResume()都有这些代码)
				MusicActivity.backfromplay = true;
				MusicActivity.clickindex = clickid;//同步
				GetDataService.firstclick = firstclick;
				finish();
				//设置切换动画，从左边进入，右边退出，此方法必须放在startActivity或者finish()之后
				overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
				break;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	//拿到intent中的数据
	private void initData()
	{
		Intent intent = getIntent();
		name = intent.getStringExtra(MusicActivity.MUSIC_NAME);
		sname = intent.getStringExtra(MusicActivity.MUSIC_SINGER_NAME);
		total_time = intent.getStringExtra(MusicActivity.MUSIC_TOTAL_TIME);
		isplay = intent.getBooleanExtra(MusicActivity.MUSIC_IS_PLAY, false);
		clickid = intent.getIntExtra(MusicActivity.MUSIC_ID, 0);
		//firstclick = intent.getBooleanExtra(MusicActivity.IS_FIRST_CLICK, false);
		firstclick = GetDataService.firstclick;
		isfavorite = intent.getBooleanExtra(MusicActivity.IS_FAVORITE, false);
		isshowwords = intent.getBooleanExtra(MusicActivity.IS_SHOW_WORDS, false);
		if(MusicActivity.mymusiclist != null && MusicActivity.mymusiclist.size() > 0)
		{
			mminfo = MusicActivity.mymusiclist.get(clickid);
		}
		//isfavorite = mminfo.getFavorite();
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	//初始化控件
	private void initView() {
		// TODO Auto-generated method stub
		tv_music_play_top_id = (TextView)findViewById(R.id.music_play_top_id);
		tv_music_play_more_info = (TextView)findViewById(R.id.music_play_top_more_info);
		iv_music_play_change = (ImageView)findViewById(R.id.music_play_change);
		
		tv_play_name = (TextView)findViewById(R.id.music_play_name);
		tv_play_sname = (TextView)findViewById(R.id.music_play_singer);
		tv_play_next = (TextView)findViewById(R.id.music_play_next);
		tv_play_pre = (TextView)findViewById(R.id.music_play_pre);
		tv_play_stop = (TextView)findViewById(R.id.music_play_playing);
		tv_play_current_time = (TextView)findViewById(R.id.music_start_time);
		tv_play_total_time = (TextView)findViewById(R.id.music_finish_time);
		tv_play_mode = (TextView)findViewById(R.id.music_play_mode);
		tv_music_play_favorite = (TextView)findViewById(R.id.music_play_favorite);
		tv_first_cicle_point = (TextView)findViewById(R.id.music_select_first);
		tv_second_cicle_point = (TextView)findViewById(R.id.music_select_second);
		musicfavorite = (ImageView) findViewById(R.id.music_play_iv_favorite);
		musicvisualizer = (VisualizerView) findViewById(R.id.music_play_visualizer);
		
		time_seekbar = (SeekBar)findViewById(R.id.music_play_seekBar);
		mLyricView = (LyricView)findViewById(R.id.LyricShow);
		
		rl_music_play = (RelativeLayout)findViewById(R.id.relative_music_play);
		
		tv_voice_add = (TextView)findViewById(R.id.voice_add);
		tv_voice_reduce = (TextView)findViewById(R.id.voice_reduce);
		tv_player_voice = (TextView)findViewById(R.id.music_play_voice);
		sb_player_voice = (SeekBar)findViewById(R.id.voice_seekBar);
		ll_player_voice = (LinearLayout)findViewById(R.id.voice_linearlayout);
		
		ll_show_music_info = (LinearLayout)findViewById(R.id.music_info_linearlayout);
		tv_music_info_name = (TextView)findViewById(R.id.music_info_song_name);
		tv_music_info_sname = (TextView)findViewById(R.id.music_info_singer_name);
		tv_music_info_duration = (TextView)findViewById(R.id.music_info_song_duration);
		iv_music_info_album = (ImageView)findViewById(R.id.music_info_song_album);
		tv_music_info_path = (TextView)findViewById(R.id.music_info_song_path);
		tv_music_info = (TextView)findViewById(R.id.music_info);
		lv_play_mode = (ListView)findViewById(R.id.play_mode_list);
		
		lv_play_mode.setVisibility(4);
		ll_show_music_info.setVisibility(4);
		ll_player_voice.setVisibility(4);
		
		musicfavorite.bringToFront();
		
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
    		tv_play_name.setText(name_str[1]);
    		tv_play_sname.setText(name_str[0]);
    	}
    	else
    	{
    		tv_play_name.setText(str_current_name);
    		if(sname.equals("") || sname == null)
    		{
    			tv_play_sname.setText(R.string.unknown_singer);
    		}
    		else
    		{
    			tv_play_sname.setText(sname);
    		}
    	}
		
		tv_play_total_time.setText(total_time);
		
		//专辑图片显示，对图片进行剪辑，原图--正方形--圆型图片，让图片旋转
		if(mminfo.getThumbnail() == null)
		{
			//iv_music_play_change.setBackgroundResource(R.drawable.default_album);
			bitmap = ((BitmapDrawable)mcontext.getResources().getDrawable(R.drawable.default_album)).getBitmap();
			round_bitmp = WelcomeActivity.makeRoundCorner(WelcomeActivity.makeRoundCorner(bitmap),50);
		}
		else
		{
			//iv_music_play_change.setImageBitmap();
			bitmap = mminfo.getThumbnail();
			round_bitmp = WelcomeActivity.makeRoundCorner(WelcomeActivity.makeRoundCorner(bitmap),50);
		}
		iv_music_play_change.setImageBitmap(round_bitmp);  //round_bitmp
		
		//loadAnimation:载入动画
		operatingAnim = AnimationUtils.loadAnimation(this, R.anim.music_tip); 
		LinearInterpolator lin = new LinearInterpolator();  // LinearInterpolator为匀速效果，
		operatingAnim.setInterpolator(lin);//setInterpolator表示设置旋转速率
		if(isshowwords)
		{
			mLyricView.setKLOK(false);
			mLyricView.setVisibility(0);
			musicvisualizer.setVisibility(4);
			iv_music_play_change.setVisibility(4);
			tv_first_cicle_point.setBackgroundResource(R.drawable.btn_radio_off);
			tv_second_cicle_point.setBackgroundResource(R.drawable.btn_radio_on);
			if(PlayService.LyricList != null && PlayService.LyricList.size() > 0)
			{
				mLyricView.setSentenceEntities(PlayService.LyricList);//设置歌词资源，以显示
				if(operatingAnim != null)
				{
					iv_music_play_change.clearAnimation();
				}
			}
		}
		else
		{
			mLyricView.setVisibility(4);    //暂停显示歌词界面
			musicvisualizer.setVisibility(4);
			iv_music_play_change.setVisibility(0);
			tv_first_cicle_point.setBackgroundResource(R.drawable.btn_radio_on);
			tv_second_cicle_point.setBackgroundResource(R.drawable.btn_radio_off);
			if (operatingAnim != null) 
			{
				if(isplay)
				{
					iv_music_play_change.startAnimation(operatingAnim);  
				}
				else
				{
					iv_music_play_change.clearAnimation(); 
				}
			}
		}
		
		if(isplay)
		{
			tv_play_stop.setBackgroundResource(R.drawable.player_play_normal);
		}
		else
		{
			tv_play_stop.setBackgroundResource(R.drawable.player_pause_normal);
		}
		
		play_type = PlayService.status;//播放模式
		tv_play_mode.setBackgroundResource(images[play_type - 1]);
		
		if(isfavorite){
			tv_music_play_favorite.setBackgroundResource(R.drawable.player_favorite_star_normal);
			
		}else{
			tv_music_play_favorite.setBackgroundResource(R.drawable.player_favorite_normal);
		}
		
		if(GetDataService.background_id == -1)
		{
			rl_music_play.setBackgroundResource(BackgroundSettingActivity.imgs[0]);
		}
		else
		{
			rl_music_play.setBackgroundResource(BackgroundSettingActivity.imgs[GetDataService.background_id]);
		}
		
		//音量初始化
		//ll_player_voice.bringToFront();
		currentVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);  //获取当前值 
		maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		sb_player_voice.setProgress(currentVolume); 
		sb_player_voice.setMax(maxVolume);
		
		mMusicPlayModeAdapter = new MusicPlayModeAdapter(mcontext);
		lv_play_mode.setAdapter(mMusicPlayModeAdapter);
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	//注册控件的监听事件
	private void initListener()
	{
		//tv_play_next.setOnClickListener(this);
		//tv_play_pre.setOnClickListener(this);
		//tv_play_stop.setOnClickListener(this);
		//tv_music_play_top_id.setOnClickListener(this);
		//tv_music_play_more_info.setOnClickListener(this);
		//tv_play_mode.setOnClickListener(this);
		//tv_music_play_favorite.setOnClickListener(this);
		iv_music_play_change.setOnClickListener(this);
		mLyricView.setOnClickListener(this);
		
		//左右滑动切换listview内容
		iv_music_play_change.setLongClickable(true);
		mLyricView.setLongClickable(true);
		iv_music_play_change.setOnTouchListener(new MyGestureListener(this));
		mLyricView.setOnTouchListener(new MyGestureListener(this));
		//tv_player_voice.setOnClickListener(this);
		//tv_voice_add.setOnClickListener(this);
		//tv_voice_reduce.setOnClickListener(this);
		
		time_seekbar.setOnSeekBarChangeListener(this);
		sb_player_voice.setOnSeekBarChangeListener(this);
		
		tv_music_play_favorite.setOnTouchListener(this);
		tv_play_next.setOnTouchListener(this);
		tv_play_pre.setOnTouchListener(this);
		tv_play_stop.setOnTouchListener(this);
		tv_music_play_top_id.setOnTouchListener(this);
		tv_music_play_more_info.setOnTouchListener(this);
		tv_play_mode.setOnTouchListener(this);
		tv_music_info.setOnTouchListener(this);
		lv_play_mode.setOnItemClickListener(this);
		//iv_music_play_change.setOnTouchListener(this);
		//mLyricView.setOnTouchListener(this);
		tv_player_voice.setOnTouchListener(this);
		tv_voice_add.setOnTouchListener(this);
		tv_voice_reduce.setOnTouchListener(this);/**/
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	//onClick操作
	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		switch(view.getId()){
		case R.id.music_play_top_id:
			//如果返回上一activity的时候，先把view隐藏再操作
			hideView();
			//避免从播放界面回到列表界面时，获取歌曲名，歌手名等信息执行两次，(在onCreat()与onResume()都有这些代码)
			MusicActivity.backfromplay = true;
			MusicActivity.clickindex = clickid;//同步
			GetDataService.firstclick = firstclick;
			finish();
			//设置切换动画，从左边进入，右边退出，此方法必须放在startActivity或者finish()之后
			overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
			break;
		case R.id.music_play_top_more_info:
			//如果显示背景选择框的时候，先把view隐藏再操作
			hideView();
			//弹出背景选择框
			show_background_dialog();
			break;
		case R.id.music_play_change:
		case R.id.LyricShow:
			//如果显示歌词或者专辑图片的时候，先把view隐藏再操作
			hideView();
			//显示歌词或者专辑图片
			show_words_album();
			break;
		case R.id.music_play_mode:
			//显示播放模式
			show_play_mode();
			break;
		case R.id.music_play_next:
			//下一首
			next_play();
			break;
		case R.id.music_play_pre:
			//上一首
			previous_play();
			break;
		case R.id.music_play_voice:
			//显示音量增减布局
			show_voice();
			break;
		case R.id.voice_add:
			//增加音量
			add_voice();
			break;
		case R.id.voice_reduce:
			//减小音量
			reduce_voice();
			break;
			
		case R.id.music_play_playing:
			//如果点击暂停或者播放按钮的时候，先把view隐藏再操作
			hideView();
			//播放或者暂停
			play_pause();			
			break;
		case R.id.music_play_favorite:
			//如果添加喜爱音乐的时候，先把view隐藏再操作
			hideView();
			//添加喜爱
			addfav();
	        break;
		}
		
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	//下一首
	private void next_play()
	{
		Intent play_next_intent = new Intent();
		clickid ++;
		if(clickid >= MusicActivity.mymusiclist.size())
		{
			clickid = 0;
		}
		
		tv_play_stop.setBackgroundResource(R.drawable.player_play_normal);
		MusicActivity.isPlay = true;
		isplay = true;
		
		if(firstclick)
		{
			firstclick = false;
		}
		
		mminfo = MusicActivity.mymusiclist.get(clickid);//初始化     
		//更新界面
		updateViews();
		play_next_intent.putExtra("url", mminfo.getUrl());
		//更新TextView内容
		play_next_intent.putExtra("MSG", MusicActivity.NEXT_MSG);
		play_next_intent.putExtra("listPosition", clickid);
		play_next_intent.setClass(MusicPlay.this, PlayService.class);  
        startService(play_next_intent);
        
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	//上一首
	private void previous_play()
	{
		Intent play_previous_intent = new Intent();
		clickid --;
		if(clickid < 0)
		{
			clickid = (MusicActivity.mymusiclist.size() - 1);
		}
		//当点击上一曲时，总是处于播放状态
		tv_play_stop.setBackgroundResource(R.drawable.player_play_normal);
		MusicActivity.isPlay = true;
		isplay = true;
		if(firstclick)
		{
			firstclick = false;
		}
		
		mminfo = MusicActivity.mymusiclist.get(clickid);
		//更新界面
		updateViews();
		play_previous_intent.putExtra("url", mminfo.getUrl());
		play_previous_intent.putExtra("MSG", MusicActivity.PRIVIOUS_MSG);
		play_previous_intent.putExtra("listPosition", clickid);
		play_previous_intent.setClass(MusicPlay.this, PlayService.class);  
        startService(play_previous_intent);       //启动服务
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	//更新界面
	private void updateViews()
	{
		name = mminfo.getName();
		sname = mminfo.getSinger();
		total_time = MusicUtils.formatTime(mminfo.getDuration());
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
    		tv_play_name.setText(name_str[1]);
    		tv_play_sname.setText(name_str[0]);
    	}
    	else
    	{
    		tv_play_name.setText(str_current_name);
    		if(sname.equals("") || sname == null)
    		{
    			tv_play_sname.setText(R.string.unknown_singer);
    		}
    		else
    		{
    			tv_play_sname.setText(sname);
    		}
    	}
		tv_play_total_time.setText(total_time);
		
		//iv_music_play_change.setImageBitmap(mminfo.getThumbnail());		
		if(mminfo.getThumbnail() == null)
		{
			//iv_music_play_change.setBackgroundResource(R.drawable.default_album);
			bitmap = ((BitmapDrawable)mcontext.getResources().getDrawable(R.drawable.default_album)).getBitmap();
			//round_bitmp = WelcomeActivity.makeRoundCorner(WelcomeActivity.makeRoundCorner(bitmap),50);
		}
		else
		{
			//iv_music_play_change.setImageBitmap();
			bitmap = mminfo.getThumbnail();
			round_bitmp = WelcomeActivity.makeRoundCorner(WelcomeActivity.makeRoundCorner(bitmap),50);
		}
		iv_music_play_change.setImageBitmap(round_bitmp);  //放置图片
		
		//loadAnimation:载入动画
		operatingAnim = AnimationUtils.loadAnimation(this, R.anim.music_tip);  
		
		LinearInterpolator lin = new LinearInterpolator(); // LinearInterpolator为匀速效果，
		operatingAnim.setInterpolator(lin);//setInterpolator表示设置旋转速率
		
		if(iv_music_play_change.isShown())
		{
			iv_music_play_change.setVisibility(0);
			mLyricView.setVisibility(4);
			musicvisualizer.setVisibility(4);
			iv_music_play_change.startAnimation(operatingAnim);
		}
		else
		{
			iv_music_play_change.setVisibility(4);
			mLyricView.setVisibility(0);
			musicvisualizer.setVisibility(4);
			if(operatingAnim != null){
				iv_music_play_change.clearAnimation();
			}
		} 
		
		if(mminfo.getFavorite()){
			tv_music_play_favorite.setBackgroundResource(R.drawable.player_favorite_star_normal);
			isfavorite = true;	
		}
		else{
			tv_music_play_favorite.setBackgroundResource(R.drawable.player_favorite_normal);
			isfavorite = false;			
		}
		//每次更新歌曲时，更新歌词列，防止切歌时出现歌词未更新的情况
		if(PlayService.LyricList != null)
		{
			PlayService.LyricList.clear();
		}
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	//进度条操作
	//进度发生改变时调用
	@Override
	public void onProgressChanged(SeekBar seekbar, int position, boolean status) {
		// TODO Auto-generated method stub
		if(seekbar.getId() == R.id.music_play_seekBar)
		{
			if(mminfo != null)   //PlayService.mediaPlayer
			{
				long time = position * (mminfo.getDuration() / 100);  //PlayService.mediaPlayer
				String curtime = MusicUtils.formatTime(time);
				tv_play_current_time.setText(curtime);
				//hideView();
			}
		}
		
		if(seekbar.getId() == R.id.voice_seekBar)
		{
			am.setStreamVolume(AudioManager.STREAM_MUSIC, position, 0);
            currentVolume = position;  //获取当前值  
            sb_player_voice.setProgress(position);
		}
	}
	//开始拖动的时候调用
	@Override
	public void onStartTrackingTouch(SeekBar seekbar) {
		// TODO Auto-generated method stub
		if(seekbar.getId() == R.id.music_play_seekBar)
		{
			if(PlayService.mediaPlayer != null)
			{
				PlayService.mediaPlayer.pause();
				musicvisualizer.clearAnimation();
    			musicvisualizer.releaseVisualizerFx();
			}
		}
	}
	//停止拖动的时候调用
	@Override
	public void onStopTrackingTouch(SeekBar seekbar) {
		// TODO Auto-generated method stub
		if(seekbar.getId() == R.id.music_play_seekBar)
		{
			if(PlayService.mediaPlayer != null)
			{
				int dest = seekbar.getProgress();  
				int time = PlayService.mediaPlayer.getDuration();  
				int max = seekbar.getMax();  
				
				PlayService.mediaPlayer.seekTo(time*dest/max);
				
				if(isplay)
				{
					PlayService.mediaPlayer.start();
				}
			}
		}
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	private void releaseImageViews() {
		releaseImageView(iv_music_play_change);
	}

	private void releaseImageView(ImageView imageView) {
		Drawable d = imageView.getDrawable();
		if (d != null)
		{
			d.setCallback(null);
		}
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	//onTouch操作
	@Override
	public boolean onTouch(View view, MotionEvent event) {
		// TODO Auto-generated method stub
		if(event.getAction() == MotionEvent.ACTION_DOWN)
		{
			switch(view.getId()){
			case R.id.music_play_top_id:
				//如果返回上一个activity的时候，先把view隐藏再操作
				hideView();
				//避免从播放界面回到列表界面时，获取歌曲名，歌手名等信息执行两次，(在onCreat()与onResume()都有这些代码)
				MusicActivity.backfromplay = true;
				MusicActivity.clickindex = clickid;//同步
				GetDataService.firstclick = firstclick;
				finish();
				//设置切换动画，从左边进入，右边退出，此方法必须放在startActivity或者finish()之后
				overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
				break;
			case R.id.music_play_top_more_info:
				//如果显示背景选择框的时候，先把view隐藏再操作
				hideView();
				//弹出背景选择框
				show_background_dialog();
				break;
			case R.id.music_play_change:
			case R.id.LyricShow:
				//如果显示歌词或者专辑图片的时候，先把view隐藏再操作
				hideView();
				//显示歌词或者专辑图片
				show_words_album();
				break;
			case R.id.music_play_mode:
				//显示播放模式
				show_play_mode();
				break;
			case R.id.music_play_next:
				hideView();
				//下一首
				next_play();
				break;
			case R.id.music_play_pre:
				hideView();
				//上一首
				previous_play();
				break;
			case R.id.music_info:
				//显示音乐信息
				show_musicinfo();
				break;
			case R.id.music_play_voice:
				//显示音量增减布局
				show_voice();
				break;
			case R.id.voice_add:
				//增加音量
				add_voice();
				break;
			case R.id.voice_reduce:
				//减小音量
				reduce_voice();
				break;
				
			case R.id.music_play_playing:
				//如果点击暂停或者播放按钮的时候，先把view隐藏再操作
				hideView();
				//播放或者暂停
				play_pause();
				break;
			case R.id.music_play_favorite:
				//如果添加喜爱音乐的时候，先把view隐藏再操作
				hideView();
				//添加喜爱
				addfav();
				break;
			}
		}
		return false;
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	//显示背景选择框对话框
	private void show_background_dialog()
	{
		show_user_dialog = new CommonDialog(this, getResources().getString(R.string.user_info), 1);
		show_user_dialog.setCancelable(false);
		show_user_dialog.show();
		show_user_dialog.setuserClicklistener(new CommonDialog.UserClickListenerInterface() {
			@Override
			public void dobackgroundsettting() {
				// TODO Auto-generated method stub
				show_user_dialog.dismiss();
				//跳转到背景设置界面
				Intent intent = new Intent();
				intent.setClass(MusicPlay.this, BackgroundSettingActivity.class);
				startActivity(intent);
				//设置切换动画，从右边进入，左边退出，此方法必须放在startActivity或者finish()之后
				overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
			}
			@Override
			public void docancel() {
				// TODO Auto-generated method stub
				show_user_dialog.dismiss();
			}
		});
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	//显示歌词或者专辑图片
	private void show_words_album()
	{
		if(isshowwords)
		{
			isshowwords = false;
			PlayService.showWords = false;
			iv_music_play_change.setVisibility(0);
			mLyricView.setVisibility(4);
			musicvisualizer.setVisibility(4);
			tv_first_cicle_point.setBackgroundResource(R.drawable.btn_radio_on);
			tv_second_cicle_point.setBackgroundResource(R.drawable.btn_radio_off);
			if(operatingAnim != null && isplay == true )
			{
				iv_music_play_change.startAnimation(operatingAnim);
			}
		}
		else
		{
			if(PlayService.mediaPlayer == null)
			{
				isshowwords = false;
				PlayService.showWords = false;
			}
			else
			{
				isshowwords = true;
				PlayService.showWords = true;
				iv_music_play_change.setVisibility(4);
				mLyricView.setVisibility(0);
				musicvisualizer.setVisibility(4);
				if(PlayService.LyricList != null && PlayService.LyricList.size() > 0)
				{
					mLyricView.setSentenceEntities(PlayService.LyricList);//设置歌词资源，以显示
					tv_first_cicle_point.setBackgroundResource(R.drawable.btn_radio_off);
					tv_second_cicle_point.setBackgroundResource(R.drawable.btn_radio_on);
					if(operatingAnim != null)
					{
						iv_music_play_change.clearAnimation();
					}
				}
			}
		}
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	//显示播放模式
	private void show_play_mode()
	{
		if(clickvoice)
		{
			ll_player_voice.setVisibility(4);
			clickvoice = false;
		}
		if(clickinfo)
		{
			ll_show_music_info.setVisibility(4);
			clickinfo = false;
		}
		
		if(clickmode)
		{
			lv_play_mode.setVisibility(View.INVISIBLE);
			clickmode = false;
		}
		else
		{
			lv_play_mode.setVisibility(View.VISIBLE);
			lv_play_mode.bringToFront();
			clickmode = true;
		}
		/*play_type++;
		if(play_type > modes.length)
		{
			play_type = 1;
		}
		PlayService.status = modes[play_type-1];
		tv_play_mode.setBackgroundResource(images[play_type - 1]);*/
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	//显示音乐信息
	private void show_musicinfo()
	{
		if(clickvoice)
		{
			ll_player_voice.setVisibility(4);
			clickvoice = false;
		}
		if(clickmode)
		{
			lv_play_mode.setVisibility(4);
			clickmode = false;
		}
		
		if(clickinfo)
		{
			ll_show_music_info.setVisibility(View.INVISIBLE);
			clickinfo = false;
		}
		else
		{
			ll_show_music_info.setVisibility(View.VISIBLE);
			ll_show_music_info.bringToFront();
			clickinfo = true;
		}
		showMusicinfo();
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	//显示音量增减
	private void show_voice()
	{
		if(clickmode)
		{
			lv_play_mode.setVisibility(4);
			clickmode = false;
		}
		if(clickinfo)
		{
			ll_show_music_info.setVisibility(4);
			clickinfo = false;
		}
		
		if(clickvoice)
		{
			ll_player_voice.setVisibility(4);
			clickvoice = false;
		}
		else
		{
			ll_player_voice.setVisibility(0);
			ll_player_voice.bringToFront();
			clickvoice = true;
		}
		//初始化音量
		currentVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);  //获取当前值
		sb_player_voice.setProgress(currentVolume);
		//voice_timer = new Timer();
		//voice_timer.schedule(voice_task, 5000);
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	//增加音量操作
	private void add_voice()
	{
		currentVolume += 1;
		if(currentVolume > 100)
		{
			currentVolume = maxVolume;
		}
		sb_player_voice.setProgress(currentVolume);
		am.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, 0);
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	//减小音量操作
	private void reduce_voice()
	{
		currentVolume -= 1;
		if(currentVolume < 0)
		{
			currentVolume = 0;
		}
		sb_player_voice.setProgress(currentVolume);
		//把当前音量设置到系统
		am.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, 0);
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	//播放或者暂停操作
	private void play_pause()
	{
		Intent play_stop_intent = new Intent();  
		if(isplay)
		{
			tv_play_stop.setBackgroundResource(R.drawable.player_pause_normal);
			MusicActivity.isPlay = false;
			isplay = false;
			play_stop_intent.putExtra("MSG", MusicActivity.PAUSE_MSG); 
			
			if(iv_music_play_change.isShown())
			{
				iv_music_play_change.setVisibility(0);
				mLyricView.setVisibility(4);
				musicvisualizer.setVisibility(4);
			}
			else
			{
				iv_music_play_change.setVisibility(4);
				mLyricView.setVisibility(0);
				musicvisualizer.setVisibility(4);
			}
			if(operatingAnim != null){
				
				iv_music_play_change.clearAnimation();
			}
			/*// 设置旋转角度  
	        matrix.setRotate(time_seekbar.getProgress());  
	        // 重新绘制Bitmap  
	        rotation_bitmap = Bitmap.createBitmap(round_bitmp, 0, 0, round_bitmp.getWidth(),round_bitmp.getHeight(), matrix, true);
	        iv_music_play_change.setImageBitmap(rotation_bitmap);
	        */
	        
		}else{
			tv_play_stop.setBackgroundResource(R.drawable.player_play_normal);
			MusicActivity.isPlay = true;
			isplay = true;
			if(firstclick)
			{
				play_stop_intent.putExtra("MSG", MusicActivity.PLAY_MSG);
				firstclick = false;
			}
			else
			{
				play_stop_intent.putExtra("MSG", MusicActivity.CONTINUE_MSG);
			}
			
			if(iv_music_play_change.isShown())
			{
				iv_music_play_change.setVisibility(0);
				mLyricView.setVisibility(4);
				musicvisualizer.setVisibility(4);
				if(operatingAnim != null){
					iv_music_play_change.startAnimation(operatingAnim);
				}
			}
			else
			{
				iv_music_play_change.setVisibility(4);
				mLyricView.setVisibility(0);
				musicvisualizer.setVisibility(4);
				if(operatingAnim != null){
					iv_music_play_change.clearAnimation();
				}
			}
		}
		play_stop_intent.putExtra("url", mminfo.getUrl());
		play_stop_intent.putExtra("listPosition", clickid);
		play_stop_intent.setClass(MusicPlay.this, PlayService.class);  
        startService(play_stop_intent);       //启动服务
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	//添加喜爱操作
	private void addfav()
	{
		if(isfavorite)
		{
			tv_music_play_favorite.setBackgroundResource(R.drawable.player_favorite_normal);
			isfavorite = false;
			mminfo.setFavorite(false);
			//更新列表
			MusicActivity.mymusiclist.set(clickid, mminfo);
		}
		else
		{
			//线程启动动画
			//threadHandler.post(animationThread);
			tv_music_play_favorite.setBackgroundResource(R.drawable.player_favorite_star_normal);
			mminfo.setFavorite(true);
			isfavorite = true;
			//更新列表
			MusicActivity.mymusiclist.set(clickid, mminfo);
		}
		//数据库的访问是比较耗时，建议放在线程中处理数据库的更新
		threadHandler.post(updatadbThread);
		
		//这时候发一个广播通知MusicActivity更新界面
        Intent intent_to_activity = new Intent();
        intent_to_activity.putExtra("updatetype", NotificationMsg.NOTIFICATION_UPDATE_TYPE_FAV);
        intent_to_activity.setAction(NotificationMsg.NOTIFICATION_MUSICACTIVITY_TO_UPDATE_FAV);
        sendBroadcast(intent_to_activity);
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	//显示当前音乐信息
	private void showMusicinfo()
	{
		String current_music_name = mminfo.getName();
		String current_music_sname = mminfo.getSinger();
		
		if(current_music_name.contains(" - "))
    	{
    		String[] name = current_music_name.split(" - ");
    		tv_music_info_name.setText(name[1]);
    		tv_music_info_sname.setText(name[0]);
    	}
    	else
    	{
    		tv_music_info_name.setText(current_music_name);
    		if(mminfo.getSinger().equals("") || mminfo.getSinger() == null || mminfo.getSinger().equals("<unknown>"))
    		{
    			tv_music_info_sname.setText(R.string.unknown_singer);
    		}
    		else
    		{
    			tv_music_info_sname.setText(current_music_sname);
    		}
    	}
		
		tv_music_info_duration.setText(MusicUtils.formatTime(mminfo.getDuration()));
		tv_music_info_path.setText(mminfo.getUrl());
		iv_music_info_album.setImageBitmap(mminfo.getThumbnail()); 
    	if(mminfo.getThumbnail() == null)
    	{
    		iv_music_info_album.setBackgroundResource(R.drawable.default_album); 
    	}
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	//隐藏播放模式或者音乐信息
	private void hideView()
	{
		if(clickinfo)
		{
			ll_show_music_info.setVisibility(4);
			clickinfo = false;
		}
		if(clickvoice)
		{
			ll_player_voice.setVisibility(4);
			clickvoice = false;
		}
		if(clickmode)
		{
			lv_play_mode.setVisibility(4);
			clickmode = false;
		}
	}

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
			return super.left();  
		}  
		
		@Override  
		public boolean right() {
			hideView();
			//避免从播放界面回到列表界面时，获取歌曲名，歌手名等信息执行两次，(在onCreat()与onResume()都有这些代码)
			MusicActivity.backfromplay = true;
			MusicActivity.clickindex = clickid;//同步
			GetDataService.firstclick = firstclick;
			if(PlayService.showWords)
			{
				PlayService.showWords = false;
			}
			else
			{
				PlayService.showWords = true;
			}
			finish();
			//设置切换动画，从左边进入，右边退出，此方法必须放在startActivity或者finish()之后
			overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
			return super.right();  
		}  
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	//onItemClick操作
	@Override
	public void onItemClick(AdapterView<?> adapterview, View view, int position, long arg3) {
		// TODO Auto-generated method stub
		switch(adapterview.getId())
		{
		case R.id.play_mode_list:
			PlayService.status = position + 1;
			tv_play_mode.setBackgroundResource(images[position]);
			lv_play_mode.setVisibility(View.INVISIBLE);
			clickmode = false;
			break;
		}
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * 我的最爱图片动画
	 */
	private void startFavoriteImageAnimation() {
		AnimationSet animationset = new AnimationSet(false);

		ScaleAnimation scaleAnimation = new ScaleAnimation(0.0f, 1.0f, 0.0f,
				1.0f, Animation.RELATIVE_TO_SELF, 0.5f,
				Animation.RELATIVE_TO_SELF, 0.5f);
		scaleAnimation.setInterpolator(new OvershootInterpolator(5F));// 弹出再回来的动画的效果
		scaleAnimation.setDuration(600);
		AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
		alphaAnimation.setDuration(500);
		alphaAnimation.setStartOffset(700);

		animationset.addAnimation(scaleAnimation);
		animationset.addAnimation(alphaAnimation);
		animationset.setFillAfter(true);

		animationset.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
				musicfavorite.setVisibility(View.INVISIBLE);
			}
		});
		musicfavorite.setVisibility(View.VISIBLE);
		musicfavorite.startAnimation(animationset);
	}
	///////////////////////////////////////////////////////////////////////////////////////////
	//线程:更新数据库
    Runnable updatadbThread= new Runnable() {
		public void run() {
			
			if(GetDataService.db != null)
			{
				GetDataService.db.open();
				//更新数据库
				if(isfavorite)
				{
					if(GetDataService.db.updateTitle(mminfo.getId(), name+".mp3", sname, mminfo.getUrl(), 
							mminfo.getDuration(), mminfo.getSize(), mminfo.getAlbum(), 1,mminfo.getWordsUrl(),mminfo.getAlbumUrl()))
					{
						myHandler.sendEmptyMessage(2);
					}
				}
				else
				{
					if(GetDataService.db.updateTitle(mminfo.getId(), name+".mp3", sname, mminfo.getUrl(), 
						mminfo.getDuration(), mminfo.getSize(), mminfo.getAlbum(), 0,mminfo.getWordsUrl(),mminfo.getAlbumUrl()))
					{
						myHandler.sendEmptyMessage(1);
					}
				}
				GetDataService.db.close();
			}
		}
	};
	///////////////////////////////////////////////////////////////////////////////////////////
	//线程:更新数据库
	Runnable animationThread= new Runnable() {
		public void run() {
			startFavoriteImageAnimation();  // 执行动画
		}
	};

}
