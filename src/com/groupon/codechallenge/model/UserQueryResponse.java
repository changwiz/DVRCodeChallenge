package com.groupon.codechallenge.model;

import java.util.List;

public interface UserQueryResponse {
	public enum DVRAction {
		Record("Recording"), Idle("Not Recording");
		private String message;

		DVRAction(String message) {
			this.message = message;
		}

		public String getMessage() {
			return message;
		}
	}

	public List<DVRRecording> getRecords();

	public DVRAction getDVRAction();
}
