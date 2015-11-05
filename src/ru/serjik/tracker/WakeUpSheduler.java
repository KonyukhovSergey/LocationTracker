package ru.serjik.tracker;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemClock;

public class WakeUpSheduler extends BroadcastReceiver
{
	private static final String WAKE_UP_SHEDULER = "WakeUpSheduler";
	private static final int ALARM_REQUEST_CODE = 0x2839;

	private Context context;
	private WakeUpShedulerListener wakeUpListener;
	private PendingIntent pendingIntent;
	private AlarmManager alarmManager;

	public WakeUpSheduler(Context context)
	{
		this.context = context;
		this.pendingIntent = PendingIntent.getBroadcast(context, ALARM_REQUEST_CODE, new Intent(WAKE_UP_SHEDULER), 0);
		this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
	}

	public void sheduleWakeUp(int delay, WakeUpShedulerListener wakeUpListener)
	{
		context.registerReceiver(this, new IntentFilter(WAKE_UP_SHEDULER));
		alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + delay, pendingIntent);
		this.wakeUpListener = wakeUpListener;
	}

	public void cancel()
	{
		alarmManager.cancel(pendingIntent);
		context.unregisterReceiver(this);
	}

	@Override
	public void onReceive(Context context, Intent intent)
	{
		context.unregisterReceiver(this);
		wakeUpListener.onWakeUp();
	}

	public interface WakeUpShedulerListener
	{
		void onWakeUp();
	}
}
