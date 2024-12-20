package com.teamteam.customComponent;

import com.teamteam.witherest.LoginActivity;
import com.teamteam.witherest.R;
import com.teamteam.witherest.RoomDetailActivity;
import com.teamteam.witherest.SignupActivity;
import com.teamteam.witherest.common.AndroUtils;
import com.teamteam.witherest.common.CommonUtils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;


public class WriteNoticeDialogFragment extends DialogFragment  {
	
	public int ownerId;
	
	public static WriteNoticeDialogFragment newInstance(int ownerId, String notice) {
		WriteNoticeDialogFragment fragment = new WriteNoticeDialogFragment();
    	Bundle args = new Bundle();
        args.putInt("ownerId",ownerId);
        args.putString("notice",notice);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         setStyle(DialogFragment.STYLE_NO_FRAME, 0);
         ownerId = getArguments().getInt("ownerId");
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
    	LayoutInflater inflator  = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    	LinearLayout common1 = (LinearLayout)inflator.inflate(R.layout.common_modify, null);
    	final EditText noticeEdit = (EditText)common1.findViewById(R.id.common_edittext);
    	noticeEdit.setText(getArguments().getString("notice"));
    	noticeEdit.selectAll();
    	
    	Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.write_notice_title);
        builder.setIcon(android.R.drawable.ic_dialog_info);
        builder.setView(common1);
        builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				if (!checkNotice(noticeEdit)){	
					return;
				}	
				RoomDetailActivity.roomBoardFragment.createNotice(noticeEdit.getText().toString(), ownerId);
			}

		});
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {	
	
			}
		});
        
        return builder.create();
    }
    
    private boolean checkNotice(EditText noticeEdit) {
		String notice = noticeEdit.getText().toString();
		if (CommonUtils.isNullOrEmpty(notice)){
			AndroUtils.showToastMessage(getActivity(), getActivity().getResources().getString(R.string.confirm_notice), Toast.LENGTH_SHORT);
			return false;
		}
		return true;
	}
    
	private void createNotice() {
		// TODO Auto-generated method stub
		
	}
}
