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
	
	private float high;  	//�����ͼ�߶�
	private float width;	//�����ͼ���
	private Paint CurrentPaint;	    //�滭���������֣����ڲ��ŵ�������ɫ�仯
	private Paint NotCurrentPaint;	//�ǵ�ǰ���ʶ���
	private float TextHigh=70;	//�ı��߶�
	private float TextSize=60;  //���ڲ��ŵ��ı������С
	private float noTextSize = 50;	//��δ���ŵ������С
	private float KOKTextSize=20;  //���ڲ��ŵ��ı������С
	private float KOKnoTextSize = 18;	//��δ���ŵ������С
	private int Index=0;     //list�����±�
	private long currentTime = 0;// ��ǰ�����Ĳ���λ��
	private long dunringTime = 0;// ��ǰ���ʵĳ���ʱ��
	private long startTime = 0;// ��ǰ���ʿ�ʼ��ʱ��
	private float tempW = 0;// ���㻭�����м�λ��(��)
	private float tempH = 0;// ���㻭�����м�λ��(��)
	private float tempYHigh = 0;// ���㿨��OKģʽ��һ���Y��λ��
	private float tempYLow = 0;// ���㿨��OKģʽ�ڶ����Y��λ��
	private float textHeight = 40;// �����ָ߶�
	private int[] paintColorsCurrent = { Color.argb(250, 251, 248, 29),
			Color.argb(250, 255, 255, 255) };// ����OKģʽ�滭�л�����ɫ����
	private int[] paintColorsDefault = { Color.argb(250, 255, 255, 255),
			Color.argb(250, 255, 255, 255) };// ����OKģʽĬ�ϻ�����ɫ����
	private boolean isKLOK = false;
	
	private List<String> mSentenceEntities=new ArrayList<String>();  
	
	//���ø����Դ�� ����ʵ���б����
	public void setSentenceEntities(List<String> mSentenceEntities)
	{
		this.mSentenceEntities = mSentenceEntities;
	}
	/**
	 * ���췽��������û�м̳�Activity��������Context���Զ������д���
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
	 * ��ʼ�����ʣ����ڹ��췽���
	 */
	private void init()
	{
		Log.e("info", "------------------------------------2=====");
		setFocusable(true);//���ý���
		
		//��������
		CurrentPaint = new Paint();//��ʼ����ǰ���ʶ���
		CurrentPaint.setAntiAlias(true);  //���ԣ����������ö���û�о��
		CurrentPaint.setTextAlign(Paint.Align.CENTER);
		
		//�Ǹ�������
		NotCurrentPaint = new Paint();
		NotCurrentPaint.setAntiAlias(true);
		NotCurrentPaint.setTextAlign(Paint.Align.CENTER);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);	
		//�û�����ÿ���ʽ��д���
		if(canvas==null){
			return;
		}
		if (isKLOK) {
			CurrentPaint.setColor(Color.YELLOW);  //��ɫ
			NotCurrentPaint.setColor(Color.GREEN);//��ɫ
			
			CurrentPaint.setTextSize(KOKTextSize);
			CurrentPaint.setTypeface(Typeface.SERIF);//��������
			NotCurrentPaint.setTextSize(KOKnoTextSize);
			NotCurrentPaint.setTypeface(Typeface.SERIF);	
			int nextIndex = Index + 1;// ��һ��
			String text = mSentenceEntities.get(Index);// ��ø��
			float len = this.getTextWidth(CurrentPaint, text);// �þ��ʾ�ȷ����
			float position = dunringTime == 0L ? 0L
					: ((float) currentTime - (float) startTime)
							/ (float) dunringTime;// ���㵱ǰλ��

			if (Index % 2 == 0) 
			{
				float start1 = len / 2;// ��һ������λ��
				LinearGradient gradient = new LinearGradient(0, 0, len, 0,
						paintColorsCurrent, new float[] { position, position },
						TileMode.CLAMP);// �ػ潥��
				CurrentPaint.setShader(gradient);
				canvas.drawText(text, start1, tempYHigh, CurrentPaint);

				if (nextIndex < mSentenceEntities.size()) 
				{
					text = mSentenceEntities.get(nextIndex);// ��һ����
					len = this.getTextWidth(CurrentPaint, text);// �þ��ʾ�ȷ����
					float start2 = width - len / 2;// �ڶ�������λ��
					gradient = new LinearGradient(start2, 0, width, 0,
							paintColorsDefault, null, TileMode.CLAMP);// �ػ潥��
					NotCurrentPaint.setShader(gradient);
					canvas.drawText(text, start2, tempYLow, NotCurrentPaint);
				}
			} 
			else 
			{
				float start2 = width - len / 2;// �ڶ�������λ��
				float w = width > len ? width - len : 0;// �ڶ���Ľ������λ��
				LinearGradient gradient = new LinearGradient(w, 0, width, 0,
						paintColorsCurrent, new float[] { position, position },
						TileMode.CLAMP);// �ػ潥��
				NotCurrentPaint.setShader(gradient);
				canvas.drawText(text, start2, tempYLow, NotCurrentPaint);

				if (nextIndex < mSentenceEntities.size()) 
				{
					text = mSentenceEntities.get(nextIndex);// ��һ����
					len = this.getTextWidth(CurrentPaint, text);// �þ��ʾ�ȷ����
					float start1 = len / 2;// ��һ������λ��
					gradient = new LinearGradient(0, 0, len, 0,
							paintColorsDefault, null, TileMode.CLAMP);// �ػ潥��
					CurrentPaint.setShader(gradient);
					canvas.drawText(text, start1, tempYHigh, CurrentPaint);
				}
			}
		}
		else
		{
			int temp = 0;
			CurrentPaint.setColor(Color.YELLOW);  //��ɫ
			NotCurrentPaint.setColor(Color.GREEN);//��ɫ
			
			CurrentPaint.setTextSize(TextSize);
			CurrentPaint.setTypeface(Typeface.SERIF);//��������
			
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
					// ��������֮ǰ�ľ���
					for (int i = temp - 1 - 1; i >= 0; i--) 
					{
						// ��������
						tempY = tempY - TextHigh;
						canvas.drawText(mSentenceEntities.get(i), width / 2, tempY, NotCurrentPaint);
					}
					
					tempY = high / 2;
					// ��������֮��ľ���
					for (int i = temp + 1; i < mSentenceEntities.size(); i++) 
					{
						// ��������
						tempY = tempY + TextHigh;
						canvas.drawText(mSentenceEntities.get(i), width / 2, tempY, NotCurrentPaint);
					}
				}
				else
				{*/
					canvas.drawText(mSentenceEntities.get(Index), width/2, high/2, CurrentPaint);
					
					float tempY = high / 2;
					// ��������֮ǰ�ľ���
					for (int i = Index - 1; i >= 0; i--) 
					{
						// ��������
						tempY = tempY - TextHigh;
						canvas.drawText(mSentenceEntities.get(i), width / 2, tempY, NotCurrentPaint);
					}
					
					tempY = high / 2;
					// ��������֮��ľ���
					for (int i = Index + 1; i < mSentenceEntities.size(); i++) 
					{
						// ��������
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
	 * �������ֵĲ��ţ��޸�index��ֵ
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
	 * ��ȷ�������ֿ��
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
	 * �Ƿ����ڿ���OKģʽ
	 * 
	 * @param isKLOK
	 *            true:��
	 */
	public void setKLOK(boolean isKLOK) {
		this.isKLOK = isKLOK;
	}
}
