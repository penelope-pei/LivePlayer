package com.activity.utils;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

/**
 * LruMemoryCache：开源框架默认的内存缓存类，缓存BitMap的强引用，其具体实现类似于android.support.v4.util.LruCache类，
 * 且都是通过LinkedHashMap的委派具体实现的，这里将LruChace的K,V分别固化为了String，和Bitmap
 * 并且简化了其中某些函数的实现
 * 注意LinkedHashMap是非线性安全的，所以要通过synchronized去手动实现线程安全
 * 
 */

public class LruMemoryCache extends LruCache<String, Bitmap>{
	public LruMemoryCache(int maxSize) {  
        super(maxSize);  
    }  
      
    @Override  
    protected int sizeOf(String key, Bitmap bitmap) {  
        // 重写此方法来衡量每张图片的大小，默认返回图片数量。  
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
