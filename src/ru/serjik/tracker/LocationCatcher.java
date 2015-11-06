package ru.serjik.tracker;

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
	public interface LocationCatcherListener
	{
		void onLocationCatchFinished();

		void onLocationCatched(Location location);
	}

	private Handler handler;
	private LocationCatcherListener locationCatcherListener;

	private int locationCatchTimeOut;
	private LocationListener locationListener = new LocationListener()
	{
		@Override
		public void onLocationChanged(Location location)
		{
			locationCatcherListener.onLocationCatched(location);

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
		public void onProviderDisabled(String provider)
		{
		}

		@Override
		public void onProviderEnabled(String provider)
		{
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras)
		{
		}
	};

	private LocationManager locationManager;

	private MotionDetector motionDetector;

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

	private String TAG = LocationCatcher.class.getName();

	private Runnable timeOutRunnable = new Runnable()
	{
		@Override
		public void run()
		{
			Log.i(TAG, "timeout");
			finishLocationCatch();
		}
	};

	public LocationCatcher(Context context, MotionDetector motionDetector)
	{
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

	public void finishLocationCatch()
	{
		locationManager.removeUpdates(locationListener);
		locationCatcherListener.onLocationCatchFinished();
	}
}
