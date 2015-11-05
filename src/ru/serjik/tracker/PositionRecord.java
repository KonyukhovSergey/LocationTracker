package ru.serjik.tracker;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class PositionRecord
{
	private static ByteBuffer bb = ByteBuffer.allocate(16);
	private int time;
	private float acc, lat, lng;

	public void write(OutputStream os)
	{
		bb.position(0);
		bb.putInt(time);
		bb.putFloat(acc);
		bb.putFloat(lat);
		bb.putFloat(lng);
		try
		{
			os.write(bb.array());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public PositionRecord(int time, float acc, float lat, float lng)
	{
		this.time = time;
		this.acc = acc;
		this.lat = lat;
		this.lng = lng;
	}

	public PositionRecord(InputStream is)
	{
		read(is);
	}

	public void read(InputStream is)
	{
		try
		{
			is.read(bb.array());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		bb.position(0);
		time = bb.getInt();
		acc = bb.getFloat();
		lat = bb.getFloat();
		lng = bb.getFloat();
	}

	@Override
	public String toString()
	{

		return String.format("%02d:%02d:%02d", (time / 3600), (time / 60) % 60, time % 60) + ", acc=" + acc + ", lat="
				+ lat + ", lng=" + lng;
	}

}
