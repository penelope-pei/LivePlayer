package com.activity.view;

import java.util.ArrayList;
import java.util.List;

import com.activity.service.PlayService;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.Shader.TileMode;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

public class LyricView extends TextView  {
	
	private float high;  	//歌词视图高度
	private float width;	//歌词视图宽度
	private Paint CurrentPaint;	    //绘画，高亮部分，正在播放的字体颜色变化
	private Paint NotCurrentPaint;	//非当前画笔对象
	private float TextHigh=70;	//文本高度
	private float TextSize=60;  //正在播放的文本字体大小
	private float noTextSize = 50;	//暂未播放的字体大小
	private float KOKTextSize=20;  //正在播放的文本字体大小
	private float KOKnoTextSize = 18;	//暂未播放的字体大小
	private int Index=0;     //list集合下标
	private long currentTime = 0;// 当前歌曲的播放位置
	private long dunringTime = 0;// 当前句歌词的持续时间
	private long startTime = 0;// 当前句歌词开始的时间
	private float tempW = 0;// 计算画布的中间位置(宽)
	private float tempH = 0;// 计算画布的中间位置(高)
	private float tempYHigh = 0;// 计算卡拉OK模式第一句的Y轴位置
	private float tempYLow = 0;// 计算卡拉OK模式第二句的Y轴位置
	private float textHeight = 40;// 单行字高度
	private int[] paintColorsCurrent = { Color.argb(250, 251, 248, 29),
			Color.argb(250, 255, 255, 255) };// 卡拉OK模式绘画中画笔颜色数组
	private int[] paintColorsDefault = { Color.argb(250, 255, 255, 255),
			Color.argb(250, 255, 255, 255) };// 卡拉OK模式默认画笔颜色数组
	private boolean isKLOK = false;
	
	private List<String> mSentenceEntities=new ArrayList<String>();  
	
	//设置歌词资源， 设置实体列表对象
	public void setSentenceEntities(List<String> mSentenceEntities)
	{
		this.mSentenceEntities = mSentenceEntities;
	}
	/**
	 * 构造方法，该类没有继承Activity，传进来Context可以对它进行处理。
	 * @param context
	 */
	public LyricView(Context context) {
		super(context);
		init();
		// TODO Auto-generated constructor stub
	}
	
	public LyricView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
		// TODO Auto-generated constructor stub
	}

	public LyricView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
		// TODO Auto-generated constructor stub
	}
	/**
	 * 初始化画笔，放在构造方法里。
	 */
	private void init()
	{
		Log.e("info", "------------------------------------2=====");
		setFocusable(true);//设置焦点
		
		//高亮部分
		CurrentPaint = new Paint();//初始化当前画笔对象
		CurrentPaint.setAntiAlias(true);  //反对；别名，设置动画没有锯齿
		CurrentPaint.setTextAlign(Paint.Align.CENTER);
		
		//非高亮部分
		NotCurrentPaint = new Paint();
		NotCurrentPaint.setAntiAlias(true);
		NotCurrentPaint.setTextAlign(Paint.Align.CENTER);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);	
		//用画布对每句歌词进行处理
		if(canvas==null){
			return;
		}
		if (isKLOK) {
			CurrentPaint.setColor(Color.YELLOW);  //黄色
			NotCurrentPaint.setColor(Color.GREEN);//绿色
			
			CurrentPaint.setTextSize(KOKTextSize);
			CurrentPaint.setTypeface(Typeface.SERIF);//衬线字体
			NotCurrentPaint.setTextSize(KOKnoTextSize);
			NotCurrentPaint.setTypeface(Typeface.SERIF);	
			int nextIndex = Index + 1;// 下一句
			String text = mSentenceEntities.get(Index);// 获得歌词
			float len = this.getTextWidth(CurrentPaint, text);// 该句歌词精确长度
			float position = dunringTime == 0L ? 0L
					: ((float) currentTime - (float) startTime)
							/ (float) dunringTime;// 计算当前位置

			if (Index % 2 == 0) 
			{
				float start1 = len / 2;// 第一句的起点位置
				LinearGradient gradient = new LinearGradient(0, 0, len, 0,
						paintColorsCurrent, new float[] { position, position },
						TileMode.CLAMP);// 重绘渐变
				CurrentPaint.setShader(gradient);
				canvas.drawText(text, start1, tempYHigh, CurrentPaint);

				if (nextIndex < mSentenceEntities.size()) 
				{
					text = mSentenceEntities.get(nextIndex);// 下一句歌词
					len = this.getTextWidth(CurrentPaint, text);// 该句歌词精确长度
					float start2 = width - len / 2;// 第二句的起点位置
					gradient = new LinearGradient(start2, 0, width, 0,
							paintColorsDefault, null, TileMode.CLAMP);// 重绘渐变
					NotCurrentPaint.setShader(gradient);
					canvas.drawText(text, start2, tempYLow, NotCurrentPaint);
				}
			} 
			else 
			{
				float start2 = width - len / 2;// 第二句的起点位置
				float w = width > len ? width - len : 0;// 第二句的渐变起点位置
				LinearGradient gradient = new LinearGradient(w, 0, width, 0,
						paintColorsCurrent, new float[] { position, position },
						TileMode.CLAMP);// 重绘渐变
				NotCurrentPaint.setShader(gradient);
				canvas.drawText(text, start2, tempYLow, NotCurrentPaint);

				if (nextIndex < mSentenceEntities.size()) 
				{
					text = mSentenceEntities.get(nextIndex);// 下一句歌词
					len = this.getTextWidth(CurrentPaint, text);// 该句歌词精确长度
					float start1 = len / 2;// 第一句的起点位置
					gradient = new LinearGradient(0, 0, len, 0,
							paintColorsDefault, null, TileMode.CLAMP);// 重绘渐变
					CurrentPaint.setShader(gradient);
					canvas.drawText(text, start1, tempYHigh, CurrentPaint);
				}
			}
		}
		else
		{
			int temp = 0;
			CurrentPaint.setColor(Color.YELLOW);  //黄色
			NotCurrentPaint.setColor(Color.GREEN);//绿色
			
			CurrentPaint.setTextSize(TextSize);
			CurrentPaint.setTypeface(Typeface.SERIF);//衬线字体
			
			NotCurrentPaint.setTextSize(noTextSize);
			NotCurrentPaint.setTypeface(Typeface.DEFAULT);	
			try {
				/*if(mSentenceEntities.get(Index).equals(""))
				{
					for(temp = 0;temp < mSentenceEntities.size();temp++)
					{
						if(mSentenceEntities.get(temp).equals(""))
						{
							break;
						}
					}
					canvas.drawText(mSentenceEntities.get(temp - 1), width/2, high/2, CurrentPaint);
					float tempY = high / 2;
					// 画出本句之前的句子
					for (int i = temp - 1 - 1; i >= 0; i--) 
					{
						// 向上推移
						tempY = tempY - TextHigh;
						canvas.drawText(mSentenceEntities.get(i), width / 2, tempY, NotCurrentPaint);
					}
					
					tempY = high / 2;
					// 画出本句之后的句子
					for (int i = temp + 1; i < mSentenceEntities.size(); i++) 
					{
						// 往下推移
						tempY = tempY + TextHigh;
						canvas.drawText(mSentenceEntities.get(i), width / 2, tempY, NotCurrentPaint);
					}
				}
				else
				{*/
					canvas.drawText(mSentenceEntities.get(Index), width/2, high/2, CurrentPaint);
					
					float tempY = high / 2;
					// 画出本句之前的句子
					for (int i = Index - 1; i >= 0; i--) 
					{
						// 向上推移
						tempY = tempY - TextHigh;
						canvas.drawText(mSentenceEntities.get(i), width / 2, tempY, NotCurrentPaint);
					}
					
					tempY = high / 2;
					// 画出本句之后的句子
					for (int i = Index + 1; i < mSentenceEntities.size(); i++) 
					{
						// 往下推移
						tempY = tempY + TextHigh;
						canvas.drawText(mSentenceEntities.get(i), width / 2, tempY, NotCurrentPaint);
					}
				//}
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		// TODO Auto-generated method stub
		super.onSizeChanged(w, h, oldw, oldh);
		this.high=h;
		this.width=w;
		this.tempW = w / 2;
		this.tempH = h / 2;
		this.tempYHigh = tempH;
		this.tempYLow = tempH + textHeight;
	}
	/**
	 * 随这音乐的播放，修改index的值
	 * @param index
	 */
	public void SetIndex(int indexInfo,long currenttime,long starttime,long dunringtime){
		this.Index=indexInfo;
		this.currentTime = currenttime;
		this.startTime = starttime;
		this.dunringTime = dunringtime;
	}
	
	public void SetIndex(int index){
		this.Index=index;
	}

	/**
	 * 精确计算文字宽度
	 * 
	 * @param paint
	 * @param str
	 * @return
	 */
	private int getTextWidth(Paint paint, String str) {
		int iRet = 0;
		if (str != null && str.length() > 0) {
			int len = str.length();
			float[] widths = new float[len];
			paint.getTextWidths(str, widths);
			for (int j = 0; j < len; j++) {
				iRet += (int) Math.ceil(widths[j]);
			}
		}
		return iRet;
	}
	
	/**
	 * 是否属于卡拉OK模式
	 * 
	 * @param isKLOK
	 *            true:是
	 */
	public void setKLOK(boolean isKLOK) {
		this.isKLOK = isKLOK;
	}
}
