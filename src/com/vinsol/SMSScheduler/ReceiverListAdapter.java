package com.vinsol.SMSScheduler;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ReceiverListAdapter extends ArrayAdapter<Receiver> {
	
	Context context;
	ArrayList<Receiver> receiverArrayList;
	
	public ReceiverListAdapter(Context context, ArrayList<Receiver> receiverArrayList) {
		super(context, 0, receiverArrayList);
		this.context = context;
    	this.receiverArrayList = receiverArrayList;
	}
	
   @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();

        // Inflate the views from XML
        View rowView = inflater.inflate(R.layout.schedule_sms_one_receiver_view, null);
        
        Receiver receiver = receiverArrayList.get(position);
        String contactDetail;
        
        if(receiver.getDisplayName().equalsIgnoreCase(Constant.UNKNOWN_NAME)) {
			contactDetail = receiver.getPhoneNumber();
		}else {
			contactDetail = receiver.getDisplayName();
		}
		
        Bitmap contactImage = receiver.getContactImage();
        
		Drawable contactDrawable;
		if(contactImage == null) {
			contactDrawable = context.getResources().getDrawable(R.drawable.icon);
		} else {
			contactDrawable = new BitmapDrawable(contactImage);
		}
		
        
        // Load the image and set it on the ImageView
        ImageView imageView = (ImageView) rowView.findViewById(R.id.schedule_sms_one_receiver_view_contact_image);
        imageView.setImageDrawable(contactDrawable);
       
        // Set the text on the TextView
        TextView contactDetailTextView = (TextView) rowView.findViewById(R.id.schedule_sms_one_reciever_view_contact_detail);
        contactDetailTextView.setText(contactDetail);
        
        // Set the type
        TextView contactTypeTextView = (TextView) rowView.findViewById(R.id.schedule_sms_one_reciever_view_contact_type);
        contactTypeTextView.setText(receiver.getPhoneType());

        return rowView;
    }
}//end class ReceiverListAdapter
