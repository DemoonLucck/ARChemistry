/*===============================================================================
Copyright (c) 2012-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of QUALCOMM Incorporated, registered in the United States 
and other countries. Trademarks of QUALCOMM Incorporated are used with permission.
===============================================================================*/

package com.qualcomm.vuforia;

import com.qualcomm.vuforia.State;
//用在activity中对vuforia进行操作的接口
//  Interface to be implemented by the activity which uses SampleApplicationSession
public interface SampleApplicationControl
{
    
    // To be called to initialize the trackers
    boolean doInitTrackers();//初始化追踪器
    
    
    // To be called to load the trackers' data
    boolean doLoadTrackersData();//加载追踪器
    
    
    // To be called to start tracking with the initialized trackers and their
    // loaded data
    boolean doStartTrackers();//启动追踪器
    
    
    // To be called to stop the trackers
    boolean doStopTrackers();//停止追踪器
    
    
    // To be called to destroy the trackers' data
    boolean doUnloadTrackersData();//删除追踪器数据
    
    
    // To be called to deinitialize the trackers
    boolean doDeinitTrackers();//取消追踪器的加载
    
    
    // This callback is called after the Vuforia initialization is complete,
    // the trackers are initialized, their data loaded and
    // tracking is ready to start 
    void onInitARDone(SampleApplicationException e);//在Vuforia初始化完毕，追踪器初始化且数据加载完毕，准备好启动时启动
    
    
    // This callback is called every cycle
    void onQCARUpdate(State state);//更新
    
}
