package com.bn.ar.views;
import static com.bn.ar.utils.Constant.*;

import java.util.ArrayList;
import com.bn.ar.activity.StereoRendering;
import com.bn.ar.draw.BN2DObject;
import com.bn.ar.draw.DownLoadingTextureRect;
import com.bn.ar.manager.ShaderManager;
import com.bn.ar.manager.TextureManager;
import com.bn.ar.utils.Constant;
import com.bn.ar.utils.MatrixState2D;
import com.bn.thread.URLConnectionThread;

import android.view.MotionEvent;

public class DataManagerView extends BNAbstractView {
	BN2DObject back;
	BN2DObject close;
	ArrayList<BN2DObject> al = new ArrayList<BN2DObject>();//类别纹理的绘制者
	ArrayList<BN2DObject> alLoadEnd = new ArrayList<BN2DObject>();//下载结束按钮绘制者
	ArrayList<DownLoadingTextureRect> alLoading = new ArrayList<DownLoadingTextureRect>();//下载中绘制者
	ArrayList<BN2DObject> alCircle = new ArrayList<BN2DObject>();//圆圈绘制者
	GlSurfaceView mv;
	public static  boolean initBitmap = true;//是否要进行初始化bitmap图
	int page = 1;
	int currPage = 0;//当前页数
	float pointLoadX[];//用于记录初始的下载按钮的x坐标
	float pointTypeX[];//用于记录初始的类别选项的x坐标
	float x,y;
	float moveSpan = 10;
	public DataManagerView(GlSurfaceView mv)
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
			break;
		case MotionEvent.ACTION_UP:			
			float upx = Constant.fromRealScreenXToStandardScreenX(e.getX());
			if(x!=0&&Math.abs(upx-x)>moveSpan)
			{
				if(page<=1){break;}//如果只有一页，则不允许滑动
				if((upx-x)<0)//左走并大于阈值
				{
					currPage++;
					if(currPage==page){currPage = 0;}//到达右边边界，循环
				}else{//右走并大于阈值
					currPage--;
					if(currPage==-1){currPage = page-1;}//到达左边边界，循环
				}
				
				for(int i=0;i<alLoadEnd.size();i++)
				{//重新计算每个纹理的x坐标
					al.get(i).x = pointTypeX[i] - Constant.fromPixSizeToNearSize(currPage*EV_BACKAGE_DISTANCE);
					alLoading.get(i).x=pointLoadX[i] -Constant.fromPixSizeToNearSize(currPage*EV_BACKAGE_DISTANCE);
					alLoadEnd.get(i).x = pointLoadX[i] - Constant.fromPixSizeToNearSize(currPage*EV_BACKAGE_DISTANCE);
				}
				if(soundOn){
					StereoRendering.sound.playMusic(Constant.BUTTON_PRESS, 0);
				}
				changeCircle();
			}else{
				if(x>(CLOSE_BUTTON_LOCATION_X-CLOSE_BUTTON_WIDTH/2)&&x<(CLOSE_BUTTON_LOCATION_X+CLOSE_BUTTON_WIDTH/2)
						&&y>(CLOSE_BUTTON_LOCATION_Y-CLOSE_BUTTON_WIDTH/2)&&y<(CLOSE_BUTTON_LOCATION_Y+CLOSE_BUTTON_WIDTH/2))
				{//点击返回按钮
					x = 0;
					reSetData();
					mv.currView = mv.menuView;
					if(soundOn)
					{
						StereoRendering.sound.playMusic(Constant.BUTTON_PRESS, 0);
					}
				}else
				{
					for(int i=0;i<alLoadEnd.size();i++)
					{
						float tmpx = Constant.fromScreenXToNearX(x);//转化为视口坐标
						float tmpy = Constant.fromScreenYToNearY(y);
						if(tmpx>(alLoadEnd.get(i).x-Constant.fromPixSizeToNearSize(LITTLE_BUTTON_WIDTH/2))
								&&tmpx<(alLoadEnd.get(i).x+Constant.fromPixSizeToNearSize(LITTLE_BUTTON_WIDTH/2))
								&&tmpy>(alLoadEnd.get(i).y-Constant.fromPixSizeToNearSize(LITTLE_BUTTON_WIDTH/2))
								&&tmpy<(alLoadEnd.get(i).y+Constant.fromPixSizeToNearSize(LITTLE_BUTTON_WIDTH/2)))
						{//判断触摸的是哪一项
							x = 0;
							if(soundOn){
								StereoRendering.sound.playMusic(Constant.BUTTON_PRESS, 0);
							}
							if(!downLoadZipFinish[i])
							{//还未下载当前想要下载的zip包
								if(!selectDataIndex[i])
								{
									selectDataIndex[i] = true;//选择下载的类别
									selectData = i;
								}else{//正在下载
									goToast(mv.activity,LOADING);
								}
								URLConnectionThread.flag = true;
							}else{
								//提示当前已下载								
								goToast(mv.activity,EXIST);
							}
						}
					}
				}
			}
		break;
		}
		return true;
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
	private void reSetData()
	{
		currPage = 0;
		x = 0;
		y = 0;
	}
	@Override
	public void onSurfaceCreated() {
		back = new BN2DObject(TextureManager.getTextures("bg_back.png"),ShaderManager.getShader(0),
				BACK_WIDTH,BACK_HEIGHT,BACK_LOCATION_X,BACK_LOCATION_Y);
		close = new BN2DObject(TextureManager.getTextures("close.png"),ShaderManager.getShader(0),
				CLOSE_BUTTON_WIDTH,CLOSE_BUTTON_WIDTH,
				CLOSE_BUTTON_LOCATION_X,CLOSE_BUTTON_LOCATION_Y);
	}

	@Override
	public void onDrawFrame() {
		initTypeData();
		
		//绘制背景
		MatrixState2D.pushMatrix();
		MatrixState2D.translate(back.x,back.y,0);
		back.drawSelf();
		MatrixState2D.popMatrix();
		
		//绘制close按钮
		MatrixState2D.pushMatrix();
		MatrixState2D.translate(close.x,close.y,0);
		close.drawSelf();
		MatrixState2D.popMatrix();
		
		//绘制类别选项
		for(int i=0;i<al.size();i++)
		{
			MatrixState2D.pushMatrix();
			MatrixState2D.translate(al.get(i).x,al.get(i).y,0);
			al.get(i).drawSelf();
			MatrixState2D.popMatrix();
		}
		//设置下载按钮纹理
		for(int i=0;i<downLoadZipChangeT.length;i++)
		{
			if(downLoadZipChangeT[i])
			{
				alLoadEnd.get(i).setTexture(TextureManager.getTextures("undownload.png"));
				downLoadZipChangeT[i] = false;
			}
		}
		//绘制下载按钮
		for(int i=0;i<selectDataIndex.length;i++)
		{
			if(selectDataIndex[i]&&!downLoadZipFinish[i])
			{//正在下载中
				if(net_success)
				{//联网成功
					MatrixState2D.pushMatrix();
					MatrixState2D.translate(alLoading.get(i).x,alLoading.get(i).y,0);
					MatrixState2D.rotate(45, 1,0,0);
					alLoading.get(i).drawSelf();
					MatrixState2D.popMatrix();
				}else{//提示联网失败
					selectDataIndex[i] = false;
					goToast(mv.activity, CONNECT_NET);
					continue;
				}
			}else{
				MatrixState2D.pushMatrix();
				MatrixState2D.translate(alLoadEnd.get(i).x,alLoadEnd.get(i).y,0);
				alLoadEnd.get(i).drawSelf();
				MatrixState2D.popMatrix();
			}
		}
		//绘制原点
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
			if(URLConnectionThread.init)
			{
				page = typeTexId.length/6+1;
				pointLoadX = new float[typeTexId.length];
				pointTypeX = new float[typeTexId.length];
				for(int i=0;i<typeTexId.length;i++)
				{
					int curPage = i/6;//当前页
					int curCol = (i%3);//当前列
					int curRow = (i/3)%2;//当前行
					typeTexId[i] = TextureManager.initTexture(typeBitmaps[i], false);
					typeTexIdNotLoad[i] = TextureManager.initTexture(typeBitmapsNotLoad[i], false);
					
					//类别选项
					al.add(new BN2DObject(typeTexId[i],ShaderManager.getShader(0),
							EV_CATEGORY_BUTTON_WIDTH,
							EV_CATEGORY_BUTTON_HEIGHT,
							DMV_CATEGORY_BUTTON_LOCATION_X+EV_BACKAGE_DISTANCE*curPage+(DMV_CATEGORY_BUTTON_DISTANCE_X*curCol),
							EV_CATEGORY_BUTTON_LOCATION_Y+(EV_CATEGORY_BUTTON_DISTANCE_Y*curRow)));
					alLoadEnd.add(new BN2DObject(TextureManager.getTextures("download.png"),ShaderManager.getShader(0),
							LITTLE_BUTTON_WIDTH,
							LITTLE_BUTTON_WIDTH,
							DMV_DOWNLOAD_BUTTON_LOCATION_X+EV_BACKAGE_DISTANCE*curPage+(DMV_DOWNLOAD_BUTTON_DISTANCE_X*curCol),
							EV_CATEGORY_BUTTON_LOCATION_Y+(EV_CATEGORY_BUTTON_DISTANCE_Y*curRow)));
					alLoading.add(new DownLoadingTextureRect(TextureManager.getTextures("downloading.png"),ShaderManager.getShader(4),
							LITTLE_BUTTON_WIDTH,
							LITTLE_BUTTON_WIDTH,
							DMV_DOWNLOAD_BUTTON_LOCATION_X+EV_BACKAGE_DISTANCE*curPage+(DMV_DOWNLOAD_BUTTON_DISTANCE_X*curCol),
							EV_CATEGORY_BUTTON_LOCATION_Y+(EV_CATEGORY_BUTTON_DISTANCE_Y*curRow)));//正在下载时的波浪箭头
					pointLoadX[i] = alLoadEnd.get(i).x;//记录下载按钮初始的x坐标
					pointTypeX[i] = al.get(i).x;//记录类别选项纹理的x坐标
				}
				
				if(page>1)
				{
					float span = (LITTLE_BUTTON_WIDTH*page+100*(page-1))/page;
					DMV_CIRCLE_LOCATION_X -=span;
				}
				
				for(int i=0;i<page;i++)
				{
					alCircle.add(new BN2DObject(TextureManager.getTextures("blackcircle.png"),ShaderManager.getShader(0),
							LITTLE_BUTTON_WIDTH,LITTLE_BUTTON_WIDTH,
							DMV_CIRCLE_LOCATION_X+i*100,EV_CIRCLE_LOCATION_Y));
				}
				changeCircle();
				initBitmap = false;
			}
		}
	}
}
