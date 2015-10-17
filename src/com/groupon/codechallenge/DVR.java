package com.groupon.codechallenge;

import java.util.Date;
import java.util.List;

import com.groupon.codechallenge.model.DVRRecording;
import com.groupon.codechallenge.model.DVRRecordingRequest;
import com.groupon.codechallenge.model.User;

public interface DVR {
	/**
	 * 
	 * @param startTime
	 * @param endTime
	 * @param channel
	 */
	public void scheduleRecordings(User user, Date startTime, Date endTime,
			int channel);

	/**
	 * 
	 * @param time
	 * @return DVR records that have already been executed at given time
	 */
	public List<DVRRecording> queryCompletedRecordings(User user, Date time);

	/**
	 * 
	 * @return all DVR records
	 */
	public List<DVRRecording> getAllCompletedRecordings(User user);

	/**
	 * 
	 * @param tunerId
	 * @return all DVR records executed on tunerId
	 */
	public List<DVRRecordingRequest> getAllPendingRequestsOnTuner(int tunerId);

	/**
	 * 
	 * @param time
	 * @return List of requests that will be executed at given time
	 */
	public List<DVRRecordingRequest> queryPendingRequests(Date time);

	/**
	 * 
	 * @return All pending requests
	 */
	public List<DVRRecordingRequest> getAllPendingRequests();

	/**
	 * FIXME execute all requests in scheduler, this method is for demo purpose
	 */
	public void executeAllRequest();
}
