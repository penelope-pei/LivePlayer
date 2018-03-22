package com.activity.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.content.Context;

public class VideoUtils {
	private static final String TAG = "Utils";

	@SuppressLint("SimpleDateFormat")
	public static String getNowDateTime() {
		String format = "yyyyMMddhhmmss";
		SimpleDateFormat s_format = new SimpleDateFormat(format);
		Date d_date = new Date();
		String s_date = "";
		s_date = s_format.format(d_date);
		return s_date;
	}

	@SuppressLint("SimpleDateFormat")
	public static String formatTime(long time) {
		DateFormat formatter = new SimpleDateFormat("mm:ss");
		return formatter.format(new Date(time));
	}

	public static int getHeightInPx(Context context) {
		int height = context.getResources().getDisplayMetrics().heightPixels;
		return height;
	}

	public static int getWidthInPx(Context context) {
		int width = context.getResources().getDisplayMetrics().widthPixels;
		return width;
	}

	public static int getHeightInDp(Context context) {
		int height = context.getResources().getDisplayMetrics().heightPixels;
		int heightInDp = px2dip(context, height);
		return heightInDp;
	}

	public static int getWidthInDp(Context context) {
		int height = context.getResources().getDisplayMetrics().heightPixels;
		int widthInDp = px2dip(context, height);
		return widthInDp;
	}

	public static int dip2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

	public static int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	public static int px2sp(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	public static int sp2px(Context context, float spValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (spValue * scale + 0.5f);
	}

}
