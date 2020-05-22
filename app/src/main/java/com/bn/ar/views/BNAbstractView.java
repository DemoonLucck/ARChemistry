package com.bn.ar.views;
import android.view.MotionEvent;

public abstract class BNAbstractView 
{
	public abstract boolean onTouchEvent(MotionEvent e);
	public abstract void onSurfaceCreated();
	public abstract void onDrawFrame();
}