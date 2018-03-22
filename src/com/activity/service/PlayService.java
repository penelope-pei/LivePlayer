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

	public static MediaPlayer mediaPlayer; // ý�岥��������  
    private String path;            // �����ļ�·��  
    private int msg;  
    public static boolean isPause;        // ��ͣ״̬
    public static boolean isplaying;      //��¼֮ǰ����״̬
    public static int current = 0;        // ��¼��ǰ���ڲ��ŵ����� 
    public static int pre_current = -1;  //��¼��һ�β��ŵ�����
    public static List<MusicInfo> mp3Infos;   //���Mp3Info����ļ���
    public static List<MusicInfo> favmusiclist ;  //���ϲ����Ŀ����
    public static int status = 2;      //����״̬��Ĭ��Ϊѭ������ ,1������ѭ��  2��ȫ��ѭ��  3��˳��  4�����
    public static int show_list_type = 0;  //��ʾ�б�״̬��0����������   1��ϲ���б�   2��������ʷ
    private int currentTime;        //��ǰ���Ž���  
    public static String name = "";       //������
    private String sname = "";      //������
    public static Bitmap bitmap = null;          //ר������
    private Bitmap bp_old = null;
    private Bitmap bp_new = null;  //�������Բ��ͼ
    private String ttime = "";      //������ʱ��
    private Context context;
    public static int index=0;
    private boolean isfavorite = false;
    private LrcHandle mLrcHandle;
    public static List<String> LyricList = null;
    public static List<Long> lyric_timeslist = null;
    public static boolean showWords = false;  //�Ƿ���ʾ���
    public static boolean showPlay = false;   //�ж��Ƿ����ڲ��Ž���
    
    private long temp1 = 0L;
    private long temp2 = 0L;
    
    // �����������¼���� ,�Զ���㲥������,��Ҫ�������绰�����ʱ����ͣ�������� 
    private MyReceiver myReceiver;  
    //��Ƶ������
    private AudioManager mAudioManager; 
    private boolean mPausedByTransientLossOfFocus = false;     //�Ƿ���ʱʧȥ����
    private MediaButtonBroadcastReceiver mMediaButtonReceiver = null;  //���������㲥������
    //////////////////////////////////////////////////////////////////////////////////////////////
    private String INTENT_BUTTONID_TAG = "ButtonId";
	/** ��һ�� ��ť��� ID */
	public final static int pre_id = 1;
	/** ����/��ͣ ��ť��� ID */
	public final static int play_id = 2;
	/** ��һ�� ��ť��� ID */
	public final static int next_id = 3;
	/** Notification ��ID */
	private int notifyId = 101;
	/** NotificationCompat ������*/
	private NotificationCompat.Builder mBuilder;
	/** ֪ͨ����ť�㲥 */
	private ButtonBroadcastReceiver bReceiver;
	/** ֪ͨ����ť����¼���Ӧ��ACTION */
	private final static String ACTION_BUTTON = "com.notifications.intent.action.ButtonClick";
	private Notification notification;
	//////////////////////////////////////////////////////////////////////////////////////////////
    /** 
     * handler����������Ϣ�������͹㲥���²���ʱ�� 
     * //������Ϣ�����̼��ͨ��
     */  
    private Handler handler = new Handler() {  
        public void handleMessage(android.os.Message msg) {
        	switch(msg.what)
        	{
        	case 1:
        		if(mediaPlayer != null)
                {            
                	//��ȡ��ǰĳһ�׸貥�ŵ���λ��,���ŵ�ʱ��
                	getCurrentPlaydata();
                    //��˳�򲥷�ģʽ�£����������һ��ʱ���Զ���ͣ������activity�еĲ��ż�״̬
            		if(msg.arg1 == 1)                	
            		{
            			updatePlaystatus();
                	}
            		//�����ֲ��Ų�ͬ����ʱ������HistoryService���񣬰ѵ�ǰ���ŵ�������ӵ���ʷ��¼���ݿ���
            		if(pre_current != current)
                    {
                    	starthistoryService();
                    }
            		//��ʱ������Ϣ,ÿ1000ms����һ�Σ�1--msg.what��ʵʱ����activity����    
            		handler.sendEmptyMessageDelayed(1, 1000);
                }  
        		break;
        	case 2:
        		//�����յ�action=ACTION_BUTTON��ͼʱ������һ���㲥����ʾ֪ͨ��
        		Intent notifyintent = new Intent(ACTION_BUTTON);
            	sendBroadcast(notifyintent);
        		break;
        	case 3:
        		// �����������ֿ��ӻ����涯��
        		if(MusicPlay.musicvisualizer != null)
        		{
        			//MusicPlay.musicvisualizer.setupVisualizerFx(mediaPlayer.getAudioSessionId());
        		}
				break;
        	case 4:
        		// ֹͣ���ֿ��ӻ����涯��
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
	//�̴߳�����
    Runnable mRunnable= new Runnable() {
		public void run() {
			
			if(showPlay)
			{
				if(MusicPlay.mLyricView != null)
				{
		    		MusicPlay.mLyricView.SetIndex(Index());//ÿ���ʵĲ��� 
		    		MusicPlay.mLyricView.invalidate();   //ˢ��
				}
			}
			else
			{
				if(MusicActivity.mLyricView != null)
				{
					if(LyricList.size() == 1)  //˵��û�и��
					{
						MusicActivity.mLyricView.SetIndex(0);//ÿ���ʵĲ���  Index()
						if(MusicActivity.mLyricView != null && MusicActivity.rl_show_music_info != null)
						{
							MusicActivity.mLyricView.setVisibility(4);
							MusicActivity.rl_show_music_info.setVisibility(0);
						}
					}
					else
					{
						MusicActivity.mLyricView.SetIndex(MusicIndex(),mediaPlayer.getCurrentPosition(),temp1, temp2 );//ÿ���ʵĲ���  Index()
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
					MusicActivity.mLyricView.invalidate();   //ˢ��
				}
			}
			myHandler.postDelayed(mRunnable, 100); //100msˢ��һ��
		}
	};
    //////////////////////////////////////////////////////////////////////////////////////////////
    //ʵʱ����activity��view,���ڷ�������£���Ҫ����Ϊ�������ں�̨ʵʱ���еģ���ȻҲ����ͨ�����㲥�ķ�ʽ��֪ͨactivity���½���
    private void initActivityView()
    {
    	//���棺���ƣ�ʱ�䣬���ȵ�
    	//����MusicPlay�ж�Ӧ�Ŀؼ�
    	updateMusicPlay();
    	//����MusicActivity�ж�Ӧ�Ŀؼ�
    	updateMusicActivity();
    }
    //////////////////////////////////////////////////////////////////////////////////////////////
    //���²��Ž���Ŀؼ�
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
			//bitmap�е�ͼƬ���ܸ���background
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
    //�����б����ؼ�
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
    //��ȡ��ǰ�������ֵ���Ϣ
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
    //�����˳�򲥷�ģʽ���򲥷������һ�׸��ʱ����ͣ���ţ����²��ż���״̬
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
    //����HistoryService���񣬰ѵ�ǰ���ŵ�������ӵ���ʷ��¼���ݿ���
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
    	//������ʹ��intent��������󣬵�ȻҲ���԰Ѷ����ÿ�����Դ���ȥ���������´���
    	
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
        startService(historyintent);       //��������
    }
    //////////////////////////////////////////////////////////////////////////////////////////////
    //onCreate��ֻ�е�һ�������÷����ʱ��Ż�ִ��onCreate��֮���������������񣬲���ִ��onCreate������ִ��onStart
    //���Ϊ�˱����γ�ʼ������,�����ݵĳ�ʼ������onCreate��
    @Override  
    public void onCreate()
    {  
        super.onCreate();
        //�����Ƶ����������
        mAudioManager = (AudioManager)getSystemService(AUDIO_SERVICE);
        context = getBaseContext();
        showWords = false;
        mediaPlayer = new MediaPlayer();
        mLrcHandle = new LrcHandle();
        LyricList = new ArrayList<String>();
        lyric_timeslist = new ArrayList<Long>();
        
        //��ʼ��mpsInfos��favmusiclist�б�,�����ڷ�����ֱ�ӵ��ã�������Ҫÿ�ζ��Ǵ�activity��������
        initmusiclist();
        /** 
         * �������ֲ������ʱ�ļ����� 
         */  
        mediaPlayer.setOnCompletionListener(this);
        //ע��㲥������
        registerbroadcastreceiver();
    } 
    //////////////////////////////////////////////////////////////////////////////////////////////
    //onCompletion�����ż��������¼�
    @Override
	public void onCompletion(MediaPlayer mp) {
		// TODO Auto-generated method stub
		if (status == 1)  // ����ѭ��  
		{ 
            mediaPlayer.start();
            //��ȡ��Ƶ����,��������Ӧ�Ĵ������統��������ʱ����ͣ��������
            mAudioManager.requestAudioFocus(mAudioFocusListener,AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
            isplaying = true;
            //��ʴ���
            LyricHandle();
            handler.sendEmptyMessage(1);
            //��ʾ��֪ͨ����
            handler.sendEmptyMessage(2);
            //����MusicPlay�еľ�����
            handler.sendEmptyMessage(3);
        } 
		else if (status == 2) // ȫ��ѭ��
		{ 
            current++;  
            if(current > mp3Infos.size() - 1) 
            {  
            	//��Ϊ��һ�׵�λ�ü�������  
                current = 0;  
            }
            path = mp3Infos.get(current).getUrl(); 
            LyricHandle();     //��ʴ���
            play(0);
            //��ʾ��֪ͨ����
            handler.sendEmptyMessage(2);
        } 
		else if (status == 3) // ˳�򲥷�  
		{ 
            current++;  //��һ��λ��  
            if (current <= mp3Infos.size() - 1) {  
                path = mp3Infos.get(current).getUrl(); 
                //��ʴ���
                LyricHandle();     
                play(0);
                //��ʾ��֪ͨ����
                handler.sendEmptyMessage(2);
            }
            else 
            {  
            	mediaPlayer.stop();
                mediaPlayer.seekTo(0);  //0--currentTime
                current = mp3Infos.size() - 1;//�������һ�׸��λ��
                LyricHandle();     //��ʴ���
                Message msg = new Message();
                msg.what = 1;
                msg.arg1 = 1;
                handler.sendMessage(msg);
            }
        } 
		else if(status == 4) //�������  
		{    
			//��ȡ�����
            current = getRandomIndex(mp3Infos.size() - 1);  
            path = mp3Infos.get(current).getUrl();
            //��ʴ���
            LyricHandle();     
            play(0);
            //��ʾ��֪ͨ����
            handler.sendEmptyMessage(2);
        }
	}
    //////////////////////////////////////////////////////////////////////////////////////////////
    //ע��㲥������
    private void registerbroadcastreceiver()
    {
    	/** 
         * ע��㲥��������MyReceiver��MediaButtonBroadcaseReceiver��ButtonBroadcastReceiver
         * MyReceiver:���ڼ����绰����ȥ�� �Լ������Ĳ���γ�״̬
         * MediaButtonBroadcaseReceiver:���ڽ��ղ����������������
         * ButtonBroadcastReceiver:���ڽ��ղ�����֪ͨ����ʾ�������Ĳ���
         * 
         */
        myReceiver = new MyReceiver(context);
        mMediaButtonReceiver = new MediaButtonBroadcastReceiver();
        bReceiver = new ButtonBroadcastReceiver();
        
        IntentFilter phoneFilter = new IntentFilter();
        IntentFilter buttonFilter = new IntentFilter();
        IntentFilter intentFilter = new IntentFilter();
        //Ϊ��Ӧ������������Ӷ��������������ݽ��յ��Ĳ�ͬ�Ķ���������ͬ�Ĵ���
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
    //��ʼ���б����ݣ���activity�л�ȡ
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
     * ��ȡ���λ�� 
     * @param end 
     * @return 
     */  
    protected int getRandomIndex(int end) {  
        int index = (int) (Math.random() * end);  
        return index;  
    }
    //////////////////////////////////////////////////////////////////////////////////////////////
    //�󶨷����Զ�ִ��
    @Override  
    public IBinder onBind(Intent arg0) {  
        return null;  
    }  
    //////////////////////////////////////////////////////////////////////////////////////////////
    //��ʴ������
    private void LyricHandle()
    {
    	//���»�ȡ�б����
        mLrcHandle = new LrcHandle();
        LyricList = new ArrayList<String>();
        lyric_timeslist = new ArrayList<Long>();
        //��ȡ���·��
        String wordfile = mp3Infos.get(current).getWordsUrl();
	    mLrcHandle.readLRC(wordfile);
	    //��ø���б�
		LyricList = mLrcHandle.getWords();
		//��ø��ʱ���б�
		lyric_timeslist = mLrcHandle.getTime();
		//��ʱ���õ���lyric_timeslist�б��е�ʱ��������ǻ��ҵģ����Ի���Ҫ������������
		lyric_timeslist = sort_time(lyric_timeslist);
		//�����ʱ���б���ʱ����ʾΪ0������õ����յ�ʱ���б�
		lyric_timeslist = LastTimeList(lyric_timeslist);
		//�ж���ʾ��ʵĿؼ��Ƿ�Ϊ��
		if(MusicPlay.mLyricView != null)
		{
			if(LyricList.size() > 0)
			{
				if(MusicPlay.mLyricView != null)
				{
					//Ϊ�ؼ�������ݣ�����ʾ�ڿ�ʼλ��
					MusicPlay.mLyricView.setSentenceEntities(LyricList);
					MusicPlay.mLyricView.SetIndex(0);  //Index()
				}
			}
		}
		
		//�ж���ʾ��ʵĿؼ��Ƿ�Ϊ��
		if(MusicActivity.mLyricView != null)
		{
			if(LyricList.size() > 0)
			{
				if(MusicActivity.mLyricView != null)
				{
					//Ϊ�ؼ�������ݣ�����ʾ�ڿ�ʼλ��
					MusicActivity.mLyricView.setSentenceEntities(LyricList);
					MusicActivity.mLyricView.SetIndex(0);  //Index()
				}
			}
		}
		//ͨ��Handler������ʴ����߳�mRunnable����ʵĴ���ȽϺ�ʱ������Ҫʵʱ�ı�״̬����˱�������߳��ﴦ��
		myHandler.post(mRunnable);
    }
    //////////////////////////////////////////////////////////////////////////////////////////////
    //onStart
    @Override  
    public void onStart(Intent intent, int startId) { 
    	
    	path = intent.getStringExtra("url");        //����·��
    	current = intent.getIntExtra("listPosition", -1);   //��ǰ���Ÿ�������mp3Infos��λ��
    	name = mp3Infos.get(current).getName();    //��ǰ������
    	sname = mp3Infos.get(current).getSinger();   //��ǰ������
    	//Ϊ�˲�������������ͨ���쳣������Ա���
    	try {
    		//��ȡ��Ϣ����id
    		msg = intent.getIntExtra("MSG", 0);         
        	if (msg == MusicActivity.PLAY_MSG)          //��������
        	{     		
        		LyricHandle();     //��ʴ���
        		play(0);  
            } 
        	else if (msg == MusicActivity.PAUSE_MSG)    //��ͣ
        	{    
                pause();      
            } 
        	else if (msg == MusicActivity.STOP_MSG)     //ֹͣ
        	{       
                stop();  
            } 
        	else if (msg == MusicActivity.CONTINUE_MSG) //�������� 
        	{  
            	isPause = true;
                resume();     
            } 
        	else if (msg == MusicActivity.PRIVIOUS_MSG) //��һ��  
        	{ 
            	LyricHandle();     //��ʴ���
                previous();  
            } 
        	else if (msg == MusicActivity.NEXT_MSG)    //��һ��
        	{       
            	LyricHandle();     //��ʴ���
                next();  
            } 
        	else if (msg == MusicActivity.PROGRESS_CHANGE) //���ȸ���
        	{    
                currentTime = intent.getIntExtra("progress", -1);  
                play(currentTime);
            } 
        	else if (msg == MusicActivity.PLAYING_MSG)    //���ڲ���
        	{
                handler.sendEmptyMessage(1);
                handler.sendEmptyMessage(2);
            }
            else if(msg == MusicActivity.SHOW_NOTIFICATION)  //��ʾ֪ͨ��
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
     * �������� 
     *  
     * @param position 
     */  
    private void play(int currentTime) 
    {  
        try {
        	mediaPlayer.reset();// �Ѹ�������ָ�����ʼ״̬  
            mediaPlayer.setDataSource(path);  
            mediaPlayer.prepare(); // ���л���  
            mediaPlayer.setOnPreparedListener(new PreparedListener(currentTime));//����ǰ׼��������
            //ʵʱ��������
            handler.sendEmptyMessage(1);
            //����MusicPlay�еľ�����
            handler.sendEmptyMessage(3);
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
    }
    //////////////////////////////////////////////////////////////////////////////////////////////
    /** 
     * ��ͣ���� 
     */  
    private void pause() 
    {  
        if (mediaPlayer != null && mediaPlayer.isPlaying()) 
        {  
            mediaPlayer.pause();
            //���´�����Ҫ��Ϊ�˷�ֹ���ʹ�ö���������ͣʱ��Activity����Ĳ�����ͣ��ťû�з����ı�
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
            //ֹͣ���ӻ���ͼ����
            handler.sendEmptyMessage(4);
        }  
    }  
    //////////////////////////////////////////////////////////////////////////////////////////////
    /** 
     * �ָ��������� 
     */ 
    private void resume() {  
        if (isPause) 
        {
            mediaPlayer.start(); 
            //��ȡ��Ƶ����,��������Ӧ�Ĵ������統��������ʱ����ͣ��������
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
            //����MusicPlay�еľ�����
            handler.sendEmptyMessage(3);
        }  
    }  
    //////////////////////////////////////////////////////////////////////////////////////////////
    /** 
     * ��һ�� 
     */  
    private void previous() 
    {  
        play(0);  
    }  
    //////////////////////////////////////////////////////////////////////////////////////////////
    /** 
     * ��һ�� 
     */  
    private void next() 
    {  
        play(0);  
    }  
    //////////////////////////////////////////////////////////////////////////////////////////////
    /** 
     * ֹͣ���� 
     */  
    public static void stop() 
    {  
        if (mediaPlayer != null) 
        {  
            mediaPlayer.stop();  
            try {  
            	// �ڵ���stop�������Ҫ�ٴ�ͨ��start���в���,��Ҫ֮ǰ����prepare����  
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
            mediaPlayer.stop();   //ֹͣ������
            mediaPlayer.release();//�ͷ�  
            mediaPlayer = null;  
        } 
        //ע���㲥������
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
        //�ͷ���Ƶ����
        mAudioManager.abandonAudioFocus(mAudioFocusListener);
        super.onDestroy();
          
    }  
    //////////////////////////////////////////////////////////////////////////////////////////////
    /** 
     *  
     * ʵ��һ��OnPrepareLister�ӿ�,������׼���õ�ʱ��ʼ���� 
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
        	
            mediaPlayer.start(); // ��ʼ����  
            isplaying = true;
            //��ȡ��Ƶ����,��������Ӧ�Ĵ������統��������ʱ����ͣ��������
            mAudioManager.requestAudioFocus(mAudioFocusListener,AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
            
            if (currentTime > 0) { // ������ֲ��Ǵ�ͷ����  
                mediaPlayer.seekTo(currentTime);  
            } 
        }  
    }  
    //////////////////////////////////////////////////////////////////////////////////////////////
    //�Ը��ʱ���������,���ַ��������ȽϺ�ʱ
    private List<Long> sort_time(List<Long> times_list)
    {
    	List<Long> list_times = times_list;
    	long temp_time = 0;
    	String temp_words = "";
    	
    	for(int i = 0;i<list_times.size();i++)
    	{
    		for(int j = i+1;j<list_times.size();j++)//���ʱ�����򣬸������
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
    //�Ը��ʱ���������,���ַ����ǿ�������
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
    //�˷����ж�ʱ����ʾΪ0���һ�γ��ֵ�λ�ã�������ʱ���б�
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
		   show_time = all_time / last_zero_index;//���ǰ��ʱ���ƽ��ʱ�䣬��[ar:,,,]
		   //���´����е�200,250�����Ǽ������ݣ���������ȫ��ȷ�����ݣ���Ϊ����ʱ��
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
				   //Ĭ�ϵ�һ��Ԫ��ֵΪ200ms
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
    //��öԸ��ʱ��ID�ĸ��
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
    //��öԸ��ʱ��ID�ĸ��
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
    //��Ƶ�����������
    private OnAudioFocusChangeListener mAudioFocusListener = new OnAudioFocusChangeListener(){
    	public void onAudioFocusChange(int focusChange) {
    		switch(focusChange)
    		{
    		case AudioManager.AUDIOFOCUS_LOSS:
    			if(mediaPlayer.isPlaying())
    			{
    				//�᳤ʱ��ʧȥ�����Ը�֪������жϣ���ý����Ҫ�Զ�����
    				mPausedByTransientLossOfFocus = false;
    				pause();//��Ϊ�᳤ʱ��ʧȥ������ֱ����ͣ
    			}
    			break;
    		case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
    		case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
    			if(mediaPlayer.isPlaying())
    			{
    				//����ʧȥ���㣬����ͣ��ͬʱ����־λ�ó����»�ý����Ϳ�ʼ����
    				mPausedByTransientLossOfFocus = true;
    				pause();
    			}
    			break;
    		case AudioManager.AUDIOFOCUS_GAIN:
    			//���»�ý��㣬�ҷ��ϲ�����������ʼ����
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
    //���������㲥������
    private class MediaButtonBroadcastReceiver extends BroadcastReceiver {
    	@Override
    	public void onReceive(Context context, Intent intent) {
    		String mAction = intent.getAction();
    		if(mAction.equals(Intent.ACTION_MEDIA_BUTTON))  //������������
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
    				case KeyEvent.KEYCODE_MEDIA_STOP:  //����ֹͣ��
    					stop();
    					break;
    				case KeyEvent.KEYCODE_HEADSETHOOK:
    				case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:  //��ͣ�󲥷�
    					isPause = true;
    					resume();
    					break;
    				case KeyEvent.KEYCODE_MEDIA_NEXT:  //������һ�װ���
    					next();
    					break;
    				case KeyEvent.KEYCODE_MEDIA_PREVIOUS:  //������һ�װ���
    					previous();
    					break;
    				case KeyEvent.KEYCODE_MEDIA_PAUSE:   //������ͣ����
    					pause();
    					break;
    				case KeyEvent.KEYCODE_MEDIA_PLAY:  //�������Ű���
    					play(0);
    					break;
    				}
    			}
    			
    		}
    		else if(mAction.equals(AudioManager.ACTION_AUDIO_BECOMING_NOISY))  //��ֹ������ͻȻ��������
    		{
    			//��ʱ����
    		}
    	}
    }
    //////////////////////////////////////////////////////////////////////////////////////////////	
    /**
    *	 �㲥����֪ͨ���ϰ�ť���ʱ�¼�
    */
    public class ButtonBroadcastReceiver extends BroadcastReceiver{
    	@Override
    	public void onReceive(Context context, Intent intent) {
    		// TODO Auto-generated method stub
    		String action = intent.getAction();
    		if(action.equals(ACTION_BUTTON))
    		{
    			//ͨ�����ݹ�����ID�жϰ�ť������Ի���ͨ��getResultCode()�����Ӧ����¼�
    			//��ʾ֪ͨ��
    			showNotification(name,bitmap,mp3Infos.get(current).getDuration());
    			int buttonId = intent.getIntExtra(INTENT_BUTTONID_TAG, 0);
    			switch (buttonId) 
    			{
    			case pre_id:
    				Toast.makeText(getApplicationContext(), "��һ��", Toast.LENGTH_SHORT).show();
    				break;
    			case play_id:
    				Toast.makeText(getApplicationContext(), "Play", Toast.LENGTH_SHORT).show();
    				break;
    			case next_id:
    				Toast.makeText(getApplicationContext(), "��һ��", Toast.LENGTH_SHORT).show();
    				break;
    			default:
    				break;
    			}
    		}
    	}
    }
    //////////////////////////////////////////////////////////////////////////////////////////////	
    /**
     * �Զ���֪ͨ��
     */
    public void showButtonNotify(String allname,Bitmap musicalbum,long nduration)
    {
    	NotificationManager mNotificationManager = 
    			(NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    	
    	mBuilder = new android.support.v4.app.NotificationCompat.Builder(context);
    	RemoteViews mRemoteViews = new RemoteViews(getPackageName(), R.layout.notification_layout);
    	mRemoteViews.setImageViewResource(R.id.notification_music_album, R.drawable.default_album);
    	//API3.0 ���ϵ�ʱ����ʾ��ť��������ʧ
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
    	//����汾�ŵ��ڣ�3��0������ô����ʾ��ť
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
    	//������¼�����
    	Intent buttonIntent = new Intent(ACTION_BUTTON);
    	/* ����/��ͣ  ��ť */
    	//������˹㲥������INTENT�ı�����getBroadcast����
    	buttonIntent.putExtra(INTENT_BUTTONID_TAG, play_id);
    	PendingIntent intent_paly = PendingIntent.getBroadcast(this, 2, buttonIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    	mRemoteViews.setOnClickPendingIntent(R.id.notification_play_pause_tv, intent_paly);
    	/* ��һ�� ��ť  */
    	buttonIntent.putExtra(INTENT_BUTTONID_TAG, next_id);
    	PendingIntent intent_next = PendingIntent.getBroadcast(this, 3, buttonIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    	mRemoteViews.setOnClickPendingIntent(R.id.notification_next_song_tv, intent_next);
    	
    	Intent notificationIntent = new Intent(this,MusicActivity.class);
    	notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
    	PendingIntent contentIntent = PendingIntent.getActivity(
    			this, 0, notificationIntent,PendingIntent.FLAG_UPDATE_CURRENT);
    	
    	mBuilder.setContent(mRemoteViews)
    	.setContentIntent(contentIntent)
    	.setWhen(System.currentTimeMillis())// ֪ͨ������ʱ�䣬����֪ͨ��Ϣ����ʾ
    	.setTicker(notifysname)
    	.setContentTitle(notifymname)
    	.setOngoing(true)
    	.setLargeIcon(musicalbum);
    	Notification notify = mBuilder.build();
    	notify.flags = Notification.FLAG_ONGOING_EVENT;
    	mNotificationManager.notify(100, notify);
    }
    	
    //////////////////////////////////////////////////////////////////////////////////////////
    // ��ʾNotification
    public void showNotification(String allname,Bitmap musicalbum,long nduration) 
    {
    	// ����һ��NotificationManager������
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
    	// ����Notification�ĸ�������
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
			��������˵�һ��View����notification 
			������֪ͨ������ʾ 
    	 */  
    	notification.contentView = contentViews;
    	
    	notification.largeIcon = musicalbum;
    	// ����֪ͨ�ŵ�֪ͨ����"Ongoing"��"��������"����
    	notification.flags |= Notification.FLAG_ONGOING_EVENT;
    	// ���֪ͨ���󣬴�֪ͨ�Զ������
    	notification.flags |= Notification.FLAG_AUTO_CANCEL;
    	notification.flags |= Notification.FLAG_SHOW_LIGHTS;
    	notification.defaults = Notification.DEFAULT_LIGHTS;
    	notification.ledARGB = Color.BLUE;
    	notification.ledOnMS = 5000;      // ����֪ͨ���¼���Ϣ       
    	//����֪ͨ���¼���Ϣ
    	CharSequence contentTitle = notifymname; // ֪ͨ������
    	Intent notificationIntent = new Intent(this,MusicActivity.class);
    	notificationIntent.setAction(Intent.ACTION_MAIN);
    	
    	notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
    	PendingIntent contentIntent = PendingIntent.getActivity(
    			this, 0, notificationIntent,PendingIntent.FLAG_UPDATE_CURRENT);
    	notification.setLatestEventInfo(
    			this, contentTitle, notifysname, contentIntent);
    	// ��Notification���ݸ�NotificationManager
    	notificationManager.notify(0, notification);
    }
    
}
