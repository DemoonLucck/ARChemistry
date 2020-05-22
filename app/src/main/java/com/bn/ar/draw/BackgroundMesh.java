package com.bn.ar.draw;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import android.annotation.SuppressLint;
import android.opengl.GLES30;
import android.util.Log;

import com.bn.ar.manager.ShaderManager;
import com.qualcomm.vuforia.Eyewear;
import com.qualcomm.vuforia.Renderer;
import com.qualcomm.vuforia.VIDEO_BACKGROUND_REFLECTION;
import com.qualcomm.vuforia.VideoBackgroundTextureInfo;

import static com.bn.ar.utils.Constant.*;

@SuppressLint("NewApi")
public class  BackgroundMesh//背景绘制类
{
    
    private static final String LOGTAG = "BackgroundMesh";

    private ByteBuffer mOrthoQuadIndices = null;//索引数据缓冲
	private FloatBuffer mVertexBuffer=null;//顶点坐标数据缓冲
    private FloatBuffer mTextureBuffer=null;//顶点着色数据缓冲
    
    private int mNumVertexRows;//顶点的行数
    private int mNumVertexCols;//顶点的列数
    
    int mProgram;//自定义渲染管线着色器程序id
    int vbVertexPositionHandle;//顶点位置属性引用id 
    int vbVertexTexCoordHandle;//顶点纹理坐标属性引用id 
    int vbTexSampler2DHandle;//外观纹理属性引用id
    int vbProjectionMatrixHandle;// 投影矩阵引用id   
    
	public BackgroundMesh(int vbNumVertexRows,int vbNumVertexCols,boolean isActivityInPortraitMode)
	{
		if ( vbNumVertexRows < 2 || vbNumVertexCols < 2 )
	    {
			throw new IllegalArgumentException("vbNumVertexRows and vbNumVertexCols must be at least 2");
	    }
		
        mNumVertexRows = vbNumVertexRows;
        mNumVertexCols = vbNumVertexCols;

        //从相机获得纹理和图像尺寸
        VideoBackgroundTextureInfo texInfo = Renderer.getInstance().getVideoBackgroundTextureInfo();
	     
        int reflection = Renderer.getInstance().getVideoBackgroundConfig()
            .getReflection();
        float reflectionOffset = (float) vbNumVertexCols - 1.0f;
	        
        Log.e(LOGTAG, "Activity is in "
            + (isActivityInPortraitMode ? "Portrait" : "Landscape"));
        
        //没有图像数据
        if ((texInfo.getImageSize().getData()[0] == 0)
            || (texInfo.getImageSize().getData()[1] == 0))
        {
            Log.e(LOGTAG, "Invalid Image Size!! "
                + texInfo.getImageSize().getData()[0] + " x "
                + texInfo.getImageSize().getData()[1]);
            return;
        }
        
	    //计算纹理坐标的斜率
	    float uRatio = ((float) texInfo.getImageSize().getData()[0] / (float) texInfo
	        .getTextureSize().getData()[0]);
	    float vRatio = ((float) texInfo.getImageSize().getData()[1] / (float) texInfo
	        .getTextureSize().getData()[1]);
	    float uSlope = uRatio / ((float) vbNumVertexCols - 1.0f);
	    float vSlope = vRatio / ((float) vbNumVertexRows - 1.0f);
	        
        // These calculate a slope for the vertex values in this case we have a
        // span of 2, from -1 to 1
        float totalSpan = 2.0f;
        float colSlope = totalSpan / ((float) vbNumVertexCols - 1.0f);
        float rowSlope = totalSpan / ((float) vbNumVertexRows - 1.0f);
	        
        //顶点数据数组
        float vertices[]=new float[vbNumVertexRows*vbNumVertexCols*3]; 
        int vcount=0;//数组索引id     
        //纹理数据数组
        float textures[]=new float[vbNumVertexRows*vbNumVertexCols*2];
        int tcount=0;//数组索引id
        //索引数据数组
        byte[] indices=new byte[getNumObjectIndex()];
        int icount=0;//数组索引id
        //索引值辅助变量
        short currentVertexIndex = 0;
        
        for (int j = 0; j < vbNumVertexRows; j++)
        {
            for (int i = 0; i < vbNumVertexCols; i++)
            { 
            	vertices[vcount++]=(colSlope * i) - (totalSpan / 2.0f);
            	vertices[vcount++]=(rowSlope * j) - (totalSpan / 2.0f);
            	vertices[vcount++]=0;
                
                float u = 0.0f, v = 0.0f;
                //计算纹理坐标
                if (isActivityInPortraitMode)
                {//竖屏模式
                    u = uRatio - (uSlope * j);
                    v = vRatio
                        - ((reflection == VIDEO_BACKGROUND_REFLECTION.VIDEO_BACKGROUND_REFLECTION_ON) ? vSlope
                            * (reflectionOffset - i)
                            : vSlope * i);
                    
                } else {//横屏模式
                    u = ((reflection == VIDEO_BACKGROUND_REFLECTION.VIDEO_BACKGROUND_REFLECTION_ON) ? uSlope
                        * (reflectionOffset - i)
                        : uSlope * i);
                    v = vRatio - (vSlope * j);
                }
                textures[tcount++]=u;
                textures[tcount++]=v;
                
                // Now we populate the triangles that compose the mesh
                // First triangle is the upper right of the vertex
                if (j < vbNumVertexRows - 1)
                {
                    // In the example above this would make triangles ABD and BCE
                    if (i < vbNumVertexCols - 1)
                    {
                    	indices[icount++]=(byte) currentVertexIndex;
                    	indices[icount++]=(byte) (currentVertexIndex + 1);
                    	indices[icount++]=(byte) (currentVertexIndex + vbNumVertexCols);
                    	
                    }
                    // In the example above this would make triangles BED and CFE
                    if (i > 0) 
                    {
                    	indices[icount++]=(byte) currentVertexIndex;
                    	indices[icount++]=(byte) (currentVertexIndex + vbNumVertexCols);
                    	indices[icount++]=(byte) (currentVertexIndex + vbNumVertexCols - 1);
                    }
                }
                currentVertexIndex += 1; // Vertex index increased by one
            }
        }
        //创建顶点坐标数据缓冲
        //vertices.length*4是因为一个整数四个字节
        ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length*4);
        vbb.order(ByteOrder.nativeOrder());//设置字节顺序
        mVertexBuffer = vbb.asFloatBuffer();//转换为int型缓冲
        mVertexBuffer.put(vertices);//向缓冲区中放入顶点坐标数据
        mVertexBuffer.position(0);//设置缓冲区起始位置
        //特别提示：由于不同平台字节顺序不同数据单元不是字节的一定要经过ByteBuffer
        //转换，关键是要通过ByteOrder设置nativeOrder()，否则有可能会出问题
        //顶点坐标数据的初始化================end============================
        
        //创建顶点纹理数据缓冲
        ByteBuffer tbb = ByteBuffer.allocateDirect(textures.length*4);
        tbb.order(ByteOrder.nativeOrder());//设置字节顺序
        mTextureBuffer= tbb.asFloatBuffer();//转换为Float型缓冲
        mTextureBuffer.put(textures);//向缓冲区中放入顶点着色数据
        mTextureBuffer.position(0);//设置缓冲区起始位置
        //特别提示：由于不同平台字节顺序不同数据单元不是字节的一定要经过ByteBuffer
        //转换，关键是要通过ByteOrder设置nativeOrder()，否则有可能会出问题
        //顶点纹理数据的初始化================end============================
        
        //索引缓冲
        mOrthoQuadIndices=ByteBuffer.allocateDirect(indices.length);
        mOrthoQuadIndices.put(indices);
        mOrthoQuadIndices.position(0);
	        
	}
    
	//初始化着色器的方法
    public void initShader()
    {
    	mProgram = ShaderManager.getShader(2);
        //获取程序中顶点位置属性引用  
        vbVertexPositionHandle = GLES30.glGetAttribLocation(mProgram, "vertexPosition");
        //获取程序中顶点纹理坐标属性引用  
        vbVertexTexCoordHandle = GLES30.glGetAttribLocation(mProgram, "vertexTexCoord");
        //获取程序中纹理图属性引用
        vbTexSampler2DHandle = GLES30.glGetUniformLocation(mProgram, "texSampler2D");
        //最终变换矩阵
        vbProjectionMatrixHandle = GLES30.glGetUniformLocation(mProgram, "projectionMatrix");
        //平行投影的投影矩阵
        vbOrthoProjMatrix=Eyewear.getInstance().getOrthographicProjectionMatrix();
    }
    
	//绘制方法
	@SuppressLint("NewApi")
	public void drawSelf(int vbVideoTextureUnit,int numEyes)
	{
   	 	//指定使用某套shader程序
   	 	GLES30.glUseProgram(mProgram);
   	 	//绑定纹理
   	 	GLES30.glUniform1i(vbTexSampler2DHandle, vbVideoTextureUnit);
   	 	GLES30.glUniformMatrix4fv(vbProjectionMatrixHandle, 1, false, vbOrthoProjMatrix.getData(), 0);
   	 	//将顶点位置数据传入渲染管线
   	 	GLES30.glVertexAttribPointer(
   	 		vbVertexPositionHandle, 
   	 		3, 
   	 		GLES30.GL_FLOAT, 
   	 		false, 
   	 		3*4,
   	 		mVertexBuffer
        );
   	 	//将顶点纹理坐标数据传入渲染管线
   	    GLES30.glVertexAttribPointer(
    		vbVertexTexCoordHandle, 
    		2, 
    		GLES30.GL_FLOAT, 
    		false, 
    		2*4,
    		mTextureBuffer
    	);
        //启用顶点位置、纹理坐标数据
        GLES30.glEnableVertexAttribArray(vbVertexPositionHandle);
        GLES30.glEnableVertexAttribArray(vbVertexTexCoordHandle);
        // Then, we issue the render call 
        for (int eyeIdx=0; eyeIdx<numEyes; eyeIdx++)
        {
            if (Eyewear.getInstance().isDeviceDetected())
            {
                int eyeViewportPosX = viewportPosX;
                int eyeViewportPosY = viewportPosY;
                int eyeViewportSizeX = viewportSizeX;
                int eyeViewportSizeY = viewportSizeY;

                // Setup the viewport filling half the screen
                // Position viewport for left or right eye
                if (eyeIdx == 0) // left eye
                {
                    eyeViewportSizeX = viewportSizeX / 2;
                }
                else // right eye
                {
                    eyeViewportPosX = viewportSizeX / 2;
                    eyeViewportSizeX = viewportSizeX / 2;
                }
                //设置视口
                GLES30.glViewport(eyeViewportPosX, eyeViewportPosY, eyeViewportSizeX, eyeViewportSizeY);
            }
            else
            {
            	//设置视口
                GLES30.glViewport(viewportPosX, viewportPosY, viewportSizeX, viewportSizeY);
            }

            //索引绘制
            GLES30.glDrawElements(GLES30.GL_TRIANGLES, getNumObjectIndex(), GLES30.GL_UNSIGNED_BYTE, 
            		mOrthoQuadIndices);
        }

        //关闭顶点位置、纹理坐标数据
        GLES30.glDisableVertexAttribArray(vbVertexPositionHandle);
        GLES30.glDisableVertexAttribArray(vbVertexTexCoordHandle);
	}
	
	//检查缓冲是否为空
    public boolean isValid()
    {
        return mVertexBuffer != null && mTextureBuffer != null
            && mOrthoQuadIndices != null;
    }
    //获取索引值个数
    public int getNumObjectIndex()
    {
        return (mNumVertexCols - 1) * (mNumVertexRows - 1) * 6;
    }
    
}
