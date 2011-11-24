package com.vinsol.sms_scheduler;

import java.io.InputStream;
import java.util.ArrayList;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.widget.Toast;

public class SplashActivity extends Activity {

	static ArrayList<MyContact> contactsList = new ArrayList<MyContact>();
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash_layout);
		
		ContactsAsync contactsAsync = new ContactsAsync();
		contactsAsync.execute();
		
	}
	
	
	public void loadContactsData(){
		contactsList.clear();
		ContentResolver cr = getContentResolver();
	    Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
	    if(cursor.moveToFirst()){
	    	do{
	    		MyContact contact = new MyContact();
	    		contact.content_uri_id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
	    		contact.name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
	    	    
	    	    Cursor phones = cr.query(Phone.CONTENT_URI, null, Phone.CONTACT_ID + " = " + contact.content_uri_id, null, null);
	    	    if(phones.moveToFirst()){
	    	    	contact.number = phones.getString(phones.getColumnIndex(Phone.NUMBER));
	    	    	contactsList.add(contact);
	    	    	Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long.parseLong(contact.content_uri_id));
		    	    InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(cr, uri);
		    	    contact.image = BitmapFactory.decodeStream(input);
	    	    }
	    	    
	    	}while(cursor.moveToNext());
	    }
	}
	
	
	class ContactsAsync extends AsyncTask<Void, Void, Void>{

		@Override
		protected Void doInBackground(Void... params) {
			loadContactsData();
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			Toast.makeText(SplashActivity.this, "Data Loaded", Toast.LENGTH_SHORT).show();
			Intent intent = new Intent(SplashActivity.this, SmsSchedulerExplActivity.class);
			SplashActivity.this.finish();
			startActivity(intent);
			
		}
	}
}
