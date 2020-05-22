package com.bn.ar.draw;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import com.bn.ar.utils.Constant;
import com.bn.ar.utils.MatrixState2D;
import android.annotation.SuppressLint;
import android.opengl.GLES30;

//有波浪效果的纹理矩形
public class DownLoadingTextureRect 
{	
	int mPrograms;//自定义渲染管线着色器程序id
    int muMVPMatrixHandle;//总变换矩阵引用
    int maPositionHandle; //顶点位置属性引用  
    int maTexCoorHandle; //顶点纹理坐标属性引用  
    int maStartAngleHandle; //本帧起始角度属性引用
    int muWidthSpanHandle;//横向长度总跨度引用    
    int currIndex=0;//当前着色器索引
	FloatBuffer   mVertexBuffer;//顶点坐标数据缓冲
	FloatBuffer   mTexCoorBuffer;//顶点纹理坐标数据缓冲
    int vCount=0;//顶点数量 
    int texId; //纹理图id
    float WIDTH_SPAN=0;//横向长度总跨度
    float currStartAngle=0;//当前帧的起始角度0~2PI
    public float x,y;
    
    
    public DownLoadingTextureRect(int texId,int mPrograms,float picWidth,float picHeight,float x,float y)
    {
    	this.texId = texId;
    	this.mPrograms= mPrograms;
    	this.x=Constant.fromScreenXToNearX(x);//将屏幕x转换成视口x坐标
		this.y=Constant.fromScreenYToNearY(y);//将屏幕y转换成视口y坐标
    	
    	initVertexData(picWidth,picHeight);//初始化顶点坐标与着色数据
    
    	initShader();	//初始化shader        
    	//启动一个线程定时换帧
    	new Thread()
    	{
    		public void run()
    		{
    			while(true)
    			{
    				currStartAngle+=(float) (Math.PI/16);
        			try 
        			{
    					Thread.sleep(30);
    				} catch (InterruptedException e) 
    				{
    					e.printStackTrace();
    				}
    			}     
    		}    
    	}.start();  
    }
    //初始化顶点坐标与着色数据的方法
    public void initVertexData(float picWidth,float picHeight)
    {
    	picWidth=Constant.fromPixSizeToNearSize(picWidth);//屏幕宽度转换成视口宽度
    	picHeight=Constant.fromPixSizeToNearSize(picHeight);//屏幕高度转换成视口高度
    	WIDTH_SPAN = picWidth;
    	final int cols=12;//列数
    	final int rows=cols;//行数
    	final float UNIT_SIZE=picWidth/cols;//每格的单位长度
    	//顶点坐标数据的初始化================begin============================
    	vCount=cols*rows*6;//每个格子两个三角形，每个三角形3个顶点
        float vertices[]=new float[vCount*3];//每个顶点xyz三个坐标
        int count=0;//顶点计数器
        for(int j=0;j<rows;j++)
        {
        	for(int i=0;i<cols;i++)
        	{
        		//计算当前格子左上侧点坐标 
        		float zsx=-UNIT_SIZE*cols/2+i*UNIT_SIZE;
        		float zsy=UNIT_SIZE*rows/2-j*UNIT_SIZE;
        		float zsz=0;
       
        		vertices[count++]=zsx;
        		vertices[count++]=zsy;
        		vertices[count++]=zsz;
        		
        		vertices[count++]=zsx;
        		vertices[count++]=zsy-UNIT_SIZE;
        		vertices[count++]=zsz;
        		
        		vertices[count++]=zsx+UNIT_SIZE;
        		vertices[count++]=zsy;
        		vertices[count++]=zsz;
        		
        		vertices[count++]=zsx+UNIT_SIZE;
        		vertices[count++]=zsy;
        		vertices[count++]=zsz;
        		
        		vertices[count++]=zsx;
        		vertices[count++]=zsy-UNIT_SIZE;
        		vertices[count++]=zsz;
        		
        		vertices[count++]=zsx+UNIT_SIZE;
        		vertices[count++]=zsy-UNIT_SIZE;
        		vertices[count++]=zsz;
        	}
        }
        //创建顶点坐标数据缓冲
        //vertices.length*4是因为一个整数四个字节
        ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length*4);
        vbb.order(ByteOrder.nativeOrder());//设置字节顺序
        mVertexBuffer = vbb.asFloatBuffer();//转换为Float型缓冲
        mVertexBuffer.put(vertices);//向缓冲区中放入顶点坐标数据
        mVertexBuffer.position(0);//设置缓冲区起始位置
        //顶点纹理坐标数据的初始化================begin============================
        float texCoor[]=generateTexCoor(cols,rows);     
        //创建顶点纹理坐标数据缓冲
        ByteBuffer cbb = ByteBuffer.allocateDirect(texCoor.length*4);
        cbb.order(ByteOrder.nativeOrder());//设置字节顺序
        mTexCoorBuffer = cbb.asFloatBuffer();//转换为Float型缓冲
        mTexCoorBuffer.put(texCoor);//向缓冲区中放入顶点着色数据
        mTexCoorBuffer.position(0);//设置缓冲区起始位置
    
    }
    //初始化shader
    @SuppressLint("NewApi")
	public void initShader()
    {
        //获取程序中顶点位置属性引用  
        maPositionHandle= GLES30.glGetAttribLocation(mPrograms, "aPosition");
        //获取程序中顶点纹理坐标属性引用  
        maTexCoorHandle= GLES30.glGetAttribLocation(mPrograms, "aTexCoor");
        //获取程序中总变换矩阵引用
        muMVPMatrixHandle = GLES30.glGetUniformLocation(mPrograms, "uMVPMatrix");  
        //获取本帧起始角度属性引用
        maStartAngleHandle=GLES30.glGetUniformLocation(mPrograms, "uStartAngle");  
        //获取横向长度总跨度引用
        muWidthSpanHandle=GLES30.glGetUniformLocation(mPrograms, "uWidthSpan");  
    }
    @SuppressLint("NewApi")
	public void drawSelf()
    {
    	GLES30.glDisable(GLES30.GL_DEPTH_TEST);
    	GLES30.glEnable(GLES30.GL_BLEND);//打开混合
    	//设置混合因子
		GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA,GLES30.GL_ONE_MINUS_SRC_ALPHA);
    	 //指定使用某套shader程序
    	 GLES30.glUseProgram(mPrograms); 
         //将最终变换矩阵传入渲染管线
         GLES30.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, MatrixState2D.getFinalMatrix(), 0); 
         
         //将本帧起始角度传入渲染管线
         GLES30.glUniform1f(maStartAngleHandle, currStartAngle);
         //将横向长度总跨度传入渲染管线
         GLES30.glUniform1f(muWidthSpanHandle, WIDTH_SPAN);  
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
         //启用顶点位置、纹理坐标数据
         GLES30.glEnableVertexAttribArray(maPositionHandle);  
         GLES30.glEnableVertexAttribArray(maTexCoorHandle);  
         //绑定纹理
         GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
         GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, texId);
         GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, vCount); 
         
     	//关闭混合
     	GLES30.glDisable(GLES30.GL_BLEND);
     	GLES30.glEnable(GLES30.GL_DEPTH_TEST);
    }
    //自动切分纹理产生纹理数组的方法
    public float[] generateTexCoor(int bw,int bh)
    {
    	float[] result=new float[bw*bh*6*2]; 
    	float sizew=1.0f/bw;//列数
    	float sizeh=1.0f/bh;//行数
    	int c=0;
    	for(int i=0;i<bh;i++)
    	{
    		for(int j=0;j<bw;j++)
    		{
    			//每行列一个矩形，由两个三角形构成，共六个点，12个纹理坐标
    			float s=j*sizew;
    			float t=i*sizeh;
    			
    			result[c++]=s;
    			result[c++]=t;
    			
    			result[c++]=s;
    			result[c++]=t+sizeh;
    			
    			result[c++]=s+sizew;
    			result[c++]=t;
    			
    			
    			result[c++]=s+sizew;
    			result[c++]=t;
    			
    			result[c++]=s;
    			result[c++]=t+sizeh;
    			
    			result[c++]=s+sizew;
    			result[c++]=t+sizeh;    			
    		}
    	}
    	return result;
    }
}
