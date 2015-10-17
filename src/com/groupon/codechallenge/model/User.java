package com.groupon.codechallenge.model;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.groupon.codechallenge.DVR;
import com.groupon.codechallenge.UserAction;
import com.groupon.codechallenge.utils.Utils;

public class User implements UserAction {

	private DVR dvr;
	private String userName;

	public User(String userName) {
		this.userName = userName;
	}

	@Override
	public boolean scheduleRecording(Date startTime, Date endTime, int channel) {
		return scheduleRecording(startTime, endTime, channel, 0);

	}

	public void registerDVR(DVR dvr) {
		System.out.println(userName + " is connected.");
		this.dvr = dvr;
	}

	public void unregisterDVR(DVR dvr) {
		System.out.println(userName + " is disconnected.");
		if (this.dvr == dvr) {
			this.dvr = null;
		}
	}

	@Override
	public void showAllCompletedRecordings() {
		if (dvr != null) {
			List<DVRRecording> records = dvr.getAllCompletedRecordings(this);
			if (records != null && records.size() > 0) {
				System.out.println("All completed video recordings:");
				for (DVRRecording r : records) {
					System.out.println("Channel: " + r.getChannel() + "Start:" + Utils.printDate(r.getStartTime()) + " End:" + Utils.printDate(r.getEndTime())
							+ " Tuner:" + r.getTunerId());
				}
			}

		}
	}

	@Override
	public void showAllPendingRequests() {
		if (dvr != null) {
			List<DVRRecordingRequest> requests = dvr.getAllPendingRequests();
			if (requests != null && requests.size() > 0) {
				System.out.println("All pending recording requests:");
				for (DVRRecordingRequest r : requests) {
					System.out.println("User:" + r.getUser().getUserName() + " Channel:" + r.getChannel() + " Tuner:" + r.getTunerId() + " Start:"
							+ Utils.printDate(r.getStartTime()) + " End:" + Utils.printDate(r.getEndTime()));
				}
			}

		}
	}

	@Override
	public void showCompletedRecordings(Date time) {
		if (dvr != null) {
			List<DVRRecording> list = dvr.queryCompletedRecordings(this, time);
			UserQueryResponse response = ModelFactory.getInstance().newUserQueryResponse(list);
			System.out.println("Completed video recordings at " + Utils.printDate(time) + ":");
			if (list != null && list.size() > 0) {
				for (DVRRecording r : list) {
					System.out.println("Channel: " + r.getChannel() + "Start:" + Utils.printDate(r.getStartTime()) + " End:" + Utils.printDate(r.getEndTime())
							+ " Tuner:" + r.getTunerId());
				}
			} else {
				System.out.println(response.toString());
			}
		}
	}

	@Override
	public void showPendingRequests(Date time) {
		if (dvr != null) {
			List<DVRRecordingRequest> requests = dvr.queryPendingRequests(time);
			System.out.println("Pending recording requests at " + Utils.printDate(time) + ":");
			if (requests != null && requests.size() > 0) {
				for (DVRRecordingRequest r : requests) {
					System.out.println("User:" + r.getUser().getUserName() + " Channel:" + r.getChannel() + " Tuner:" + r.getTunerId() + " Start:"
							+ Utils.printDate(r.getStartTime()) + " End:" + Utils.printDate(r.getEndTime()));
				}
			} else {
				System.out.println("No pending requests");
			}

		}
	}

	@Override
	public void showRequestsAndRecordings(Date time) {
		System.out.println("");
		showPendingRequests(time);
		showCompletedRecordings(time);
	}

	public String getUserName() {
		return userName;
	}

	@Override
	public boolean scheduleRecording(Date startTime, Date endTime, int channel, int recurrence) {
		if (startTime == null || endTime == null || channel == -1 || !startTime.before(endTime)) {
			return false;
		}
		if (dvr != null) {
			dvr.scheduleRecordings(this, startTime, endTime, channel);
			Calendar calendar = Calendar.getInstance();
			for (int i = 1; i <= recurrence; ++i) {
				calendar.setTime(startTime);
				calendar.add(Calendar.DATE, i);
				Date s = calendar.getTime();

				calendar.setTime(endTime);
				calendar.add(Calendar.DATE, i);
				Date e = calendar.getTime();
				dvr.scheduleRecordings(this, s, e, channel);
			}
			return true;
		} else {
			return false;
		}
	}
}
