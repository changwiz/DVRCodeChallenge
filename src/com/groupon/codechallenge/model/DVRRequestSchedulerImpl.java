package com.groupon.codechallenge.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Random;

import com.groupon.codechallenge.utils.Utils;

/**
 * 
 * @author wentaochang The scheduling strategy considers the scenario that
 *         multiple tuners are available. The request scheduling strategy I used
 *         to resolve conflicts is simple and effective. Three principles: (1)
 *         If there'rs no conflicts, which means an idle tuner is available to
 *         handle the recording request in full, the new request can always be
 *         executed. (2) If there're conflicts, the new request will be executed
 *         partially to best utilize available tuners, the already scheduled
 *         requests are not affected. (3) The best available tuner that can
 *         records as more as possible of partial request will be picked by our
 *         algorithm.
 * 
 */
class DVRRequestSchedulerImpl implements DVRRequestScheduler {
	
	private int numOfTuners;
	// Request scheduling pool
	private List<ArrayList<DVRRecordingRequestImpl>> requestPool = new ArrayList<ArrayList<DVRRecordingRequestImpl>>();

	// Comparator used to sort requests by start and end time
	static Comparator<DVRTimeSpan> comparator = new Comparator<DVRTimeSpan>() {
		@Override
		public int compare(DVRTimeSpan o1, DVRTimeSpan o2) {
			if (o1.getStartTime().before(o2.getStartTime())) {
				return -1;
			} else if (o1.getStartTime().after(o2.getStartTime())) {
				return 1;
			} else {
				if (o1.getEndTime().before(o2.getEndTime())) {
					return -1;
				} else if (o1.getEndTime().after(o2.getEndTime())) {
					return 1;
				} else {
					return 0;
				}
			}
		}
	};

	DVRRequestSchedulerImpl() {
		this(1);// Default tuners number is 1

	}

	DVRRequestSchedulerImpl(int num) {
		this.numOfTuners = num;
		// Initialize scheduling pool
		for (int i = 0; i < numOfTuners; ++i) {
			requestPool.add(new ArrayList<DVRRecordingRequestImpl>());
		}
	}

	/**
	 * Add a new request to scheduling pool and resolve all conflicts if any
	 */
	@Override
	public boolean addRequest(DVRRecordingRequest r) throws Exception {
		if (!(r instanceof DVRRecordingRequestImpl)) {
			throw new Exception("Invalid request type!");
		}
		DVRRecordingRequestImpl request = (DVRRecordingRequestImpl) r;

		int selectedTuner = -1;
		long minOverlapLength = Long.MAX_VALUE;

		// randomize the order of queue sequence to balance the workload
		int startQueue = new Random().nextInt(numOfTuners);

		for (int i = 0; i < numOfTuners; ++i) {
			int id = (startQueue + i) % numOfTuners;
			ArrayList<DVRRecordingRequestImpl> queue = requestPool.get(id);
			queue.add(request);
			request.setTunerId(id);

			long length = testForConflicts(queue, request);
			if (length == 0) {// Tuner executes this request without
								// conflicts, thus we don't have to resolve
								// conflicts, return here
				return true;
			}
			queue.remove(request);
			request.setTunerId(-1);

			if (length < minOverlapLength) {// find the tuner queue that has
											// least conflicts
				minOverlapLength = length;
				selectedTuner = id;
			}
		}
		// If control flow falls here, we need to resolve conflicts
		return resolveConflicts(request, selectedTuner);
	}

	// Resolve conflicts. The already scheduled tasks have higher priorities,
	// thus they are not changed; the new task will find any available bandwidth
	// to execute
	/**
	 * 
	 * @param request
	 * @param tunerId
	 * @return true if conflicts are resolved;false otherwise. This method aims
	 *         to resolve conflicts. The rule of thumb is that the earlier
	 *         scheduled requests have higher priorities over the new ones, the
	 *         scheduler will try its best to re-shape the new requests to
	 *         utilize any free queues, cut them into several small requests if
	 *         necessary. Boundary cases: duplicate or overlapped requests to
	 *         record the same channel
	 */
	private boolean resolveConflicts(DVRRecordingRequestImpl request, int tunerId) {
		// The queue is conflict-free from previous resolution.
		ArrayList<DVRRecordingRequestImpl> queue = requestPool.get(tunerId);
		request.setTunerId(tunerId);

		ArrayList<DVRRecordingRequestImpl> res = new ArrayList<DVRRecordingRequestImpl>();
		
		// Index position the new request should be inserted at
		int index = Collections.binarySearch(queue, request, comparator);
		if (index < 0) {
			index = -index - 1;
		}
		// Small adjustment to make sure all elements from 0 to index - 1 have less or equal start time than request
		while (index != queue.size() && Utils.compareDate(queue.get(index).getStartTime(), request.getStartTime()) == 0) {
			index++;
		}
		// Elements from 0 to index - 1 are conflicts free 
		for (int i = 0; i < index; ++i) {
			res.add(queue.get(i));
		}
		
		// Insert at first position. In this case, it definitely has conflicts with element 0 in the queue
		if(index == 0) { 
			IntervalRelation relation = getRelation(request, queue.get(0));
			if(relation == IntervalRelation.Overlap) {
				if(isSameChannel(request, queue.get(0))){
					queue.get(0).setStartTime(request.getStartTime());
					return true;
				} else {
					request.setEndTime(queue.get(0).getStartTime());
					if (request.isValidRequest()) {
						queue.add(0, request);
					}
					return true;
				}
			} else if(relation == IntervalRelation.LeftContain){ //Only these two relationships are possible if they can pass testForConflicts()
				if(isSameChannel(request,queue.get(0))) {
					queue.get(0).setStartTime(request.getStartTime());
					request.setStartTime(queue.get(0).getEndTime());
					index = 1;
				} else {
					DVRRecordingRequestImpl r = new DVRRecordingRequestImpl(request);
					r.setEndTime(queue.get(0).getStartTime());
					request.setStartTime(queue.get(0).getEndTime());
					if (r.isValidRequest()) {
						queue.add(0, r);
						index = 2;
					} else {
						index = 1;
					}
				}
			}
		}
		int i;
		for(i=index-1;i<queue.size() && request.isValidRequest();++i){
			DVRRecordingRequestImpl last = queue.get(i);
			IntervalRelation relation = getRelation(last, request);
			if(relation == IntervalRelation.LeftContain) {
				for(int j = index;j<queue.size();++j){
					res.add(queue.get(j));
				}
				requestPool.set(tunerId, res);
				return true;
			} else if(relation == IntervalRelation.Adjacent) {
				if(isSameChannel(last, request)){
					if(i == queue.size()-1){
						last.setEndTime(request.getEndTime());
						requestPool.set(tunerId, res);
						return true;
					} else {
						last.setEndTime(Utils.minDate(request.getEndTime(), queue.get(i+1).getStartTime()));
						request.setStartTime(queue.get(i+1).getEndTime());
					}
				} else {
					if(i == queue.size()-1){
						res.add(request);
						requestPool.set(tunerId, res);
						return true;
					} else {
						DVRRecordingRequestImpl r = new DVRRecordingRequestImpl(request);
						r.setEndTime(Utils.minDate(request.getEndTime(), queue.get(i+1).getStartTime()));
						if(r.isValidRequest()) {
							res.add(r);
						}
						request.setStartTime(queue.get(i+1).getEndTime());
					}
				}
			} else if(relation == IntervalRelation.Overlap) {
				if(isSameChannel(last, request)){
					if(i == queue.size()-1){
						last.setEndTime(request.getEndTime());
						requestPool.set(tunerId, res);
						return true;
					} else {
						last.setEndTime(Utils.minDate(request.getEndTime(), queue.get(i+1).getStartTime()));
						request.setStartTime(queue.get(i+1).getEndTime());
					}
				} else {
					if(i == queue.size()-1){
						request.setStartTime(last.getEndTime());
						if(request.isValidRequest()) {
							res.add(request);
						}
						requestPool.set(tunerId, res);
						return true;
					} else {
						DVRRecordingRequestImpl r = new DVRRecordingRequestImpl(request);
						r.setStartTime(last.getEndTime());
						r.setEndTime(Utils.minDate(request.getEndTime(), queue.get(i+1).getStartTime()));
						if(r.isValidRequest()) {
							res.add(r);
						}
						request.setStartTime(queue.get(i+1).getEndTime());
					}
				}
			} else {
				if(i == queue.size()-1){
					res.add(request);
					requestPool.set(tunerId, res);
					return true;
				} else {
					DVRRecordingRequestImpl r = new DVRRecordingRequestImpl(request);
					r.setEndTime(Utils.minDate(request.getEndTime(), queue.get(i+1).getStartTime()));
					if(r.isValidRequest()) {
						res.add(r);
					}
					request.setStartTime(queue.get(i+1).getEndTime());
				}
			}
			res.add(queue.get(i+1));
		}
		
		//Request is invalid, we only need to add rest of elements in queue to res
		for(int j = i + 1;j<queue.size();++j){
			res.add(queue.get(j));
		}
		requestPool.set(tunerId, res);
		return true;
		
		/*
		requestPool.set(tunerId, res);
		return true;
		
		if (index == queue.size()) {
			DVRRecordingRequestImpl last = queue.get(index - 1);
			Date startTime = Utils.maxDate(last.getEndTime(), request.getStartTime());
			request.setStartTime(startTime);
			if (startTime.before(request.getEndTime())) {
				queue.add(request);
			}
			return true;
		}
		

		int current;
		for (current = index; current < queue.size() && request.isValidRequest(); ++current) {
			DVRRecordingRequestImpl cur = queue.get(current);
			DVRRecordingRequestImpl last = null;
			if (current != 0) {
				last = queue.get(current - 1);
			}
			// special case: record on the same channel
			if (last != null && last.getChannel() == request.getChannel()) {
				if (request.getStartTime().before(last.getEndTime()) || request.getStartTime().equals(last.getEndTime())) {
					Date endTime = Utils.maxDate(last.getEndTime(), request.getEndTime());
					endTime = Utils.minDate(endTime, cur.getStartTime());
					last.setEndTime(endTime);
					request.setStartTime(cur.getEndTime());
				} else {
					DVRRecordingRequestImpl r = new DVRRecordingRequestImpl();
					r.setStartTime(request.getStartTime());
					r.setEndTime(Utils.minDate(request.getEndTime(), cur.getStartTime()));
					r.setChannel(request.getChannel());
					r.setTunerId(tunerId);
					r.setUser(request.getUser());
					res.add(r);
					request.setStartTime(cur.getEndTime());
				}
				res.add(cur);
			} else if (cur.getChannel() == request.getChannel()
					&& (request.getEndTime().after(cur.getStartTime()) || request.getEndTime().equals(cur.getStartTime()))) {
				Date endTime = Utils.maxDate(cur.getEndTime(), request.getEndTime());
				cur.setStartTime(request.getStartTime());
				cur.setEndTime(endTime);
				res.add(cur);
				request.setStartTime(cur.getEndTime());
			} else {
				Date startTime = last != null ? Utils.maxDate(last.getEndTime(), request.getStartTime()) : request.getStartTime();
				Date endTime = Utils.minDate(cur.getStartTime(), request.getEndTime());
				if (startTime.before(endTime)) {
					DVRRecordingRequestImpl r = new DVRRecordingRequestImpl();
					r.setStartTime(startTime);
					r.setEndTime(endTime);
					r.setChannel(request.getChannel());
					r.setTunerId(tunerId);
					r.setUser(request.getUser());
					res.add(r);
				}
				res.add(cur);

				request.setStartTime(cur.getEndTime());
			}
		}
		if (current != queue.size()) {
			for (int i = current; i < queue.size(); ++i) {
				res.add(queue.get(current));
			}
		}
		requestPool.set(tunerId, res);
		return true;*/
	}

	/**
	 * 
	 * @param queue
	 * @param request
	 * @return total length of overlapped intervals, which is used to measure
	 *         the severity of conflicts
	 */
	private long testForConflicts(ArrayList<DVRRecordingRequestImpl> queue, DVRRecordingRequestImpl request) {
		Collections.sort(queue, comparator);

		ArrayList<DVRRecordingRequestImpl> res = new ArrayList<DVRRecordingRequestImpl>();
		long overlapLength = 0;
		for (int i = 0; i < queue.size(); i++) {
			DVRRecordingRequestImpl cur = new DVRRecordingRequestImpl(queue.get(i));
			if (res.isEmpty()) {
				res.add(cur);
			} else {
				DVRRecordingRequestImpl last = res.get(res.size() - 1);

				// Special case handling: if there're duplicate/overlapped
				// quests to record the same channel on one tuner queue,
				// the algorithm will favor this queue by not incrementing the
				// length of overlapped intervals
				if (last.getChannel() != cur.getChannel()) {
					overlapLength += getOverlapLength(last, cur);
				}
				if (last.getEndTime().after(cur.getStartTime())) {
					Date date = Utils.maxDate(last.getEndTime(), cur.getEndTime());
					last.setEndTime(date);
				} else {
					res.add(cur);
				}
			}
		}
		if (overlapLength == 0) {
			queue.clear();
			queue.addAll(res);
		}
		return overlapLength;
	}

	/**
	 * 
	 * @param a
	 * @param b
	 *            a.startTime <= b.starTime
	 * @return total length of overlapped intervals
	 */
	private long getOverlapLength(DVRRecordingRequest a, DVRRecordingRequest b) {
		long leftBound = Math.max(a.getStartTime().getTime(), b.getStartTime().getTime());
		long rightBound = Math.min(a.getEndTime().getTime(), b.getEndTime().getTime());
		if (rightBound >= leftBound) {
			return rightBound - leftBound;
		}
		return 0;
	}

	/**
	 * @return List of pending requests on selected tuner queue
	 */
	@Override
	public List<DVRRecordingRequest> getSchedulesOnTuner(int tunerId) {
		List<DVRRecordingRequest> temp = new ArrayList<DVRRecordingRequest>();
		if (tunerId >= 0 && tunerId < numOfTuners) {
			ArrayList<DVRRecordingRequestImpl> t = requestPool.get(tunerId);
			for (DVRRecordingRequestImpl i : t) {
				temp.add(new DVRRecordingRequestImpl(i));
			}
		}

		return temp;
	}

	/**
	 * Clear all requests in scheduler
	 */
	@Override
	public void clearScheduler() {
		for (int i = 0; i < numOfTuners; ++i) {
			ArrayList<DVRRecordingRequestImpl> queue = requestPool.get(i);
			queue.clear();
		}
	}
	
	private enum IntervalRelation {
		LeftContain, //[a,b] [c,d] a<=c, c<=b, d<=b
		Overlap, //[a,b] [c,d] a<=c, c<b, b<d
		Seperate, //[a,b] [c,d] a<c, b<c
		Adjacent // [a,b] [c,d] a<c, b==c
	}
	
	private IntervalRelation getRelation(DVRRecordingRequest a, DVRRecordingRequest b) {
		if (Utils.compareDate(a.getEndTime(), b.getStartTime()) == 0 || Utils.compareDate(a.getEndTime(), b.getStartTime()) == -1) {
			return Utils.compareDate(a.getEndTime(), b.getStartTime()) == 0 ? IntervalRelation.Adjacent : IntervalRelation.Seperate;
		} else if (Utils.compareDate(b.getEndTime(), a.getEndTime()) == -1 || Utils.compareDate(b.getEndTime(), a.getEndTime()) == 0) {
			return IntervalRelation.LeftContain;
		} else {
			return IntervalRelation.Overlap;
		}
	}
	
	private boolean isSameChannel(DVRRecordingRequest a, DVRRecordingRequest b) {
		return a.getChannel() == b.getChannel();
	}

}
