package ad.labs.sdk.tasks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import ad.labs.sdk.AdBanner;
import ad.labs.sdk.AdTarget;
import ad.labs.sdk.ResponseParser;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

public class BannerLoader extends AsyncTask<Void, Void, String> {
	public interface OnBannerRequestListener {
		public void onFailedBannerRequest(String message);
	}
	
	private static final String DEBUG_TAG = "My";
	
	private static final String SERVER = "http://luxup.ru/show/";
	private static final String OUTPUT_FORMAT_JSON = "/?div=plainJson";
	
	public static final String POST_PARAM_AREA_ID = "area_id";
	public static final String POST_PARAM_DEVICE_ID = "device_id";
	public static final String POST_PARAM_DEVICE_MODEL = "device_model";
	public static final String POST_PARAM_DEVICE_BRAND = "device_brand";
	public static final String POST_PARAM_DEVICE_OS_VERSION = "device_os_version";
	public static final String POST_PARAM_DEVICE_DENSITY = "device_density";
	public static final String POST_PARAM_COUNTRY = "country";
	public static final String POST_PARAM_SCREEN_WIDTH = "screen_width";
	public static final String POST_PARAM_SCREEN_HEIGHT = "screen_height";
	public static final String POST_PARAM_IS_TABLET = "is_tablet";
	public static final String POST_PARAM_LATITUDE = "latitude";
	public static final String POST_PARAM_LONGITUDE = "longitude";
	public static final String POST_PARAM_SECRET = "secret";
	public static final String POST_PARAM_WIFI_MAC = "wifi_mac";
	public static final String POST_PARAM_ODIN1 = "odin1_id";
	
	private AdBanner adBanner;
	private AdTarget adTarget;
	private OnBannerRequestListener onBannerRequestListener;
	private int responseStatusCode;
	
	private String serverUrl;
	
	public BannerLoader(AdBanner banner, AdTarget target, String yourAppId) {
		this.adBanner = banner;
		this.adTarget = target;
		
		this.serverUrl = SERVER + yourAppId + OUTPUT_FORMAT_JSON;
	}
	
	public void setOnBannerResponseListener(OnBannerRequestListener listener) {
		onBannerRequestListener = listener;
	}

	@Override
	protected String doInBackground(Void... params) {
		List<NameValuePair> nameValuePairs = buildPostRequestParams(adTarget);
		
		HttpPost post = new HttpPost(serverUrl);
		try {
			post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		} catch (UnsupportedEncodingException e1) {
			Log.e(DEBUG_TAG, "Error: new entity");
		}
		
		try {
			HttpClient httpClient = new DefaultHttpClient();
			HttpResponse httpResponse = httpClient.execute(post);
			
			String stringResponse = buildStringResponse(httpResponse);
			return stringResponse;
		} catch (Exception e) {
			Log.e(DEBUG_TAG, "Error in doInbackground: " + e.getMessage());
			return null;
		}
	}
	
	private List<NameValuePair> buildPostRequestParams(AdTarget target) {
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
	    nameValuePairs.add(new BasicNameValuePair(POST_PARAM_AREA_ID, target.getAreaId()));
	    nameValuePairs.add(new BasicNameValuePair(POST_PARAM_DEVICE_ID, target.getDeviceId()));
	    nameValuePairs.add(new BasicNameValuePair(POST_PARAM_DEVICE_MODEL, target.getDeviceModel()));
	    nameValuePairs.add(new BasicNameValuePair(POST_PARAM_DEVICE_BRAND, target.getDeviceBrand()));
	    nameValuePairs.add(new BasicNameValuePair(POST_PARAM_DEVICE_OS_VERSION, target.getDeviceOSVersion()));
	    nameValuePairs.add(new BasicNameValuePair(POST_PARAM_COUNTRY, target.getCountry()));
	    nameValuePairs.add(new BasicNameValuePair(POST_PARAM_SCREEN_WIDTH, String.valueOf(target.getScreenWidth())));
	    nameValuePairs.add(new BasicNameValuePair(POST_PARAM_SCREEN_HEIGHT, String.valueOf(target.getScreenHeight())));
		
		int isTabletIndex = target.isTablet() ? 1 : 0;
		nameValuePairs.add(new BasicNameValuePair(POST_PARAM_IS_TABLET, String.valueOf(isTabletIndex)));
		
		String secret = (ResponseParser.getSecret() != null) ? ResponseParser.getSecret() : "";
		nameValuePairs.add(new BasicNameValuePair(POST_PARAM_SECRET, secret));
		
		if (target.getMACAddress() != null)
			nameValuePairs.add(new BasicNameValuePair(POST_PARAM_WIFI_MAC, target.getMACAddress()));
		
		nameValuePairs.add(new BasicNameValuePair(POST_PARAM_ODIN1, target.getODIN()));
		
		if (target.getLocation() != null) {
			Location location = target.getLocation();
			nameValuePairs.add(new BasicNameValuePair(POST_PARAM_LATITUDE, String.valueOf(location.getLatitude())));
			nameValuePairs.add(new BasicNameValuePair(POST_PARAM_LONGITUDE, String.valueOf(location.getLongitude())));
		}

	    return nameValuePairs;
	}
	
	private String buildStringResponse(HttpResponse httpResponse) {
		StatusLine statusLine = httpResponse.getStatusLine();
		int statusCode = statusLine.getStatusCode();
		responseStatusCode = statusCode;
		
		StringBuilder builder = new StringBuilder();
		if (statusCode == 200) {
			try {
			    HttpEntity entity = httpResponse.getEntity();
			    InputStream content = entity.getContent();
			        
			    BufferedReader reader = new BufferedReader(new InputStreamReader(content));
			    String line = "";
			    while ((line = reader.readLine()) != null) {
			    	Log.i(DEBUG_TAG, "  line: " + line);
			        builder.append(line);
			    }
			} catch (IOException e) {
				Log.e(DEBUG_TAG, "Error at PostTask: " + e.getMessage());
				return null;
			}
		}
		
		return builder.toString();
	}
	
	@Override
	protected void onPostExecute(String response) {
		super.onPostExecute(response);
		
		if (response != null) {
			ResponseParser responseParser = new ResponseParser(response);
			adBanner.setBanner(responseParser);
			
			if (responseParser.getBitmapUrl() == null && responseParser.getArticle() == null) {
				if (onBannerRequestListener != null) {
					onBannerRequestListener.onFailedBannerRequest("Information about banner not found at the " +
							"response from server. Banner will not be shown. StatusCode: " + responseStatusCode);
				}
			}
		} else {
			Log.e(DEBUG_TAG, "Error: response == null");
			if (onBannerRequestListener != null) {
				onBannerRequestListener.onFailedBannerRequest("Error: response from server is null. StatusCode: "
						+ responseStatusCode);
			}
		}
	}
	
	public static String usedURL(String areaId) {
		return SERVER + areaId + OUTPUT_FORMAT_JSON;
	}
}