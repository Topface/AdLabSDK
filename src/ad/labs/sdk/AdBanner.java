package ad.labs.sdk;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import ad.labs.sdk.ResponseParser.BannerType;
import ad.labs.sdk.consts.Constants;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.Html;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class AdBanner extends View {
	public interface OnCloseBannerListener {
		public void onClose();
	}
	private OnCloseBannerListener onCloseBannerListener;
	
	private static final int TEXT_PADDING = 3;
	
	private Bitmap bannerBitmap;
	private Rect bitmapSourceRect;
	private static Rect bitmapDestRect;
	
	private Bitmap closeButtonBitmap;
	private Rect closeButtonSourceRect;
	private Rect closeButtonDestRect;
	private int closeButtonSize;
	
	private Paint bitmapPaint;
	private Paint textPaint;
	
	private int bannerWidth;
	private int bannerHeight;
	
	private String bannerText;
	private String destinationUrl;
	private BannerType bannerType;

	public AdBanner(Context context) {
		super(context);
		init(context);
	}
	
	public AdBanner(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	
	@SuppressLint("NewApi") 
	public AdBanner(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}
	
	private void init(Context context) {
		setBackgroundColor(getResources().getColor(android.R.color.background_dark));
		
		bitmapPaint = new Paint(Paint.DITHER_FLAG);
		
		textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		textPaint.setColor(Color.WHITE);
		textPaint.setTextSize(getResources().getDimension(R.dimen.ad_text_size));
		textPaint.setTextAlign(Align.LEFT);
		
		bannerHeight = getBannerHeight();
		bitmapDestRect = new Rect(0, 0, bannerHeight, bannerHeight);
		
		closeButtonBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.close_button);
		closeButtonSourceRect = new Rect(0, 0, closeButtonBitmap.getWidth(), closeButtonBitmap.getHeight());
		closeButtonSize = (int) getResources().getDimension(R.dimen.close_button_size);
		closeButtonDestRect = new Rect(0, 0, closeButtonSize, closeButtonSize);
	}
	
	private int getBannerHeight() {
		DisplayMetrics metrics = getResources().getDisplayMetrics();
		int minimalSize = metrics.widthPixels > metrics.heightPixels ? metrics.heightPixels : metrics.widthPixels;
		minimalSize += (Constants.STATUS_BAR_HEIGHT * metrics.density);
		
		float widthInDP = minimalSize / metrics.density;
		int bannerHeight = 0;
		if (widthInDP <= Constants.WIDTH_IN_DP_RATIO) {
			// used handset
			bannerHeight = (int) getResources().getDimension(R.dimen.ad_banner_height_for_hadset);
		} else {
			// used tablets
			bannerHeight = (int) getResources().getDimension(R.dimen.ad_banner_height_for_tablet);
		}
		
		return bannerHeight;
	}
	
	private int getBannerWidth() {
		DisplayMetrics metrics = getResources().getDisplayMetrics();
		int minimalSize = metrics.widthPixels > metrics.heightPixels ? metrics.heightPixels : metrics.widthPixels;
		minimalSize += (Constants.STATUS_BAR_HEIGHT * metrics.density);
		
		float widthInDP = minimalSize / metrics.density;
		int bannerWidth = 0;
		if (widthInDP <= Constants.WIDTH_IN_DP_RATIO) {
			// used handset
			bannerWidth = (int) getResources().getDimension(R.dimen.ad_banner_width_for_handset);
		} else {
			// used tablets
			bannerWidth = (int) getResources().getDimension(R.dimen.ad_banner_width_for_tablet);
		}
		
		return bannerWidth;
	}

	public void setBanner(ResponseParser responseParser) {
		bannerText = responseParser.getArticle();
		destinationUrl = responseParser.getDestinationUrl();
		bannerType = responseParser.getBannerType();
		
		// set color for banner background and font if we drawing IMAGE_WITH_TEXT banner
		if (bannerType == BannerType.IMAGE_WITH_TEXT) {
			setBackgroundColor(responseParser.getBackgroundColor());
			textPaint.setColor(responseParser.getFontColor());
		}
		
		{
			Log.d("My", AdBanner.class.getSimpleName() + ": setBanner information...");
			Log.d("My", "  banner text: " + bannerText);
			Log.d("My", "  banner url: " + responseParser.getBitmapUrl());
		}
		
		new BannerImageLoader(responseParser.getBitmapUrl(), bannerText).execute();
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if (bannerBitmap != null || bannerText != null) {
			DisplayMetrics metrics = getResources().getDisplayMetrics();
			if (metrics.widthPixels > metrics.heightPixels) {
				// landscape orientation
				bannerWidth = getBannerWidth();
			} else {
				// portrait orientation
				bannerWidth = metrics.widthPixels;
			}
			
			int width = getPaddingLeft() + getPaddingRight() + bannerWidth;
			int height = getPaddingBottom() + getPaddingTop() + bannerHeight;
			
			setMeasuredDimension(resolveSize(width, widthMeasureSpec), 
					resolveSize(height, heightMeasureSpec));
		} else {
			setMeasuredDimension(0, 0);
		}
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		drawBannerBitmap(canvas);
		drawCloseButton(canvas);
	}
	
	private void drawBannerBitmap(Canvas canvas) {
		if (bannerType == BannerType.IMAGE_WITH_TEXT) {
			drawMultilineText(canvas, bannerText, textPaint);
			
			if (bannerBitmap !=  null) {
				fillBitmapDestRect(0, 0, bannerHeight, bannerHeight);
				canvas.drawBitmap(bannerBitmap, bitmapSourceRect, bitmapDestRect, bitmapPaint);
			}
		}
		
		if (bannerType == BannerType.FULLSCREEN_IMAGE) {
			if (bannerBitmap != null) {
				fillBitmapDestRect(0, 0, bannerWidth, bannerHeight);
				canvas.drawBitmap(bannerBitmap, bitmapSourceRect, bitmapDestRect, bitmapPaint);
			}
		}
	}
	
	private static void fillBitmapDestRect(int left, int top, int right, int bottom) {
		bitmapDestRect.left = left;
		bitmapDestRect.top = top;
		bitmapDestRect.right = right;
		bitmapDestRect.bottom = bottom;
	}
	
	private void drawCloseButton(Canvas canvas) {
		closeButtonDestRect.left = bannerWidth - closeButtonSize;
		closeButtonDestRect.top = 0;
		closeButtonDestRect.right = bannerWidth;
		closeButtonDestRect.bottom = closeButtonSize;
		
		canvas.drawBitmap(closeButtonBitmap, closeButtonSourceRect, closeButtonDestRect, bitmapPaint);
	}
	
	private static Rect rect = new Rect();
	private int getTextHeight(String text, Paint textPaint) {
		if (text != null && !text.equals("")) { 
			textPaint.getTextBounds(text, 0, text.length()-1, rect);
			return rect.bottom - rect.top;
		} else 
			return 0;
	}
	
	private void drawMultilineText(Canvas canvas, String text, Paint textPaint) {
		int bitmapWidht = bitmapDestRect.right - bitmapDestRect.left;
		float textX = bitmapWidht + TEXT_PADDING*3;
		int textHeight = getTextHeight(bannerText, textPaint);
		float textY = textHeight + TEXT_PADDING;
		
		ArrayList<String> lines = createMultiLines(text);
		try {
			for (String line : lines) {
				canvas.drawText(Html.fromHtml(line).toString(), textX, textY, textPaint);
				textY += textHeight + TEXT_PADDING;
			}
		} catch (Exception e) {
			Log.e("My", "error: " + e.getMessage());
		}
	}
	
	private ArrayList<String> createMultiLines(String text) {
		ArrayList<String> result = new ArrayList<String>();
		String[] words = text.split(" ");
		String temp = "";
		
		for (int i = 0; i != words.length; ++i) {
			temp += (words[i] + " ");
			int lineWidth = (int) (bannerHeight + TEXT_PADDING + textPaint.measureText(temp));
			
			if (lineWidth >= (bannerWidth-closeButtonSize)) {
				if (i != words.length-1) {
					int offset = temp.length() - words[i].length()-1;
					result.add(temp.substring(0, offset));
					temp = words[i] + " ";
				} 
				
				if (i == words.length-1) {
					int offset = temp.length() - words[i].length()-1;
					String stringWithoutLastWord = temp.substring(0, offset);
					
					if (lineWidth <= (bannerWidth-closeButtonSize)) {
						String stringWithLastWord = stringWithoutLastWord + " " + words[i];
						lineWidth = (int) (bannerHeight + TEXT_PADDING + textPaint.measureText(stringWithLastWord)); 
						if (lineWidth <= (bannerWidth-closeButtonSize))
							result.add(stringWithLastWord);
						else
							result.add(stringWithoutLastWord);
					} else {
						// add line without last word
						result.add(stringWithoutLastWord);
					
						// add last word at lines
						result.add(words[i]);
					}
				}
			}
			
			if (i == words.length-1) {
				result.add(temp);
			}
		}
		
		return result;
	}
	
	public void setOnCloseBannerListener(OnCloseBannerListener onCloseBannerListener) {
		this.onCloseBannerListener = onCloseBannerListener;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float x = event.getX();
		
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			// Checking for touch on 'close' button area...
			if (x >= (bannerWidth-closeButtonSize) && x <= bannerWidth) {
				onCloseBannerListener.onClose();
				bannerBitmap = null;
				
				requestLayout();
				invalidate();
			} else {
				// Click on banner was detected, then open a browser with URL
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(destinationUrl));
				getContext().startActivity(i);
			}
			
			return true;
		}
		
		return super.onTouchEvent(event);
	}
	
	private class BannerImageLoader extends AsyncTask<Void, Void, Bitmap> {
		private String bitmapUrl;
		
		public BannerImageLoader(String bitmapUrl, String article) {
			this.bitmapUrl = bitmapUrl;
		}

		@Override
		protected Bitmap doInBackground(Void... params) {
			InputStream in;
			try {
				in = new URL(bitmapUrl).openStream();
				Bitmap bitmap = BitmapFactory.decodeStream(in);
				return bitmap;
			} catch (MalformedURLException e) {
				Log.e("My", "ImageLoader: MalformedURLException " + e.getMessage());
				return null;
			} catch (IOException e) {
				Log.e("My", "ImageLoader: IOException " + e.getMessage());
				return null;
			}
		}
		
		@Override
		protected void onPostExecute(Bitmap bitmap) {
			super.onPostExecute(bitmap);
			if (bitmap != null) {
				bannerBitmap = bitmap;
				bitmapSourceRect = new Rect(0, 0, bannerBitmap.getWidth(), bannerBitmap.getHeight());
			} else {
				bannerBitmap = null;
			}
			
			requestLayout();
			invalidate();
		}
	}
}