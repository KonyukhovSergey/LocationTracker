package ru.serjik.tracker;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;

import ru.serjik.tracker.LocationCatcher.LocationCatcherListener;
import ru.serjik.tracker.MotionDetector.MotionDetectorListener;
import ru.serjik.tracker.WakeUpSheduler.WakeUpShedulerListener;
import ru.serjik.tracker.data.PositionRecord;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class TrackerService extends Service
{
	private static final String TAG = TrackerService.class.getName();

	private TrackerServiceBinder binder = new TrackerServiceBinder();

	private TrackerConfiguration trackerConfiguration;

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

		trackerConfiguration = new TrackerConfiguration();

		wakeUpSheduler = new WakeUpSheduler(this);
		motionDetector = new MotionDetector(this, trackerConfiguration.motionThresold);
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
			wakeUpSheduler.sheduleWakeUp(trackerConfiguration.motionPollDelay, wakeUpShedulerListener);
		}

		@Override
		public void onLocationCatched(Location location)
		{
			storeLocation(location);
		}
	};

	private MotionDetectorListener motionDetectorListener = new MotionDetectorListener()
	{
		@Override
		public void onMotionDetected()
		{
			locationCatcher.beginLocationCatch(trackerConfiguration.locationPollDelay,
					trackerConfiguration.locationCatchTimeOut, locationCatcherListener);
		}

		@Override
		public void onStillnesDetected()
		{
			wakeUpSheduler.sheduleWakeUp(trackerConfiguration.motionPollDelay, wakeUpShedulerListener);
		}
	};

	public static String fileName(Calendar cal)
	{
		return String.format("tr%02d%02d%02d.gps", cal.get(Calendar.YEAR) % 100, cal.get(Calendar.MONTH),
				cal.get(Calendar.DAY_OF_MONTH));
	}

	private void storeLocation(Location location)
	{
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(location.getTime());

		PositionRecord positionRecord = new PositionRecord(cal.get(Calendar.HOUR_OF_DAY) * 60 * 60
				+ cal.get(Calendar.MINUTE) * 60 + cal.get(Calendar.SECOND), location.getAccuracy(),
				(float) location.getLatitude(), (float) location.getLongitude());

		try
		{
			OutputStream outputStream = openFileOutput(fileName(cal), Context.MODE_PRIVATE | Context.MODE_APPEND);
			positionRecord.write(outputStream);
			outputStream.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

}
