package com.activity.adapter;

import java.util.List;

import com.activity.BackgroundSettingActivity;
import com.activity.utils.LruMemoryCache;
import com.example.liveplayer.R;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class BackGroundAdapter extends BaseAdapter{

	private Context mcontext;
	private LruMemoryCache myMemoryCache;
	private int[] resIDs = null;
	
	private class ViewHolder{
		private ImageView iv_background;
	}
	
	//构造方法
	public BackGroundAdapter(Context context,LruMemoryCache myMemoryCache,int[] resIDs)
	{
		this.mcontext = context;
		this.myMemoryCache = myMemoryCache;
		this.resIDs = resIDs;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return resIDs.length;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return resIDs[position];
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder viewHolder = null;
		if(view == null)
		{
			viewHolder = new ViewHolder();
			view = LayoutInflater.from(mcontext).inflate(R.layout.background_item,null);
			
			viewHolder.iv_background = (ImageView) view.findViewById(R.id.background_imageview);
			view.setTag(viewHolder);
		}
		else{
			viewHolder = (ViewHolder) view.getTag();
		}
		
		viewHolder.iv_background.setImageResource(resIDs[position]);
		
		return view;
	}
	
	public void loadBitmap(int resId, ImageView imageView) {  
        final String imageKey = String.valueOf(resId);  
        final Bitmap bitmap = myMemoryCache.getBitmapFromMemCache(imageKey);  
        if (bitmap != null) {  
            imageView.setImageBitmap(bitmap);  
        } else {  
            imageView.setImageResource(R.drawable.ic_launcher);  
            BitmapWorkerTask task = new BitmapWorkerTask(imageView);  
            task.execute(resId);  
        }  
    }  
  
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {  
        // 源图片的高度和宽度  
        final int height = options.outHeight;  
        final int width = options.outWidth;  
        int inSampleSize = 1;  
        if (height > reqHeight || width > reqWidth) {  
            // 计算出实际宽高和目标宽高的比率  
            final int heightRatio = Math.round((float) height / (float) reqHeight);  
            final int widthRatio = Math.round((float) width / (float) reqWidth);  
            // 选择宽和高中最小的比率作为inSampleSize的值，这样可以保证最终图片的宽和高  
            // 一定都会大于等于目标的宽和高。  
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;  
        }  
        return inSampleSize;  
    }  
  
    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {  
        // 第一次解析将inJustDecodeBounds设置为true，来获取图片大小  
        final BitmapFactory.Options options = new BitmapFactory.Options();  
        options.inJustDecodeBounds = true;  
        BitmapFactory.decodeResource(res, resId, options);  
        // 调用上面定义的方法计算inSampleSize值  
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);  
        // 使用获取到的inSampleSize值再次解析图片  
        options.inJustDecodeBounds = false;  
        return BitmapFactory.decodeResource(res, resId, options);  
    }  
  
    class BitmapWorkerTask extends AsyncTask<Integer, Void, Bitmap> {  
        ImageView mImageView;  
  
        public BitmapWorkerTask(ImageView imageView) {  
            mImageView = imageView;  
        }  
  
        // 在后台加载图片。  
        @Override  
        protected Bitmap doInBackground(Integer... params) {  
            final Bitmap bitmap = decodeSampledBitmapFromResource(mcontext.getResources(), params[0], 100, 100);  
            myMemoryCache.addBitmapToMemoryCache(String.valueOf(params[0]), bitmap);  
            return bitmap;  
        }  
  
        @Override  
        protected void onPostExecute(Bitmap result) {  
            mImageView.setImageBitmap(result);  
            super.onPostExecute(result);  
        }  
    }

}
