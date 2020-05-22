package com.bn.ar.utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.Stack;

import com.qualcomm.vuforia.Matrix44F;

import android.opengl.Matrix;

//存储系统矩阵状态的类
public class MatrixState 
{
     static float[] mVMatrix = new float[16];//摄像机位置朝向9参数矩阵
    static float[] mMVPMatrix;//最后起作用的总变换矩阵
    public static float[] lightLocation=new float[]{0,0,0};//定位光光源位置
    public static Stack<float[]> mStack=new Stack<float[]>();//保护变换矩阵的栈
    public static float[] currMatrix;//当前变换矩阵
    public static float[] cameraLocation;//设置光源位置用
    public static FloatBuffer cameraFB;    
    public static FloatBuffer lightPositionFB;
 
    public static void setInitStack()//获取不变换初始矩阵
    {
    	currMatrix=new float[16];
    	Matrix.setRotateM(currMatrix, 0, 0, 1, 0, 0);
    }
    
    public static void pushMatrix()//保护变换矩阵
    {
    	mStack.push(currMatrix.clone());
    }
    
    public static void popMatrix()//恢复变换矩阵
    {
    	currMatrix=mStack.pop();
    }
    
    public static void translate(float x,float y,float z)//设置沿xyz轴移动
    {
    	Matrix.translateM(currMatrix, 0, x, y, z);
    }
    
    public static void rotate(float angle,float x,float y,float z)//设置绕xyz轴移动
    {
    	Matrix.rotateM(currMatrix,0,angle,x,y,z);
    }
    
    public static void scale(float x,float y,float z){
    	Matrix.scaleM(currMatrix, 0, x, y, z);
    }
    //获取具体物体的变换矩阵
	public static float[] getMMatrix() {
		return currMatrix;
	}
	
    //根据投影矩阵，获取物体最终变换矩阵
    public static float[] getFinalMatrix(Matrix44F projectionMatrix)
    {
    	//投影矩阵、摄像机矩阵、当前矩阵得乘积
    	float[] mMVPMatrix=new float[16];    	
    	Matrix.multiplyMM(mMVPMatrix, 0, mVMatrix, 0, currMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, projectionMatrix.getData(), 0, mMVPMatrix, 0);    
    	return mMVPMatrix;
    }
	
    //设置摄像机
    public static void setCamera(float[]  matrix){
    	mVMatrix = Arrays.copyOf(matrix, matrix.length); 	
    	
    	cameraLocation = new float[3];//getCameraPosition();//摄像机位置
//    	cameraLocation[0] = mVMatrix[12];
//    	cameraLocation[1] = mVMatrix[13];
//    	cameraLocation[2] = mVMatrix[14];
    	
    	cameraLocation = getCameraPosition();//摄像机位置
//    	System.out.println("===:::"+cameraLocation[0]+"    "+cameraLocation[1]+"   "+cameraLocation[2]);
    	
    	ByteBuffer llbb = ByteBuffer.allocateDirect(3*4);
        llbb.order(ByteOrder.nativeOrder());//设置字节顺序
        cameraFB=llbb.asFloatBuffer();
        cameraFB.put(cameraLocation);
        cameraFB.position(0);  
    }
    
    //获取摄像机位置数据
    public static float[] getCameraPosition()
    {
    	float fx=mVMatrix[2];
    	float fy=mVMatrix[6];
    	float fz=mVMatrix[10];
    	
    	float ex=mVMatrix[12];
    	float ey=mVMatrix[13];
    	float ez=mVMatrix[14];
    	
    	float result=(float) Math.sqrt((ex * ex + ey * ey + ez * ez));
    	
    	float kx=fx*result;
    	float ky=fy*result;
    	float kz=fz*result;
    	
    	float[] cameraL = new float[3];
    	//摄像机位置
    	cameraL[0]=kx;
    	cameraL[1]=ky;
    	cameraL[2]=kz;
    	return cameraL;
    }

    
    
  //设置灯光位置的方法
    public static void setLightLocation(float x,float y,float z)
    {
    	lightLocation[0]=x;
    	lightLocation[1]=y;
    	lightLocation[2]=z;
    	ByteBuffer llbb = ByteBuffer.allocateDirect(3*4);
        llbb.order(ByteOrder.nativeOrder());//设置字节顺序
        lightPositionFB=llbb.asFloatBuffer();
        lightPositionFB.put(lightLocation);
        lightPositionFB.position(0);
    }
}
