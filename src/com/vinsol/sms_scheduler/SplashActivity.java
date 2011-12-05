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

public class SplashActivity extends Activity {

	static ArrayList<MyContact> contactsList = new ArrayList<MyContact>();
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash_layout);
		
		ContactsAsync contactsAsync = new ContactsAsync();
		contactsAsync.execute();
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
			Intent intent = new Intent(SplashActivity.this, SmsSchedulerExplActivity.class);
			//intent.putExtra("ORIGIN", "new");
			SplashActivity.this.finish();
			startActivity(intent);
		}
	}
	
	public void loadContactsData(){
		contactsList.clear();
		ContentResolver cr = getContentResolver();
	    Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
	    if(cursor.moveToFirst()){
	    	do{
	    	  if(!(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)).equals("0"))){
	    		String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
	    	    
	    	    Cursor phones = cr.query(Phone.CONTENT_URI, null, Phone.CONTACT_ID + " = " + id, null, null);
	    	    if(phones.moveToFirst()){
	    	    	MyContact contact = new MyContact();
		    		contact.content_uri_id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
		    		contact.name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
		    		contact.number = " ";
	    	    	contact.number = phones.getString(phones.getColumnIndex(Phone.NUMBER));
	    	    	
	    	    	Cursor cur = managedQuery(ContactsContract.Data.CONTENT_URI, new String[]{ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID}, ContactsContract.CommonDataKinds.GroupMembership.CONTACT_ID + "=" + contact.content_uri_id, null, null);
	    	    	
	    	    	if(cur.moveToFirst()){
	    	    		do{
	    	    			contact.groupRowId.add(cur.getLong(cur.getColumnIndex(ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID)));
	    	    		}while(cur.moveToNext());
	    	    	}
	    	    	
	    	    	Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long.parseLong(contact.content_uri_id));
		    	    InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(cr, uri);
		    	    try{
		    	    	contact.image = BitmapFactory.decodeStream(input);
		    	    	contact.image.getHeight();
		    	    } catch (NullPointerException e){
		    	    	contact.image = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.no_image_thumbnail);
		    	    }
		    	    
		    	    
		    	    contactsList.add(contact);
		    	    
		    	    
		    	    //Log.i("MSG", contact.groupRowId.size() + "");
		    	    
	    	    }
	    	  }  
	    	}while(cursor.moveToNext());
	    }
	}
	
	
	
//	public void loadContactsData(){
//		Uri myUri = ContactsContract.Contacts.CONTENT_URI;
//		Cursor cursor = managedQuery(myUri, null, null, null, ContactsContract.Contacts.DISPLAY_NAME);
//		Cursor phCursor;
//		ContentResolver cr = getContentResolver();
//		if(cursor.moveToFirst()){
//			Log.i("MSG", "total contacts : " + cursor.getCount());
//			String numberString = "";
//			while(cursor.moveToNext()){
//				if(!(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)).equals("0"))){
//					MyContact contact = new MyContact();
//					contact.content_uri_id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
//					String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
//					int idInt = Integer.parseInt(contact.content_uri_id);
//					phCursor = cr.query(
//				 		    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
//				 		    null, 
//				 		    ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?", 
//				 		    new String[]{id}, null);
//					
//					Log.i("MSG", "size of PhCursor : " + phCursor.getCount());
//					if(phCursor.moveToFirst()){
//						contact.number = phCursor.getString(phCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
//					}
//					Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long.parseLong(contact.content_uri_id));
//				    InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(cr, uri);
//				    contact.image = BitmapFactory.decodeStream(input);    
//					contact.name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
//					contactsList.add(contact);
//				}else{
//					//numberString = "Not Available";
//				}
//				
//			}
//		}
//	}
	
}