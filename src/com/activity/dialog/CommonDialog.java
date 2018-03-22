package com.activity.dialog;

import com.activity.service.PlayService;
import com.example.liveplayer.R;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class CommonDialog extends Dialog{
	
	private int type = 0;
	private Context context;
	private String title;
	private ClickListenerInterface clickListenerInterface;
	private UserClickListenerInterface userclickListenerInterface;
	
	private TextView tv_dialog_title;
	private TextView tv_del_list;
	private TextView tv_del_local;
	private TextView tv_background_setting;
	private TextView tv_dialog_cancel;
	private TextView tv_dialog_img;
	private TextView tv_dialog_tip;
	private RelativeLayout rl_del;
	private LinearLayout ll_del;
	private LinearLayout ll_user;
	
	public interface ClickListenerInterface {
		public void dodeletelocalmusic();
		public void dodeletemusic();
		public void docancel();
	}
	
	public interface UserClickListenerInterface {
		public void dobackgroundsettting();
		public void docancel();
	}
	
	public CommonDialog(Context context, String title, int type) {
		super(context, R.style.NoticeDialog);
		this.context = context;
		this.title = title;
		this.type = type;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		init();
	}
	
	public void init() {
		LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(R.layout.common_dialog, null);
		setContentView(view);
		
		tv_dialog_title = (TextView) view.findViewById(R.id.dialog_tip);
		tv_del_local = (TextView) view.findViewById(R.id.del_local);
		tv_del_list = (TextView) view.findViewById(R.id.del_list);
		tv_background_setting = (TextView) view.findViewById(R.id.tv_background_setting);
		tv_dialog_cancel = (TextView) view.findViewById(R.id.dialog_cancel);
		tv_dialog_img = (TextView)view.findViewById(R.id.dialog_delete_img);
		tv_dialog_tip = (TextView)view.findViewById(R.id.dialog_tips);
		rl_del = (RelativeLayout)view.findViewById(R.id.dialog_title_rl);
		ll_del = (LinearLayout)view.findViewById(R.id.music_del_ll);
		ll_user = (LinearLayout)view.findViewById(R.id.music_play_more_ll);
		
		tv_dialog_title.setText(title);
		
		tv_dialog_cancel.setOnClickListener(new clickListener());
		tv_del_local.setOnClickListener(new clickListener());
		tv_del_list.setOnClickListener(new clickListener());
		tv_background_setting.setOnClickListener(new clickListener());
		
		if(type == 0)
		{
			ll_del.setVisibility(View.VISIBLE);
			ll_user.setVisibility(View.GONE);
			rl_del.setVisibility(View.VISIBLE);
			tv_dialog_tip.setVisibility(View.GONE);
			if(PlayService.show_list_type == 0)
			{
				tv_del_list.setText(R.string.del_list);
				tv_del_local.setText(R.string.del_local);
			}
			else
			{
				tv_del_list.setText(R.string.del_one);
				tv_del_local.setText(R.string.del_all);
			}
		}
		else if(type == 1)
		{
			ll_del.setVisibility(View.GONE);
			ll_user.setVisibility(View.VISIBLE);
			rl_del.setVisibility(View.GONE);
			tv_dialog_tip.setVisibility(View.GONE);
		}
		
		Window dialogWindow = getWindow();
		WindowManager.LayoutParams lp = dialogWindow.getAttributes();
		DisplayMetrics d = context.getResources().getDisplayMetrics(); // 获取屏幕宽、高用
		lp.width = (int) (d.widthPixels * 0.65); // 宽度设置为屏幕的0.65
		lp.height = (int) (d.heightPixels * 0.35); // 高度设置为屏幕的0.45
		dialogWindow.setAttributes(lp);
	}
	
	public void setClicklistener(ClickListenerInterface clickListenerInterface) {
		this.clickListenerInterface = clickListenerInterface;
	}
	
	public void setuserClicklistener(UserClickListenerInterface userclickListenerInterface) {
		this.userclickListenerInterface = userclickListenerInterface;
	}
	
	private class clickListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			int id = v.getId();
			switch (id) {
			case R.id.dialog_cancel:
				if(type == 0)
				{
					clickListenerInterface.docancel();
				}
				else if(type == 1)
				{
					userclickListenerInterface.docancel();
				}
				break;
			case R.id.del_local:
				clickListenerInterface.dodeletelocalmusic();
				break;
				
			case R.id.del_list:
				clickListenerInterface.dodeletemusic();
				break;
				
			case R.id.tv_background_setting:
				userclickListenerInterface.dobackgroundsettting();
				break;
				
			}
		}
		
	};
}
