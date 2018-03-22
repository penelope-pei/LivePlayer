package com.activity.info;

import java.io.Serializable;

import android.graphics.Bitmap;

public class MusicInfo implements Serializable{

	private String file;// �ļ���(ͨ���ļ�������ΪΨһ�жϲ��һ�ø��)
	private long music_id;  //����ID
	private String music_name;// ����
	private String music_singer;// ����
	private long music_duration;// ʱ��
	private long music_size;// ��С
	private String music_url;// ����·��
	private String word_url; //���·��
	private String album_url;  //ר��·��
	private long music_albumid;// ר��
	private long music_songid;
	
	private Bitmap bitmap; //λͼ����������ר��ͼƬ
	
	private String music_format;// ��ʽ(��������)
	private String music_album;// ר��
	private String music_years;// ���
	private String music_channels;// ����
	private String music_genre;// ���
	private String music_kbps;// ������
	private String music_hz;// ������

	private int audioSessionId;// ��Ƶ�ỰID
	private boolean favorite;// �Ƿ��
	
	private String sortLetters;  //��ʾ����ƴ��������ĸ
	
	public MusicInfo()
	{
		
	}
	
	public String getFile() {
		return file;
	}
	public void setFile(String file) {
		this.file = file;
	}
	
	public void setId(long music_id)
	{
		this.music_id = music_id;
	}
	public long getId()
	{
		return music_id;
	}
	
	public void setName(String music_name)
	{
		this.music_name = music_name;
	}
	public String getName()
	{
		return music_name;
	}
	
	public void setSinger(String music_singer)
	{
		this.music_singer = music_singer;
	}
	public String getSinger()
	{
		return music_singer;
	}
	
	public void setDuration(long music_duration)
	{
		this.music_duration = music_duration;
	}
	public long getDuration()
	{
		return music_duration;
	}
	
	public void setUrl(String music_url)
	{
		this.music_url = music_url;
	}
	public String getUrl()
	{
		return music_url;
	}
	
	public void setWordsUrl(String word_url)
	{
		this.word_url = word_url;
	}
	public String getWordsUrl()
	{
		return word_url;
	}
	
	public void setAlbumUrl(String album_url)
	{
		this.album_url = album_url;
	}
	public String getAlbumUrl()
	{
		return album_url;
	}
	
	public void setSize(long music_size)
	{
		this.music_size = music_size;
	}
	public long getSize()
	{
		return music_size;
	}
	
	public void setAlbumId(long music_albumid)
	{
		this.music_albumid = music_albumid;
	}
	public long getAlbumId()
	{
		return music_albumid;
	}
	
	public void setSongId(long music_songid)
	{
		this.music_songid = music_songid;
	}
	public long getSongId()
	{
		return music_songid;
	}
	
	public void setFormat(String music_format)
	{
		this.music_format = music_format;
	}
	public String getFormat()
	{
		return music_format;
	}
	
	public void setAlbum(String music_album)
	{
		this.music_album = music_album;
	}
	public String getAlbum()
	{
		return music_album;
	}
	
	public void setYears(String music_years)
	{
		this.music_years = music_years;
	}
	public String geYears()
	{
		return music_years;
	}
	
	public void setChannels(String music_channels)
	{
		this.music_channels = music_channels;
	}
	public String getChannels()
	{
		return music_channels;
	}
	
	public void setKbps(String music_kbps)
	{
		this.music_kbps = music_kbps;
	}
	public String getKbps()
	{
		return music_kbps;
	}
	
	public void setGenre(String music_genre)
	{
		this.music_genre = music_genre;
	}
	public String getGenre()
	{
		return music_genre;
	}
	
	public void setHz(String music_hz)
	{
		this.music_hz = music_hz;
	}
	public String getHz()
	{
		return music_hz;
	}
	//ר��ͼƬ
	public void setThumbnail(Bitmap bitmap)
	{
		this.bitmap = bitmap;
	}
	public Bitmap getThumbnail()
	{
		return bitmap;
	}
	
	// ���audioSessionId,returnֵ MediaPlayer.getAudioSessionId()
	public int getAudioSessionId() {
		return audioSessionId;
	}
	public void setAudioSessionId(int audioSessionId) {
		this.audioSessionId = audioSessionId;
	}
	
	//return true||false
	public void setFavorite(boolean favorite) {
		this.favorite = favorite;
	}
	public boolean getFavorite() {
		return favorite;
	}
	
	public String getSortLetters() {  
        return sortLetters;  
    }  
    public void setSortLetters(String sortLetters) {  
        this.sortLetters = sortLetters;  
    } 

}
