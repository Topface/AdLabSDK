package ad.labs.sdk;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Color;
import android.util.Log;

public class ResponseParser {
	public enum BannerType {
		IMAGE_WITH_TEXT,
		FULLSCREEN_IMAGE
	}
	
	private static final String DEBUG_TAG = "My";
	
	private static final String JSON_ARRAY_BANNERS = "banners";
	
	// Parameters of a banner
	private static final String JSON_PARAM_URL = "url";
	private static final String JSON_PARAM_ARTICLE = "article";
	private static final String JSON_PARAM_IMAGE = "image";
	private static final String JSON_PARAM_BGCOLOR = "bgcolor";
	private static final String JSON_PARAM_FONTCOLOR = "fontcolor";
	private static final String JSON_PARAM_BANNER_TYPE = "banner_type";
	
	private static final String JSON_PARAM_SECRET = "secret";

	private String bitmapUrl;
	private String article;
	private String destinationUrl;
	
	private static String secret = "";
	
	private int backgroundColor;
	private int fontColor;
	
	private BannerType bannerType;
	
	public ResponseParser(String response) {
		parseResponse(response);
	}

	private void parseResponse(String response) {
		Log.d(DEBUG_TAG, "Response: " + response);
		JSONObject jsonRoot = null;
		try {
			jsonRoot = new JSONObject(response);
		} catch (JSONException e) {
			Log.e(DEBUG_TAG, "ResponseParser: JSON root not created!");
			setClassFieldsAtNull();
			return;
		}
		
		JSONArray banners = null;
		try {
			banners = jsonRoot.getJSONArray(JSON_ARRAY_BANNERS);
		} catch (JSONException e) {
			Log.e(DEBUG_TAG, "ResponseParser: JSON array for banner not created!");
			setClassFieldsAtNull();
			return;
		}
		
		Log.i(DEBUG_TAG, "Parsed information:");
		JSONObject banner = null;
		try {
			banner = banners.getJSONObject(0);
		} catch (JSONException e) {
			Log.e(DEBUG_TAG, ResponseParser.class.getSimpleName() + ": banner json not created");
		}
			
		if (banner != null) {
			destinationUrl = getStringFromJSON(banner, JSON_PARAM_URL);
			Log.d(DEBUG_TAG, "  Dest url: " + destinationUrl);
			
			article = getStringFromJSON(banner, JSON_PARAM_ARTICLE);
			Log.d(DEBUG_TAG, "  Article: " + article);
			
			bitmapUrl = getStringFromJSON(banner, JSON_PARAM_IMAGE);
			Log.d(DEBUG_TAG, "  bitmap url: " + bitmapUrl);
			
			String colorString = getStringFromJSON(banner, JSON_PARAM_BGCOLOR);
			backgroundColor = parseColorValue(colorString, Color.BLACK);
			Log.d(DEBUG_TAG, "  bgColor: " + backgroundColor + " (" + colorString + ")");
			
			colorString = getStringFromJSON(banner, JSON_PARAM_FONTCOLOR);
			fontColor = parseColorValue(colorString, Color.WHITE);
			Log.d(DEBUG_TAG, "  fontColor: " + fontColor + " (" + colorString + ")");
			
			// Parse 'banner_type' value. If parsed value will be equals to '0' then 
			// we should draw image with text. If parsed value will be '1' then draw fullscreen banner.
			String parsedBannerType = getStringFromJSON(banner, JSON_PARAM_BANNER_TYPE);
			if (parsedBannerType != null) {
				if (parsedBannerType.equals("1"))
					bannerType = BannerType.FULLSCREEN_IMAGE;
				else
					bannerType = BannerType.IMAGE_WITH_TEXT;
				
				Log.d(DEBUG_TAG, "  BannerType: " + bannerType.toString());
			}
		}
		
		// Parse secret value...
		secret = parseSecretValue(jsonRoot);
	}
	
	private String getStringFromJSON(JSONObject ob, String paramName) {
		try {
			return ob.getString(paramName);
		} catch (JSONException e) {
			return null;
		}
	}
	
	private void setClassFieldsAtNull() {
		bitmapUrl = null;
		article = null;
		destinationUrl = null;
		
		secret = null;
	}
	
	private String parseSecretValue(JSONObject jsonRoot) {
		try {
			return jsonRoot.getString(JSON_PARAM_SECRET);
		} catch (JSONException e) {
			return null;
		}
	}
	
	private int parseColorValue(String colorString, int defaultColor) {
		try {
			return Color.parseColor(colorString);
		} catch (Exception e) {
			return defaultColor;
		}
	}

	public String getBitmapUrl() {
		return bitmapUrl;
	}

	public String getArticle() {
		return article;
	}

	public String getDestinationUrl() {
		return destinationUrl;
	}
	
	public static String getSecret() {
		return secret;
	}
	
	public int getBackgroundColor() {
		return backgroundColor;
	}
	
	public int getFontColor() {
		return fontColor;
	}
	
	public BannerType getBannerType() {
		return bannerType;
	}
}
