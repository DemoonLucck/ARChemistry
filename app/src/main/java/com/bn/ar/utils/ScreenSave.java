package com.bn.ar.utils;
import static com.bn.ar.utils.Constant.*;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import com.bn.ar.views.GlSurfaceView;
import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Matrix;
import android.opengl.GLES30;
import android.os.Build;

public class ScreenSave {
	
	//创建Matrixy对象
	Matrix mMatrix=new Matrix();
	//创建File对象，用于存储截图。
	File file=new File(screenSavePath);
	GlSurfaceView view;
	public ScreenSave(GlSurfaceView view){
		this.view=view;
	}
	//设置标志位
	public synchronized   void setFlag(boolean flag)
    {
    	saveFlag=flag;
    }
    
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
	public void saveScreen()
    {				
		//矩阵重置
		mMatrix.reset();
		//上下旋转
		mMatrix.setRotate(180);
		//缩放图片，当px=-1时表示左右翻转
		mMatrix.postScale(-1, 1);
		//分配新的直接字节缓冲区,参数表示缓冲区的容量
		ByteBuffer cbbTemp = ByteBuffer.allocateDirect(
    			(int) ((SCREEN_WIDTH_STANDARD*Constant.ssr.ratio)*
    					(SCREEN_HEIGHT_STANDARD*Constant.ssr.ratio)*4));
    	
    	//简单的OpenGL像素操作:读取一些像素。当前可以简单理解为“把已经绘制好的像素（它可能已经被保存到显卡的显存中）读取到内存”。
    	//前四个参数可以得到一个矩形，该矩形所包括的像素都会被读取出来
    	//(第一、二个参数表示了矩形的左下角横、纵坐标，坐标以窗口最左下角为零，最右上角为最大值；
    	//第三、四个参数表示了矩形的宽度和高度)
    	//第五个参数表示读取的内容,GL_RGBA则会依次读取像素的红、绿、蓝、alpha四种数据
    	//第六个参数表示读取的内容保存到内存时所使用的格式，
    	//例如：GL_UNSIGNED_BYTE会把各种数据保存为GLubyte，GL_FLOAT会把各种数据保存为GLfloat等
    	//第七个参数表示一个指针，像素数据被读取后，将被保存到这个指针所表示的地址.
    	GLES30.glReadPixels(
    			Constant.ssr.lucX,
				Constant.ssr.lucY, 
    			(int)(SCREEN_WIDTH_STANDARD*Constant.ssr.ratio), 
    			(int)(SCREEN_HEIGHT_STANDARD*Constant.ssr.ratio), 
    			GLES30.GL_RGBA,//表示读取的内容
    			GLES30.GL_UNSIGNED_BYTE, //表示读取的内容保存到内存时所使用的格式
    			cbbTemp);
    	
    	//创建新图片
    	Bitmap bm =Bitmap.createBitmap(
    			(int)(SCREEN_WIDTH_STANDARD*Constant.ssr.ratio), //指定位图宽度、单位为像素
    			(int)(SCREEN_HEIGHT_STANDARD*Constant.ssr.ratio), //指定位图高度、单位为像素。
    			Config.ARGB_8888);//设置色彩模式
    	 
    	bm.copyPixelsFromBuffer(cbbTemp);
    	bm=Bitmap.createBitmap(
    			bm, 
    			0, 0,
    			(int)(SCREEN_WIDTH_STANDARD*Constant.ssr.ratio), 
    			(int)(SCREEN_HEIGHT_STANDARD*Constant.ssr.ratio), 
    			mMatrix,
    			true);
		
    	try
		{

            //如果目标目录不存在，则创建  
            if (!file.exists()) 
            {  
                file.mkdirs();  
            }  
    		File myFile = File.createTempFile(
    						"ScreenShot",  //基本文件名
    						".png",     //后缀
    						file
    		);
    		
			 FileOutputStream fout=new FileOutputStream(myFile);
			 BufferedOutputStream bos = new BufferedOutputStream(fout);  
			 bm.compress
			 (
					 Bitmap.CompressFormat.PNG,   //图片格式
					 100, 						   //品质0-100
					 bos						   //使用的输出流
			  );   
			 bos.flush();   
			 bos.close();
			 System.out.println("保存成功，文件名："+myFile.getName());
 			 goToast(view.activity,Constant.GO_TOAST_SUCCESS);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("保存失败！"); 
			goToast(view.activity,Constant.GO_TOAST_FAIL);
		}
    }
}
