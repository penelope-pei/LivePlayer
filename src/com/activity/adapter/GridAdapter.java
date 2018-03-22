package com.activity.adapter;

import java.util.ArrayList;
import java.util.HashMap;

import com.example.liveplayer.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class GridAdapter extends BaseAdapter{
	
	private Context mContext;
	private ArrayList<HashMap<String,Object>> datas;
	 //datas ,需要绑定到view的数据 ;mContext ,传入上下文 
	
	public GridAdapter(Context context,ArrayList<HashMap<String,Object>> datas){
		this.datas = datas;
		mContext=context;
	}
	
     public int getCount(){
    	 if(datas == null)
    	     return 0;
    	 else 
    		 return datas.size();// 返回数据的总数
     }

	public Object getItem(int position) {
		
		return datas.get(position);// 返回在list中指定位置的数据的内容
	}
	
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;// 返回数据在list中所在的位置
	}
	
	public View getView(int position, View view, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder viewHolder;
		if(view == null){
			// 使用自定义的grid_items作为Layout,LayoutInflater.from(Context),找xml布局文件；
			//对于一个没有被载入或者想要动态载入的界面，都需要使用LayoutInflater.inflate()来载入
			view = LayoutInflater.from(mContext).inflate(R.layout.grid_item,null);
			
			 // 减少findView的次数
			viewHolder = new ViewHolder();
			// 初始化布局中的元素
			viewHolder.imageview = (ImageView) view.findViewById(R.id.image);
			viewHolder.textview = (TextView) view.findViewById(R.id.text);
			
			view.setTag(viewHolder);
		}
		else{
			viewHolder = (ViewHolder) view.getTag();
		}
		// 从传入的数据中提取数据并绑定到指定的view中
		viewHolder.imageview.setImageResource((Integer) datas.get(position).get("image"));
		viewHolder.textview.setText(datas.get(position).get("text").toString());
		
		return view;
	}
	class ViewHolder{

		private TextView textview;
		private ImageView imageview;
	}
	
}
