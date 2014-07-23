package com.vinsol.sms_scheduler.utils;

import java.io.InputStream;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.vinsol.sms_scheduler.Constants;
import com.vinsol.sms_scheduler.R;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.ImageView;

public class DisplayImage {

	ExecutorService executor = Executors.newFixedThreadPool(5);

	public void shutDownExecutor(){
    	executor.shutdownNow();
    	Log.d("MSG", "Display Image: Executor Shutdown.....");
    }


	
	/**
	 * @details starts off assigning of an image to an ImageView on a background thread.
	 * @param iv
	 * @param contactId
	 * @param context
	 */
	public void submitImage(final ImageView iv, long contactId, final Context context){
		iv.setImageBitmap(BitmapFactory.decodeResource(context.getApplicationContext().getResources(), R.drawable.no_image_thumbnail));
		MyRunnable runnable = new MyRunnable(iv, contactId, context);
		executor.execute(runnable);
	}



	/**
	 * @details stores image for a particular contactId into a HashMap that is shown in the list. If no image is available for the
	 * 			contact, it is assigned a default thumbnail for "No Image".
	 * @param contactId
	 * @param childParameters
	 * @param context
	 */
	public void storeImage(final long contactId, final HashMap<String, Object> childParameters, final Context context){
		Runnable action = new Runnable(){
			public void run(){
				Bitmap b = addImage(contactId, context.getContentResolver(), context);
				if(b!=null)
					childParameters.put(Constants.CHILD_IMAGE, b);
				else
					childParameters.put(Constants.CHILD_IMAGE, BitmapFactory.decodeResource(context.getApplicationContext().getResources(), R.drawable.no_image_thumbnail));
			}
		};
		executor.execute(action);
	}


	/**
	 * @details runs on another thread and adds an image to an ImageView. First it gets the bitmap against a particular contactId, 
	 * 			then puts it into an ImageView.
	 */
	class MyRunnable implements Runnable{

    	ImageView iv;
    	long contactId;
    	Context context;
    	public MyRunnable(ImageView iv, long contactId, Context context){
    		this.iv = iv;
    		this.contactId = contactId;
    		this.context = context;
    	}

		public void run() {
			final Bitmap b = addImage(contactId, context.getContentResolver(), context);
    		
			if(b!=null) {
				Runnable action = new Runnable() {	
					public void run() {
							iv.setImageBitmap(b);
					}
				};
				
				Activity activity = (Activity) context;
	    		activity.runOnUiThread(action);
			}
		}
    }


	/**
	 * @details to get image against a particular contactId that is stored in ContactsContract database.
	 * @param contactId
	 * @param cr
	 * @param context
	 * @return a bitmap image.
	 */
	private Bitmap addImage(long contactId, ContentResolver cr, Context context) {
		try {
			Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
	  	  	InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(cr, uri);
	  		BitmapFactory.Options o = new BitmapFactory.Options();
	  		o.inPurgeable = true;
	  		o.inInputShareable = true;
	  		Bitmap b = BitmapFactory.decodeStream(input, null, o);

	  		return b;
		} catch(OutOfMemoryError oome) {
			oome.printStackTrace();
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
