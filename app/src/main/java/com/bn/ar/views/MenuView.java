package com.bn.ar.views;

import static com.bn.ar.utils.Constant.ABOUT_LOCATION_X;
import static com.bn.ar.utils.Constant.ABOUT_LOCATION_Y;
import static com.bn.ar.utils.Constant.BACK_BUTTON_LOCATION_X;
import static com.bn.ar.utils.Constant.BACK_BUTTON_LOCATION_Y;
import static com.bn.ar.utils.Constant.BACK_BUTTON_WIDTH;
import static com.bn.ar.utils.Constant.BACK_HEIGHT;
import static com.bn.ar.utils.Constant.BACK_LOCATION_X;
import static com.bn.ar.utils.Constant.BACK_LOCATION_Y;
import static com.bn.ar.utils.Constant.BACK_WIDTH;
import static com.bn.ar.utils.Constant.BUTTON1_LOCATION_X;
import static com.bn.ar.utils.Constant.BUTTON1_LOCATION_Y;
import static com.bn.ar.utils.Constant.BUTTON2_LOCATION_Y;
import static com.bn.ar.utils.Constant.BUTTON3_LOCATION_Y;
import static com.bn.ar.utils.Constant.BUTTON_HEIGHT;
import static com.bn.ar.utils.Constant.BUTTON_WIDTH;
import static com.bn.ar.utils.Constant.CONNECT_NET;
import static com.bn.ar.utils.Constant.GROUND_HEIGHT;
import static com.bn.ar.utils.Constant.GROUND_LOCATION_X;
import static com.bn.ar.utils.Constant.GROUND_LOCATION_Y;
import static com.bn.ar.utils.Constant.GROUND_WIDTH;
import static com.bn.ar.utils.Constant.LITTLE_BUTTON_WIDTH;
import static com.bn.ar.utils.Constant.SETTINGS_LOCATION_X;
import static com.bn.ar.utils.Constant.TITLE_HEIGHT;
import static com.bn.ar.utils.Constant.TITLE_LOCATION_X;
import static com.bn.ar.utils.Constant.TITLE_LOCATION_Y;
import static com.bn.ar.utils.Constant.TITLE_WIDTH;
import static com.bn.ar.utils.Constant.goToast;
import static com.bn.ar.utils.Constant.soundOn;
import static com.bn.ar.utils.Constant.typeBitmaps;

import java.util.ArrayList;

import android.view.MotionEvent;

import com.bn.ar.activity.StereoRendering;
import com.bn.ar.draw.BN2DObject;
import com.bn.ar.manager.ShaderManager;
import com.bn.ar.manager.TextureManager;
import com.bn.ar.utils.Constant;
import com.bn.ar.utils.MatrixState2D;
import com.bn.thread.URLConnectionThread;

public class MenuView extends BNAbstractView{
	GlSurfaceView mv;
	ArrayList<BN2DObject> al = new ArrayList<BN2DObject>();//存储主菜单界面的2D画面
	ArrayList<BN2DObject> alAboutView = new ArrayList<BN2DObject>();//存储关于界面的2D画面
	boolean isAboutView = false;//用于区分是现在处于关于界面还是主菜单界面
	float translateY = -0.8f;//关于界面Y方向的初始平移量
	public MenuView(GlSurfaceView mv){
		this.mv = mv;
		onSurfaceCreated();
	}
	@Override
	public boolean onTouchEvent(MotionEvent e) {
		float x=Constant.fromRealScreenXToStandardScreenX(e.getX());//将当前屏幕x坐标转换为标准屏幕x坐标
		float y=Constant.fromRealScreenYToStandardScreenY(e.getY());//将当前屏幕y坐标转换为标准屏幕y坐标
		switch(e.getAction())
		{
		case MotionEvent.ACTION_DOWN:
			if(isAboutView&&x>(BACK_BUTTON_LOCATION_X-BACK_BUTTON_WIDTH/2)&&x<(BACK_BUTTON_LOCATION_X+BACK_BUTTON_WIDTH/2)
					&&y>(BACK_BUTTON_LOCATION_Y-BACK_BUTTON_WIDTH/2)&&y<(BACK_BUTTON_LOCATION_Y+BACK_BUTTON_WIDTH/2))
			{//点击关于界面内的返回按钮
				if(soundOn){
					StereoRendering.sound.playMusic(Constant.BUTTON_PRESS, 0);//播放按键声
				}
				isAboutView = !isAboutView;
			}else if(!isAboutView&&x>(BUTTON1_LOCATION_X-BUTTON_WIDTH/2)&&x<(BUTTON1_LOCATION_X+BUTTON_WIDTH/2)
					&&y>(BUTTON1_LOCATION_Y-BUTTON_HEIGHT/2)&&y<(BUTTON1_LOCATION_Y+BUTTON_HEIGHT/2)){
				//点击了进入体验按钮
				if(soundOn){
					StereoRendering.sound.playMusic(Constant.BUTTON_PRESS, 0);//播放按键声
				}
				if(typeBitmaps!=null&&typeBitmaps.length>0)//如果类别图已下载完毕
				{
					ExperienceView.initBitmap=true;//加载数据加载界面的纹理图
					mv.currView = mv.experienceView;//跳转到体验界面
				}else{
					URLConnectionThread.isWantToUpdata=false;
					URLConnectionThread.init = false;//资源初始化未完成
					URLConnectionThread.flag= true;//进行资源初始化线程
					goToast(mv.activity, CONNECT_NET);
				}

			}else if(!isAboutView&&x>(BUTTON1_LOCATION_X-BUTTON_WIDTH/2)&&x<(BUTTON1_LOCATION_X+BUTTON_WIDTH/2)
					&&y>(BUTTON2_LOCATION_Y-BUTTON_HEIGHT/2)&&y<(BUTTON2_LOCATION_Y+BUTTON_HEIGHT/2))
			{
				//点击到了数据包管理
				if(soundOn){
					StereoRendering.sound.playMusic(Constant.BUTTON_PRESS, 0);//播放按键声
				}		
				URLConnectionThread.isWantToUpdata=true;
				URLConnectionThread.init = false;
				URLConnectionThread.flag= true;
				try{
					Thread.sleep(100);
				}catch(Exception exception)
					{
						exception.printStackTrace();
					}
				
				if(!Constant.net_success)
				{
					goToast(mv.activity, CONNECT_NET);
				}
				else 
					if(typeBitmaps!=null&&typeBitmaps.length>0)//如果类别图已下载完毕
				{
					mv.currView = mv.dataManagerView;//跳转到数据包管理界面
				}
				
			}else if(!isAboutView&&x>(BUTTON1_LOCATION_X-BUTTON_WIDTH/2)&&x<(BUTTON1_LOCATION_X+BUTTON_WIDTH/2)
					&&y>(BUTTON3_LOCATION_Y-BUTTON_HEIGHT/2)&&y<(BUTTON3_LOCATION_Y+BUTTON_HEIGHT/2))
			{//帮助
				if(soundOn){
					StereoRendering.sound.playMusic(Constant.BUTTON_PRESS, 0);//播放按键声
				}
				mv.currView = mv.helpView;//跳转到帮助界面
			}else if(!isAboutView&&x>(ABOUT_LOCATION_X-LITTLE_BUTTON_WIDTH/2)&&x<(ABOUT_LOCATION_X+LITTLE_BUTTON_WIDTH/2)
					&&y>(ABOUT_LOCATION_Y-LITTLE_BUTTON_WIDTH/2)&&y<(ABOUT_LOCATION_Y+LITTLE_BUTTON_WIDTH/2))
			{//关于
				if(soundOn){
					StereoRendering.sound.playMusic(Constant.BUTTON_PRESS, 0);//播放按键声
				}
				isAboutView = true;//表示现在处于关于界面
			}else if(!isAboutView&&x>(SETTINGS_LOCATION_X-LITTLE_BUTTON_WIDTH/2)&&x<(SETTINGS_LOCATION_X+LITTLE_BUTTON_WIDTH/2)
					&&y>(ABOUT_LOCATION_Y-LITTLE_BUTTON_WIDTH/2)&&y<(ABOUT_LOCATION_Y+LITTLE_BUTTON_WIDTH/2))
			{//点击到了音效按钮
				soundOn = !soundOn;//将表示声音是否开启的标志位置反
				if(soundOn){
					al.get(3).setTexture(TextureManager.getTextures("soundon.png"));//设置音效开启的纹理
				}else{
					al.get(3).setTexture(TextureManager.getTextures("soundoff.png"));//设置音效关闭的纹理
				}
				if(soundOn){
					StereoRendering.sound.playMusic(Constant.BUTTON_PRESS, 0);//播放按键声
				}
			}
			break;
		}
		return true;
	}

	@Override
	public void onSurfaceCreated(){
		TextureManager.loadingTexture(mv, 0, TextureManager.texturesName.length);//加载所有的纹理图
		ShaderManager.loadingShader(mv, 0, ShaderManager.programs.length);//加载所有的着色器
		al.add(new BN2DObject(TextureManager.getTextures("bg_back.png"),ShaderManager.getShader(0),
				BACK_WIDTH,BACK_HEIGHT,BACK_LOCATION_X,BACK_LOCATION_Y));//设置背景的纹理和位置
		al.add(new BN2DObject(TextureManager.getTextures("title.png"),ShaderManager.getShader(0),
				TITLE_WIDTH,TITLE_HEIGHT,TITLE_LOCATION_X,TITLE_LOCATION_Y));//设置标题的纹理和位置
		al.add(new BN2DObject(TextureManager.getTextures("about.png"),ShaderManager.getShader(0),
				LITTLE_BUTTON_WIDTH,LITTLE_BUTTON_WIDTH,ABOUT_LOCATION_X,ABOUT_LOCATION_Y));//设置关于按钮的纹理和位置
		al.add(new BN2DObject(TextureManager.getTextures("soundon.png"),ShaderManager.getShader(0),
				LITTLE_BUTTON_WIDTH,LITTLE_BUTTON_WIDTH,SETTINGS_LOCATION_X,ABOUT_LOCATION_Y));//设置音效按钮的纹理和位置
		
		al.add(new BN2DObject(TextureManager.getTextures("into.png"),ShaderManager.getShader(0),
				BUTTON_WIDTH,BUTTON_HEIGHT,BUTTON1_LOCATION_X,BUTTON1_LOCATION_Y));//设置进入体验按钮的纹理和位置
		al.add(new BN2DObject(TextureManager.getTextures("datamanager.png"),ShaderManager.getShader(0),
				BUTTON_WIDTH,BUTTON_HEIGHT,BUTTON1_LOCATION_X,BUTTON2_LOCATION_Y));//设置数据包管理按钮的纹理和位置
		al.add(new BN2DObject(TextureManager.getTextures("help.png"),ShaderManager.getShader(0),
				BUTTON_WIDTH,BUTTON_HEIGHT,BUTTON1_LOCATION_X,BUTTON3_LOCATION_Y));//设置帮助按钮的纹理和位置
		al.get(4).setScaleVaule(0.004f,0.004f,0.004f);//设置当前按钮的三次的缩放量
		al.get(5).setScaleVaule(0.003f,0.003f,0.003f);//设置当前按钮的三次的缩放量
		al.get(6).setScaleVaule(0.0045f,0.0045f,0.0045f);//设置当前按钮的三次的缩放量
		
		alAboutView.add(new BN2DObject(TextureManager.getTextures("ground.png"),ShaderManager.getShader(0),
				GROUND_WIDTH,GROUND_HEIGHT,GROUND_LOCATION_X,GROUND_LOCATION_Y));//设置关于界面中黑色背景的纹理和位置
		alAboutView.add(new BN2DObject(TextureManager.getTextures("aboutC.png"),ShaderManager.getShader(0),
				GROUND_WIDTH,GROUND_HEIGHT,GROUND_LOCATION_X,GROUND_LOCATION_Y));//设置关于界面中文字介绍的纹理和位置
		alAboutView.add(new BN2DObject(TextureManager.getTextures("back.png"),ShaderManager.getShader(0),
				BACK_BUTTON_WIDTH,BACK_BUTTON_WIDTH,BACK_BUTTON_LOCATION_X,BACK_BUTTON_LOCATION_Y));//设置返回按钮的纹理和位置
	}

	@Override
	public void onDrawFrame()
	{
		for(int i=0;i<al.size();i++){
			MatrixState2D.pushMatrix();//保护现场
			if(i>=4){al.get(i).scaleButton();}//更新缩放倍数
			MatrixState2D.translate(al.get(i).x,al.get(i).y,0);//进行平移
			MatrixState2D.scale(al.get(i).scaleTemp,al.get(i).scaleTemp,al.get(i).scaleTemp);//对按钮进行缩放
			al.get(i).drawSelf();//绘制按钮
			MatrixState2D.popMatrix();//恢复现场
		}
		if(isAboutView)
		{//绘制关于内容
			translateY+=0.025f;//更新关于界面y轴上的平移量
			translateY = translateY>0?0:translateY;//平移量增加到0后保持不变
			MatrixState2D.pushMatrix();//保护现场
			MatrixState2D.translate(0,translateY,0);//进行平移操作
			for(BN2DObject temp:alAboutView){
				MatrixState2D.pushMatrix();//保护现场
				MatrixState2D.translate(temp.x,temp.y,0);//进行平移操作
				temp.drawSelf();//绘制按钮
				MatrixState2D.popMatrix();//恢复现场
			}
			MatrixState2D.popMatrix();//恢复现场
		}else{
			translateY= -0.8f;//初始化平移量
		}
	}
}
