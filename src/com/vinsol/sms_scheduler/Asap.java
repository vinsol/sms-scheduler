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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class Asap extends Activity {

	Button b;
	static ArrayList<MyContact> contactsList = new ArrayList<MyContact>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.asap);
		
		ContactsAsync contactsAsync = new ContactsAsync();
		contactsAsync.execute();
		
		b = (Button) findViewById(R.id.asap_button);
		b.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Asap.this, ContactsListActivity.class);
				startActivityForResult(intent, 10);
			}
		});
		
	}
	
	
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if(requestCode == 10){
			ArrayList<Integer> ids = new ArrayList<Integer>();
			ids = data.getIntegerArrayListExtra("IDSLIST");
			for(int i = 0; i< contactsList.size(); i++){
				if(ids.get(i)==1){
					contactsList.get(i).checked = true;
				}else{
					contactsList.get(i).checked = false;
				}
				 
			}
		}
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
	    //Toast.makeText(Asap.this, contactsList.size()+"", Toast.LENGTH_SHORT).show();
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
			Toast.makeText(Asap.this, "Data Loaded", Toast.LENGTH_SHORT).show();
		}
	}
}
