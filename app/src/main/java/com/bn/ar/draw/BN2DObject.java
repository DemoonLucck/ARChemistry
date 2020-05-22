package com.bn.ar.draw;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import com.bn.ar.utils.Constant;
import com.bn.ar.utils.MatrixState2D;

import android.annotation.SuppressLint;
import android.opengl.GLES30;

@SuppressLint("NewApi")
public class BN2DObject {
	FloatBuffer mVertexBuffer;// 顶点坐标数据缓冲
	FloatBuffer mTexCoorBuffer;// 顶点纹理坐标数据缓冲
	int muMVPMatrixHandle;// 总变换矩阵引用id
	int maPositionHandle;// 顶点位置属性引用id
	int maTexCoorHandle;// 顶点纹理坐标属性引用id
	int programId;// 自定义渲染管线程序id
	int texId;// 纹理图片名
	int vCount;// 顶点个数
	public float x = 0;// 起始位置的x坐标
	public float y = 0;// 起始位置的y坐标
	public float scaleTemp = 1.0f;// 进行缩放的倍数
	boolean firstOver = false;// 第一次缩放是否结束
	boolean secondOver = false;// 第二次缩放是否结束
	boolean thirdOver = false;// 第三次缩放是否结束
	boolean scaleOverFlag = false;// 是否缩放结束的标志位
	float scaleFirstSpan = 0.005f;// 缩小时的步长
	float scaleSecondSpan = 0.005f;// 扩大时的步长
	float scaleThirdSpan = 0.005f;// 缩小时的步长
	float scaleFirstEnd = 0.93f;// 判断缩小是否结束的值
	float scaleSecondEnd = 1.07f;// 判断扩大是否结束的值
	float scaleThirdEnd = 1f;// 判断缩小是否结束的值

	public BN2DObject(int texId, int programId, float picWidth,
			float picHeight, float x, float y) {
		this.x = Constant.fromScreenXToNearX(x);// 将屏幕x转换成视口x坐标
		this.y = Constant.fromScreenYToNearY(y);// 将屏幕y转换成视口y坐标
		this.texId = texId;
		this.programId = programId;
		initVertexData(picWidth, picHeight);// 初始化顶点数据
		initShader();// 初始化着色器
	}

	public void initVertexData(float width, float height)// 初始化顶点数据
	{
		vCount = 4;// 顶点个数
		width = Constant.fromPixSizeToNearSize(width);// 屏幕宽度转换成视口宽度
		height = Constant.fromPixSizeToNearSize(height);// 屏幕高度转换成视口高度
		// 初始化顶点坐标数据
		float vertices[] = new float[] { -width / 2, height / 2, 0, -width / 2,
				-height / 2, 0, width / 2, height / 2, 0, width / 2,
				-height / 2, 0 };
		ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);// 创建顶点坐标数据缓冲
		vbb.order(ByteOrder.nativeOrder());// 设置字节顺序
		mVertexBuffer = vbb.asFloatBuffer();// 转换为Float型缓冲
		mVertexBuffer.put(vertices);// 向缓冲区中放入顶点坐标数据
		mVertexBuffer.position(0);// 设置缓冲区起始位置

		float[] texCoor = new float[12];// 初始化纹理坐标数据
		texCoor = new float[] { 0, 0, 0, 1, 1, 0, 1, 1, 1, 0, 0, 1 };
		ByteBuffer cbb = ByteBuffer.allocateDirect(texCoor.length * 4);// 创建顶点纹理坐标数据缓冲
		cbb.order(ByteOrder.nativeOrder());// 设置字节顺序
		mTexCoorBuffer = cbb.asFloatBuffer();// 转换为Float型缓冲
		mTexCoorBuffer.put(texCoor);// 向缓冲区中放入顶点着色数据
		mTexCoorBuffer.position(0);// 设置缓冲区起始位置
	}

	// 初始化着色器
	public void initShader() {
		// 获取程序中顶点位置属性引用id
		maPositionHandle = GLES30.glGetAttribLocation(programId, "aPosition");
		// 获取程序中顶点纹理坐标属性引用id
		maTexCoorHandle = GLES30.glGetAttribLocation(programId, "aTexCoor");
		// 获取程序中总变换矩阵引用id
		muMVPMatrixHandle = GLES30
				.glGetUniformLocation(programId, "uMVPMatrix");
	}

	// 绘制图形
	public void drawSelf() {
		// 关闭深度检测
		GLES30.glDisable(GLES30.GL_DEPTH_TEST);
		// 打开混合
		GLES30.glEnable(GLES30.GL_BLEND);
		// 设置混合因子
		GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA);
		// 指定使用某套shader程序
		GLES30.glUseProgram(programId);
		// 将最终变换矩阵传入渲染管线
		GLES30.glUniformMatrix4fv(muMVPMatrixHandle, 1, false,
				MatrixState2D.getFinalMatrix(), 0);
		// 将顶点坐标数据传入渲染管线
		GLES30.glVertexAttribPointer(maPositionHandle, 3, GLES30.GL_FLOAT,
				false, 3 * 4, mVertexBuffer);
		// 将顶点纹理坐标数据送入渲染管线
		GLES30.glVertexAttribPointer(maTexCoorHandle, 2, GLES30.GL_FLOAT,
				false, 2 * 4, mTexCoorBuffer);
		// 允许顶点位置数据数组
		GLES30.glEnableVertexAttribArray(maPositionHandle);
		GLES30.glEnableVertexAttribArray(maTexCoorHandle);

		// 绑定纹理
		GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
		GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, texId);

		// 绘制纹理矩形--条带法
		GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, vCount);
		// 关闭混合
		GLES30.glDisable(GLES30.GL_BLEND);
		// 打开深度检测
		GLES30.glEnable(GLES30.GL_DEPTH_TEST);
	}

	// 设置纹理编号
	public void setTexture(int texId) {
		this.texId = texId;
	}

	// ====================按钮的缩放方法 start===============
	// 设置三次缩放的缩放步长
	public void setScaleVaule(float one, float two, float three) {
		scaleFirstSpan = one;
		scaleSecondSpan = two;
		scaleThirdSpan = three;
	}

	public void scaleButton()// 缩放按钮的方法
	{
		if (scaleOverFlag)// 如果全部缩放结束
		{
			firstOver = false;// 第一轮缩放没有结束
			secondOver = false;// 第二轮缩放没有结束
			thirdOver = false;// 第三轮缩放没有结束
			scaleOverFlag = false;// 开始下一轮缩放
		}
		calCartoonGo();
	}

	// 执行三次缩放的动画
	public void calCartoonGo() {
		if (!firstOver) {//第一轮缩放未结束
			scaleTemp = scaleTemp - scaleFirstSpan;//计算缩放系数
			//缩放系数小于阈值则第二轮动画播放完毕
			if (scaleTemp <= scaleFirstEnd) {firstOver = true;}
			return;
		}
		if (!secondOver) {//第二轮缩放未结束
			scaleTemp = scaleTemp + scaleSecondSpan;//计算缩放系数
			//缩放系数大于阈值则第二轮动画播放完毕
			if (scaleTemp >= scaleSecondEnd) {secondOver = true;}
			return;
		}
		if (!thirdOver) {//第三轮缩放未结束
			scaleTemp = scaleTemp - scaleThirdSpan;//计算缩放系数
			if (scaleTemp <= scaleThirdEnd) {
				thirdOver = true;// 第三轮动画播放完毕
				scaleOverFlag = true;// 缩放完毕
			}
			return;
		}
	}
	// ====================按钮的缩放方法 end===============

}
