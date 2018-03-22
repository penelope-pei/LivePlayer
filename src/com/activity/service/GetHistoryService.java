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
	private int count = 0;        //统计历史记录个数，如果超过100个，自动清零
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
    	//创建并初始化数据库
		historydb = new HistoryDBAdapter(this);
		isgettinghistorydata = true;
		isfirsthasgethistorydata = false;
		
		mHandlerThread = new HandlerThread("delmusics", 5);
		mHandlerThread.start();
		mHandler = new Handler(mHandlerThread.getLooper());
    	
        super.onCreate();
    } 
  
    //绑定服务，自动执行
    @Override  
    public IBinder onBind(Intent arg0) {  
        return null;  
    }  
    
    @Override  
    public void onStart(Intent intent, int startId) {
    	//从数据库获取数据
    	insertMusicinfo = new MusicInfo();
    	type_str = intent.getStringExtra("type");
    	if(type_str.equals("toget"))
    	{
    		getDataFromdb();
    	}
    	
    	if(type_str.equals("toinsert"))
    	{
    		//重新拿数据，不然musiclist是空的，就无法比较数据
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
    		//把数据加入到数据库中
    		insertTodb();
    		//然后再从数据库中拿取数据
    		getDataFromdb();
    	}
    	
    	//这时候得到的播放历史记录，并不是按照当前播放的放在最前面，而是最先加进入的放在最前面
    	//而我们所要的效果是最后播放的音乐在最前面
    	musiclist = resortlist(musiclist);
    	//这时候已经把所有数据保存到本地数据库中，发一个广播通知MusicActivity更新界面
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
    
    //从数据库获取数据
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
					//函数:getMusicName(String name)是指去掉歌曲名后面的.mp3
					musicInfo.setName(getMusicName(title));
					musicInfo.setId(music_id);
					musicInfo.setFavorite(favorite);
					musicInfo.setWordsUrl(word_url);
					musicInfo.setAlbumUrl(album_url);
					//获取专辑封面（如果数据量大的话，会很耗时——需要考虑如何开辟子线程加载）
					//url = /storage/emulated/0/Music/Download/Delacey - Dream It Possible/Dream It Possible.mp3
					Bitmap albumArt = createAlbumArt(album_url);//convertToBitmap(album_url,40,40);
					//判断歌曲是否有对于的专辑图片，没有时置空，防止专辑图片为空时出现错误，异常处理
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
    		//之所以要加上.mp3，是因为在数据库表里保存的时候是有.mp3的，而
    		//当前getName()获取的没有.mp3
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
    		//先判断数据库里是否存在当前插入的音乐
    		//如果存在，则先把数据库原来的删除掉，然后再重新添加进去
    		if(historydb != null)
    		{
    			historydb.open();
    			if(isExsit(insertMusicinfo.getUrl())) //title，参数不是title   insertMusicinfo.getName()
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
    				//添加之前，先清空数据库
    				historydb.deleteTitle();
    			}
        		historydb.insertTitle(title, artist, url, duration, size, album,favorite,wpath,apath);
        		historydb.close();
    		}
    	}
    }
    private String getMusicName(String name)
	{
		//在正则表达式中,特殊符号（"."、"|"、"^"等字符）
		//必须用 \ 来进行转义，而在java字符串中，\ 也是个已经被使用的特殊符号，也需要使用 \ 来转义。
		//有些歌曲名字中带有符号'.'
		return name.substring(0, name.length() - 4);
	}
    
    /**
	 * @Description 获取专辑封面
	 * @param filePath 文件路径，like XXX/XXX/XX.mp3
	 * 如果图片太大，会导致bitmap装不下，所以解决办法为把图片大小设置为原图的一半
	 * @return 专辑封面bitmap
	 */
	public Bitmap createAlbumArt(final String filePath) 
	{
	    Bitmap bitmap = null;
	    File file = new File(filePath);
        if (file.exists()) { 
        	bitmap = BitmapFactory.decodeFile(filePath,getBitmapOption(2));//从本地中读取图片,解码文件  albumfile
        }
	    return bitmap;
	}
	
	//将图片的长和宽缩小味原来的1/2
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

