package com.groupon.codechallenge;

import java.util.Date;



public interface UserAction {
	public void registerDVR(DVR dvr);

	public void unregisterDVR(DVR dvr);

	public boolean scheduleRecording(Date startTime, Date endTime, int channel);
	
	public boolean scheduleRecording(Date startTime, Date endTime, int channel, int recurrence);

	public void showAllCompletedRecordings();

	public void showAllPendingRequests();

	public void showCompletedRecordings(Date time);

	public void showPendingRequests(Date time);

	public void showRequestsAndRecordings(Date time);
}
