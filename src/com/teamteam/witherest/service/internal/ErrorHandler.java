package com.teamteam.witherest.service.internal;

import com.teamteam.witherest.R;
import com.teamteam.witherest.service.callback.object.BaseResponseObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class ErrorHandler {
	public static final int NETWORK_DISABLE = 1000;
	public static final int READ_TIMEOUT = 1001;
	public static final int CONNECTION_TIMEOUT= 1002;
	public static final int PARSING_ERROR = 1003;
	public static final int COMMON_ERROR = 1004; 
	
	public static final String NETWORK_DISABLE_STIRNG ="network_disable";
	public static final String READ_TIMEOUT_STIRNG = "readtimeout";
	public static final String CONNECTION_TIMEOUT_STIRNG= "connectiontimeout";
	public static final String PARSING_ERROR_STIRNG ="parsingerror";
	public static final String COMMON_ERROR_STRING = "commonerror";
	
	
	public static boolean isFatalError(BaseResponseObject obj){
		switch (obj.resultCode){
		case NETWORK_DISABLE:
		case PARSING_ERROR:
		case COMMON_ERROR:
			return true;
		case READ_TIMEOUT:
		case CONNECTION_TIMEOUT:
			return false;
		}
		return false;
	}


	private static void showAppDownDialog(Context context) {
		final Activity act = (Activity)context;
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		/*builder.setTitle(R.string.network_error);
		builder.setMessage(R.string.network_error_message);*/
		builder.setCancelable(false);
		builder.setPositiveButton(R.string.confirm,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						act.finish();
						
					}
				});
		builder.create().show();
		
	}
}
