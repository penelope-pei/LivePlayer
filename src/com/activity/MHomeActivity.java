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
	
	private String[] texts = {"����","��Ƶ","����"};  //"��ʷ",
	private Integer[] images = {R.drawable.music, R.drawable.video, R.drawable.setting};  //R.drawable.history, 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);//ȥ��������	
		/*// ������ɾ��֮ǰ���Ƕ����֪ͨ   
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
		
        galleryFlow.setFadingEdgeLength(0);//���ñ߿򽥱���  
        galleryFlow.setSpacing(-100); //ͼƬ֮��ļ��
        
        ImageAdapter adapter = new ImageAdapter(this, images);
        adapter.createReflectedImages();//������ӰЧ��
        galleryFlow.setAdapter(adapter);
        
        galleryFlow.setOnItemClickListener(this);
        galleryFlow.setOnItemSelectedListener(this);
        galleryFlow.setSelection(clickindex);//gallery�������ͼ���б��0λ�ÿ�ʼ��ʾ��
        //tv_show_type.setText(texts[clickindex]);
        
        //���һ����л�����
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
			    //��ֵ���ж���תҳ�棬��ʾ���Ӧ��string
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
			   startActivity(new Intent(Settings.ACTION_SETTINGS));//�ֻ�ϵͳ����
			    /*intent.putExtra(TYPE, position);
			    startActivity(intent);*/
			    break;
		}
		//�����л����������ұ߽��룬����˳����˷����������startActivity����finish()֮��
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
	 * �̳�GestureListener����дleft��right���� 
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
				    //��ֵ���ж���תҳ�棬��ʾ���Ӧ��string
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
			//�����л����������ұ߽��룬����˳����˷����������startActivity����finish()֮��
			overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
			return super.left();  
		}  
		
		@Override  
		public boolean right() {
			return super.right();  
		}  
	}
	
}
