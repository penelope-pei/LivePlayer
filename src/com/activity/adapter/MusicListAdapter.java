package com.activity.adapter;


import java.util.List;

import com.activity.info.MusicInfo;
import com.activity.utils.MusicUtils;
import com.example.liveplayer.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.SelectionBoundsAdjuster;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;

//音乐列表Adapter设置
public class MusicListAdapter extends BaseAdapter implements SectionIndexer{

	private List<MusicInfo> musicList = null; 
	private Context mcontext;
	
	private class ViewHolder{
		private TextView tv_name;
		private TextView tv_singerName;
		private TextView tv_duration;
		private TextView tv_letter;
		private ImageView iv_music;
		private RelativeLayout show_listview_sort_rl;
		private LinearLayout show_listview_sort_ll;
	}
	
	public MusicListAdapter(Context context)
	{
		this.mcontext = context;
	}
	
	public MusicListAdapter(Context context,List<MusicInfo> musicList)
	{
		this.mcontext = context;
		this.musicList = musicList;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		if(musicList == null)
		{
			return 0;
		}
		else
		{
			return musicList.size();			
		}
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return musicList.get(position);
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
			view = LayoutInflater.from(mcontext).inflate(R.layout.music_list_item,null);
			
			// 初始化布局中的元素
			viewHolder.iv_music = (ImageView) view.findViewById(R.id.music_album);
			viewHolder.tv_duration = (TextView) view.findViewById(R.id.music_duration);
			viewHolder.tv_name = (TextView) view.findViewById(R.id.music_name);
			viewHolder.tv_singerName = (TextView) view.findViewById(R.id.music_singer);
			viewHolder.tv_letter = (TextView) view.findViewById(R.id.music_sort_textview);
			viewHolder.show_listview_sort_rl = (RelativeLayout)view.findViewById(R.id.show_sort_relativelayout);
			viewHolder.show_listview_sort_ll = (LinearLayout)view.findViewById(R.id.music_sort_linearlayout);
			view.setTag(viewHolder);
		}
		else{
			viewHolder = (ViewHolder) view.getTag();
		}
		// 从传入的数据中提取数据并绑定到指定的view中
		MusicInfo musicinfo = musicList.get(position);
		
		//根据position获取分类的首字母的char ascii值  
        int section = getSectionForPosition(position);  
        //如果当前位置等于该分类首字母的Char的位置 ，则认为是第一次出现  
        if(position == getPositionForSection(section)){
        	viewHolder.show_listview_sort_ll.setVisibility(View.VISIBLE);
        	//viewHolder.show_listview_sort_rl.setVisibility(View.GONE);
            viewHolder.tv_letter.setText(musicinfo.getSortLetters());
        }
        else
        {
        	//viewHolder.show_listview_sort_rl.setVisibility(View.VISIBLE);
        	viewHolder.show_listview_sort_ll.setVisibility(View.GONE);
        }
        //Bitmap bitmap = MusicUtils.getArtwork(mcontext, musicinfo.getId(),musicinfo.getAlbumId(), true, true); 
    	//显示专辑图片，获取来自本地的数据
    	viewHolder.iv_music.setImageBitmap(musicinfo.getThumbnail()); 
    	if(musicinfo.getThumbnail() == null)
    	{
    		viewHolder.iv_music.setBackgroundResource(R.drawable.default_album); 
    	}
    	viewHolder.tv_duration.setText(MusicUtils.formatTime(musicinfo.getDuration()));
    	//显示歌曲名和歌手名，musicinfo.getName()得到的字符串类型为:陈小春 - 我爱的人
    	//显示的时候歌曲名为：我爱的人      歌手名为：陈小春
    	String str_name = musicinfo.getName();
    	if(str_name.contains("["))
		{
    		str_name = str_name.replace("[", "=");
			String name_str[] = str_name.split("=");
			//String name_str[] = str_name.split("[");
			str_name = name_str[0];
		}
    	if(str_name.contains(" - "))
    	{
    		String[] name = str_name.split(" - ");
    		viewHolder.tv_name.setText(name[1]);
    		viewHolder.tv_singerName.setText(name[0]);
    	}
    	else
    	{
    		viewHolder.tv_name.setText(str_name);
    		if(musicinfo.getSinger().equals("") || musicinfo.getSinger() == null || musicinfo.getSinger().equals("<unknown>"))
    		{
    			viewHolder.tv_singerName.setText(R.string.unknown_singer);
    		}
    		else
    		{
    			viewHolder.tv_singerName.setText(musicinfo.getSinger());
    		}
    	}
		return view;
	}
	
	//notifyDataSetChanged方法通过一个外部的方法控制,
	//如果适配器的内容改变时需要强制调用getView来刷新每个Item的内容。
	public void notifyDataSetChanged(List<MusicInfo> musicList)
	{
		if(musicList != null){
			this.musicList = musicList;
		}
		else
		{
			this.musicList = null;
		}
		super.notifyDataSetChanged();
	}
	
	
	/** 
     * 根据ListView的当前位置获取分类的首字母的char ascii值 
     */  
    public int getSectionForPosition(int position) {
    	/*String sortStr = "";
    	String str_name = musicList.get(position).getName();
		str_name = str_name.replace(" ", "");
		String[] name = str_name.split("-");
		try {
			sortStr = name[1];
		} catch (Exception e) {
			// TODO: handle exception
			sortStr = name[0];
		}
		
        return sortStr.charAt(0);*/ 
    	int pos = -1;
    	try {
			pos = musicList.get(position).getSortLetters().charAt(0);
		} catch (Exception e) {
			// TODO: handle exception
		}
    	return pos;
    }  
  
    /** 
     * 根据分类的首字母的Char ascii值获取其第一次出现该首字母的位置 
     */  
    public int getPositionForSection(int section) {  
        /*for (int i = 0; i < getCount(); i++) {
        	String sortStr = "";
        	String str_name = musicList.get(i).getName();
			str_name = str_name.replace(" ", "");
			String[] name = str_name.split("-");
			try {
				sortStr = name[1];
			} catch (Exception e) {
				// TODO: handle exception
				sortStr = name[0];
			}
			
            char firstChar = sortStr.toUpperCase().charAt(0);  
            if (firstChar == section) {  
                return i;  
            }  
        }*/
    	char firstChar = '#';
    	for (int i = 0; i < getCount(); i++) {
			String sortStr = musicList.get(i).getSortLetters();
			try {
				firstChar = sortStr.toUpperCase().charAt(0);
			} catch (Exception e) {
				// TODO: handle exception
			}
			if (firstChar == section) {
				return i;
			}
		}
        return -1;  
    }  
	
    @Override  
    public Object[] getSections() {  
        return null;  
    }
    
}
