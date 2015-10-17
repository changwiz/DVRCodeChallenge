package com.groupon.codechallenge.model;

import java.util.Date;
import java.util.List;

import com.groupon.codechallenge.DVR;
import com.groupon.codechallenge.utils.DVRSettings;

public class ModelFactory {
	private long duration = 30; //seconds
	private static ModelFactory singleton = new ModelFactory();

	private ModelFactory() {

	}

	public static ModelFactory getInstance() {
		return singleton;
	}

	public DVRRecordingRequest newDVRRecordRequest(User user, Date startTime, Date endTime, int channel) {
		if (startTime != null && endTime != null && startTime.before(endTime) && (endTime.getTime() - startTime.getTime()) / 1000 >= duration) {
			DVRRecordingRequestImpl request = new DVRRecordingRequestImpl();
			request.setStartTime(startTime);
			request.setEndTime(endTime);
			request.setChannel(channel);
			request.setUser(user);
			return request;
		}
		return null;
	}

	public void setMinimumRecordingDuration(long duration) {
		this.duration = duration;
	}

	public long getMinimumRecordDuration() {
		return duration;
	}

	public DVR newDVR(DVRSettings settings) {
		DVRImpl dvr = new DVRImpl(settings);
		return dvr;
	}

	public UserQueryResponse newUserQueryResponse(List<DVRRecording> records) {
		UserQueryResponseImpl response = new UserQueryResponseImpl();
		response.setRecords(records);
		return response;
	}

}
