package com.groupon.codechallenge.model;

import java.io.File;
import java.util.Date;

class DVRRecordingImpl implements DVRRecording {
	// Any member fields related to record, video format, etc
	private String filePath;
	private int channel;
	private long duration;
	private Date startTime;
	private Date endTime;
	private int tunerId;

	DVRRecordingImpl(String path, int channel, Date startTime, Date endTime,
			int tunerId) {
		this.filePath = path;
		this.channel = channel;
		if (endTime != null && startTime != null) {
			this.duration = (endTime.getTime() - startTime.getTime()) / 1000;
		}
		this.startTime = startTime;
		this.endTime = endTime;
		this.tunerId = tunerId;
	}

	@Override
	public long getDuration() {
		return duration;
	}

	@Override
	public long getSize() {
		if (filePath != null && filePath.length() > 0) {
			File file = new File(filePath);
			if (file != null) {
				return file.length();
			}
		}
		return 0;
	}

	@Override
	public File getFile() {
		if (filePath != null && filePath.length() > 0) {
			File file = new File(filePath);
			if (file != null) {
				return file;
			}
		}
		return null;
	}

	@Override
	public int getChannel() {
		return channel;
	}

	@Override
	public Date getStartTime() {
		return startTime;
	}

	@Override
	public Date getEndTime() {
		return endTime;
	}

	@Override
	public int getTunerId() {
		return tunerId;
	}

}
