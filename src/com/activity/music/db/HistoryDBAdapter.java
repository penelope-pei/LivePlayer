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
	//����
	public static String KEY_ROWID = "_id";
	//������
	public static final String KEY_NAME = "name";
	//������
	public static final String KEY_SNAME = "sname";
	//����·��
	public static final String KEY_URL = "url";
	//����ʱ��
	public static final String KEY_DURATION = "duration";
	//������С
	public static final String KEY_SIZE = "size";
	//����ר��
	public static final String KEY_ALBUM = "album";
	//�Ƿ�ϲ��
	public static final String KEY_FAVORITE = "favorite";
	//���·��
	public static final String KEY_WORDS_URL = "word_url";
	//ר��·��
	public static final String KEY_ALBUM_URL = "album_url";
	//��ӡ��־
	private static final String TAG = "HistoryDBAdapter";
	//���ݿ�����
	private static final String DATABASE_NAME = "musichistory.db";
	//���ݱ���,һ�����ݿ���Դ��������
	private static final String DATABASE_TABLE = "musichistorylist";
	//���ݿ�汾��
	private static final int DATABASE_VERSION = 2;
	
	//�������ݿ�SQL���
	//�ܹ���7�У��ֱ�Ϊ����������������������·��������ʱ����������С������ר��,�Ƿ�ϲ��
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
		 * SQLiteOpenHelper ��һ�������࣬�����������ݿ�Ĵ����Ͱ汾���ṩ������Ĺ���:
		 *  
		 *��һ��getReadableDatabase() �� getWriteableDatabase() ���Ի��SQLiteDatabase����ͨ���ö�����Զ����ݿ���в���
		 * 
		 *�ڶ���ʵ������������
		 *�����캯�������ø��� SQLiteOpenHelper �Ĺ��캯��;
		 *��onCreate��������.�������ݿ�󣬶����ݿ�Ĳ���;
		 *��onUpgrage()�������������ݿ�汾�Ĳ���;
		 *  ��������˶����ݿ�Ĳ������������ Activity �Ѿ��ر�),��Ҫ���� SQLiteDatabase�� Close()�������ͷŵ����ݿ�����
		 */
		DatabaseHelper(Context context)
		{
			/**
			 * DatabaseHelper(Context context, String name, CursorFactory factory,int version)��
			 *  ��1�������Ļ���;��2:���ݿ�����(��.db��β) ; 
			 * ��3���α깤��(Ĭ��Ϊnull) ; ��4������ʹ�����ݿ�ģ�Ͱ汾��֤��
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
	
	//---�����ݿ⣬�׳��쳣����ֹ����---
	public HistoryDBAdapter open() throws SQLException
	{
		db = DBHelper.getWritableDatabase();
		return this;
	}
	
	//---�ر����ݿ�---
	public void close()
	{
		DBHelper.close();
	}
	
	//---�����ݿ��в���һ��������Ϣ---
	public long insertTitle(String name, String sname, String url,long duration,long size,
			String album,int favorite,String word_url,String album_url)
	{
		ContentValues initialValues = new ContentValues();//�洢��/ֵ��
		
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
	
	//---ɾ��һ��ָ������---
	public void deleteTitle(long rowId)
	{
		SQLiteDatabase db = DBHelper.getReadableDatabase();  
		db.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null);
	}
	
	//--ɾ����������--
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
				//ִ��ɾ����������SQL���
				db.delete(DATABASE_TABLE, KEY_ROWID + "=" + music_id, null);
			} while (old_cursor.moveToNext());
		}
	}
	
	//---������������---
	public Cursor getAllTitles()
	{
		return db.query(DATABASE_TABLE, new String[] {
				KEY_ROWID,KEY_NAME,KEY_SNAME,KEY_URL,
				KEY_DURATION,KEY_SIZE,KEY_ALBUM,KEY_FAVORITE,
				KEY_WORDS_URL,KEY_ALBUM_URL},null,null,null,null,null);
	}
	
	//---����һ��ָ������---
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
	
	//---����---
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
	
	//����id����ѯ���ݿ⣬�����ѯidΪ6�����ݿ����ݲ�����
	public MusicInfo find(Integer id){  
        //���ֻ�����ݽ��ж�ȡ������ʹ�ô˷���
		SQLiteDatabase db = DBHelper.getReadableDatabase();
		//�õ��α�  
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
	
	//������������ѯ���ݿ⣬�����ѯ1--20�����ݿ����ݲ�����,offset:λ��
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
        //���Ҫ�����ݽ��и��ģ��͵��ô˷����õ����ڲ������ݿ��ʵ��,�÷����Զ���д��ʽ�����ݿ�  
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
