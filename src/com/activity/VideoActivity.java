package com.activity;

import java.util.ArrayList;
import java.util.List;

import com.activity.adapter.VideoListAdapter;
import com.activity.dialog.CommonDialog;
import com.activity.info.VideoInfo;
import com.activity.message.NotificationMsg;
import com.activity.service.GetDataService;
import com.activity.service.PlayService;
import com.activity.utils.GestureListener;
import com.example.liveplayer.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class VideoActivity extends Activity implements OnClickListener, OnItemClickListener, OnItemLongClickListener{
    
	public static String VIDEO_DURATION = "vduration";
	public static String VIDEO_NAME = "vname";
	public static String VIDEO_PATH = "vpath";
	public static String VIDEO_ALBUM = "bitmap";
	
	private String ACTION_TYPE = "action_type";
	private String GET_VIDEO_DATA = "get_vedio_data";
	private int clickposition = -1; 
	
	private RelativeLayout rl_back_title;
    private TextView iv_back;
    private TextView textview_title;
    private TextView textview_num;
    private TextView textview_set_background;
    private TextView textview_no_data;
    private GridView video_gv;
    private Intent intent;
    private int activity_type;
    
    private List<VideoInfo> video_list = null;
    private CommonDialog show_user_dialog;
    private VideoListAdapter videoListAdapter;
    private AlertDialog.Builder builder = null;
    
    private Handler mHandler = new Handler()
    {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch(msg.what)
			{
			case 1:
				Intent intent = new Intent();
				intent.putExtra(ACTION_TYPE, GET_VIDEO_DATA);
				intent.setClass(VideoActivity.this, GetDataService.class);  
				startService(intent);
				break;
			case 2:
				if(!GetDataService.hasgetvideodata)
				{
					Toast.makeText(VideoActivity.this,"���ڻ�ȡ����,�����ĵȴ�...", Toast.LENGTH_SHORT).show();
				}
    			break;
			}
			mHandler.sendEmptyMessageDelayed(2, 5000);
			super.handleMessage(msg);
		}
    };
    ///////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////
    //onCreate
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);		
		requestWindowFeature(Window.FEATURE_NO_TITLE);//ȥ��������		
		setContentView(R.layout.activity_video);
		
		video_list = new ArrayList<VideoInfo>();
		
		iv_back = (TextView)findViewById(R.id.video_title_button_id);
		textview_title = (TextView)findViewById(R.id.video_page_title_name);
		textview_num = (TextView)findViewById(R.id.video_num);
		textview_set_background = (TextView)findViewById(R.id.video_set_background);
		textview_no_data = (TextView)findViewById(R.id.no_video_data);
		rl_back_title = (RelativeLayout)findViewById(R.id.Relative_video);
		video_gv = (GridView)findViewById(R.id.video_gridview);
		
		iv_back.setOnClickListener(this);
		textview_set_background.setOnClickListener(this);
		video_gv.setOnItemClickListener(this);
		video_gv.setOnItemLongClickListener(this);
		
		//���һ����л�����
		video_gv.setLongClickable(true);
		video_gv.setOnTouchListener(new MyGestureListener(this));
		
		videoListAdapter = new VideoListAdapter(this);
		video_gv.setAdapter(videoListAdapter);
		
		//��ʼ��gridview
		initdata();
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	private void initdata()
	{
		//���ñ���ͼƬ������GetDataService��������е�background_id���ж���ʾ���ֱ���
		if(GetDataService.background_id == -1)
		{
			rl_back_title.setBackgroundResource(BackgroundSettingActivity.imgs[0]);
		}
		else
		{
			rl_back_title.setBackgroundResource(BackgroundSettingActivity.imgs[GetDataService.background_id]);
		}
		
		
		if(GetDataService.allVideoList != null && GetDataService.allVideoList.size() > 0)
		{
			video_list = GetDataService.allVideoList;
		}
		else  //���Ǵӷ����ã�Ϊ�˷�ֹ��ʱ�������������������Ϊ�գ�Ӧ���ڼ���һ���ж������������ݿ���,�������������ô���ױ�ɱ������ô�Ͳ����ڸ�����
		{
			//��ʾ���ڻ�ȡ����
			mHandler.sendEmptyMessage(2);
			//��������,��ȡ��Ƶ��Դ
			mHandler.sendEmptyMessage(1);
		}
		if(video_list.size() == 0)
		{
			textview_no_data.setVisibility(0);
			videoListAdapter.notifyDataSetChanged(null);
		}
		else
		{
			textview_no_data.setVisibility(4);
			videoListAdapter.notifyDataSetChanged(video_list);
		}
		
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	//onStart
	protected void onStart(){
		super.onStart();
		intent = this.getIntent();
		activity_type = intent.getIntExtra(MHomeActivity.TYPE,-1);
		if(activity_type == 1)
		{
			textview_title.setText(this.getResources().getString(R.string.icon_video));
		}
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	//onResume
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		//���ñ���ͼƬ������GetDataService��������е�background_id���ж���ʾ���ֱ���
		if(GetDataService.background_id == -1)
		{
			rl_back_title.setBackgroundResource(BackgroundSettingActivity.imgs[0]);
		}
		else
		{
			rl_back_title.setBackgroundResource(BackgroundSettingActivity.imgs[GetDataService.background_id]);
		}
		
		if(video_list.size() == 0)
		{
			//��ʾ���ڻ�ȡ����
			mHandler.sendEmptyMessage(2);
		}
		//ע��һ���㲥����Ҫ������֪ͨMusicActivity���½���
    	IntentFilter UpdateMusicFilter = new IntentFilter();  
    	UpdateMusicFilter.addAction(NotificationMsg.NOTIFICATION_UPDATE_VIDEO);
        registerReceiver(updateReceiver,UpdateMusicFilter);
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		mHandler.removeMessages(2);
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	//onClick
	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		switch(view.getId())
		{
		case R.id.video_title_button_id:
			mHandler.removeMessages(2);
			Intent intent = new Intent(VideoActivity.this,MHomeActivity.class);
		    startActivity(intent);
			finish();
			//�����л�����������߽��룬�ұ��˳����˷����������startActivity����finish()֮��
			overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
			break;
		case R.id.video_set_background:
			//����ѡ�񱳾��Ի���
			show_background_dialog();
			break;
		}
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	//onItemClick
	@Override
	public void onItemClick(AdapterView<?> adapterview, View view, int position, long arg3) {
		// TODO Auto-generated method stub
		
		//�ж�Music�Ƿ��ڲ������֣�����ڲ������֣���ֹͣ��������
		isMusicPlaying();
		
		//��ת�����Ž���
		Intent nouseintent = new Intent(this, VideoPlayActivity.class);  //NoUseActivity
		nouseintent.putExtra(VIDEO_PATH, video_list.get(position).getVideoPath());
		nouseintent.putExtra(VIDEO_NAME, video_list.get(position).getVideoName());
		nouseintent.putExtra(VIDEO_DURATION, video_list.get(position).getVideoDuration());
		nouseintent.putExtra(VIDEO_ALBUM, video_list.get(position).getThumbnail());
		startActivity(nouseintent);
		//�����л����������ұ߽��룬����˳����˷����������startActivity����finish()֮��
		overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	//�ж��Ƿ����������ڲ���
	private void isMusicPlaying()
	{
		if(PlayService.mediaPlayer != null && PlayService.mediaPlayer.isPlaying())
		{
			PlayService.mediaPlayer.pause();
			if(MusicPlay.tv_play_stop != null)
    		{
				MusicPlay.tv_play_stop.setBackgroundResource(R.drawable.player_pause_normal);
    		}
			if(MusicActivity.btn_play_stop != null)
    		{
    			MusicActivity.btn_play_stop.setBackgroundResource(R.drawable.player_pause_normal);
    		}
			// ֹͣ���ֿ��ӻ����涯��
    		if(MusicPlay.musicvisualizer != null)
    		{
    			MusicPlay.musicvisualizer.clearAnimation();
    			MusicPlay.musicvisualizer.releaseVisualizerFx();
    		}
			MusicActivity.isPlay = false;
			MusicPlay.isplay = false;
			GetDataService.firstclick = false;
		}
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	//onItemLongClick
	@Override
	public boolean onItemLongClick(AdapterView<?> adapterview, View view, int position,
			long arg3) {
		// TODO Auto-generated method stub
		builder = new Builder(VideoActivity.this);
		//����ɾ��
		clickposition = position;
		showDialogDeletevideo();
		return true;
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	//��ʾ����ѡ���Ի���
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
				//��ת���������ý���
				mHandler.removeMessages(2);
				Intent intent = new Intent();
				intent.setClass(VideoActivity.this, BackgroundSettingActivity.class);
				startActivity(intent);
				//�����л����������ұ߽��룬����˳����˷����������startActivity����finish()֮��
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
	//�㲥�����������½���
	BroadcastReceiver updateReceiver = new BroadcastReceiver() {
		
		@Override  
		public void onReceive(Context context, Intent intent) {
			
			
			if(intent.getAction().equals(NotificationMsg.NOTIFICATION_UPDATE_VIDEO))
			{ 
				video_list = GetDataService.allVideoList;
				if(video_list.size() == 0)
				{
					textview_no_data.setVisibility(0);
					videoListAdapter.notifyDataSetChanged(null);
				}
				else
				{
					textview_no_data.setVisibility(4);
					videoListAdapter.notifyDataSetChanged(video_list);
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
			return super.left();  
		}  
		
		@Override  
		public boolean right() {
			mHandler.removeMessages(2);
			Intent intent = new Intent(VideoActivity.this,MHomeActivity.class);
		    startActivity(intent);
			finish();
			//�����л�����������߽��룬�ұ��˳����˷����������startActivity����finish()֮��
			overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
			return super.right();  
		}  
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	//ɾ����Ƶ
	private void showDialogDeletevideo()
	{
		builder.setTitle("ɾ����Ƶ")  //���öԻ������
		.setMessage("��ȷ��Ҫɾ����Ƶ: "+video_list.get(clickposition).getVideoName()+" ��?")//������ʾ������ 
		.setCancelable(false)       //����հ״����Ի����Զ��˳���������ؼ�Ҳ�����˳�
		.setPositiveButton("ȷ��",new DialogInterface.OnClickListener() {//���ȷ����ť  
			@Override  
			public void onClick(DialogInterface dialog, int which) {//ȷ����ť����Ӧ�¼�  
				// TODO Auto-generated method stub
				if(GetDataService.allVideoList != null && GetDataService.allVideoList.size() > 0)
				{
					GetDataService.allVideoList.remove(clickposition);
					video_list = new ArrayList<VideoInfo>();
					video_list = GetDataService.allVideoList;
					GetDataService.video_num = video_list.size();
					videoListAdapter.notifyDataSetChanged(video_list);
					dialog.dismiss();
					Toast.makeText(VideoActivity.this, "�ѰѸ���Ƶ�Ƴ��б�", Toast.LENGTH_LONG).show();
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
	//test
	////////////////////////////////////////////////////////////////////////////////////

}
