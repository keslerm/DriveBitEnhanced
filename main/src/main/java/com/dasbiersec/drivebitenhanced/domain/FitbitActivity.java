package com.dasbiersec.drivebitenhanced.domain;

import org.joda.time.DateTime;

public class FitbitActivity
{
	public String activityId = "16010";
	public DateTime startTime;
	public Integer duration;

	public FitbitActivity (DateTime startTime, Integer duration)
	{
		this.startTime = startTime;
		this.duration = duration;
	}

	public String getActivityId()
	{
		return activityId;
	}

	public void setActivityId(String activityId)
	{
		this.activityId = activityId;
	}

	public DateTime getStartTime()
	{
		return startTime;
	}

	public void setStartTime(DateTime startTime)
	{
		this.startTime = startTime;
	}

	public Integer getDuration()
	{
		return duration;
	}

	public void setDuration(Integer duration)
	{
		this.duration = duration;
	}


	public String getFitbitStartTime()
	{
		// Subtract the duration
		DateTime time = startTime.minusMillis(duration);

		return time.toString("HH:mm");
	}

	public String getFitbitStartDate()
	{
		// Subtract the duration
		DateTime time = startTime.minusMillis(duration);

		return time.toString("yyyy-MM-dd");
	}
}
