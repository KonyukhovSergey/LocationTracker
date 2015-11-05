package ru.serjik.tracker;

public class TrackerConfiguration
{
	public final int motionPollDelay;
	public final float motionThresold;
	public final int locationCatchTimeOut;
	public final int locationPollDelay;

	public TrackerConfiguration(int motionPollDelay, float motionThresold, int locationCatchTimeOut,
			int locationPollDelay)
	{
		this.motionPollDelay = motionPollDelay;
		this.motionThresold = motionThresold;
		this.locationCatchTimeOut = locationCatchTimeOut;
		this.locationPollDelay = locationPollDelay;
	}

	public TrackerConfiguration()
	{
		this(30000, 0.5f, 30000, 5000);
	}

}
