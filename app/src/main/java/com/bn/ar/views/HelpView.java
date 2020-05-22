package com.bn.ar.views;

import static com.bn.ar.utils.Constant.*;

import java.util.ArrayList;

import com.bn.ar.activity.StereoRendering;
import com.bn.ar.draw.BN2DObject;
import com.bn.ar.manager.ShaderManager;
import com.bn.ar.manager.TextureManager;
import com.bn.ar.utils.Constant;
import com.bn.ar.utils.MatrixState2D;

import android.annotation.SuppressLint;
import android.opengl.GLES30;
import android.view.MotionEvent;

public class HelpView extends BNAbstractView{
	
	ArrayList<BN2DObject> al = new ArrayList<BN2DObject>();//用来存储白圆点和黑圆点
	ArrayList<BN2DObject> alHelpView = new ArrayList<BN2DObject>();//用来存储帮助页面中各个按钮
	float[] trans = new float[3];//动态保存各个画面的平移量
	float[] changeTrans = new float[3];//画面移动时的视口坐标跨度
	int moveSpan = 10;//进行平移操作的阈值
	GlSurfaceView mv;
	float x,y;//触点的xy坐标
	int selectIndex = 0;//记录当前界面的索引
	public HelpView(GlSurfaceView mv)
	{
		this.mv = mv;
		onSurfaceCreated();
	}
	@Override
	public boolean onTouchEvent(MotionEvent e) {		
		switch(e.getAction())
		{
		case MotionEvent.ACTION_DOWN:
			x=Constant.fromRealScreenXToStandardScreenX(e.getX());//将当前屏幕坐标转换为标准屏幕坐标
			y=Constant.fromRealScreenYToStandardScreenY(e.getY());
			if(x>(CLOSE_BUTTON_LOCATION_X-CLOSE_BUTTON_WIDTH/2)
					&&x<(CLOSE_BUTTON_LOCATION_X+CLOSE_BUTTON_WIDTH/2)
					&&y>(CLOSE_BUTTON_LOCATION_Y-CLOSE_BUTTON_WIDTH/2)
					&&y<(CLOSE_BUTTON_LOCATION_Y+CLOSE_BUTTON_WIDTH/2))
			{//点击到返回按钮
				x=0;
				reSetData();
				mv.currView = mv.menuView;//切换至主菜单界面
				if(soundOn){
					StereoRendering.sound.playMusic(Constant.BUTTON_PRESS, 0);//播放按键声
				}
			}
			if(x>(CIRCLE_ONE_LOCATION_X-LITTLE_BUTTON_WIDTH/2)
					&&x<(CIRCLE_ONE_LOCATION_X+LITTLE_BUTTON_WIDTH/2)
					&&y>(CIRCLE_LOCATION_Y-LITTLE_BUTTON_WIDTH/2)
					&&y<(CIRCLE_LOCATION_Y+LITTLE_BUTTON_WIDTH/2))
			{//点击到了第一个小圆点
				selectPage(0);
				if(soundOn){
					StereoRendering.sound.playMusic(Constant.BUTTON_PRESS, 0);//播放按键声
				}
			}
			if(x>(CIRCLE_TWO_LOCATION_X-LITTLE_BUTTON_WIDTH/2)
					&&x<(CIRCLE_TWO_LOCATION_X+LITTLE_BUTTON_WIDTH/2)
					&&y>(CIRCLE_LOCATION_Y-LITTLE_BUTTON_WIDTH/2)
					&&y<(CIRCLE_LOCATION_Y+LITTLE_BUTTON_WIDTH/2))
			{//点击到了第二个小圆点
				selectPage(1);
				if(soundOn){
					StereoRendering.sound.playMusic(Constant.BUTTON_PRESS, 0);//播放按键声
				}
			}
			if(x>(CIRCLE_THREE_LOCATION_X-LITTLE_BUTTON_WIDTH/2)
					&&x<(CIRCLE_THREE_LOCATION_X+LITTLE_BUTTON_WIDTH/2)
					&&y>(CIRCLE_LOCATION_Y-LITTLE_BUTTON_WIDTH/2)
					&&y<(CIRCLE_LOCATION_Y+LITTLE_BUTTON_WIDTH/2))
			{//点击到了第三个小圆点
				selectPage(2);
				if(soundOn){
					StereoRendering.sound.playMusic(Constant.BUTTON_PRESS, 0);//播放按键声
				}
			}
			break;
		case MotionEvent.ACTION_MOVE:
			float mx = Constant.fromRealScreenXToStandardScreenX(e.getX());
			if(x!=0)
			{
				float movex = mx-x;
				for(int i=0;i<changeTrans.length;i++)
				{
					float changeBefore = trans[i]+movex;
					changeTrans[i] = Constant.fromScreenXToNearX(changeBefore);
				}
			}
			break;
		case MotionEvent.ACTION_UP:
			float upx = Constant.fromRealScreenXToStandardScreenX(e.getX());
			if(x!=0)
			{
				if(upx-x<0&&Math.abs(upx-x)>moveSpan)//左走并大于阈值
				{
					if(soundOn){
						StereoRendering.sound.playMusic(Constant.BUTTON_PRESS, 0);//播放按键声
					}
					for(int i=0;i<changeTrans.length;i++)
					{
						trans[i]=trans[i]-1920;
						changeTrans[i] = Constant.fromScreenXToNearX(trans[i]);
						if(trans[i] == -2880){
							trans[i] = 2880;
						}
						if(trans[i] == 960){selectIndex = i;}//获取当前的帮助界面索引
					}
				}else if(upx-x>0&&Math.abs(upx-x)>moveSpan){//右走并大于阈值
					if(soundOn){
						StereoRendering.sound.playMusic(Constant.BUTTON_PRESS, 0);//播放按键声
					}
					for(int i=0;i<changeTrans.length;i++)
					{
						trans[i]=trans[i]+1920;
						changeTrans[i] = Constant.fromScreenXToNearX(trans[i]);
						if(trans[i] == 4800){
							trans[i] = -960;
						}
						if(trans[i] == 960){selectIndex = i;}//获取当前的帮助界面索引
					}
				}
			}
			changeCircle(selectIndex);//改变选中状态
			break;
		}
		return true;
	}

	private void changeCircle(int selectIndex)//更换白圆点/黑圆点的纹理方法
	{
		switch(selectIndex)
		{
		case 0:
			al.get(1).setTexture(TextureManager.getTextures("blackcircle.png"));
			al.get(2).setTexture(TextureManager.getTextures("whitecircle.png"));
			al.get(3).setTexture(TextureManager.getTextures("whitecircle.png"));
			break;
		case 1:
			al.get(1).setTexture(TextureManager.getTextures("whitecircle.png"));
			al.get(2).setTexture(TextureManager.getTextures("blackcircle.png"));
			al.get(3).setTexture(TextureManager.getTextures("whitecircle.png"));
			break;
		case 2:
			al.get(1).setTexture(TextureManager.getTextures("whitecircle.png"));
			al.get(2).setTexture(TextureManager.getTextures("whitecircle.png"));
			al.get(3).setTexture(TextureManager.getTextures("blackcircle.png"));
			break;
		}
	}
	
	@Override
	public void onSurfaceCreated() {
		al.add(new BN2DObject(TextureManager.getTextures("bg_back.png"),ShaderManager.getShader(0),
				BACK_WIDTH,BACK_HEIGHT,BACK_LOCATION_X,BACK_LOCATION_Y));//设置背景	
		al.add(new BN2DObject(TextureManager.getTextures("blackcircle.png"),ShaderManager.getShader(0),
				LITTLE_BUTTON_WIDTH,LITTLE_BUTTON_WIDTH,CIRCLE_ONE_LOCATION_X,CIRCLE_LOCATION_Y));//设置第一个圆圈
		al.add(new BN2DObject(TextureManager.getTextures("whitecircle.png"),ShaderManager.getShader(0),
				LITTLE_BUTTON_WIDTH,LITTLE_BUTTON_WIDTH,CIRCLE_TWO_LOCATION_X,CIRCLE_LOCATION_Y));//设置第二个圆圈
		al.add(new BN2DObject(TextureManager.getTextures("whitecircle.png"),ShaderManager.getShader(0),
				LITTLE_BUTTON_WIDTH,LITTLE_BUTTON_WIDTH,CIRCLE_THREE_LOCATION_X,CIRCLE_LOCATION_Y));//设置第三个圆圈
		al.add(new BN2DObject(TextureManager.getTextures("close.png"),ShaderManager.getShader(0),
				CLOSE_BUTTON_WIDTH,CLOSE_BUTTON_WIDTH,CLOSE_BUTTON_LOCATION_X,CLOSE_BUTTON_LOCATION_Y));//设置返回按钮
		alHelpView.add(new BN2DObject(TextureManager.getTextures("help1.png"),ShaderManager.getShader(0),
				BACK_WIDTH,BACK_HEIGHT,0,0));//帮助界面中的第一个界面
		alHelpView.add(new BN2DObject(TextureManager.getTextures("help2.png"),ShaderManager.getShader(0),
				BACK_WIDTH,BACK_HEIGHT,0,0));//帮助界面中的第二个界面
		alHelpView.add(new BN2DObject(TextureManager.getTextures("help3.png"),ShaderManager.getShader(0),
				BACK_WIDTH,BACK_HEIGHT,0,0));//帮助界面中的第三个界面
		
		
		trans[0] = 960;trans[1] = 2880;trans[2] = -960;//设置各个界面的平移量
		changeTrans[0] = Constant.fromScreenXToNearX(trans[0]);
		changeTrans[1] = Constant.fromScreenXToNearX(trans[1]);
		changeTrans[2] = Constant.fromScreenXToNearX(trans[2]);
	}

	@SuppressLint("NewApi")
	@Override
	public void onDrawFrame() {
		  //清除深度缓冲与颜色缓冲
		GLES30.glClear( GLES30.GL_DEPTH_BUFFER_BIT | GLES30.GL_COLOR_BUFFER_BIT);
		for(int i=0;i<al.size()-1;i++)
		{
			MatrixState2D.pushMatrix();//保护现场
			MatrixState2D.translate(al.get(i).x,al.get(i).y,0);//进行平移操作
			al.get(i).drawSelf();//绘制小圆点
			MatrixState2D.popMatrix();//恢复现场
		}
		for(int i=0;i<alHelpView.size();i++)
		{
			MatrixState2D.pushMatrix();//保护现场
			MatrixState2D.translate(changeTrans[i], 0, 0);//进行平移操作
			alHelpView.get(i).drawSelf();//绘制帮助页面
			MatrixState2D.popMatrix();//恢复现场
		}
		
		MatrixState2D.pushMatrix();//保护现场
		MatrixState2D.translate(al.get(al.size()-1).x,al.get(al.size()-1).y,0);//进行平移操作
		al.get(al.size()-1).drawSelf();  //绘制关闭按钮
		MatrixState2D.popMatrix();//恢复现场
	}
	
	//重新初始化数据
	public void reSetData()
	{
		trans[0] = 960;trans[1] = 2880;trans[2] = -960;
		changeTrans[0] = Constant.fromScreenXToNearX(trans[0]);
		changeTrans[1] = Constant.fromScreenXToNearX(trans[1]);
		changeTrans[2] = Constant.fromScreenXToNearX(trans[2]);
		
		selectIndex = 0;
		x = 0;
		y = 0;
	}
	//点击小圆点后选择
	public void selectPage(int page) 
	{
		switch (page) {
		case 0:
			trans[0] = 960;trans[1] = 2880;trans[2] = -960;
			break;
		case 1:
			trans[0] = -960;trans[1] = 960;trans[2] = 2880;
			break;
		case 2:
			trans[0] = 2880;trans[1] = -960;trans[2] = 960;
			break;
		}
		changeTrans[0] = Constant.fromScreenXToNearX(trans[0]);
		changeTrans[1] = Constant.fromScreenXToNearX(trans[1]);
		changeTrans[2] = Constant.fromScreenXToNearX(trans[2]);
		selectIndex=page;
	}
}
