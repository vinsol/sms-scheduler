package com.vinsol.SMSScheduler;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class ShowAlertDialog {
	
	Context ctx;
	String titleOfDialog, messageOnDialog;
	boolean onOkClickFinishActivity = false;
	
	/**=======================================================================================
	 * Constructor ShowAlertDialog 
	 * @param ctx
	 * @param titleOfDialog
	 * @param messageOnDialog
	 *=========================================================================================*/
	public ShowAlertDialog(Context ctx, String titleOfDialog, String messageOnDialog){
		this.ctx = ctx;
		this.titleOfDialog = titleOfDialog;
		this.messageOnDialog = messageOnDialog;
	}
	
	/**=======================================================================================
	 * 2nd Constructor ShowAlertDialog 
	 * @param ctx
	 * @param titleOfDialog
	 * @param messageOnDialog
	 * @param onOkClickFinishActivity
	 *=========================================================================================*/
	public ShowAlertDialog(Context ctx, String titleOfDialog, String messageOnDialog, Boolean onOkClickFinishActivity){
		this(ctx, titleOfDialog, messageOnDialog);
		this.onOkClickFinishActivity = onOkClickFinishActivity;
	}
	
	/**=======================================================================================
	 * method showDialog
	 * =======================================================================================*/
	public boolean showDialog(){
		AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
		builder.setTitle(titleOfDialog).setMessage(messageOnDialog)
		       .setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                dialog.dismiss();
		                if(onOkClickFinishActivity){
		                	((Activity)ctx).finish();
		                }
		           }
		       });
		AlertDialog alert = builder.create();
		alert.show();
		return true;
	}//end method showDialog
}//end class ShowAlertDialog