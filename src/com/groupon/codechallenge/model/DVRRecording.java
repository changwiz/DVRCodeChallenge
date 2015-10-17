package com.groupon.codechallenge.model;

import java.io.File;

public interface DVRRecording extends DVRTimeSpan{

	public long getSize();

	public File getFile();

	public int getChannel();
	
	public int getTunerId();
}
