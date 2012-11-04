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

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.jdom.ajatt.viewer.util.HtmlUtil;

public class TableOfContentsActivity extends DashboardActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.table_of_contents);
		setTitleFromActivityLabel(R.id.title_text);

		final ProgressDialog pd = ProgressDialog
				.show(this, "",
						"Loading, this may take a while if this is the first time it's been loaded..");
		pd.show();

		new Thread() {
			List<String[]> links;

			@Override
			public void run() {
				links = HtmlUtil
						.getTableOfContentsLinks(TableOfContentsActivity.this);
				handler.sendEmptyMessage(0);
			}

			Handler handler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					try {
						if (pd.isShowing()) {
							pd.dismiss();
						}
					} catch (Exception e) {
						logException(e);
					}
					processLinks(links);
				}
			};
		}.start();
	}

	private void processLinks(final List<String[]> links) {
		String[] titles = new String[links.size()];
		for (int i = 0; i < links.size(); i++) {
			titles[i] = links.get(i)[0];
		}

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, titles);

		ListView listView = (ListView) findViewById(R.id.tableOfContents_list);
		listView.setAdapter(adapter);
		adapter.notifyDataSetChanged();
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				String url = links.get(arg2)[1];

				startBrowserActivity(url);
			}
		});

	}
}
