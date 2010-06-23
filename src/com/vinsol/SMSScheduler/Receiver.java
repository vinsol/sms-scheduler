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
}//end class Receiver