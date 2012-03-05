package com.vinsol.sms_scheduler;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

public class ExtendedImageView extends ImageView{

	public ExtendedImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	public ExtendedImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public ExtendedImageView(Context context) {
		super(context);
	}
	
	@Override
    public void setPressed(boolean pressed) {
        if (pressed && ((View) getParent()).isPressed()) {
            return;
        }
        super.setPressed(pressed);
    } 

}
