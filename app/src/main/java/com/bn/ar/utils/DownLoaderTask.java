package com.bn.ar.utils;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import com.bn.thread.URLConnectionThread;

import android.os.AsyncTask;

public class DownLoaderTask extends AsyncTask<Void, Integer, Long> {//下载任务类继承了异步类，把耗时下载任务放在其中。
	private URL mUrl;//下载文件需要的URL
	private File mFile;//下载的文件
	private File mFileParent;//下载的文件的根目录
	private FileOutputStream mOutputStream;
	int index;
	int type = -1;
    //url为下载文件的网络地址，out为存放文件的路径，index为要下载的模型的索引，type指定下载文件的类型
	public DownLoaderTask(String url,String out,int index,int type){
		super();
		this.index = index;
		this.type = type;
		try {
			mUrl = new URL(url);//下载地址
			String fileName = new File(mUrl.getFile()).getName();//获得文件名称（zip包名称或pic名称）
			mFileParent = new File(out);//创建用于存放zip或pic的文件
			if(!mFileParent.exists()){mFileParent.mkdirs();}//如果目录不存在， 就创建一个目录
			mFile = new File(mFileParent.getPath(), fileName);//在存储路径上创建一个文件用来存储下载下来的字节流
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
	@Override
	protected Long doInBackground(Void... params) {
		return download();  
	}  
	
	private long download(){
		URLConnection connection = null;  
		int bytesCopied = 0;//表示当前从网络下载到指定文件的byte数
		try {
			connection = mUrl.openConnection();//连接网络
			int length = connection.getContentLength();//获得要下载文件的长度
			if(mFile.exists()&&length==-1)//下载失败
			{
				mFile.delete();//删除下载文件目录
				mFileParent.delete();//删除文件所在的根目录
				Constant.net_success = false;//表示联网失败
				URLConnectionThread.flag = false;//线程停止内层循环
				return 0l;
			}

			mOutputStream = new FileOutputStream(mFile);
			bytesCopied =copy(connection.getInputStream(),mOutputStream);//把从url得到的数据在指定文件上拷贝
			Constant.net_success = true;//表示联网成功
			URLConnectionThread.flag = true;//下载任务进行
			if(bytesCopied!=length&&length!=-1){//下载不完整				
				System.out.println("Download incomplete bytesCopied="+bytesCopied+", length"+length);
			}
			if(bytesCopied==length)
			{//下载成功
				if(type==Constant.ZIP_FLAG){//如果当前下载的是zip包，则进行解压					
					//解压SD卡指定目录下的zip包
					String sZipPathFile = Constant.ZIP_LOAD_PATH+mFile.getName();
					ZipUtil.Ectract(sZipPathFile, Constant.UNZIP_TO_PATH);
					
					Constant.downLoadZipChangeT[index] =true;//改变是否改变纹理图的标志位
					Constant.downLoadZipFinish[index] = true;//表示此模型下载完成
					Constant.selectDataIndex[index] = false;//将此模型设置为未选中状态
				}

			}
			mOutputStream.close();
		} catch (IOException e) {
			System.out.println("%%%%%%%%联网失败%%%%%%%%%");
			Constant.net_success = false;
			URLConnectionThread.flag = false;
			e.printStackTrace();  
		}  
		return bytesCopied;  
	}  
	private int copy(InputStream input, OutputStream output){  
		byte[] buffer = new byte[1024*8];  
		BufferedInputStream in = new BufferedInputStream(input, 1024*8);  
		BufferedOutputStream out  = new BufferedOutputStream(output, 1024*8);  
		int count =0,n=0;  
		try {  
			while((n=in.read(buffer, 0, 1024*8))!=-1){  
				out.write(buffer, 0, n);  
				count+=n;  
			}  
			out.flush();  
		} catch (IOException e) {  
			e.printStackTrace();  
		}finally{  
			try {
				out.close();  
			} catch (IOException e) {  
				e.printStackTrace();  
			}  
			try {  
				in.close();  
			} catch (IOException e) {
				e.printStackTrace();  
			}  
		}  
		return count;  
	}
}