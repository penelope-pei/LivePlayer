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
	
	//���췽��
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
        // ԴͼƬ�ĸ߶ȺͿ��  
        final int height = options.outHeight;  
        final int width = options.outWidth;  
        int inSampleSize = 1;  
        if (height > reqHeight || width > reqWidth) {  
            // �����ʵ�ʿ�ߺ�Ŀ���ߵı���  
            final int heightRatio = Math.round((float) height / (float) reqHeight);  
            final int widthRatio = Math.round((float) width / (float) reqWidth);  
            // ѡ���͸�����С�ı�����ΪinSampleSize��ֵ���������Ա�֤����ͼƬ�Ŀ�͸�  
            // һ��������ڵ���Ŀ��Ŀ�͸ߡ�  
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;  
        }  
        return inSampleSize;  
    }  
  
    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {  
        // ��һ�ν�����inJustDecodeBounds����Ϊtrue������ȡͼƬ��С  
        final BitmapFactory.Options options = new BitmapFactory.Options();  
        options.inJustDecodeBounds = true;  
        BitmapFactory.decodeResource(res, resId, options);  
        // �������涨��ķ�������inSampleSizeֵ  
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);  
        // ʹ�û�ȡ����inSampleSizeֵ�ٴν���ͼƬ  
        options.inJustDecodeBounds = false;  
        return BitmapFactory.decodeResource(res, resId, options);  
    }  
  
    class BitmapWorkerTask extends AsyncTask<Integer, Void, Bitmap> {  
        ImageView mImageView;  
  
        public BitmapWorkerTask(ImageView imageView) {  
            mImageView = imageView;  
        }  
  
        // �ں�̨����ͼƬ��  
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
