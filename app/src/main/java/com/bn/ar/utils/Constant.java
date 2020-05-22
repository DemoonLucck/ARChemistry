package com.bn.ar.utils;
import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Message;

import com.bn.ar.activity.StereoRendering;
import com.bn.ar.draw.LoadedObjectVertexNormalTexture;
import com.bn.screenscale.ScreenScaleResult;
import com.qualcomm.vuforia.Matrix44F;

@SuppressLint("SdCardPath")
public class Constant {	
	@SuppressLint("SdCardPath")
	public static final String screenSavePath = "/sdcard/Chemistry/screenshot/";//截屏图片的存储路径
	@SuppressLint("SdCardPath")
	public static final String ZIP_LOAD_PATH = "/sdcard/Chemistry/zip_download/";//下载的zip包的存储路径
	@SuppressLint("SdCardPath")
	public static final String YELLOW_PIC_LOAD_PATH = "/sdcard/Chemistry/pic_download/yellow/";//下载的pic的存储路径
	public static final String GRAY_PIC_LOAD_PATH = "/sdcard/Chemistry/pic_download/gray/";//下载的pic的存储路径
	@SuppressLint("SdCardPath")
	public static final String UNZIP_TO_PATH = "/sdcard/Chemistry/unzip/";//zip包解压后的存放路径
	public static final String path="http://192.168.1.103:8080/huaxue/package_list.txt";//"package_list.txt"文件的下载路径
	public static int typeNumber=2;//类别数量
	public static int currentNumber=2;
	public static int ZIP_FLAG = 1;
	public static int PIC_FLAG = 2;
	public static int TXT_FLAG = 3;
	
	public static boolean[] selectDataIndex;//代表选中了第几个数据
	public static int selectData = 0;//默认选中的是第0项
	public static boolean[] downLoadZipChangeT;
	public static boolean[] downLoadZipFinish;//下载的文件是否解压缩完成
	public static boolean[] isSourceInit;//下载的文件是否解压缩完成
	
	public static boolean saveFlag=false;//是否照相
	public static  boolean soundOn = true;
	public static boolean net_success = true;//联网是否成功
	
	public static int viewportPosX;//屏幕x
	public static int viewportPosY;//屏幕y
	public static int viewportSizeX;//屏幕大小x
	public static int viewportSizeY;//屏幕大小x
	public final static int GO_TOAST_SUCCESS = 20;//截图成功
	public final static int GO_TOAST_FAIL = 21;//截图失败
    public final static int BUTTON_PRESS = 22;//点击按钮
    public final static int NOT_EXIST = 23;//不存在指定文件
    public final static int EXIST = 24;//存在指定文件
    public final static int CONNECT_NET = 25;//请联网
    public final static int NOT_CONTENT_FAIL = 26;//无讲解内容失败
    public final static int SPEAKING = 27;//正在播放中
    public final static int LOADING = 28;//正在加载中
    
    public static int[] types;//类型编号
    public static int curType;//默认类型为化学仪器

	public static ScreenSave mScreenSave;	//照相对象
	public static Matrix44F vbOrthoProjMatrix;//投影矩阵

	//==============MenuView  start==================
	public static float BACK_WIDTH = 1920;//背景宽度
	public static float BACK_HEIGHT = 1080;//背景高度
	public static float TITLE_WIDTH = 1200;//标题宽度  
	public static float TITLE_HEIGHT = 500;//标题高度   
	public static float BUTTON_WIDTH = 460;//标题宽度
	public static float BUTTON_HEIGHT =150;//标题高度
	
	public static float LITTLE_BUTTON_WIDTH = 90;//小圆圈按钮的宽度
	public static float CLOSE_BUTTON_WIDTH = 90;//关闭按钮的宽度
	
	public static float GROUND_WIDTH = 820;//"关于"界面中黑色背景图的宽度
	public static float ABOUT_WIDTH = 1002;//"关于"界面的高度
	public static float GROUND_HEIGHT = 600;//"关于"界面中黑色背景图的高度
	public static float BACK_BUTTON_WIDTH = 120;//返回键的宽度
	
	public static float BACK_LOCATION_X = 960;//背景图片的x坐标
	public static float BACK_LOCATION_Y = 540;//背景图片的y坐标
	public static float TITLE_LOCATION_X = 960;//大标题图片的x坐标
	public static float TITLE_LOCATION_Y = 300;//大标题图片的y坐标
	public static float BUTTON1_LOCATION_X = 960;//"进入体验"按钮的x坐标
	public static float BUTTON1_LOCATION_Y =550;//"进入体验"按钮的y坐标
	public static float BUTTON2_LOCATION_Y =750;//"数据包管理"按钮的x坐标
	public static float BUTTON3_LOCATION_Y =950;//"帮助"按钮的y坐标
	public static float ABOUT_LOCATION_X = 70;//"关于"按钮的x坐标
	public static float ABOUT_LOCATION_Y = 1010;//"关于"按钮的y坐标
	public static float SETTINGS_LOCATION_X = 1850;//设置音效按钮的x坐标
	public static float GROUND_LOCATION_X = 960;//关于界面中背景和文字的x坐标
	public static float GROUND_LOCATION_Y = 740;//关于界面中背景和文字的y坐标
	public static float BACK_BUTTON_LOCATION_X = 620;//关于界面中"返回"按钮的x位置
	public static float BACK_BUTTON_LOCATION_Y = 960;//关于界面中"返回"按钮的y位置
	public static float CLOSE_BUTTON_LOCATION_X = 1850;//返回按钮的x坐标
	public static float CLOSE_BUTTON_LOCATION_Y = 70;  //返回按钮的y坐标
	//==============MenuView  end==================
	
	//=======================ExperienceView  start===================
	public static float EV_BACKAGE_DISTANCE = 1920;//背景图片的宽度
	public static float EV_BACKAGE_DISTANCE_Y = 1080;//背景图片的高度
	public static float EV_CATEGORY_BUTTON_WIDTH = 520;//每个类的模型标签的宽度
	public static float EV_CATEGORY_BUTTON_HEIGHT =300;//每个类的模型标签的高度
	public static float EV_CATEGORY_BUTTON_LOCATION_X = 400;//第一个类别的x坐标
	public static float EV_CATEGORY_BUTTON_LOCATION_Y = 400;//第一个类别的y坐标
	public static float EV_CATEGORY_BUTTON_DISTANCE_X = 520;//左右两个类别的x坐标间隔
	public static float EV_CATEGORY_BUTTON_DISTANCE_Y = 250;//上下两个类别的y坐标间隔
	public static float EV_CIRCLE_LOCATION_X = 960;  //体验界面中小圆点的绘制的x坐标
	public static float EV_CIRCLE_LOCATION_Y = 960;  //体验界面中小圆点的绘制的y坐标
	public static float EV_TYPE_LOCATION_X = 960;   //"体验类别"标题的x坐标
	public static float EV_TYPE_LOCATION_Y= 150;    //"体验类别"标题的y坐标
	
	public static float DMV_DOWNLOAD_BUTTON_DISTANCE_X = 600;//左右两个下载图标的x间距
	public static float DMV_CATEGORY_BUTTON_DISTANCE_X = 600;//左右两个模型文字描述的x间距
	public static float DMV_CATEGORY_BUTTON_LOCATION_X = 300;//第一个模型的文字的x坐标
	public static float DMV_DOWNLOAD_BUTTON_LOCATION_X =570;//第一个下载图标的x坐标
	public static float DMV_CIRCLE_LOCATION_X = 960;//小圆圈的x坐标
	//=======================ExperienceView  end===================
	
	//HelpView
	public static float CIRCLE_ONE_LOCATION_X = 790;//帮助界面中第一个圆点的x坐标
	public static float CIRCLE_LOCATION_Y = 1000;//帮助界面中原点的Y坐标
	public static float CIRCLE_TWO_LOCATION_X = 960;//帮助界面中第二个圆点的x坐标
	public static float CIRCLE_THREE_LOCATION_X = 1130;//帮助界面中第三个圆点的x坐标
	
	//==============LoadView  start==================
	public static float PROGRESS_LOCATION_X = 960;//进度条的x坐标
	public static float PROGRESS_LOCATION_Y = 650;//进度条的y坐标
	public static float PROGRESS_WIDTH = 1000;//进度条宽度
	public static float PROGRESS_HEIGHT = 40;//进度条高度
	public static float PROGRESS_STRAT_X = PROGRESS_LOCATION_X-PROGRESS_WIDTH/2;//进度条开始的x位置
	public static float LOAD_LOCATION_X=260;//人物位置**********************************
	public static float LOAD_LOCATION_Y=850;		
	public static float LOAD_WIDTH = 600;//****************
	public static float LOAD_HEIGHT = 200;
	public static float WORD_WIDTH = 800;//"分子模型加载中..."图片的宽度
	public static float WORD_HEIGHT = 130;//"分子模型加载中..."图片的高度
	public static float WORD_LOCATION_X = 960;//"分子模型加载中..."图片的x坐标
	public static float WORD_LOCATION_Y = 500;//"分子模型加载中..."图片的y坐标
	//==============LoadView  end==================
	
	//==============MainView  start==================
	public static float SOUND_X = 100;//控制声音按钮的x坐标
	public static float SOUND_Y= 90;//控制声音按钮的y坐标
	public static float SOUND_WIDTH = 115; //控制声音按钮的宽度
	public static float SCREEN_SHOT_X = 1800;//截图按钮的x坐标
	public static float SCREEN_SHOT_Y = 1000;//截图按钮的y坐标
	public static float SCREEN_WIDTH = 100;//截图按钮的宽度
	public static float GO_BACK_Y=90;//返回按钮的y坐标
    public static  LoadedObjectVertexNormalTexture spiritLampLine;//加载酒精灯线的模型
    public static  LoadedObjectVertexNormalTexture spiritLampTop;//加载酒精灯盖的模型
	//==============MainView  end==================

    public static int length= 6;//长度，表示"package_list.txt"中每一类模型有几个部分组成
	public static String[] ListArray;//txt分割数组
	public static Bitmap []  typeBitmaps;//表示已经下载的类别图
	public static Bitmap []  typeBitmapsNotLoad;//类别图,表示本类还未下载
	public static  int[] typeTexId;//类别纹理id,表示已经下载*************
	public static  int[] typeTexIdNotLoad;//类别纹理id,表示本类还未下载**************
	public static int[] countTypeObj;//每个类别中物体模型的个数
	public static String[] zipNameArray;
	//====================注意：模型名称要与识别图的名称一致  start=========================
	public static int[][]  mTextures;//分子模型纹理列表
	
	public static String[][] nameList;
	public static LoadedObjectVertexNormalTexture[][] LoadedObj;
	public static String[][] ContentList;
	
	//显示Toast
	public static void goToast(StereoRendering activity,int flag)
	{
		Message message = new Message();
		Bundle bundle = new Bundle();
		bundle.putInt("operation", flag);
		message.setData(bundle);
		activity.myHandler.sendMessage(message);
	}
	
	//设置模型透明度的方法
	public static float getFactor(String name)
	{
		float factor= 1.0f;
		if(name.equals("ConicalFlask")||name.equals("CultureDish")||name.equals("Cup")||name.equals("Cylinder")||
				name.equals("Drier")||name.equals("LiquidFunnel")||name.equals("OrdinaryFunnel")||name.equals("RoundFlask")||
				name.equals("SpiritLamp")||name.equals("ThreeGuideTube")||name.equals("Ucuvette")){
			factor = 0.6f;
		}
		return factor;
	}
	
	//屏幕自适应===============start=====================
	public static float SCREEN_WIDTH_STANDARD = 1920;//标准屏幕的宽度
	public static float SCREEN_HEIGHT_STANDARD = 1080;//标准屏幕的高度
		
	//标准屏幕宽高比
	public static float ratio=SCREEN_WIDTH_STANDARD/SCREEN_HEIGHT_STANDARD;
	//缩放计算结果
	public static ScreenScaleResult ssr;
	
	public static float fromPixSizeToNearSize(float size)
	{
		return size*2/SCREEN_HEIGHT_STANDARD;
	}
	//屏幕x坐标到视口x坐标
	public static float fromScreenXToNearX(float x)
	{
		return (x-SCREEN_WIDTH_STANDARD/2)/(SCREEN_HEIGHT_STANDARD/2);
	}
	//屏幕y坐标到视口y坐标
	public static float fromScreenYToNearY(float y)
	{
		return -(y-SCREEN_HEIGHT_STANDARD/2)/(SCREEN_HEIGHT_STANDARD/2);
	}
	//实际屏幕x坐标到标准屏幕x坐标
	public static float fromRealScreenXToStandardScreenX(float rx)
	{
		return (rx-ssr.lucX)/ssr.ratio;
	}
	//实际屏幕y坐标到标准屏幕y坐标
	public static float fromRealScreenYToStandardScreenY(float ry)
	{
		return (ry-ssr.lucY)/ssr.ratio;
	}
	//===================end========================
	
}
