package ad.labs.sdk;

import java.math.BigInteger;

import ad.labs.sdk.AdBanner.OnCloseBannerListener;
import ad.labs.sdk.tasks.BannerLoader;
import ad.labs.sdk.tasks.BannerLoader.OnBannerRequestListener;
import android.content.Context;
import android.location.Location;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class AdHandler extends Handler implements OnCloseBannerListener {
	public static final int MESSAGE_MAKE_AD_REQUEST = 1000;
	public static final int MESSAGE_LOCATION_UPDATE = 1001;
	
	private static final String DEBUG_TAG = "My";
	
	private static long DEFAULT_REQUEST_DELAY = 10000;
	private static long DELAY_FOR_RESTORE_AD_REQUESTS = 90000;
	
	private static final String XOR_AREA_ID_TO = "5123";
	
	private AdBanner adBanner;
	private long postRequestDelayInMillis;
	private String yourAppId;
	
	private AdTarget adTarget;
	private OnBannerRequestListener onBannerRequestListener;
	
	public AdHandler(Context context, AdBanner adBanner, String yourAppId) {
		this.adBanner = adBanner;
		this.adBanner.setOnCloseBannerListener(this);
		
		this.yourAppId = String.valueOf(xorString(yourAppId));
		Log.d(DEBUG_TAG, "XOR: " + this.yourAppId);
		postRequestDelayInMillis = DEFAULT_REQUEST_DELAY;
		
		// Get static information about device at constructor. This information never change,
		// then we can define it once.
		adTarget = new AdTarget(context, this.yourAppId);
	}
	
	private static BigInteger xorString(String from) {
		BigInteger one = new BigInteger(from);
		BigInteger two = new BigInteger(XOR_AREA_ID_TO);
		return one.xor(two);
	}
	
	public void setOnBannerRequestListener(OnBannerRequestListener listener) {
		onBannerRequestListener = listener;
	}

	@Override
	public void handleMessage(Message msg) {
		switch (msg.what) {
		case MESSAGE_MAKE_AD_REQUEST:
			BannerLoader bannerLoader = new BannerLoader(adBanner, adTarget, yourAppId);
			if (onBannerRequestListener != null)
				bannerLoader.setOnBannerResponseListener(onBannerRequestListener);
			bannerLoader.execute();
			
			sendMessageWithPOSTRequest(postRequestDelayInMillis);
			
			break;
			
		case MESSAGE_LOCATION_UPDATE:
			Log.d(DEBUG_TAG, "Handler message: location update");
			
			Location newLocation = (Location) msg.obj;
			adTarget.setLocation(newLocation);
			
			break;
			
		default:
			break;
		}
	}
	
	private void sendMessageWithPOSTRequest(long delayInMillis) {
		removeCallbacksAndMessages(null);
		sendMessageDelayed(obtainMessage(MESSAGE_MAKE_AD_REQUEST), delayInMillis);
	}
	
	public void setRequestDelay(long delayInMillis) {
		postRequestDelayInMillis = delayInMillis;
	}

	@Override
	public void onClose() {
		sendMessageWithPOSTRequest(DELAY_FOR_RESTORE_AD_REQUESTS);
	}
	
	public AdTarget getTarget() {
		return adTarget;
	}
}
