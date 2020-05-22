package com.bn.ar.draw;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.opengl.GLES30;
import android.os.Build;

import com.bn.ar.utils.Constant;
import com.bn.ar.utils.MatrixState2D;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class ProgressBarDraw {
	FloatBuffer mVertexBuffer;//顶点坐标数据缓冲
	FloatBuffer mTexCoorBuffer;//顶点纹理坐标数据缓冲
    int muMVPMatrixHandle;//总变换矩阵引用id
    int maPositionHandle;//顶点位置属性引用id  
    int maTexCoorHandle;//顶点纹理坐标属性引用id
    int maXPosition;//当前加载位置属性引用id
    int maBottomTextureId;//纹理属性引用id
    int maTopTextureId;//纹理属性引用id
    
    int programId;//自定义渲染管线程序id
   	int texId;//纹理图片名
   	int texIdTop;
   	int vCount;//顶点个数
    public  float x,y;//需要平移的x,y坐标
   	float xPosition;//当前进度
    
    public ProgressBarDraw(int texId1,int texId2,int programId,float picWidth,float picHeight,float x,float y)
    {
    	this.texId = texId1;
		this.texIdTop = texId2;
		this.x=Constant.fromScreenXToNearX(x);//将屏幕x转换成视口x坐标
		this.y=Constant.fromScreenYToNearY(y);//将屏幕y转换成视口y坐标
		this.programId=programId;
		initVertexData(picWidth,picHeight);//初始化顶点数据
		initShader();//初始化着色器
    }
    
	public void initVertexData(float width,float height)//初始化顶点数据
	{
		vCount=4;//顶点个数
		width=Constant.fromPixSizeToNearSize(width);//屏幕宽度转换成视口宽度
		height=Constant.fromPixSizeToNearSize(height);//屏幕高度转换成视口高度
		//初始化顶点坐标数据
		float vertices[]=new float[]
		{
				-width/2,height/2,0,
				-width/2,-height/2,0,
				width/2,height/2,0,
				width/2,-height/2,0
		};
		ByteBuffer vbb=ByteBuffer.allocateDirect(vertices.length*4);//创建顶点坐标数据缓冲
		vbb.order(ByteOrder.nativeOrder());//设置字节顺序
		mVertexBuffer=vbb.asFloatBuffer();//转换为Float型缓冲
		mVertexBuffer.put(vertices);//向缓冲区中放入顶点坐标数据
		mVertexBuffer.position(0);//设置缓冲区起始位置
		float[] texCoor=new float[12];//初始化纹理坐标数据
		texCoor=new float[]{
				0,0,0,1,1,0,
				1,1,1,0,0,1};
		ByteBuffer cbb=ByteBuffer.allocateDirect(texCoor.length*4);//创建顶点纹理坐标数据缓冲
		cbb.order(ByteOrder.nativeOrder());//设置字节顺序
		mTexCoorBuffer=cbb.asFloatBuffer();//转换为Float型缓冲
		mTexCoorBuffer.put(texCoor);//向缓冲区中放入顶点着色数据
		mTexCoorBuffer.position(0);//设置缓冲区起始位置
	}
	@SuppressLint("NewApi")
	public void initShader()
	{
		//获取程序中顶点位置属性引用id  
		maPositionHandle = GLES30.glGetAttribLocation(programId, "aPosition");
		//获取程序中顶点纹理坐标属性引用id  
		maTexCoorHandle= GLES30.glGetAttribLocation(programId, "aTexCoor");
		//获取程序中总变换矩阵引用id
        muMVPMatrixHandle = GLES30.glGetUniformLocation(programId, "uMVPMatrix");  
        //当前加载到的位置
        maXPosition = GLES30.glGetUniformLocation(programId, "uXPosition");  
        //半透明纹理引用id
        maBottomTextureId = GLES30.glGetUniformLocation(programId, "uSampleBottom");  
      //纹理引用id
        maTopTextureId= GLES30.glGetUniformLocation(programId, "uSampleTop");  
	}
	
	
	public void setPositionX(float xPosition,float startX,float width)
	{
		this.xPosition = (xPosition-startX)/width;
	}
	
	public void setTexture(int texId,int texIdTop)
	{
		this.texId = texId;
		this.texIdTop = texIdTop;
	}
	//绘制图形
	public void drawSelf()
	{
		GLES30.glDisable(GLES30.GL_DEPTH_TEST);
		GLES30.glEnable(GLES30.GL_BLEND);//打开混合
		//设置混合因子
		GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA,GLES30.GL_ONE_MINUS_SRC_ALPHA);
		 //指定使用某套着色器程序
		GLES30.glUseProgram(programId);		
		//将最终变换矩阵传入渲染管线
		GLES30.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, MatrixState2D.getFinalMatrix(), 0); 
		GLES30.glUniform1f(maXPosition, xPosition);
		//将顶点位置数据传入渲染管线
		GLES30.glVertexAttribPointer  
		(
				maPositionHandle,
				3, 
				GLES30.GL_FLOAT,
				false,
				3*4,
				mVertexBuffer
				);
		//将顶点纹理坐标数据传入渲染管线
		GLES30.glVertexAttribPointer
		(
				maTexCoorHandle,
				2,
				GLES30.GL_FLOAT,
				false,
				2*4,
				mTexCoorBuffer
				);   
		//允许顶点位置数据数组
		GLES30.glEnableVertexAttribArray(maPositionHandle);  
		GLES30.glEnableVertexAttribArray(maTexCoorHandle);  
		
		//绑定纹理
		GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
		GLES30.glBindTexture(GLES30.GL_TEXTURE_2D,texId);
		GLES30.glActiveTexture(GLES30.GL_TEXTURE1);
		GLES30.glBindTexture(GLES30.GL_TEXTURE_2D,texIdTop);
		GLES30.glUniform1i(maBottomTextureId, 0);
		GLES30.glUniform1i(maTopTextureId, 1);
		
		//绘制纹理矩形--条带法
		GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, vCount); 
		//关闭混合
		GLES30.glDisable(GLES30.GL_BLEND);
		GLES30.glEnable(GLES30.GL_DEPTH_TEST);
	}
	
}
