/*
 * Copyright 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package si.majeric.smarthouse;

import si.majeric.android.TaskCallbacks;
import si.majeric.smarthouse.client.Client;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.FloatMath;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MessagesActivity extends FragmentActivity implements TaskCallbacks<String>, OnTouchListener {
	private static final String MESSAGES_FRAGMENT_TAG = "messages_task";
	private static final String TAG = "Touch";
	@SuppressWarnings("unused")
	private static final float MIN_ZOOM = 1f, MAX_ZOOM = 1f;

	Client _client;
	private MessagesFragment mTaskFragment;

	// These matrices will be used to scale points of the image
	// Matrix matrix = new Matrix();
	// Matrix savedMatrix = new Matrix();

	// The 3 states (events) which the user is trying to perform
	static final int NONE = 0;
	static final int DRAG = 1;
	static final int ZOOM = 2;
	int mode = NONE;

	// these PointF objects are used to record the point(s) the user is touching
	PointF start = new PointF();
	PointF mid = new PointF();
	float oldDist = 1f;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_messages);

		_client = ((SmartHouseApplication) getApplication()).getClient();

		// Set up the action bar.
		final ActionBar actionBar = getActionBar();

		actionBar.setHomeButtonEnabled(true);

		final FragmentManager fm = getFragmentManager();
		mTaskFragment = (MessagesFragment) fm.findFragmentByTag(MESSAGES_FRAGMENT_TAG);

		// If the Fragment is non-null, then it is currently being
		// retained across a configuration change.
		if (mTaskFragment == null) {
			mTaskFragment = new MessagesFragment();
			fm.beginTransaction().add(mTaskFragment, MESSAGES_FRAGMENT_TAG).commit();
		}

		_messagesTV = (TextView) findViewById(R.id.messagesTextView);
		_messagesTV.setOnTouchListener(this);
		Button refreshButton = (Button) findViewById(R.id.messagesRefreshButton);
		refreshButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				mTaskFragment.startMessagesDownload();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_messages, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
			case R.id.menu_send_messages:
				sendMessages();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Send messages to specific email address
	 */
	protected void sendMessages() {
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/html");
		intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"uros@majeric.si", "milos.kocbek@gmail.com"});
		intent.putExtra(Intent.EXTRA_SUBJECT, "Server Logs");

		StringBuilder emailText = new StringBuilder();
		for (String str : _client.getServerMessages()) {
			emailText.append(str);
		}
		intent.putExtra(Intent.EXTRA_TEXT, emailText.toString());

		try {
			startActivity(Intent.createChooser(intent, "Send Server Logs"));
		} catch (android.content.ActivityNotFoundException ex) {
			Toast.makeText(this, "There are no email clients installed.", Toast.LENGTH_LONG).show();
		}
	}

	static ProgressDialog pd;

	private TextView _messagesTV;

	@Override
	public void onPreExecute() {
		if (pd == null || !pd.isShowing()) {
			pd = new ProgressDialog(this);
			pd.setIndeterminate(true);
			pd.setMessage("Loading...");
			pd.show();
		}
	}

	@Override
	public void onProgressUpdate(int percent) {
	}

	@Override
	public void onCancelled() {
	}

	@Override
	public void onPostExecute(String result) {
		if (pd != null && pd.isShowing()) {
			pd.dismiss();
		}

		_messagesTV.setText("");
		if (result != null) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Error").setMessage(result).setNegativeButton("OK", null).create().show();
		} else {
			for (String m : _client.getServerMessages()) {
				_messagesTV.append(m);
			}
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		TextView view = (TextView) v;
		float scale;

		// dumpEvent(event);
		// Handle touch events here...
		switch (event.getAction() & 255) {
			case MotionEvent.ACTION_DOWN: // first finger down only
				start.set(event.getX(), event.getY());
				mode = DRAG;
				break;
			case MotionEvent.ACTION_UP: // first finger lifted
			case 6: // second finger lifted
				mode = NONE;
				break;
			case 5: // first and second finger down
				oldDist = spacing(event);
				if (oldDist > 5f) {
					midPoint(mid, event);
					mode = ZOOM;
				}
				break;
			case MotionEvent.ACTION_MOVE:
				if (mode == ZOOM) {
					float newDist = spacing(event);
					if (newDist > 10f) {
						scale = newDist / oldDist;
						if (scale > 1) {
							scale = 1.1f;
						} else if (scale < 1) {
							scale = 0.95f;
						}

						float newSize = view.getTextSize() * scale;
						if (newSize < 15) {
							newSize = 15;
						} else if (newSize > 70) {
							newSize = 70;
						}
						view.setTextSize(TypedValue.COMPLEX_UNIT_PX, newSize);
						Log.d(TAG, "scale=" + scale + ", view.getTextSize(): " + view.getTextSize());
					}
				}
				break;
		}
		return true; // indicate event was handled
	}

	private float spacing(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return (float) Math.sqrt(x * x + y * y);
	}

	/*
	 * -------------------------------------------------------------------------- Method: midPoint Parameters: PointF object, MotionEvent Returns: void
	 * Description: calculates the midpoint between the two fingers ------------------------------------------------------------
	 */

	private void midPoint(PointF point, MotionEvent event) {
		float x = event.getX(0) + event.getX(1);
		float y = event.getY(0) + event.getY(1);
		point.set(x / 2, y / 2);
	}

}
