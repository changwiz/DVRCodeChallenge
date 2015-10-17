package com.groupon.codechallenge.model;

import java.util.List;

class UserQueryResponseImpl implements UserQueryResponse {
	private List<DVRRecording> records;

	@Override
	public DVRAction getDVRAction() {
		if (records == null || records.size() == 0) {
			return DVRAction.Idle;
		}
		return DVRAction.Record;
	}

	@Override
	public List<DVRRecording> getRecords() {
		return records;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		if (records != null && records.size() > 0) {
			for (DVRRecording r : records) {
				builder.append(getDVRAction().getMessage() + " "
						+ r.getChannel() + " on Tuner " + r.getTunerId() +"\n");
			}
		} else {
			builder.append(getDVRAction().getMessage());
		}
		return builder.toString();

	}
	
	void setRecords(List<DVRRecording> records) {
		this.records = records;
	}

}
