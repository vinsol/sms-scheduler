package com.vinsol.SMSScheduler.ContactAppManager;

/**=========================================================
 * A model object containing contact data.
 *==========================================================*/
public class ContactInfo {

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
}//end class ContactInfo