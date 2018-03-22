package com.activity.view;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.TextView;

//��Ļ�ƶ�Ч��
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
	
	public boolean isFocused() {  //�ɾ۽�
		return true;
	}
	
	protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {  
	}


}
