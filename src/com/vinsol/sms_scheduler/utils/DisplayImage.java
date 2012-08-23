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
    	Log.d("MSG", "Executor Shutdown.....");
    }
	
	
	public void submitImage(final ImageView iv, long contactId, final Context context){
		iv.setImageBitmap(BitmapFactory.decodeResource(context.getApplicationContext().getResources(), R.drawable.no_image_thumbnail));
		MyRunnable runnable = new MyRunnable(iv, contactId, context);
		executor.execute(runnable);
	}
	
	
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
    		
    		Runnable action = new Runnable() {	
				public void run() {
					if(b!=null)
						iv.setImageBitmap(b);
				}
			};
    		Activity activity = (Activity) context;
    		activity.runOnUiThread(action);
		}
    }
	
	
	public Bitmap addImage(long contactId, ContentResolver cr, Context context){
		Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
  	  	InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(cr, uri);
  		BitmapFactory.Options o = new BitmapFactory.Options();
  		o.inPurgeable = true;
  		o.inInputShareable = true;
  		Bitmap b = BitmapFactory.decodeStream(input, null, o);

  		return b;
	}
}
