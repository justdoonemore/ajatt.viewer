/** 
 *  Copyright (C) 2012  Just Do One More
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jdom.ajatt.viewer.android;

import java.util.List;
import java.util.Random;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.jdom.ajatt.viewer.util.HtmlUtil;

public abstract class DashboardActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	public void onClickHome(View v) {
		goHome(this);
	}

	public void onClickSearch(View v) {
		startActivity(new Intent(getApplicationContext(), SearchActivity.class));
	}

	public void onClickAbout(View v) {
		startActivity(new Intent(getApplicationContext(), AboutActivity.class));
	}

	public void onClickFeature(View v) {
		final ProgressDialog pd = ProgressDialog
				.show(this, "",
						"Loading, this may take a while if this is the first time it's been loaded..");

		int id = v.getId();
		switch (id) {
		case R.id.home_btn_newest:
			new Thread() {
				String urlToNewest;

				@Override
				public void run() {
					urlToNewest = HtmlUtil
							.getUrlToNewestPost(DashboardActivity.this);
					handler.sendEmptyMessage(0);
				}

				Handler handler = new Handler() {
					@Override
					public void handleMessage(Message msg) {
						dismissDialog(pd);
						startBrowserActivity(urlToNewest);
					}
				};

			}.start();
			break;
		case R.id.home_btn_tableOfContents:
			dismissDialog(pd);
			startActivity(new Intent(getApplicationContext(),
					TableOfContentsActivity.class));
			break;
		case R.id.home_btn_random:
			new Thread() {
				List<String[]> links;

				@Override
				public void run() {
					links = HtmlUtil
							.getTableOfContentsLinks(DashboardActivity.this);
					handler.sendEmptyMessage(0);
				}

				Handler handler = new Handler() {
					@Override
					public void handleMessage(Message msg) {
						dismissDialog(pd);
						loadRandomPost(links);
					}
				};
			}.start();

			break;
		case R.id.home_btn_settings:
			dismissDialog(pd);
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse("market://search?q=pub:Just Do One More"));
			startActivity(intent);
			break;
		default:
			break;
		}
	}

	private void loadRandomPost(List<String[]> links) {
		String url = links.get(new Random().nextInt(links.size()))[1];
		startBrowserActivity(url);
	}

	protected void startBrowserActivity(String url) {
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setData(Uri.parse(url));
		startActivity(i);
	}

	public void goHome(Context context) {
		final Intent intent = new Intent(context, HomeActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		context.startActivity(intent);
	}

	public void setTitleFromActivityLabel(int textViewId) {
		TextView tv = (TextView) findViewById(textViewId);
		if (tv != null)
			tv.setText(getTitle());
	}

	public void toast(String msg) {
		Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
	}

	/**
	 * Send a message to the debug log and display it using Toast.
	 */
	public void trace(String msg) {
		Log.d("AJATT Viewer", msg);
		toast(msg);
	}

	protected void logException(Exception e) {
		Log.e(DashboardActivity.class.getName(), "Exception occured: ", e);
	}

	private void dismissDialog(ProgressDialog pd) {
		try {
			if (pd.isShowing()) {
				pd.dismiss();
			}
		} catch (Exception e) {
			logException(e);
		}
	}
}
