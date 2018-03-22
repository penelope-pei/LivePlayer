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
	
	public Timer timer = null;  //��ʱ������ʱ��
	private Animation operatingAnim = null;  //��������
	
	TimerTask task = new TimerTask(){
		public void run(){
			Intent intent = new Intent(WelcomeActivity.this,MHomeActivity.class);
			startActivity(intent);
			finish();
			/**
			 * �Զ��巭ҳЧ����
			 *�����л����������ұ߽��룬����˳����˷����������startActivity����finish()֮��
			 *��һ����Ϊ�������Activity�Ķ���Ч��,�ڶ�������Ϊǰһ��Activity�뿪ʱ�Ķ�����
			 *ֻ��Android��2.0(SdkVersion�汾��Ϊ5)�Ժ�İ汾��֧��
			 * 
			 * �������Ƴ�Ч��:overridePendingTransition(R.anim.push_up_in,R.anim.push_up_out);
			 */
			overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
		}
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);		
		requestWindowFeature(Window.FEATURE_NO_TITLE);//ȥ��������
		//ȥ��Activity�����״̬��
		//getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);  
		
		// ������ɾ��֮ǰ���Ƕ����֪ͨ   
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
		 * Bitmap������һ��ԭʼ��λͼ�����ҿ��Զ�λͼ����һϵ�еı任����
		 * Bitmap�̳�Parcelable����һ�����Կ���̴���Ķ���;
		 * BitmapDrawable�̳�Drawable,��Drawableֻ��һ�������࣬������һ�����������������
		 * BitmapDrawable���Ƿ�װ��һ��λͼ,��Xml��ʽ�����Զ�ԭʼ��λͼ����һϵ�еĴ����翹��ݣ����죬�����
		 *
		 * */
		
		//��ͼƬ��bitmap���󱣴棬BitmapDrawable������Ե���getBitmap�������õ����λͼ  
		Bitmap bitmap = ((BitmapDrawable) getResources().getDrawable(R.drawable.ic_icon)).getBitmap();
		Bitmap bm = makeRoundCorner(bitmap);
		iv_welcome.setImageBitmap(makeRoundCorner(bm,50));
		
		//loadAnimation:���붯��
		operatingAnim = AnimationUtils.loadAnimation(this, R.anim.wel_tip);  
		//setInterpolator��ʾ������ת����:LinearInterpolatorΪ����Ч����
		//AccelerateInterpolatorΪ����Ч����DecelerateInterpolatorΪ����Ч����
		LinearInterpolator lin = new LinearInterpolator();  
		operatingAnim.setInterpolator(lin);
		if (operatingAnim != null) {  
			iv_welcome.startAnimation(operatingAnim);  
		}  
		
		
		timer = new Timer();
		timer.schedule(task, 1000*3);//3���ִ��
		
		//��������һ����������������ں�̨���еģ�ר��������ȡ���������ļ���Դ
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

	//�����ת���ں���(������Ϊ�˲��ػ�activity)ʱ��������⣬����ת����ƫ�ƣ����¶�����תƫ��ԭ��ת���ġ�
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
		//�ͷ�ͼƬ��Դ�Ͷ�����Դ����Ȼ���ڴ�й¶
		if (operatingAnim != null) 
		{  
			iv_welcome.clearAnimation(); 
		}
		timer.cancel();
		releaseImageViews();
	}
	
	/*
	 * ���´���ʵ��Բ��ͼƬ��ʾ��Ҫ��ʾԲ��ͼƬ�������ԭͼ�����ó������Σ���Ȼ������Բ��
	 * ��ԭͼ��������ε�ͼƬ
	 * 
	 * Drawable:�ӿ�,�ж�����࣬���磺λͼ(BitmapDrawable)��ͼ��(ShapeDrawable)��ͼ��(LayerDrawable)��
	 *
	 * PorterDuffXfermode��һ���ǳ�ǿ���ת��ģʽ��������ʹ��ͼ��ϳɵ�16��Porter-Duff���������һ��������Paint��������е�Canvasͼ����н�����
	 * ʹ�� PorterDuff ģʽ����һ��ͼ����ģʽ��PorterDuff����������������ͼ��ϳɵĻ����ַ���
	 * ͨ�����ʹ�� Porter-Duff ��������������� 2Dͼ��ĺϳɣ�
	 *
	 * */
	public static Bitmap makeRoundCorner(Bitmap bitmap) 
	{ 
	  int width = bitmap.getWidth();  // ��ȡλͼ�Ŀ�� 
	  int height = bitmap.getHeight(); // ��ȡλͼ�ĸ߶�
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
	  //����ָ����ʽ����С��λͼ  
	  Bitmap output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444); 
	  //����һ��Canvas���󣬲����ڻ滭�ɹ��󣬽��û�ͼ����ת��ΪDrawableͼƬ����ͨ��setBitmap(bitmap)���ֳ�����
	  Canvas canvas = new Canvas(output);  
	  int color = 0xff424242; 	  
	  Paint paint = new Paint();   //���ʶ��� paint
	  Rect rect = new Rect(left, top, right, bottom); //���Σ�rectangular,����һ������
	  RectF rectF = new RectF(rect); 
	  
	  paint.setAntiAlias(true); //��ֹ��Ե�ľ��
	  canvas.drawARGB(0, 0, 0, 0); 
	  paint.setColor(color);  //������ɫ����ʾ��ͼ����  
	  canvas.drawRoundRect(rectF, roundPx, roundPx, paint); //���Ʒ���ͼ
	  // ����ͼ����ģʽ ��PorterDuff.Mode.SRC_IN��ȡ������ƽ�������ʾ�ϲ�ͼƬ��������
	  paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));  
	  canvas.drawBitmap(bitmap, rect, rect, paint); // �������ڱ�ɫͼ
	  return output; 
	}
	
	//�������α��Բ��,Բ��
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
	//�ͷ�
	private void releaseImageViews() {
		releaseImageView(iv_welcome);
	}
    // Drawable ���������й© ,�ص�
	/**
	 * @param imageView
	 *����ͼʵ��С�ڴ���ٻ���ɻ��У�Ϊ�˱���رյĻ����Ҫ�����Щ�ɻ��ƺ��ڴ�й©�������Լ��Ļص�Ϊnull
     *���ڱ��ֿɻ��ƻ����е�ÿ�������Ҫ�����code(����)������� setImageDrawable�����ƣ�
     *����MyImageView�ࣨ��չ��ImageView ��code��
	 */
	private void releaseImageView(ImageView imageView) {
		Drawable d = imageView.getDrawable();
		if (d != null)
		{
			d.setCallback(null);
		}
	}
	
}
