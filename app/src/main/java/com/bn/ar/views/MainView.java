package com.bn.ar.views;
import static com.bn.ar.utils.Constant.ContentList;
import static com.bn.ar.utils.Constant.GO_BACK_Y;
import static com.bn.ar.utils.Constant.LoadedObj;
import static com.bn.ar.utils.Constant.NOT_CONTENT_FAIL;
import static com.bn.ar.utils.Constant.SCREEN_SHOT_X;
import static com.bn.ar.utils.Constant.SCREEN_SHOT_Y;
import static com.bn.ar.utils.Constant.SCREEN_WIDTH;
import static com.bn.ar.utils.Constant.SCREEN_WIDTH_STANDARD;
import static com.bn.ar.utils.Constant.SOUND_WIDTH;
import static com.bn.ar.utils.Constant.SOUND_X;
import static com.bn.ar.utils.Constant.SOUND_Y;
import static com.bn.ar.utils.Constant.SPEAKING;
import static com.bn.ar.utils.Constant.curType;
import static com.bn.ar.utils.Constant.getFactor;
import static com.bn.ar.utils.Constant.goToast;
import static com.bn.ar.utils.Constant.mScreenSave;
import static com.bn.ar.utils.Constant.mTextures;
import static com.bn.ar.utils.Constant.nameList;
import static com.bn.ar.utils.Constant.saveFlag;
import static com.bn.ar.utils.Constant.soundOn;
import static com.bn.ar.utils.Constant.spiritLampLine;
import static com.bn.ar.utils.Constant.spiritLampTop;
import static com.bn.ar.utils.Constant.vbOrthoProjMatrix;
import static com.bn.ar.utils.Constant.viewportPosX;
import static com.bn.ar.utils.Constant.viewportPosY;
import static com.bn.ar.utils.Constant.viewportSizeX;
import static com.bn.ar.utils.Constant.viewportSizeY;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.res.Configuration;
import android.opengl.GLES30;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.view.MotionEvent;

import com.bn.ar.activity.StereoRendering;
import com.bn.ar.draw.BN2DObject;
import com.bn.ar.draw.BackgroundMesh;
import com.bn.ar.draw.LoadedObjectVertexNormalTexture;
import com.bn.ar.manager.ShaderManager;
import com.bn.ar.manager.TextureManager;
import com.bn.ar.utils.*;
import com.qualcomm.vuforia.*;

//此界面为扫码界面，对3D模型进行绘制。
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
@SuppressLint("UseSparseArrays")
public class MainView extends BNAbstractView{
	private final float TOUCH_SCALE_FACTOR = 540.0f/SCREEN_WIDTH_STANDARD;//角度缩放比例
	private LoadedObjectVertexNormalTexture currLOVN;//模型绘制对象
	private BackgroundMesh vbMesh = null;//相机所照背景图绘制对象
	private float mPreviousX;//记录上一次点击的X坐标
    private float x,y;//记录当前点击的X,Y坐标
    private String curContent=null;//语音介绍功能所用的文字内容
    private boolean mIsEyewear = true;//是否检测到眼镜设备
    ArrayList<BN2DObject>  al=new ArrayList<BN2DObject>();//存储2D界面
    GlSurfaceView mv;
    boolean isDrawSpiritLamp=false;//是否要画酒精灯
    public MainView(GlSurfaceView mv){
    	this.mv = mv;
    	onSurfaceCreated();
    }
	@Override
	public boolean onTouchEvent(MotionEvent e){
        switch (e.getAction()) {
          case MotionEvent.ACTION_MOVE:
        	  if(x!=0){
        		  x = Constant.fromRealScreenXToStandardScreenX(e.getX()); 
        		  float dx = x-mPreviousX;//计算触控笔X位移
            	  if(currLOVN!=null&&Math.abs(dx)>10f){
            		  currLOVN.yAngle += dx * TOUCH_SCALE_FACTOR;//设置沿y轴旋转角度
            	  }	
        	  }
       	   break;
          case MotionEvent.ACTION_DOWN:
        	  y = Constant.fromRealScreenYToStandardScreenY(e.getY());
        	  x = Constant.fromRealScreenXToStandardScreenX(e.getX()); 
        	  if(x>(SCREEN_SHOT_X-SCREEN_WIDTH/2)&&x<(SCREEN_SHOT_X+SCREEN_WIDTH/2)
        			  &&y>(SCREEN_SHOT_Y-SCREEN_WIDTH/2)&&y<(SCREEN_SHOT_Y+SCREEN_WIDTH/2)){//截屏按钮
        		  x = 0;
        		  if(soundOn){
        			  StereoRendering.sound.playMusic(Constant.BUTTON_PRESS, 0);//播放按键声
        		  }
        		  mScreenSave.setFlag(true);//进行截屏
        	  }else if(x>(SCREEN_SHOT_X-SCREEN_WIDTH/2)&&x<(SCREEN_SHOT_X+SCREEN_WIDTH/2)
        			  &&y>(GO_BACK_Y-SCREEN_WIDTH/2)&&y<(GO_BACK_Y+SCREEN_WIDTH/2)){//返回
        		  x = 0;
        		  if(soundOn){
        			  StereoRendering.sound.playMusic(Constant.BUTTON_PRESS, 0);//播放按键声
        		  }
        		  if(mv.activity.mTTS.isSpeaking()){//如果正在播放声音
        			  mv.activity.mTTS.stop();//停止播放
        		  }
        		  reSetData();//重置数据
        		  mv.currView = mv.menuView;//返回菜单界面
        		  
        	  }else if(x>(SOUND_X-SOUND_WIDTH/2)&&x<(SOUND_X+SOUND_WIDTH/2)
        			  &&y>(SOUND_Y-SOUND_WIDTH/2)&&y<(SOUND_Y+SOUND_WIDTH/2)){//点到了讲解按钮
        		  x = 0;
        		  if(curContent!=null&&!curContent.endsWith("NOTSTRING"))//如果存在讲解内容
        		  {
        			  //当引用 “TextToSpeech.QUEUE_FLUSH” 调用Speak()方法时，
        			  //会中断当前实例正在运行的任务（也可以理解为清除当前语音任务，转而执行新的列队任务）。
        			  //引用 “TextToSpeech.QUEUE_ADD”标签的发音任务将被添加到当前任务列队之后。
        			  if(mv.activity.mTTS.isSpeaking()){
        				  goToast(mv.activity, SPEAKING);
        			  }else{
        				  mv.activity.mTTS.speak(curContent, TextToSpeech.QUEUE_FLUSH, null);//进行讲解动作
        			  }
        		  }else{
        			  goToast(mv.activity, NOT_CONTENT_FAIL);
        		  }
        	  }
          case MotionEvent.ACTION_UP:
        	  break;
        }
        mPreviousX = x;
        return true;
	}
	@Override
	public void onSurfaceCreated() {
    	al.add(new BN2DObject(TextureManager.getTextures("screenshot.png"), ShaderManager.getShader(0), 
    			SCREEN_WIDTH,SCREEN_WIDTH,SCREEN_SHOT_X,SCREEN_SHOT_Y));//添加截屏按钮
    	al.add(new BN2DObject(TextureManager.getTextures("goback.png"), ShaderManager.getShader(0), 
    			SCREEN_WIDTH,SCREEN_WIDTH,SCREEN_SHOT_X,GO_BACK_Y));//添加返回按钮
    	al.add(new BN2DObject(TextureManager.getTextures("speech.png"), ShaderManager.getShader(0), 
    			SOUND_WIDTH,SOUND_WIDTH,SOUND_X,SOUND_Y));//添加语音讲解按钮
	}

	@Override
	public void onDrawFrame() {
		 renderFrame();//绘制背景和3D物体
		 draw2DObjects();//绘制2D按钮
		 if(saveFlag) {//对当前画面进行截屏
			 mScreenSave.saveScreen();
			 mScreenSave.setFlag(false);
		 }
	}
	 //绘制方法
	private void renderFrame()
    {
        Eyewear eyewear = Eyewear.getInstance();//得到一个眼镜实例
        checkEyewearStereo(eyewear);//查看眼镜是否是3D模式
        int numEyes = 1;
        if (eyewear.isStereoEnabled())//返回是否开启立体显示功能
        {//未执行，所以numEyes=1        	
            numEyes = 2;//如果开启立体显示功能的话，numEyes=2
        }
        //清除深度缓冲与颜色缓冲
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);
        //启动AR渲染器
        State state = mv.renderer.begin();
        //开启深度检测
        GLES30.glEnable(GLES30.GL_DEPTH_TEST);
        
        //判断是前相机还是后相机
        if (Renderer.getInstance().getVideoBackgroundConfig().getReflection() == 
        		VIDEO_BACKGROUND_REFLECTION.VIDEO_BACKGROUND_REFLECTION_ON)
        {//未执行
        	//开启顺时针卷绕
            GLES30.glFrontFace(GLES30.GL_CW);
        }
        else
        {//执行此处
        	//开启逆时针卷绕
            GLES30.glFrontFace(GLES30.GL_CCW);
        }
            
        if (!eyewear.isSeeThru())
        {//判断眼镜是否可见，此处执行
        	//绘制相机所照背景图
            renderVideoBackground(0, numEyes);
        }
        //Render once for each eye
        for (int eyeIdx = 0; eyeIdx < numEyes; eyeIdx++)
        {//因为numEyes=1，所以仅执行一次
            Matrix44F projectionMatrix;//投影矩阵
            //视口大小相关数据
            int eyeViewportPosX = viewportPosX;
            int eyeViewportPosY = viewportPosY;
            int eyeViewportSizeX = viewportSizeX;
            int eyeViewportSizeY = viewportSizeY;

            //=======================获取投影矩阵和视口数据====start=======================
            
           
            
            // 这是一个标准的移动设备，因此使用提供的投影矩阵mProjectionMatrix
            projectionMatrix = mv.vuforiaAppSession.getProjectionMatrix();
            
            //=======================获取投影矩阵和视口数据====end=======================
            
            //设置视口
            GLES30.glViewport(eyeViewportPosX, eyeViewportPosY, eyeViewportSizeX, eyeViewportSizeY);
            //=======================绘制三维物体   start==============
            // 通过这里判断是否检测到target
            for (int tIdx = 0; tIdx < state.getNumTrackableResults(); tIdx++)
            {
            	// 查看一帧中有几个待跟踪目标，在Vuforia中最多可同时跟踪5个目标
                TrackableResult result = state.getTrackableResult(tIdx);
                Trackable trackable = result.getTrackable();
                MatrixState.setCamera(Tool.convertPose2GLMatrix(result.getPose()).getData());//设置摄像机
                drawObjects(nameList[curType],LoadedObj[curType],ContentList[curType],trackable,projectionMatrix,curType);
            }
          //==================绘制三维物体   end==============
        }
        //关闭深度检测
        GLES30.glDisable(GLES30.GL_DEPTH_TEST);
        //关闭AR渲染器
        mv.renderer.end();
    }
	public void draw2DObjects()
	{
		for(BN2DObject  temp:al)
		{
			MatrixState2D.pushMatrix();//保护场景
			MatrixState2D.translate(temp.x,temp.y,0);//根据点击
			temp.drawSelf();
			MatrixState2D.popMatrix();
		}
	}
    
    //绘制具体的物体
    public void drawObjects(String[] pics,
    		LoadedObjectVertexNormalTexture[] obj,
    		String[] contant,
    		Trackable trackable,Matrix44F projectionMatrix,int id)
    {
    	float factor = 1;//透明因子
    	MatrixState.pushMatrix();//保护现场
        for(int i=0;i<pics.length;i++){
    		boolean flag = trackable.getName().equalsIgnoreCase(pics[i]); //判断当前识别图
    		if(flag){
    			isDrawSpiritLamp = (pics[i].equals("SpiritLamp"))?true:false;//判断此模型是不是酒精灯
    			currLOVN = obj[i];
    			curContent = contant[i];
    			if(currLOVN==null)
    			{
    				System.out.println("当前绘制对象为null");
    				return;
    			}
    			factor = getFactor(pics[i]);//得到绘制时的透明度
//    			if(isDrawSpiritLamp)//如果需要绘制酒精灯的模型
				if(false)//TODO：绘制酒精灯出了BUG！！！spiritLampTop空指针异常
    			{
    				//关闭深度检测
    				GLES30.glDisable(GLES30.GL_DEPTH_TEST);
    				MatrixState.pushMatrix();
    				MatrixState.rotate(currLOVN.yAngle, 0,0,1);//绕Z轴旋转一定角度
    				currLOVN.drawSelf(mTextures[id][0],projectionMatrix,0.9f);//绘制酒精灯灯体
    				MatrixState.popMatrix();

    				MatrixState.pushMatrix();
    				MatrixState.translate(0, 0, 200);
    				MatrixState.rotate(spiritLampTop.yAngle, 0,0,1);//绕Z轴旋转一定角度
    				spiritLampTop.drawSelf(mTextures[id][0], projectionMatrix, 1.0f);//绘制酒精灯盖的模型
    				MatrixState.popMatrix();//恢复现场

    				MatrixState.pushMatrix();
    				MatrixState.translate(0, 10, 85);
    				MatrixState.rotate(spiritLampLine.yAngle, 0,0,1);//绕Z轴旋转一定角度
    				MatrixState.scale(0.8f, 0.8f, 0.8f);//缩小为原模型的0.8
    				spiritLampLine.drawSelf(mTextures[id][0], projectionMatrix, 1.0f);//绘制酒精灯线的模型
    				MatrixState.popMatrix();//恢复现场

    				GLES30.glEnable(GLES30.GL_DEPTH_TEST);
    				isDrawSpiritLamp = false;
    			}else
    			{
    				MatrixState.pushMatrix();
    				MatrixState.rotate(currLOVN.yAngle, 0,0,1);//******
    				currLOVN.drawSelf(mTextures[id][0],projectionMatrix,factor);//绘制物体
    				MatrixState.popMatrix();
    			}
    			break;
    		}
    	}      
    	MatrixState.popMatrix();//恢复现场
    }

    private void checkEyewearStereo(Eyewear eyewear)
    {
    	//如果有眼镜的硬件检测并且支持3D立体显示
        if (eyewear.isDeviceDetected() && eyewear.isStereoCapable())
        {
        	//=======此处未执行========
            mIsEyewear = true;

            // Change the glasses into stereo mode 
            //如果眼镜现在不是3D显示模式
            if (!eyewear.isStereoEnabled())
            {
            	//将眼镜设置成3D显示模式
                if (eyewear.setStereo(true))
                {
                	//重新获取可能已经改变了的正交投影矩阵
                    vbOrthoProjMatrix = Eyewear.getInstance().getOrthographicProjectionMatrix();
                }
                else
                {
                	System.out.println("checkEyewearStereo::Error setting device to stereo mode");
                }
            }
        }
        else
        {
        	//======执行此处======
            if (mIsEyewear)
            {
                mIsEyewear = false;
                //重新获取可能已经改变了的正交投影矩阵
                vbOrthoProjMatrix = Eyewear.getInstance().getOrthographicProjectionMatrix();
            }
        }
    }
    
    private void renderVideoBackground(int vbVideoTextureUnit, int numEyes)
    {
    	//绑定摄像机的背景纹理并从Vuforia获取纹理Id
        if (!Renderer.getInstance().bindVideoBackground(vbVideoTextureUnit))
        {
        	System.out.println("Unable to bind video background texture!!");
            return;
        }
        if (vbMesh == null)
        {
            boolean isActivityPortrait;
            Configuration config = mv.activity.getResources().getConfiguration();//获取布局配置
            //判断是横屏还是竖屏
            if (config.orientation == Configuration.ORIENTATION_LANDSCAPE)//横屏
            {
                isActivityPortrait = false;
            }
            else
            {
                isActivityPortrait = true;//竖屏
            }
            //创建相机所照背景图绘制对象
            vbMesh = new BackgroundMesh(2, 2, isActivityPortrait);
            
            if (!vbMesh.isValid())//如果绘制背景是无效的
            {
                vbMesh = null;
                System.out.println("VB Mesh not valid!!");
                return;
            }
			vbMesh.initShader();//初始化着色器
        }
        //关闭深度检测
        GLES30.glDisable(GLES30.GL_DEPTH_TEST);
        //关闭背面剪裁
        GLES30.glDisable(GLES30.GL_CULL_FACE);
        //绘制相机所照图
        vbMesh.drawSelf(vbVideoTextureUnit, numEyes);
        //检测错误
        ShaderUtil.checkGlError("Rendering of the video background failed");
        //打开深度检测
        GLES30.glEnable(GLES30.GL_DEPTH_TEST);
        //打开背面剪裁
        GLES30.glEnable(GLES30.GL_CULL_FACE);
    }

    public void reSetData()
    {
    	vbMesh = null;//相机所照背景图绘制对象
        mIsEyewear = true;
        isDrawSpiritLamp=false;
    }

//  private void printUserData(Trackable trackable)
//  {
//      String userData = (String) trackable.getUserData();
//      System.out.println( "UserData:Retreived User Data \"" + userData + "\"");
//  }
}
