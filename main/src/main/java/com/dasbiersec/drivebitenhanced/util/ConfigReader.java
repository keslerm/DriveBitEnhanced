package com.dasbiersec.drivebitenhanced.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigReader
{
	private Properties properties;

	public ConfigReader(Context context)
	{

		try
		{

			AssetManager assetManager = context.getAssets();
			InputStream inputStream = assetManager.open("config.properties");

			properties = new Properties();
			properties.load(inputStream);

			System.out.println("The properties are now loaded");
			System.out.println("properties: " + properties);
		}
		catch (IOException e)
		{
			System.err.println("Failed to open main property file");
			e.printStackTrace();
		}
	}

	public String getProperty(String name)
	{
		String value = (String) properties.get(name);
		Log.d("ConfigReader", "Getting property: " + name + " Value:" + value);
		return value;
	}
}
