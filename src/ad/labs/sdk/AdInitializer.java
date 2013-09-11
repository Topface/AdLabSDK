package ad.labs.sdk;

import ad.labs.sdk.tasks.BannerLoader.OnBannerRequestListener;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Message;
import android.util.Log;

public class AdInitializer {
	private static final String DEBUG_TAG = "My";
	
	private AdHandler adHandler;
	
	private LocationManager locationManager;
	private String bestLocationProvider;
	
	public AdInitializer(Context context, AdBanner adBanner, String yourAppId) {
		adHandler = new AdHandler(context, adBanner, yourAppId);
		
		locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		bestLocationProvider = LocationManager.NETWORK_PROVIDER;
		
		Location lastKnownLocation = locationManager.getLastKnownLocation(bestLocationProvider);
		if (lastKnownLocation != null) {
			Log.d(DEBUG_TAG, "last known location found for provider - " + bestLocationProvider);
			notifyHandlerWithNewLocation(lastKnownLocation);
		}
	}
	
	public void setOnBannerRequestListener(OnBannerRequestListener listener) {
		adHandler.setOnBannerRequestListener(listener);
	}
	
	private void notifyHandlerWithNewLocation(Location location) {
		Message message = new Message();
		message.what = AdHandler.MESSAGE_LOCATION_UPDATE;
		message.obj = location;
			
		adHandler.sendMessage(message);
	}
	
	public void resume() {
		adHandler.sendMessage(adHandler.obtainMessage(AdHandler.MESSAGE_MAKE_AD_REQUEST));
	}
	
	public void pause() {
		adHandler.removeCallbacksAndMessages(null);
	}
	
	public void setRequestTimeout(long timeoutInMillis) {
		adHandler.setRequestDelay(timeoutInMillis);
	}
	
	public AdTarget getTarget() {
		return adHandler.getTarget();
	}
}
