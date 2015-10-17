package com.groupon.codechallenge.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.groupon.codechallenge.DVR;
import com.groupon.codechallenge.utils.DVRSettings;
import com.groupon.codechallenge.utils.Utils;

class DVRImpl implements DVR {
	private DVRRequestScheduler scheduler;
	private DVRSettings settings;
	private HashMap<User, List<DVRRecording>> map = new HashMap<User, List<DVRRecording>>();

	public DVRImpl(DVRSettings settings) {
		this.settings = settings;
		this.scheduler = new DVRRequestSchedulerImpl(settings.NUM_OF_TUNERS);
		ModelFactory.getInstance().setMinimumRecordingDuration(
				settings.MINIMUM_RECORDING_DURATION);
		printoutWelcome();
	}

	private void printoutWelcome() {
		System.out
				.printf("Welcome to Groupon DVR (GUI will be available soon)\nThis DVR has %d tuners\n",
						settings.NUM_OF_TUNERS);
		System.out.println("Please connect your user to DVR....");
	}

	@Override
	public void scheduleRecordings(User user, Date startTime, Date endTime,
			int channel) {
		DVRRecordingRequest request = ModelFactory.getInstance()
				.newDVRRecordRequest(user, startTime, endTime, channel);
		if(request != null) {
			System.out.println("Schedule a recording on channel "
					+ request.getChannel() + " from "
					+ Utils.printDate(request.getStartTime()) + " to "
					+ Utils.printDate(request.getEndTime()));
			
			try {
				scheduler.addRequest(request);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public List<DVRRecording> queryCompletedRecordings(User user, Date time) {
		List<DVRRecording> temp = new ArrayList<DVRRecording>();

		DVRRecordingImpl record = new DVRRecordingImpl(null, -1, time, time, -1);
		int index = Collections.binarySearch(getRecords(user), record,
				DVRRequestSchedulerImpl.comparator);
		if (index < 0) {
			index = -index - 1;
		}
		if (index == 0) {
			return temp;
		}
		for (int i = index - 1; i >= 0; i--) {
			DVRRecording r = getRecords(user).get(i);
			if (r.getStartTime().before(record.getStartTime())
					&& (r.getEndTime().after(record.getStartTime()) || r
							.getEndTime().equals(record.getStartTime()))) {
				temp.add(r);
			}
		}
		return temp;
	}

	@Override
	public List<DVRRecording> getAllCompletedRecordings(User user) {
		return getRecords(user);
	}

	@Override
	public List<DVRRecordingRequest> getAllPendingRequestsOnTuner(int tunerId) {
		return scheduler.getSchedulesOnTuner(tunerId);
	}

	@Override
	public void executeAllRequest() {
		for (int i = 0; i < settings.NUM_OF_TUNERS; ++i) {
			List<DVRRecordingRequest> queue = scheduler.getSchedulesOnTuner(i);
			for (DVRRecordingRequest r : queue) {
				String filePath = r.getTunerId() + r.getStartTime().toString()
						+ r.getChannel();
				DVRRecordingImpl record = new DVRRecordingImpl(filePath,
						r.getChannel(), r.getStartTime(), r.getEndTime(),
						r.getTunerId());
				getRecords(r.getUser()).add(record);
			}
		}
		scheduler.clearScheduler();
		for (List<DVRRecording> records : map.values()) {
			Collections.sort(records, DVRRequestSchedulerImpl.comparator);
		}
	}

	@Override
	public List<DVRRecordingRequest> queryPendingRequests(Date time) {
		List<DVRRecordingRequest> temp = new ArrayList<DVRRecordingRequest>();

		List<DVRRecordingRequest> queue = getAllPendingRequests();
		Collections.sort(queue, DVRRequestSchedulerImpl.comparator);

		DVRRecordingRequestImpl request = new DVRRecordingRequestImpl();
		request.setStartTime(time);
		request.setEndTime(time);
		int index = Collections.binarySearch(queue, request,
				DVRRequestSchedulerImpl.comparator);
		if (index < 0) {
			index = -index - 1;
		}
		if (index == 0) {
			return temp;
		}
		for (int i = index - 1; i >= 0; i--) {
			DVRRecordingRequest r = queue.get(i);
			if (r.getStartTime().before(request.getStartTime())
					&& (r.getEndTime().after(request.getStartTime()) || r
							.getEndTime().equals(request.getStartTime()))) {
				temp.add(r);
			}
		}
		return temp;
	}

	@Override
	public List<DVRRecordingRequest> getAllPendingRequests() {
		List<DVRRecordingRequest> list = new ArrayList<DVRRecordingRequest>();
		for (int i = 0; i < settings.NUM_OF_TUNERS; ++i) {
			list.addAll(getAllPendingRequestsOnTuner(i));
		}
		return list;
	}

	private List<DVRRecording> getRecords(User user) {
		if (map.get(user) == null) {
			List<DVRRecording> records = new ArrayList<DVRRecording>();
			map.put(user, records);
		}
		return map.get(user);
	}

}
