package com.activity;

import java.util.ArrayList;
import java.util.List;

import com.activity.adapter.BackGroundAdapter;
import com.activity.service.GetDataService;
import com.activity.utils.LruMemoryCache;
import com.example.liveplayer.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class BackgroundSettingActivity extends Activity implements OnItemClickListener, OnClickListener{

	private int pre_position = -1;
	
	private TextView tv_return;
	private GridView gv_background;
	private RelativeLayout rl_background;
	
	private BackGroundAdapter backGroundAdapter;
	private LruMemoryCache mMemoryCache;
	
	public static int[] imgs = {R.drawable.background,R.drawable.background2,R.drawable.background3,
		R.drawable.background4,R.drawable.background5,R.drawable.background6,R.drawable.background7,
		R.drawable.background8,R.drawable.background9,R.drawable.background10,R.drawable.background11,
		R.drawable.background12,R.drawable.background13,R.drawable.background14,R.drawable.background15,
		R.drawable.background16,R.drawable.background17,R.drawable.background18,R.drawable.background19,
		R.drawable.background20,R.drawable.background21,R.drawable.background22,R.drawable.background23,
		R.drawable.background24,R.drawable.background25,R.drawable.background26,R.drawable.background27,
		R.drawable.background28,R.drawable.background29,R.drawable.background30,R.drawable.background31,
		R.drawable.background32,R.drawable.background33};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);//ȥ��������
		setContentView(R.layout.background_setting);
		
		pre_position = GetDataService.background_id;
		initLruCache();
		
		initView();
		
	}
	
	private void initView()
	{
		tv_return = (TextView)findViewById(R.id.background_setting_return);
		rl_background = (RelativeLayout)findViewById(R.id.background_settings);
		gv_background = (GridView)findViewById(R.id.background_gridview);
		
		backGroundAdapter = new BackGroundAdapter(this,mMemoryCache,imgs);
		gv_background.setAdapter(backGroundAdapter);
		
		tv_return.setOnClickListener(this);
		gv_background.setOnItemClickListener(this);
		
		if(GetDataService.background_id == -1)
		{
			rl_background.setBackgroundResource(imgs[0]);
		}
		else
		{
			rl_background.setBackgroundResource(imgs[GetDataService.background_id]);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int position, long arg3) {
		// TODO Auto-generated method stub
		if(pre_position != position)
		{
			rl_background.setBackgroundResource(imgs[position]);
			pre_position = position;
			GetDataService.background_id = position;
		}
	}

	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		if(view.getId() == R.id.background_setting_return)
		{
			finish();
			//�����л�����������߽��룬�ұ��˳����˷����������startActivity����finish()֮��
			overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
		}
	}
	
	private void initLruCache() {  
        // ��ȡ�������ڴ�����ֵ��ʹ���ڴ泬�����ֵ������OutOfMemory�쳣��  
        // LruCacheͨ�����캯�����뻺��ֵ����KBΪ��λ��  
        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);  
        // ʹ���������ڴ�ֵ��1/8��Ϊ����Ĵ�С��  
        int cacheSize = maxMemory / 8;  
        mMemoryCache = new LruMemoryCache(cacheSize);  
    }
}
