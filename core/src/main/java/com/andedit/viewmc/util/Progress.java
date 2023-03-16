package com.andedit.viewmc.util;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Null;

public class Progress {
	
	private volatile String status = "Initializing";
	private volatile float progress;
	
	private volatile int totalTask;
	private volatile int taskFinished;
	
	private volatile int totalStep = 1;
	private volatile int stepFinished;
	
	private volatile double pre;
	
	public void newProgess(int totalTask) {
		this.totalTask = totalTask;
		taskFinished = -1;
		totalStep = 1;
		stepFinished = 0;
		
		pre = (1d-(double)((totalTask-1) / (double)totalTask));
	}
	
	public void newStep(int totalStep) {
		this.taskFinished = Math.min(taskFinished+1, totalTask);
		this.totalStep = totalStep;
		stepFinished = 0;
	}
	
	public void incStep() {
		incStep(1);
	}
	
	public void incStep(int inc) {
		stepFinished = Math.min(stepFinished+inc, totalStep);
		update();
	}
	
	private void update() {
		double newProgress = taskFinished / (double)totalTask;
		newProgress += (stepFinished / (double)totalStep) * pre;
		progress = (float)newProgress;
	}
	
	public void setStatus(@Null Object obj) {
		status = String.valueOf(obj);
	}
	
	public String getStatus() {
		return status;
	}
	
	public float getProgress() {
		return MathUtils.clamp(progress, 0f, 1f);
	}
}
