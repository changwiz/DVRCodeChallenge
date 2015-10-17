package com.groupon.codechallenge;

import com.groupon.codechallenge.model.ModelFactory;
import com.groupon.codechallenge.model.User;
import com.groupon.codechallenge.utils.DVRSettings;
/**
 * 
 * @author wentaochang
 *  Features implemented: 
 *	(1)MVC architectural patterns (GUI is not implemented), the skeleton of the project is constructed and can be easily extended
 *	(2)Support two types of inputs: from console or read from file: input.txt or query.txt
 *	File format of input/input2.txt: channel_id;MM/dd/yy kk:mm:ss to MM/dd/yy kk:mm:ss
 *	File format of query.txt: MM/dd/yy kk:mm:ss
 *	(3)The scheduling algorithm supports multiple tuners(multiple queues), the high-level design of the algorithm is described in next paragraph
 *  (4)Support multiple users. Different users can view each others' pending requests, but can't view others' completed recording
 *  (5)Well-defined external APIs for UserAction and DVR
 *  (6)Data model for pending requests and completed recordings, and a basic API to naively simulate time events
 *	
 *	Scheduling algorithm design:
 *	(1) Non-preemptive priority. Already scheduled requests have higher priority over new requests, thus new requests can't take tuner resource from old requests.
 *	(2) Multiple tuner queues. The algorithm locates the queue that is capable of satisfying most of its request. 
 *	(3) Requests to record the same channel will be automatically merged.
 *  (4) If a new request can't be executed in its entirety, the new request will be divided into multiple small requests in order to best utilize the tuner resource. 
 *  	For example, for existing queue: [0,10][15,20][30,40], a new request [10,50] will be divided into [10,15][20,30][40,50]
 *  (5) Our algorithm will always sort the intervals based on their start time (if equal,then end time), and resolves any conflicts if any. The schedule action takes
 *  	NUM_OF_TUNER * O(log(n)) + n time, while query takes O(log(n)).
 *  
 *  For the sake of simplicity, I did not adopt or implement some of the better design.
 *  (1) All actions are executed synchronously. Ideally, query and schedule command should be executed asynchronously.
 *  (2) The list of scheduled tasks should be persisted to disk as well to prevent data lost against system reboot.
 *  (3) Multiple user support is rather rudimentary.
 *  (4) Lack of a better implementation to simulate time events that trigger execution of scheduled requests.
 *	
 */
public class DVRMainProgram {
	public enum InputSource {
		File, Console;
	}
	
	private final static InputSource source = InputSource.File; //InputSource.Console
	
	public static void main(String[] args){
		DVRSettings settings = new DVRSettings();
		settings.MINIMUM_RECORDING_DURATION = 1; //seconds
		settings.NUM_OF_TUNERS = 2;
		
		User user = new User("ann");
		DVR dvr = ModelFactory.getInstance().newDVR(settings);
		
		user.registerDVR(dvr);
		
		UserInputHelper helper = new UserInputHelper(user);
		if(source == InputSource.File) {
			helper.readRecordingCommandsFromFile("./input.txt");
			
			/* Query pending requests given time */
			helper.readQueryCommandsFromFile("./query.txt");
			
			/* Show all pending requests in queue*/
			user.showAllPendingRequests();
			
			
			/* Remove all requests from queue and generate video records, I use this method to simulate the time events */
			//System.out.println("");
			//System.out.println("Recording.......");
			//System.out.println("All recording requests are executed!");
			//System.out.println("");
			//dvr.executeAllRequest();
			
			
			/* User actions that have been implemented */
			//user.showAllRecords();
			//user.showAllPendingRequests();
			//user.queryAll(time);
			//user.queryPendingRequests(time);
			//user.queryRecords(time);
		} else {
			helper.readRecordingCommandsFromFile("./input.txt");
			helper.waitForConsoleInput();
		}
		user.unregisterDVR(dvr);
		
		//User2
		User user2 = new User("bob");
		user2.registerDVR(dvr);
		user2.showAllPendingRequests();
		UserInputHelper helper2 = new UserInputHelper(user2);
		helper2.readRecordingCommandsFromFile("./input2.txt");
		helper2.waitForConsoleInput();
		user2.unregisterDVR(dvr);
	}
}
