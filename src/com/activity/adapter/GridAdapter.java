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
	 //datas ,��Ҫ�󶨵�view������ ;mContext ,���������� 
	
	public GridAdapter(Context context,ArrayList<HashMap<String,Object>> datas){
		this.datas = datas;
		mContext=context;
	}
	
     public int getCount(){
    	 if(datas == null)
    	     return 0;
    	 else 
    		 return datas.size();// �������ݵ�����
     }

	public Object getItem(int position) {
		
		return datas.get(position);// ������list��ָ��λ�õ����ݵ�����
	}
	
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;// ����������list�����ڵ�λ��
	}
	
	public View getView(int position, View view, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder viewHolder;
		if(view == null){
			// ʹ���Զ����grid_items��ΪLayout,LayoutInflater.from(Context),��xml�����ļ���
			//����һ��û�б����������Ҫ��̬����Ľ��棬����Ҫʹ��LayoutInflater.inflate()������
			view = LayoutInflater.from(mContext).inflate(R.layout.grid_item,null);
			
			 // ����findView�Ĵ���
			viewHolder = new ViewHolder();
			// ��ʼ�������е�Ԫ��
			viewHolder.imageview = (ImageView) view.findViewById(R.id.image);
			viewHolder.textview = (TextView) view.findViewById(R.id.text);
			
			view.setTag(viewHolder);
		}
		else{
			viewHolder = (ViewHolder) view.getTag();
		}
		// �Ӵ������������ȡ���ݲ��󶨵�ָ����view��
		viewHolder.imageview.setImageResource((Integer) datas.get(position).get("image"));
		viewHolder.textview.setText(datas.get(position).get("text").toString());
		
		return view;
	}
	class ViewHolder{

		private TextView textview;
		private ImageView imageview;
	}
	
}
