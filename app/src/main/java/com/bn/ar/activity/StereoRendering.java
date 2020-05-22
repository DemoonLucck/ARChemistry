package com.bn.ar.activity;

import java.util.ArrayList;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.bn.ar.manager.SoundManager;
import com.bn.ar.utils.Constant;
import com.bn.ar.views.GlSurfaceView;
import com.bn.screenscale.ScreenScaleUtil;
import com.qualcomm.vuforia.CameraDevice;
import com.qualcomm.vuforia.DataSet;
import com.qualcomm.vuforia.Eyewear;
import com.qualcomm.vuforia.ObjectTracker;
import com.qualcomm.vuforia.STORAGE_TYPE;
import com.qualcomm.vuforia.SampleApplicationControl;
import com.qualcomm.vuforia.SampleApplicationException;
import com.qualcomm.vuforia.SampleApplicationSession;
import com.qualcomm.vuforia.State;
import com.qualcomm.vuforia.Trackable;
import com.qualcomm.vuforia.Tracker;
import com.qualcomm.vuforia.TrackerManager;
import com.qualcomm.vuforia.Vuforia;

public class StereoRendering extends Activity implements
		SampleApplicationControl {
	private static final String LOGTAG = "StereoRendering"; // 后台调试信息
	private boolean isExit = false;// 是否退出
	private DataSet mCurrentDataset;// 数据集类
	private int mCurrentDatasetSelectionIndex = 0;// 当前使用的数据集索引
	private ArrayList<String> mDatasetStrings = new ArrayList<String>();// 存储从阿里云下载的xml文件路径
	private GlSurfaceView mGlView;// 绘制界面对象
	private boolean mPredictionEnabled = true;// 开启预测功能
	private RelativeLayout mUILayout;// 布局引用
	private AlertDialog mErrorDialog;// 弹出错误信息的类
	public SampleApplicationSession vuforiaAppSession;
	public static SoundManager sound;// 声音管理器
	public TextToSpeech mTTS;// 实现朗读文字功能的类
	private static final int REQ_TTS_STATUS_CHECK = 0;// 定义TTS的查询状态

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 检查TTS数据是否已经安装并且可用
		Intent checkIntent = new Intent();// 新建一个意图对象
		checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);// 设置其动作为查询TTS数据
//		startActivityForResult(checkIntent, REQ_TTS_STATUS_CHECK);// 发送请求
		mTTS = new TextToSpeech(this, null);// 新建实现文字朗读的类
		requestWindowFeature(Window.FEATURE_NO_TITLE);// 隐藏标题栏
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, // 设置为全屏
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		sound = new SoundManager(this);// 新建声音的管理类
		vuforiaAppSession = new SampleApplicationSession(this);// 新建初始化vuforia的类
		startLoadingAnimation(); // 开始加载欢迎画面
		mDatasetStrings.add("xml/ARLJ118.xml");// 在vuforia下载的xml数据包
		vuforiaAppSession.initAR(this, // 初始化AR的方法
				ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);// 横屏模式
		DisplayMetrics dm = new DisplayMetrics();// 新建度量对象
		getWindowManager().getDefaultDisplay().getMetrics(dm);// 存入当前显示尺寸信息
		Constant.ssr = ScreenScaleUtil
				.calScale(dm.widthPixels, dm.heightPixels);// 计算屏幕自适应方面的数据
	}

	// Called when the activity will start interacting with the user.
	@Override
	protected void onResume() {
		super.onResume();
		try {
			vuforiaAppSession.resumeAR();// ***************************
		} catch (SampleApplicationException e) {
			Log.e(LOGTAG, e.getString());
		}
		if (mGlView != null) {
			mGlView.setVisibility(View.VISIBLE);
			mGlView.onResume();
		}
	}

	// Callback for configuration changes the activity handles itself
	// 重写onConfigurationChanged方法可判断当前屏幕朝向，监听屏幕变化以此做出响应。
	@Override
	public void onConfigurationChanged(Configuration config) {
		Log.d(LOGTAG, "onConfigurationChanged");
		super.onConfigurationChanged(config);

		vuforiaAppSession.onConfigurationChanged();// ********************
		System.out.println("onConfigurationChanged");
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mTTS != null) {
			mTTS.stop();
		}
		if (mGlView != null) {
			mGlView.setVisibility(View.INVISIBLE);// 将绘制界面设置成不可见
			mGlView.onPause();
		}
		try {
			vuforiaAppSession.pauseAR();// ****************
		} catch (SampleApplicationException e) {
			Log.e(LOGTAG, e.getString());
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mTTS != null) {
			mTTS.shutdown();// 停止播放讲解声音
		}

		try {
			vuforiaAppSession.stopAR();// *********************
		} catch (SampleApplicationException e) {
			Log.e(LOGTAG, e.getString());
		}

	}

	// Initializes AR application components.
	// 初始化AR组件
	private void initApplicationAR() {
		// Create OpenGL ES view:
		boolean translucent = Vuforia.requiresAlpha();// 是否为半透明
		// 创建绘制界面类对象
		mGlView = new GlSurfaceView(this);
		mGlView.init(translucent, vuforiaAppSession);// ****************
	}

	private void startLoadingAnimation() {
		mUILayout = (RelativeLayout) View.inflate(this,
				R.layout.camera_overlay, null);//加载一个布局文件
		mUILayout.setVisibility(View.VISIBLE);//设置布局可见
		mUILayout.setBackgroundResource(R.drawable.splash);//设置布局背景
		addContentView(mUILayout, new LayoutParams//添加此布局
				(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));
	}

	// Methods to load and destroy tracking data.
	// 读取追踪器数据的方法
	@Override
	public boolean doLoadTrackersData() {
		TrackerManager tManager = TrackerManager.getInstance();//得到追踪器的管理者
		ObjectTracker objectTracker = (ObjectTracker) tManager//得到一个追踪器的对象
					.getTracker(ObjectTracker.getClassType());
		if (objectTracker == null){return false;}//如果追踪器为空
		if (mCurrentDataset == null){//如果当前数据集为空
			mCurrentDataset = objectTracker.createDataSet();//创建数据集
		}
		if (mCurrentDataset == null){return false;}//数据集创建失败
		if (!mCurrentDataset.load(//读取数据失败
				mDatasetStrings.get(mCurrentDatasetSelectionIndex),
				STORAGE_TYPE.STORAGE_APPRESOURCE))
			return false;
		if (!objectTracker.activateDataSet(mCurrentDataset))//数据集未激活
			return false;
		int numTrackables = mCurrentDataset.getNumTrackables();//得到追踪器的个数
		for (int count = 0; count < numTrackables; count++) {
			Trackable trackable = mCurrentDataset.getTrackable(count);
			String name = "Current Dataset : " + trackable.getName();
			trackable.setUserData(name);
			Log.d(LOGTAG, "UserData:Set the following user data "
					+ (String) trackable.getUserData());
		}
		return true;
	}

	// 清除追踪器数据的方法
	@Override
	public boolean doUnloadTrackersData() {
		boolean result = true;// 表示的追踪器数据是否成功清除的标志位

		TrackerManager tManager = TrackerManager.getInstance();// 获得一个追踪器的管理者
		ObjectTracker objectTracker = (ObjectTracker) tManager
				.getTracker(ObjectTracker.getClassType());
		if (objectTracker == null)
			return false;

		if (mCurrentDataset != null && mCurrentDataset.isActive()) {
			if (objectTracker.getActiveDataSet().equals(mCurrentDataset)
					&& !objectTracker.deactivateDataSet(mCurrentDataset))// 停用追踪器数据失败
			{
				result = false;
			} else if (!objectTracker.destroyDataSet(mCurrentDataset))// 销毁追踪器数据失败
			{
				result = false;
			}

			mCurrentDataset = null;
		}

		return result;
	}

	@Override
	public void onInitARDone(SampleApplicationException exception) {
		if (exception == null) {//如果没有异常发生
			initApplicationAR();//初始化程序中的AR部分
			mGlView.mRenderer.mIsActive = true;//开始渲染
			addContentView(mGlView, new LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.MATCH_PARENT));// 添加一个绘制界面
			mUILayout.bringToFront();//设置布局在相机之前
			new Handler().postDelayed(new Runnable()//过3.5秒后将布局设为半透明
					{
						@Override
						public void run() {
							mUILayout.setBackgroundColor(Color.TRANSPARENT);//将布局设置为半透明
						}
					}, 3500);
			try {
				vuforiaAppSession.startAR(CameraDevice.CAMERA.CAMERA_DEFAULT);//启动AR
			} catch (SampleApplicationException e) {//如果发生异常
				Log.e(LOGTAG, e.getString());//在后台显示错误信息
			}
			boolean result = CameraDevice.getInstance().setFocusMode(//查看相机是否成功开启自动连续对焦
					CameraDevice.FOCUS_MODE.FOCUS_MODE_CONTINUOUSAUTO);
			if (!result){//如果开启失败
				Log.e(LOGTAG, "Unable to enable continuous autofocus");//在后台显示错误信息
			}
		} else {
			Log.e(LOGTAG, exception.getString());//显示异常信息
			showInitializationErrorMessage(exception.getString());//中文的错误信息显示在屏幕上
		}
	}

	// Shows initialization error messages as System dialogs
	// 用来显示初始化时的错误信息的方法
	public void showInitializationErrorMessage(String message) {
		final String errorMessage = message;
		runOnUiThread(new Runnable() {
			public void run() {
				if (mErrorDialog != null) {
					mErrorDialog.dismiss();
				}
				// Generates an Alert Dialog to show the error message
				AlertDialog.Builder builder = new AlertDialog.Builder(
						StereoRendering.this);
				builder.setMessage(errorMessage)
						.setTitle(getString(R.string.INIT_ERROR))
						.setCancelable(false)
						.setIcon(0)
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										finish();
									}
								});

				mErrorDialog = builder.create();
				mErrorDialog.show();
			}
		});
	}

	@Override
	public void onQCARUpdate(State state) {
	}

	@Override
	public boolean doInitTrackers() {
		// Indicate if the trackers were initialized correctly
		// 用来表示追踪器是否初始化成功
		boolean result = true;

		TrackerManager tManager = TrackerManager.getInstance();
		Tracker tracker;

		// Trying to initialize the image tracker
		// 试着去初始化图片追踪器
		tracker = tManager.initTracker(ObjectTracker.getClassType());
		if (tracker == null) {
			Log.e(LOGTAG,
					"Tracker not initialized. Tracker already initialized or the camera is already started");
			result = false;
		} else {
			Log.i(LOGTAG, "Tracker successfully initialized");
		}
		System.out.println("doInitTrackers");
		return result;
	}

	@Override
	public boolean doStartTrackers() {
		// Indicate if the trackers were started correctly
		// 表示追踪器是否成功启动
		boolean result = true;
		Tracker objectTracker = TrackerManager.getInstance().getTracker(
				ObjectTracker.getClassType());
		if (objectTracker != null)
			objectTracker.start();
		return result;
	}

	@Override
	public boolean doStopTrackers() {
		// Indicate if the trackers were stopped correctly
		// 表示追踪器是否被成功停止
		boolean result = true;

		Tracker objectTracker = TrackerManager.getInstance().getTracker(
				ObjectTracker.getClassType());
		if (objectTracker != null)
			objectTracker.stop();

		return result;
	}

	@Override
	public boolean doDeinitTrackers() {
		// Indicate if the trackers were deinitialized correctly
		// 用来表示追踪器是否反初始化成功
		boolean result = true;

		TrackerManager tManager = TrackerManager.getInstance();
		tManager.deinitTracker(ObjectTracker.getClassType());

		return result;
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			System.out.println("keyCode:" + keyCode);
			// Toggle prediction
			// NOTE: You would typically want to check the result
			// of setPredictiveTracking to see if it was successful
			Eyewear.getInstance().setPredictiveTracking(!mPredictionEnabled);
			mPredictionEnabled = !mPredictionEnabled;
			return true;
		}

		return super.onKeyUp(keyCode, event);
	}

	@SuppressLint("HandlerLeak")
	public Handler myHandler = new Handler() {// 用来更新UI线程中的控件
		public void handleMessage(Message msg) {
			Bundle bundle = msg.getData();
			int view = bundle.getInt("operation");
			switch (view) {
			case Constant.GO_TOAST_SUCCESS:
				Toast.makeText(StereoRendering.this,
						"截图成功！图片路径:" + Constant.screenSavePath,
						Toast.LENGTH_SHORT).show();
				break;
			case Constant.GO_TOAST_FAIL:
				Toast.makeText(StereoRendering.this, "截图失败！",
						Toast.LENGTH_SHORT).show();
				break;
			case Constant.NOT_EXIST:
				Toast.makeText(StereoRendering.this, "不存在该文件，请到数据包管理中下载!",
						Toast.LENGTH_SHORT).show();
				break;
			case Constant.EXIST:
				Toast.makeText(StereoRendering.this, "该文件已下载，无需重新下载！",
						Toast.LENGTH_SHORT).show();
				break;
			case Constant.CONNECT_NET:
				Toast.makeText(StereoRendering.this, "当前网络状况不佳，请重新连接！",
						Toast.LENGTH_SHORT).show();
				break;
			case Constant.NOT_CONTENT_FAIL:
				Toast.makeText(StereoRendering.this, "当前无介绍内容！",
						Toast.LENGTH_SHORT).show();
				break;
			case Constant.SPEAKING:
				Toast.makeText(StereoRendering.this, "当前正在播放！",
						Toast.LENGTH_SHORT).show();
				break;
			case Constant.LOADING:
				Toast.makeText(StereoRendering.this, "当前正在下载中！",
						Toast.LENGTH_SHORT).show();
				break;
			}
		}
	};

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (mGlView == null) {
			return false;
		}
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (mGlView.currView == mGlView.mainView) {
				mGlView.currView = mGlView.menuView;
			} else if (mGlView.currView == mGlView.helpView)// 从帮助界面返回到菜单界面
			{
				mGlView.currView = mGlView.menuView;
			} else if (mGlView.currView == mGlView.dataManagerView)// 从数据包管理界面到菜单界面
			{
				mGlView.currView = mGlView.menuView;
			} else if (mGlView.currView == mGlView.experienceView)// 从体验界面到菜单界面
			{
				mGlView.currView = mGlView.menuView;
			} else if (mGlView.currView == mGlView.menuView)// 只有处于菜单界面时才可以按返回键返回桌面
			{
				exit();
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void exit() {
		if (isExit == false) {
			isExit = true; // 准备退出
			Toast.makeText(StereoRendering.this, "再按一次退出!", Toast.LENGTH_SHORT)
					.show();// 显示退出提示
			new Handler().postDelayed(new Runnable()// 过2.5秒后将退出的标志置为false
					{
						@Override
						public void run() {
							isExit = false;
						}
					}, 2500);
		} else {
			android.os.Process.killProcess(android.os.Process.myPid()); // 结束此进程
		}
	}

}
