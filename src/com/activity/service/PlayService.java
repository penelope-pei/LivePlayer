package com.activity.service;

import java.util.ArrayList;
import java.util.List;

import com.activity.MusicActivity;
import com.activity.MusicPlay;
import com.activity.WelcomeActivity;
import com.activity.adapter.LrcHandle;
import com.activity.info.MusicInfo;
import com.activity.message.NotificationMsg;
import com.activity.receiver.MyReceiver;
import com.activity.utils.BaseTools;
import com.activity.utils.MusicUtils;
import com.example.liveplayer.R;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

public class PlayService extends Service implements OnCompletionListener{

	public static MediaPlayer mediaPlayer; // 媒体播放器对象  
    private String path;            // 音乐文件路径  
    private int msg;  
    public static boolean isPause;        // 暂停状态
    public static boolean isplaying;      //记录之前播放状态
    public static int current = 0;        // 记录当前正在播放的音乐 
    public static int pre_current = -1;  //记录上一次播放的音乐
    public static List<MusicInfo> mp3Infos;   //存放Mp3Info对象的集合
    public static List<MusicInfo> favmusiclist ;  //存放喜爱节目集合
    public static int status = 2;      //播放状态，默认为循环播放 ,1、单曲循环  2、全部循环  3、顺序  4、随机
    public static int show_list_type = 0;  //显示列表状态，0、所有音乐   1、喜爱列表   2、播放历史
    private int currentTime;        //当前播放进度  
    public static String name = "";       //音乐名
    private String sname = "";      //歌手名
    public static Bitmap bitmap = null;          //专辑封面
    private Bitmap bp_old = null;
    private Bitmap bp_new = null;  //处理过的圆形图
    private String ttime = "";      //歌曲总时长
    private Context context;
    public static int index=0;
    private boolean isfavorite = false;
    private LrcHandle mLrcHandle;
    public static List<String> LyricList = null;
    public static List<Long> lyric_timeslist = null;
    public static boolean showWords = false;  //是否显示歌词
    public static boolean showPlay = false;   //判断是否是在播放界面
    
    private long temp1 = 0L;
    private long temp2 = 0L;
    
    // 添加来电监听事件相关 ,自定义广播接收器,主要用来当电话打进来时，暂停播放音乐 
    private MyReceiver myReceiver;  
    //音频管理器
    private AudioManager mAudioManager; 
    private boolean mPausedByTransientLossOfFocus = false;     //是否暂时失去焦点
    private MediaButtonBroadcastReceiver mMediaButtonReceiver = null;  //耳机按键广播接收器
    //////////////////////////////////////////////////////////////////////////////////////////////
    private String INTENT_BUTTONID_TAG = "ButtonId";
	/** 上一首 按钮点击 ID */
	public final static int pre_id = 1;
	/** 播放/暂停 按钮点击 ID */
	public final static int play_id = 2;
	/** 下一首 按钮点击 ID */
	public final static int next_id = 3;
	/** Notification 的ID */
	private int notifyId = 101;
	/** NotificationCompat 构造器*/
	private NotificationCompat.Builder mBuilder;
	/** 通知栏按钮广播 */
	private ButtonBroadcastReceiver bReceiver;
	/** 通知栏按钮点击事件对应的ACTION */
	private final static String ACTION_BUTTON = "com.notifications.intent.action.ButtonClick";
	private Notification notification;
	//////////////////////////////////////////////////////////////////////////////////////////////
    /** 
     * handler用来接收消息，来发送广播更新播放时间 
     * //处理信息，进程间的通信
     */  
    private Handler handler = new Handler() {  
        public void handleMessage(android.os.Message msg) {
        	switch(msg.what)
        	{
        	case 1:
        		if(mediaPlayer != null)
                {            
                	//获取当前某一首歌播放到的位置,播放的时间
                	getCurrentPlaydata();
                    //在顺序播放模式下，播放完最后一首时，自动暂停，更新activity中的播放键状态
            		if(msg.arg1 == 1)                	
            		{
            			updatePlaystatus();
                	}
            		//当发现播放不同音乐时，启动HistoryService服务，把当前播放的音乐添加到历史记录数据库中
            		if(pre_current != current)
                    {
                    	starthistoryService();
                    }
            		//延时发送信息,每1000ms更新一次，1--msg.what，实时更新activity界面    
            		handler.sendEmptyMessageDelayed(1, 1000);
                }  
        		break;
        	case 2:
        		//当接收到action=ACTION_BUTTON意图时，发送一个广播，显示通知栏
        		Intent notifyintent = new Intent(ACTION_BUTTON);
            	sendBroadcast(notifyintent);
        		break;
        	case 3:
        		// 启动更新音乐可视化界面动画
        		if(MusicPlay.musicvisualizer != null)
        		{
        			//MusicPlay.musicvisualizer.setupVisualizerFx(mediaPlayer.getAudioSessionId());
        		}
				break;
        	case 4:
        		// 停止音乐可视化界面动画
        		if(MusicPlay.musicvisualizer != null)
        		{
        			//MusicPlay.musicvisualizer.clearAnimation();
        			//MusicPlay.musicvisualizer.releaseVisualizerFx();
        		}
        		break;
        	}
        };  
    };
    //////////////////////////////////////////////////////////////////////////////////////////////
    Handler myHandler=new Handler();
	//线程处理歌词
    Runnable mRunnable= new Runnable() {
		public void run() {
			
			if(showPlay)
			{
				if(MusicPlay.mLyricView != null)
				{
		    		MusicPlay.mLyricView.SetIndex(Index());//每句歌词的播放 
		    		MusicPlay.mLyricView.invalidate();   //刷新
				}
			}
			else
			{
				if(MusicActivity.mLyricView != null)
				{
					if(LyricList.size() == 1)  //说明没有歌词
					{
						MusicActivity.mLyricView.SetIndex(0);//每句歌词的播放  Index()
						if(MusicActivity.mLyricView != null && MusicActivity.rl_show_music_info != null)
						{
							MusicActivity.mLyricView.setVisibility(4);
							MusicActivity.rl_show_music_info.setVisibility(0);
						}
					}
					else
					{
						MusicActivity.mLyricView.SetIndex(MusicIndex(),mediaPlayer.getCurrentPosition(),temp1, temp2 );//每句歌词的播放  Index()
						if(mediaPlayer.isPlaying())
						{
							if(MusicActivity.mLyricView != null && MusicActivity.rl_show_music_info != null)
							{
								MusicActivity.mLyricView.setVisibility(4);
								MusicActivity.rl_show_music_info.setVisibility(0);
							}
						}
						else
						{
							if(MusicActivity.mLyricView != null && MusicActivity.rl_show_music_info != null)
							{
								MusicActivity.mLyricView.setVisibility(4);
								MusicActivity.rl_show_music_info.setVisibility(0);
							}
						}
					}
					MusicActivity.mLyricView.invalidate();   //刷新
				}
			}
			myHandler.postDelayed(mRunnable, 100); //100ms刷新一次
		}
	};
    //////////////////////////////////////////////////////////////////////////////////////////////
    //实时更新activity的view,放在服务里更新，主要是因为服务是在后台实时运行的，当然也可以通过发广播的方式，通知activity更新界面
    private void initActivityView()
    {
    	//界面：名称，时间，进度等
    	//更新MusicPlay中对应的控件
    	updateMusicPlay();
    	//更新MusicActivity中对应的控件
    	updateMusicActivity();
    }
    //////////////////////////////////////////////////////////////////////////////////////////////
    //更新播放界面的控件
    private void updateMusicPlay()
    {
    	if(MusicPlay.tv_play_name != null && MusicPlay.tv_play_sname != null)
		{
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
        		MusicPlay.tv_play_name.setText(name_str[1]);
        		MusicPlay.tv_play_sname.setText(name_str[0]);
        	}
        	else
        	{
        		MusicPlay.tv_play_name.setText(str_current_name);
        		if(sname.equals("") || sname == null)
        		{
        			MusicPlay.tv_play_sname.setText(R.string.unknown_singer);
        		}
        		else
        		{
        			MusicPlay.tv_play_sname.setText(sname);
        		}
        	}
		}
        
        if(MusicPlay.tv_music_play_favorite != null)
		{
        	if(isfavorite)
        	{
        		MusicPlay.tv_music_play_favorite.setBackgroundResource(R.drawable.player_favorite_star_normal);
    		}
        	else
        	{
    			MusicPlay.tv_music_play_favorite.setBackgroundResource(R.drawable.player_favorite_normal);
    		}
		}
        
		if(MusicPlay.tv_play_total_time != null)
		{
			MusicPlay.tv_play_total_time.setText(ttime);
		}
		if(MusicPlay.time_seekbar != null)
		{
			int i = currentTime / ((int)mp3Infos.get(current).getDuration() / 100);
            MusicPlay.time_seekbar.setProgress(i);  //times / 1000 / 100
		}
		
		if(MusicPlay.tv_play_current_time != null)
		{
			MusicPlay.tv_play_current_time.setText(MusicUtils.formatTime(currentTime));
		}
		
		if(MusicPlay.iv_music_play_change != null)
		{   
			//bitmap中的图片可能覆盖background
			if(bitmap == null)
			{
				//MusicPlay.iv_music_play_change.setBackgroundResource(R.drawable.default_album);
				bp_old = ((BitmapDrawable)context.getResources().getDrawable(R.drawable.default_album)).getBitmap();
				bp_new = WelcomeActivity.makeRoundCorner(WelcomeActivity.makeRoundCorner(bp_old),50);
			}
			else
			{
				//MusicPlay.iv_music_play_change.setImageBitmap();
				bp_old = bitmap;
				bp_new = WelcomeActivity.makeRoundCorner(WelcomeActivity.makeRoundCorner(bp_old),50);
			}
			MusicPlay.iv_music_play_change.setImageBitmap(bp_new); 
		}
		
		if(MusicPlay.musicvisualizer != null)
		{
			//MusicPlay.musicvisualizer.setupVisualizerFx(mediaPlayer.getAudioSessionId());
		}
    }
    //////////////////////////////////////////////////////////////////////////////////////////////
    //更新列表界面控件
    private void updateMusicActivity()
    {
    	if(MusicActivity.textview_music_name != null && MusicActivity.textview_music_singername != null)
		{
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
	    		MusicActivity.textview_music_name.setText(name_str[1]);
	    		MusicActivity.textview_music_singername.setText(name_str[0]);
	    	}
	    	else
	    	{
	    		MusicActivity.textview_music_name.setText(str_current_name);
	    		if(sname.equals("") || sname == null)
	    		{
	    			MusicActivity.textview_music_singername.setText(R.string.unknown_singer);
	    		}
	    		else
	    		{
	    			MusicActivity.textview_music_singername.setText(sname);
	    		}
	    	}
		}
		if(MusicActivity.iv_paly_music != null)
		{
			MusicActivity.iv_paly_music.setBackgroundResource(0);
			MusicActivity.iv_paly_music.setImageBitmap(bitmap);
			if(bitmap == null)
			{
				MusicActivity.iv_paly_music.setBackgroundResource(R.drawable.default_album); 
			}
		}
    }
    //////////////////////////////////////////////////////////////////////////////////////////////
    //获取当前播放音乐的信息
    private void getCurrentPlaydata()
    {
    	MusicPlay.clickid = current;
    	MusicActivity.clickindex = current;
    	currentTime = mediaPlayer.getCurrentPosition();
        name = mp3Infos.get(current).getName();
        sname = mp3Infos.get(current).getSinger();
        bitmap = mp3Infos.get(current).getThumbnail();
        ttime = MusicUtils.formatTime(mp3Infos.get(current).getDuration());
        isfavorite = mp3Infos.get(current).getFavorite();
        initActivityView();
    }
    //////////////////////////////////////////////////////////////////////////////////////////////
    //如果是顺序播放模式，则播放完最后一首歌的时候，暂停播放，更新播放键的状态
    private void updatePlaystatus()
    {
    	GetDataService.firstclick= true;
		if(MusicPlay.tv_play_stop != null)
		{
			MusicPlay.tv_play_stop.setBackgroundResource(R.drawable.player_pause_normal);
			MusicPlay.isplay = false;
		}
		if(MusicActivity.btn_play_stop != null)
		{
			MusicActivity.btn_play_stop.setBackgroundResource(R.drawable.player_pause_normal);
			MusicActivity.isPlay = false;
		}
    }
    //////////////////////////////////////////////////////////////////////////////////////////////
    //启动HistoryService服务，把当前播放的音乐添加到历史记录数据库中
    private void starthistoryService()
    {
    	GetHistoryService.isgettinghistorydata = true;
    	GetHistoryService.isfirsthasgethistorydata = false;
    	pre_current = current;
    	Intent historyintent = new Intent();
    	historyintent.putExtra("type", "toinsert");
    	/*Bundle bundle = new Bundle();
    	MusicInfo mymusicinfo = mp3Infos.get(current);
    	bundle.putSerializable("musicinfo", mymusicinfo);
    	historyintent.putExtras(bundle);*/
    	//上面是使用intent传递类对象，当然也可以把对象的每个属性传过去，就像以下代码
    	
    	historyintent.putExtra("music_name", name);
    	historyintent.putExtra("singer_name", sname);
    	historyintent.putExtra("fav", mp3Infos.get(current).getFavorite());
    	historyintent.putExtra("music_url", mp3Infos.get(current).getUrl());
    	historyintent.putExtra("album", mp3Infos.get(current).getAlbum());
    	historyintent.putExtra("duration", mp3Infos.get(current).getDuration());
    	historyintent.putExtra("size", mp3Infos.get(current).getSize());
    	historyintent.putExtra("album_url", mp3Infos.get(current).getAlbumUrl());
    	historyintent.putExtra("word_url", mp3Infos.get(current).getWordsUrl());
		historyintent.setClass(context, GetHistoryService.class);  
        startService(historyintent);       //启动服务
    }
    //////////////////////////////////////////////////////////////////////////////////////////////
    //onCreate，只有第一次启动该服务的时候才会执行onCreate，之后如果多次启动服务，不会执行onCreate，但会执行onStart
    //因此为了避免多次初始化数据,把数据的初始化放在onCreate中
    @Override  
    public void onCreate()
    {  
        super.onCreate();
        //获得音频管理器对象
        mAudioManager = (AudioManager)getSystemService(AUDIO_SERVICE);
        context = getBaseContext();
        showWords = false;
        mediaPlayer = new MediaPlayer();
        mLrcHandle = new LrcHandle();
        LyricList = new ArrayList<String>();
        lyric_timeslist = new ArrayList<Long>();
        
        //初始化mpsInfos和favmusiclist列表,方便在服务中直接调用，而不必要每次都是从activity中拿数据
        initmusiclist();
        /** 
         * 设置音乐播放完成时的监听器 
         */  
        mediaPlayer.setOnCompletionListener(this);
        //注册广播接收器
        registerbroadcastreceiver();
    } 
    //////////////////////////////////////////////////////////////////////////////////////////////
    //onCompletion，播放监听处理事件
    @Override
	public void onCompletion(MediaPlayer mp) {
		// TODO Auto-generated method stub
		if (status == 1)  // 单曲循环  
		{ 
            mediaPlayer.start();
            //获取音频焦点,并进行相应的处理，比如当语音聊天时，暂停播放音乐
            mAudioManager.requestAudioFocus(mAudioFocusListener,AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
            isplaying = true;
            //歌词处理
            LyricHandle();
            handler.sendEmptyMessage(1);
            //显示在通知栏上
            handler.sendEmptyMessage(2);
            //启动MusicPlay中的均衡器
            handler.sendEmptyMessage(3);
        } 
		else if (status == 2) // 全部循环
		{ 
            current++;  
            if(current > mp3Infos.size() - 1) 
            {  
            	//变为第一首的位置继续播放  
                current = 0;  
            }
            path = mp3Infos.get(current).getUrl(); 
            LyricHandle();     //歌词处理
            play(0);
            //显示在通知栏上
            handler.sendEmptyMessage(2);
        } 
		else if (status == 3) // 顺序播放  
		{ 
            current++;  //下一首位置  
            if (current <= mp3Infos.size() - 1) {  
                path = mp3Infos.get(current).getUrl(); 
                //歌词处理
                LyricHandle();     
                play(0);
                //显示在通知栏上
                handler.sendEmptyMessage(2);
            }
            else 
            {  
            	mediaPlayer.stop();
                mediaPlayer.seekTo(0);  //0--currentTime
                current = mp3Infos.size() - 1;//听到最后一首歌的位置
                LyricHandle();     //歌词处理
                Message msg = new Message();
                msg.what = 1;
                msg.arg1 = 1;
                handler.sendMessage(msg);
            }
        } 
		else if(status == 4) //随机播放  
		{    
			//获取随机数
            current = getRandomIndex(mp3Infos.size() - 1);  
            path = mp3Infos.get(current).getUrl();
            //歌词处理
            LyricHandle();     
            play(0);
            //显示在通知栏上
            handler.sendEmptyMessage(2);
        }
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
        myReceiver = new MyReceiver(context);
        mMediaButtonReceiver = new MediaButtonBroadcastReceiver();
        bReceiver = new ButtonBroadcastReceiver();
        
        IntentFilter phoneFilter = new IntentFilter();
        IntentFilter buttonFilter = new IntentFilter();
        IntentFilter intentFilter = new IntentFilter();
        //为相应的容器对象添加动作，接收器根据接收到的不同的动作，做不同的处理
        phoneFilter.addAction("android.intent.action.PHONE_STATE");
        phoneFilter.addAction("android.intent.action.NEW_OUTGOING_CALL");
        phoneFilter.addAction("android.intent.action.HEADSET_PLUG");
        buttonFilter.addAction("android.intent.action.MEDIA_BUTTON");
        buttonFilter.addAction("android.media.AUDIO_BECOMING_NOISY");
        intentFilter.addAction(ACTION_BUTTON);
        
        registerReceiver(myReceiver, phoneFilter);
        registerReceiver(mMediaButtonReceiver, buttonFilter);
		registerReceiver(bReceiver, intentFilter);
    }
    //////////////////////////////////////////////////////////////////////////////////////////////
    //初始化列表数据，从activity中获取
    private void initmusiclist()
    {
    	mp3Infos = new ArrayList<MusicInfo>();
        favmusiclist = new ArrayList<MusicInfo>();
        for(int i = 0;i  <MusicActivity.mymusiclist.size(); i++)
        {
        	MusicInfo minfo = MusicActivity.mymusiclist.get(i);
        	mp3Infos.add(minfo);
        }
        
        for(int i = 0;i  <MusicActivity.myfavmusiclist.size(); i++)
        {
        	MusicInfo minfo = MusicActivity.myfavmusiclist.get(i);
        	favmusiclist.add(minfo);
        }
    }
    //////////////////////////////////////////////////////////////////////////////////////////////
    /** 
     * 获取随机位置 
     * @param end 
     * @return 
     */  
    protected int getRandomIndex(int end) {  
        int index = (int) (Math.random() * end);  
        return index;  
    }
    //////////////////////////////////////////////////////////////////////////////////////////////
    //绑定服务，自动执行
    @Override  
    public IBinder onBind(Intent arg0) {  
        return null;  
    }  
    //////////////////////////////////////////////////////////////////////////////////////////////
    //歌词处理操作
    private void LyricHandle()
    {
    	//重新获取列表对象
        mLrcHandle = new LrcHandle();
        LyricList = new ArrayList<String>();
        lyric_timeslist = new ArrayList<Long>();
        //获取歌词路径
        String wordfile = mp3Infos.get(current).getWordsUrl();
	    mLrcHandle.readLRC(wordfile);
	    //获得歌词列表
		LyricList = mLrcHandle.getWords();
		//获得歌词时间列表
		lyric_timeslist = mLrcHandle.getTime();
		//这时候拿到的lyric_timeslist列表中的时间的排序是混乱的，所以还需要进行重新排序
		lyric_timeslist = sort_time(lyric_timeslist);
		//分配给时间列表里时间显示为0的项，并得到最终的时间列表
		lyric_timeslist = LastTimeList(lyric_timeslist);
		//判断显示歌词的控件是否为空
		if(MusicPlay.mLyricView != null)
		{
			if(LyricList.size() > 0)
			{
				if(MusicPlay.mLyricView != null)
				{
					//为控件添加数据，并显示在开始位置
					MusicPlay.mLyricView.setSentenceEntities(LyricList);
					MusicPlay.mLyricView.SetIndex(0);  //Index()
				}
			}
		}
		
		//判断显示歌词的控件是否为空
		if(MusicActivity.mLyricView != null)
		{
			if(LyricList.size() > 0)
			{
				if(MusicActivity.mLyricView != null)
				{
					//为控件添加数据，并显示在开始位置
					MusicActivity.mLyricView.setSentenceEntities(LyricList);
					MusicActivity.mLyricView.SetIndex(0);  //Index()
				}
			}
		}
		//通过Handler开启歌词处理线程mRunnable，歌词的处理比较耗时，并且要实时改变状态，因此必须放在线程里处理
		myHandler.post(mRunnable);
    }
    //////////////////////////////////////////////////////////////////////////////////////////////
    //onStart
    @Override  
    public void onStart(Intent intent, int startId) { 
    	
    	path = intent.getStringExtra("url");        //歌曲路径
    	current = intent.getIntExtra("listPosition", -1);   //当前播放歌曲的在mp3Infos的位置
    	name = mp3Infos.get(current).getName();    //当前歌曲名
    	sname = mp3Infos.get(current).getSinger();   //当前歌手名
    	//为了不出现死机现象，通过异常处理可以避免
    	try {
    		//获取消息处理id
    		msg = intent.getIntExtra("MSG", 0);         
        	if (msg == MusicActivity.PLAY_MSG)          //播放音乐
        	{     		
        		LyricHandle();     //歌词处理
        		play(0);  
            } 
        	else if (msg == MusicActivity.PAUSE_MSG)    //暂停
        	{    
                pause();      
            } 
        	else if (msg == MusicActivity.STOP_MSG)     //停止
        	{       
                stop();  
            } 
        	else if (msg == MusicActivity.CONTINUE_MSG) //继续播放 
        	{  
            	isPause = true;
                resume();     
            } 
        	else if (msg == MusicActivity.PRIVIOUS_MSG) //上一首  
        	{ 
            	LyricHandle();     //歌词处理
                previous();  
            } 
        	else if (msg == MusicActivity.NEXT_MSG)    //下一首
        	{       
            	LyricHandle();     //歌词处理
                next();  
            } 
        	else if (msg == MusicActivity.PROGRESS_CHANGE) //进度更新
        	{    
                currentTime = intent.getIntExtra("progress", -1);  
                play(currentTime);
            } 
        	else if (msg == MusicActivity.PLAYING_MSG)    //正在播放
        	{
                handler.sendEmptyMessage(1);
                handler.sendEmptyMessage(2);
            }
            else if(msg == MusicActivity.SHOW_NOTIFICATION)  //显示通知栏
            {
            	handler.sendEmptyMessage(2);
            }
		} catch (Exception e) {
			// TODO: handle exception
		}
    	
    	super.onStart(intent, startId);
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
        	mediaPlayer.reset();// 把各项参数恢复到初始状态  
            mediaPlayer.setDataSource(path);  
            mediaPlayer.prepare(); // 进行缓冲  
            mediaPlayer.setOnPreparedListener(new PreparedListener(currentTime));//播放前准备监听器
            //实时更新数据
            handler.sendEmptyMessage(1);
            //启动MusicPlay中的均衡器
            handler.sendEmptyMessage(3);
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
    }
    //////////////////////////////////////////////////////////////////////////////////////////////
    /** 
     * 暂停音乐 
     */  
    private void pause() 
    {  
        if (mediaPlayer != null && mediaPlayer.isPlaying()) 
        {  
            mediaPlayer.pause();
            //以下代码主要是为了防止如果使用耳机按键暂停时，Activity界面的播放暂停按钮没有发生改变
            if(MusicPlay.tv_play_stop != null)
    		{
				MusicPlay.tv_play_stop.setBackgroundResource(R.drawable.player_pause_normal);
    		}
			if(MusicActivity.btn_play_stop != null)
    		{
    			MusicActivity.btn_play_stop.setBackgroundResource(R.drawable.player_pause_normal);
    		}
			MusicActivity.isPlay = false;
			MusicPlay.isplay = false;
			GetDataService.firstclick = false;
			MusicActivity.mymusiclist = mp3Infos;
            isplaying = false;
            isPause = true;  
            //停止可视化视图动画
            handler.sendEmptyMessage(4);
        }  
    }  
    //////////////////////////////////////////////////////////////////////////////////////////////
    /** 
     * 恢复播放音乐 
     */ 
    private void resume() {  
        if (isPause) 
        {
            mediaPlayer.start(); 
            //获取音频焦点,并进行相应的处理，比如当语音聊天时，暂停播放音乐
            mAudioManager.requestAudioFocus(mAudioFocusListener,AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
            if(MusicPlay.tv_play_stop != null)
    		{
				MusicPlay.tv_play_stop.setBackgroundResource(R.drawable.player_play_normal);
    		}
			if(MusicActivity.btn_play_stop != null)
    		{
    			MusicActivity.btn_play_stop.setBackgroundResource(R.drawable.player_play_normal);
    		}
			MusicActivity.isPlay = true;
			MusicPlay.isplay = true;
            isplaying = true;
            isPause = false; 
            //启动MusicPlay中的均衡器
            handler.sendEmptyMessage(3);
        }  
    }  
    //////////////////////////////////////////////////////////////////////////////////////////////
    /** 
     * 上一首 
     */  
    private void previous() 
    {  
        play(0);  
    }  
    //////////////////////////////////////////////////////////////////////////////////////////////
    /** 
     * 下一首 
     */  
    private void next() 
    {  
        play(0);  
    }  
    //////////////////////////////////////////////////////////////////////////////////////////////
    /** 
     * 停止音乐 
     */  
    public static void stop() 
    {  
        if (mediaPlayer != null) 
        {  
            mediaPlayer.stop();  
            try {  
            	// 在调用stop后如果需要再次通过start进行播放,需要之前调用prepare函数  
                mediaPlayer.prepare(); 
            } catch (Exception e) {  
                e.printStackTrace();  
            }  
        }  
    }  
    //////////////////////////////////////////////////////////////////////////////////////////////
    @Override  
    public void onDestroy() {  
        if (mediaPlayer != null) 
        {  
            mediaPlayer.stop();   //停止播放器
            mediaPlayer.release();//释放  
            mediaPlayer = null;  
        } 
        //注销广播接收器
        if(myReceiver != null)
        {
        	unregisterReceiver(myReceiver);
        }
        if(mMediaButtonReceiver != null)
        {
        	unregisterReceiver(mMediaButtonReceiver);
        }
        if(bReceiver != null)
        {
        	unregisterReceiver(bReceiver);
        }
        //释放音频焦点
        mAudioManager.abandonAudioFocus(mAudioFocusListener);
        super.onDestroy();
          
    }  
    //////////////////////////////////////////////////////////////////////////////////////////////
    /** 
     *  
     * 实现一个OnPrepareLister接口,当音乐准备好的时候开始播放 
     *  
     */  
    private final class PreparedListener implements OnPreparedListener 
    {  
        private int currentTime;  
  
        public PreparedListener(int currentTime) 
        {  
            this.currentTime = currentTime;
        }  
  
        @Override  
        public void onPrepared(MediaPlayer mp) {  
        	
            mediaPlayer.start(); // 开始播放  
            isplaying = true;
            //获取音频焦点,并进行相应的处理，比如当语音聊天时，暂停播放音乐
            mAudioManager.requestAudioFocus(mAudioFocusListener,AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
            
            if (currentTime > 0) { // 如果音乐不是从头播放  
                mediaPlayer.seekTo(currentTime);  
            } 
        }  
    }  
    //////////////////////////////////////////////////////////////////////////////////////////////
    //对歌词时间进行排序,这种方法排序会比较耗时
    private List<Long> sort_time(List<Long> times_list)
    {
    	List<Long> list_times = times_list;
    	long temp_time = 0;
    	String temp_words = "";
    	
    	for(int i = 0;i<list_times.size();i++)
    	{
    		for(int j = i+1;j<list_times.size();j++)//歌词时间排序，歌词排序
    		{
				if(list_times.get(i) > list_times.get(j))
    			{
    				temp_time = list_times.get(i);
    				temp_words = LyricList.get(i);
    				list_times.set(i, list_times.get(j));
    				LyricList.set(i, LyricList.get(j));
    				list_times.set(j, temp_time);
    				LyricList.set(j, temp_words);
    			}
    		}
    	}
    	return list_times;
    }
    //////////////////////////////////////////////////////////////////////////////////////////////
    //对歌词时间进行排序,这种方法是快速排序法
    private List<Integer> quick_sort(List<Integer> pData, int left, int right) 
    {
    	int i = left;
    	int j = right;
    	int middle;
    	int strTemp;
    	
    	if (pData.size() <= 1) 
    	{
    		return pData;
    	}
    	middle = pData.get((left + right) / 2);
    	do {
    		while ((pData.get(i) < middle) && (i < right))
    		{
    			i++;
    		}
    		while ((pData.get(j) > middle) && (j > left))
    		{
    			j--;
    		}
    		
    		if (i <= j) 
    		{
    			strTemp = pData.get(i);
    			pData.set(i, pData.get(j));
    			pData.set(j, strTemp);
    			i++;
    			j--;
    		}
    	} while (i <= j);
    	if (left < j) 
    	{
    		quick_sort(pData, left, j);
    	}
    	if (right > i)
    	{
    		quick_sort(pData, i, right);
    	}
    	
    	return pData;
    }
    //////////////////////////////////////////////////////////////////////////////////////////////
    //此方法判断时间显示为0最后一次出现的位置，并返回时间列表
    private List<Long> LastTimeList(List<Long> data)
    {
	   long all_time = 0;
	   double show_time = 0.00;
	   int last_zero_index = 0;
  
	   try {
		   for(int k = 0;k<data.size();k++)
		   {
			   if(data.get(k) != 0L)
			   {
				   last_zero_index = k;
				   break;
			   }
		   }
		   
		   all_time = data.get(last_zero_index);
		   show_time = all_time / last_zero_index;//歌词前无时间的平均时间，如[ar:,,,]
		   //以下代码中的200,250数字是假设数据，并不是完全正确的数据；人为分配时间
		   for(int m = 0; m< last_zero_index;m++)
		   {
			   if(m != 0)
			   {
				   /*if((m+1)*250 < all_time)
				   {
					   data.set(m, (m+1)*250);
				   }
				   else
				   {
					   show_time = all_time - 100;
					   data.set(m, show_time);
				   }*/
				   data.set(m, Math.round(m * show_time)); //
			   }
			   else
			   {
				   //默认第一个元素值为200ms
				   //data.set(m, 200);
				   data.set(m, 0L);
			   }
		   }
	   } catch (Exception e) {
		   // TODO: handle exception
	   }
	   return data;
   }
    //////////////////////////////////////////////////////////////////////////////////////////////
    //获得对歌词时间ID的歌词
    private int Index()
    {
    	int Curtime = 0;
    	int Coutime = 0;
    	index = 0;
        if(mediaPlayer != null)
        {
        	Curtime = mediaPlayer.getCurrentPosition();
        	Coutime = mediaPlayer.getDuration();
        }
		if(Curtime < Coutime)
		{
			for(int i = 0;i < lyric_timeslist.size();i++)
			{
				if(i < lyric_timeslist.size()-1)
				{
					if(Curtime < lyric_timeslist.get(i) && i==0)
					{
						index = i;
						break;
					}
					if(Curtime > lyric_timeslist.get(i) && Curtime < lyric_timeslist.get(i+1))
					{
						index = i;
						break;
					}
				}
				if(i == lyric_timeslist.size()-1 && Curtime > lyric_timeslist.get(i))
				{
					index = i;
					break;
				}
			}
		}
		return index;
	}
    //////////////////////////////////////////////////////////////////////////////////////////////
    //获得对歌词时间ID的歌词
    private int MusicIndex()
    {
    	long Curtime = 0;
    	long Coutime = 0;
    	index = 0;
    	if(mediaPlayer != null)
    	{
    		Curtime = mediaPlayer.getCurrentPosition();
    		Coutime = mediaPlayer.getDuration();
    	}
    	if(Curtime < Coutime)
    	{
    		for(int i = 0;i < lyric_timeslist.size();i++)
    		{
    			if(i < lyric_timeslist.size()-1)
    			{
    				if(Curtime < lyric_timeslist.get(i) && i==0)
    				{
    					index = i;
    					break;
    				}
    				if(Curtime > lyric_timeslist.get(i) && Curtime < lyric_timeslist.get(i+1))
    				{
    					index = i;
    					break;
    				}
    			}
    			if(i == lyric_timeslist.size()-1 && Curtime > lyric_timeslist.get(i))
    			{
    				index = i;
    				break;
    			}
    		}
    	}
    	temp1 = lyric_timeslist.get(index);
    	temp2 = (index == (lyric_timeslist.size() - 1)) ? 0L : lyric_timeslist.get(index + 1) - temp1;
    	return index;
    }
    //////////////////////////////////////////////////////////////////////////////////////////////
    //音频焦点监听处理
    private OnAudioFocusChangeListener mAudioFocusListener = new OnAudioFocusChangeListener(){
    	public void onAudioFocusChange(int focusChange) {
    		switch(focusChange)
    		{
    		case AudioManager.AUDIOFOCUS_LOSS:
    			if(mediaPlayer.isPlaying())
    			{
    				//会长时间失去，所以告知下面的判断，获得焦点后不要自动播放
    				mPausedByTransientLossOfFocus = false;
    				pause();//因为会长时间失去，所以直接暂停
    			}
    			break;
    		case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
    		case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
    			if(mediaPlayer.isPlaying())
    			{
    				//短暂失去焦点，先暂停。同时将标志位置成重新获得焦点后就开始播放
    				mPausedByTransientLossOfFocus = true;
    				pause();
    			}
    			break;
    		case AudioManager.AUDIOFOCUS_GAIN:
    			//重新获得焦点，且符合播放条件，开始播放
    			if(!mediaPlayer.isPlaying()&& mPausedByTransientLossOfFocus)
    			{
    				mPausedByTransientLossOfFocus = false;
    				isPause = true;
    				resume();
    			}
    			break;
    		}
    	}};
    //////////////////////////////////////////////////////////////////////////////////////////////
    //耳机按键广播接收器
    private class MediaButtonBroadcastReceiver extends BroadcastReceiver {
    	@Override
    	public void onReceive(Context context, Intent intent) {
    		String mAction = intent.getAction();
    		if(mAction.equals(Intent.ACTION_MEDIA_BUTTON))  //监听耳机按键
    		{
    			KeyEvent event = (KeyEvent) intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
    			if (event == null)
    			{
    				return;
    			}
    			int keycode = event.getKeyCode();
    			int action = event.getAction();
    			long eventtime = event.getEventTime();
    			if(mediaPlayer != null)
    			{
    				switch (keycode)
    				{
    				case KeyEvent.KEYCODE_MEDIA_STOP:  //耳机停止键
    					stop();
    					break;
    				case KeyEvent.KEYCODE_HEADSETHOOK:
    				case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:  //暂停后播放
    					isPause = true;
    					resume();
    					break;
    				case KeyEvent.KEYCODE_MEDIA_NEXT:  //耳机下一首按键
    					next();
    					break;
    				case KeyEvent.KEYCODE_MEDIA_PREVIOUS:  //耳机上一首按键
    					previous();
    					break;
    				case KeyEvent.KEYCODE_MEDIA_PAUSE:   //耳机暂停按键
    					pause();
    					break;
    				case KeyEvent.KEYCODE_MEDIA_PLAY:  //耳机播放按键
    					play(0);
    					break;
    				}
    			}
    			
    		}
    		else if(mAction.equals(AudioManager.ACTION_AUDIO_BECOMING_NOISY))  //防止扬声器突然发出声音
    		{
    			//暂时不做
    		}
    	}
    }
    //////////////////////////////////////////////////////////////////////////////////////////////	
    /**
    *	 广播监听通知栏上按钮点击时事件
    */
    public class ButtonBroadcastReceiver extends BroadcastReceiver{
    	@Override
    	public void onReceive(Context context, Intent intent) {
    		// TODO Auto-generated method stub
    		String action = intent.getAction();
    		if(action.equals(ACTION_BUTTON))
    		{
    			//通过传递过来的ID判断按钮点击属性或者通过getResultCode()获得相应点击事件
    			//显示通知栏
    			showNotification(name,bitmap,mp3Infos.get(current).getDuration());
    			int buttonId = intent.getIntExtra(INTENT_BUTTONID_TAG, 0);
    			switch (buttonId) 
    			{
    			case pre_id:
    				Toast.makeText(getApplicationContext(), "上一首", Toast.LENGTH_SHORT).show();
    				break;
    			case play_id:
    				Toast.makeText(getApplicationContext(), "Play", Toast.LENGTH_SHORT).show();
    				break;
    			case next_id:
    				Toast.makeText(getApplicationContext(), "下一首", Toast.LENGTH_SHORT).show();
    				break;
    			default:
    				break;
    			}
    		}
    	}
    }
    //////////////////////////////////////////////////////////////////////////////////////////////	
    /**
     * 自定义通知栏
     */
    public void showButtonNotify(String allname,Bitmap musicalbum,long nduration)
    {
    	NotificationManager mNotificationManager = 
    			(NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    	
    	mBuilder = new android.support.v4.app.NotificationCompat.Builder(context);
    	RemoteViews mRemoteViews = new RemoteViews(getPackageName(), R.layout.notification_layout);
    	mRemoteViews.setImageViewResource(R.id.notification_music_album, R.drawable.default_album);
    	//API3.0 以上的时候显示按钮，否则消失
    	String str_current_name = allname;
    	String notifymname = "";
    	String notifysname = "";
    	
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
    		notifymname = name_str[1];
    		notifysname = name_str[0];
    	}
    	else
    	{
    		notifymname = str_current_name;
    		if(sname.equals("") || sname == null)
    		{
    			notifysname = getResources().getString(R.string.unknown_singer);
    		}
    		else
    		{
    			notifysname = sname;
    		}
    	}
    	
    	mRemoteViews.setTextViewText(R.id.notification_music_sname, notifysname);
    	mRemoteViews.setTextViewText(R.id.notification_music_name, notifymname);
    	mRemoteViews.setTextViewText(R.id.notification_music_duration, MusicUtils.formatTime(nduration));
    	//如果版本号低于（3。0），那么不显示按钮
    	if(BaseTools.getSystemVersion() <= 9){
    		mRemoteViews.setViewVisibility(R.id.notification_next_song_tv, View.GONE);
    		mRemoteViews.setViewVisibility(R.id.notification_play_pause_tv, View.GONE);
    	}else{
    		mRemoteViews.setViewVisibility(R.id.notification_next_song_tv, View.VISIBLE);
    		mRemoteViews.setViewVisibility(R.id.notification_play_pause_tv, View.VISIBLE);
    		if(mediaPlayer != null && mediaPlayer.isPlaying()){
    			mRemoteViews.setImageViewResource(R.id.notification_play_pause_tv, R.drawable.player_play_normal);
    		}else{
    			mRemoteViews.setImageViewResource(R.id.notification_play_pause_tv, R.drawable.player_pause_normal);
    		}
    	}
    	//点击的事件处理
    	Intent buttonIntent = new Intent(ACTION_BUTTON);
    	/* 播放/暂停  按钮 */
    	//这里加了广播，所及INTENT的必须用getBroadcast方法
    	buttonIntent.putExtra(INTENT_BUTTONID_TAG, play_id);
    	PendingIntent intent_paly = PendingIntent.getBroadcast(this, 2, buttonIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    	mRemoteViews.setOnClickPendingIntent(R.id.notification_play_pause_tv, intent_paly);
    	/* 下一首 按钮  */
    	buttonIntent.putExtra(INTENT_BUTTONID_TAG, next_id);
    	PendingIntent intent_next = PendingIntent.getBroadcast(this, 3, buttonIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    	mRemoteViews.setOnClickPendingIntent(R.id.notification_next_song_tv, intent_next);
    	
    	Intent notificationIntent = new Intent(this,MusicActivity.class);
    	notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
    	PendingIntent contentIntent = PendingIntent.getActivity(
    			this, 0, notificationIntent,PendingIntent.FLAG_UPDATE_CURRENT);
    	
    	mBuilder.setContent(mRemoteViews)
    	.setContentIntent(contentIntent)
    	.setWhen(System.currentTimeMillis())// 通知产生的时间，会在通知信息里显示
    	.setTicker(notifysname)
    	.setContentTitle(notifymname)
    	.setOngoing(true)
    	.setLargeIcon(musicalbum);
    	Notification notify = mBuilder.build();
    	notify.flags = Notification.FLAG_ONGOING_EVENT;
    	mNotificationManager.notify(100, notify);
    }
    	
    //////////////////////////////////////////////////////////////////////////////////////////
    // 显示Notification
    public void showNotification(String allname,Bitmap musicalbum,long nduration) 
    {
    	// 创建一个NotificationManager的引用
    	NotificationManager notificationManager = (
    			NotificationManager)this.getSystemService(
    					android.content.Context.NOTIFICATION_SERVICE);  
    	String str_current_name = allname;
    	String notifymname = "";
    	String notifysname = "";
    	
    	if(str_current_name.contains("["))
    	{
    		str_current_name = str_current_name.replace("[", "=");
			String name_str[] = str_current_name.split("=");
			str_current_name = name_str[0];
    		//str_current_name = str_current_name.split("[")[0];
    	}
    	if(str_current_name.contains("-"))
    	{
    		String[] name_str = str_current_name.split("-");
    		notifymname = name_str[1];
    		notifysname = name_str[0];
    	}
    	else
    	{
    		notifymname = str_current_name;
    		if(sname.equals("") || sname == null)
    		{
    			notifysname = getResources().getString(R.string.unknown_singer);
    		}
    		else
    		{
    			notifysname = sname;
    		}
    	}
    	// 定义Notification的各种属性
    	notification = new Notification(
    			R.drawable.default_album,notifymname,System.currentTimeMillis());
    	
    	RemoteViews contentViews = new RemoteViews(getPackageName(), R.layout.notification_layout);  
    	contentViews.setImageViewResource  
    	(R.id.notification_music_album, R.drawable.default_album);
    	
    	contentViews.setTextViewText(R.id.notification_music_name, notifymname);  
    	contentViews.setTextViewText(R.id.notification_music_sname, notifysname);
    	contentViews.setTextViewText(R.id.notification_music_duration, MusicUtils.formatTime(nduration));
    	
    	if(PlayService.mediaPlayer.isPlaying()){  
    		contentViews.setImageViewResource(R.id.notification_play_pause_tv,R.drawable.player_play_normal);  
    	}  
    	else{  
    		contentViews.setImageViewResource(R.id.notification_play_pause_tv,R.drawable.player_pause_normal);  
    	}
    	
    	/* 
			将处理好了的一个View传给notification 
			让其在通知栏上显示 
    	 */  
    	notification.contentView = contentViews;
    	
    	notification.largeIcon = musicalbum;
    	// 将此通知放到通知栏的"Ongoing"即"正在运行"组中
    	notification.flags |= Notification.FLAG_ONGOING_EVENT;
    	// 点击通知栏后，此通知自动清除。
    	notification.flags |= Notification.FLAG_AUTO_CANCEL;
    	notification.flags |= Notification.FLAG_SHOW_LIGHTS;
    	notification.defaults = Notification.DEFAULT_LIGHTS;
    	notification.ledARGB = Color.BLUE;
    	notification.ledOnMS = 5000;      // 设置通知的事件消息       
    	//设置通知的事件消息
    	CharSequence contentTitle = notifymname; // 通知栏标题
    	Intent notificationIntent = new Intent(this,MusicActivity.class);
    	notificationIntent.setAction(Intent.ACTION_MAIN);
    	
    	notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
    	PendingIntent contentIntent = PendingIntent.getActivity(
    			this, 0, notificationIntent,PendingIntent.FLAG_UPDATE_CURRENT);
    	notification.setLatestEventInfo(
    			this, contentTitle, notifysname, contentIntent);
    	// 把Notification传递给NotificationManager
    	notificationManager.notify(0, notification);
    }
    
}
