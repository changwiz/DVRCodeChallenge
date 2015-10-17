package com.groupon.codechallenge;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

import com.groupon.codechallenge.model.User;

public class UserInputHelper {
	public enum UserState {
		ChooseCommand, SelectChannel, SelectRecordDate, Recurrence, SelectQueryDate
	}

	private User user;
	private LinkedList<UserState> stack = new LinkedList<UserState>();

	public UserInputHelper(User user) {
		super();
		this.user = user;
	}

	public void readRecordingCommandsFromFile(String path) {
		File file = new File(path);
		InputStream is;
		try {
			is = new FileInputStream(file);
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String input = null;
			while ((input = br.readLine()) != null) {
				String[] str = input.split(";");
				if (str.length == 3) {
					int channel = parseInt(str[0]);
					String[] strs = str[1].split("to");
					if (strs.length != 2) {
						br.close();
						return;
					}
					Date startTime = parseTime(strs[0]);
					Date endTime = parseTime(strs[1]);
					int recurrence = parseInt(str[2]);
					user.scheduleRecording(startTime, endTime, channel, recurrence);
				}
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void readQueryCommandsFromFile(String path) {
		File file = new File(path);
		InputStream is;
		try {
			is = new FileInputStream(file);
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String input = null;
			while ((input = br.readLine()) != null) {
				String[] str = input.split(";");
				if (str.length > 0) {
					Date date = parseTime(str[0]);
					user.showPendingRequests(date);
				}
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private Date parseTime(String s) {
		SimpleDateFormat format = new SimpleDateFormat("MM/dd/yy kk:mm:ss");
		Date date = null;
		try {
			date = format.parse(s);
		} catch (Exception e) {
			System.out.println("Unrecognized format!");
		}
		return date;
	}

	public void waitForConsoleInput() {
		System.out.println("You can type 'return' to go back to previous state!");
		System.out.println("Press 1 to schedule a recording event, press 2 to query a scheduled record, press 3 to show all pending requests");
		stack.push(UserState.ChooseCommand);
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		String input;
		int channel = -1;
		Date startTime = null;
		Date endTime = null;

		try {
			while ((input = br.readLine()) != null) {
				if (input.equalsIgnoreCase("return")) {
					if (stack.isEmpty() || stack.peek() == UserState.ChooseCommand) {
						break;
					} else {
						stack.pop();
						System.out.println("current state:" + stack.peek().toString());
						continue;
					}
				}

				switch (stack.peek()) {
				case ChooseCommand:
					if (input.equals("1")) {
						stack.push(UserState.SelectChannel);
						System.out.println("Select a channel");
					} else if (input.equals("2")) {
						stack.push(UserState.SelectQueryDate);
						System.out.println("Input query time in following format ONLY: MM/dd/yy kk:mm:ss");
					} else if (input.equalsIgnoreCase("3")) {
						user.showAllPendingRequests();
					}
					break;
				case SelectChannel:
					channel = parseInt(input);
					if (channel != -1) {
						stack.push(UserState.SelectRecordDate);
						System.out.println("Input record start and end time in following format ONLY: MM/dd/yy kk:mm:ss to MM/dd/yy kk:mm:ss");
					}
					break;
				case SelectRecordDate:
					String[] strs = input.split("to");
					if (strs.length != 2) {
						continue;
					}
					startTime = parseTime(strs[0]);
					endTime = parseTime(strs[1]);
					if(startTime != null && endTime != null) {
						stack.push(UserState.Recurrence);
						System.out.println("Input number of daily recurrence of this request(0 to "+Integer.MAX_VALUE+"):");
					}
					break;
				case SelectQueryDate:
					Date time = parseTime(input);
					if (time != null) {
						user.showPendingRequests(time);
						stack.pop();
						System.out.println("Press 1 to schedule a recording event, press 2 to query a scheduled record, press 3 to show all pending requests");
					}
					break;
				case Recurrence:
					int num = 0;
					if(input.length() != 0) {
						num = parseInt(input);
					}
					boolean isSuccess = user.scheduleRecording(startTime, endTime, channel, num);
					if (isSuccess) {
						stack.pop();
						stack.pop();
						stack.pop();
						System.out.println("Press 1 to schedule a recording event, press 2 to query a scheduled record, press 3 to show all pending requests");
					}
					break;
				}

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private int parseInt(String s) {
		int channel = -1;
		try {
			channel = Integer.parseInt(s);
		} catch (Exception e) {
			System.out.println("Unrecognized format!");
		}
		return channel;
	}
}
