package com.vinsol.SMSScheduler;

import android.graphics.Bitmap;


/**=========================================================
 * A model object containing contact data.
 *==========================================================*/
public class Receiver {

    private String displayName;
    private String phoneNumber;
    
    private Bitmap contactImage;

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    @Override
    public boolean equals(Object o) {
    	if (this == o) {
    		return true;
    	}
    	if (o == null || getClass() != o.getClass()) {
    		return false;
    	}

    	final Receiver receiver = (Receiver) o;

    	if(this.displayName.equalsIgnoreCase(receiver.displayName) && this.phoneNumber.equalsIgnoreCase(receiver.phoneNumber)) {
    		return true;
    	} else {
    		return false;
    	}
	}//end method equals   

	public void setContactImage(Bitmap contactImage) {
		this.contactImage = contactImage;
	}

	public Bitmap getContactImage() {
		return contactImage;
	}
}//end class Receiver