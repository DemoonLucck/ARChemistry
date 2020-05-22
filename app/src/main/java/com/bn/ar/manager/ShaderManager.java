package com.bn.ar.manager;

import java.util.HashMap;

import com.bn.ar.utils.ShaderUtil;
import com.bn.ar.views.GlSurfaceView;

import android.annotation.SuppressLint;

@SuppressLint("UseSparseArrays")
public class ShaderManager 
{
	public static String[][] programs={
		{"vertex_rect.sh","frag_rect.sh"},//0
		{"vertex.sh","frag.sh"},//1
		{"vertex_background.sh","frag_background.sh"},//2
		{"vertex_load2d.sh","frag_load2d.sh"},//3
		{"vertex_loading.sh","frag_loading.sh"},//4
		};//所有着色器的名称
	static HashMap<Integer,Integer> list=new HashMap<Integer,Integer>();
	public static void loadingShader(GlSurfaceView mv,int start,int num)//加载着色器
	{
		for(int i=start;i<start+num;i++)
		{
			//加载顶点着色器的脚本内容
			String mVertexShader=ShaderUtil.loadFromAssetsFile(programs[i][0], mv.getResources());
			//加载片元着色器的脚本内容
			String mFragmentShader=ShaderUtil.loadFromAssetsFile(programs[i][1],mv.getResources());
			//基于顶点着色器与片元着色器创建程序
			int mProgram = ShaderUtil.createProgram(mVertexShader, mFragmentShader);
			list.put(i, mProgram);
		}  
	}
	public static int getShader(int index)//获得某套程序id
	{
		int result=0;
		if(list.get(index)!=null)//如果列表中有此套程序
		{
			result=list.get(index);
		}
		return result;
	}
}
