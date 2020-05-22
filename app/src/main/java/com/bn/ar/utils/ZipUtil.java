package com.bn.ar.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipUtil {
	//将SD卡某个文件中的zip包解压到指定位置
      /** 
       * 解压缩 
       * @param sZipPathFile 要解压的文件 
       * @param sDestPath 解压到某文件夹 
       * @return 
       */  
      public static ArrayList<String> Ectract(String sZipPathFile, String sDestPath) {
    	  
          ArrayList<String> allFileName = new ArrayList<String>();
          try {
              // 先指定压缩档的位置和档名，建立FileInputStream对象
              FileInputStream fins = new FileInputStream(sZipPathFile);
              // 将fins传入ZipInputStream中
              ZipInputStream zins = new ZipInputStream(fins);  
              ZipEntry ze = null;  
              byte[] ch = new byte[256];  
              while ((ze = zins.getNextEntry()) != null) {  
            	  File zfileTemp = new File(sDestPath); //创建解压目标目录  
            	  if(!zfileTemp.exists())
            	  { //如果目标目录不存在，则创建  
            		  zfileTemp.mkdirs();
            	  }
            	  //在zfileTemp文件下创建名称为ze.getName()的文件目录
                  File zfile = new File(zfileTemp.getPath(),ze.getName());  
                  File fpath = new File(zfile.getParentFile().getPath());  
                  if (ze.isDirectory()){ //判断是否是一个目录实体
                      if (!zfile.exists())  //不存在对应名字的目录
                          zfile.mkdirs();  //创建相应目录
                      zins.closeEntry();  //关闭Entry
                  } else {  
                      if (!fpath.exists())   //如果父目录不存在
                          fpath.mkdirs();  	//创建对应的父目录
                      FileOutputStream fouts = new FileOutputStream(zfile);  
                      int i;  
                      allFileName.add(zfile.getAbsolutePath());  
                      while ((i = zins.read(ch)) != -1)  
                          fouts.write(ch, 0, i);  
                      zins.closeEntry();  
                      fouts.close();  
                  }  
              }  
              fins.close();  
              zins.close();  
          } catch (Exception e) {  
              System.err.println("Extract error:" + e.getMessage());  
          }  
          return allFileName;  
      }  
}
