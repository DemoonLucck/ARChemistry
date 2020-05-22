package com.bn.ar.views;

import static com.bn.ar.utils.Constant.*;

import java.util.ArrayList;

import com.bn.ar.activity.StereoRendering;
import com.bn.ar.draw.BN2DObject;
import com.bn.ar.manager.ShaderManager;
import com.bn.ar.manager.TextureManager;
import com.bn.ar.utils.Constant;
import com.bn.ar.utils.MatrixState2D;
import com.bn.thread.URLConnectionThread;
import android.view.MotionEvent;

public class ExperienceView extends BNAbstractView{
	ArrayList<BN2DObject> al = new ArrayList<BN2DObject>();//背景等
	ArrayList<BN2DObject> alCircle = new ArrayList<BN2DObject>();//背景等
	ArrayList<BN2DObject> alCategoryLoad = new ArrayList<BN2DObject>();//已经下载完的类别
	GlSurfaceView mv;
	public static boolean initBitmap = true;//是否要初始化类别图
	public static boolean isAddBitmap=true;//是否要添加类别图
	private float x,y,max[],min[];//用来存储各个页面能到达的最大坐标和最小坐标
	int page = 1;//总页数
	float pointX[];//存储各个页面的X坐标
	int currPage = 0;//当前页数
	float moveSpan = 10;//滑动的阈值
	public ExperienceView(GlSurfaceView mv)
	{
		this.mv =mv;
		onSurfaceCreated();
	}

	@Override
	public boolean onTouchEvent(MotionEvent e) {
		switch(e.getAction()){
		case MotionEvent.ACTION_DOWN://如果当前动作为按下
			x=Constant.fromRealScreenXToStandardScreenX(e.getX());//将屏幕x坐标转换为标准屏幕坐标
			y=Constant.fromRealScreenYToStandardScreenY(e.getY());//将屏幕y坐标转换为标准屏幕坐标
			break;
		case MotionEvent.ACTION_UP://如果当前动作为抬起
			float upx = Constant.fromRealScreenXToStandardScreenX(e.getX());
			if(x!=0&&Math.abs(upx-x)>moveSpan){//如果滑动大于阈值
				if(page<=1){break;}//如果只有一页，则不允许滑动
				if((upx-x)<0){//左走并大于阈值
					currPage++;//向左滑动，当前页自加
					if(currPage==page){currPage = 0;}//到达右边边界，循环
					for(int i=0;i<alCategoryLoad.size();i++){//所有图片的x坐标减去屏幕宽度
						pointX[i]=pointX[i]-EV_BACKAGE_DISTANCE;
						if(pointX[i] == min[i]){//到达最小边界，人为改变坐标使其循环
							pointX[i] =max[i]-EV_BACKAGE_DISTANCE;
						}
						alCategoryLoad.get(i).x = Constant.fromScreenXToNearX(pointX[i]);						
					}
				}else{//右走并大于阈值
					currPage--;//向右滑动，当前页自减
					if(currPage==-1){currPage = page-1;}//到达左边边界，循环
					for(int i=0;i<alCategoryLoad.size();i++){//所有图片的x坐标加上屏幕宽度
						pointX[i]=pointX[i]+EV_BACKAGE_DISTANCE;
						if(pointX[i] == max[i]){//到达最大边界，人为改变坐标使其循环
							pointX[i] =min[i]+EV_BACKAGE_DISTANCE;
						}
						alCategoryLoad.get(i).x= Constant.fromScreenXToNearX(pointX[i]);
				}}
				changeCircle();
				if(soundOn){
					StereoRendering.sound.playMusic(Constant.BUTTON_PRESS, 0);
			}}else{//如果当前动作为按下
				if(x>(CLOSE_BUTTON_LOCATION_X-CLOSE_BUTTON_WIDTH/2)
						&&x<(CLOSE_BUTTON_LOCATION_X+CLOSE_BUTTON_WIDTH/2)
						&&y>(CLOSE_BUTTON_LOCATION_Y-CLOSE_BUTTON_WIDTH/2)
						&&y<(CLOSE_BUTTON_LOCATION_Y+CLOSE_BUTTON_WIDTH/2))
				{//点击返回按钮
					x = 0;
					reSetData();
					mv.currView = mv.menuView;
					if(soundOn){
						StereoRendering.sound.playMusic(Constant.BUTTON_PRESS, 0);
				}}
				else{//点击任意一项
					for(int i=0;i<alCategoryLoad.size();i++)
					{
						float tmpx = Constant.fromScreenXToNearX(x);//转化为视口坐标
						float tmpy = Constant.fromScreenYToNearY(y);
						if(tmpx>(alCategoryLoad.get(i).x-Constant.fromPixSizeToNearSize(EV_CATEGORY_BUTTON_WIDTH/2))
								&&tmpx<(alCategoryLoad.get(i).x+Constant.fromPixSizeToNearSize(EV_CATEGORY_BUTTON_WIDTH/2))
								&&tmpy>(alCategoryLoad.get(i).y-Constant.fromPixSizeToNearSize(EV_CATEGORY_BUTTON_HEIGHT/2))
								&&tmpy<(alCategoryLoad.get(i).y+Constant.fromPixSizeToNearSize(EV_CATEGORY_BUTTON_HEIGHT/2))){
							//判断触摸的是哪一项
							x = 0;
							curType = types[i];
							reSetData();
							if(soundOn){
								StereoRendering.sound.playMusic(Constant.BUTTON_PRESS, 0);
							}
							if(downLoadZipFinish[curType])
							{
								mv.currView = mv.loadView;
							}else{
								goToast(mv.activity,NOT_EXIST);
							}
							break;
			}}}}
		break;
		case MotionEvent.ACTION_MOVE:
			if(x==0||page==1){break;}
			float moveX= Constant.fromRealScreenXToStandardScreenX(e.getX());
			float dx = moveX-x;
			if(Math.abs(dx)>moveSpan){
				//滑动当前页面
				for(int i=0;i<alCategoryLoad.size();i++)
				{
					float changeBefore = pointX[i]+dx;
					alCategoryLoad.get(i).x = Constant.fromScreenXToNearX(changeBefore);
				}
			}
			break;
		}
		return true;
	}
	

	private void reSetData()
	{
		currPage = 0;
		x = 0;
		y = 0;
	}
	
	private void changeCircle()
	{
		for(int i=0;i<alCircle.size();i++)
		{
			if(i==currPage)
			{
				alCircle.get(i).setTexture(TextureManager.getTextures("blackcircle.png"));
			}else{
				alCircle.get(i).setTexture(TextureManager.getTextures("whitecircle.png"));
			}
		}
	}
	@Override
	public void onSurfaceCreated() {
		al.add(new BN2DObject(TextureManager.getTextures("bg_back.png"),ShaderManager.getShader(0),
				BACK_WIDTH,BACK_HEIGHT,BACK_LOCATION_X,BACK_LOCATION_Y));	//背景
		al.add(new BN2DObject(TextureManager.getTextures("close.png"),ShaderManager.getShader(0),
				CLOSE_BUTTON_WIDTH,CLOSE_BUTTON_WIDTH,
				CLOSE_BUTTON_LOCATION_X,CLOSE_BUTTON_LOCATION_Y));		//返回
		al.add(new BN2DObject(TextureManager.getTextures("experience.png"),ShaderManager.getShader(0),
				EV_CATEGORY_BUTTON_WIDTH,EV_CATEGORY_BUTTON_HEIGHT,
				EV_TYPE_LOCATION_X,EV_TYPE_LOCATION_Y));
	}

	@Override
	public void onDrawFrame() {
		
		initTypeData();//联网初始化
		//绘制背景等
		for(BN2DObject tmp:al)
		{
			MatrixState2D.pushMatrix();
			MatrixState2D.translate(tmp.x,tmp.y,0);
			tmp.drawSelf();
			MatrixState2D.popMatrix();
		}
		
		for(int i=0;i<downLoadZipFinish.length;i++)
		{
			if(!downLoadZipFinish[i])
			{//该类别的zip包未下载
				alCategoryLoad.get(i).setTexture(typeTexIdNotLoad[i]);
			}else{//该类别的zip包已下载
				alCategoryLoad.get(i).setTexture(typeTexId[i]);
			}
		}
		
		for(int i=0;i<alCategoryLoad.size();i++)
		{//绘制
			MatrixState2D.pushMatrix();
			MatrixState2D.translate(alCategoryLoad.get(i).x,alCategoryLoad.get(i).y,0);
			alCategoryLoad.get(i).drawSelf();
			MatrixState2D.popMatrix();
		}
	
		for(int i=0;i<page;i++)
		{
			MatrixState2D.pushMatrix();
			MatrixState2D.translate(alCircle.get(i).x,alCircle.get(i).y,0);
			alCircle.get(i).drawSelf();
			MatrixState2D.popMatrix();
		}

	}
	
	public void initTypeData()
	{
		if(!net_success&&typeBitmaps.length==0)
		{//联网失败并且SD卡不存在指定文件
			return;
		}
		while(initBitmap)
		{ 	
			URLConnectionThread.init=true;
			if(URLConnectionThread.init)
			{   
				page = typeTexId.length/6+1;
				pointX = new float[typeTexId.length];
				max = new float[typeTexId.length];
				min = new float[typeTexId.length];
				for(int i=0;i<typeTexId.length;i++)
				{
					int curPage = i/6;//当前页,每一个包含6项
					int curCol = (i%3);//当前列，
					int curRow = (i/3)%2;//当前行
					typeTexId[i] = TextureManager.initTexture(typeBitmaps[i], false);
					typeTexIdNotLoad[i] = TextureManager.initTexture(typeBitmapsNotLoad[i], false);
					if(isAddBitmap)
					{//添加类别图
						alCategoryLoad.add(new BN2DObject(
								typeTexId[i],ShaderManager.getShader(0),
								EV_CATEGORY_BUTTON_WIDTH,
								EV_CATEGORY_BUTTON_HEIGHT,
								EV_CATEGORY_BUTTON_LOCATION_X+EV_BACKAGE_DISTANCE*curPage+(EV_CATEGORY_BUTTON_DISTANCE_X*curCol),
								EV_CATEGORY_BUTTON_LOCATION_Y+(EV_CATEGORY_BUTTON_DISTANCE_Y*curRow))
						);
						
					}
			
					pointX[i] = EV_CATEGORY_BUTTON_LOCATION_X+EV_BACKAGE_DISTANCE*curPage+(EV_CATEGORY_BUTTON_DISTANCE_X*curCol);
					max[i] = pointX[i] +(page-curPage)*EV_BACKAGE_DISTANCE;//记录每个小纹理可到达的最大边界					
					min[i] = pointX[i] - (curPage+1)*EV_BACKAGE_DISTANCE;//记录每个小纹理可到达的最小边界				
				}
				
				if(page>1)
				{
					float span = (LITTLE_BUTTON_WIDTH*page+100*(page-1))/page;
					EV_CIRCLE_LOCATION_X -=span;
				}
				
				for(int i=0;i<page;i++)
				{
					alCircle.add(new BN2DObject(TextureManager.getTextures("blackcircle.png"),ShaderManager.getShader(0),
							LITTLE_BUTTON_WIDTH,LITTLE_BUTTON_WIDTH,
							EV_CIRCLE_LOCATION_X+i*100,EV_CIRCLE_LOCATION_Y));
				}

				changeCircle();
				isAddBitmap=false;
				initBitmap = false;
			}
		}
	}
}
