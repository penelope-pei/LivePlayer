package com.activity.adapter;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;

public class LrcHandle {
	
	/*
	 * 
	 * 一般歌词文件的格式大概如下:
	 * [ar:艺人名]
	 * [ti:曲名]
	 * [al:专辑名]
	 * [by:编者(指编辑LRC歌词的人)]
	 * [offset:时间补偿值] 其单位是毫秒，正值表示整体提前，负值相反。这是用于总体调整显示快慢的。
	 * 但也不一定，有时候并没有前面那些ar:等标识符，所以我们这里也提供了另一种解析方式。
	 * 歌词文件中的时间格式则比较统一:[00:00.50]等等，00:表示分钟，00.表示秒数，.50表示毫秒数，
	 * 当然，我们最后是要将它们转化为毫秒数处理才比较方便。
	 * 
	 * 
	 * */
	
	//歌词内容列表
	private List<String> mWords = new ArrayList<String>();
	//歌词每一句时间列表
	private List<Long> mTimeList = new ArrayList<Long>();
	
	//每句歌词
	private String str_word = "";
	//每句歌词对应的时间
	private long current_time = 0;
	
	//处理歌词文件，歌词的获取是通过读取文件的方式获取，所以需要传入歌词的路径
	public void readLRC(String path) {
		try {
			File file = new File(path);
			FileInputStream fileInputStream = new FileInputStream(file);  //stream：流
			//打开文件的方式，默认"UTF-8"
			InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, GetCharset(file));  //utf-8
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
			//一行一行读取，[00:00.532]回忆那么伤 - 孙子涵
			String s = "";
			while ((s = bufferedReader.readLine()) != null) 
			{
				//分为以下五种情况
				//第一种：分离出作曲、作词、作曲等信息，这种情况下，对应的歌词时间可以先设为0
				if(s.contains("[ar:") || s.contains("[ti:") || s.contains("[by:")
						|| s.contains("[al:") || s.contains("[ly:") || s.contains("[mu:")
						|| s.contains("[ma:") || s.contains("[pu:") || s.contains("[by:") 
						|| s.contains("[total:") || s.contains("[offset:"))
				{
					//先把"["替换成""，再把"]"替换成":"，最后再以":"为分割标志，分割出作词、作曲等信息，并保存到mWords列表里
					s = s.replace("[", "");
					s = s.replace("]", ":");
					String[] wordData = s.split(":");
					//出现这种情况下：[ma:]，mWords列表对应位置保存的内容为""
					//这样就可以避免有时候看不出哪一句是当前显示的情况，显示时判断上一句是否为""即可
					if(wordData.length > 1)
					{
						str_word = wordData[1];
						mWords.add(str_word);
					}
					else  //[ma:]
					{
						mWords.add("");  //str_word
					}
					//分离出时间
					addTimeToTimeList();
				}
				//第二种：有时间，但是没有歌词（判断方式：句子中包含有"["并且第二个字符是数字，并且包含有"]",且最后一个"]"后面没有字符串），
				//判断没有歌词的方法：直接判断字符串最有一个字符是否为"]"即可
				//这种情况下，时间点对应的歌词还是显示上一次的歌词内容
				else if(s.contains("[") && Character.isDigit(s.charAt(1)) && s.charAt(s.length() - 1) == ']')
				{
					//先把所有的"["替换成""，然后再以"]"为分割标志
					s = s.replace("[", "");
					String s0 = s.replace("]", "=");
					String[] timeData = s0.split("=");
					//String[] timeData = s.split("]");
					for(String timestr : timeData)
					{
						//分割出每个[ ]中的时间
						addTimeToList(timestr);
						//歌词还是显示为""
						mWords.add("");//str_word
					}
				}
				//第三种：有时间，也有歌词（判断方式：句子中包含有"["并且第二个字符是数字，并且包含有"]",
				//且最后一个"]"后面还有字符串，也即歌词），
				//判断有没有歌词的方法：直接判断字符串最有一个字符是否为"]"即可
				else if(s.contains("[") && Character.isDigit(s.charAt(1)) && s.charAt(s.length() - 1) != ']')
				{
					//此时s内容还是没有变化，把"["替换成""
					s = s.replace("[", "");
					String s0 = s.replace("]", "=");
					String[] str = s0.split("=");
					//String[] str = s.split("]");
					for(int i = 0;i<str.length-1;i++)
					{
						//分割出每个[ ]中的时间
						addTimeToList(str[i]);
						//数组的最后一项一定是歌词
						mWords.add(str[str.length - 1]);
					}
				}
				//第四种：没有时间，但是有歌词，判断有歌词方法：直接判断内容是否为""
				//这种情况下，对应位置的时间可以设置成上一次时间+1
				else if(!s.contains("[") && !s.equals(""))
				{
					//设置歌词
					mWords.add(s);
					//设置时间
					addOneTimeToTimeList();
				}
				//没有第五种情况了
				//第五种：既没有时间，也没有歌词
				//这种情况下，对应位置的时间可以设置成上一次时间+1，歌词设置为""
				/*else
				{
					//设置歌词
					mWords.add("");
					//设置时间
					addOneTimeToTimeList();
				}*/
			}
			bufferedReader.close();
			inputStreamReader.close();
			fileInputStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			mWords.add("没有歌词文件，赶紧去下载");
		} catch (IOException e) {
			e.printStackTrace();
			mWords.add("没有读取到歌词");
		}
	}
	
	private void addTimeToTimeList()
	{
		//暂时先设置对应位置的时间值为0
		current_time = 0L;
		mTimeList.add(current_time);
	}
	
	private void addOneTimeToTimeList()
	{
		//暂时先设置对应位置的时间值为上一次时间+10
		mTimeList.add((current_time+10));
	}
	
	// 分离出时间
	private void addTimeToList(String string) 
	{
		// 分离出分、秒并转换为整型
		int millisecond = 0;
		int minute = 0;
		int second = 0;
		//00:00.532
		if(string.contains("."))
		{
			string = string.replace(":", ".");
			string = string.replace(".", "@");
			String time[] = string.split("@");
			minute = Integer.parseInt(time[0]);
			second = Integer.parseInt(time[1]);
			millisecond = Integer.parseInt(time[2]);
		}
		//00:00
		else
		{
			string = string.replace(":", "@");
			String time[] = string.split("@");
			minute = Integer.parseInt(time[0]);
			second = Integer.parseInt(time[1]);
			millisecond = 0;
		}
		current_time = (minute*60+second) * 1000 + millisecond;
		mTimeList.add(current_time);
	}
	
	public List<String> getWords() {
		return mWords;
	}
	
	public List<Long> getTime() {
		return mTimeList;
	}
	
	//判断是否为数字，避免出现字母等其他情况
	public static boolean isNumeric(String str){
		if(str == null)
		{
			return false;
		}
		else
		{
			for (int i = 0; i < str.length();i++){   
				if (!Character.isDigit(str.charAt(i))){
					return false;
				}
			}
		}
		return true;
	}
	
	//判断打开文件的方式，默认"GBK"
    public String GetCharset(File file)
    {  
    	String charset = "GBK";  
    	byte[] first3Bytes = new byte[3];  
    	try  
    	{  
    		boolean checked = false;  
    		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));  
    		bis.mark(0);  
    		int read = bis.read(first3Bytes, 0, 3);  
    		if (read == -1)  
    			return charset;  
    		if (first3Bytes[0] == (byte) 0xFF && first3Bytes[1] == (byte) 0xFE)  
    		{  
    			charset = "UTF-16LE";  
    			checked = true;  
    		}  
    		else if (first3Bytes[0] == (byte) 0xFE && first3Bytes[1] == (byte) 0xFF)  
    		{  
    			charset = "UTF-16BE";  
    			checked = true;  
    		}  
    		else if (first3Bytes[0] == (byte) 0xEF && first3Bytes[1] == (byte) 0xBB && first3Bytes[2] == (byte) 0xBF)  
    		{  
    			charset = "UTF-8";  
    			checked = true;  
    		}  
    		bis.reset();  
    		if (!checked)  
    		{  
    			int loc = 0;  
    			while ((read = bis.read()) != -1)  
    			{  
    				loc++;  
    				if (read >= 0xF0)  
    					break;  
    				if (0x80 <= read && read <= 0xBF) //   
    					break;  
    				if (0xC0 <= read && read <= 0xDF)  
    				{  
    					read = bis.read();  
    					if (0x80 <= read && read <= 0xBF) //   
    						continue;  
    					else  
    						break;  
    				}  
    				else if (0xE0 <= read && read <= 0xEF)  
    				{// 
    					read = bis.read();  
    					if (0x80 <= read && read <= 0xBF)  
    					{  
    						read = bis.read();  
    						if (0x80 <= read && read <= 0xBF)  
    						{  
    							charset = "UTF-8";  
    							break;  
    						}  
    						else  
    							break;  
    					}  
    					else  
    						break;  
    				}  
    			}  
    		}  
    		bis.close();  
    	}  
    	catch (Exception e)  
    	{  
    		e.printStackTrace();  
    	}  
    	return charset;  
    }
}
