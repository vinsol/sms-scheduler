package com.vinsol.SMSScheduler;
 
import java.util.Calendar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
 
public class ScheduleSMS extends Activity implements OnClickListener {
    final int DATE_DIALOG_ID = 1;
    final int TIME_DIALOG_ID = 2;
	
    EditText sendDateEditText, sendTimeEditText;
	
	Calendar currentTimeCalendar, scheduledTimeCalendar;
	
	int currentDate, currentMonth, currentYear;
	int currentHour, currentMinute;
    
    /**=========================================================== 
     * method onCreate()
     * Called when the activity is first created. 
     * ===========================================================*/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
    	super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
    	setContentView(R.layout.schedule_sms);        
        
    	//======================== setting current Date and time ===========================//
    	currentTimeCalendar = Calendar.getInstance();
    	currentDate = currentTimeCalendar.get(Calendar.DATE);
    	currentMonth = currentTimeCalendar.get(Calendar.MONTH);
    	currentYear = currentTimeCalendar.get(Calendar.YEAR);
    	//if we wants hour in 24 hour format then use HOUR_OF_DAY else use HOUR
    	currentHour = currentTimeCalendar.get(Calendar.HOUR_OF_DAY);
    	currentMinute = currentTimeCalendar.get(Calendar.MINUTE);
    	
    	
    	scheduledTimeCalendar = Calendar.getInstance();
        
        //======================== Send Date EditText ===============================//
        sendDateEditText = (EditText)findViewById(R.id.schedule_sms_send_date_edit_text);
        sendDateEditText.setInputType(InputType.TYPE_NULL);
        sendDateEditText.setOnClickListener(this);
        
        //========================== Send Time EditText ===============================//
        sendTimeEditText = (EditText)findViewById(R.id.schedule_sms_send_time_edit_text);
        sendTimeEditText.setInputType(InputType.TYPE_NULL);
        sendTimeEditText.setOnClickListener(this);
        
        //========================== Add phone Number Button ============================//
        Button addPhoneNumberButton = (Button)findViewById(R.id.schedule_sms_add_number_button);
        addPhoneNumberButton.setOnClickListener(this);
        
        //========================== Add from Contact Button ==========================//
        Button addFromContactButton = (Button)findViewById(R.id.schedule_sms_add_from_contact_button);
        addFromContactButton.setOnClickListener(this);
        
        //========================== Choose Message From Template Button ==========================//
        Button chooseMessageFromTemplateButton = (Button)findViewById(R.id.schedule_sms_message_from_template_button);
        chooseMessageFromTemplateButton.setOnClickListener(this);
        
        //========================== Schedule SMS Button ==========================//
        Button scheduleSMSButton = (Button) findViewById(R.id.schedule_sms_done_button);
        scheduleSMSButton.setOnClickListener(this);
                
    }//end method onCreate
    
    /**===============================================================
     * method onClick
     *================================================================*/
    @Override
	public void onClick(View clickedView) {
    	
    	int idOfClickedView = clickedView.getId();
    	
    	switch(idOfClickedView){
    		case R.id.schedule_sms_add_number_button: {
    			break;
    		}
    		case R.id.schedule_sms_add_from_contact_button: {
    			break;
    		}
    		case R.id.schedule_sms_message_from_template_button: {
    			break;
    		}
    		case R.id.schedule_sms_done_button: {
    			break;
    		}
    		case R.id.schedule_sms_send_date_edit_text: {
    			showDialog(DATE_DIALOG_ID);
    			break;
    		}
    		case R.id.schedule_sms_send_time_edit_text: {
    			showDialog(TIME_DIALOG_ID);
    			break;
    		}
    	}//end switch
    	
    }//end method onClick
    
    /**================================================================
     * method onCreateDialog
     *================================================================*/
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
	        case DATE_DIALOG_ID: {
	            return new DatePickerDialog(this, dateSetListenerObject, currentYear, currentMonth, currentDate);
	        }
	        case TIME_DIALOG_ID: {
	        	return new TimePickerDialog(this, timeSetListenerObject, currentHour, currentMinute, true);
	        }
        }//end switch
		return null;
    }//end method onCreateDialog
    
    /**==================================================================
     * object Of OnDateSetListener
     * the callback received when the user "sets" the date in the dialog
     *====================================================================*/ 
    private DatePickerDialog.OnDateSetListener dateSetListenerObject =
    	new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                //================== setting calendar date ======================//
            	scheduledTimeCalendar.set(Calendar.YEAR, year);
            	scheduledTimeCalendar.set(Calendar.MONTH, monthOfYear + 1);
            	scheduledTimeCalendar.set(Calendar.DATE, dayOfMonth);
            		
            	sendDateEditText.setText("" + scheduledTimeCalendar.get(Calendar.MONTH)
            							+ "/" + scheduledTimeCalendar.get(Calendar.DATE)
            							+ "/" + scheduledTimeCalendar.get(Calendar.YEAR));
            }
    };//end object dateSetListenerObject
    
    /**==================================================================
     * object Of OnTimeSetListener
     * the callback received when the user "sets" the time in the dialog
     *====================================================================*/ 
    private TimePickerDialog.OnTimeSetListener timeSetListenerObject =
    	new TimePickerDialog.OnTimeSetListener() {
    		
    		@Override
            public void onTimeSet(TimePicker view, int hour, int minute) {
                Log.v("in onTimeSet", "hour = " + hour);
                Log.v("in onTimeSet", "minute = " + minute);
                
    			//================== setting calendar time ======================//
            	scheduledTimeCalendar.set(Calendar.HOUR_OF_DAY, hour);
            	scheduledTimeCalendar.set(Calendar.MINUTE, minute);
            	scheduledTimeCalendar.set(Calendar.SECOND, 0);
            	scheduledTimeCalendar.set(Calendar.MILLISECOND, 0);
            	
            	Log.v("Current Time calendar millisecond = " , "" + currentTimeCalendar.getTimeInMillis());
            	Log.v("Scheduled Time calendar millisecond = ", "" + scheduledTimeCalendar.getTimeInMillis());
            	
            	Log.v("Current Time year = "  + currentTimeCalendar.get(Calendar.YEAR) , "Scheduled Time year = "  + scheduledTimeCalendar.get(Calendar.YEAR));
            	Log.v("Current Time month = "  + currentTimeCalendar.get(Calendar.MONTH) , "Scheduled Time month = "  + scheduledTimeCalendar.get(Calendar.MONTH));
            	Log.v("Current Time date = "  + currentTimeCalendar.get(Calendar.DATE) , "Scheduled Time date = "  + scheduledTimeCalendar.get(Calendar.DATE));
            	Log.v("Current Time hour = "  + currentTimeCalendar.get(Calendar.HOUR_OF_DAY) , "Scheduled Time hour = "  + scheduledTimeCalendar.get(Calendar.HOUR_OF_DAY));
            	Log.v("Current Time minute = "  + currentTimeCalendar.get(Calendar.MINUTE) , "Scheduled Time minute = "  + scheduledTimeCalendar.get(Calendar.MINUTE));
            	
            	String amORpm;
            	//int amORpm1 = scheduledTimeCalendar.get(Calendar.AM_PM);
            	//Log.v("amOrPm = ", "" + amORpm1);
            	
            	//if(hour < 12){
            	//	amORpm = "AM";
            	//} else {
            	//	amORpm = "PM";
            	//}
            	
            	//int extractedHour = scheduledTimeCalendar.get(Calendar.HOUR);
            	//int extractedMinute = scheduledTimeCalendar.get(Calendar.MINUTE);
            	
            	sendTimeEditText.setText("" + pad(hour)
            							+ ":" + pad(minute));
            								
    		}
	};//end object timeSetListenerObject
	
	private static String pad(int c) {
	    if (c >= 10)
	        return String.valueOf(c);
	    else
	        return "0" + String.valueOf(c);
	}
    
}//end class scheduleSMS
