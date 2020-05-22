package com.bn.ar.manager;

import java.util.HashMap;

import com.bn.ar.activity.R;
import com.bn.ar.activity.StereoRendering;
import com.bn.ar.utils.Constant;

import android.annotation.SuppressLint;
import android.media.AudioManager;
import android.media.SoundPool;

@SuppressLint("UseSparseArrays")
public class SoundManager
{
	SoundPool sp ;
	HashMap<Integer	,Integer> hm ;
	StereoRendering activity ;
	public SoundManager(StereoRendering activity)
	{
		this.activity = activity  ;
		initSound();
	}
	
	//声音 初始化
	public void initSound()
	{
		sp = new SoundPool
		(4, 
		AudioManager.STREAM_MUSIC, 
		100
		);
		hm = new HashMap<Integer, Integer>(); 
		hm.put(Constant.BUTTON_PRESS, sp.load(activity, R.raw.clickbutton, 1));//点击按钮
	}
	
	public void playMusic(int sound,int loop)
	{
		@SuppressWarnings("static-access")
		AudioManager am = (AudioManager)activity.getSystemService(activity.AUDIO_SERVICE);
		float steamVolumCurrent = am.getStreamVolume(AudioManager.STREAM_MUSIC)  ;
		float steamVolumMax = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)  ;
		float volum = steamVolumCurrent/steamVolumMax  ;
		sp.play(hm.get(sound), volum, volum, 1	, loop, 1)  ;//播放
	}

}

