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

import android.os.Bundle;
import android.widget.TextView;

import com.jdom.ajatt.viewer.util.HtmlUtil;

public class PostViewerActivity extends DashboardActivity {
	static final String POST_LINK = "postLink";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.post_viewer);
		setTitleFromActivityLabel(R.id.title_text);

		String link = getIntent().getExtras().getString(POST_LINK);

		String postContents = HtmlUtil.getRequest(this, link);

		TextView postContent = (TextView) findViewById(R.id.post_content);
		postContent.setText(postContents);
	}

}
