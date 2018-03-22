package com.activity.view;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.TextView;

//字幕移动效果
public class MarqueeTextView extends TextView{

    public MarqueeTextView(Context con) {
	  super(con);
	}

	public MarqueeTextView(Context context, AttributeSet attrs) {
	  super(context, attrs);
	}
	public MarqueeTextView(Context context, AttributeSet attrs, int defStyle) {
	  super(context, attrs, defStyle);
	}
	
	public boolean isFocused() {  //可聚焦
		return true;
	}
	
	protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {  
	}


}
