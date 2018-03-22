package com.activity.adapter;


import java.util.List;

import com.activity.info.VideoInfo;
import com.activity.utils.MusicUtils;
import com.example.liveplayer.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

//音乐列表Adapter设置
public class VideoListAdapter extends BaseAdapter{

	private List<VideoInfo> videoList = null; 
	private Context mcontext;
	
	private class ViewHolder{
		private TextView tv_name;
		private ImageView iv_video;
		private TextView tv_duration;
		private TextView tv_pause;
	}
	
	public VideoListAdapter(Context context)
	{
		this.mcontext = context;
	}
	
	public VideoListAdapter(Context context,List<VideoInfo> videoList)
	{
		this.mcontext = context;
		this.videoList = videoList;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		if(videoList == null)
		{
			return 0;
		}
		else
		{
			return videoList.size();			
		}
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return videoList.get(position);
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
			view = LayoutInflater.from(mcontext).inflate(R.layout.video_item,null);
			
			// 初始化布局中的元素
			viewHolder.iv_video = (ImageView) view.findViewById(R.id.video_imageview);
			viewHolder.tv_name = (TextView) view.findViewById(R.id.video_item_name);
			viewHolder.tv_duration = (TextView) view.findViewById(R.id.video_duration);
			viewHolder.tv_pause = (TextView)view.findViewById(R.id.tv_pause_video);
			view.setTag(viewHolder);
		}
		else{
			viewHolder = (ViewHolder) view.getTag();
		}
		// 从传入的数据中提取数据并绑定到指定的view中
		VideoInfo videoinfo = videoList.get(position);
		
        //Bitmap bitmap = MusicUtils.getArtwork(mcontext, musicinfo.getId(),musicinfo.getAlbumId(), true, true); 
    	//显示专辑图片，获取来自本地的数据
    	viewHolder.iv_video.setImageBitmap(videoinfo.getThumbnail()); 
    	if(videoinfo.getThumbnail() == null)
    	{
    		viewHolder.iv_video.setBackgroundResource(R.drawable.default_album); 
    	}
    	viewHolder.tv_name.setText(videoinfo.getVideoName());
    	viewHolder.tv_duration.setText(MusicUtils.formatTime(videoinfo.getVideoDuration()));
		return view;
	}
	
	//notifyDataSetChanged方法通过一个外部的方法控制,
	//如果适配器的内容改变时需要强制调用getView来刷新每个Item的内容。
	public void notifyDataSetChanged(List<VideoInfo> videoList)
	{
		if(videoList != null){
			this.videoList = videoList;
		}
		else
		{
			this.videoList = null;
		}
		super.notifyDataSetChanged();
	}
    
}
