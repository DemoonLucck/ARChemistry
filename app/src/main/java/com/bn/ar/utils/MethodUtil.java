package com.bn.ar.utils;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import com.bn.thread.URLConnectionThread;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class MethodUtil {
	
	public static boolean isExistFile(String fileName,String path)
	{

		File file1=new File(path);
		File file2 = new File(path+fileName);
		if(!file1.exists()||!file2.exists())
		{
			return false;
		}
		return true;
	}
	
	//从SD卡获取文件信息
	public  static String loadFromFile(String fileName)
	{
		File file1=new File(Constant.UNZIP_TO_PATH);
		File file=new File(Constant.UNZIP_TO_PATH+fileName);
		if(!file1.exists()||!file.exists())
		{
			return "NOTSTRING";
		}
		String result=null;
		try
		{			
			int length=(int)file.length();
			byte[] buff=new byte[length];
			FileInputStream fin=new FileInputStream(file);
			fin.read(buff);
			fin.close();
			result=new String(buff,"UTF-8"); 
			result=result.replaceAll("\\r\\n","\n");
		}
		catch(Exception e)
		{
			System.out.println("对不起，没有找到指定文件！"+fileName);
		}
		return result;
	}
	
	//从SD卡获取zip解压后的指定名称的bitmap
	public static Bitmap  getBitmapFromSDCard(String fileName,String path)
	{
		File file = new File(path);
		File f=new File(path+fileName);
		if(!file.exists()||!f.exists())//如果path不存在或者path下没有指定名称的文件则返回
		{
			return  null;
		}
		Bitmap bp=null;
		try
		{			
			int length=(int)f.length();//获得目的文件的byte长度
			byte[] buff=new byte[length];
			FileInputStream fin=new FileInputStream(f);//
			fin.read(buff);
			fin.close();
			bp = BitmapFactory.decodeByteArray(buff, 0, buff.length);//进行编码,得到所需要的bitmap
			
		}
		catch(Exception e)
		{
			System.out.println("对不起，没有找到指定图片！"+fileName);
			e.printStackTrace();
		}
		return bp;
	}
	//从网页获取指定路径的信息字符串
	public static String getStringInfoFromURL(String subPath)
	 {
		 String result=null;		 
		 try
		 {
			 URL url=new URL(subPath);
			 URLConnection uc=url.openConnection();
			 InputStream in=uc.getInputStream();
			 int ch=0;
			 ByteArrayOutputStream baos = new ByteArrayOutputStream();
			 while((ch=in.read())!=-1)
			 {
				 baos.write(ch);
			 }      
			 byte[] bb=baos.toByteArray();
			 baos.close();
			 in.close();
			 result=new String(bb,"UTF-8");
			 result=result.replaceAll("\\r\\n","\n");
			 Constant.net_success = true;
			 URLConnectionThread.flag = true;
		 }
		 catch(Exception e)
		 {
			 Constant.net_success = false;
			 URLConnectionThread.flag = false;
			 System.out.println("txt  从服务器读取txt失败！"+Constant.net_success );
			 e.printStackTrace();
		 }
		 System.out.println("返回结果:"+result);

		 return result;
	 }
	
	//从网页获取指定路径的Bitmap
	public static  Bitmap getBitmapFromURL(String path) 
	{
		Bitmap bitmap=null;
		try {
			URL url = new URL(path);
			HttpURLConnection con=(HttpURLConnection) url.openConnection();//建立http连接
			con.setDoInput(true);
			con.connect();
			InputStream inputStream=con.getInputStream();
			
			bitmap=BitmapFactory.decodeStream(inputStream); 
			inputStream.close();
			Constant.net_success = true;
			URLConnectionThread.flag = true;
		}
		catch (MalformedURLException e) {
			Constant.net_success = false;
			URLConnectionThread.flag = false;
			System.out.println("bitmap  从服务器读取bitmap失败！"+Constant.net_success );
			e.printStackTrace();
		}catch (IOException e) {
			Constant.net_success = false;
			URLConnectionThread.flag = false;
			System.out.println("bitmap  从服务器读取bitmap失败！"+Constant.net_success );
			e.printStackTrace();
		} 
		return bitmap;
	}
	
	//=================下面的两个方法仅供测试使用=======================
	//从SD卡的指定文件下获取所有的图片信息
	public static  Bitmap[] getBitmapsFromSDCard(String picPath)
	{
		ArrayList<File> files = MethodUtil.getListFiles(picPath);
		Bitmap bp[]=new Bitmap[files.size()];		
		for(int i=0;i<files.size();i++)
		{
			try
			{
				int length=(int)files.get(i).length();
				byte[] buff=new byte[length];
				FileInputStream fin=new FileInputStream(files.get(i));
				fin.read(buff);
				fin.close();
				bp[i] = BitmapFactory.decodeByteArray(buff, 0, buff.length);
			}catch(Exception e)
			{
				System.out.println("类别名称的纹理获取失败！！");
				e.printStackTrace();
			}
		}
		return bp;
	}
	/*** 
     * 获取指定目录下的所有的文件（不包括文件夹），采用了递归 
     *  
     * @param obj 
     * @return 
     */  
    public static ArrayList<File> getListFiles(Object obj) {  
        File directory = null;  
        if (obj instanceof File) 
        {  
            directory = (File) obj;  
        }
        else 
        {
            directory = new File(obj.toString()); //创建文件目录
        }
        ArrayList<File> files = new ArrayList<File>();  
        if (directory.isFile()) 
        {  
            files.add(directory);  
            return files;  
        } 
        else if (directory.isDirectory())
        {  
            File[] fileArr = directory.listFiles();  
            for (int i = 0; i < fileArr.length; i++)
            {  
                File fileOne = fileArr[i];  
                files.addAll(getListFiles(fileOne));  
            }  
        }
        return files;  
    }  
}
