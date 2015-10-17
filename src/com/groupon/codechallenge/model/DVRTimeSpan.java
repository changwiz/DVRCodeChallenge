package com.groupon.codechallenge.model;

import java.util.Date;

public interface DVRTimeSpan {

	public Date getStartTime();

	public Date getEndTime();

	public long getDuration();// seconds
}
