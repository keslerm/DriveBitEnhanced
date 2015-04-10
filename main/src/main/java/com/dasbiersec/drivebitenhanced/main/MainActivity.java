package com.dasbiersec.drivebitenhanced.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import com.dasbiersec.drivebitenhanced.domain.FitbitActivity;
import com.dasbiersec.drivebitenhanced.util.ConfigReader;
import net.danlew.android.joda.JodaTimeAndroid;
import oauth.signpost.OAuth;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.joda.time.DateTime;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity
{
	private static final String TAG = "DriveBitEnhanced";

	private static String OAUTH_KEY = null; // Put your Consumer key here
	private static String OAUTH_SECRET = null; // Put your Consumer secret here
	private static final String OAUTH_CALLBACK_SCHEME = "demo"; // Arbitrary, but make sure this matches the scheme in the manifest
	private static final String OAUTH_CALLBACK_URL = OAUTH_CALLBACK_SCHEME + "://callback";

	private OAuthConsumer oAuthConsumer;
	private OAuthProvider oAuthProvider;
	private SharedPreferences prefs;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Says i need this
		JodaTimeAndroid.init(this);

		// Load settings
		ConfigReader configReader = new ConfigReader(this);
		OAUTH_KEY = configReader.getProperty("oauth_key");
		OAUTH_SECRET = configReader.getProperty("oauth_secret");


		oAuthConsumer = new CommonsHttpOAuthConsumer(OAUTH_KEY, OAUTH_SECRET);
		oAuthProvider = new DefaultOAuthProvider(
				"https://api.fitbit.com/oauth/request_token",
				"https://api.fitbit.com/oauth/access_token",
				"https://www.fitbit.com/oauth/authorize");

		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String token = prefs.getString("token", null);
		String tokenSecret = prefs.getString("tokenSecret", null);

		if (token != null && tokenSecret != null)
		{
			oAuthConsumer.setTokenWithSecret(token, tokenSecret);

			// Hide the auth button - shitty but just for now
			(findViewById(R.id.authButton)).setVisibility(View.INVISIBLE);
		}
	}

	@Override
	public void onNewIntent(Intent intent)
	{
		super.onNewIntent(intent);
		Log.d(TAG, "New intent: " + intent);

		Uri uri = intent.getData();
		if (uri != null && uri.getScheme().equals(OAUTH_CALLBACK_SCHEME))
		{
			Log.d(TAG, "Callback: " + uri.getPath());

			String verifier = uri.getQueryParameter(OAuth.OAUTH_VERIFIER);
			Log.d(TAG, "Verifier: " + verifier);

			new RetrieveAccessTokenTask(this).execute(verifier);
		}
	}

	public void onClickAuthorize(View view)
	{
		new OAuthAuthorizeTask().execute();
	}

	private class OAuthAuthorizeTask extends AsyncTask<Void, Void, String>
	{
		@Override
		protected String doInBackground(Void... params)
		{
			String authUrl;
			String message = null;

			try
			{
				authUrl = oAuthProvider.retrieveRequestToken(oAuthConsumer, OAUTH_CALLBACK_URL);
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(authUrl));
				startActivity(intent);
			}
			catch (OAuthCommunicationException e)
			{
				e.printStackTrace();
			}
			catch (OAuthExpectationFailedException e)
			{
				e.printStackTrace();
			}
			catch (OAuthNotAuthorizedException e)
			{
				e.printStackTrace();
			}
			catch (OAuthMessageSignerException e)
			{
				e.printStackTrace();
			}

			return message;
		}
	}

	private class RetrieveAccessTokenTask extends AsyncTask<String, Void, String>
	{
		public MainActivity myActivity = null;

		public RetrieveAccessTokenTask(MainActivity myActivity)
		{
			this.myActivity = myActivity;
		}

		@Override
		public String doInBackground(String... params)
		{
			String message = null;
			String verifier = params[0];

			try
			{
				Log.d(TAG, "oAuthConsumer: " + oAuthConsumer);
				Log.d(TAG, "oAuthProvider: " + oAuthProvider);

				oAuthProvider.retrieveAccessToken(oAuthConsumer, verifier);

				String token = oAuthConsumer.getToken();
				String tokenSecret = oAuthConsumer.getTokenSecret();

				oAuthConsumer.setTokenWithSecret(token, tokenSecret);

				Log.d(TAG, String.format("verifier: %s, token: %s, tokenSecret: %s", verifier, token, tokenSecret));

				prefs.edit().putString("token", token)
						.putString("tokenSecret", tokenSecret).commit();

				Log.d(TAG, "Token: " + token);

			}
			catch (OAuthCommunicationException e)
			{
				e.printStackTrace();
			}
			catch (OAuthExpectationFailedException e)
			{
				e.printStackTrace();
			}
			catch (OAuthNotAuthorizedException e)
			{
				e.printStackTrace();
			}
			catch (OAuthMessageSignerException e)
			{
				e.printStackTrace();
			}

			return message;
		}

	}

	public void postActivity15Minutes(View view)
	{
		FitbitActivity activity = new FitbitActivity(new DateTime(), 15*60*1000);
		new PostActivity().execute(activity);
	}

	public void postActivity30Minutes(View view)
	{
		FitbitActivity activity = new FitbitActivity(new DateTime(), 30*60*1000);
		new PostActivity().execute(activity);
	}

	public void postActivity45Minutes(View view)
	{
		FitbitActivity activity = new FitbitActivity(new DateTime(), 45*60*1000);
		new PostActivity().execute(activity);
	}

	public void postActivity60Minutes(View view)
	{
		FitbitActivity activity = new FitbitActivity(new DateTime(), 60*60*1000);
		new PostActivity().execute(activity);
	}

	private class PostActivity extends AsyncTask<FitbitActivity, Void, String>
	{

		@Override
		public String doInBackground(FitbitActivity... activities)
		{
			try
			{
				Log.d("PostActivity", "Got to post activity method");

				HttpClient client = new DefaultHttpClient();
				Log.d("PostActivity", "Running post");
				HttpPost post = new HttpPost("https://api.fitbit.com/1/user/-/activities.json");

				List<BasicNameValuePair> values = new ArrayList<>();
				values.add(new BasicNameValuePair("activityId", activities[0].getActivityId()));
				values.add(new BasicNameValuePair("startTime", activities[0].getFitbitStartTime()));
				values.add(new BasicNameValuePair("date", activities[0].getFitbitStartDate()));
				values.add(new BasicNameValuePair("durationMillis", String.valueOf(activities[0].getDuration())));

				post.setEntity(new UrlEncodedFormEntity(values));

				oAuthConsumer.sign(post);
				HttpResponse response = client.execute(post);

				String temp;
				StringBuilder sb = new StringBuilder();
				BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

				while ((temp = rd.readLine()) != null)
				{

					sb.append(temp);
				}

				Log.d("PostActivity", "Got to post the activity: " + sb.toString());
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

			return "Executed";
		}
	}



	/* All of this is boilerplate??*/

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings)
		{
			return true;
		}

		return super.onOptionsItemSelected(item);
	}
}
