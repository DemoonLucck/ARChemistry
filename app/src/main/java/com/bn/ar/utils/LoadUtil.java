package com.bn.ar.utils;//声明包
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import org.apache.http.util.EncodingUtils;
import com.bn.ar.draw.LoadedObjectVertexNormalTexture;
import android.content.res.Resources;
import android.util.Log;

public class LoadUtil 
{		
	//从obj文件中加载携带顶点信息的物体
    public static LoadedObjectVertexNormalTexture loadFromFile
    (String fname, Resources r)
    {
    	//加载后物体的引用
    	LoadedObjectVertexNormalTexture lo=null;
    	//原始顶点坐标列表--直接从obj文件中加载
    	ArrayList<Float> alv=new ArrayList<Float>();
    	//结果顶点坐标列表--按面组织好
    	ArrayList<Float> alvResult=new ArrayList<Float>(); 
    	//原始纹理坐标列表
    	ArrayList<Float> alt=new ArrayList<Float>();  
    	//纹理坐标结果列表
    	ArrayList<Float> altResult=new ArrayList<Float>();  
    	//原始法向量列表
    	ArrayList<Float> aln=new ArrayList<Float>();    
    	//法向量结果列表
    	ArrayList<Float> alnResult=new ArrayList<Float>(); 
    	
    	try
    	{
    		File mFile = new File(Constant.UNZIP_TO_PATH);
    		/* 创建File对象，确定需要读取文件的信息 */
    		File file = new File(Constant.UNZIP_TO_PATH+fname);    		
    		
    		if(!file.exists()||!mFile.exists())
    		{
    			System.out.println(Constant.UNZIP_TO_PATH+"，此文件不存在！");
    			return null;
    		}
    		/* FileInputSteam 输入流的对象， */    
    		FileInputStream fis = new FileInputStream(file);  
    		
    		/* 准备一个字节数组用户装即将读取的数据 */  
    		byte[] buffer = new byte[fis.available()];  
    		
    		/* 开始进行文件的读取 */  
    		fis.read(buffer);            
    		/* 关闭流  */  
    		fis.close();  
    		
    		/* 将字节数组转换成字符串， 并转换编码的格式 */
    		String allString = EncodingUtils.getString(buffer, "UTF-8"); 
    		String[] tempArray = allString.split("\\n");
    		
    		for(String tmp:tempArray)
    		{//遍历每一行
		    	String[] tempsa=tmp.split("[ ]+");//将文本行用空格符切分
		      	if(tempsa[0].trim().equals("v"))
		      	{//此行为顶点坐标行
		      	    //若为顶点坐标行则提取出此顶点的XYZ坐标添加到原始顶点坐标列表中
		      		alv.add(Float.parseFloat(tempsa[1]));
		      		alv.add(Float.parseFloat(tempsa[2]));
		      		alv.add(Float.parseFloat(tempsa[3]));
		      	}
		      	else if(tempsa[0].trim().equals("vt"))
		      	{//此行为纹理坐标行
		      		//若为纹理坐标行则提取ST坐标并添加进原始纹理坐标列表中
		      		alt.add(Float.parseFloat(tempsa[1]));
		      		alt.add(1-Float.parseFloat(tempsa[2])); 
		      	}
		      	else if(tempsa[0].trim().equals("vn"))
		      	{//此行为法向量行
		      		//若为纹理坐标行则提取ST坐标并添加进原始纹理坐标列表中
		      		aln.add(Float.parseFloat(tempsa[1]));//放进aln列表中
		      		aln.add(Float.parseFloat(tempsa[2])); //放进aln列表中
		      		aln.add(Float.parseFloat(tempsa[3])); //放进aln列表中
		      	}
		      	else if(tempsa[0].trim().equals("f")) 
		      	{//此行为三角形面     		
		      	    //=================顶点坐标   start================================
		      		//计算第0个顶点的索引，并获取此顶点的XYZ三个坐标	      		
		      		int index=Integer.parseInt(tempsa[1].split("/")[0])-1;
		      		float x0=alv.get(3*index);
		      		float y0=alv.get(3*index+1);
		      		float z0=alv.get(3*index+2);
		      		alvResult.add(x0);
		      		alvResult.add(y0);
		      		alvResult.add(z0);		
		      		
		      	    //计算第1个顶点的索引，并获取此顶点的XYZ三个坐标	  
		      		index=Integer.parseInt(tempsa[2].split("/")[0])-1;
		      		float x1=alv.get(3*index);
		      		float y1=alv.get(3*index+1);
		      		float z1=alv.get(3*index+2);
		      		alvResult.add(x1);
		      		alvResult.add(y1);
		      		alvResult.add(z1);
		      		
		      	    //计算第2个顶点的索引，并获取此顶点的XYZ三个坐标	
		      		index=Integer.parseInt(tempsa[3].split("/")[0])-1;
		      		float x2=alv.get(3*index);
		      		float y2=alv.get(3*index+1);
		      		float z2=alv.get(3*index+2);
		      		alvResult.add(x2);
		      		alvResult.add(y2); 
		      		alvResult.add(z2);	
		      	    //=================顶点坐标   end================================
		      		
		      		//==============纹理坐标   start===================
		      		//将纹理坐标组织到结果纹理坐标列表中
		      		//第0个顶点的纹理坐标
		      		int indexTex=Integer.parseInt(tempsa[1].split("/")[1])-1;
		      		altResult.add(alt.get(indexTex*2));
		      		altResult.add(alt.get(indexTex*2+1));
		      	    //第1个顶点的纹理坐标
		      		indexTex=Integer.parseInt(tempsa[2].split("/")[1])-1;
		      		altResult.add(alt.get(indexTex*2));
		      		altResult.add(alt.get(indexTex*2+1));
		      	    //第2个顶点的纹理坐标
		      		indexTex=Integer.parseInt(tempsa[3].split("/")[1])-1;
		      		altResult.add(alt.get(indexTex*2));
		      		altResult.add(alt.get(indexTex*2+1));		
		      		//==============纹理坐标   end===================
		      		
		      	    //=================法向量   start================================
		      		//计算第0个顶点的法向量索引
		      		int indexN=Integer.parseInt(tempsa[1].split("/")[2])-1;//获取法向量编号
		      		float nx0=aln.get(3*indexN);//获取法向量的x值
		      		float ny0=aln.get(3*indexN+1);//获取法向量的y值
		      		float nz0=aln.get(3*indexN+2);//获取法向量的z值
		      		alnResult.add(nx0);//放入alnResult列表
		      		alnResult.add(ny0);//放入alnResult列表
		      		alnResult.add(nz0);	//放入alnResult列表	
		      		
		      	    //计算第1个顶点的索引，并获取此顶点的XYZ三个坐标	  
		      		indexN=Integer.parseInt(tempsa[2].split("/")[2])-1;
		      		float nx1=aln.get(3*indexN);
		      		float ny1=aln.get(3*indexN+1);
		      		float nz1=aln.get(3*indexN+2);
		      		alnResult.add(nx1);
		      		alnResult.add(ny1);
		      		alnResult.add(nz1);
		      		
		      	    //计算第2个顶点的索引，并获取此顶点的XYZ三个坐标	
		      		indexN=Integer.parseInt(tempsa[3].split("/")[2])-1;
		      		float nx2=aln.get(3*indexN);
		      		float ny2=aln.get(3*indexN+1);
		      		float nz2=aln.get(3*indexN+2);
		      		alnResult.add(nx2);
		      		alnResult.add(ny2); 
		      		alnResult.add(nz2);	
		      	    //=================法向量   end================================
		      	}		
    		}

		    //生成顶点数组
		    int size=alvResult.size();
		    float[] vXYZ=new float[size];
		    for(int i=0;i<size;i++)
		    {
		    	vXYZ[i]=alvResult.get(i);
		    } 
		    
		    //生成纹理数组
		    size=altResult.size();
		    float[] tST=new float[size];
		    for(int i=0;i<size;i++)
		    {
		    	tST[i]=altResult.get(i);
		    }
		    
		    //生成法向量数组
		    size=alnResult.size();//获取法向量列表的大小
		    float[] nXYZ=new float[size];//创建存放法向量的数组
		    for(int i=0;i<size;i++)
		    {
		    	nXYZ[i]=alnResult.get(i);//将法向量值存入数组
		    }
		   
		    //创建加载物体对象
		    lo=new LoadedObjectVertexNormalTexture(vXYZ,nXYZ,tST);
    	}
    	catch(Exception e)
    	{
    		Log.d("load error", "load error");
    		e.printStackTrace();
    	}    	
    	return lo;//返回创建的物体对象的引用
    }
}
