package com.vinsol.SMSScheduler.ContactAppManager;

import java.io.InputStream;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.CommonDataKinds.Phone;

import com.vinsol.SMSScheduler.Constant;
import com.vinsol.SMSScheduler.Receiver;

/**==============================================================================================
 * An implementation of {@link ContactAccessor} that uses current Contacts API.
 * This class should be used on Eclair or beyond, but would not work on any earlier
 * release of Android.  As a matter of fact, it could not even be loaded.
 *===============================================================================================*/
public class ContactAccessorSdk5 extends ContactAccessor {

    /**=============================================================================
     * method getPickContactIntent
     * Returns a Pick Contact intent using the Eclair "contacts" URI.
     *==============================================================================*/
    @Override
    public Intent getPickContactIntent() {
        return new Intent(Intent.ACTION_PICK, Contacts.CONTENT_URI);
    }//end method getPickContactIntent

    /**===============================================================================
     * method loadContact
     * Retrieves the contact information.
     *================================================================================*/
    @Override
    public Receiver loadContact(Context context, ContentResolver contentResolver, Uri contactUri) {
        Receiver contactInfo = new Receiver();
        long contactId = -1;
        
        // Load the display name for the specified person
        Cursor cursor = contentResolver.query(contactUri,
                new String[]{Contacts._ID, Contacts.DISPLAY_NAME}, null, null, null);
        try {
            if (cursor.moveToFirst()) {
                contactId = cursor.getLong(0);
                contactInfo.setDisplayName(cursor.getString(1));
            }
        } finally {
            cursor.close();
        }
        

        // Load the phone number (if any).
        cursor = contentResolver.query(Phone.CONTENT_URI, new String[]{Phone.NUMBER, Phone.TYPE},
                Phone.CONTACT_ID + "=" + contactId, null, Phone.IS_SUPER_PRIMARY + " DESC");
        try {
        	int i=0;
    		int phoneNumberCount = cursor.getCount();
            String[] phoneNumberArray = new String[phoneNumberCount];
    		String[] phoneTypeArray = new String[phoneNumberCount];
    		if(cursor.moveToFirst()){
	    		do {
	    			phoneNumberArray[i] = cursor.getString(cursor.getColumnIndex(Phone.NUMBER));
	    			String phoneType = cursor.getString(cursor.getColumnIndex(Phone.TYPE));
	    			phoneTypeArray[i] = convertPhoneTypeValueToString(phoneType);
	    			i++;
	    		}while (cursor.moveToNext());         	
	    		contactInfo.setPhoneNumber(phoneNumberArray[0]);
	    		contactInfo.setPhoneType(phoneTypeArray[0]);
	    		contactInfo.setPhoneNumberArray(phoneNumberArray);
	    		contactInfo.setPhoneTypeArray(phoneTypeArray);
    		}
            if (cursor.moveToFirst()) {
                contactInfo.setPhoneNumber(cursor.getString(0));
            }
        } finally {
            cursor.close();
        }    
        
        //Load contact Image (if any).
        InputStream is = openPhoto(contentResolver, contactId);
        if(is != null) {
        	Bitmap bitmapimage = BitmapFactory.decodeStream(is);
        	contactInfo.setContactImage(bitmapimage);
        }
        return contactInfo;
    }//end method loadContact
    
    
    /**===============================================================================
     * method loadContactFromContactNumber
     * Retrieves the contact information.
     *================================================================================*/
    public Receiver loadContactFromContactNumber(Context context, String contactNumber) {
        Receiver contactInfo = new Receiver();
        long contactId = -1;
        
        ContentResolver contentResolver = context.getContentResolver();
        
        Cursor cursor = contentResolver.query(Phone.CONTENT_URI, new String[]{Phone.CONTACT_ID, Phone.TYPE},
        		Phone.NUMBER + "='" + contactNumber + "'", null, null);
        
     
        try {
        	if(cursor.moveToFirst()){
				String phoneType = cursor.getString(cursor.getColumnIndex(Phone.TYPE));
				String phoneTypeString = convertPhoneTypeValueToString(phoneType);
				contactId = cursor.getLong(cursor.getColumnIndex(Phone.CONTACT_ID));
				
				contactInfo.setPhoneNumber(contactNumber);
	    		contactInfo.setPhoneType(phoneTypeString);
    		}
            if (cursor.moveToFirst()) {
                contactInfo.setPhoneNumber(cursor.getString(0));
            }
        } finally {
            cursor.close();
        }
        
        cursor = contentResolver.query(Contacts.CONTENT_URI, new String[]{Contacts.DISPLAY_NAME}, Contacts._ID + "='" + contactId + "'", null, null);
        try {
            if (cursor.moveToFirst()) {
                contactInfo.setDisplayName(cursor.getString(cursor.getColumnIndex(Contacts.DISPLAY_NAME)));
            }
        } finally {
            cursor.close();
        }
        
        //Load contact Image (if any).
        InputStream is = openPhoto(contentResolver, contactId);
        if(is != null) {
        	Bitmap bitmapimage = BitmapFactory.decodeStream(is);
        	contactInfo.setContactImage(bitmapimage);
        }
        
        return contactInfo;
    }//end class loadContactFromContactNumber
    
    /**============================================================================
     * method openPhoto
     * @param contentResolver
     * @param contactId
     * @return
     *=============================================================================*/
    public InputStream openPhoto(ContentResolver contentResolver, long contactId) {
    	Uri contactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId);
    	
    	return Contacts.openContactPhotoInputStream(contentResolver, contactUri);	
    }//end method openType
    
    /**============================================================================
     * method convertPhoneTypeValueToString
     *=============================================================================*/
    String convertPhoneTypeValueToString (String numericValue) {
    	int phoneTypeNumeric = Integer.parseInt(numericValue);
    	String phoneType = null;
    	switch(phoneTypeNumeric) {
	    	case Phone.TYPE_CUSTOM:
	    		phoneType = "Custom";
	    		break;
	    	case Phone.TYPE_FAX_HOME:
	    		phoneType = "Fax Home";
	    		break;
	    	case Phone.TYPE_FAX_WORK:
	    		phoneType = "Fax Work";
	    		break;
	    	case Phone.TYPE_HOME:
	    		phoneType = "Home";
	    		break;
	    	case Phone.TYPE_MOBILE:
	    		phoneType = "Mobile";
	    		break;
	    	case Phone.TYPE_OTHER:
	    		phoneType = "Other";
	    		break;
	    	case Phone.TYPE_PAGER:
	    		phoneType = "Pager";
	    		break;
	    	case Phone.TYPE_WORK:
	    		phoneType = "Work";
	    		break;
	    	case Phone.TYPE_ASSISTANT:
	    		phoneType = "Assistant";
	    		break;
	    	case Phone.TYPE_CALLBACK:
	    		phoneType = "Callback";
	    		break;
	    	case Phone.TYPE_CAR:
	    		phoneType = "Car";
	    		break;
	    	case Phone.TYPE_COMPANY_MAIN:
	    		phoneType = "Company Main";
	    		break;
	    	case Phone.TYPE_ISDN:
	    		phoneType = "ISDN";
	    		break;
	    	case Phone.TYPE_MMS:
	    		phoneType = "MMS";
	    		break;
	    	case Phone.TYPE_MAIN:
	    		phoneType = "Main";
	    		break;
	    	case Phone.TYPE_OTHER_FAX:
	    		phoneType = "Fax Other";
	    		break;
	    	case Phone.TYPE_RADIO:
	    		phoneType = "Radio";
	    		break;
	    	case Phone.TYPE_TELEX:
	    		phoneType = "Telex";
	    		break;
	    	case Phone.TYPE_WORK_MOBILE:
	    		phoneType = "Work Mobile";
	    		break;
	    	case Phone.TYPE_WORK_PAGER:
	    		phoneType = "Work Pager";
	    		break;
	    	case Phone.TYPE_TTY_TDD:
	    		phoneType = "TTY TDD";
	    		break;
	    	default:
    			phoneType = Constant.UNKNOWN_TYPE;
    	}//end switch
    	return phoneType;
    }
    	
}//end class ContactAccessorSdk5