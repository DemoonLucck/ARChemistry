 package com.bn.ar.manager;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import com.bn.ar.views.GlSurfaceView;
import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES30;
import android.opengl.GLUtils;
import android.os.Build;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class TextureManager
{
	public static String[] texturesName={
		"bg_back.png","load_front.png","load_bottom.png",
		"title.png","screenshot.png",
		"download.png","undownload.png","downloading.png","speech.png",
		"goback.png","soundoff.png",
		"help.png","about.png","soundon.png",		
		"about.png","back.png","ground.png","help1.png","help2.png","help3.png",
		"close.png","whitecircle.png","blackcircle.png","into.png","datamanager.png",
		"experience.png","aboutC.png",
	};//纹理图的名称
	static HashMap<String,Integer> texList=new HashMap<String,Integer>();//放纹理图的列表
	public static int initTexture(GlSurfaceView mv,String texName,boolean isRepeat)//生成纹理id
	{
		int[] textures=new int[1];
		GLES30.glGenTextures
		(
				1,//产生的纹理id的数量
				textures,//纹理id的数组
				0//偏移量
		);
		GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textures[0]);//绑定纹理id
		//设置MAG时为线性采样
		GLES30.glTexParameterf
		(
				GLES30.GL_TEXTURE_2D,
				GLES30.GL_TEXTURE_MAG_FILTER,
				GLES30.GL_LINEAR
		);
		//设置MIN时为最近点采样
		GLES30.glTexParameterf
		(
				GLES30.GL_TEXTURE_2D,
				GLES30.GL_TEXTURE_MIN_FILTER, 
				GLES30.GL_NEAREST
		);
		if(isRepeat)
		{
			//设置S轴的拉伸方式为重复拉伸
			GLES30.glTexParameterf
			(
					GLES30.GL_TEXTURE_2D,
					GLES30.GL_TEXTURE_WRAP_S, 
					GLES30.GL_REPEAT
			);
			//设置T轴的拉伸方式为重复拉伸
			GLES30.glTexParameterf
			(
					GLES30.GL_TEXTURE_2D,
					GLES30.GL_TEXTURE_WRAP_T, 
					GLES30.GL_REPEAT
			);
		}else
		{
			//设置S轴的拉伸方式为截取
			GLES30.glTexParameterf
			(
					GLES30.GL_TEXTURE_2D,
					GLES30.GL_TEXTURE_WRAP_S, 
					GLES30.GL_CLAMP_TO_EDGE
			);
			//设置T轴的拉伸方式为截取
			GLES30.glTexParameterf
			(
					GLES30.GL_TEXTURE_2D,
					GLES30.GL_TEXTURE_WRAP_T, 
					GLES30.GL_CLAMP_TO_EDGE
			);
		}
		String path="pic/"+texName;//定义图片路径
		InputStream in = null;
		try {
			in = mv.getResources().getAssets().open(path);
		}catch (IOException e) {
			e.printStackTrace();
		}
		Bitmap bitmap=BitmapFactory.decodeStream(in);//从流中加载图片内容
		GLUtils.texImage2D
		(
				GLES30.GL_TEXTURE_2D,//纹理类型，在OpenGL ES中必须为GL10.GL_TEXTURE_2D
				0,//纹理的层次，0表示基本图像层，可以理解为直接贴图
				bitmap,//纹理图像
				0//纹理边框尺寸
		);
		bitmap.recycle();//纹理加载成功后释放内存中的纹理图
		return textures[0];
	}
	//用来初始化bitmap格式图片并生成纹理id的方法
	public static int initTexture(Bitmap bitmap,boolean isRepeat)
	{
		int[] textures=new int[1];
		GLES30.glGenTextures
		(
				1,//产生的纹理id的数量
				textures,//纹理id的数组
				0//偏移量
		);
		GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textures[0]);//绑定纹理id
		//设置MAG时为线性采样
		GLES30.glTexParameterf
		(
				GLES30.GL_TEXTURE_2D,
				GLES30.GL_TEXTURE_MAG_FILTER,
				GLES30.GL_LINEAR
		);
		//设置MIN时为最近点采样
		GLES30.glTexParameterf
		(
				GLES30.GL_TEXTURE_2D,
				GLES30.GL_TEXTURE_MIN_FILTER, 
				GLES30.GL_NEAREST
		);
		if(isRepeat)
		{
			//设置S轴的拉伸方式为重复拉伸
			GLES30.glTexParameterf
			(
					GLES30.GL_TEXTURE_2D,
					GLES30.GL_TEXTURE_WRAP_S, 
					GLES30.GL_REPEAT
			);
			//设置T轴的拉伸方式为重复拉伸
			GLES30.glTexParameterf
			(
					GLES30.GL_TEXTURE_2D,
					GLES30.GL_TEXTURE_WRAP_T, 
					GLES30.GL_REPEAT
			);
		}else
		{
			//设置S轴的拉伸方式为截取
			GLES30.glTexParameterf
			(
					GLES30.GL_TEXTURE_2D,
					GLES30.GL_TEXTURE_WRAP_S, 
					GLES30.GL_CLAMP_TO_EDGE
			);
			//设置T轴的拉伸方式为截取
			GLES30.glTexParameterf
			(
					GLES30.GL_TEXTURE_2D,
					GLES30.GL_TEXTURE_WRAP_T, 
					GLES30.GL_CLAMP_TO_EDGE
			);
		}
		
		GLUtils.texImage2D
		(
				GLES30.GL_TEXTURE_2D,//纹理类型，在OpenGL ES中必须为GL10.GL_TEXTURE_2D
				0,//纹理的层次，0表示基本图像层，可以理解为直接贴图
				bitmap,//纹理图像
				0//纹理边框尺寸
		);
		return textures[0];
	}
	
	public static void loadingTexture(GlSurfaceView mv,int start,int picNum)//加载所有纹理图
	{
		for(int i=start;i<start+picNum;i++)
		{
			int texture=0;
			if((texturesName[i].equals("ghxp.png"))||(texturesName[i].equals("modeltexture.png")))
			{
				//以重复拉伸的方式对图片进行初始化操作
				texture=initTexture(mv,texturesName[i],true);
			}else
			{
				//以截取的方式对图片进行初始化操作
				texture=initTexture(mv,texturesName[i],false);
			}
			texList.put(texturesName[i],texture);//将数据加入到列表中
		}
	}
	public static int getTextures(String texName)//获得纹理图
	{
		int result=0;
		if(texList.get(texName)!=null)//如果列表中有此纹理图
		{
			result=texList.get(texName);//获取纹理图
		}else
		{
			result=-1;
		}
		return result;
	}
}
