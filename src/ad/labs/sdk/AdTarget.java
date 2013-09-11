package ad.labs.sdk;

import java.util.Locale;

import ad.labs.sdk.consts.Constants;
import ad.labs.sdk.utils.ODIN;
import android.content.Context;
import android.location.Location;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings.Secure;
import android.util.DisplayMetrics;

public class AdTarget {
	private String areaId;
	
	private String deviceId;
	private String deviceModel;
	private String deviceBrand;
	private String deviceOSVersion;
	private float deviceDensity;
	
	private String country;
	
	private int screenWidth;
	private int screenHeight;
	
	private Location location;
	
	private boolean isTablet;
	
	private String macAddress;
	private String odinId;

	public AdTarget(Context context, String yourAppId) {
		areaId = yourAppId;
		
		deviceId = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
		
		deviceModel = Build.MODEL;
		deviceBrand = Build.BRAND;
		deviceOSVersion = "Android " + Build.VERSION.RELEASE;
		
		country = Locale.getDefault().getDisplayCountry();
		
		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		deviceDensity = metrics.density;
		screenWidth = metrics.widthPixels;
		screenHeight = metrics.heightPixels;
		
		isTablet = isTablet(context);
		
		// Get MAC-address of device
		WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wInfo = wifiManager.getConnectionInfo();
		macAddress = wInfo.getMacAddress(); 
		
		// Get ODIN-1 identifier
		odinId = ODIN.getODIN1(context);
	}
	
	private boolean isTablet(Context context) {
		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		
		int minimalSize = metrics.widthPixels > metrics.heightPixels ? metrics.heightPixels : metrics.widthPixels;
		minimalSize += (Constants.STATUS_BAR_HEIGHT * metrics.density);
		
		float widthInDP = minimalSize / metrics.density;
		if (widthInDP < Constants.WIDTH_IN_DP_RATIO) {
			// used handset
			return false;
		} else {
			// used tablets
			return true;
		}
	}
	
	public String getAreaId() {
		return areaId;
	}
	
	public String getDeviceId() {
		return deviceId;
	}

	public String getDeviceModel() {
		return deviceModel;
	}

	public String getDeviceBrand() {
		return deviceBrand;
	}

	public String getDeviceOSVersion() {
		return deviceOSVersion;
	}
	
	public float getDeviceDensity() {
		return deviceDensity;
	}

	public String getCountry() {
		return country;
	}

	public int getScreenWidth() {
		return screenWidth;
	}

	public int getScreenHeight() {
		return screenHeight;
	}

	public Location getLocation() {
		return location;
	}
	
	public void setLocation(Location location) {
		this.location = location;
	}
	
	public boolean isTablet() {
		return isTablet;
	}
	
	public String getMACAddress() {
		return macAddress;
	}
	
	public String getODIN() {
		return odinId;
	}
}
