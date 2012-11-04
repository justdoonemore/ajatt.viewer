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
package com.jdom.ajatt.viewer.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.jdom.ajatt.viewer.android.HttpHelper;

public class HtmlUtil {

	private static final String CLASS_NAME = HtmlUtil.class.getName();

	private static final String SILVERSPOON = "Permalink to Which SilverSpoon";

	private static final String SILVERSPOON_TITLE = "Which SilverSpoon?";

	private static final long CACHE_URL_MILLISECONDS = 1000 * 60 * 10;

	private static Pattern LINK_PATTERN = Pattern
			.compile("<li><a.*?href=\"(.*?)\".*?>(.*?)</a></li>");

	private static String getTableOfContents(Activity activity) {
		try {
			String baseHtml = getRequest(
					activity,
					"http://www.alljapaneseallthetime.com/blog/all-japanese-all-the-time-ajatt-how-to-learn-japanese-on-your-own-having-fun-and-to-fluency");
			baseHtml = baseHtml.substring(baseHtml
					.indexOf("<!-- end sidebar -->"));
			baseHtml = baseHtml.substring(0,
					baseHtml.indexOf("11. Further Reading"));

			return baseHtml;
		} catch (Exception e) {
			return "Error: can't show table of contents.";
		}
	}

	public static String getRequest(Activity activity, final String url) {
		SharedPreferences prefs = activity.getSharedPreferences(CLASS_NAME,
				Context.MODE_PRIVATE);
		String cachedUrlContents = prefs.getString(url, null);
		String urlRetrievalTimeKey = url + ".time";
		long cachedUrlRetrievalTime = prefs.getLong(urlRetrievalTimeKey, 0L);
		long ageOfCachedData = System.currentTimeMillis()
				- cachedUrlRetrievalTime;
		if (cachedUrlRetrievalTime == 0) {
			Log.d(CLASS_NAME, "Did not find cached data for URL [" + url + "].");
		} else {
			Log.d(CLASS_NAME, "URL [" + url + "] has been cached for ["
					+ ageOfCachedData + "] ms.");
		}

		Future<String> result = null;

		boolean expired = ageOfCachedData > CACHE_URL_MILLISECONDS;

		if (expired) {
			Log.d(CLASS_NAME, "URL [" + url + "] data is stale.");
		} else {
			long timeRemainingValidCache = CACHE_URL_MILLISECONDS
					- ageOfCachedData;
			Log.d(CLASS_NAME, "URL [" + url + "] data has ["
					+ timeRemainingValidCache + "] ms of validity remaining.");
		}

		if (cachedUrlContents == null || expired) {
			Callable<String> callable = new Callable<String>() {
				public String call() throws Exception {
					long start = System.currentTimeMillis();
					Log.d(CLASS_NAME, "Retrieving URL [" + url + "].");
					HttpClient client = new DefaultHttpClient();
					HttpGet request = new HttpGet(url);
					try {
						HttpResponse response = client.execute(request);
						return HttpHelper.request(response);
					} catch (Exception ex) {
						Log.e(CLASS_NAME, "Failure to retrieve the url!", ex);
						return null;
					} finally {
						Log.d(CLASS_NAME, "Retrieving URL [" + url + "] took ["
								+ (System.currentTimeMillis() - start)
								+ "] ms to retrieve.");
					}
				}
			};

			ExecutorService executor = Executors.newSingleThreadExecutor();
			result = executor.submit(callable);
		}

		if (cachedUrlContents == null) {
			try {
				cachedUrlContents = result.get();

				Editor editor = prefs.edit();
				editor.putLong(urlRetrievalTimeKey, System.currentTimeMillis());
				editor.putString(url, cachedUrlContents);
				editor.commit();
			} catch (Exception e) {
				Log.e(CLASS_NAME, "Failure to retrieve the url!", e);
			}
		}

		return cachedUrlContents;
	}

	static List<String[]> parseLinks(String string) {
		List<String[]> links = new ArrayList<String[]>();
		Matcher m = LINK_PATTERN.matcher(string);

		while (m.find()) {
			String link = m.group(1);
			String title = m.group(2);

			if (!"Edit".equals(title)) {
				links.add(new String[] { title, link });
			}
		}

		return links;
	}

	public static List<String[]> getTableOfContentsLinks(Activity activity) {
		String tableOfContents = HtmlUtil.getTableOfContents(activity);
		return HtmlUtil.parseLinks(tableOfContents);
	}

	public static String getUrlToNewestPost(Activity activity) {
		String pageHtml = getRequest(activity,
				"http://www.alljapaneseallthetime.com/blog/");
		pageHtml = pageHtml.substring(pageHtml.indexOf("Latest Story"));

		Pattern LATEST_PATTERN = Pattern
				.compile("<h2><a\\shref=\"(.*)?\"\\stitle=\"(.*)?\".*?>(.*)?</a></h2>");

		Matcher m = LATEST_PATTERN.matcher(pageHtml);

		while (m.find()) {
			String url = m.group(1);
			String urlTitle = m.group(2);
			String displayTitle = m.group(3);

			if (!SILVERSPOON.equals(urlTitle)
					&& !SILVERSPOON_TITLE.equals(displayTitle)) {
				return url;
			}
		}

		return "Unable to find latest post!";
	}
}
