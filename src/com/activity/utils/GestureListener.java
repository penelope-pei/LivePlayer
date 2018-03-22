package com.activity.utils;

import android.content.Context;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

//手势监听
public class GestureListener extends SimpleOnGestureListener implements OnTouchListener{
	/** 左右滑动的最短距离 */  
    private int distance = 500;   //不要那么灵敏，增加用户体验 
    /** 左右滑动的最大速度 */  
    private int velocity = 200;  
      
    private GestureDetector gestureDetector;  
      
    public GestureListener(Context context) {  
        super();  
        gestureDetector = new GestureDetector(context, this);  
    }  
  
    /** 
     * 向左滑的时候调用的方法，子类重写 
     * @return 
     */  
    public boolean left() {  
        return false;  
    }  
      
    /** 
     * 向右滑的时候调用的方法，子类重写 
     * @return 
     */  
    public boolean right() {  
        return false;  
    }  
      
    @Override  
    public boolean onFling(MotionEvent motionEvent1, MotionEvent motionEvent2, float velocityX,  
            float velocityY) {  
        // TODO Auto-generated method stub  
        // motionEvent1：第1个ACTION_DOWN MotionEvent  
        // motionEvent2：最后一个ACTION_MOVE MotionEvent  
        // velocityX：X轴上的移动速度（像素/秒）  
        // velocityY：Y轴上的移动速度（像素/秒）  
    	Log.e("info", "------------------------------------2=====");
        // 向左滑  
        if (motionEvent1.getX() - motionEvent2.getX() > distance  
                && Math.abs(velocityX) > velocity) {  
            left();  
        }  
        // 向右滑  
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
