package com.groupon.codechallenge.model;

import java.util.List;

public interface DVRRequestScheduler {
	public boolean addRequest(DVRRecordingRequest r) throws Exception;

	public List<DVRRecordingRequest> getSchedulesOnTuner(int tunerId);

	public void clearScheduler();
}
