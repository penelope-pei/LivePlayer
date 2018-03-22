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
	 * һ�����ļ��ĸ�ʽ�������:
	 * [ar:������]
	 * [ti:����]
	 * [al:ר����]
	 * [by:����(ָ�༭LRC��ʵ���)]
	 * [offset:ʱ�䲹��ֵ] �䵥λ�Ǻ��룬��ֵ��ʾ������ǰ����ֵ�෴�������������������ʾ�����ġ�
	 * ��Ҳ��һ������ʱ��û��ǰ����Щar:�ȱ�ʶ����������������Ҳ�ṩ����һ�ֽ�����ʽ��
	 * ����ļ��е�ʱ���ʽ��Ƚ�ͳһ:[00:00.50]�ȵȣ�00:��ʾ���ӣ�00.��ʾ������.50��ʾ��������
	 * ��Ȼ�����������Ҫ������ת��Ϊ����������űȽϷ��㡣
	 * 
	 * 
	 * */
	
	//��������б�
	private List<String> mWords = new ArrayList<String>();
	//���ÿһ��ʱ���б�
	private List<Long> mTimeList = new ArrayList<Long>();
	
	//ÿ����
	private String str_word = "";
	//ÿ���ʶ�Ӧ��ʱ��
	private long current_time = 0;
	
	//�������ļ�����ʵĻ�ȡ��ͨ����ȡ�ļ��ķ�ʽ��ȡ��������Ҫ�����ʵ�·��
	public void readLRC(String path) {
		try {
			File file = new File(path);
			FileInputStream fileInputStream = new FileInputStream(file);  //stream����
			//���ļ��ķ�ʽ��Ĭ��"UTF-8"
			InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, GetCharset(file));  //utf-8
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
			//һ��һ�ж�ȡ��[00:00.532]������ô�� - ���Ӻ�
			String s = "";
			while ((s = bufferedReader.readLine()) != null) 
			{
				//��Ϊ�����������
				//��һ�֣���������������ʡ���������Ϣ����������£���Ӧ�ĸ��ʱ���������Ϊ0
				if(s.contains("[ar:") || s.contains("[ti:") || s.contains("[by:")
						|| s.contains("[al:") || s.contains("[ly:") || s.contains("[mu:")
						|| s.contains("[ma:") || s.contains("[pu:") || s.contains("[by:") 
						|| s.contains("[total:") || s.contains("[offset:"))
				{
					//�Ȱ�"["�滻��""���ٰ�"]"�滻��":"���������":"Ϊ�ָ��־���ָ�����ʡ���������Ϣ�������浽mWords�б���
					s = s.replace("[", "");
					s = s.replace("]", ":");
					String[] wordData = s.split(":");
					//������������£�[ma:]��mWords�б��Ӧλ�ñ��������Ϊ""
					//�����Ϳ��Ա�����ʱ�򿴲�����һ���ǵ�ǰ��ʾ���������ʾʱ�ж���һ���Ƿ�Ϊ""����
					if(wordData.length > 1)
					{
						str_word = wordData[1];
						mWords.add(str_word);
					}
					else  //[ma:]
					{
						mWords.add("");  //str_word
					}
					//�����ʱ��
					addTimeToTimeList();
				}
				//�ڶ��֣���ʱ�䣬����û�и�ʣ��жϷ�ʽ�������а�����"["���ҵڶ����ַ������֣����Ұ�����"]",�����һ��"]"����û���ַ�������
				//�ж�û�и�ʵķ�����ֱ���ж��ַ�������һ���ַ��Ƿ�Ϊ"]"����
				//��������£�ʱ����Ӧ�ĸ�ʻ�����ʾ��һ�εĸ������
				else if(s.contains("[") && Character.isDigit(s.charAt(1)) && s.charAt(s.length() - 1) == ']')
				{
					//�Ȱ����е�"["�滻��""��Ȼ������"]"Ϊ�ָ��־
					s = s.replace("[", "");
					String s0 = s.replace("]", "=");
					String[] timeData = s0.split("=");
					//String[] timeData = s.split("]");
					for(String timestr : timeData)
					{
						//�ָ��ÿ��[ ]�е�ʱ��
						addTimeToList(timestr);
						//��ʻ�����ʾΪ""
						mWords.add("");//str_word
					}
				}
				//�����֣���ʱ�䣬Ҳ�и�ʣ��жϷ�ʽ�������а�����"["���ҵڶ����ַ������֣����Ұ�����"]",
				//�����һ��"]"���滹���ַ�����Ҳ����ʣ���
				//�ж���û�и�ʵķ�����ֱ���ж��ַ�������һ���ַ��Ƿ�Ϊ"]"����
				else if(s.contains("[") && Character.isDigit(s.charAt(1)) && s.charAt(s.length() - 1) != ']')
				{
					//��ʱs���ݻ���û�б仯����"["�滻��""
					s = s.replace("[", "");
					String s0 = s.replace("]", "=");
					String[] str = s0.split("=");
					//String[] str = s.split("]");
					for(int i = 0;i<str.length-1;i++)
					{
						//�ָ��ÿ��[ ]�е�ʱ��
						addTimeToList(str[i]);
						//��������һ��һ���Ǹ��
						mWords.add(str[str.length - 1]);
					}
				}
				//�����֣�û��ʱ�䣬�����и�ʣ��ж��и�ʷ�����ֱ���ж������Ƿ�Ϊ""
				//��������£���Ӧλ�õ�ʱ��������ó���һ��ʱ��+1
				else if(!s.contains("[") && !s.equals(""))
				{
					//���ø��
					mWords.add(s);
					//����ʱ��
					addOneTimeToTimeList();
				}
				//û�е����������
				//�����֣���û��ʱ�䣬Ҳû�и��
				//��������£���Ӧλ�õ�ʱ��������ó���һ��ʱ��+1���������Ϊ""
				/*else
				{
					//���ø��
					mWords.add("");
					//����ʱ��
					addOneTimeToTimeList();
				}*/
			}
			bufferedReader.close();
			inputStreamReader.close();
			fileInputStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			mWords.add("û�и���ļ����Ͻ�ȥ����");
		} catch (IOException e) {
			e.printStackTrace();
			mWords.add("û�ж�ȡ�����");
		}
	}
	
	private void addTimeToTimeList()
	{
		//��ʱ�����ö�Ӧλ�õ�ʱ��ֵΪ0
		current_time = 0L;
		mTimeList.add(current_time);
	}
	
	private void addOneTimeToTimeList()
	{
		//��ʱ�����ö�Ӧλ�õ�ʱ��ֵΪ��һ��ʱ��+10
		mTimeList.add((current_time+10));
	}
	
	// �����ʱ��
	private void addTimeToList(String string) 
	{
		// ������֡��벢ת��Ϊ����
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
	
	//�ж��Ƿ�Ϊ���֣����������ĸ���������
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
	
	//�жϴ��ļ��ķ�ʽ��Ĭ��"GBK"
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
