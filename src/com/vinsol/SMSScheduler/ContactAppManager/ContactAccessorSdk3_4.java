package com.vinsol.SMSScheduler.ContactAppManager;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.Contacts.People;
import android.provider.Contacts.People.Phones;

import com.vinsol.SMSScheduler.Constant;
import com.vinsol.SMSScheduler.R;
import com.vinsol.SMSScheduler.Receiver;

/**=========================================================================================
 * An implementation of {@link ContactAccessor} that uses legacy Contacts API.
 * These APIs are deprecated and should not be used unless we are running on a
 * pre-Eclair SDK.
 * =========================================================================================*/
@SuppressWarnings("deprecation")
public class ContactAccessorSdk3_4 extends ContactAccessor {

    /**===================================================================================
     * method getpickContact
     * Returns a Pick Contact intent using the pre-Eclair "people" URI.
     *====================================================================================*/
    @Override
    public Intent getPickContactIntent() {
        return new Intent(Intent.ACTION_PICK, People.CONTENT_URI);
    }//end method getPickContact

    /**===================================================================================
     * method loadContact
     * Retrieves the contact information.
     *====================================================================================*/
    @Override
    public Receiver loadContact(Context context, ContentResolver contentResolver, Uri contactUri) {
        Receiver contactInfo = new Receiver();
        long contactId = -1;
        
        Cursor cursor = contentResolver.query(contactUri,
                new String[]{People._ID, People.DISPLAY_NAME}, null, null, null);
        try {
            if (cursor.moveToFirst()) {
            	contactId = cursor.getLong(0);
                contactInfo.setDisplayName(cursor.getString(1));
            }
        } finally {
            cursor.close();
        }

        Uri phoneUri = Uri.withAppendedPath(contactUri, Phones.CONTENT_DIRECTORY);
        cursor = contentResolver.query(phoneUri, new String[]{Phones.NUMBER, Phones.TYPE}, null, null, Phones.ISPRIMARY + " DESC");

        try {
        	int i=0;
    		int phoneNumberCount = cursor.getCount();
            String[] phoneNumberArray = new String[phoneNumberCount];
    		String[] phoneTypeArray = new String[phoneNumberCount];
    		if(cursor.moveToFirst()){
	    		do {
	    			phoneNumberArray[i] = cursor.getString(cursor.getColumnIndex(Phones.NUMBER));
	    			String phoneType = cursor.getString(cursor.getColumnIndex(Phones.TYPE));
	    			phoneTypeArray[i] = convertPhoneTypeValueToString(phoneType);
	    			i++;
	    		}while (cursor.moveToNext());         	
	    		contactInfo.setPhoneNumber(phoneNumberArray[0]);
	    		contactInfo.setPhoneType(phoneTypeArray[0]);
	    		contactInfo.setPhoneNumberArray(phoneNumberArray);
	    		contactInfo.setPhoneTypeArray(phoneTypeArray);
    		}

        } finally {
            cursor.close();
        }
        
      //Load contact Image (if any).
        Bitmap bitmapImage = openPhoto(context, contentResolver, contactId);
        
    	contactInfo.setContactImage(bitmapImage);
        
        return contactInfo;
    }//end method LoadContact
    
    /**===============================================================================
     * method loadContactFromContactNumber
     * Retrieves the contact information.
     *================================================================================*/
    @Override
    public Receiver loadContactFromContactNumber(Context context, String contactNumber){
    	return null;
    }
    
    /**============================================================================
     * method convertPhoneTypeValueToString
     *=============================================================================*/
    String convertPhoneTypeValueToString (String numericValue) {
    	int phoneTypeNumeric = Integer.parseInt(numericValue);
    	String phoneType = null;
    	switch(phoneTypeNumeric) {
	    	case Phones.TYPE_CUSTOM:
	    		phoneType = "Custom";
	    		break;
	    	case Phones.TYPE_FAX_HOME:
	    		phoneType = "Fax Home";
	    		break;
	    	case Phones.TYPE_FAX_WORK:
	    		phoneType = "Fax Work";
	    		break;
	    	case Phones.TYPE_HOME:
	    		phoneType = "Home";
	    		break;
	    	case Phones.TYPE_MOBILE:
	    		phoneType = "Mobile";
	    		break;
	    	case Phones.TYPE_OTHER:
	    		phoneType = "Other";
	    		break;
	    	case Phones.TYPE_PAGER:
	    		phoneType = "Pager";
	    		break;
	    	case Phones.TYPE_WORK:
	    		phoneType = "Work";
	    		break;
    		default:
    			phoneType = Constant.UNKNOWN_TYPE;
    	}//end switch
    	return phoneType;
    }
    
    /**============================================================================
     * method openPhoto
     * @param Context
     * @param contentResolver
     * @param contactId
     * @return
     *=============================================================================*/
    public Bitmap openPhoto(Context context, ContentResolver contentResolver, long contactId) {
    	Uri contactUri = ContentUris.withAppendedId(People.CONTENT_URI, contactId);
    	
    	return People.loadContactPhoto(context, contactUri, R.drawable.icon, null);
    }
    
}//end class ContactAccessorSdk3_4