package com.vinsol.SMSScheduler.ContactAppManager;

import java.io.InputStream;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.CommonDataKinds.Phone;

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
    public Receiver loadContact(ContentResolver contentResolver, Uri contactUri) {
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
        cursor = contentResolver.query(Phone.CONTENT_URI,
                new String[]{Phone.NUMBER, Phone.PHOTO_ID },
                Phone.CONTACT_ID + "=" + contactId, null, Phone.IS_SUPER_PRIMARY + " DESC");
        try {
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
    
    /**============================================================================
     * method openPhoto
     * @param contentResolver
     * @param contactId
     * @return
     *=============================================================================*/
    public InputStream openPhoto(ContentResolver contentResolver, long contactId) {
    	Uri contactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId);
    	
    	return Contacts.openContactPhotoInputStream(contentResolver, contactUri);	
    }
    	
}//end class ContactAccessorSdk5