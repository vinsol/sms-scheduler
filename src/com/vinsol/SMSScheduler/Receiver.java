package com.vinsol.SMSScheduler;


/**=========================================================
 * A model object containing contact data.
 *==========================================================*/
public class Receiver {

    private String displayName;
    private String phoneNumber;

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
}//end class Receiver