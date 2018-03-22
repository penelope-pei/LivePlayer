package com.activity;

import com.activity.receiver.MyReceiver;
import com.activity.service.GetDataService;
import com.activity.service.PlayService;
import com.activity.utils.MusicUtils;
import com.activity.view.MyVideoView;
import com.example.liveplayer.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.VideoView;

public class VideoPlayActivity extends Activity implements OnClickListener, OnCompletionListener, OnSeekBarChangeListener, OnTouchListener, OnInfoListener{
	
	private static final String TAG = "VideoCustomActivity";
	private int control_play_showtimes = 0;
	private int control_voice_showtimes = 0;
	public static boolean isplay = false;
	public static boolean isPause = true;
	public static boolean isFirstClick = true;
	
	private TextView tv_back;
	public static VideoView vv_play;
	public static TextView tv_pause_play;
	private LinearLayout ll_play_control;
	public static TextView tv_control_pause_play;
	private TextView tv_control_fb_play;  //快退，fast backward
	private TextView tv_control_ff_play;//快进，fast forward
	private TextView tv_control_start_time;
	private TextView tv_control_all_time;
	private SeekBar seek_play_control;
	private RelativeLayout rl_all_view;
	
	private String video_play_name;
	private String video_play_path;
	private Long video_play_duration;
	private Bitmap video_play_bitmap;

	private int tv_voice_num = 16;  //测试，最大音量是60
	private TextView[] tv_allvoice;
	private LinearLayout ll_video_play_voice;
	private LinearLayout ll_video_play_all_view;
    private TextView tv_video_play_voice_add;
    private TextView tv_video_play_voice_reduce;
	private AudioManager am;        //音频管理引用，提供对音频的控制
	private int currentVolume;              //当前音量  
	private int maxVolume;                  //最大音量
	private int everynum;               //每个textview应该显示的量
	//private MediaPlayer mediaPlayer;
	
	// 添加来电监听事件相关 ,自定义广播接收器,主要用来当电话打进来时，暂停播放视频 
    private MyReceiver myReceiver;
	
	
	private Handler myHandler = new Handler()
	{

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch(msg.what)
			{
			case 1:
				if(ll_play_control != null && ll_play_control.isShown())  //5s后把控制栏去掉
				{
					control_play_showtimes = msg.arg1;
					if(control_play_showtimes > 5000)
					{
						control_play_showtimes = 0;
						ll_play_control.setVisibility(4);
					}
					if(vv_play != null)
					{
						int i = vv_play.getCurrentPosition() / ((int)vv_play.getDuration() / 100);
			            seek_play_control.setProgress(i);  //times / 1000 / 100
			   			tv_control_start_time.setText(MusicUtils.formatTime(vv_play.getCurrentPosition()));
					}
				}
				break;
			case 2:
				if(ll_video_play_voice != null && ll_video_play_voice.isShown())
				{
					control_voice_showtimes += 1000;
					if(control_voice_showtimes > 5000)
					{
						Log.e("info", "------------------------------------1=====");
						control_voice_showtimes = 0;
						ll_video_play_voice.setVisibility(4);
					}
				}
				myHandler.sendEmptyMessageDelayed(2, 1000);
				break;
			}
			Message message = new Message();
			message.what = 1;
			control_play_showtimes += 1000;
			message.arg1 = control_play_showtimes;
			myHandler.sendMessageDelayed(message, 1000);
			
			super.handleMessage(msg);
		}
		
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏
		//防止在进入有VideoView界面的Activity时会出现闪黑屏的情况
		getWindow().setFormat(PixelFormat.TRANSLUCENT);
		setContentView(R.layout.video_play_layout);
		
		//获取数据
		initdata();
		//初始化控件
		initView();
		//监听事件
		initListener();
		
		registerbroadcastreceiver();
	}
	//////////////////////////////////////////////////////////////////////////////////////////////
	//注册广播接收器
	private void registerbroadcastreceiver()
	{
		/** 
		 * 注册广播接收器：MyReceiver、MediaButtonBroadcaseReceiver、ButtonBroadcastReceiver
		 * MyReceiver:用于监听电话的来去电 以及耳机的插入拔出状态
		 * MediaButtonBroadcaseReceiver:用于接收并处理耳机按键操作
		 * ButtonBroadcastReceiver:用于接收并处理通知栏显示及按键的操作
		 * 
		 */
		myReceiver = new MyReceiver(this);
		
		IntentFilter phoneFilter = new IntentFilter();
		//为相应的容器对象添加动作，接收器根据接收到的不同的动作，做不同的处理
		phoneFilter.addAction("android.intent.action.PHONE_STATE");
		phoneFilter.addAction("android.intent.action.NEW_OUTGOING_CALL");
		phoneFilter.addAction("android.intent.action.HEADSET_PLUG");
		
		registerReceiver(myReceiver, phoneFilter);
	}
	////////////////////////////////////////////////////////////////////////////////////
	private void initdata()
	{
		isplay = false;
		isPause = true;
		isFirstClick = true;
		//control_play_showtimes = 0;
		//获得系统音频管理服务对象  
		am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
		maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		//根据最大音量，得到每个textview显示的量，默认最大值应该是100
		everynum = maxVolume / tv_voice_num;
		Log.e("info", "------------------------------------maxVolume====="+maxVolume);
		tv_allvoice = new TextView[tv_voice_num];
		
		//mediaPlayer = new MediaPlayer();
		Intent intent = getIntent();
		video_play_name = intent.getStringExtra(VideoActivity.VIDEO_NAME);
		video_play_duration = intent.getLongExtra(VideoActivity.VIDEO_DURATION, 0L);
		video_play_path = intent.getStringExtra(VideoActivity.VIDEO_PATH);
		//video_play_bitmap = getVideoThumb(video_play_path);   //我估计这里耗时间了
		video_play_bitmap = intent.getParcelableExtra(VideoActivity.VIDEO_ALBUM);
	}
	////////////////////////////////////////////////////////////////////////////////////
	private void initView()
	{
		vv_play = (VideoView)findViewById(R.id.vv_content);
		tv_back = (TextView)findViewById(R.id.tv_back);
		tv_pause_play = (TextView)findViewById(R.id.tv_pause_video_play);
		tv_control_all_time = (TextView)findViewById(R.id.video_all_time);
		tv_control_ff_play = (TextView)findViewById(R.id.video_ff_play);
		tv_control_pause_play = (TextView)findViewById(R.id.video_pause_play);
		tv_control_fb_play = (TextView)findViewById(R.id.video_fb_play);
		tv_control_start_time = (TextView)findViewById(R.id.video_start_time);
		seek_play_control = (SeekBar)findViewById(R.id.video_play_seek);
		ll_play_control = (LinearLayout)findViewById(R.id.video_play_control);
		ll_video_play_voice = (LinearLayout)findViewById(R.id.video_play_voice);
		ll_video_play_all_view = (LinearLayout)findViewById(R.id.video_show_all_view);
		tv_video_play_voice_add = (TextView)findViewById(R.id.video_show_add);
		tv_video_play_voice_reduce = (TextView)findViewById(R.id.video_show_reduce);
		rl_all_view = (RelativeLayout)findViewById(R.id.all_view_layout);
		
		BitmapDrawable bd = new BitmapDrawable(video_play_bitmap);
		//用这个试一下吧，之前就是用这个的
		vv_play.setBackgroundDrawable(bd);
		//vv_play.setBackgroundDrawable(bd);
		
		//rl_all_view.setVisibility(View.GONE);
		ll_video_play_voice.setVisibility(4);
		ll_play_control.setVisibility(4);
		tv_pause_play.setVisibility(0);
		ll_play_control.bringToFront();
		tv_pause_play.bringToFront();
		tv_back.bringToFront();
		seek_play_control.setProgress(0);
		tv_control_all_time.setText(MusicUtils.formatTime(video_play_duration));
		//往linearLayout里添加控件
		initvoicelayout();
	}
	////////////////////////////////////////////////////////////////////////////////////
	private void initListener()
	{
		tv_back.setOnClickListener(this);
		tv_pause_play.setOnClickListener(this);
		tv_control_fb_play.setOnClickListener(this);
		tv_control_ff_play.setOnClickListener(this);
		tv_control_pause_play.setOnClickListener(this);
		vv_play.setOnClickListener(this);
		vv_play.setOnTouchListener(this);
		tv_video_play_voice_add.setOnClickListener(this);
		tv_video_play_voice_reduce.setOnClickListener(this);
		seek_play_control.setOnSeekBarChangeListener(this);
		
		vv_play.setOnCompletionListener(this);
		vv_play.setOnInfoListener(this);
		//根据当前音量值初始化TextView数组的背景
		inittextviewbackground();
	}
	////////////////////////////////////////////////////////////////////////////////////
	private void initvoicelayout()
	{
		for(int i = 0;i<tv_voice_num;i++)
		{
			TextView child = new TextView(this);
			child.setId(tv_voice_num - i);  //设置id
			child.setWidth(28);
			child.setHeight(8);
			ll_video_play_all_view.addView(child,i);
			tv_allvoice[tv_voice_num - 1 - i] = child;    //往textview数组添加控件
			//设置监听事件
			//child.setOnClickListener(this);
			child.setOnTouchListener(this);
		}
	}
	///////////////////////////////////////////////////////////////////////////////////
	private void inittextviewbackground()
	{
		currentVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);  //获取当前值 
		//根据当前音量，判断应该多少个textview的背景显示为蓝色
		//在这里加上异常处理
		int v_num;
		try {
			v_num = currentVolume/everynum;  //得到当前音量占比个数
		} catch (Exception e) {
			// TODO: handle exception
			v_num = 0;
		}
		for(int i = 0;i<tv_voice_num;i++)
		{
			if(i < v_num)
			{
				tv_allvoice[i].setBackgroundResource(R.drawable.voice_selected);  //把textview背景设为蓝色，代表当前音量值
			}
			else
			{
				tv_allvoice[i].setBackgroundResource(R.drawable.voice_nomal); 
			}
		}
	}
	/////////////////////////////////////////////////////////////////////////////////
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		/* 设置路径 */
		vv_play.setVideoPath(video_play_path);
		/* 设置模式-播放进度条 */
		//vv_play.setMediaController(new MediaController(this));
		super.onStart();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(myReceiver);
		
	}
	//////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		int position = vv_play.getCurrentPosition();
		switch(view.getId())
		{
		case R.id.tv_back:
			vv_play.stopPlayback();
			Intent videointent = new Intent(VideoPlayActivity.this, VideoActivity.class);
			startActivity(videointent);
			finish();
			//设置切换动画，从左边进入，右边退出，此方法必须放在startActivity或者finish()之后
			overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
			break;
		case R.id.tv_pause_video_play:
			//vv_play.setZOrderOnTop(true);   //避免出现瞬间的透明
			tv_pause_play.setVisibility(4);
			if(!isplay)
			{
				if(isFirstClick)
				{
					isFirstClick = false;
					vv_play.setBackgroundResource(0);
					play(0);
				}
				else
				{
					resume();
				}
				tv_control_pause_play.setBackgroundResource(R.drawable.ktv_dynamic_pause_src);
			}
			break;
		case R.id.video_ff_play:
			position += 1000;
			if(position >= vv_play.getDuration())
			{
				position = vv_play.getDuration();
			}
			int ff = position / ((int)vv_play.getDuration() / 100);
            seek_play_control.setProgress(ff);  //times / 1000 / 100
   			tv_control_start_time.setText(MusicUtils.formatTime(position));
   			vv_play.seekTo(position);
			break;
		case R.id.video_pause_play:
			if(!isplay)
			{
				if(isPause)
				{
					resume();
				}
				else
				{
					play(0);
				}
				tv_pause_play.setVisibility(4);
				tv_control_pause_play.setBackgroundResource(R.drawable.ktv_dynamic_pause_src);
			}
			else
			{
				pause();
				tv_pause_play.setVisibility(0);
				tv_control_pause_play.setBackgroundResource(R.drawable.ktv_dynamic_play_src);
			}
			break;
		case R.id.video_fb_play:
			position -= 1000;
			if(position < 1000)
			{
				position = 0;
			}
			int rew = position / ((int)vv_play.getDuration() / 100);
            seek_play_control.setProgress(rew);  //times / 1000 / 100
   			tv_control_start_time.setText(MusicUtils.formatTime(position));
   			vv_play.seekTo(position);
			break;
		case R.id.vv_content:
			if(vv_play != null && vv_play.isPlaying())
			{
				Log.e("info", "------------------------------------0=====");
				control_play_showtimes = 0;
				ll_play_control.setVisibility(0);
				tv_control_pause_play.setBackgroundResource(R.drawable.ktv_dynamic_pause_src);
			}
			break;
		case R.id.video_show_add:
			currentVolume += everynum;
			if(currentVolume >= maxVolume)
			{
				currentVolume = maxVolume;
			}
			am.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, 0);
			inittextviewbackground();
			break;
		case R.id.video_show_reduce:
			currentVolume -= everynum;
			if(currentVolume <= 0)
			{
				currentVolume = 0;
			}
			am.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, 0);
			inittextviewbackground();
			break;
		case 1:
		case 2:
		case 3:
		case 4:
		case 5:
		case 6:
		case 7:
		case 8:
		case 9:
		case 10:
		case 11:
		case 12:
		case 13:
		case 14:
		case 15:
		case 16:
			currentVolume = view.getId() * everynum;
			am.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, 0);
			inittextviewbackground();
			break;
		}
	}
	////////////////////////////////////////////////////////////////////////////////////
	//根据路径来获取视频的bitmap
	private Bitmap getVideoThumb(String path) 
    {
        MediaMetadataRetriever media = new MediaMetadataRetriever();
    	media.setDataSource(path);
    	return media.getFrameAtTime();
    }
    
	/////////////////////////////////////////////////////////////////////////////////////
	// 播放完成后的操作
	public void onCompletion(MediaPlayer mp) {
		//播放结束后的动作 
		//跳转到播放界面
		Intent videointent = new Intent(VideoPlayActivity.this, VideoActivity.class);
		startActivity(videointent);
		finish();
		//设置切换动画，从左边进入，右边退出，此方法必须放在startActivity或者finish()之后
		overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	//进度条操作
	//进度发生改变时调用
	@Override
	public void onProgressChanged(SeekBar seekbar, int position, boolean status) {
		// TODO Auto-generated method stub
		if(seekbar.getId() == R.id.video_play_seek)
		{
			if(vv_play != null)   //
			{
				long time = position * (vv_play.getDuration() / 100);  //PlayService.mediaPlayer
				String curtime = MusicUtils.formatTime(time);
				tv_control_start_time.setText(curtime);
				//hideView();
			}
		}
		
	}
	//开始拖动的时候调用
	@Override
	public void onStartTrackingTouch(SeekBar seekbar) {
		// TODO Auto-generated method stub
		if(seekbar.getId() == R.id.video_play_seek)
		{
			if(vv_play != null)
			{
				vv_play.pause();
			}
		}
	}
	//停止拖动的时候调用
	@Override
	public void onStopTrackingTouch(SeekBar seekbar) {
		// TODO Auto-generated method stub
		if(seekbar.getId() == R.id.video_play_seek)
		{
			if(vv_play != null)
			{
				int dest = seekbar.getProgress();  
				int time = vv_play.getDuration();  
				int max = seekbar.getMax();  
				vv_play.seekTo(time*dest/max);
				if(isplay)
				{
					vv_play.start();
				}
			}
		}
	}
	//////////////////////////////////////////////////////////////////////////////////////////////
	/** 
	 * 播放音乐 
	 *  
	 * @param position 
	 */  
	private void play(int currentTime) 
	{  
		try {
			//mediaPlayer.reset();// 把各项参数恢复到初始状态  
			//mediaPlayer.setDataSource(video_play_path);  
			//mediaPlayer.prepare(); // 进行缓冲  
			//vv_play.setOnPreparedListener(new PreparedListener(currentTime));//播放前准备监听器
			vv_play.start(); // 开始播放  
			isplay = true;
			//实时更新数据
			myHandler.sendEmptyMessage(1);
		} catch (Exception e) {  
			e.printStackTrace();  
		}  
	}
	//////////////////////////////////////////////////////////////////////////////////////////////
	/** 
	*  
	* 实现一个OnPrepareLister接口,当音乐准备好的时候开始播放 
	*  
	*/  
	private final class PreparedListener implements OnPreparedListener 
	{  
		int current_time = 0;
		public PreparedListener(int currenttime) 
		{  
			this.current_time = currenttime;
		}  
		
		@Override  
		public void onPrepared(MediaPlayer mp) {  
			
			vv_play.start(); // 开始播放  
			isplay = true;
		}  
	} 
	//////////////////////////////////////////////////////////////////////////////////////////////
	/** 
	* 暂停音乐 
	*/  
	private void pause() 
	{  
		if (vv_play != null && vv_play.isPlaying()) 
		{  
			vv_play.pause();
			isplay = false;
			isPause = true;  
		}  
	}  
	//////////////////////////////////////////////////////////////////////////////////////////////
	/** 
	* 恢复播放音乐 
	*/ 
	private void resume() {  
		if (isPause) 
		{
			vv_play.start(); 
			isplay = true;
			isPause = false;  
		}  
	}
	////////////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean onTouch(View view, MotionEvent event) {
		// TODO Auto-generated method stub
		if(event.getAction() == MotionEvent.ACTION_DOWN)
		{
			
			switch(view.getId())
			{
			case R.id.vv_content:
				if(vv_play != null && vv_play.isPlaying())
				{
					control_play_showtimes = 0;
					ll_play_control.setVisibility(0);
					tv_control_pause_play.setBackgroundResource(R.drawable.ktv_dynamic_pause_src);
				}
			break;
			case R.id.video_show_add:
				currentVolume += everynum;
				if(currentVolume >= maxVolume)
				{
					currentVolume = maxVolume;
				}
				am.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, 0);
				inittextviewbackground();
				break;
			case R.id.video_show_reduce:
				currentVolume -= everynum;
				if(currentVolume <= 0)
				{
					currentVolume = 0;
				}
				am.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, 0);
				inittextviewbackground();
				break;
			case 1:
			case 2:
			case 3:
			case 4:
			case 5:
			case 6:
			case 7:
			case 8:
			case 9:
			case 10:
			case 11:
			case 12:
			case 13:
			case 14:
			case 15:
			case 16:
			case 17:
			case 18:
			case 19:
			case 20:
				currentVolume = view.getId() * everynum;
				am.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, 0);
				inittextviewbackground();
				break;
			}
		}
		return true;
	}
	//////////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean onInfo(MediaPlayer mp, int what, int extra) {
		// TODO Auto-generated method stub
		switch (what) {  
		//开始缓冲  
		case MediaPlayer.MEDIA_INFO_BUFFERING_START:  
			//rl_all_view.setVisibility(View.VISIBLE);  
			break;  
			//缓冲结束  
		case MediaPlayer.MEDIA_INFO_BUFFERING_END:  
			break;  
		}  
		return false;
	}
	/////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if(event.getAction() == KeyEvent.ACTION_DOWN)
		{
			switch(keyCode)
			{
			case KeyEvent.KEYCODE_VOLUME_DOWN:
			case KeyEvent.KEYCODE_VOLUME_UP:
				control_voice_showtimes = 0; //置为0是想让每次显示的时候就从0开始算起
				ll_video_play_voice.setVisibility(0);
				ll_video_play_voice.bringToFront();
				myHandler.sendEmptyMessage(2);
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		switch(keyCode)
		{
		case KeyEvent.KEYCODE_VOLUME_DOWN:
		case KeyEvent.KEYCODE_VOLUME_UP:
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}
	
}
