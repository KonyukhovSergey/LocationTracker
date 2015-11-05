package ru.serjik.tracker;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class MotionDetector implements SensorEventListener
{
	private float ax, ay, az;

	private SensorManager sensorManager;
	private MotionDetectorListener motionDetectorListener;

	private float thresold;

	public MotionDetector(Context context, float thresold)
	{
		sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		this.thresold = thresold;
	}

	public void beginMotionDetect(MotionDetectorListener motionDetectorListener)
	{
		this.motionDetectorListener = motionDetectorListener;
		sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_NORMAL);
	}

	@Override
	public void onSensorChanged(SensorEvent event)
	{
		sensorManager.unregisterListener(this);

		float delta = 0;

		delta += Math.abs(ax - event.values[0]);
		delta += Math.abs(ay - event.values[1]);
		delta += Math.abs(az - event.values[2]);

		if (delta < thresold)
		{
			motionDetectorListener.onStillnesDetected();
		}
		else
		{
			motionDetectorListener.onMotionDetected();
		}

		ax = event.values[0];
		ay = event.values[1];
		az = event.values[2];
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy)
	{
	}

	public interface MotionDetectorListener
	{
		void onMotionDetected();

		void onStillnesDetected();
	}
}
