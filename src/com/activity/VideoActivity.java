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
					Toast.makeText(VideoActivity.this,"正在获取数据,请耐心等待...", Toast.LENGTH_SHORT).show();
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
		requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏		
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
		
		//左右滑动切换内容
		video_gv.setLongClickable(true);
		video_gv.setOnTouchListener(new MyGestureListener(this));
		
		videoListAdapter = new VideoListAdapter(this);
		video_gv.setAdapter(videoListAdapter);
		
		//初始化gridview
		initdata();
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	private void initdata()
	{
		//设置背景图片，根据GetDataService这个服务中的background_id来判断显示哪种背景
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
		else  //这是从服务拿，为了防止有时候服务重新启动，数据为空，应该在加上一个判断条件，从数据库拿,但是如果服务不那么容易被杀死，那么就不存在该问题
		{
			//提示正在获取数据
			mHandler.sendEmptyMessage(2);
			//启动服务,获取视频资源
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
		//设置背景图片，根据GetDataService这个服务中的background_id来判断显示哪种背景
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
			//提示正在获取数据
			mHandler.sendEmptyMessage(2);
		}
		//注册一个广播，主要是用于通知MusicActivity更新界面
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
			//设置切换动画，从左边进入，右边退出，此方法必须放在startActivity或者finish()之后
			overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
			break;
		case R.id.video_set_background:
			//弹出选择背景对话框
			show_background_dialog();
			break;
		}
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	//onItemClick
	@Override
	public void onItemClick(AdapterView<?> adapterview, View view, int position, long arg3) {
		// TODO Auto-generated method stub
		
		//判断Music是否在播放音乐，如果在播放音乐，则停止播放音乐
		isMusicPlaying();
		
		//跳转到播放界面
		Intent nouseintent = new Intent(this, VideoPlayActivity.class);  //NoUseActivity
		nouseintent.putExtra(VIDEO_PATH, video_list.get(position).getVideoPath());
		nouseintent.putExtra(VIDEO_NAME, video_list.get(position).getVideoName());
		nouseintent.putExtra(VIDEO_DURATION, video_list.get(position).getVideoDuration());
		nouseintent.putExtra(VIDEO_ALBUM, video_list.get(position).getThumbnail());
		startActivity(nouseintent);
		//设置切换动画，从右边进入，左边退出，此方法必须放在startActivity或者finish()之后
		overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	//判断是否有音乐正在播放
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
			// 停止音乐可视化界面动画
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
		//长按删除
		clickposition = position;
		showDialogDeletevideo();
		return true;
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
				mHandler.removeMessages(2);
				Intent intent = new Intent();
				intent.setClass(VideoActivity.this, BackgroundSettingActivity.class);
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
	//广播接收器，更新界面
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
			mHandler.removeMessages(2);
			Intent intent = new Intent(VideoActivity.this,MHomeActivity.class);
		    startActivity(intent);
			finish();
			//设置切换动画，从左边进入，右边退出，此方法必须放在startActivity或者finish()之后
			overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
			return super.right();  
		}  
	}
	//////////////////////////////////////////////////////////////////////////////////////////
	//删除视频
	private void showDialogDeletevideo()
	{
		builder.setTitle("删除视频")  //设置对话框标题
		.setMessage("你确定要删除视频: "+video_list.get(clickposition).getVideoName()+" 吗?")//设置显示的内容 
		.setCancelable(false)       //点击空白处，对话框不自动退出，点击返回键也不会退出
		.setPositiveButton("确定",new DialogInterface.OnClickListener() {//添加确定按钮  
			@Override  
			public void onClick(DialogInterface dialog, int which) {//确定按钮的响应事件  
				// TODO Auto-generated method stub
				if(GetDataService.allVideoList != null && GetDataService.allVideoList.size() > 0)
				{
					GetDataService.allVideoList.remove(clickposition);
					video_list = new ArrayList<VideoInfo>();
					video_list = GetDataService.allVideoList;
					GetDataService.video_num = video_list.size();
					videoListAdapter.notifyDataSetChanged(video_list);
					dialog.dismiss();
					Toast.makeText(VideoActivity.this, "已把该视频移出列表", Toast.LENGTH_LONG).show();
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
	//test
	////////////////////////////////////////////////////////////////////////////////////

}
