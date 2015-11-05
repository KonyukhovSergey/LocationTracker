package ru.serjik.tracker;

import ru.serjik.tracker.LocationCatcher.LocationCatcherListener;
import ru.serjik.tracker.MotionDetector.MotionDetectorListener;
import ru.serjik.tracker.WakeUpSheduler.WakeUpShedulerListener;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class TrackerService extends Service
{
	private static final String TAG = TrackerService.class.getName();

	public static final int MOTION_POLL_DELAY = 60000;
	public static final float MOTION_THRESOLD = 0.5f;
	public static final int LOCATION_CATCH_TIMEOUT = 30000;
	public static final int LOCATION_POLL_DELAY = 5000;

	private TrackerServiceBinder binder = new TrackerServiceBinder();

	private WakeUpSheduler wakeUpSheduler;
	private MotionDetector motionDetector;
	private LocationCatcher locationCatcher;

	@Override
	public IBinder onBind(Intent intent)
	{
		return binder;
	}

	public class TrackerServiceBinder extends Binder
	{
		public TrackerService getServiceInstance()
		{
			return TrackerService.this;
		}
	}

	@Override
	public void onCreate()
	{
		super.onCreate();

		wakeUpSheduler = new WakeUpSheduler(this);
		motionDetector = new MotionDetector(this, MOTION_THRESOLD);
		locationCatcher = new LocationCatcher(this, motionDetector);

		wakeUpSheduler.sheduleWakeUp(2000, wakeUpShedulerListener);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		return START_STICKY;
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
	}

	private WakeUpShedulerListener wakeUpShedulerListener = new WakeUpShedulerListener()
	{
		@Override
		public void onWakeUp()
		{
			Log.i(TAG, "onWakeUp");
			motionDetector.beginMotionDetect(motionDetectorListener);
		}
	};

	private LocationCatcherListener locationCatcherListener = new LocationCatcherListener()
	{
		@Override
		public void onLocationCatchFinished()
		{
			wakeUpSheduler.sheduleWakeUp(MOTION_POLL_DELAY, wakeUpShedulerListener);
		}
	};

	private MotionDetectorListener motionDetectorListener = new MotionDetectorListener()
	{
		@Override
		public void onMotionDetected()
		{
			locationCatcher.beginLocationCatch(LOCATION_POLL_DELAY, LOCATION_CATCH_TIMEOUT, locationCatcherListener);
		}

		@Override
		public void onStillnesDetected()
		{
			wakeUpSheduler.sheduleWakeUp(MOTION_POLL_DELAY, wakeUpShedulerListener);
		}
	};
}
