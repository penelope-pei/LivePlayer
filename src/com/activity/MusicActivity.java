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

	public static int PLAY_MSG = 0;//����
	public static int PAUSE_MSG = 1;//��ͣ
	public static int STOP_MSG = 2;//ֹͣ
	public static int CONTINUE_MSG = 3;//����
	public static int PRIVIOUS_MSG = 4;//��һ��
	public static int NEXT_MSG = 5;//��һ��
	public static int PROGRESS_CHANGE = 6;//ʱ��
	public static int PLAYING_MSG = 7;//
	public static int SEEKBAR_CHANGE = 8;//������
	public static int SHOW_NOTIFICATION = 9;  //��ʾ֪ͨ��
	
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
	
	private int list_type = 0;   //��ʾ�б���������   0�������б�   1��ϲ���б�   2�����ż�¼�б�
	private int activity_type;
	public static int clickindex = 0;
	private Intent intent;
	public static boolean isPlay = false;
	public static boolean backfromplay = false;
	public static boolean isfavorite = false;
	private boolean firstclick = false;//�Ƿ��һ�ν����͵�����Ű�ť
	
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
	public static LyricView mLyricView;//��ʾ�����ʣ��滻�����͸���
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
	private AlertDialog.Builder builder = null;  //����ListView�����Ի���
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
    private TextView tv_dialog;//��ʾ��ĸ��TextView 
    private CharacterParser characterParser;//����ת����ƴ������
    private PinyinComparator pinyinComparator;//����ƴ��������ListView����������� 
	
    //���ڿ����̵߳�Handler
  	private Handler threadHandler = new Handler();
  	private int common_del_position = 0;  //��¼���г���ɾ�������ֵ�id
    
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
    			Toast.makeText(MusicActivity.this,"�б�����...", Toast.LENGTH_SHORT).show();
    			break;
    			
    		case 6:
    			Toast.makeText(MusicActivity.this,"���ڻ�ȡ����,�����ĵȴ�...", Toast.LENGTH_SHORT).show();
    			break;
    			
    		case 7:
    			//�ӷ����л�ȡ���ݣ����Ұ����ݴ浽mymusiclist��
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
    			//���PlayService���Ѿ�����ϲ������
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
		requestWindowFeature(Window.FEATURE_NO_TITLE);//ȥ��������
		// ������ɾ��֮ǰ���Ƕ����֪ͨ   
        NotificationManager notificationManager = (NotificationManager) this  
                .getSystemService(NOTIFICATION_SERVICE);   
        notificationManager.cancel(0);  
		setContentView(R.layout.activity_music);
		backfromplay = false;
		
		PlayService.showPlay = false;
		//ʵ��������תƴ����
		characterParser = CharacterParser.getInstance();
		pinyinComparator = new PinyinComparator();
		
		//��ʼ���ؼ�
		initViews();
		//���ü����¼�
		initListener();		
		//��ʼ���б�����
		initData();
		
		//������ȡ������ʷ��¼����
		getHistoryListfromdb();
	}
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		//��ֵ���ж�
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
		//ע��һ���㲥����Ҫ������֪ͨMusicActivity���½���
    	IntentFilter UpdateMusicFilter = new IntentFilter();  
    	UpdateMusicFilter.addAction(NotificationMsg.NOTIFICATION_MUSICACTIVITY_TO_UPDATE_LOCAL);
    	UpdateMusicFilter.addAction(NotificationMsg.NOTIFICATION_MUSICACTIVITY_TO_UPDATE_FAV);
    	UpdateMusicFilter.addAction(NotificationMsg.NOTIFICATION_MUSICACTIVITY_TO_UPDATE_HISTORY); 
        registerReceiver(updateReceiver,UpdateMusicFilter);
		
		//���ñ���ͼƬ������GetDataService��������е�background_id���ж���ʾ���ֱ���
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
			//Ĭ�ϲ���ͼƬ,��ͣ��
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
			
			//ר��ͼƬ����
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
		//�ͷ�ͼƬ��Դ����Ȼ���ڴ�й¶
		releaseImageViews();
		//����������ʾ֪ͨ��
		if(PlayService.mediaPlayer != null)
		{
			Intent noticationintent = new Intent();
			noticationintent.setClass(MusicActivity.this, PlayService.class);  
			noticationintent.putExtra("url", minfo.getUrl());
			noticationintent.putExtra("listPosition", clickindex);
			noticationintent.putExtra("MSG", SHOW_NOTIFICATION);
			startService(noticationintent);       //��������
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(updateReceiver);
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	//ͨ��findViewById������ȡ�ؼ�
	private void initViews() {
		// TODO Auto-generated method stub		
		iv_back = (TextView)findViewById(R.id.title_button_id);
		iv_paly_music = (ImageView)findViewById(R.id.paly_music_album);
		textview_more_info = (TextView)findViewById(R.id.page_title_more_info);
		textview_title = (TextView)findViewById(R.id.page_title_name);
		textview_music_resource = (TextView)findViewById(R.id.music_local_resource);
		textview_no_data = (TextView)findViewById(R.id.no_music_data);
		textview_nums = (TextView)findViewById(R.id.song_num);
		//��ʼ����Ļ�����ؼ�
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
		
		//���ñ���ͼƬ������GetDataService��������е�background_id���ж���ʾ���ֱ���
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
				//mLyricView.setSentenceEntities(PlayService.LyricList);//���ø����Դ������ʾ
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
	//��Ӹ����ؼ��ļ����¼�
	private void initListener()
	{
		listview_music.setOnItemClickListener(this);  //����ÿ��Item������������в���
		listview_music.setOnItemLongClickListener(this);
		//���һ����л�listview����
		listview_music.setLongClickable(true);
		listview_music.setOnTouchListener(new MyGestureListener(this));
		
		iv_paly_music.setOnClickListener(this);
		rl_music_about_layout.setOnClickListener(this);
		mLyricView.setOnClickListener(this);
		//iv_back.setOnClickListener(this);//�������ؼ�		
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
		//�����Ҳഥ������
		sideBar.setOnTouchingLetterChangedListener(new OnTouchingLetterChangedListener() {
			@Override
			public void onTouchingLetterChanged(String s) {
				//����ĸ�״γ��ֵ�λ��
				int position = musicListAdapter.getPositionForSection(s.charAt(0));
				if(position != -1){
					listview_music.setSelection(position);
				}
			}
		});
		
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	//��ʼ��������ʼ������
	private void initData()
	{
		mymusiclist = new ArrayList<MusicInfo>();
		myfavmusiclist = new ArrayList<MusicInfo>();
		myhistorymusiclist = new ArrayList<MusicInfo>();
		bufferlist = new ArrayList<MusicInfo>();
		
		musicListAdapter = new MusicListAdapter(this);
		listview_music.setAdapter(musicListAdapter);
		
		firstclick = GetDataService.firstclick;
		
		//�������������,���һ�û�õ����ݣ�������ʾ��
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
	//onClick����
	@Override
	public void onClick(View view) {
		Intent intent = null;
		// TODO Auto-generated method stub
		switch(view.getId())
		{
		case R.id.title_button_id:
			finish();  //ֱ��������һ�����
			//�����л�����������߽��룬�ұ��˳����˷����������startActivity����finish()֮��
			overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
			break;
			
		case R.id.page_title_more_info:
			//����ѡ�񱳾��Ի���
			showbackground_dialog();
			break;
		case R.id.music_local_resource:
			//��ʾ���������б�
			showmusiclist();
			break;
		case R.id.paly_music_album:
		case R.id.music_about_layout:
		case R.id.LyricShowOnMusic:
			//��ת�����Ž���
			to_play_activity();
			break;
			
		case R.id.play_on_music:
			//��ͣ�򲥷�
			play_pause_music();
			break;
			
		case R.id.play_next_music:
			//��һ��
			play_next_music();
			break;
			
		case R.id.play_previous_music:
			//��һ��
			play_previous_music();
			break;
			
		case R.id.music_favorite:
			//��ʾϲ���б�
			showfavlist();
			break;
			
		case R.id.music_history:
			//��ʾ��ʷ��¼�б�
			showhistorylist();
			break;
		}
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	//��һ�ײ���
	private void play_next()
	{
		Intent play_next_intent = new Intent();
		clickindex++;
		if(clickindex > mymusiclist.size())
		{
			clickindex = 0;
		}
		minfo = mymusiclist.get(clickindex);
		//����TextView����
		updateViews();
		play_next_intent.putExtra("url", minfo.getUrl());
		play_next_intent.putExtra("MSG", NEXT_MSG);
		play_next_intent.putExtra("listPosition", clickindex);
		play_next_intent.setClass(MusicActivity.this, PlayService.class);       
		//��������ֻ�ڵ�һ��������ʱ��ִ��Service�е�onCreate�����治ִ�У���ִ��onStart�����һ�ȡ������onStart�л�ȡ
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
					//mLyricView.setSentenceEntities(PlayService.LyricList);//���ø����Դ������ʾ
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
	//��һ�ײ���
	private void play_previous()
	{
		Intent play_previous_intent = new Intent();
		clickindex--;
		if(clickindex < 0)
		{
			clickindex = mymusiclist.size() - 1;
		}
		minfo = mymusiclist.get(clickindex);
		//����TextView����
		updateViews();
		play_previous_intent.putExtra("url", minfo.getUrl());
		play_previous_intent.putExtra("MSG", PRIVIOUS_MSG);
		play_previous_intent.putExtra("listPosition", clickindex);
		play_previous_intent.setClass(MusicActivity.this, PlayService.class);  
        startService(play_previous_intent);       //��������
        
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
					//mLyricView.setSentenceEntities(PlayService.LyricList);//���ø����Դ������ʾ
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
	//�����б�
	private void updateViews()
	{
		btn_play_stop.setBackgroundResource(R.drawable.player_play_normal);
		//ȥ���������е�mp3����
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
	//ɾ��֮����¿ؼ�
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
		
		//�ӷ����л�ȡ���ݣ����Ұ����ݴ浽mymusiclist��
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
	//�����ݿ��л�ȡ���������б�
	private void getAllListfromdb()
	{
		//��ֹͳ���б��������������Ҫ����newһ��mymusiclist
		mymusiclist = new ArrayList<MusicInfo>();
		PlayService.mp3Infos = new ArrayList<MusicInfo>();
		if(GetDataService.db != null)
		{
			//�����ݿ�
			GetDataService.db.open();
			//�������ݿ�����
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
					//����:getMusicName(String name)��ָȥ�������������.mp3
					musicInfo.setName(getMusicName(title));
					musicInfo.setId(music_id);
					musicInfo.setFavorite(favorite);
					musicInfo.setWordsUrl(word_url);
					musicInfo.setAlbumUrl(album_url);
					//��ȡר�����棨�����������Ļ�����ܺ�ʱ������Ҫ������ο������̼߳��أ�
					//url = /storage/emulated/0/Music/Download/Delacey - Dream It Possible/Dream It Possible.mp3
					Bitmap albumArt = createAlbumArt(album_url);//convertToBitmap(album_url,40,40);
					//�жϸ����Ƿ��ж��ڵ�ר��ͼƬ��û��ʱ�ÿգ���ֹר��ͼƬΪ��ʱ���ִ����쳣����
					try {
						musicInfo.setThumbnail(albumArt);
					} catch (Exception e) {
						// TODO: handle exception
						musicInfo.setThumbnail(null);
					}
					//��ӵ�list��
					mymusiclist.add(musicInfo);
				}while(c.moveToNext());
				mymusiclist = filledData(mymusiclist);
				// ����a-z��������Դ����
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
	//�����ݿ��л�ȡϲ�������б�
	private void getFavListfromdb()
	{
		//��ֹͳ���б��������������Ҫ����newһ��myfavmusiclist
		myfavmusiclist = new ArrayList<MusicInfo>();
		bufferlist = new ArrayList<MusicInfo>();
		
		if(GetDataService.db != null)
		{
			//�����ݿ�
			GetDataService.db.open();
			//�������ݿ�����
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
					//����:getMusicName(String name)��ָȥ�������������.mp3
					musicInfo.setName(getMusicName(title));
					musicInfo.setId(music_id);
					musicInfo.setFavorite(favorite);
					musicInfo.setWordsUrl(word_url);
					musicInfo.setAlbumUrl(album_url);
					
					Bitmap albumArt = createAlbumArt(album_url);
					//�жϸ����Ƿ��ж��ڵ�ר��ͼƬ��û��ʱ�ÿգ���ֹר��ͼƬΪ��ʱ���ִ����쳣����
					try {
						musicInfo.setThumbnail(albumArt);
					} catch (Exception e) {
						// TODO: handle exception
						musicInfo.setThumbnail(null);
					}
					//��ӵ�list��
					//mymusiclist.add(musicInfo);
					//��ӵ�list��
					if(favorite)
					{
						myfavmusiclist.add(musicInfo);
					}
				}while(c.moveToNext());
				
				myfavmusiclist = filledData(myfavmusiclist);
				// ����a-z��������Դ����
				Collections.sort(myfavmusiclist, pinyinComparator);
				PlayService.favmusiclist = myfavmusiclist;
				bufferlist = myfavmusiclist;
				GetDataService.isfirsthasgetfavdata = true;
				GetDataService.db.close();
			}
		}
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	//�����ݿ��л�ȡ���ֲ�����ʷ�б�
	private void getHistoryListfromdb()
	{
		//�������񣬻�ȡ������ʷ��¼
		Intent historyintent = new Intent();
	  	historyintent.putExtra("type", "toget");
	  	historyintent.setClass(MusicActivity.this, GetHistoryService.class);  
	  	startService(historyintent);
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	//�����ݿ��ȡ����
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
    				//����:getMusicName(String name)��ָȥ�������������.mp3
    				musicInfo.setName(getMusicName(title));
    				musicInfo.setId(music_id);
    				musicInfo.setFavorite(favorite);
    				musicInfo.setWordsUrl(word_url);
    				musicInfo.setAlbumUrl(album_url);
    				//��ȡר�����棨�����������Ļ�����ܺ�ʱ������Ҫ������ο������̼߳��أ�
    				//url = /storage/emulated/0/Music/Download/Delacey - Dream It Possible/Dream It Possible.mp3
    				Bitmap albumArt = createAlbumArt(album_url);//convertToBitmap(album_url,40,40);
    				//�жϸ����Ƿ��ж��ڵ�ר��ͼƬ��û��ʱ�ÿգ���ֹר��ͼƬΪ��ʱ���ִ����쳣����
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
    //onItemClick����
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
			//���жϵ�ǰ���������������б���ĸ�λ��
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
			intent.putExtra("url", minfo.getUrl());    //·��
			intent.putExtra("MSG", PLAY_MSG);          //����״̬
			intent.putExtra("listPosition", clickindex);//λ�ã���һ��
			intent.setClass(MusicActivity.this, PlayService.class);  
			startService(intent);       //��������
			
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
						//mLyricView.setSentenceEntities(PlayService.LyricList);//���ø����Դ������ʾ
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
	//�õ�������
	private String getMusicName(String name)
	{
		//��������ʽ��,������ţ�"."��"|"��"^"���ַ���
		//������ \ ������ת�壬����java�ַ����У�\ Ҳ�Ǹ��Ѿ���ʹ�õ�������ţ�Ҳ��Ҫʹ�� \ ��ת�塣
		//��Щ���������д��з���'.'
		return name.substring(0, name.length() - 4);
		//���������·�����ԭ���ǣ���Щ����������"."���ʲ�����split���ָ�
		//�������и�������������.mp3�ַ������ʹ��substring�����;����ܱ�֤�õ���ȷ�ĸ�����
		/*String[] arrstr = name.split("\\.");
		return arrstr[0];*/
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * @Description ��ȡר������
	 * @param filePath �ļ�·����like XXX/XXX/XX.mp3
	 * ���ͼƬ̫�󣬻ᵼ��bitmapװ���£����Խ���취Ϊ��ͼƬ��С����Ϊԭͼ��һ��
	 * @return ר������bitmap
	 */
	public Bitmap createAlbumArt(final String filePath) {
	    Bitmap bitmap = null;
	    //�������εĴ���ʹ����ͼƬ�ľ���·������ȡ�ģ����ǲ�ͬ���ֻ���ר��ͼƬ�����·����һ��
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
        	bitmap = BitmapFactory.decodeFile(filePath,getBitmapOption(2));//�ӱ����ж�ȡͼƬ,�����ļ�  albumfile
        }
	    return bitmap;
	}
	//��ͼƬ�ĳ��Ϳ���Сζԭ����1/2
	private Options getBitmapOption(int inSampleSize){
		System.gc();
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPurgeable = true;
		options.inSampleSize = inSampleSize;
		return options;
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	//�ڶ��ַ�����ȡbitmap,�������ַ�����ȡ����ͼƬ�Ƚ�ģ��
	public Bitmap convertToBitmap(String path, int w, int h) 
	{
		Bitmap bitmap = null;
		if(!path.equals("nothisfile"))
		{
			BitmapFactory.Options opts = new BitmapFactory.Options();
			// ����Ϊtureֻ��ȡͼƬ��С
			opts.inJustDecodeBounds = true;
			opts.inPreferredConfig = Bitmap.Config.ARGB_4444;
			// ����Ϊ��
			BitmapFactory.decodeFile(path, opts);
			int width = opts.outWidth;
			int height = opts.outHeight;
			float scaleWidth = 0.f, scaleHeight = 0.f;
			if (width > w || height > h) {
				// ����
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
    //����ɾ������
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
					//���µ����ĶԻ�����ϵͳ�Դ��ĶԻ���
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
					//���µ����ĶԻ�����ϵͳ�Դ��ĶԻ���
					showDialogDeletemusic(position,name);
					
				}
				@Override
				public void docancel() {
					// TODO Auto-generated method stub
					delete_dialog.dismiss();
				}
			});
		}
		return true;  //����true����ֹ��onItemClick��ͻ
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	//��ɾ���б��Դ��������ݿ��ж�Ӧ������
	private void showDialogDeletemusic(final int del_position,String del_name)
	{
		 builder.setTitle("ɾ������")  //���öԻ������
	     .setMessage("��ȷ��Ҫɾ�� "+"��"+name+"��"+ "���׸���?")//������ʾ������ 
	     .setCancelable(false)       //����հ״����Ի����Զ��˳���������ؼ�Ҳ�����˳�
	     .setPositiveButton("ȷ��",new DialogInterface.OnClickListener() {//���ȷ����ť  
	         @Override  
	         public void onClick(DialogInterface dialog, int which) {//ȷ����ť����Ӧ�¼�  
	        	 // TODO Auto-generated method stub
	        	 if(list_type == 0)
	        	 {
	        		 mymusiclist.remove(del_position);
	        		 //�����б�
	        		 updatelist(del_position,name,minfo.getName());
	        		 dialog.dismiss();
	        		 //������ʾ
	        		 Toast.makeText(MusicActivity.this, "ɾ���ɹ�", Toast.LENGTH_LONG).show();
	        		 //�������ݿ�Ĳ����ȽϺ�ʱ��������Ҫ�����߳���ִ��
		        	 threadHandler.post(delmusicdataThread);
	        	 }
	        	 //������ϲ���б����ɾ������������Ҫɾ�����ݿ����ݣ�ֻ��Ҫ�������ݿ����ݼ��б�����
	        	 else if(list_type == 1)
	        	 {
	        		//�����б�
	        		myfavmusiclist.remove(del_position);
	        		updateOffavmusic();
	        		dialog.dismiss();
	        		//������ʾ
	        		Toast.makeText(MusicActivity.this, "ɾ���ɹ�", Toast.LENGTH_LONG).show();
	        		//�������ݿ�Ĳ����ȽϺ�ʱ��������Ҫ�����߳���ִ��
	        		threadHandler.post(delonefavdataThread);
	        	 }
	        	 else if(list_type == 2)
	        	 {
	        		 myhistorymusiclist.remove(del_position);
    				 updateOfhistorymusic();
	 	        	 dialog.dismiss();
	 	        	 //������ʾ
	   				 Toast.makeText(MusicActivity.this, "ɾ���ɹ�", Toast.LENGTH_LONG).show();
	        		//�������ݿ�Ĳ����ȽϺ�ʱ��������Ҫ�����߳���ִ��
	    			threadHandler.post(delonehistorydataThread);
	        	 }
	         }  
	  
	     }).setNegativeButton("ȡ��",new DialogInterface.OnClickListener() {//��ӷ��ذ�ť  
	         @Override  
	         public void onClick(DialogInterface dialog, int which) {//��Ӧ�¼�  
	             // TODO Auto-generated method stub 
	        	 dialog.dismiss();
	         }  
	     })
	     .create()
	     .show();//�ڰ�����Ӧ�¼�����ʾ�˶Ի���
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	//ɾ������local�����ļ�
	private void showDialogDeletelocalmusic(final int del_position,String del_name)
	{
		 builder.setTitle("ɾ������")  //���öԻ������
	     .setMessage("��ȷ��Ҫɾ�� "+"��"+name+"��"+ "���׸���?")//������ʾ������ 
	     .setCancelable(false)       //����հ״����Ի����Զ��˳���������ؼ�Ҳ�����˳�
	     .setPositiveButton("ȷ��",new DialogInterface.OnClickListener() {//���ȷ����ť  
	         @Override  
	         public void onClick(DialogInterface dialog, int which) {//ȷ����ť����Ӧ�¼�  
	        	 // TODO Auto-generated method stub
	        	 mymusiclist.remove(del_position);
        		 //�����б�
        		 updatelist(del_position,name,minfo.getName());
        		 dialog.dismiss();
        		 //������ʾ
        		 Toast.makeText(MusicActivity.this, "ɾ���ɹ�", Toast.LENGTH_LONG).show();
        		 //�������ݿ�Ĳ����ȽϺ�ʱ��������Ҫ�����߳���ִ��
        		 threadHandler.post(dellocaldataThread);
	         }  
	  
	     }).setNegativeButton("ȡ��",new DialogInterface.OnClickListener() {//��ӷ��ذ�ť  
	         @Override  
	         public void onClick(DialogInterface dialog, int which) {//��Ӧ�¼�  
	             // TODO Auto-generated method stub 
	        	 dialog.dismiss();
	         }  
	     })
	     .create()
	     .show();//�ڰ�����Ӧ�¼�����ʾ�˶Ի���
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	//ɾ������ϲ������
	private void showDialogDeleteallfav()
	{
		 builder.setTitle("ɾ��ȫ������")  //���öԻ������
	     .setMessage("��ȷ��Ҫɾ�������б��ϲ��������?")//������ʾ������ 
	     .setCancelable(false)       //����հ״����Ի����Զ��˳���������ؼ�Ҳ�����˳�
	     .setPositiveButton("ȷ��",new DialogInterface.OnClickListener() {//���ȷ����ť  
	         @Override  
	         public void onClick(DialogInterface dialog, int which) {//ȷ����ť����Ӧ�¼�  
	        	 // TODO Auto-generated method stub
	        	 //�����б�
	        	 bufferlist = myfavmusiclist;
	        	 if(GetDataService.db != null)
	        	 {
	        		 GetDataService.db.open();
	        		 //�������ݿ��Ӧλ������
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
	        				 //���²�����ʷ��¼���ݿ��Ӧλ������
	        				 updatehistorydb(need_to_update_position);
	        			 }
	        		 }
	        		 GetHistoryService.historydb.close();
	        	 }
	        	 //threadHandler.post(delallfavdataThread);
	        	 myfavmusiclist.removeAll(myfavmusiclist);
	        	 updateOffavmusic();
	        	 dialog.dismiss();
	        	 //������ʾ
	        	 Toast.makeText(MusicActivity.this, "�Ѿ�ȫ��ɾϲ���赥���뵽���Ž���������", Toast.LENGTH_LONG).show();
	        	 //�������ݿ�Ĳ����ȽϺ�ʱ��������Ҫ�����߳���ִ��
	        	 getAllListfromdb();
	        	 mHandler.sendEmptyMessage(10);
	         }  
	  
	     }).setNegativeButton("ȡ��",new DialogInterface.OnClickListener() {//��ӷ��ذ�ť  
	         @Override  
	         public void onClick(DialogInterface dialog, int which) {//��Ӧ�¼�  
	             // TODO Auto-generated method stub 
	        	 dialog.dismiss();
	         }  
	     })
	     .create()
	     .show();//�ڰ�����Ӧ�¼�����ʾ�˶Ի���
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	//ɾ��������ʷ��¼
	private void showDialogDeleteallhistory()
	{
		 builder.setTitle("ɾ��ȫ�����ż�¼")  //���öԻ������
	     .setMessage("��ȷ��Ҫ��ղ��ż�¼�б���?")//������ʾ������ 
	     .setCancelable(false)       //����հ״����Ի����Զ��˳���������ؼ�Ҳ�����˳�
	     .setPositiveButton("ȷ��",new DialogInterface.OnClickListener() {//���ȷ����ť  
	         @Override  
	         public void onClick(DialogInterface dialog, int which) {//ȷ����ť����Ӧ�¼�  
	        	 // TODO Auto-generated method stub
	        	 //���²�����ʷ��¼�б�
	        	 myhistorymusiclist.removeAll(myhistorymusiclist);
	        	 updateOfhistorymusic();
	        	 dialog.dismiss();
	        	 //������ʾ
	        	 Toast.makeText(MusicActivity.this, "����ղ��ż�¼�б�", Toast.LENGTH_LONG).show();
   				//�������ݿ�Ĳ����ȽϺ�ʱ��������Ҫ�����߳���ִ��
   				threadHandler.post(delallhistorydataThread);
	         }  
	  
	     }).setNegativeButton("ȡ��",new DialogInterface.OnClickListener() {//��ӷ��ذ�ť  
	         @Override  
	         public void onClick(DialogInterface dialog, int which) {//��Ӧ�¼�  
	             // TODO Auto-generated method stub 
	        	 dialog.dismiss();
	         }  
	     })
	     .create()
	     .show();//�ڰ�����Ӧ�¼�����ʾ�˶Ի���
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	//ɾ������ĳһ���ֵĲ���,���ڱȽϺ�ʱ����˷���GerMusicDataService�д���
	private void del_from_local(String url)
	{
		Intent intent = new Intent();
 		intent.putExtra(ACTION_TYPE, DEL_DATA);
 		intent.putExtra("del_url", url);
 		intent.setClass(MusicActivity.this, GetDataService.class);  
 		startService(intent);
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	//ɾ��ĳһ���ֺ�����б�Ĳ���
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
		//���ɾ�������������벥�Ÿ���������ͬ����������沥��״̬��������һ��
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
				 startService(del_intent);       //��������
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
	//�����������ֶ�Ӧ�����ݿ�
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
	//������ʷ��¼���ݿ�
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
	//�����б�id�ж��Ƿ���ϲ���б���
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
	//�����б�id�ж��Ƿ�����ʷ��¼��
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
	//�������Ĳ���
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
	//���ݵ�ǰ���ֵ����ֺ�id�õ������������е�id
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
		if(temp_position == -1)  //˵�����������ж��ò�������ʱ����Ҫ����music_id���ж�
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
	//��������url�õ���ǰ���������������е�id
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
	//�����������Լ�����id�õ���ǰ��������ʷ��¼�����е�id
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
			if(temp_position == -1)  //˵�����������ж��ò�������ʱ����Ҫ����music_id���ж�
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
	//�������ֵ�url�õ���ǰ��������ʷ��¼�е�id
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
	//���±����б�����
	private void updateOflocalmusic()
	{
		if(mymusiclist != null && mymusiclist.size() > 0)
		{
			textview_no_data.setVisibility(4);
			textview_music_to_play.setVisibility(4);
			//notifyDataSetChanged����ǿ��listview����getView��ˢ��ÿ��Item������
			
			mymusiclist = filledData(mymusiclist);
			// ����a-z��������Դ����
			Collections.sort(mymusiclist, pinyinComparator);
			PlayService.mp3Infos = mymusiclist;
			
			musicListAdapter.notifyDataSetChanged(mymusiclist);
			textview_nums.setText(""+mymusiclist.size());
			
			//�Ե��ÿ��item������Ӧ�ķ�Ӧ������ܻ�����쳣������Խ��
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
	//����ϲ���б�����
	private void updateOffavmusic()
	{
		if(myfavmusiclist != null && myfavmusiclist.size() > 0)
		{
			textview_no_data.setVisibility(4);
			//notifyDataSetChanged����ǿ��listview����getView��ˢ��ÿ��Item������
			myfavmusiclist = filledData(myfavmusiclist);
			// ����a-z��������Դ����
			Collections.sort(myfavmusiclist, pinyinComparator);
			PlayService.favmusiclist = myfavmusiclist;
			bufferlist = myfavmusiclist;
			
			musicListAdapter.notifyDataSetChanged(myfavmusiclist);
			if(PlayService.mp3Infos != null && PlayService.mp3Infos.size() > 0)
			{
				textview_nums.setText(""+PlayService.mp3Infos.size());
				//�Ե��ÿ��item������Ӧ�ķ�Ӧ������ܻ�����쳣������Խ��
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
	//���²��ż�¼�б�����
	private void updateOfhistorymusic()
	{
		if(myhistorymusiclist != null && myhistorymusiclist.size() > 0)
		{
			textview_no_data.setVisibility(4);
			//notifyDataSetChanged����ǿ��listview����getView��ˢ��ÿ��Item������
			musicListAdapter.notifyDataSetChanged(myhistorymusiclist);
			if(PlayService.mp3Infos != null && PlayService.mp3Infos.size() > 0)
			{
				textview_nums.setText(""+PlayService.mp3Infos.size());
				//�Ե��ÿ��item������Ӧ�ķ�Ӧ������ܻ�����쳣������Խ��
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
	 * ��ȡÿ�׸������ĸ
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
			//����ת����ƴ��
			String pinyin = characterParser.getSelling(sortStr);
			String sortString = pinyin.substring(0, 1).toUpperCase();
			// ������ʽ���ж�����ĸ�Ƿ���Ӣ����ĸ
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
	// ��ʾNotification
	///////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean onTouch(View view, MotionEvent event) {
		// TODO Auto-generated method stub
		if(event.getAction() == MotionEvent.ACTION_DOWN)
		{
			switch(view.getId())
			{
			case R.id.music_local_resource:
				//��ʾ��������ϲ���б�
        		showmusiclist();
				break;
				
			case R.id.music_favorite:
				//��ʾϲ���б�
        		showfavlist();
				break;
				
			case R.id.music_history:
				//��ʾ��ʷ��¼�б�
        		showhistorylist();
				break;
			case R.id.paly_music_album:
			case R.id.music_about_layout:
				//��ת�����Ž���
        		to_play_activity();
				break;
				
			case R.id.play_on_music:
				//��ͣ�򲥷�
				play_pause_music();
				break;
				
			case R.id.play_next_music:
				//��һ��
				play_next_music();
				break;
				
			case R.id.play_previous_music:
				//��һ��
				play_previous_music();
				break;
				
			case R.id.title_button_id:
				//ֱ��������һ�����
				//�����л�����������߽��룬�ұ��˳����˷����������startActivity����finish()֮��
				finish();  
				overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
				break;
				
			case R.id.page_title_more_info:
				//����ѡ�񱳾��Ի���
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
	//�㲥�����������½���
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
     * �̳�GestureListener����дleft��right���� 
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
        		//��ʾϲ���б�
        		showfavlist();
        	}
        	else if(list_type == 1)
        	{
        		//��ʾ��ʷ��¼�б�
        		showhistorylist();
        	}
        	else if(list_type == 2)
        	{
        		//��ת�����Ž���
        		to_play_activity();
        	}
            return super.left();  
        }  
  
        @Override  
        public boolean right() {
        	if(list_type == 0)
        	{
        		//��ת��Home����
        		finish();  //ֱ��������һ�����
				//�����л�����������߽��룬�ұ��˳����˷����������startActivity����finish()֮��
				overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
        	}
        	else if(list_type == 1)
        	{
        		//��ʾ�����б�
        		showmusiclist();
        	}
        	else if(list_type == 2)
        	{
        		//��ʾϲ���б�
        		showfavlist();
        	}
            return super.right();  
        }  
    }
    
    ///////////////////////////////////////////////////////////////////////////////////////
    //��ʾϲ���б�Ĳ���
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
    //��ʾ���������б�Ĳ���
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
    //��ʾ���ż�¼�б�Ĳ���
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
   //��ת�����Ž���Ĳ���
    private void to_play_activity()
    {
    	if(mymusiclist == null || mymusiclist.size() == 0)
		{
			Toast.makeText(MusicActivity.this,"�������ݣ���������������Ӹ�����", Toast.LENGTH_SHORT).show();
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
				//�����л����������ұ߽��룬����˳����˷����������startActivity����finish()֮��
				overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
			}
		}
    }
    //////////////////////////////////////////////////////////////////////////////////////////
    //��ͣ��ָ����ֲ��ŵĲ���
    private void play_pause_music()
    {
    	if(mymusiclist == null || mymusiclist.size() == 0)
		{
			Toast.makeText(this,"�������ݣ���������������Ӹ�����", Toast.LENGTH_SHORT).show();
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
				startService(play_stop_intent);       //��������
				
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
							//mLyricView.setSentenceEntities(PlayService.LyricList);//���ø����Դ������ʾ
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
    //��ʾѡ�񱳾��Ի���Ĳ���
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
				//��ת���������ý���
				Intent intent = new Intent();
				intent.setClass(MusicActivity.this, BackgroundSettingActivity.class);
				startActivity(intent);
				//�����л����������ұ߽��룬����˳����˷����������startActivity����finish()֮��
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
    //��ʾ��һ�׸�Ĳ���
    private void play_next_music()
    {
    	if(mymusiclist == null || mymusiclist.size() == 0)
		{
			Toast.makeText(this,"�������ݣ���������������Ӹ�����", Toast.LENGTH_SHORT).show();
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
    //��ʾ��һ�׸�Ĳ���
    private void play_previous_music()
    {
    	if(mymusiclist == null || mymusiclist.size() == 0)
		{
			Toast.makeText(this,"�������ݣ���������������Ӹ�����", Toast.LENGTH_SHORT).show();
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
    //�߳�:�������б��У�ɾ�������б�����
    Runnable delmusicdataThread= new Runnable() {
    	public void run() {
    		//�жϵ�ǰɾ���������Ƿ���ϲ���б���߲��ż�¼�б�
     		if(isFav(del_url))
     		{
     			myfavmusiclist.remove(update_position);
     		}
     		
     		if(ishistory(del_url))
     		{
     			//��ʷ��¼���ݿ�ɾ��
     			if(GetHistoryService.historydb != null)
     			{
     				GetHistoryService.historydb.open();
     				GetHistoryService.historydb.deleteTitle(del_position_in_history);
     				GetHistoryService.historydb.close();
     			}
     			//���²�����ʷ��¼�б�
     			if(GetHistoryService.musiclist != null)
     			{
     				GetHistoryService.musiclist.remove(update_position_in_history);
     			}
     			mHandler.sendEmptyMessage(10);
     		}
     		//ִ��ɾ����������SQL���
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
    //�߳�:ɾ������ϲ���б�����
    Runnable delallfavdataThread= new Runnable() {
    	public void run() {
    		
    		//�����ǰɾ��������Ҳ�ڲ��ż�¼�����Ҫ���²��ż�¼���ݿ��Ӧ����
    		if(GetHistoryService.historydb != null)
    		{
    			GetHistoryService.historydb.open();
    			for(MusicInfo mfavinfo : bufferlist)
    			{
    				int need_to_update_position = getpositionInhistorylist(mfavinfo.getUrl());
    				if(need_to_update_position != -1)
    				{
    					//���²�����ʷ��¼���ݿ��Ӧλ������
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
    //�߳�:ɾ������ϲ������
    Runnable delonefavdataThread= new Runnable() {
    	public void run() {
    		//ɾ����ʱ����Ҫ�жϵ�ǰɾ���������Ƿ��ڲ����б���Ҳ���ڣ�������ڣ���Ҫ���£�ͬʱ��Ҫ���������б��ж�Ӧ������
    		mHandler.sendEmptyMessage(10);
    		 if(GetDataService.db != null)
     		{
    			GetDataService.db.open();
     			//�������ݿ��Ӧλ������
     			updatedb(getpositionInalllist(del_url));
     			GetDataService.db.close();
     		}
     		//�����ǰɾ��������Ҳ�ڲ��ż�¼�����Ҫ���²��ż�¼���ݿ��Ӧ����
     		if(GetHistoryService.historydb != null)
     		{
     			GetHistoryService.historydb.open();
     			int need_to_update_position = getpositionInhistorylist(del_url);
     			if(need_to_update_position != -1)
     			{
     				//���²�����ʷ��¼���ݿ��Ӧλ������
     				updatehistorydb(need_to_update_position);
     			}
     			GetHistoryService.historydb.close();
     		}
     		//����֮������������
     		getAllListfromdb();
     		mHandler.sendEmptyMessage(10);
    	}
    };
    ///////////////////////////////////////////////////////////////////////////////////////////
    //�߳�:ɾ���������ż�¼����
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
    //�߳�:������ʷ��¼����
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
    //�߳�:ɾ������������������
    Runnable dellocaldataThread= new Runnable() {
    	public void run() {
    		//�жϵ�ǰɾ���������Ƿ���ϲ���б���߲��ż�¼�б�
     		if(isFav(del_url))
     		{
     			myfavmusiclist.remove(update_position);
     		}
     		
     		if(ishistory(del_url))
     		{
     			//��ʷ��¼���ݿ�ɾ��
     			if(GetHistoryService.historydb != null)
     			{
     				GetHistoryService.historydb.open();
     				GetHistoryService.historydb.deleteTitle(del_position_in_history);
     				GetHistoryService.historydb.close();
     			}
     			//���²�����ʷ��¼�б�
     			if(GetHistoryService.musiclist != null)
     			{
     				GetHistoryService.musiclist.remove(update_position_in_history);
     			}
     			mHandler.sendEmptyMessage(10);
     		}
     		//ִ��ɾ����������SQL���
    		if(GetDataService.db != null)
    		{
    			GetDataService.db.open();
    			GetDataService.db.deleteTitle(del_id);
    			Toast.makeText(MusicActivity.this, "���ɾ���ɹ���", Toast.LENGTH_LONG).show();
    			GetDataService.db.close();
    		}
    		getAllListfromdb();
	        //�����ڱ��������б����ɾ������
	        del_from_local(del_url);
    	}
    };
    ///////////////////////////////////////////////////////////////////////////////////////////
}
