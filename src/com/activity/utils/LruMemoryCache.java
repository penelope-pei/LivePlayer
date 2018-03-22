package com.activity.utils;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

/**
 * LruMemoryCache����Դ���Ĭ�ϵ��ڴ滺���࣬����BitMap��ǿ���ã������ʵ��������android.support.v4.util.LruCache�࣬
 * �Ҷ���ͨ��LinkedHashMap��ί�ɾ���ʵ�ֵģ����ｫLruChace��K,V�ֱ�̻�Ϊ��String����Bitmap
 * ���Ҽ�������ĳЩ������ʵ��
 * ע��LinkedHashMap�Ƿ����԰�ȫ�ģ�����Ҫͨ��synchronizedȥ�ֶ�ʵ���̰߳�ȫ
 * 
 */

public class LruMemoryCache extends LruCache<String, Bitmap>{
	public LruMemoryCache(int maxSize) {  
        super(maxSize);  
    }  
      
    @Override  
    protected int sizeOf(String key, Bitmap bitmap) {  
        // ��д�˷���������ÿ��ͼƬ�Ĵ�С��Ĭ�Ϸ���ͼƬ������  
        return bitmap.getByteCount() / 1024;  
    }  
      
    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {     
        if (getBitmapFromMemCache(key) == null) {   
            put(key, bitmap);  
        }     
    }     
       
    public Bitmap getBitmapFromMemCache(String key) {     
        return get(key);     
    }
}
