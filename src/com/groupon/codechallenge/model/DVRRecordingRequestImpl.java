package com.groupon.codechallenge.model;

import java.util.Date;

class DVRRecordingRequestImpl implements DVRRecordingRequest {

	private Date startTime;
	private Date endTime;
	private int channel;
	private int tunerId;
	private User user;

	DVRRecordingRequestImpl() {

	}

	DVRRecordingRequestImpl(DVRRecordingRequestImpl t) {
		this.startTime = t.startTime;
		this.endTime = t.endTime;
		this.channel = t.channel;
		this.tunerId = t.tunerId;
		this.user = t.user;
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
	public int getChannel() {
		return channel;
	}

	@Override
	public long getDuration() {
		return (endTime.getTime() - startTime.getTime()) / 1000;
	}

	@Override
	public int getTunerId() {
		return tunerId;
	}

	@Override
	public void setTunerId(int id) {
		this.tunerId = id;
	}

	void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	void setChannel(int channel) {
		this.channel = channel;
	}

	void setUser(User user) {
		this.user = user;
	}

	boolean isValidRequest() {
		if (getDuration() >= ModelFactory.getInstance().getMinimumRecordDuration()) {
			return true;
		}
		return false;
	}

	@Override
	public User getUser() {
		return user;
	}
}
