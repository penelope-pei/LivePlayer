package com.activity.adapter;


import java.util.List;

import com.example.liveplayer.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

//音乐列表Adapter设置
public class MusicPlayModeAdapter extends BaseAdapter{

	private Context mcontext;
	private int[] imgs = {R.drawable.player_mode_repeat_one_normal,R.drawable.player_mode_repeat_all_normal,
			R.drawable.player_mode_normal_normal,R.drawable.player_mode_random_normal};
	
	private int[] texts = {R.string.play_danqu,R.string.play_shunxuxunhuan,R.string.play_shunxu,R.string.play_suiji};
	
	private class ViewHolder{
		private TextView tv_mode_img;
		private TextView tv_mode_text;
	}
	
	public MusicPlayModeAdapter(Context context)
	{
		this.mcontext = context;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return imgs.length;			
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return imgs[position];
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View view, ViewGroup viewGroup) {
		// TODO Auto-generated method stub
		ViewHolder viewHolder = null;
		if(view == null)
		{
			viewHolder = new ViewHolder();
			view = LayoutInflater.from(mcontext).inflate(R.layout.play_mode_layout,null);
			
			// 初始化布局中的元素
			viewHolder.tv_mode_img = (TextView) view.findViewById(R.id.music_play_mode_type_img);
			viewHolder.tv_mode_text = (TextView) view.findViewById(R.id.music_play_mode_type_text);
			view.setTag(viewHolder);
		}
		else{
			viewHolder = (ViewHolder) view.getTag();
		}
		viewHolder.tv_mode_img.setBackgroundResource(imgs[position]);
		viewHolder.tv_mode_text.setText(texts[position]);
		return view;
	}
	
	//notifyDataSetChanged方法通过一个外部的方法控制,
	//如果适配器的内容改变时需要强制调用getView来刷新每个Item的内容。
	public void notifyDataSetChanged()
	{
		super.notifyDataSetChanged();
	}
}
