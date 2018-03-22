package com.activity.dialog;

import com.example.liveplayer.R;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class DeleteDialog extends Dialog implements android.view.View.OnClickListener{
	
	Context context;
	private NoticeDialogListener listener;
	//�Ի����¼������ӿڣ����ڴ���ص�����¼�
	public interface NoticeDialogListener 
	{
		public void onClick(View view);
	} 
	
	public DeleteDialog(Context context) 
	{
		super(context);
		// TODO Auto-generated constructor stub
		this.context = context;
	}
	
	public DeleteDialog(Context context,int theme)
	{
		super(context, theme);
		this.context = context;
	}
	
	public DeleteDialog(Context context,int theme,NoticeDialogListener listener)
	{
		super(context, theme);
		this.context = context;
		this.listener = listener;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		TextView enter = (TextView)findViewById(R.id.dialog_enter);
		TextView cancel = (TextView)findViewById(R.id.dialog_cancle);
		if(enter != null && cancel != null){//����ǲ���ȷ��ѡ��򣬲����¼���������
			enter.setOnClickListener(this);
			cancel.setOnClickListener(this);
		}
	}
	
	public void onClick(View view) {
		// TODO Auto-generated method stub
		listener.onClick(view);
	}
	
}
