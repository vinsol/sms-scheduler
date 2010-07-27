package com.vinsol.SMSScheduler;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Window;

public class SplashScreen extends Activity {
    /**================================================================
     *  method onCreate
     *  Called when the activity is first created. 
     * =============================================================== */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.splash_screen);
        
        new AsyncTaskSubClass(this).execute((Void[])null);
    }//end method onCreate
    
    
    /**================================================================
     *  Inner Class AsyncTaskSubClass 
     * =============================================================== */
    public class AsyncTaskSubClass extends AsyncTask<Void, Void, Boolean > {
        
    	Context ctx;
    	ProgressDialog splashScreenProgressDialog;
    	
    	/**====================================================================================   
		  * Constructor   
		  * ====================================================================================*/  
		AsyncTaskSubClass(Context ctx) {
			this.ctx = ctx;
		}//end constructor
		
    	
		 /**====================================================================================   
		  * method onPreExecute   
		  * ====================================================================================*/  
		 @Override
		 protected void onPreExecute() {
			 String shownOnProgressDialog = ctx.getString(R.string.progress_dialog_message_splash_screen); 
			 splashScreenProgressDialog = new ProgressDialog(ctx);
			 splashScreenProgressDialog.setMessage(shownOnProgressDialog);	
			 splashScreenProgressDialog.show();
		 }//end method onPreExecute
	
		 /**====================================================================================   
		  * method doInBackground   
		  * ====================================================================================*/   
		 @Override   
		 protected Boolean doInBackground(Void... v) {   
		   
		     boolean isDBExist = new SMSSchedulerDBHelper(ctx).checkOrCreateDB();
			 return isDBExist;  
		  
		 }//end method doInBackground()
	
			
		 /**====================================================================================   
		  * method onPostExecute   
		  * ====================================================================================*/  
		 @Override
		 protected void onPostExecute(Boolean isDBExist){
			 splashScreenProgressDialog.dismiss();
			 
			 if(isDBExist) {
				 new IntentHandler().gotoSMSListingTabActivity(ctx, Constant.SELECTED_TAB_SCHEDULED_SMS);
			 } else{
				 String alertDialogHeading = ctx.getString(R.string.alert_dialog_heading_db_not_exist);
				 String alertDialogMessage = ctx.getString(R.string.alert_dialog_message_db_not_exist);
				 
				 new ShowAlertDialog(ctx, alertDialogHeading, alertDialogMessage, true).showDialog();
			 }
			 				
		 }//end method onPostExecute

    }//end inner class AsyncTaskSubClass
    
}//end class SplashScreen