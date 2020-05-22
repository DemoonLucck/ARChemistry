package com.bn.ar.views;

import static com.bn.ar.utils.Constant.mScreenSave;
import static com.bn.ar.utils.Constant.path;
import static com.bn.ar.utils.Constant.ratio;
import static com.bn.ar.utils.Constant.viewportPosX;
import static com.bn.ar.utils.Constant.viewportPosY;
import static com.bn.ar.utils.Constant.viewportSizeX;
import static com.bn.ar.utils.Constant.viewportSizeY;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.MotionEvent;

import com.bn.ar.activity.StereoRendering;
import com.bn.ar.utils.Constant;
import com.bn.ar.utils.MatrixState;
import com.bn.ar.utils.MatrixState2D;
import com.bn.ar.utils.ScreenSave;
import com.bn.screenscale.ScreenScaleUtil;
import com.bn.thread.URLConnectionThread;
import com.qualcomm.vuforia.SampleApplicationSession;
import com.qualcomm.vuforia.Vuforia;

/*说明：
 * 1、连接网络，根据url获取txt字符串，并将其切分为String数组
 * 2、根据String[i*4+2]下载所有的类别纹理，并存储在PIC_LOAD_PATH文件下
 * 3、根据String[i*4+3]下载所有类别对应的zip包，并存储在ZIP_LOAD_PATH文件下
 * 4、将SD卡的ZIP_LOAD_PATH文件下的zip包解压到UNZIP_TO_PATH文件下
 * 5、从SD卡的PIC_LOAD_PATH文件下读取所有的纹理，并将其转化为bitmap对象
 * 6、从SD卡的UNZIP_TO_PATH文件下的指定文件夹下读取objname.txt和texturename.txt
 * 7、从SD卡的UNZIP_TO_PATH文件下的指定文件夹中读取obj模型 
 * 8、本应用中包含语音朗诵的功能，前提是手机上必须含有支持中文的语音包（可安装讯飞科技的语音引擎）
 * */

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class GlSurfaceView extends GLSurfaceView {
	public SceneRenderer mRenderer;//绘制界面渲染器对象
	public StereoRendering activity;//与Vufroia关联的activity
	public BNAbstractView currView;//当前界面
	public BNAbstractView mainView;//主界面
	public BNAbstractView menuView;//菜单界面
	public BNAbstractView loadView;//加载界面
	public BNAbstractView helpView;//帮助界面
	public BNAbstractView dataManagerView;//数据管理界面
	public BNAbstractView experienceView;//体验界面
	public URLConnectionThread urlThread;//执行下载和初始化任务的线程
	SampleApplicationSession vuforiaAppSession;//初始化Vufroia
	public com.qualcomm.vuforia.Renderer renderer;//AR渲染器

	public GlSurfaceView(Context context)// 此类的构造器
	{
		super(context);
		activity = (StereoRendering) context;// 初始化本类中的activity
		mScreenSave = new ScreenSave(GlSurfaceView.this);// 截屏使用的类
	}

	public void init(boolean translucent,
			SampleApplicationSession vuforiaAppSession) {
		if (translucent)// 是否为半透明
		{
			this.getHolder().setFormat(PixelFormat.TRANSLUCENT);
		}
		this.vuforiaAppSession = vuforiaAppSession;
		this.setEGLContextClientVersion(3);// 设置使用OPENGL ES3.0
		mRenderer = new SceneRenderer();// 创建渲染器对象
		this.setRenderer(mRenderer);// 设置渲染器
	}
	@Override
	public boolean onTouchEvent(MotionEvent e) {
		if (currView == null) {return false;}
		return currView.onTouchEvent(e);
	}

	public class SceneRenderer implements GLSurfaceView.Renderer {
		public boolean mIsActive = false;// 控制此类是否进行绘制的标志位
		@Override
		public void onDrawFrame(GL10 gl) {//绘制方法
			if (!mIsActive) {return;}//不进行任何绘制
			GLES30.glClear(GLES30.GL_DEPTH_BUFFER_BIT|GLES30.GL_COLOR_BUFFER_BIT);//清除深度缓存和颜色缓存
			if (currView != null) {currView.onDrawFrame();}//绘制当前界面
		}
		@Override
		public void onSurfaceChanged(GL10 gl, int width, int height) {
			initViewPort(width, height);//初始化视口
			MatrixState.setInitStack();//初始化矩阵类
			MatrixState.setLightLocation(0, -300, 350);//设置光源位置
			MatrixState2D.setInitStack(); // 初始化矩阵类
			MatrixState2D.setProjectOrtho(-ratio, ratio, -1, 1, 1, 100);//调用此方法计算产生平行投影矩阵
			MatrixState2D.setCamera(0, 0, 5, 0f, 0f, 0f, 0f, 1f, 0f);//调用此方法产生摄像机9参数位置矩阵
			MatrixState2D.setLightLocation(0, 50, 0);//设置光源位置
			if (currView == null){currView = menuView;}//将主菜单界面作为当前界面
		}
		@Override
		public void onSurfaceCreated(GL10 gl, EGLConfig config) {
			urlThread = new URLConnectionThread(path);// 建立初始化数据和下载线程
			urlThread.start();// 开启线程
			initRendering();
			menuView = new MenuView(GlSurfaceView.this);
			helpView = new HelpView(GlSurfaceView.this);
			mainView = new MainView(GlSurfaceView.this);
			loadView = new LoadView(GlSurfaceView.this);
			dataManagerView = new DataManagerView(GlSurfaceView.this);
			experienceView = new ExperienceView(GlSurfaceView.this);
			if (currView == null) {currView = menuView;}
		}
		public void initViewPort(int width, int height) {
			DisplayMetrics metrics = new DisplayMetrics();//新建度量对象
			activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);//计算activity的尺寸
			Constant.ssr = ScreenScaleUtil.calScale(metrics.widthPixels,metrics.heightPixels);//计算缩放比
			viewportPosX = 0;//调整视口的起始X坐标
			viewportPosY = 0;//调整视口的起始Y坐标
			viewportSizeX = width;//获得视口的宽度
			viewportSizeY = height;//获得视口的高度
			GLES30.glViewport(viewportPosX, viewportPosY, viewportSizeX,viewportSizeY);//调整视口大小
			vuforiaAppSession.onSurfaceChanged(width, height);//调整Vuforia的大小
		}

		// 初始化渲染器相关数据方法
		private void initRendering() {
			// 设置背景颜色
			GLES30.glClearColor(0.0f, 0.0f, 0.0f,
					Vuforia.requiresAlpha() ? 0.0f : 1.0f);
			// 开启背面检测
			GLES30.glEnable(GLES30.GL_CULL_FACE);
			// 开启设置卷绕方式
			GLES30.glCullFace(GLES30.GL_BACK);
			// 开启深度
			GLES30.glEnable(GLES30.GL_DEPTH_TEST);
			// 初始化AR渲染器
			renderer = com.qualcomm.vuforia.Renderer.getInstance();
			vuforiaAppSession.onSurfaceCreated();
		}

	}

}
