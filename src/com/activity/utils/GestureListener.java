package com.activity.utils;

import android.content.Context;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

//���Ƽ���
public class GestureListener extends SimpleOnGestureListener implements OnTouchListener{
	/** ���һ�������̾��� */  
    private int distance = 500;   //��Ҫ��ô�����������û����� 
    /** ���һ���������ٶ� */  
    private int velocity = 200;  
      
    private GestureDetector gestureDetector;  
      
    public GestureListener(Context context) {  
        super();  
        gestureDetector = new GestureDetector(context, this);  
    }  
  
    /** 
     * ���󻬵�ʱ����õķ�����������д 
     * @return 
     */  
    public boolean left() {  
        return false;  
    }  
      
    /** 
     * ���һ���ʱ����õķ�����������д 
     * @return 
     */  
    public boolean right() {  
        return false;  
    }  
      
    @Override  
    public boolean onFling(MotionEvent motionEvent1, MotionEvent motionEvent2, float velocityX,  
            float velocityY) {  
        // TODO Auto-generated method stub  
        // motionEvent1����1��ACTION_DOWN MotionEvent  
        // motionEvent2�����һ��ACTION_MOVE MotionEvent  
        // velocityX��X���ϵ��ƶ��ٶȣ�����/�룩  
        // velocityY��Y���ϵ��ƶ��ٶȣ�����/�룩  
    	Log.e("info", "------------------------------------2=====");
        // ����  
        if (motionEvent1.getX() - motionEvent2.getX() > distance  
                && Math.abs(velocityX) > velocity) {  
            left();  
        }  
        // ���һ�  
        if (motionEvent2.getX() - motionEvent1.getX() > distance  
                && Math.abs(velocityX) > velocity) {  
            right();  
        }  
        return false;  
    }  
  
    @Override  
    public boolean onTouch(View view, MotionEvent event) {  
        // TODO Auto-generated method stub  
        gestureDetector.onTouchEvent(event);
        Log.e("info", "------------------------------------3=====");
        return false;  
    }  
  
    public int getDistance() {  
        return distance;  
    }  
  
    public void setDistance(int distance) {  
        this.distance = distance;  
    }  
  
    public int getVelocity() {  
        return velocity;  
    }  
  
    public void setVelocity(int velocity) {  
        this.velocity = velocity;  
    }  
  
    public GestureDetector getGestureDetector() {  
        return gestureDetector;  
    }  
  
    public void setGestureDetector(GestureDetector gestureDetector) {  
        this.gestureDetector = gestureDetector;  
    }
}
