package com.bn.ar.views;
import com.bn.ar.draw.BN2DObject;
import com.bn.ar.draw.LoadedObjectVertexNormalTexture;
import com.bn.ar.draw.ProgressBarDraw;
import com.bn.ar.manager.ShaderManager;
import com.bn.ar.manager.TextureManager;
import com.bn.ar.utils.LoadUtil;
import com.bn.ar.utils.MatrixState2D;
import com.bn.ar.utils.MethodUtil;
import com.bn.thread.URLConnectionThread;

import static com.bn.ar.utils.Constant.*;
import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.view.MotionEvent;

@SuppressLint("NewApi")
public class LoadView extends BNAbstractView{
	ProgressBarDraw loadTop;//加载时的进度条
	BN2DObject back;//背景图
	ProgressBarDraw load;//进度条上面文字的绘制者
	GlSurfaceView mv;//界面控制类引用
	int initIndex=0;//加载步骤的索引
	int initDataIndex= 0;//加载模型的索引
	public static  boolean initBitmap = true;//是否要初始化图片
	
	public LoadView(GlSurfaceView mv)//加载界面的构造器
	{
		this.mv=mv;//传入界面控制类
		onSurfaceCreated();//初始化加载界面
	}
	@Override
	public boolean onTouchEvent(MotionEvent e) {//本界面中没有触控设置
		return false;
	}
	public void initBNDeviceView(int count,int index,int numberOfType){
		if((index-2)>=0&&(index-2)<count){
			initDataIndex = index -2;     //initDataIndex为模型的索引
			index = 2;					  //index为加载步骤
		}
		switch(index)
		{
			case 0:
				break;
			case 1:
				//读取模型obj文件的名字
				String texDevice = MethodUtil.loadFromFile(zipNameArray[numberOfType]+"/objname.txt");
				if(texDevice.equals( "NOTSTRING")){//如果不存在上述文件
					System.out.println("不存在文件："+zipNameArray[numberOfType]+"/objname.txt");
					goToast(mv.activity,NOT_EXIST);//将错误信息显示在屏幕上
					reSetData();//初始化数据
					mv.currView = mv.menuView;//返回主菜单界面
					return;
				}
				nameList[numberOfType] =new String[texDevice.split("\\|").length/2];//所有模型的名字
				LoadedObj[numberOfType] = new LoadedObjectVertexNormalTexture[texDevice.split("\\|").length/2];//３ｄ模型的绘制者
				ContentList[numberOfType] = new String[texDevice.split("\\|").length/2];//讲解内容数据
				for(int i=0;i<nameList[numberOfType].length;i++)
				{
					nameList[numberOfType][i] = texDevice.split("\\|")[i*2+1];//将模型名字存入数组
					//读取仪器模型的讲解数据
					ContentList[numberOfType][i] = MethodUtil.loadFromFile(zipNameArray[numberOfType]+"/readcontent/"+nameList[numberOfType][i]+".txt");	
				}
				if(numberOfType==0)//如果读取仪器模型
				{
					//加载酒精灯灯芯模型
			    	spiritLampLine = LoadUtil.loadFromFile(zipNameArray[numberOfType]+"/obj/SpiritLampLine.obj", mv.getResources());
			    	//加载酒精灯灯盖模型
			        spiritLampTop = LoadUtil.loadFromFile(zipNameArray[numberOfType]+"/obj/SpiritLampTop.obj", mv.getResources());
				}
		        break;
			case 2:
				//加载个模型，并将其存储到模型数组中
				LoadedObj[numberOfType][initDataIndex]= LoadUtil.loadFromFile(zipNameArray[numberOfType]+"/obj/"+nameList[numberOfType][initDataIndex]+".obj", mv.getResources());
				break;
			default:
				isSourceInit[numberOfType]=true;//资源加载完毕
				mv.currView=mv.mainView;//跳转到主体验界面
				break;
		}
	}
	//加载“分子模型加载中...”和“仪器模型加载中...”的两张纹理
	public boolean  initTextures(String path,int id){
		String txtStr = MethodUtil.loadFromFile(path+"/texturename.txt");//在指定文件中读取纹理的名字
		if(txtStr.equals( "NOTSTRING")){//如果读取失败
			System.out.println("不存在文件："+path+"/texturename.txt");//打印错误信息
			return false;
		}
		String[] bitmapName =  txtStr.split("\\|");//将字符串以＂｜＂进行切分
		mTextures[id] = new int[bitmapName.length/2];//新建存储纹理id的数组
		for(int i=0;i<bitmapName.length/2;i++){
			//在解压缩路径中找到纹理图
			Bitmap bitmap = MethodUtil.getBitmapFromSDCard(path+"/texture/"+bitmapName[i*2+1],UNZIP_TO_PATH);
			if(bitmap==null){//如果读取失败
				System.out.println("不存在bitmap："+ bitmapName[i*2+1]);
				return false;
			}			
			mTextures[id][i] = TextureManager.initTexture(bitmap, true);//初始化纹理图，将id存入数组中
		}
		
		return true;
	}
	public void reSetData()//恢复初始数据
	{
		initIndex=0;//重置加载步骤
		initDataIndex=0;//重置加载模型索引
	}
	@Override
	public void onSurfaceCreated() {		
		isSourceInit=new boolean[currentNumber];
		System.out.println("isSourceInit的长度为"+isSourceInit.length);
		back = new BN2DObject(TextureManager.getTextures("bg_back.png"),ShaderManager.getShader(0),
				BACK_WIDTH,BACK_HEIGHT,BACK_LOCATION_X,BACK_LOCATION_Y);//背景		
		loadTop = new ProgressBarDraw(TextureManager.getTextures("load_bottom.png"),//进度条
				TextureManager.getTextures("load_front.png"),ShaderManager.getShader(3),
				PROGRESS_WIDTH,PROGRESS_HEIGHT,PROGRESS_LOCATION_X,PROGRESS_LOCATION_Y);
	}
	public void initTypeData()
	{
		int texIdBottom = 0;
		int texIdTop = 1;

		for (int i = 0; i < types.length; i++) 
		{
			if ((mTextures[i] == null)|| (mTextures[i] != null && mTextures[i].length == 0)) 
			{
				initBitmap = true;
				while (initBitmap) 
				{
					if (URLConnectionThread.init) 
					{
						boolean isSuccess = initTextures(zipNameArray[i], types[i]);// 加载分子模型纹理
						initBitmap = false;
						if (isSuccess) 
						{
							texIdBottom = mTextures[i][2];
							texIdTop = mTextures[i][1];
						}

					}
				}
			}
		}
		
		if(load==null){//“分子模型加载中”或“仪器模型加载中”的绘制者
			load = new ProgressBarDraw(texIdBottom,texIdTop,ShaderManager.getShader(3),
					PROGRESS_WIDTH,WORD_HEIGHT,WORD_LOCATION_X,WORD_LOCATION_Y);
		}

	}
	@Override
	public void onDrawFrame(){
		initTypeData();
		if(!isSourceInit[curType]==true)
		{
			draw(countTypeObj[curType]+3,curType);
		}else{
			mv.currView = mv.mainView;//跳转到主界面
		}
		
	}
	
	public void draw(int span,int index)
	{

		initBNDeviceView(span-3,initIndex,index);//初始化界面资源

		if(initIndex<span){initIndex++;}//图片索引加1
		if(initIndex == span){reSetData();}//恢复变量
		
		
		MatrixState2D.pushMatrix();
		MatrixState2D.translate(back.x, back.y, 0);
		back.drawSelf();//绘制背景
		MatrixState2D.popMatrix();
		
		if(load!=null)
		{
			load.setPositionX(//计算当前进度
					PROGRESS_STRAT_X+initIndex*(PROGRESS_WIDTH/(span-1)),
					PROGRESS_STRAT_X, PROGRESS_WIDTH);
			//绘制文字
			MatrixState2D.pushMatrix();
			MatrixState2D.translate(load.x,load.y,0);
			if(mTextures[index]!=null)
			{
				load.setTexture(mTextures[index][2],mTextures[index][1]);
			}
			load.drawSelf();
			MatrixState2D.popMatrix();
		}
		
		loadTop.setPositionX(//计算当前进度
				PROGRESS_STRAT_X+initIndex*(PROGRESS_WIDTH/(span-1)),
				PROGRESS_STRAT_X, PROGRESS_WIDTH);
		MatrixState2D.pushMatrix();
		MatrixState2D.translate(loadTop.x,loadTop.y,0);
		loadTop.drawSelf();	//绘制进度条
		MatrixState2D.popMatrix();
	}
}
