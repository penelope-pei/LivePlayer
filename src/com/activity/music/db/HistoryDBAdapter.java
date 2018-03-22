package com.activity.music.db;

import java.util.ArrayList;
import java.util.List;

import com.activity.info.MusicInfo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class HistoryDBAdapter {
	//行数
	public static String KEY_ROWID = "_id";
	//歌曲名
	public static final String KEY_NAME = "name";
	//歌手名
	public static final String KEY_SNAME = "sname";
	//歌曲路径
	public static final String KEY_URL = "url";
	//歌曲时长
	public static final String KEY_DURATION = "duration";
	//歌曲大小
	public static final String KEY_SIZE = "size";
	//歌曲专辑
	public static final String KEY_ALBUM = "album";
	//是否喜爱
	public static final String KEY_FAVORITE = "favorite";
	//歌词路径
	public static final String KEY_WORDS_URL = "word_url";
	//专辑路径
	public static final String KEY_ALBUM_URL = "album_url";
	//打印日志
	private static final String TAG = "HistoryDBAdapter";
	//数据库名称
	private static final String DATABASE_NAME = "musichistory.db";
	//数据表名,一个数据库可以创建多个表
	private static final String DATABASE_TABLE = "musichistorylist";
	//数据库版本号
	private static final int DATABASE_VERSION = 2;
	
	//创建数据库SQL语句
	//总共有7列，分别为：歌曲名，歌手名，歌曲路径，歌曲时长，歌曲大小，歌曲专辑,是否喜爱
	private static final String DATABASE_CREATE =
			"create table musichistorylist (_id integer primary key autoincrement, "
			+ "name text not null, sname text not null, url text not null," 
			+ "duration text not null," +"size text not null,"+ "album text not null," 
			+ "favorite text not null,word_url text not null,album_url text not null);";
	
	private final Context context;
	private DatabaseHelper DBHelper;
	private SQLiteDatabase db;
	
	public HistoryDBAdapter(Context ctx)
	{
		this.context = ctx;
		DBHelper = new DatabaseHelper(context);
	}
	
	private static class DatabaseHelper extends SQLiteOpenHelper
	{
		/**
		 * SQLiteOpenHelper 是一个辅助类，用来管理数据库的创建和版本，提供两方面的功能:
		 *  
		 *第一：getReadableDatabase() 、 getWriteableDatabase() 可以获得SQLiteDatabase对象，通过该对象可以对数据库进行操作
		 * 
		 *第二：实现三个方法：
		 *　构造函数，调用父类 SQLiteOpenHelper 的构造函数;
		 *　onCreate（）方法.创建数据库后，对数据库的操作;
		 *　onUpgrage()方法。更改数据库版本的操作;
		 *  当你完成了对数据库的操作（例如你的 Activity 已经关闭),需要调用 SQLiteDatabase的 Close()方法来释放掉数据库连接
		 */
		DatabaseHelper(Context context)
		{
			/**
			 * DatabaseHelper(Context context, String name, CursorFactory factory,int version)；
			 *  参1：上下文环境;参2:数据库名称(以.db结尾) ; 
			 * 参3：游标工厂(默认为null) ; 参4：代表使用数据库模型版本的证书
			 */ 
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}
		
		@Override
		public void onCreate(SQLiteDatabase db)
		{
			 try  
	            {  
	                db.execSQL(DATABASE_CREATE);  
	            }  
	            catch(SQLException e)  
	            {  
	                e.printStackTrace();  
	            } 
		}
		
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion,int newVersion)
		{
			db.execSQL("DROP TABLE IF EXISTS musichistorylist");
			onCreate(db);
		}
	}
	
	//---打开数据库，抛出异常，防止出错---
	public HistoryDBAdapter open() throws SQLException
	{
		db = DBHelper.getWritableDatabase();
		return this;
	}
	
	//---关闭数据库---
	public void close()
	{
		DBHelper.close();
	}
	
	//---向数据库中插入一条音乐信息---
	public long insertTitle(String name, String sname, String url,long duration,long size,
			String album,int favorite,String word_url,String album_url)
	{
		ContentValues initialValues = new ContentValues();//存储键/值对
		
		initialValues.put(KEY_NAME, name);
		initialValues.put(KEY_SNAME, sname);
		initialValues.put(KEY_URL, url);
		initialValues.put(KEY_DURATION, duration);
		initialValues.put(KEY_SIZE, size);
		initialValues.put(KEY_ALBUM, album);
		initialValues.put(KEY_FAVORITE, favorite);
		initialValues.put(KEY_WORDS_URL, word_url);
		initialValues.put(KEY_ALBUM_URL, album_url);
		return db.insert(DATABASE_TABLE, null, initialValues);
	}
	
	//---删除一个指定音乐---
	public void deleteTitle(long rowId)
	{
		SQLiteDatabase db = DBHelper.getReadableDatabase();  
		db.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null);
	}
	
	//--删除所有数据--
	public void deleteTitle()
	{
		SQLiteDatabase db = DBHelper.getReadableDatabase();  
		Cursor old_cursor = db.query(DATABASE_TABLE, new String[] {
				KEY_ROWID,KEY_NAME,KEY_SNAME,KEY_URL,
				KEY_DURATION,KEY_SIZE,KEY_ALBUM,KEY_FAVORITE,
				KEY_WORDS_URL,KEY_ALBUM_URL},null,null,null,null,null);
		if (old_cursor.moveToFirst())
		{
			do {
				long music_id = old_cursor.getLong(0);
				//执行删除单个数据SQL语句
				db.delete(DATABASE_TABLE, KEY_ROWID + "=" + music_id, null);
			} while (old_cursor.moveToNext());
		}
	}
	
	//---检索所有音乐---
	public Cursor getAllTitles()
	{
		return db.query(DATABASE_TABLE, new String[] {
				KEY_ROWID,KEY_NAME,KEY_SNAME,KEY_URL,
				KEY_DURATION,KEY_SIZE,KEY_ALBUM,KEY_FAVORITE,
				KEY_WORDS_URL,KEY_ALBUM_URL},null,null,null,null,null);
	}
	
	//---检索一个指定标题---
	public Cursor getTitle(long rowId) throws SQLException
	{
		Cursor mCursor = db.query(true, DATABASE_TABLE, new String[] {
						KEY_ROWID,KEY_NAME,KEY_SNAME,KEY_URL,
						KEY_DURATION,KEY_SIZE,KEY_ALBUM,KEY_FAVORITE,
						KEY_WORDS_URL,KEY_ALBUM_URL},
				KEY_ROWID + "=" + rowId,null,null,null,null,null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}
	
	//---更新---
	public boolean updateTitle(long rowId, String name, String sname, String url, 
		              	long duration, long size, String album, int favorite,String word_url,String album_url)
	{
		ContentValues args = new ContentValues();
		
		args.put(KEY_NAME, name);
		args.put(KEY_SNAME, sname);
		args.put(KEY_URL, url);
		args.put(KEY_DURATION, duration);
		args.put(KEY_SIZE, size);
		args.put(KEY_ALBUM, album);
		args.put(KEY_FAVORITE, favorite);
		args.put(KEY_WORDS_URL, word_url);
		args.put(KEY_ALBUM_URL, album_url);
		return db.update(DATABASE_TABLE, args,KEY_ROWID + "=" + rowId, null) > 0;
	}
	
	//根据id来查询数据库，比如查询id为6的数据库内容并返回
	public MusicInfo find(Integer id){  
        //如果只对数据进行读取，建议使用此方法
		SQLiteDatabase db = DBHelper.getReadableDatabase();
		//得到游标  
        Cursor cursor = db.rawQuery("select * from musichistorylist where _id=?", new String[]{id.toString()});
        if(cursor.moveToFirst()){ 
        	boolean favorite = false;
            String name = cursor.getString(cursor.getColumnIndex("name"));  
            String sname = cursor.getString(cursor.getColumnIndex("sname"));
            String url = cursor.getString(cursor.getColumnIndex("url"));
            long duration = cursor.getLong(cursor.getColumnIndex("duration")); 
            long size = cursor.getLong(cursor.getColumnIndex("size"));  
            String album = cursor.getString(cursor.getColumnIndex("album"));
            if(cursor.getInt(cursor.getColumnIndex("favorite")) == 1)
            {
            	favorite = true;
            	
            }
            else
            {
            	favorite = false;
            }
            String word_url = cursor.getString(cursor.getColumnIndex("word_url"));
            String album_url = cursor.getString(cursor.getColumnIndex("album_url"));
            
            
            MusicInfo musicinfo = new MusicInfo();  
            
            musicinfo.setName(name);
            musicinfo.setSinger(sname);
            musicinfo.setUrl(url);
            musicinfo.setDuration(duration);
            musicinfo.setSize(size);
            musicinfo.setAlbum(album);
            musicinfo.setFavorite(favorite);
            musicinfo.setWordsUrl(word_url);
            musicinfo.setAlbumUrl(album_url);
            return musicinfo;  
        }  
        return null;  
    }
	
	//根据区间来查询数据库，比如查询1--20条数据库内容并返回,offset:位移
	public List<MusicInfo> getScrollData(Integer offset, Integer maxResult)
	{  
        List<MusicInfo> musics = new ArrayList<MusicInfo>();  
        SQLiteDatabase db = DBHelper.getReadableDatabase();  
        Cursor cursor = db.rawQuery("select * from musichistorylist limit ?,?",  
                new String[]{offset.toString(), maxResult.toString()});  
        
        while(cursor.moveToNext()){  
        	boolean favorite = false;
        	String name = cursor.getString(cursor.getColumnIndex("name"));  
            String sname = cursor.getString(cursor.getColumnIndex("sname"));
            String url = cursor.getString(cursor.getColumnIndex("url"));
            long duration = cursor.getLong(cursor.getColumnIndex("duration")); 
            long size = cursor.getLong(cursor.getColumnIndex("size"));  
            String album = cursor.getString(cursor.getColumnIndex("album"));
            if(cursor.getInt(cursor.getColumnIndex("favorite")) == 1)
            {
            	favorite = true;
            	
            }
            else
            {
            	favorite = false;
            }
            String word_url = cursor.getString(cursor.getColumnIndex("word_url"));
            String album_url = cursor.getString(cursor.getColumnIndex("album_url"));
            
            MusicInfo musicinfo = new MusicInfo();  
            musicinfo.setName(name);
            musicinfo.setSinger(sname);
            musicinfo.setUrl(url);
            musicinfo.setDuration(duration);
            musicinfo.setSize(size);
            musicinfo.setAlbum(album);
            musicinfo.setFavorite(favorite);
            musicinfo.setWordsUrl(word_url);
            musicinfo.setAlbumUrl(album_url);
            
            musics.add(musicinfo);  
        }  
        cursor.close();  
        return musics;  
    }  
	
	public void save(MusicInfo musicinfo){  
        //如果要对数据进行更改，就调用此方法得到用于操作数据库的实例,该方法以读和写方式打开数据库  
        SQLiteDatabase db = DBHelper.getWritableDatabase();  
        db.execSQL("insert into musichistorylist (name,sname,url,duration,size,album,favorite,word_url,album_url) values(?,?,?,?,?,?,?,?,?)",  
                new Object[]{musicinfo.getName(),musicinfo.getSinger(),musicinfo.getUrl(),
        							musicinfo.getDuration(),musicinfo.getSize(),musicinfo.getAlbum(),
        							musicinfo.getWordsUrl(),musicinfo.getAlbumUrl()});  
    } 
	
	public void clearFeedTable(){
		//DBHelper.onUpgrade(db, 1, 2);
		db.execSQL("DROP TABLE IF EXISTS musichistorylist");
		DBHelper.onCreate(db);
	}
	
}
