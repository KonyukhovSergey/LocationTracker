package ru.serjik.tracker;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;

import ru.serjik.tracker.MotionDetector.MotionDetectorListener;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

public class LocationCatcher
{
	private String TAG = LocationCatcher.class.getName();

	private LocationManager locationManager;
	private Handler handler;
	private Context context;
	private int locationCatchTimeOut;

	private MotionDetector motionDetector;
	private LocationCatcherListener locationCatcherListener;

	public LocationCatcher(Context context, MotionDetector motionDetector)
	{
		this.context = context;
		this.motionDetector = motionDetector;
		handler = new Handler();
		locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
	}

	public void beginLocationCatch(int pollDelay, int locationCatchTimeOut,
			LocationCatcherListener locationCatcherListener)
	{
		this.locationCatcherListener = locationCatcherListener;
		this.locationCatchTimeOut = locationCatchTimeOut;
		handler.postDelayed(timeOutRunnable, locationCatchTimeOut);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, pollDelay, 0, locationListener);
	}

	private LocationListener locationListener = new LocationListener()
	{
		@Override
		public void onLocationChanged(Location location)
		{
			Log.i(TAG, location.toString());
			storeLocation(location);
			handler.removeCallbacks(timeOutRunnable);

			if (location.getSpeed() < 1.0)
			{
				motionDetector.beginMotionDetect(motionDetectorListener);
			}
			else
			{
				handler.postDelayed(timeOutRunnable, locationCatchTimeOut);
			}
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras)
		{
		}

		@Override
		public void onProviderEnabled(String provider)
		{
		}

		@Override
		public void onProviderDisabled(String provider)
		{
		}
	};

	private void storeLocation(Location location)
	{
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(location.getTime());

		PositionRecord positionRecord = new PositionRecord(cal.get(Calendar.HOUR_OF_DAY) * 60 * 60
				+ cal.get(Calendar.MINUTE) * 60 + cal.get(Calendar.SECOND), location.getAccuracy(),
				(float) location.getLatitude(), (float) location.getLongitude());

		try
		{
			OutputStream outputStream = context.openFileOutput(fileName(cal), Context.MODE_PRIVATE
					| Context.MODE_APPEND);
			positionRecord.write(outputStream);
			outputStream.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public static String fileName(Calendar cal)
	{
		return String.format("tr%02d%02d%02d.gps", cal.get(Calendar.YEAR) % 100, cal.get(Calendar.MONTH),
				cal.get(Calendar.DAY_OF_MONTH));
	}

	private Runnable timeOutRunnable = new Runnable()
	{
		@Override
		public void run()
		{
			Log.i(TAG, "timeout");
			finishLocationCatch();
		}
	};

	public interface LocationCatcherListener
	{
		void onLocationCatchFinished();
	}

	private MotionDetectorListener motionDetectorListener = new MotionDetectorListener()
	{
		@Override
		public void onMotionDetected()
		{
			handler.postDelayed(timeOutRunnable, locationCatchTimeOut);
		}

		@Override
		public void onStillnesDetected()
		{
			finishLocationCatch();
		}
	};

	public void finishLocationCatch()
	{
		locationManager.removeUpdates(locationListener);
		locationCatcherListener.onLocationCatchFinished();
	}
}
