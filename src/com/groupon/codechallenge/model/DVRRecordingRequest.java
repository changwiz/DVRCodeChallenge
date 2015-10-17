package com.groupon.codechallenge.model;


public interface DVRRecordingRequest extends DVRTimeSpan {

	public int getChannel();

	public int getTunerId();

	public void setTunerId(int id);
	
	public User getUser();
}
