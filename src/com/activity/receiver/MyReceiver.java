package com.activity.receiver;

import com.activity.MusicActivity;
import com.activity.MusicPlay;
import com.activity.VideoPlayActivity;
import com.activity.service.GetDataService;
import com.activity.service.PlayService;
import com.example.liveplayer.R;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

//广播接收器
public class MyReceiver extends BroadcastReceiver{
	private Context mContext;
	private String mAction;
	private Intent mIntent;
	
	public MyReceiver()
	{
		
	}
	public MyReceiver(Context context)
	{
		super();
		mContext = context;
	}
	
	@Override
	public void onReceive(Context context, Intent intent) 
	{
		mAction = intent.getAction();
		if(mAction.equals(Intent.ACTION_NEW_OUTGOING_CALL))   //电话监听
		{
			//去电
			if(PlayService.mediaPlayer != null && PlayService.mediaPlayer.isPlaying())
			{
				PlayService.mediaPlayer.pause();
				// 停止音乐可视化界面动画
        		if(MusicPlay.musicvisualizer != null)
        		{
        			MusicPlay.musicvisualizer.clearAnimation();
        			MusicPlay.musicvisualizer.releaseVisualizerFx();
        		}
			}
			
			if (VideoPlayActivity.vv_play != null && VideoPlayActivity.vv_play.isPlaying()) 
			{  
				VideoPlayActivity.vv_play.pause();
				VideoPlayActivity.isplay = false;
				VideoPlayActivity.isPause = true; 
				VideoPlayActivity.isFirstClick = false;
				VideoPlayActivity.tv_pause_play.setVisibility(0);
				VideoPlayActivity.tv_control_pause_play.setBackgroundResource(R.drawable.ktv_dynamic_play_src);
			}
		}
		else if(mAction.equals(Intent.ACTION_HEADSET_PLUG))  //耳机监听
		{
			//监听耳机的拔出和插入
			if (intent.hasExtra("state"))
			{  
				if (intent.getIntExtra("state", 0) == 0)  //耳机拔出
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
				else if (intent.getIntExtra("state", 0) == 1)  //耳机插入
				{
					
				}  
			}
		}
		else
		{
			//来电
			TelephonyManager manager = (TelephonyManager) context.getSystemService(Service.TELEPHONY_SERVICE);// 获取系统服务
			manager.listen(stateListener, PhoneStateListener.LISTEN_CALL_STATE);
		}
	}
	//////////////////////////////////////////////////////////////////////////////////////////////
	PhoneStateListener stateListener = new PhoneStateListener(){
		
		public void onCallStateChanged(int state, String incomingNumber) {
			super.onCallStateChanged(state, incomingNumber);
			switch(state){
			case TelephonyManager.CALL_STATE_IDLE:
				//挂断
				if(PlayService.mediaPlayer != null)
				{
					if(PlayService.isplaying)  //记录之前的状态
					{
						PlayService.mediaPlayer.start();
						PlayService.isplaying = true;
						// 启动更新音乐可视化界面动画
		        		if(MusicPlay.musicvisualizer != null)
		        		{
		        			MusicPlay.musicvisualizer.setupVisualizerFx(PlayService.mediaPlayer.getAudioSessionId());
		        		}
					}
				}
				break;
			case TelephonyManager.CALL_STATE_OFFHOOK:
				//接听
			case TelephonyManager.CALL_STATE_RINGING:
				//响铃
				if(PlayService.mediaPlayer != null && PlayService.mediaPlayer.isPlaying())
				{
					PlayService.mediaPlayer.pause();
					// 停止音乐可视化界面动画
	        		if(MusicPlay.musicvisualizer != null)
	        		{
	        			MusicPlay.musicvisualizer.clearAnimation();
	        			MusicPlay.musicvisualizer.releaseVisualizerFx();
	        		}
				}
				if (VideoPlayActivity.vv_play != null && VideoPlayActivity.vv_play.isPlaying()) 
				{  
					VideoPlayActivity.vv_play.pause();
					VideoPlayActivity.isplay = false;
					VideoPlayActivity.isPause = true; 
					VideoPlayActivity.isFirstClick = false;
					VideoPlayActivity.tv_pause_play.setVisibility(0);
					VideoPlayActivity.tv_control_pause_play.setBackgroundResource(R.drawable.ktv_dynamic_play_src);
				}
				break;
			}
		};
	}; 
}
