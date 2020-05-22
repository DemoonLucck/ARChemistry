/*===============================================================================
Copyright (c) 2012-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of QUALCOMM Incorporated, registered in the United States 
and other countries. Trademarks of QUALCOMM Incorporated are used with permission.
===============================================================================*/

package com.qualcomm.vuforia;

//发送错误信息--异常处理
// Used to send back to the activity any error during vuforia processes
public class SampleApplicationException extends Exception {
	private static final long serialVersionUID = 2L;				//序列化时保持版本的兼容性
	public static final int INITIALIZATION_FAILURE = 0;				//初始化Vuforia SDK的过程中失败
	public static final int VUFORIA_ALREADY_INITIALIZATED = 1;		//Vuforia SDK已经初始化
	public static final int TRACKERS_INITIALIZATION_FAILURE = 2;	//跟踪器初始化失败
	public static final int LOADING_TRACKERS_FAILURE = 3;			//加载跟踪器失败
	public static final int UNLOADING_TRACKERS_FAILURE = 4;         //反初始化跟踪器失败
	public static final int TRACKERS_DEINITIALIZATION_FAILURE = 5; 	//跟踪器反初始化失败
	public static final int CAMERA_INITIALIZATION_FAILURE = 6; 		//相机初始化失败
	public static final int SET_FOCUS_MODE_FAILURE = 7;				//对焦失败
	public static final int ACTIVATE_FLASH_FAILURE = 8; 			//开始动画失败

	private int mCode = -1;
	private String mString = "";

	public SampleApplicationException(int code, String description) {
		super(description);
		mCode = code;
		mString = description;
	}

	public int getCode() {
		return mCode;
	}

	public String getString() {
		return mString;
	}
}
