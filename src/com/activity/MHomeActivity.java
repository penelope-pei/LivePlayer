package com.activity;

import com.activity.adapter.BackGroundAdapter;
import com.activity.adapter.ImageAdapter;
import com.activity.service.GetDataService;
import com.activity.service.PlayService;
import com.activity.utils.GestureListener;
import com.activity.view.GalleryFlow;
import com.example.liveplayer.R;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

public class MHomeActivity extends Activity implements OnItemClickListener,OnItemSelectedListener{
	
	private int clickindex = 0;
	public static String TYPE = "activity_type";
	
	private TextView tv_show_type;
	private TextView screen_textview;
	private GalleryFlow galleryFlow;
	private RelativeLayout relativelayout_home;
	
	private String[] texts = {"音乐","视频","设置"};  //"历史",
	private Integer[] images = {R.drawable.music, R.drawable.video, R.drawable.setting};  //R.drawable.history, 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏	
		/*// 启动后删除之前我们定义的通知   
        NotificationManager notificationManager = (NotificationManager) this  
                .getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(0);*/
		setContentView(R.layout.activity_mhome);
		clickindex = 0;
		
		tv_show_type = (TextView)findViewById(R.id.type_textview);
		screen_textview = (TextView)findViewById(R.id.to_music_textview);
        galleryFlow = (GalleryFlow) this.findViewById(R.id.galleryview);
        relativelayout_home = (RelativeLayout)findViewById(R.id.relativelayout_mhome);
        
        if(GetDataService.background_id == -1)
		{
        	relativelayout_home.setBackgroundResource(BackgroundSettingActivity.imgs[0]);
		}
        else
        {
        	relativelayout_home.setBackgroundResource(BackgroundSettingActivity.imgs[GetDataService.background_id]);
        }
		
        galleryFlow.setFadingEdgeLength(0);//设置边框渐变宽度  
        galleryFlow.setSpacing(-100); //图片之间的间距
        
        ImageAdapter adapter = new ImageAdapter(this, images);
        adapter.createReflectedImages();//创建倒影效果
        galleryFlow.setAdapter(adapter);
        
        galleryFlow.setOnItemClickListener(this);
        galleryFlow.setOnItemSelectedListener(this);
        galleryFlow.setSelection(clickindex);//gallery组件跳到图标列表的0位置开始显示。
        //tv_show_type.setText(texts[clickindex]);
        
        //左右滑动切换内容
        screen_textview.setLongClickable(true);
        screen_textview.setOnTouchListener(new MyGestureListener(this));
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if(GetDataService.background_id == -1)
		{
        	relativelayout_home.setBackgroundResource(BackgroundSettingActivity.imgs[0]);
		}
        else
        {
        	relativelayout_home.setBackgroundResource(BackgroundSettingActivity.imgs[GetDataService.background_id]);
        }
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}


	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int position, long l) {
		// TODO Auto-generated method stub
		clickindex = position;
		tv_show_type.setText(texts[clickindex]);
		Intent intent;
		Log.e("info", "------------------------------------0====="+position);
		switch(position)
		{
		   case 0:
			 	
			    intent = new Intent(this,MusicActivity.class);
			    //传值，判断跳转页面，显示相对应的string
			    intent.putExtra(TYPE, position);			    
			    startActivity(intent);
			    //Toast.makeText(this,"music",Toast.LENGTH_SHORT).show();
			    break;
		   case 1:
			  
			    intent = new Intent(this,VideoActivity.class);
			    intent.putExtra(TYPE, position);
			    startActivity(intent);
			    break;
		   case 2:
			   startActivity(new Intent(Settings.ACTION_SETTINGS));//手机系统设置
			    /*intent.putExtra(TYPE, position);
			    startActivity(intent);*/
			    break;
		}
		//设置切换动画，从右边进入，左边退出，此方法必须放在startActivity或者finish()之后
		overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
	}

	@Override
	public void onItemSelected(AdapterView<?> adapter, View view, int position,
			long l) {
		// TODO Auto-generated method stub
		clickindex = position;
		tv_show_type.setText(texts[clickindex]);
		Log.e("info", "------------------------------------3====="+clickindex);
	}

	@Override
	public void onNothingSelected(AdapterView<?> adapter) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if(event.getAction() == KeyEvent.ACTION_DOWN)
		{
			switch(keyCode)
			{
			case KeyEvent.KEYCODE_BACK:
				break;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	
	/** 
	 * 继承GestureListener，重写left和right方法 
	 */  
	private class MyGestureListener extends GestureListener {  
		public MyGestureListener(Context context) {
			super(context);  
		}  
		@Override  
		public boolean left() {
			Intent intent;
			switch(clickindex)
			{
			   case 0:
				    intent = new Intent(MHomeActivity.this,MusicActivity.class);
				    //传值，判断跳转页面，显示相对应的string
				    intent.putExtra(TYPE, clickindex);			    
				    startActivity(intent);
				    //Toast.makeText(this,"music",Toast.LENGTH_SHORT).show();
				    break;
			   case 1:
				    intent = new Intent(MHomeActivity.this,VideoActivity.class);
				    intent.putExtra(TYPE, clickindex);
				    startActivity(intent);
				    //finish();
				    break;
			   case 2:
				   startActivity(new Intent(Settings.ACTION_SETTINGS));
				    /*intent.putExtra(TYPE, position);
				    startActivity(intent);*/
				    break;
			}
			//设置切换动画，从右边进入，左边退出，此方法必须放在startActivity或者finish()之后
			overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
			return super.left();  
		}  
		
		@Override  
		public boolean right() {
			return super.right();  
		}  
	}
	
}
