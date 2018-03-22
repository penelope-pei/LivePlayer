package com.activity.service;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.activity.MusicActivity;
import com.activity.MusicPlay;
import com.activity.WelcomeActivity;
import com.activity.adapter.LrcHandle;
import com.activity.info.MusicInfo;
import com.activity.info.VideoInfo;
import com.activity.message.NotificationMsg;
import com.activity.music.db.DBAdapter;
import com.activity.receiver.MyReceiver;
import com.activity.utils.MusicUtils;
import com.example.liveplayer.R;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
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
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.Media;
import android.util.Log;
import android.widget.Toast;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH) public class GetDataService extends Service{
    
	private String ACTION_TYPE = "action_type";
	private String GET_DATA = "get_data";
	private String DEL_DATA = "del_data";
	private String GET_VIDEO_DATA = "get_vedio_data";
	private String DEL_VIDEO_DATA = "del_vedio_data";
	private String type = "";
	private String album_path = "";
	private String words_path = "";
	private String del_url = "";
	public static int numbers = 0;
	public static int temp_numbers = 0;
	public static int startIndex = 0;
	private boolean startoneget = false;//专辑图
	private boolean starttwoget = false;//指歌词
	public static boolean isgetAll = false;
	public static boolean isgetFav = false;
	private int allcounts = -1;
	
	public static boolean isgettingdata = true;
	public static boolean isfirsthasgetdata = false;//是否第一次获取数据
	public static boolean isfirsthasgetfavdata = false;//是否第一次获取喜爱数据
	public static boolean firstclick = true;  //判断第一次进来点击播放按钮
	
	private Cursor cursor = null;
	public static DBAdapter db;
	private Handler mHandler;
	private HandlerThread mHandlerThread;
	private File common_file;   //指定的路径的所有文件
	private Context mcontext;
	
	public static int background_id = -1;
	
	private static List<String> music_word_path = null;
	private static List<String> music_album_path = null;
	
	public static boolean hasgetvideodata = false;
	public static boolean firstgetvideodata = true;
	public static int video_num = 0;
	public static List<VideoInfo> allVideoList = null;// 视频信息集合  
	
	/**
	 *Handler不带参数的默认构造函数：new Handler()，实际上是通过Looper.myLooper()来获取当前线程中的消息循环,
     *而默认情况下，线程是没有消息循环的，所以要调用 Looper.prepare()来给线程创建消息循环，再通过Looper.loop()来使消息循环起作用.
     *另，Activity的MainUI线程默认是有消息队列的。所以在Activity中新建Handler时，不需要先调用Looper.prepare()。
     * 
     * Looper用于封装了android线程中的消息循环，默认情况下一个线程是不存在消息循环（message loop）的，
     * 需要调用Looper.prepare()来给线程创建一个消息循环，调用Looper.loop()来使消息循环起作用，从消息队列里拿消息，处理消息。
     * 注：写在Looper.loop()之后的代码不会被立即执行，当调用后mHandler.getLooper().quit()后，loop才会中止，其后的代码才能得以运行。
     * Looper对象通过MessageQueue来存放消息和事件。一个线程只能有一个Looper，对应一个MessageQueue。
	 */
	//handler,处理线程，消息队列
    //handler.post(runnable)方法是将Runnable对象放入队列，当从队列中取出Runnable后，在handler所在的线程执行run方法内容，但是并没有开启新线程，

	private Handler myHandler = new Handler()
	{
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch(msg.what)
			{
				case 1:
					if(startoneget && starttwoget)  //只要执行完搜索资料，避免多次执行
					{
						startoneget = false;
						starttwoget = false;
						mHandler.post(mRunnable);
					}
					break;
					
				case 2:  //获取视频文件
					break;
				case 3:   //删除视频文件
					break;
			}
			//延时发送信息,每5s更新一次    
            myHandler.sendEmptyMessageDelayed(1, 5000);
			super.handleMessage(msg);
		}
	};
	//////////////////////////////////////////////////////////////////////////////////////////////
	//onCreate
    @Override  
    public void onCreate()
    {  
    	type = "";
    	mcontext = getBaseContext();
    	startoneget = false;
		starttwoget = false;
		isgettingdata = true;
		isfirsthasgetdata = false;
		isfirsthasgetfavdata = false;
		firstclick = true;
		hasgetvideodata = false;
		firstgetvideodata = true;
		allVideoList = new ArrayList<VideoInfo>();
		video_num = allVideoList.size();
		allcounts = 0;
		
    	mHandlerThread = new HandlerThread("delmusics", 5);
		mHandlerThread.start();
		mHandler = new Handler(mHandlerThread.getLooper());//循环；回路
		
		common_file = new File("/storage/emulated/0/");
    	//创建并初始化数据库
    	db = new DBAdapter(this);
    	
        super.onCreate();
    } 
    //绑定服务，自动执行
    @Override  
    public IBinder onBind(Intent arg0) {  
        return null;  
    } 
    ///////////////////////////////////////////////////////////////////////////////////////////////
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		//当service运行在低内存的环境时，将会kill掉一些存在的进程。因此进程的优先级将会很重要，可以使用startForeground 将service放到前台状态。这样在低内存时被kill的几率会低一些。
		Notification notification = new Notification(R.drawable.ic_icon,  
				 getString(R.string.app_name), System.currentTimeMillis());  
				  
				 PendingIntent pendingintent = PendingIntent.getActivity(this, 0,  
				 new Intent(this, WelcomeActivity.class), 0);  
				 notification.setLatestEventInfo(this, getString(R.string.app_name), "保持程序在后台运行",  
				 pendingintent);  
				startForeground(0x111, notification);
    	return super.onStartCommand(intent, flags, startId);
	}
	//////////////////////////////////////////////////////////////////////////////////////////////
    //onStart
  //打印格式其中有：陈奕迅 - 主旋律.mp3，花花世界.mp3
    @Override  
    public void onStart(Intent intent, int startId) {  
    	//从数据库获取数据，初始化music_albun_path和music_word_path列表
    	type = intent.getStringExtra(ACTION_TYPE);
    	if(type.equals(GET_DATA))  //获取本地资源
    	{
    		startIndex = 0;
      		cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,null,
      				null, null,MediaStore.Audio.Media.DEFAULT_SORT_ORDER);  //音频
      		if(cursor != null && cursor.moveToFirst())
      		{
      			do{
      				String title = cursor.getString(cursor.getColumnIndex(Media.DISPLAY_NAME));  
      				if(title.contains(".mp3"))
      				{
      					startIndex++;
      				}
      			}while(cursor.moveToNext());
      		}
      		//检测到有新添加的歌曲或者删除了一首歌，则需要重新去本地文件中获取数据
      		if(allcounts != startIndex)
  			{
      			music_album_path = new ArrayList<String>();
      			music_word_path = new ArrayList<String>();
            	//获取专辑和歌词路径
      			getDataFromdb();
  				allcounts = startIndex;
  				isgettingdata = true;      //正在第一次拿，置true
  				isfirsthasgetdata = false;  //还没第一次拿到数据，置false
  				//获取本地数据，并添加到数据库
  				Message msg = new Message();
  				msg.what = 1;
  				myHandler.sendMessage(msg);
  			}
    	}
    	
    	if(type.equals(DEL_DATA))  //删除本地某一音乐文件,由于耗时,在线程中操作
    	{
    		del_url = intent.getStringExtra("del_url");
    		mHandler.post(delRunnable);
    	}
    	
    	if(type.equals(GET_VIDEO_DATA))  //获取视频文件
    	{
    		if(video_num == 0 && firstgetvideodata)
			{
    			firstgetvideodata = false;
    			mHandler.post(getvideoRunnable);
			}
			else if(video_num != allVideoList.size())
			{
				mHandler.post(getvideoRunnable);
			}
    	}
    	
    	if(type.equals(DEL_VIDEO_DATA))  //删除视频文件
    	{
    		myHandler.sendEmptyMessage(3);
    	}
    	super.onStart(intent, startId); 
    }  
    //////////////////////////////////////////////////////////////////////////////////////////////
    @Override  
    public void onDestroy() {  
    	stopForeground(true);
        super.onDestroy();
    }  
    //////////////////////////////////////////////////////////////////////////////////////////////
    //从数据库获取数据，填充music_album_path和music_word_path列表
    private void getDataFromdb()
    {
    	if(music_album_path.size() == 0 || music_word_path.size() == 0)
		{
    		//通过Handler开启线程，获取符合要求的歌曲专辑以及歌词
			mHandler.post(getalbumRunnable);
			mHandler.post(getwordRunnable);
		}
    }
    
  	//获取以.mp3结尾以及不是以.mp3结尾的音乐文件总数，并逐一添加到数据库中
  	//之所以要获取总数，一是列表只需要显示以.mp3结尾的音乐，
  	//二是为了检查本地数据库是否有发生增删改查等变化
  	private Runnable mRunnable = new Runnable() {
		@Override
		public void run() {
			/**
	  		 * 利用ContentResolver的query函数来查询数据，所含参数为：resolver:分解器，解析器
	  		 * 指明要查询的数据库名称加上表的名称；
	  		 * 指定查询数据库表中的哪几列，返回的游标中将包括相应的信息，null则返回所有信息；
	  		 * 指定查询条件；
	  		 * 如果selection这个没有？的话，那么这个String数组可以为null，若有的话可用实际值代替；
	  		 * 指定查询结果的排列顺序
	  		 * Cursor cursor = contentResolver.query(contentUri, projection, selection, selectionArgs, sortOrder);
	  		 */
			//获取本地信息
			Cursor mycursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,null,
					null, null,MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
			if(db != null)
			{
				db.open();
				db.deleteTitle();//重新导入数据前，删除数据库所有数据
				
				if(mycursor != null && mycursor.moveToFirst())
				{
					do{
						String title = mycursor.getString(mycursor.getColumnIndex(Media.DISPLAY_NAME)); 
						//应该判断下有些音频不是MP3格式的，比如有些是m4a格式的,只添加.mp3格式的歌曲
						//M4A是MPEG4音频标准文件的扩展名,普通的MPEG4文件扩展名是.mp4。m4a区别MPEG4的视频和音频文件
						if(title.contains(".mp3"))
						{
							String album = mycursor.getString(mycursor.getColumnIndex(Media.ALBUM));  
		  					long duration = mycursor.getLong(mycursor.getColumnIndex(Media.DURATION));  
		  					long size = mycursor.getLong(mycursor.getColumnIndex(Media.SIZE));
		  					String artist = mycursor.getString(mycursor.getColumnIndex(Media.ARTIST));  
		  					String url = mycursor.getString(mycursor.getColumnIndex(Media.DATA));  
		  					int favorite = 0;
		  					String wpath = "";
		  					String apath = "";
		  					//根据歌曲名以及歌手名获取当前音乐是否有专辑，如果有，则把专辑路径添加到数据库中，否则，专辑路径添加：nothisfile
		  					if(music_album_path.size() > 0)
		  					{
		  						//apath = getAlbumpath(mycursor.getString(mycursor.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM_KEY)));
		  						//无论是包含有歌手名还是歌曲名,substring:截取字符串中的子串.jpg--4位
		  						int aposition = getalbumIndex(title.substring(0, title.length() - 4),artist); 
		  						if(aposition == -1)
		  						{
		  							apath = "nothisfile";
		  						}
		  						else
		  						{
		  							apath=music_album_path.get(aposition);
		  						}
		  					}
		  					else
		  					{
		  						apath = "nothisfile";
		  					}
		  					//根据歌曲名以及歌手名获取当前音乐是否有歌词，如果有，则把歌词路径添加到数据库中，否则，歌词路径添加：nothisfile
		  					if(music_word_path.size() > 0)
		  					{
		  						int wposition = getwordIndex(title.substring(0, title.length() - 4),artist);
		  						if(wposition == -1)
		  						{
		  							wpath = "nothisfile";
		  						}
		  						else
		  						{
		  							wpath=music_word_path.get(wposition);
		  						}
		  					}
		  					else
		  					{
		  						wpath = "nothisfile";
		  					}
		  					db.insertTitle(title, artist, url, duration, size, album,favorite,wpath,apath);
						}
					}while(mycursor.moveToNext());
					//这时候已经把所有数据保存到本地数据库中，发一个广播通知MusicActivity更新界面
			        Intent intent_to_activity = new Intent();
			        intent_to_activity.putExtra("updatetype", NotificationMsg.NOTIFICATION_UPDATE_TYPE_ALL);
			        intent_to_activity.setAction(NotificationMsg.NOTIFICATION_MUSICACTIVITY_TO_UPDATE_LOCAL);
			        sendBroadcast(intent_to_activity);
				}
				db.close();
			}
		}
	};
	//////////////////////////////////////////////////////////////////////////////////////////////
	//得到专辑图片线程，线程结束，把状态置true，其他音乐播放器下载的歌曲的专辑路径可能包含album，或是包含Cover
	private Runnable getalbumRunnable = new Runnable() {
		@Override
		public void run() {
			//获取本地包含有.album字符串的路径
			BrowserFile(common_file,"album");
			BrowserFile(common_file,"Cover");
			startoneget = true;
		}
	};
	//得到歌词线程，线程结束，把状态置true
	private Runnable getwordRunnable = new Runnable() {
		@Override
		public void run() {
			//获取本地包含有lyrics字符串的路径
			BrowserwordFile(common_file,"lyrics");
			BrowserwordFile(common_file,"Lyric");
			starttwoget = true;
		}
	};
	
	//根据特定的字符串key在/storage/emulated/0/下查找所有符合要求的专辑图片文件，
	//BrowserFile:浏览文件，打印格式其中有：陈奕迅 - 主旋律.jpg
	private void BrowserFile(File fileold,String key) 
	{  
		try{
			File[] files = fileold.listFiles();//获取当前文件夹下的所有文件和文件夹
			if(files.length>0)
			{
				for(int j=0;j<files.length;j++)
				{
					album_path = "";
					if(!files[j].isDirectory())  //目录，文件夹,若表示的是一个目录则返回true
					{
						if(files[j].getPath().contains(key))
						{
							album_path = files[j].getPath(); 
							if(album_path.contains(".jpg"))
							{
								music_album_path.add(album_path);
							}
						}
					}
					else
					{
						this.BrowserFile(files[j],key); //查找文件夹下的文件
					}
				}
			}
		}
		catch(Exception e)
		{
			
		}
	}
	
	//根据特点的字符串key在/storage/emulated/0/下查找所有符合要求的歌词文件，打印格式其中有：田馥甄 - 花花世界.lrc
	private void BrowserwordFile(File fileold,String key) 
	{  
		try{
			File[] files=fileold.listFiles();
			if(files.length>0)
			{
				for(int j=0;j<files.length;j++)
				{
					words_path = "";
					if(!files[j].isDirectory())
					{
						if(files[j].getPath().contains(key))
						{
							words_path = files[j].getPath(); 
							if(words_path.contains(".lrc"))
							{
								music_word_path.add(words_path);
							}
						}
					}
					else
					{
						this.BrowserwordFile(files[j],key);
					}
				}
			}
		}
		catch(Exception e)
		{
			
		}
	}
	//////////////////////////////////////////////////////////////////////////////////////////////
	//返回index，获取对应位置的专辑URL
	private int getalbumIndex(String title,String sname)
	{
		//格式：刀郎 - 2002年的第一场雪
		int index = -1;
		String allname = "";//歌名
		if(title.contains(" - "))
		{
			allname = title.split(" - ")[1]; // 留恋 [mqms2]; 魔鬼中的天使
		}
		else
		{
			allname = title;
		}
		
		if(allname.contains("["))
		{
			allname = allname.replace(" ", "");
			allname = allname.replace("[", "=");
			String name_str[] = allname.split("=");
			//String name_str[] = allname.split("[");
			allname = name_str[0];
		}
		
		for(int i = 0;i<music_album_path.size();i++)
		{
			if(music_album_path.get(i).contains(title))  //这是专辑图片路径直接包含歌曲名（已经去掉.mp3字样）
			{
				index = i;
				break;
			}
			else if(music_album_path.get(i).contains(allname))  //这是专辑图片路径包含歌名
			{
				index = i;
				break;
			}
			else if(music_album_path.get(i).contains(sname))  //这是专辑图片路径包含有歌手名
			{
				index = i;
				break;
			}
		}
		return index;
	}
	//////////////////////////////////////////////////////////////////////////////////////////////
	//返回index，获取对应位置的歌词URL,田馥甄 - 花花世界.lrc
	private int getwordIndex(String name,String sname)
	{
		int index = -1;
		String allname = "";
		if(name.contains(" - "))
		{
			allname = name.split(" - ")[1]; //留恋 [mqms2]
		}
		else
		{
		    allname = name;	
		}
		if(allname.contains("["))
		{
			allname = allname.replace(" ", "");
			allname = allname.replace("[", "=");
			String name_str[] = allname.split("=");
			//String name_str[] = allname.split("[");
			allname = name_str[0];
		}
		
		for(int i = 0;i<music_word_path.size();i++)
		{
			if(music_word_path.get(i).contains(allname))
			{
				index = i;
				break;
			}
			
			/*if(music_word_path.get(i).contains(name))  //这是歌词路径直接包含歌曲名（已经去掉.mp3字样）
			{
				index = i;
				break;
			}
			else if(music_word_path.get(i).contains(sname))  //这是歌词路径包含有歌手名
			{
				index = i;
				break;
			}*/
		}
		
		return index;
	}
	//////////////////////////////////////////////////////////////////////////////////////////////
	//删除本地对应的歌曲
	private Runnable delRunnable = new Runnable() {
		@Override
		public void run() {
			File musicfile = new File(del_url);
			if(musicfile.exists())
			{
				if (musicfile.isFile()) { 
					//设置可执行
					musicfile.setExecutable(true, false);
					//设置可读
					musicfile.setReadable(true, false);
					//设置可写
					musicfile.setWritable(true, false);
					if(musicfile.delete())
		   			{
		   				//弹出提示
		   				Toast.makeText(mcontext, "删除成功", Toast.LENGTH_LONG).show();
		   			}
				}  
			}
			else
			{
				//弹出提示
				Toast.makeText(mcontext, "该音乐已不存在，删除失败", Toast.LENGTH_LONG).show();
			}
		}
	};
	//////////////////////////////////////////////////////////////////////////////////////////////
	//另一种得到专辑图片方法，根据album属性获取，但是该方法目前行不通
	private String getAlbumpath(String album_str)
	{
		String music_album_url = "";
		String[] argArr = { album_str };
		ContentResolver albumResolver = mcontext.getContentResolver();
		Cursor albumCursor = albumResolver.query(
				MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, null,
				MediaStore.Audio.AudioColumns.ALBUM_ID + " = ?",argArr, null);
		if (null != albumCursor && albumCursor.moveToFirst() && albumCursor.getCount() > 0) 
		{
			int albumArtIndex = albumCursor
					.getColumnIndex(MediaStore.Audio.AlbumColumns.ALBUM_ID);
			String musicAlbumArtPath = albumCursor.getString(albumArtIndex);
			if (null != musicAlbumArtPath&& !musicAlbumArtPath.equals("")) 
			{
				music_album_url = musicAlbumArtPath;
			} 
			else 
			{
				music_album_url = "nothisfile";
			}
		} 
		else 
		{
			// 没有专辑定义，给默认图片
			music_album_url = "nothisfile";
		}
		return music_album_url;
	}
	//////////////////////////////////////////////////////////////////////////////////////////////
	//获取所有视频文件
	private void getVideoFile(final List<VideoInfo> list, File file) 
	{
		// 获得视频文件  
        file.listFiles(new FileFilter() {  
  
            @Override  
            public boolean accept(File file) {  
                // sdCard找到视频名称  
                String name = file.getName();  
                int i = name.indexOf('.');  
                if (i != -1) {  
                    name = name.substring(i); 
                    //只拿MP4格式的
                    /*
                     *  || name.equalsIgnoreCase(".3gp") || name.equalsIgnoreCase(".wmv")  
                            || name.equalsIgnoreCase(".ts") || name.equalsIgnoreCase(".rmvb") || name.equalsIgnoreCase(".mov")  
                            || name.equalsIgnoreCase(".m4v") || name.equalsIgnoreCase(".avi") || name.equalsIgnoreCase(".m3u8")  
                            || name.equalsIgnoreCase(".3gpp")  || name.equalsIgnoreCase(".3gpp2")  || name.equalsIgnoreCase(".mkv")  
                            || name.equalsIgnoreCase(".flv")  || name.equalsIgnoreCase(".divx") || name.equalsIgnoreCase(".f4v")  
                            || name.equalsIgnoreCase(".rm")  || name.equalsIgnoreCase(".asf") || name.equalsIgnoreCase(".ram")  
                            || name.equalsIgnoreCase(".mpg") || name.equalsIgnoreCase(".v8") || name.equalsIgnoreCase(".swf")  
                            || name.equalsIgnoreCase(".m2v")  || name.equalsIgnoreCase(".asx") || name.equalsIgnoreCase(".ra")  
                            || name.equalsIgnoreCase(".ndivx")  || name.equalsIgnoreCase(".xvid")
                            */
                    if (name.equalsIgnoreCase(".mp4") && file.getAbsolutePath().contains("Camera"))   
                    {  
                        VideoInfo vi = new VideoInfo();  
                        vi.setVideoName(file.getName());  
                        vi.setVideoPath(file.getAbsolutePath());
                        list.add(vi);
                        return true;  
                    }  
                } 
                else if (file.isDirectory()) 
                {  
                    getVideoFile(list, file);  
                }  
                return false;  
            }  
        });  
    }
	//////////////////////////////////////////////////////////////////////////////////////////////
	/**
     * 获取视频的缩略图
     * 先通过ThumbnailUtils来创建一个视频的缩略图，然后再利用ThumbnailUtils来生成指定大小的缩略图。
     * 如果想要的缩略图的宽和高都小于MICRO_KIND，则类型要使用MICRO_KIND作为kind的值，这样会节省内存。
     * @param videoPath 视频的路径
     * @param width 指定输出视频缩略图的宽度
     * @param height 指定输出视频缩略图的高度度
     * @param kind 参照MediaStore.Images(Video).Thumbnails类中的常量MINI_KIND和MICRO_KIND。
     *            其中，MINI_KIND: 512 x 384，MICRO_KIND: 96 x 96
     * @return 指定大小的视频缩略图
     */
    private Bitmap getVideoThumbnail(String videoPath, int width, int height,int kind) {
        Bitmap bitmap = null;
        // 获取视频的缩略图
        bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, kind);
        if(bitmap!= null){
            bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
                    ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        }
        return bitmap;
    }
    
    public static Bitmap getVideoThumb2(String path, int kind) 
    {
    	return ThumbnailUtils.createVideoThumbnail(path, kind);
    }
    
    public static Bitmap getVideoThumb2(String path) 
    {
    	return getVideoThumb2(path, MediaStore.Video.Thumbnails.FULL_SCREEN_KIND);
    }

    public static Bitmap getVideoThumb(String path) 
    {
    	MediaMetadataRetriever media = new MediaMetadataRetriever();
    	media.setDataSource(path);
    	return media.getFrameAtTime();
    }
    ////////////////////////////////////////////////////////////////////////////////////////////
    //获取视频文件的时长
    public static String getVideoDuring(String mUri){
        String duration=null;
        android.media.MediaMetadataRetriever mmr = new android.media.MediaMetadataRetriever();
        try {
            if (mUri != null) {
                HashMap<String, String> headers=null;
                if (headers == null) {
                    headers = new HashMap<String, String>();
                    headers.put("User-Agent", "Mozilla/5.0 (Linux; U; Android 4.4.2; zh-CN; MW-KW-001 Build/JRO03C) " +
                    		"AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 UCBrowser/1.0.0.001 U4/0.8.0 Mobile " +
                    		"Safari/533.1");
                }
                mmr.setDataSource(mUri, headers);
            }

             duration = mmr.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION);
        } catch (Exception ex) {
        } finally {
            mmr.release();
        }
        return duration;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////
    //获取最终的视频列表
    private List<VideoInfo> getLastvideolist(List<VideoInfo> vlist)
    {
    	List<VideoInfo> tempVideoList = new ArrayList<VideoInfo>();
    	
    	for(VideoInfo vinfo : vlist)
    	{
    		VideoInfo video_info = new VideoInfo();
    		video_info.setVideoName(vinfo.getVideoName());
    		video_info.setVideoPath(vinfo.getVideoPath());
    		video_info.setThumbnail(getVideoThumbnail(vinfo.getVideoPath(),100,100,MediaStore.Video.Thumbnails.MINI_KIND));
    		//video_info.setVideoDuration(getVideoDuring(vinfo.getVideoPath()));
    		tempVideoList.add(video_info);
    	}
    	
    	return tempVideoList;
    }
    
    //////////////////////////////////////////////////////////////////////////////////////////////
    //启动线程，获取视频资源
    private Runnable getvideoRunnable = new Runnable() {
    	@Override
    	public void run() {
    		/*
    		getVideoFile(allVideoList,Environment.getExternalStorageDirectory());// 获得视频文件
			hasgetvideodata = true;
			firstgetvideodata = true;
			video_num = allVideoList.size();
			//获取到最终我们需要的视频列表：包含有视频名、视频缩略图、视频路径、视频时长
			allVideoList = getLastvideolist(allVideoList);
			Intent intent_to_activity = new Intent();
	        intent_to_activity.setAction(NotificationMsg.NOTIFICATION_UPDATE_VIDEO);
	        sendBroadcast(intent_to_activity);
	        */
    		
    		getvideodata();
    	}
    };
    //////////////////////////////////////////////////////////////////////////////////////////////
    private void getvideodata()
    {
    	
    	Cursor cursor = getContentResolver().query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null, null,
                null, MediaStore.Video.Media.DEFAULT_SORT_ORDER);
        if (cursor != null && cursor.moveToFirst()) {
            do{
            	String displayName = cursor
                        .getString(cursor
                                .getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME));
            	String path = cursor
            			.getString(cursor
            					.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
            	if(displayName.contains(".mp4") && (path.contains("Camera") || path.contains("DCIM")))
            	{
            		VideoInfo videoInfo = new VideoInfo();
                	
                    int id = cursor.getInt(cursor
                            .getColumnIndexOrThrow(MediaStore.Video.Media._ID));
                    String title = cursor
                            .getString(cursor
                                    .getColumnIndexOrThrow(MediaStore.Video.Media.TITLE));
                    String album = cursor
                            .getString(cursor
                                    .getColumnIndexOrThrow(MediaStore.Video.Media.ALBUM));
                    String artist = cursor
                            .getString(cursor
                                    .getColumnIndexOrThrow(MediaStore.Video.Media.ARTIST));
                    String mimeType = cursor
                            .getString(cursor
                                    .getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE));
                    long duration = cursor
                            .getInt(cursor
                                    .getColumnIndexOrThrow(MediaStore.Video.Media.DURATION));
                    long size = cursor
                            .getLong(cursor
                                    .getColumnIndexOrThrow(MediaStore.Video.Media.SIZE));
                    videoInfo.setAlbum(album);
                    videoInfo.setArtist(artist);
                    videoInfo.setId(id);
                    videoInfo.setMimeType(mimeType);
                    videoInfo.setSize(size);
                    videoInfo.setThumbnail(getVideoThumbnail(path,100,100,MediaStore.Video.Thumbnails.MINI_KIND));
                    videoInfo.setTitle(title);
                    videoInfo.setVideoDuration(duration);
                    videoInfo.setVideoName(displayName);
                    videoInfo.setVideoPath(path);
                    allVideoList.add(videoInfo);
            	}
            }while(cursor.moveToNext());
            
    	/*
    	try{
    		File videofile = new File("/storage/emulated/0/DCIM/Camera/");
    		File[] files=videofile.listFiles();
    		if(files.length>0)
    		{
    			for(int j=0;j<files.length;j++)
    			{
    				if(!files[j].isDirectory())
    				{
    					if(files[j].getPath().contains(".mp4"))
    					{
    						VideoInfo videoInfo = new VideoInfo();
    						videoInfo.setThumbnail(getVideoThumbnail(files[j].getPath(),100,100,MediaStore.Video.Thumbnails.MINI_KIND));
    						videoInfo.setVideoDuration(Long.getLong(getVideoDuring(files[j].getPath())));
    	                    videoInfo.setVideoName(files[j].getPath().split("/")[5]);
    	                    videoInfo.setVideoPath(files[j].getPath());
    	                    allVideoList.add(videoInfo);
    	                    Log.e("info", "------------------------------------name====="+files[j].getPath().split("/")[5]);
    	                    Log.e("info", "------------------------------------path====="+files[j].getPath());
    					}
    				}
    			}
    		}
    	}
    	catch(Exception e)
    	{
    		
    	}
    	*/
            hasgetvideodata = true;
			firstgetvideodata = true;
			video_num = allVideoList.size();
			Intent intent_to_activity = new Intent();
	        intent_to_activity.setAction(NotificationMsg.NOTIFICATION_UPDATE_VIDEO);
	        sendBroadcast(intent_to_activity);
        }
    }

}
