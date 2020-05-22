package com.bn.thread;
import android.graphics.Bitmap;
import static com.bn.ar.utils.Constant.*;

import com.bn.ar.draw.LoadedObjectVertexNormalTexture;
import com.bn.ar.utils.DownLoaderTask;
import com.bn.ar.utils.MethodUtil;

public class URLConnectionThread extends  Thread{
	private String path;//"package_list.txt"的下载地址
	DownLoaderTask dlt;//下载任务
	public static boolean flag = true;//控制此线程是否继续循环的标志位
	public static boolean init = false;//初始化是否完成的标志位
	String str =null;//txt内容
	public static boolean isWantToUpdata=false;
	public URLConnectionThread(String path){
		this.path = path;
	}
	@Override
	public void run()
	{
		while(true)
		{
			while(flag)
			{
				try
				 {
					if(!init)//如果数据还没有初始化
					{
						//=================txt   数据  start================
						if(isWantToUpdata)
						{
							getTxtFromURL();//下载txt到SD卡
							System.out.println("从网络上下载package_list.txt");
						}else{
							getTxtFromSD();
							System.out.println("从SD卡中package_list.txt");
						}
						
						initData();
						//=================txt   数据  end================
						
						//=================bitmap   数据  start================
						String[] bitmapNameYellow= new String[typeNumber];//用于存放类别纹理的名称			
						String[] bitmapNameGray= new String[typeNumber];//用于存放类别纹理的名称
						for(int i=0;i<typeNumber;i++)
						{
							String urlYellow = ListArray[i*length+2];
							String[] contentY = urlYellow.split("/");//对字符串进行切分
							bitmapNameYellow[i] = contentY[contentY.length-1];//获取黄色纹理名称
							String urlGray = ListArray[i*length+5];
							String[] contentG = urlGray.split("/");//对字符串进行切分
							bitmapNameGray[i] = contentG[contentG.length-1];//获取灰色纹理名称
						}
						//判断所需要的bitmap是否存储在手机SD卡上
						boolean isExistBitmap = isExistsInFile(bitmapNameYellow,YELLOW_PIC_LOAD_PATH)
						                                            &&isExistsInFile(bitmapNameGray,GRAY_PIC_LOAD_PATH);
						if(isExistBitmap){//SD卡存在bitmap
							getBitmapFromSD();//在SD卡上取bitmap
						}else{//下载bitmap
							getBitmapFromURL();//下载bitmap到SD卡
						}
						//=================bitmap   数据  end===============						
						 init = true;//初始化成功
						 flag = false;//线程停止
					}
					
					//进入了数据包管理界面==================start======================
					if(init&&flag&&selectDataIndex[selectData])
					{
						System.out.println("next thread=============");
						//下载选中模型的zip数据
						dlt = new DownLoaderTask(ListArray[selectData*length+3],ZIP_LOAD_PATH,selectData,ZIP_FLAG);
						dlt.execute();
						flag = false;
					}
					//进入了数据包管理界面==================end======================
					
				 }
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
			
			try{
				Thread.sleep(10);
			}catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	private void initData()
	{
		//初始化工作
		typeBitmaps=new Bitmap[typeNumber];//类别图,表示本类已经下载
		typeBitmapsNotLoad = new Bitmap[typeNumber];//类别图,表示本类还未下载
		typeTexId = new int[typeNumber];//类别纹理id,表示已经下载
		typeTexIdNotLoad =  new int[typeNumber];//类别纹理id,表示本类还未下载
		types = new int[typeNumber];//类型编号
		selectDataIndex = new boolean[typeNumber];//表示选中了第几类模型
		downLoadZipChangeT = new boolean[typeNumber];//*****************************
		downLoadZipFinish  = new boolean[typeNumber];//表示模型是否初始化完成	
		
		countTypeObj = new int[typeNumber];//每个类别中物体模型的个数
		mTextures = new int[typeNumber][];//分子模型纹理列表
		
		zipNameArray=new String[typeNumber];//存放zip包的名字
		ContentList=new String[typeNumber][];
		LoadedObj=new LoadedObjectVertexNormalTexture[typeNumber][];
		nameList=new String[typeNumber][];
		 for(int i=0;i<typeNumber;i++){
			 selectDataIndex[i] = false;//把所有的类别的模型都设置为未选中
			 types[i] = i;//初始化模型编号
			 String url = ListArray[i*length+3];                        
			 String[] content = url.split("/");
			 String zipName = content[content.length-1].substring(0, content[content.length-1].length()-4);
			 zipNameArray[i]=zipName;
			 System.out.println("zipName:"+zipName);
			 boolean isExistZIP = MethodUtil.isExistFile(zipName,UNZIP_TO_PATH);//判断zip解压的数据包是否存在
			 downLoadZipFinish[i] = isExistZIP;
			 downLoadZipChangeT[i] = isExistZIP;
			 countTypeObj[i] =  Integer.parseInt(ListArray[i*length+4]);//初始化每个类别的模型个数
		 }		
		 
	}
	//从指定URL读取"package_list.txt"文件
	private void getTxtFromURL()
	{	
		ListArray = MethodUtil.getStringInfoFromURL(path).split("\\|");//对"package_list.txt"中的数据进行切分
		System.out.println("得到切分后的数据");
		typeNumber = ListArray.length/length;//得到模型类别的数量
		if(typeNumber>2){currentNumber=typeNumber;}
		dlt = new DownLoaderTask(path,UNZIP_TO_PATH,-1,TXT_FLAG);//下载"package_list.txt"文件
		dlt.execute();
		
	}
	//从SD卡读取txt文件
	private void getTxtFromSD()
	{
		str = MethodUtil.loadFromFile("package_list.txt");//在文件清单中读取所有内容
		ListArray = str.trim().split("\\|");//将读取的内容以|进行切分				
		typeNumber = ListArray.length/length;//类别个数
		if(typeNumber>2){currentNumber=typeNumber;}
	}
	
	//从指定URL中读取Bitmap
	private void getBitmapFromURL()
	{
		 for(int i=0;i<typeNumber;i++)
		 {
			 dlt = new DownLoaderTask(ListArray[i*length+2],YELLOW_PIC_LOAD_PATH,i,PIC_FLAG);
			 dlt.execute();//下载黄色图
			 dlt = new DownLoaderTask(ListArray[i*length+5],GRAY_PIC_LOAD_PATH,i,PIC_FLAG);
			 dlt.execute();//下载灰色图
			 //从指定URL读取黄色bitmap
			 typeBitmaps[i] = MethodUtil.getBitmapFromURL(ListArray[i*length+2]);
			 //从指定URL读取灰色bitmap
			 typeBitmapsNotLoad[i] = MethodUtil.getBitmapFromURL(ListArray[i*length+5]);
		 }
	}
	//从SD卡读取类别纹理
	private void getBitmapFromSD()
	{
		//=====================用于测试多张图片的代码  start==================
//		typeBitmaps = MethodUtil.getBitmapsFromSDCard(YELLOW_PIC_LOAD_PATH);
//		typeBitmapsNotLoad = MethodUtil.getBitmapsFromSDCard(GRAY_PIC_LOAD_PATH);
//		
//		System.out.println("-----sd   bitmap  typeBitmaps-----:"+typeBitmaps.length);
		//=====================用于测试多张图片的代码  end===================
		 for(int i=0;i<typeNumber;i++)
		 {
			 String url = ListArray[i*length+2];
			 String[] content = url.split("/");
			 String zipName = content[content.length-1];//获取黄色纹理名称
			 //从指定的SD卡目录中获取黄色图片
			 typeBitmaps[i] = MethodUtil.getBitmapFromSDCard(zipName,YELLOW_PIC_LOAD_PATH);
			 
			 url = ListArray[i*length+5];
			 content = url.split("/");
			 zipName = content[content.length-1];//获取灰色纹理名称
			 //从指定的SD卡目录中获取灰色图片
			 typeBitmapsNotLoad[i] = MethodUtil.getBitmapFromSDCard(zipName,GRAY_PIC_LOAD_PATH);
		 }
		 System.out.println("-----sd   bitmap  -----:");
	}
	
//	private boolean isExistInFile(String fileName,String path)//判断指定的文件下是否存在需要查找的单个文件
//	{
//		boolean isExist = MethodUtil.isExistFile(fileName,path);
//		return isExist;
//	}

	
	private boolean isExistsInFile(String[] fileNames,String path)//判断指定的文件下是否存在需要查找的多个文件
	{
		boolean isExist = true;
		for(int i=0;i<fileNames.length;i++)
		{
			if(!MethodUtil.isExistFile(fileNames[i],path))
			{
				isExist = false;
				break;
			}
		}
		return isExist;
	}
	
}
