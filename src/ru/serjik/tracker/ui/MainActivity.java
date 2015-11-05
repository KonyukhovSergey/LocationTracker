package ru.serjik.tracker.ui;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import ru.serjik.perfectgpstracker.R;
import ru.serjik.tracker.LocationCatcher;
import ru.serjik.tracker.PositionRecord;
import ru.serjik.tracker.TrackerService;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

public class MainActivity extends Activity implements OnClickListener, ServiceConnection
{
	private Calendar cal = Calendar.getInstance();
	private Button buttonRefresh;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		cal.setTimeInMillis(System.currentTimeMillis());
		buttonRefresh = (Button) findViewById(R.id.button_refresh);
		buttonRefresh.setOnClickListener(this);
		
		startService(new Intent(this, TrackerService.class));
	}

	@Override
	public void onClick(View v)
	{
		if (v.getId() == R.id.button_refresh)
		{
			buttonRefresh.setText(String.format("tr%02d%02d%02d.gps", cal.get(Calendar.YEAR) % 100,
					cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)));

			((ListView) findViewById(R.id.list_track)).setAdapter(new ArrayAdapter<PositionRecord>(this,
					android.R.layout.simple_list_item_1, positions(this, cal)));
		}
	}

	public void buttonPrevClick(View v)
	{
		cal.add(Calendar.DATE, -1);
		buttonRefresh.performClick();
	}

	public void buttonNextClick(View v)
	{
		cal.add(Calendar.DATE, 1);
		buttonRefresh.performClick();
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onServiceDisconnected(ComponentName name)
	{
		// TODO Auto-generated method stub

	}

	private List<PositionRecord> positions(Context context, Calendar cal)
	{
		List<PositionRecord> positions = new ArrayList<PositionRecord>();

		try
		{
			InputStream is = new BufferedInputStream(context.openFileInput(LocationCatcher.fileName(cal)));

			while (is.available() > 0)
			{
				positions.add(new PositionRecord(is));
			}

			is.close();
		}
		catch (IOException e)
		{
			// e.printStackTrace();
		}

		return positions;
	}

}
