package com.activity.info;

import android.graphics.Bitmap;

public class VideoInfo {
	
	private String videoName;   //视频文件名
	private String videoPath;   //视频播放地址
	private Bitmap bitmap;      //位图，这里用于专辑图片
	
	
	private int id;
    private String title;
    private String album;
    private String artist;
    private String mimeType;
    private long size;
    private long videoDuration;  //视频播放时长
	
	public VideoInfo()
	{
		
	}
	
	public void setVideoName(String vname)
	{
		this.videoName = vname;
	}
	public String getVideoName()
	{
		return videoName;
	}
	
	public void setVideoPath(String vpath)
	{
		this.videoPath = vpath;
	}
	public String getVideoPath()
	{
		return videoPath;
	}
	
	//专辑图片
	public void setThumbnail(Bitmap bitmap)
	{
		this.bitmap = bitmap;
	}
	public Bitmap getThumbnail()
	{
		return bitmap;
	}
	
	
	public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getVideoDuration() {
        return videoDuration;
    }

    public void setVideoDuration(long duration) {
        this.videoDuration = duration;
    }
    
}
