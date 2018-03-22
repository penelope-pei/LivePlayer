package com.activity.service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.activity.MusicActivity;
import com.activity.MusicPlay;
import com.activity.WelcomeActivity;
import com.activity.adapter.LrcHandle;
import com.activity.info.MusicInfo;
import com.activity.message.NotificationMsg;
import com.activity.music.db.DBAdapter;
import com.activity.music.db.HistoryDBAdapter;
import com.activity.receiver.MyReceiver;
import com.activity.utils.MusicUtils;
import com.example.liveplayer.R;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.BitmapFactory.Options;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.Media;
import android.util.Log;
import android.widget.Toast;

public class GetHistoryService extends Service{
    
	public static HistoryDBAdapter historydb;
	private Context mcontext;
	private String type_str = "";
	private int count = 0;        //ͳ����ʷ��¼�������������100�����Զ�����
	private MusicInfo insertMusicinfo;
	public static List<MusicInfo> musiclist = null;
	private List<MusicInfo> mymusiclist = null;
	private int del_position = 0;
	private Handler mHandler;
	private HandlerThread mHandlerThread;
	
	public static boolean isgettinghistorydata = true;
	public static boolean isfirsthasgethistorydata = false;
	
    @Override  
    public void onCreate()
    {  
    	mcontext = getBaseContext();
    	musiclist = new ArrayList<MusicInfo>();
    	//��������ʼ�����ݿ�
		historydb = new HistoryDBAdapter(this);
		isgettinghistorydata = true;
		isfirsthasgethistorydata = false;
		
		mHandlerThread = new HandlerThread("delmusics", 5);
		mHandlerThread.start();
		mHandler = new Handler(mHandlerThread.getLooper());
    	
        super.onCreate();
    } 
  
    //�󶨷����Զ�ִ��
    @Override  
    public IBinder onBind(Intent arg0) {  
        return null;  
    }  
    
    @Override  
    public void onStart(Intent intent, int startId) {
    	//�����ݿ��ȡ����
    	insertMusicinfo = new MusicInfo();
    	type_str = intent.getStringExtra("type");
    	if(type_str.equals("toget"))
    	{
    		getDataFromdb();
    	}
    	
    	if(type_str.equals("toinsert"))
    	{
    		//���������ݣ���Ȼmusiclist�ǿյģ����޷��Ƚ�����
    		getDataFromdb();
    		insertMusicinfo.setAlbum(intent.getStringExtra("album"));
    		insertMusicinfo.setName(intent.getStringExtra("music_name"));
    		insertMusicinfo.setSinger(intent.getStringExtra("singer_name"));
    		insertMusicinfo.setDuration(intent.getLongExtra("duration", 0L));
    		insertMusicinfo.setSize(intent.getLongExtra("size", 0L));
    		insertMusicinfo.setWordsUrl(intent.getStringExtra("word_url"));
    		insertMusicinfo.setAlbumUrl(intent.getStringExtra("album_url"));
    		insertMusicinfo.setFavorite(intent.getBooleanExtra("fav", false));
    		insertMusicinfo.setUrl(intent.getStringExtra("music_url"));
    		
    		//insertMusicinfo = (MusicInfo)intent.getSerializableExtra("musicinfo");
    		//�����ݼ��뵽���ݿ���
    		insertTodb();
    		//Ȼ���ٴ����ݿ�����ȡ����
    		getDataFromdb();
    	}
    	
    	//��ʱ��õ��Ĳ�����ʷ��¼�������ǰ��յ�ǰ���ŵķ�����ǰ�棬�������ȼӽ���ķ�����ǰ��
    	//��������Ҫ��Ч������󲥷ŵ���������ǰ��
    	musiclist = resortlist(musiclist);
    	//��ʱ���Ѿ����������ݱ��浽�������ݿ��У���һ���㲥֪ͨMusicActivity���½���
        Intent intent_to_activity = new Intent();
        intent_to_activity.putExtra("updatetype", NotificationMsg.NOTIFICATION_UPDATE_TYPE_HISTORY);
        intent_to_activity.setAction(NotificationMsg.NOTIFICATION_MUSICACTIVITY_TO_UPDATE_HISTORY);
        sendBroadcast(intent_to_activity);
    	super.onStart(intent, startId); 
    }  
  
    @Override  
    public void onDestroy() {  
        super.onDestroy();
    }  
    
    //�����ݿ��ȡ����
    private void getDataFromdb()
    {
    	musiclist = new ArrayList<MusicInfo>();
    	if(historydb != null)
    	{
    		historydb.open();
    		Cursor c = historydb.getAllTitles();
    		if (c != null && c.moveToFirst())
			{
				do{
					long music_id = c.getLong(0);
					String title = c.getString(1);  
					String artist = c.getString(2);  
					String url = c.getString(3);  
					long duration = c.getLong(4);  
					long size = c.getLong(5);
					String album = c.getString(6);  
					boolean favorite = c.getInt(7) == 1 ? true : false;
					String word_url = c.getString(8);
					String album_url = c.getString(9);
					
					MusicInfo musicInfo = new MusicInfo();  
					musicInfo.setAlbum(album);  
					musicInfo.setDuration(duration);  
					musicInfo.setSize(size);  
					musicInfo.setSinger(artist);  
					musicInfo.setUrl(url);
					//����:getMusicName(String name)��ָȥ�������������.mp3
					musicInfo.setName(getMusicName(title));
					musicInfo.setId(music_id);
					musicInfo.setFavorite(favorite);
					musicInfo.setWordsUrl(word_url);
					musicInfo.setAlbumUrl(album_url);
					//��ȡר�����棨�����������Ļ�����ܺ�ʱ������Ҫ������ο������̼߳��أ�
					//url = /storage/emulated/0/Music/Download/Delacey - Dream It Possible/Dream It Possible.mp3
					Bitmap albumArt = createAlbumArt(album_url);//convertToBitmap(album_url,40,40);
					//�жϸ����Ƿ��ж��ڵ�ר��ͼƬ��û��ʱ�ÿգ���ֹר��ͼƬΪ��ʱ���ִ����쳣����
					try {
						musicInfo.setThumbnail(albumArt);
					} catch (Exception e) {
						// TODO: handle exception
						musicInfo.setThumbnail(null);
					}
					musiclist.add(musicInfo);
					Log.e("info", "------------------------------------n====="+musicInfo.getName());
				}while(c.moveToNext());
				isfirsthasgethistorydata = true;
			}
    		historydb.close();
    	}
    }
 
    private void insertTodb()
    {
    	if(insertMusicinfo != null)
    	{
    		//֮����Ҫ����.mp3������Ϊ�����ݿ���ﱣ���ʱ������.mp3�ģ���
    		//��ǰgetName()��ȡ��û��.mp3
    		String title = insertMusicinfo.getName()+".mp3";
    		String album = insertMusicinfo.getAlbum();  
    		long duration = insertMusicinfo.getDuration();  
    		long size = insertMusicinfo.getSize();
    		String artist = insertMusicinfo.getSinger();  
    		String url = insertMusicinfo.getUrl();
    		int favorite = 0;
    		if(insertMusicinfo.getFavorite())
    		{
    			favorite = 1;
    		}
    		String wpath = insertMusicinfo.getWordsUrl();
    		String apath = insertMusicinfo.getAlbumUrl();
    		//���ж����ݿ����Ƿ���ڵ�ǰ���������
    		//������ڣ����Ȱ����ݿ�ԭ����ɾ������Ȼ����������ӽ�ȥ
    		if(historydb != null)
    		{
    			historydb.open();
    			if(isExsit(insertMusicinfo.getUrl())) //title����������title   insertMusicinfo.getName()
        		{
    				Log.e("info", "------------------------------------3====="+del_position);
        			historydb.deleteTitle(musiclist.get(del_position).getId());  //insertMusicinfo.getId()
        		}
    			else
    			{
    				count++;
    			}
    			
    			if(count > 100)
    			{
    				count = 0;
    				//���֮ǰ����������ݿ�
    				historydb.deleteTitle();
    			}
        		historydb.insertTitle(title, artist, url, duration, size, album,favorite,wpath,apath);
        		historydb.close();
    		}
    	}
    }
    private String getMusicName(String name)
	{
		//��������ʽ��,������ţ�"."��"|"��"^"���ַ���
		//������ \ ������ת�壬����java�ַ����У�\ Ҳ�Ǹ��Ѿ���ʹ�õ�������ţ�Ҳ��Ҫʹ�� \ ��ת�塣
		//��Щ���������д��з���'.'
		return name.substring(0, name.length() - 4);
	}
    
    /**
	 * @Description ��ȡר������
	 * @param filePath �ļ�·����like XXX/XXX/XX.mp3
	 * ���ͼƬ̫�󣬻ᵼ��bitmapװ���£����Խ���취Ϊ��ͼƬ��С����Ϊԭͼ��һ��
	 * @return ר������bitmap
	 */
	public Bitmap createAlbumArt(final String filePath) 
	{
	    Bitmap bitmap = null;
	    File file = new File(filePath);
        if (file.exists()) { 
        	bitmap = BitmapFactory.decodeFile(filePath,getBitmapOption(2));//�ӱ����ж�ȡͼƬ,�����ļ�  albumfile
        }
	    return bitmap;
	}
	
	//��ͼƬ�ĳ��Ϳ���Сζԭ����1/2
	private Options getBitmapOption(int inSampleSize){
		System.gc();
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPurgeable = true;
		options.inSampleSize = inSampleSize;
		return options;
	}
	
	private boolean isExsit(String insert_url)
	{
		Log.e("info", "------------------------------------5====="+insert_url);
		boolean isexsit = false;
		if(musiclist != null && musiclist.size() > 0)
		{
			for(int i = 0; i< musiclist.size();i++)
			{
				if(musiclist.get(i).getUrl().equals(insert_url))
				{
					Log.e("info", "------------------------------------4====="+musiclist.get(i).getUrl());
					del_position = i;
					isexsit = true;
					break;
				}
			}
		}
		return isexsit;
	}
	
	private List<MusicInfo> resortlist(List<MusicInfo> music_list)
	{
		mymusiclist = new ArrayList<MusicInfo>();
		try {
			for(int i = music_list.size() - 1;i >= 0;i--)
			{
				MusicInfo minfo = music_list.get(i);
				mymusiclist.add(minfo);
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		return mymusiclist;
	}
	
	private Runnable mRunnable = new Runnable() {
		@Override
		public void run() {
			
		}
	};
}

