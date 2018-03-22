package com.activity;


import java.util.Timer;
import java.util.TimerTask;

import com.activity.adapter.BackGroundAdapter;
import com.activity.service.GetDataService;
import com.activity.service.PlayService;
import com.example.liveplayer.R;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class WelcomeActivity extends Activity{
	
	private String ACTION_TYPE = "action_type";
	private String GET_DATA = "get_data";
	
	private ImageView iv_welcome;
	private LinearLayout ll_welcome;
	
	public Timer timer = null;  //定时器，计时器
	private Animation operatingAnim = null;  //动画操作
	
	TimerTask task = new TimerTask(){
		public void run(){
			Intent intent = new Intent(WelcomeActivity.this,MHomeActivity.class);
			startActivity(intent);
			finish();
			/**
			 * 自定义翻页效果：
			 *设置切换动画，从右边进入，左边退出，此方法必须放在startActivity或者finish()之后
			 *第一参数为所进入的Activity的动画效果,第二个参数为前一个Activity离开时的动画，
			 *只有Android的2.0(SdkVersion版本号为5)以后的版本才支持
			 * 
			 * 下往上推出效果:overridePendingTransition(R.anim.push_up_in,R.anim.push_up_out);
			 */
			overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
		}
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);		
		requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏
		//去掉Activity上面的状态栏
		//getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);  
		
		// 启动后删除之前我们定义的通知   
        /*NotificationManager notificationManager = (NotificationManager) this  
                .getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(0);*/
		
		setContentView(R.layout.activity_welcome);
		
		ll_welcome = (LinearLayout)findViewById(R.id.linearlayout_welcome);
		iv_welcome = (ImageView)findViewById(R.id.image_icon);
		
		if(GetDataService.background_id == -1)
		{
			ll_welcome.setBackgroundResource(BackgroundSettingActivity.imgs[0]);
		}
		else
		{
			ll_welcome.setBackgroundResource(BackgroundSettingActivity.imgs[GetDataService.background_id]);
		}
		/**
		 * Bitmap代表了一个原始的位图，并且可以对位图进行一系列的变换操作
		 * Bitmap继承Parcelable，是一个可以跨进程传输的对象;
		 * BitmapDrawable继承Drawable,可Drawable只是一个抽象类，此类是一个存放数据流的载体
		 * BitmapDrawable就是封装了一个位图,以Xml方式，可以对原始的位图进行一系列的处理，如抗锯齿，拉伸，对齐等
		 *
		 * */
		
		//把图片用bitmap对象保存，BitmapDrawable对象可以调用getBitmap方法，得到这个位图  
		Bitmap bitmap = ((BitmapDrawable) getResources().getDrawable(R.drawable.ic_icon)).getBitmap();
		Bitmap bm = makeRoundCorner(bitmap);
		iv_welcome.setImageBitmap(makeRoundCorner(bm,50));
		
		//loadAnimation:载入动画
		operatingAnim = AnimationUtils.loadAnimation(this, R.anim.wel_tip);  
		//setInterpolator表示设置旋转速率:LinearInterpolator为匀速效果，
		//AccelerateInterpolator为加速效果、DecelerateInterpolator为减速效果。
		LinearInterpolator lin = new LinearInterpolator();  
		operatingAnim.setInterpolator(lin);
		if (operatingAnim != null) {  
			iv_welcome.startAnimation(operatingAnim);  
		}  
		
		
		timer = new Timer();
		timer.schedule(task, 1000*3);//3秒后执行
		
		//开机启动一个服务，这个服务是在后台运行的，专门用来获取本地音乐文件资源
		Intent intent = new Intent();
		intent.putExtra(ACTION_TYPE, GET_DATA);
		intent.setClass(WelcomeActivity.this, GetDataService.class);  
		startService(intent);  
		
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if(GetDataService.background_id == -1)
		{
			ll_welcome.setBackgroundResource(BackgroundSettingActivity.imgs[0]);
		}
		else
		{
			ll_welcome.setBackgroundResource(BackgroundSettingActivity.imgs[GetDataService.background_id]);
		}
	}

	//解决了转动在横屏(被设置为了不重绘activity)时会出现问题，即旋转中心偏移，导致动画旋转偏离原旋转中心。
	public void onConfigurationChanged(Configuration newConfig) {  
		super.onConfigurationChanged(newConfig);  
		if (operatingAnim != null && iv_welcome != null && operatingAnim.hasStarted()) {  
			iv_welcome.clearAnimation();  
			iv_welcome.startAnimation(operatingAnim);  
		}  
	}
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		//释放图片资源和动画资源，不然会内存泄露
		if (operatingAnim != null) 
		{  
			iv_welcome.clearAnimation(); 
		}
		timer.cancel();
		releaseImageViews();
	}
	
	/*
	 * 以下代码实现圆形图片显示，要显示圆形图片，必须把原图先设置成正方形，不然会是椭圆形
	 * 把原图变成正方形的图片
	 * 
	 * Drawable:接口,有多个子类，例如：位图(BitmapDrawable)、图形(ShapeDrawable)、图层(LayerDrawable)等
	 *
	 * PorterDuffXfermode是一个非常强大的转换模式，它可以使用图像合成的16条Porter-Duff规则的任意一条来控制Paint如何与已有的Canvas图像进行交互。
	 * 使用 PorterDuff 模式创建一个图层混合模式，PorterDuff则是用于描述数字图像合成的基本手法，
	 * 通过组合使用 Porter-Duff 操作，可完成任意 2D图像的合成；
	 *
	 * */
	public static Bitmap makeRoundCorner(Bitmap bitmap) 
	{ 
	  int width = bitmap.getWidth();  // 获取位图的宽度 
	  int height = bitmap.getHeight(); // 获取位图的高度
	  int left = 0, top = 0, right = width, bottom = height; 
	  float roundPx = height/2; 
	  if (width > height) 
	  { 	  
	    left = (width - height)/2; 
	    top = 0; 
	    right = left + height; 
	    bottom = height; 
	  } 
	  else if (height > width) { 
	    left = 0; 
	    top = (height - width)/2; 
	    right = width; 
	    bottom = top + width; 
	    roundPx = width/2; 
	  } 
	  //创建指定格式、大小的位图  
	  Bitmap output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444); 
	  //创建一个Canvas对象，并且在绘画成功后，将该画图区域转换为Drawable图片或者通过setBitmap(bitmap)显现出来。
	  Canvas canvas = new Canvas(output);  
	  int color = 0xff424242; 	  
	  Paint paint = new Paint();   //画笔对象 paint
	  Rect rect = new Rect(left, top, right, bottom); //矩形：rectangular,剪裁一个区域
	  RectF rectF = new RectF(rect); 
	  
	  paint.setAntiAlias(true); //防止边缘的锯齿
	  canvas.drawARGB(0, 0, 0, 0); 
	  paint.setColor(color);  //设置颜色来显示画图区域  
	  canvas.drawRoundRect(rectF, roundPx, roundPx, paint); //绘制方形图
	  // 设置图层混合模式 ，PorterDuff.Mode.SRC_IN：取两层绘制交集，显示上层图片到画布上
	  paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));  
	  canvas.drawBitmap(bitmap, rect, rect, paint); // 绘制用于变色图
	  return output; 
	}
	
	//把正方形变成圆形,圆角
	public static Bitmap makeRoundCorner(Bitmap bitmap, int px) 
	{ 
	  int width = bitmap.getWidth(); 
	  int height = bitmap.getHeight(); 
	  Bitmap output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888); 
	  
	  Canvas canvas = new Canvas(output); 
	  int color = 0xff424242; 
	  Paint paint = new Paint(); 
	  Rect rect = new Rect(0, 0, width, height); 
	  RectF rectF = new RectF(rect);
	  
	  paint.setAntiAlias(true); 
	  canvas.drawARGB(0, 0, 0, 0); 
	  paint.setColor(color); 
	  canvas.drawRoundRect(rectF, px, px, paint); 
	  paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN)); 
	  canvas.drawBitmap(bitmap, rect, rect, paint); 
	  return output;
	}
	//释放
	private void releaseImageViews() {
		releaseImageView(iv_welcome);
	}
    // Drawable 引起的内在泄漏 ,回调
	/**
	 * @param imageView
	 *在试图实现小内存高速缓存可绘中，为了避免关闭的活动我需要解除那些可绘制后内存泄漏：设置自己的回调为null
     *由于保持可绘制缓存中的每个活动都需要额外的code(代码)，解除绑定 setImageDrawable（绘制）
     *这是MyImageView类（扩展的ImageView ）code：
	 */
	private void releaseImageView(ImageView imageView) {
		Drawable d = imageView.getDrawable();
		if (d != null)
		{
			d.setCallback(null);
		}
	}
	
}
