package com.qualcomm.vuforia;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import com.bn.ar.activity.R;
import com.qualcomm.vuforia.CameraCalibration;
import com.qualcomm.vuforia.CameraDevice;
import com.qualcomm.vuforia.Matrix44F;
import com.qualcomm.vuforia.Eyewear;
import com.qualcomm.vuforia.Renderer;
import com.qualcomm.vuforia.State;
import com.qualcomm.vuforia.Tool;
import com.qualcomm.vuforia.Vec2I;
import com.qualcomm.vuforia.VideoBackgroundConfig;
import com.qualcomm.vuforia.VideoMode;
import com.qualcomm.vuforia.Vuforia;
import com.qualcomm.vuforia.Vuforia.UpdateCallbackInterface;

//类中主要包括对sdk和相机的初始化配置操作
public class SampleApplicationSession implements UpdateCallbackInterface {
	private static final String LOGTAG = "Vuforia_Sample";//显示信息
	private Activity mActivity;//对当前activity的引用
	private SampleApplicationControl mSessionControl;//vuforia的控制接口
	private boolean mStarted = false;//AR是否在运行
	private boolean mCameraRunning = false;//照相机是否正在运行
	private int mScreenWidth = 0;//用来存储当前设备的屏幕宽度
	private int mScreenHeight = 0;//用来存储当前设备的屏幕高度
	private InitVuforiaTask mInitVuforiaTask;// 初始化Vuforia的异步任务
	private LoadTrackerTask mLoadTrackerTask;// 加载追踪器的异步任务
	// 一个用来同步Vuforia初始化、数据加载、销毁事件的对象。当数据正在被读取的时候程序被销毁，加载操作
	// 会执行完后再停止Vuforia
	private Object mShutdownLock = new Object();//操作过程中被加锁的对象
	private int mVuforiaFlags = 0;// 用来表示使用openGL的版本
	private int mCamera = CameraDevice.CAMERA.CAMERA_DEFAULT;//表示照相机的默认模式
	private Matrix44F mProjectionMatrix;//存储用于渲染目的的投影矩阵
	private boolean mIsPortrait = false;// 屏幕是否处在竖屏模式
	public SampleApplicationSession(SampleApplicationControl sessionControl) {//此类的构造器
		mSessionControl = sessionControl;
	}
	
	public void initAR(Activity activity, int screenOrientation) { //初始化AR的方法
		SampleApplicationException vuforiaException = null;//声明一个异常类的引用
		mActivity = activity;//将本类中的引用指向当前的activity对象
		if ((screenOrientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR)//如果屏幕显示模式为物理感应器决定显示方向
				&& (Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO))//并且版本号高于2.2
			screenOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR;//表示根据重力变换朝向
		mActivity.setRequestedOrientation(screenOrientation);// 设置屏幕模式
		updateActivityOrientation();// 更新vuforia所在activity的显示模式
		storeScreenDimensions();// 查询并记录屏幕尺寸
		mActivity.getWindow().setFlags(							// 只要这个窗口是可见，设置窗体始终点亮
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		mVuforiaFlags = Vuforia.GL_20;// 使用OPENGL2.0版本
		// 异步初始化Vuforia SDK ，避免主线程发生阻塞
		// 注意：该任务必须在UI线程被创建和调用，且只能被执行一次
		if (mInitVuforiaTask != null) {//如果已经初始化了Vuforia SDK
			String logMessage = "Cannot initialize SDK twice";//增加异常描述
			vuforiaException = new SampleApplicationException(//新建异常类
					SampleApplicationException.VUFORIA_ALREADY_INITIALIZATED,
					logMessage);
			Log.e(LOGTAG, logMessage);//显示异常信息
		}

		if (vuforiaException == null) {//如果该过程中没有发生异常
			try {
				mInitVuforiaTask = new InitVuforiaTask();//新建初始化Vuforia的异步任务
				mInitVuforiaTask.execute();//执行初始化Vuforia的异步任务
			} catch (Exception e) {//初始化过程中存在异常
				String logMessage = "Initializing Vuforia SDK failed";//错误描述
				vuforiaException = new SampleApplicationException(//新建异常类
						SampleApplicationException.INITIALIZATION_FAILURE,
						logMessage);
				Log.e(LOGTAG, logMessage);//显示异常信息
			}
		}

		if (vuforiaException != null)
			mSessionControl.onInitARDone(vuforiaException);
	}

	public void startAR(int camera) throws SampleApplicationException {//启动Vuforia，相机和追踪器
		String error;//异常描述信息
		if (mCameraRunning) //如果相机正在运行中
		{
			error = "Camera already running, unable to open again";////异常描述为重复开启相机
			Log.e(LOGTAG, error);//将异常信息显示在屏幕上
			throw new SampleApplicationException(//抛出异常信息
					SampleApplicationException.CAMERA_INITIALIZATION_FAILURE,
					error);
		}
		mCamera = camera;//将相机编号传入成员变量中
		if (!CameraDevice.getInstance().init(camera))//如果初始化相机失败
		{
			error = "Unable to open camera device: " + camera;//异常描述为无法加载驱动
			Log.e(LOGTAG, error);//将异常信息显示在屏幕上
			throw new SampleApplicationException(//抛出异常信息
					SampleApplicationException.CAMERA_INITIALIZATION_FAILURE,
					error);
		}
		if (!CameraDevice.getInstance().selectVideoMode(
				CameraDevice.MODE.MODE_DEFAULT))//设置照相机为默认模式失败
		{
			error = "Unable to set video mode";//异常描述为设置相机模式失败
			Log.e(LOGTAG, error);//将异常信息显示在屏幕上
			throw new SampleApplicationException(//抛出异常信息
					SampleApplicationException.CAMERA_INITIALIZATION_FAILURE,
					error);
		}
		configureVideoBackground();//背景渲染的配置方法
		configureRenderingFrameRate();//控制渲染的帧速率
		if (!CameraDevice.getInstance().start()) {//如果相机启动失败
			error = "Unable to start camera device: " + camera;//异常描述为启动相机失败
			Log.e(LOGTAG, error);//将异常信息显示在屏幕上
			throw new SampleApplicationException(//抛出异常信息
					SampleApplicationException.CAMERA_INITIALIZATION_FAILURE,
					error);
		}
		setProjectionMatrix();//更新投影矩阵
		mSessionControl.doStartTrackers();//开启跟踪器
		mCameraRunning = true;//表示现在相机正在运行
		if (!CameraDevice.getInstance().setFocusMode(
				CameraDevice.FOCUS_MODE.FOCUS_MODE_CONTINUOUSAUTO)) {//将相机设置为连续自动对焦
			if (!CameraDevice.getInstance().setFocusMode(
					CameraDevice.FOCUS_MODE.FOCUS_MODE_TRIGGERAUTO)) {//设置此对焦模式将触发自动对焦操作
				CameraDevice.getInstance().setFocusMode(
						CameraDevice.FOCUS_MODE.FOCUS_MODE_NORMAL);//将相机设置为默认的对焦模式
			}
		}
	}

	public void stopAR() throws SampleApplicationException {//终止当前加载任务，停止AR
		// Cancel potentially running tasks
		if (mInitVuforiaTask != null
				&& mInitVuforiaTask.getStatus() != InitVuforiaTask.Status.FINISHED)// 如果正在初始化vuforia
		{
			mInitVuforiaTask.cancel(true);// 取消当前的初始化任务
			mInitVuforiaTask = null;// 将初始化任务置空
		}

		if (mLoadTrackerTask != null
				&& mLoadTrackerTask.getStatus() != LoadTrackerTask.Status.FINISHED)// 如果正在加载追踪器
		{
			mLoadTrackerTask.cancel(true);// 取消当前的加载任务
			mLoadTrackerTask = null;// 将加载任务置空
		}

		mInitVuforiaTask = null;
		mLoadTrackerTask = null;

		mStarted = false;

		stopCamera();// 停止照相机

		// Ensure that all asynchronous operations to initialize Vuforia
		// and loading the tracker datasets do not overlap:
		synchronized (mShutdownLock) {

			boolean unloadTrackersResult;
			boolean deinitTrackersResult;

			// Destroy the tracking data set:
			unloadTrackersResult = mSessionControl.doUnloadTrackersData();

			// Deinitialize the trackers:
			deinitTrackersResult = mSessionControl.doDeinitTrackers();

			// Deinitialize Vuforia SDK:
			Vuforia.deinit();

			if (!unloadTrackersResult)
				throw new SampleApplicationException(
						SampleApplicationException.UNLOADING_TRACKERS_FAILURE,
						"Failed to unload trackers\' data");

			if (!deinitTrackersResult)
				throw new SampleApplicationException(
						SampleApplicationException.TRACKERS_DEINITIALIZATION_FAILURE,
						"Failed to deinitialize trackers");

		}
	}
	
	public void resumeAR() throws SampleApplicationException {//重新启动相机和AR的方法
		Vuforia.onResume();//重新启动Vuforia
		if (mStarted) {//如果Vuforia正在运行
			startAR(mCamera);//以某一方式重启相机
		}
	}

	public void pauseAR() throws SampleApplicationException {//暂停AR,停止相机的方法
		if (mStarted) {
			stopCamera();
		}

		Vuforia.onPause();
	}

	public Matrix44F getProjectionMatrix() {// 获取用于渲染的投影矩阵
		return mProjectionMatrix;
	}
	@Override
	public void QCAR_onUpdate(State s) {
		mSessionControl.onQCARUpdate(s);
	}
	public void onConfigurationChanged() {//布局改变时进行管理的方法
		updateActivityOrientation();//更新Activity的横屏竖屏设置
		storeScreenDimensions();//保存屏幕尺寸
		if (isARRunning()) {//判断AR是否正在运行
			configureVideoBackground();//配置视频背景
			setProjectionMatrix();//更新投影矩阵
		}
	}

	// Methods to be called to handle lifecycle
	public void onResume() {
		Vuforia.onResume();
	}

	public void onPause() {
		Vuforia.onPause();
	}

	public void onSurfaceChanged(int width, int height) {
		Vuforia.onSurfaceChanged(width, height);
	}

	public void onSurfaceCreated() {
		Vuforia.onSurfaceCreated();
	}

	private class InitVuforiaTask extends AsyncTask<Void, Integer, Boolean> {//初始化Vuforia的异步任务
		private int mProgressValue = -1;// 初始化加载进度
		protected Boolean doInBackground(Void... params) {//在后台执行的任务
			synchronized (mShutdownLock) {// 在加载的过程中避免了调用onDestroy()方法
				//Vuforia授予开发者的键值
				String keyTemp = "AVO5svD/////AAAAAer2jxDDu0Ylv7wLg+tjKPE/xZKTf9R6GjbS/DldnAKrNDP0dvt7YNi9FP2fjwPAkLBVgNvr+IqKdGmlsRFDXsm98l4/NDKGZZBCjT23vhsZhb3psuWTvE4K9JFzuFlsI7mpdTBLxa6W8qU317wCplDMjvD1imSCoQ1/87YR7jUCizgPLxj6hBX792/ntdKD4kzQeGGgTjo4cR0SQNkgg8ydmABqYqNkzMwKaBN+uXJXEbNLs0BjIzIEqakky45YvSLygENfZdpDiimreBYoKmVzNmUk6RHZv9EKAiffaLv6YH9ffR0W77misc+b8sHyOJF4qJejWp74BrA0aH/0RPgnxav7mg2xoS/IxVqBQzqH";
				Vuforia.setInitParameters(mActivity, mVuforiaFlags, keyTemp);//设置Vuforia初始化时的参数
				do {
					//在初始化过程中，只有在本步骤全部完成时，Vuforia.init()才不会阻塞。然后它会开始下一个步骤并返回加载过程完成的百分比。假如Vuforia.init()返回-1，表明初始化过程中发生错误。
					mProgressValue = Vuforia.init();	
					publishProgress(mProgressValue);//发送加载的进度值
				} while (!isCancelled() && mProgressValue >= 0//如果此过程没有被取消且加载并没有完全完成就继续执行
						&& mProgressValue < 100);

				return (mProgressValue > 0);
			}
		}

		protected void onProgressUpdate(Integer... values) {
			// Do something with the progress value "values[0]", e.g. update
			// splash screen, progress bar, etc.
		}
		//初始化Vuforia，根据状态判断是否进入下一初始化步骤
		protected void onPostExecute(Boolean result) {
			SampleApplicationException vuforiaException = null;//初始化异常信息
			if (result) {//如果此步骤初始化成功
				Log.d(LOGTAG, "InitVuforiaTask.onPostExecute: Vuforia "
						+ "initialization successful");//显示成功信息
				boolean initTrackersResult;//声明一个判断是否成功初始化追踪器的标志位
				initTrackersResult = mSessionControl.doInitTrackers();//得到初始化结果
				if (initTrackersResult) {//如果初始化追踪器成功
					try {
						mLoadTrackerTask = new LoadTrackerTask();//新建读取追踪器的异步任务
						mLoadTrackerTask.execute();//执行读取追踪器的异步任务
					} catch (Exception e) {
						String logMessage = "Loading tracking data set failed";//异常描述为读取追踪器信息失败
						vuforiaException = new SampleApplicationException(//抛出异常信息
								SampleApplicationException.LOADING_TRACKERS_FAILURE,
								logMessage);
						Log.e(LOGTAG, logMessage);//将错误信息显示在屏幕上
						mSessionControl.onInitARDone(vuforiaException);//调用此方法停止加载加载过程
					}
				} else {//如果初始化追踪器失败
					vuforiaException = new SampleApplicationException(//抛出异常信息
							SampleApplicationException.TRACKERS_INITIALIZATION_FAILURE,
							"Failed to initialize trackers");
					mSessionControl.onInitARDone(vuforiaException);//调用此方法停止加载加载过程
				}
			} else {//上一个初始化步骤执行失败
				String logMessage;//用来存储错误信息的字符串
				//检查是否是因为设备的驱动不支持造成初始化失败
				logMessage = getInitializationErrorString(mProgressValue);//得到错误信息
				Log.e(LOGTAG, "InitVuforiaTask.onPostExecute: " + logMessage
						+ " Exiting.");//将错误信息显示在屏幕上
				vuforiaException = new SampleApplicationException(//抛出错误信息
						SampleApplicationException.INITIALIZATION_FAILURE,
						logMessage);
				mSessionControl.onInitARDone(vuforiaException);//调用此方法停止初始化
			}
		}
	}

	// An async task to load the tracker data asynchronously.
	private class LoadTrackerTask extends AsyncTask<Void, Integer, Boolean> {
		protected Boolean doInBackground(Void... params) {
			synchronized (mShutdownLock) {//避免和onDestroy()同时进行
				return mSessionControl.doLoadTrackersData();//读取追踪器的数据
			}
		}

		protected void onPostExecute(Boolean result) {
			SampleApplicationException vuforiaException = null;
			Log.d(LOGTAG, "LoadTrackerTask.onPostExecute: execution "
					+ (result ? "successful" : "failed"));
			if (!result) {
				String logMessage = "Failed to load tracker data.";//读取数据集发生错误
				Log.e(LOGTAG, logMessage);//将错误信息显示在屏幕上
				vuforiaException = new SampleApplicationException(
						SampleApplicationException.LOADING_TRACKERS_FAILURE,
						logMessage);
			} else {
				System.gc();//使用java的垃圾回收
				Vuforia.registerCallback(SampleApplicationSession.this);//注册回调的对象
				mStarted = true;//AR正在运行
			}

			// Done loading the tracker, update application status, send the
			// exception to check errors
			mSessionControl.onInitARDone(vuforiaException);
		}
	}

	// Returns the error message for each error code
	private String getInitializationErrorString(int code) {
		if (code == Vuforia.INIT_DEVICE_NOT_SUPPORTED)
			return mActivity
					.getString(R.string.INIT_ERROR_DEVICE_NOT_SUPPORTED);
		if (code == Vuforia.INIT_NO_CAMERA_ACCESS)
			return mActivity.getString(R.string.INIT_ERROR_NO_CAMERA_ACCESS);
		if (code == Vuforia.INIT_LICENSE_ERROR_MISSING_KEY)
			return mActivity.getString(R.string.INIT_LICENSE_ERROR_MISSING_KEY);
		if (code == Vuforia.INIT_LICENSE_ERROR_INVALID_KEY)
			return mActivity.getString(R.string.INIT_LICENSE_ERROR_INVALID_KEY);
		if (code == Vuforia.INIT_LICENSE_ERROR_NO_NETWORK_TRANSIENT)
			return mActivity
					.getString(R.string.INIT_LICENSE_ERROR_NO_NETWORK_TRANSIENT);
		if (code == Vuforia.INIT_LICENSE_ERROR_NO_NETWORK_PERMANENT)
			return mActivity
					.getString(R.string.INIT_LICENSE_ERROR_NO_NETWORK_PERMANENT);
		if (code == Vuforia.INIT_LICENSE_ERROR_CANCELED_KEY)
			return mActivity
					.getString(R.string.INIT_LICENSE_ERROR_CANCELED_KEY);
		if (code == Vuforia.INIT_LICENSE_ERROR_PRODUCT_TYPE_MISMATCH)
			return mActivity
					.getString(R.string.INIT_LICENSE_ERROR_PRODUCT_TYPE_MISMATCH);
		else {
			return mActivity
					.getString(R.string.INIT_LICENSE_ERROR_UNKNOWN_ERROR);
		}
	}

	
	private void storeScreenDimensions() {//保存屏幕尺寸的方法
		DisplayMetrics metrics = new DisplayMetrics();//得到显示的度量对象
		//获得当前activity的尺寸并将其存入上述对象中
		mActivity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		mScreenWidth = metrics.widthPixels;//保存activity的宽度
		mScreenHeight = metrics.heightPixels;//保存activity的高度
	}
	private void updateActivityOrientation() {// 更新activity的屏幕显示模式
		//得到当前activity的界面配置信息
		Configuration config = mActivity.getResources().getConfiguration();
		switch (config.orientation) {//界面的显示模式
		case Configuration.ORIENTATION_PORTRAIT://如果界面为竖屏模式
			mIsPortrait = true;//将竖屏的标志位置为真
			break;
		case Configuration.ORIENTATION_LANDSCAPE://如果界面为横屏模式
			mIsPortrait = false;//将竖屏的标志位置为真
			break;
		case Configuration.ORIENTATION_UNDEFINED://如果界面为未定义模式
		default:
			break;
		}
	}

	// Method for setting / updating the projection matrix for AR content
	// rendering
	public void setProjectionMatrix() {
		CameraCalibration camCal = CameraDevice.getInstance()
				.getCameraCalibration();
		mProjectionMatrix = Tool.getProjectionGL(camCal, 10.0f, 5000.0f);
	}

	public void stopCamera() {
		if (mCameraRunning) {//如果相机正在运行中
			mSessionControl.doStopTrackers();//停止追踪器
			CameraDevice.getInstance().stop();//停止相机
			CameraDevice.getInstance().deinit();//反初始化相机
			mCameraRunning = false;//表示相机停止运行
		}
	}

	// Configures the video mode and sets offsets for the camera's image
	private void configureVideoBackground() {
		CameraDevice cameraDevice = CameraDevice.getInstance();
		VideoMode vm = cameraDevice
				.getVideoMode(CameraDevice.MODE.MODE_DEFAULT);

		VideoBackgroundConfig config = new VideoBackgroundConfig();

		// see-through devices do not draw the video background
		if (Eyewear.getInstance().isDeviceDetected()
				&& Eyewear.getInstance().isSeeThru()) {
			config.setEnabled(false);

		} else {
			config.setEnabled(true);
		}

		config.setPosition(new Vec2I(0, 0));

		int xSize = 0, ySize = 0;
		if (mIsPortrait) {
			xSize = (int) (vm.getHeight() * (mScreenHeight / (float) vm
					.getWidth()));
			ySize = mScreenHeight;

			if (xSize < mScreenWidth) {
				xSize = mScreenWidth;
				ySize = (int) (mScreenWidth * (vm.getWidth() / (float) vm
						.getHeight()));
			}
		} else {
			xSize = mScreenWidth;
			ySize = (int) (vm.getHeight() * (mScreenWidth / (float) vm
					.getWidth()));

			if (ySize < mScreenHeight) {
				xSize = (int) (mScreenHeight * (vm.getWidth() / (float) vm
						.getHeight()));
				ySize = mScreenHeight;
			}
		}

		config.setSize(new Vec2I(xSize, ySize));

		Log.i(LOGTAG, "Configure Video Background : Video (" + vm.getWidth()
				+ " , " + vm.getHeight() + "), Screen (" + mScreenWidth + " , "
				+ mScreenHeight + "), mSize (" + xSize + " , " + ySize + ")");

		Renderer.getInstance().setVideoBackgroundConfig(config);
	}

	private boolean configureRenderingFrameRate() {
		// In this example we selected the default preset hint for best Mobile
		// AR Experience
		// See website documentation for more information on the rendering hint
		// modes
		// relevant to your AR experience.
		int myRenderingOptions = Renderer.FPSHINT_FLAGS.FPSHINT_DEFAULT_FLAGS;

		// Optical see-through devices don't render video background
		if (Eyewear.getInstance().isDeviceDetected()
				&& Eyewear.getInstance().isSeeThru()) {
			myRenderingOptions = Renderer.FPSHINT_FLAGS.FPSHINT_NO_VIDEOBACKGROUND;
		}

		// Retrieve recommended rendering frame rate best on currently
		// configured/enabled vuforia features
		// and selected application hint
		int vuforiaRecommendedFPS = Renderer.getInstance().getRecommendedFps(
				myRenderingOptions);

		// Use the recommended fps value computed by QCAR
		if (!Renderer.getInstance().setTargetFps(vuforiaRecommendedFPS)) {
			Log.e(LOGTAG, "Failed to set rendering frame rate to: "
					+ vuforiaRecommendedFPS + " fps");
			return false;
		} else {
			Log.i(LOGTAG,
					"Configured frame rate set to recommended frame rate: "
							+ vuforiaRecommendedFPS + " fps");
		}
		return true;
	}

	// Returns true if Vuforia is initialized, the trackers started and the
	// tracker data loaded
	private boolean isARRunning() {
		return mStarted;
	}

}
