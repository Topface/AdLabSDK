package ad.labs.sdk;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class AdBanner2 extends LinearLayout {
	private static final String DEBUG_TAG = "My";

	public AdBanner2(Context context) {
		super(context);
		initializeBannerView(context);
	}

	public AdBanner2(Context context, AttributeSet attrs) {
		super(context, attrs);
		initializeBannerView(context);
	}
	
	@SuppressLint("NewApi") 
	public AdBanner2(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initializeBannerView(context);
	}
	
	private void initializeBannerView(Context context) {
		setBackgroundColor(Color.BLACK);
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
	}
}
