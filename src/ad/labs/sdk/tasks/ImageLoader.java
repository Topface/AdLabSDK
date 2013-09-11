package ad.labs.sdk.tasks;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

public class ImageLoader extends AsyncTask<String, Void, Bitmap> {
	private ImageView imageView;
	
	public ImageLoader(ImageView destImageView) {
		imageView = destImageView;
	}

	@Override
	protected Bitmap doInBackground(String... urls) {
		InputStream in;
		try {
			in = new URL(urls[0]).openStream();
			Bitmap bitmap = BitmapFactory.decodeStream(in);
			return bitmap;
		} catch (MalformedURLException e) {
			return null;
		} catch (IOException e) {
			return null;
		}
	}

	@Override
	protected void onPostExecute(Bitmap bitmap) {
		super.onPostExecute(bitmap);
		
		if (bitmap != null)
			imageView.setImageBitmap(bitmap);
	}
}
