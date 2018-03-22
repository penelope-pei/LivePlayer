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
	private boolean startoneget = false;//ר��ͼ
	private boolean starttwoget = false;//ָ���
	public static boolean isgetAll = false;
	public static boolean isgetFav = false;
	private int allcounts = -1;
	
	public static boolean isgettingdata = true;
	public static boolean isfirsthasgetdata = false;//�Ƿ��һ�λ�ȡ����
	public static boolean isfirsthasgetfavdata = false;//�Ƿ��һ�λ�ȡϲ������
	public static boolean firstclick = true;  //�жϵ�һ�ν���������Ű�ť
	
	private Cursor cursor = null;
	public static DBAdapter db;
	private Handler mHandler;
	private HandlerThread mHandlerThread;
	private File common_file;   //ָ����·���������ļ�
	private Context mcontext;
	
	public static int background_id = -1;
	
	private static List<String> music_word_path = null;
	private static List<String> music_album_path = null;
	
	public static boolean hasgetvideodata = false;
	public static boolean firstgetvideodata = true;
	public static int video_num = 0;
	public static List<VideoInfo> allVideoList = null;// ��Ƶ��Ϣ����  
	
	/**
	 *Handler����������Ĭ�Ϲ��캯����new Handler()��ʵ������ͨ��Looper.myLooper()����ȡ��ǰ�߳��е���Ϣѭ��,
     *��Ĭ������£��߳���û����Ϣѭ���ģ�����Ҫ���� Looper.prepare()�����̴߳�����Ϣѭ������ͨ��Looper.loop()��ʹ��Ϣѭ��������.
     *��Activity��MainUI�߳�Ĭ��������Ϣ���еġ�������Activity���½�Handlerʱ������Ҫ�ȵ���Looper.prepare()��
     * 
     * Looper���ڷ�װ��android�߳��е���Ϣѭ����Ĭ�������һ���߳��ǲ�������Ϣѭ����message loop���ģ�
     * ��Ҫ����Looper.prepare()�����̴߳���һ����Ϣѭ��������Looper.loop()��ʹ��Ϣѭ�������ã�����Ϣ����������Ϣ��������Ϣ��
     * ע��д��Looper.loop()֮��Ĵ��벻�ᱻ����ִ�У������ú�mHandler.getLooper().quit()��loop�Ż���ֹ�����Ĵ�����ܵ������С�
     * Looper����ͨ��MessageQueue�������Ϣ���¼���һ���߳�ֻ����һ��Looper����Ӧһ��MessageQueue��
	 */
	//handler,�����̣߳���Ϣ����
    //handler.post(runnable)�����ǽ�Runnable���������У����Ӷ�����ȡ��Runnable����handler���ڵ��߳�ִ��run�������ݣ����ǲ�û�п������̣߳�

	private Handler myHandler = new Handler()
	{
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch(msg.what)
			{
				case 1:
					if(startoneget && starttwoget)  //ֻҪִ�����������ϣ�������ִ��
					{
						startoneget = false;
						starttwoget = false;
						mHandler.post(mRunnable);
					}
					break;
					
				case 2:  //��ȡ��Ƶ�ļ�
					break;
				case 3:   //ɾ����Ƶ�ļ�
					break;
			}
			//��ʱ������Ϣ,ÿ5s����һ��    
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
		mHandler = new Handler(mHandlerThread.getLooper());//ѭ������·
		
		common_file = new File("/storage/emulated/0/");
    	//��������ʼ�����ݿ�
    	db = new DBAdapter(this);
    	
        super.onCreate();
    } 
    //�󶨷����Զ�ִ��
    @Override  
    public IBinder onBind(Intent arg0) {  
        return null;  
    } 
    ///////////////////////////////////////////////////////////////////////////////////////////////
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		//��service�����ڵ��ڴ�Ļ���ʱ������kill��һЩ���ڵĽ��̡���˽��̵����ȼ��������Ҫ������ʹ��startForeground ��service�ŵ�ǰ̨״̬�������ڵ��ڴ�ʱ��kill�ļ��ʻ��һЩ��
		Notification notification = new Notification(R.drawable.ic_icon,  
				 getString(R.string.app_name), System.currentTimeMillis());  
				  
				 PendingIntent pendingintent = PendingIntent.getActivity(this, 0,  
				 new Intent(this, WelcomeActivity.class), 0);  
				 notification.setLatestEventInfo(this, getString(R.string.app_name), "���ֳ����ں�̨����",  
				 pendingintent);  
				startForeground(0x111, notification);
    	return super.onStartCommand(intent, flags, startId);
	}
	//////////////////////////////////////////////////////////////////////////////////////////////
    //onStart
  //��ӡ��ʽ�����У�����Ѹ - ������.mp3����������.mp3
    @Override  
    public void onStart(Intent intent, int startId) {  
    	//�����ݿ��ȡ���ݣ���ʼ��music_albun_path��music_word_path�б�
    	type = intent.getStringExtra(ACTION_TYPE);
    	if(type.equals(GET_DATA))  //��ȡ������Դ
    	{
    		startIndex = 0;
      		cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,null,
      				null, null,MediaStore.Audio.Media.DEFAULT_SORT_ORDER);  //��Ƶ
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
      		//��⵽������ӵĸ�������ɾ����һ�׸裬����Ҫ����ȥ�����ļ��л�ȡ����
      		if(allcounts != startIndex)
  			{
      			music_album_path = new ArrayList<String>();
      			music_word_path = new ArrayList<String>();
            	//��ȡר���͸��·��
      			getDataFromdb();
  				allcounts = startIndex;
  				isgettingdata = true;      //���ڵ�һ���ã���true
  				isfirsthasgetdata = false;  //��û��һ���õ����ݣ���false
  				//��ȡ�������ݣ�����ӵ����ݿ�
  				Message msg = new Message();
  				msg.what = 1;
  				myHandler.sendMessage(msg);
  			}
    	}
    	
    	if(type.equals(DEL_DATA))  //ɾ������ĳһ�����ļ�,���ں�ʱ,���߳��в���
    	{
    		del_url = intent.getStringExtra("del_url");
    		mHandler.post(delRunnable);
    	}
    	
    	if(type.equals(GET_VIDEO_DATA))  //��ȡ��Ƶ�ļ�
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
    	
    	if(type.equals(DEL_VIDEO_DATA))  //ɾ����Ƶ�ļ�
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
    //�����ݿ��ȡ���ݣ����music_album_path��music_word_path�б�
    private void getDataFromdb()
    {
    	if(music_album_path.size() == 0 || music_word_path.size() == 0)
		{
    		//ͨ��Handler�����̣߳���ȡ����Ҫ��ĸ���ר���Լ����
			mHandler.post(getalbumRunnable);
			mHandler.post(getwordRunnable);
		}
    }
    
  	//��ȡ��.mp3��β�Լ�������.mp3��β�������ļ�����������һ��ӵ����ݿ���
  	//֮����Ҫ��ȡ������һ���б�ֻ��Ҫ��ʾ��.mp3��β�����֣�
  	//����Ϊ�˼�鱾�����ݿ��Ƿ��з�����ɾ�Ĳ�ȱ仯
  	private Runnable mRunnable = new Runnable() {
		@Override
		public void run() {
			/**
	  		 * ����ContentResolver��query��������ѯ���ݣ���������Ϊ��resolver:�ֽ�����������
	  		 * ָ��Ҫ��ѯ�����ݿ����Ƽ��ϱ�����ƣ�
	  		 * ָ����ѯ���ݿ���е��ļ��У����ص��α��н�������Ӧ����Ϣ��null�򷵻�������Ϣ��
	  		 * ָ����ѯ������
	  		 * ���selection���û�У��Ļ�����ô���String�������Ϊnull�����еĻ�����ʵ��ֵ���棻
	  		 * ָ����ѯ���������˳��
	  		 * Cursor cursor = contentResolver.query(contentUri, projection, selection, selectionArgs, sortOrder);
	  		 */
			//��ȡ������Ϣ
			Cursor mycursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,null,
					null, null,MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
			if(db != null)
			{
				db.open();
				db.deleteTitle();//���µ�������ǰ��ɾ�����ݿ���������
				
				if(mycursor != null && mycursor.moveToFirst())
				{
					do{
						String title = mycursor.getString(mycursor.getColumnIndex(Media.DISPLAY_NAME)); 
						//Ӧ���ж�����Щ��Ƶ����MP3��ʽ�ģ�������Щ��m4a��ʽ��,ֻ���.mp3��ʽ�ĸ���
						//M4A��MPEG4��Ƶ��׼�ļ�����չ��,��ͨ��MPEG4�ļ���չ����.mp4��m4a����MPEG4����Ƶ����Ƶ�ļ�
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
		  					//���ݸ������Լ���������ȡ��ǰ�����Ƿ���ר��������У����ר��·����ӵ����ݿ��У�����ר��·����ӣ�nothisfile
		  					if(music_album_path.size() > 0)
		  					{
		  						//apath = getAlbumpath(mycursor.getString(mycursor.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM_KEY)));
		  						//�����ǰ����и��������Ǹ�����,substring:��ȡ�ַ����е��Ӵ�.jpg--4λ
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
		  					//���ݸ������Լ���������ȡ��ǰ�����Ƿ��и�ʣ�����У���Ѹ��·����ӵ����ݿ��У����򣬸��·����ӣ�nothisfile
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
					//��ʱ���Ѿ����������ݱ��浽�������ݿ��У���һ���㲥֪ͨMusicActivity���½���
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
	//�õ�ר��ͼƬ�̣߳��߳̽�������״̬��true���������ֲ��������صĸ�����ר��·�����ܰ���album�����ǰ���Cover
	private Runnable getalbumRunnable = new Runnable() {
		@Override
		public void run() {
			//��ȡ���ذ�����.album�ַ�����·��
			BrowserFile(common_file,"album");
			BrowserFile(common_file,"Cover");
			startoneget = true;
		}
	};
	//�õ�����̣߳��߳̽�������״̬��true
	private Runnable getwordRunnable = new Runnable() {
		@Override
		public void run() {
			//��ȡ���ذ�����lyrics�ַ�����·��
			BrowserwordFile(common_file,"lyrics");
			BrowserwordFile(common_file,"Lyric");
			starttwoget = true;
		}
	};
	
	//�����ض����ַ���key��/storage/emulated/0/�²������з���Ҫ���ר��ͼƬ�ļ���
	//BrowserFile:����ļ�����ӡ��ʽ�����У�����Ѹ - ������.jpg
	private void BrowserFile(File fileold,String key) 
	{  
		try{
			File[] files = fileold.listFiles();//��ȡ��ǰ�ļ����µ������ļ����ļ���
			if(files.length>0)
			{
				for(int j=0;j<files.length;j++)
				{
					album_path = "";
					if(!files[j].isDirectory())  //Ŀ¼���ļ���,����ʾ����һ��Ŀ¼�򷵻�true
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
						this.BrowserFile(files[j],key); //�����ļ����µ��ļ�
					}
				}
			}
		}
		catch(Exception e)
		{
			
		}
	}
	
	//�����ص���ַ���key��/storage/emulated/0/�²������з���Ҫ��ĸ���ļ�����ӡ��ʽ�����У������ - ��������.lrc
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
	//����index����ȡ��Ӧλ�õ�ר��URL
	private int getalbumIndex(String title,String sname)
	{
		//��ʽ������ - 2002��ĵ�һ��ѩ
		int index = -1;
		String allname = "";//����
		if(title.contains(" - "))
		{
			allname = title.split(" - ")[1]; // ���� [mqms2]; ħ���е���ʹ
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
			if(music_album_path.get(i).contains(title))  //����ר��ͼƬ·��ֱ�Ӱ������������Ѿ�ȥ��.mp3������
			{
				index = i;
				break;
			}
			else if(music_album_path.get(i).contains(allname))  //����ר��ͼƬ·����������
			{
				index = i;
				break;
			}
			else if(music_album_path.get(i).contains(sname))  //����ר��ͼƬ·�������и�����
			{
				index = i;
				break;
			}
		}
		return index;
	}
	//////////////////////////////////////////////////////////////////////////////////////////////
	//����index����ȡ��Ӧλ�õĸ��URL,����� - ��������.lrc
	private int getwordIndex(String name,String sname)
	{
		int index = -1;
		String allname = "";
		if(name.contains(" - "))
		{
			allname = name.split(" - ")[1]; //���� [mqms2]
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
			
			/*if(music_word_path.get(i).contains(name))  //���Ǹ��·��ֱ�Ӱ������������Ѿ�ȥ��.mp3������
			{
				index = i;
				break;
			}
			else if(music_word_path.get(i).contains(sname))  //���Ǹ��·�������и�����
			{
				index = i;
				break;
			}*/
		}
		
		return index;
	}
	//////////////////////////////////////////////////////////////////////////////////////////////
	//ɾ�����ض�Ӧ�ĸ���
	private Runnable delRunnable = new Runnable() {
		@Override
		public void run() {
			File musicfile = new File(del_url);
			if(musicfile.exists())
			{
				if (musicfile.isFile()) { 
					//���ÿ�ִ��
					musicfile.setExecutable(true, false);
					//���ÿɶ�
					musicfile.setReadable(true, false);
					//���ÿ�д
					musicfile.setWritable(true, false);
					if(musicfile.delete())
		   			{
		   				//������ʾ
		   				Toast.makeText(mcontext, "ɾ���ɹ�", Toast.LENGTH_LONG).show();
		   			}
				}  
			}
			else
			{
				//������ʾ
				Toast.makeText(mcontext, "�������Ѳ����ڣ�ɾ��ʧ��", Toast.LENGTH_LONG).show();
			}
		}
	};
	//////////////////////////////////////////////////////////////////////////////////////////////
	//��һ�ֵõ�ר��ͼƬ����������album���Ի�ȡ�����Ǹ÷���Ŀǰ�в�ͨ
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
			// û��ר�����壬��Ĭ��ͼƬ
			music_album_url = "nothisfile";
		}
		return music_album_url;
	}
	//////////////////////////////////////////////////////////////////////////////////////////////
	//��ȡ������Ƶ�ļ�
	private void getVideoFile(final List<VideoInfo> list, File file) 
	{
		// �����Ƶ�ļ�  
        file.listFiles(new FileFilter() {  
  
            @Override  
            public boolean accept(File file) {  
                // sdCard�ҵ���Ƶ����  
                String name = file.getName();  
                int i = name.indexOf('.');  
                if (i != -1) {  
                    name = name.substring(i); 
                    //ֻ��MP4��ʽ��
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
     * ��ȡ��Ƶ������ͼ
     * ��ͨ��ThumbnailUtils������һ����Ƶ������ͼ��Ȼ��������ThumbnailUtils������ָ����С������ͼ��
     * �����Ҫ������ͼ�Ŀ�͸߶�С��MICRO_KIND��������Ҫʹ��MICRO_KIND��Ϊkind��ֵ���������ʡ�ڴ档
     * @param videoPath ��Ƶ��·��
     * @param width ָ�������Ƶ����ͼ�Ŀ��
     * @param height ָ�������Ƶ����ͼ�ĸ߶ȶ�
     * @param kind ����MediaStore.Images(Video).Thumbnails���еĳ���MINI_KIND��MICRO_KIND��
     *            ���У�MINI_KIND: 512 x 384��MICRO_KIND: 96 x 96
     * @return ָ����С����Ƶ����ͼ
     */
    private Bitmap getVideoThumbnail(String videoPath, int width, int height,int kind) {
        Bitmap bitmap = null;
        // ��ȡ��Ƶ������ͼ
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
    //��ȡ��Ƶ�ļ���ʱ��
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
    //��ȡ���յ���Ƶ�б�
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
    //�����̣߳���ȡ��Ƶ��Դ
    private Runnable getvideoRunnable = new Runnable() {
    	@Override
    	public void run() {
    		/*
    		getVideoFile(allVideoList,Environment.getExternalStorageDirectory());// �����Ƶ�ļ�
			hasgetvideodata = true;
			firstgetvideodata = true;
			video_num = allVideoList.size();
			//��ȡ������������Ҫ����Ƶ�б���������Ƶ������Ƶ����ͼ����Ƶ·������Ƶʱ��
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
