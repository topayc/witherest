package com.teamteam.witherest;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.teamteam.customComponent.SimpleProgressDialog;
import com.teamteam.witherest.alarm.WitherestAlarms;
import com.teamteam.witherest.alarm.WitherestAlarms.Alarm;
import com.teamteam.witherest.alarm.WitherestAlarms.AlarmCancelAction;
import com.teamteam.witherest.alarm.WitherestAlarms.AlarmRegisterAction;
import com.teamteam.witherest.cacheload.ImageLoader;
import com.teamteam.witherest.common.AndroUtils;
import com.teamteam.witherest.common.CommonUtils;
import com.teamteam.witherest.model.AppCache;
import com.teamteam.witherest.model.Category;
import com.teamteam.witherest.model.Session;
import com.teamteam.witherest.service.callback.RoomServiceCallback;
import com.teamteam.witherest.service.callback.object.BaseResponseObject;
import com.teamteam.witherest.service.callback.object.CreateRoomResponseObject;
import com.teamteam.witherest.service.callback.object.RoomActionResponseObject;
import com.teamteam.witherest.service.callback.object.RoomInfo;
import com.teamteam.witherest.service.callback.object.RoomInfoResponseObject;
import com.teamteam.witherest.service.internal.ErrorHandler;
import com.teamteam.witherest.service.internal.RoomService;
import com.teamteam.witherest.service.internal.Service;
import com.teamteam.witherest.service.internal.ServiceManager;

public class MakeRoomActivity extends  FragmentActivity implements View.OnClickListener, RoomServiceCallback {

	//현재 액티비티가 방을 새로 만드는 것인지, 방을 수정하기 위해 열린 것인지를 체크하는 변수 
	//0: 방을 새로 만듬 , 1 : 방을 수정함 
	public int mode;
	
	public RoomInfo mRoomInfo;
	public RoomService roomSerVvice;
	
	private SimpleProgressDialog waitProgressDialog;
	public ArrayList<Category> mCategoryList;
	
	private ImageView mIv_roomImage;
	private TextView mTv_roomName;
	private TextView mTv_roomPurpose;
	private TextView mTv_roomCategory;
	private TextView mTv_roomPeriod;
	private TextView mTv_rooomPeriodType;
	private TextView mTv_roomAlarm;
	private TextView mTv_roomAlarmTime;
	private TextView mTv_roomPublic;
	private TextView mTv_roomMaxMember;
	private Button deleteRoomBtn;
	
	private RadioGroup radioGroup;
	private View necessaryView;
	private View optionView;
	
	
	public int roomId;
	public String roomTitle;
	public int curMemberCount;
	
	private boolean isDialogOpend = false;
	private SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyy/MM/dd");
	private SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyyMMdd");
	
	public RoomService roomService;
	public static ArrayList<Category> appCategories;

	public static final int DEFAULT_ALARM_HOUR= 12;
	public static final int DEFAULT_ALARM_MINUTE= 0;
	//다이알로그 생성 변수 
	public static final int DIALOG_ROOMNAME = 1;        
	public static final int DIALOG_PURPOSE = 2;        
	public static final int DIALOG_LIST_CATEGORY = 3;    
	public static final int DIALOG_LIST_PUBLIC = 4;         
	public static final int DIALOG_TIME = 5;                 
	public static final int DIALOG_ALARM = 6;              
	public static final int DIALOG_PERIOD = 7;              
	public static final int DIALOG_PERIODTYPE = 8;        
	public static final int DIALOG_IMAGE_GET = 9;
	public static final int DIALOG_MAXMEMBERS = 10;
	public static final int DIALOG_DATESELECT = 11;
	
	//방 이미지 변경을 위한 변수로, 갤러리, 카메라, 크랍인텐트를 나타냄
	public static final int PICK_FROM_ALBUM = 0;
	public static final int PICK_FROM_CAMERA = 1;
	public static final int CROP_PICK_FROM_ALBUM = 2;
	public static final int CROP_FROM_IMAGE = 3;

	
	public String tmpFile ;
	public String tmpImagePath ;
	public Uri mImageCaptureUri;
	
	//현재 액티비티의 생성 모드 
	public static final int ROOM_NEW =0;
	public static final int ROOM_MODIFY = 1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_makeroom);
		
		Intent i = getIntent();
		
		if (i != null) {
			mode = i.getIntExtra("mode", ROOM_NEW);
		}
		
		initInstance();
		initView();
		initListener();
		if (mode == ROOM_NEW){
			updateUi();
		}
		mainContentToggle(R.id.makeroom_necessary_radio_btn);
	}
	
	private void initInstance(){
		roomService = ServiceManager.getServiceManager().getRoomService();
		if (mode == ROOM_NEW){
			mRoomInfo = new RoomInfo();
		} else if (mode==ROOM_MODIFY){
			mRoomInfo = null;
			roomId = getIntent().getIntExtra("roomId", -1);
			roomTitle = getIntent().getStringExtra("roomTitle");
			curMemberCount = getIntent().getIntExtra("curMemberCount",-1);
			if (roomId == -1){
				AndroUtils.showToastMessage(this, "접근 경로가 잘못되었습니다", Toast.LENGTH_SHORT);
				onBackPressed();
				return;
			}
		}
		roomService = ServiceManager.getServiceManager().getRoomService();
		appCategories = AppCache.getInstance().getAppCategory();
	}
	

	private boolean[] convertTypeToArray(String periodType) {
		boolean[] tmpArray = new boolean[7];
		for (int i = 1; i < periodType.length(); i++){
			tmpArray[i-1] = periodType.charAt(i) == '1'? true:false;
		}
		return tmpArray;
	}

	private void initView(){
		
		((Button)findViewById(R.id.submit_btn)).setText(mode == ROOM_NEW ?R.string.create : R.string.modify);
		//((TextView)findViewById(R.id.title)).setText(mode == ROOM_NEW?"방만들기":"방수정하기");
		((TextView)findViewById(R.id.title)).setText(mode == ROOM_NEW? R.string.create_room : R.string.modify_room);
		
		//타이틀에 룸 이름도 같이 보여준다.
		//((TextView)findViewById(R.id.title)).setText(mode == ROOM_NEW?"할일 추가하기":"["+roomTitle+" ] 할 일 수정하기");
		deleteRoomBtn = (Button)findViewById(R.id.deleteRoomBtn);
		mIv_roomImage =   (ImageView)findViewById(R.id.activity_makeroom_thumb_image);
		mTv_roomName =    (TextView)findViewById(R.id.activity_makeroom_roomname_textview);
		mTv_roomPurpose = (TextView)findViewById(R.id.activity_makeroom_purpose_textview);
		mTv_roomCategory = (TextView)findViewById(R.id.activity_makeroom_category_textview);
		mTv_roomPeriod =    (TextView)findViewById(R.id.activity_makeroom_period_textview);
		mTv_rooomPeriodType = (TextView)findViewById(R.id.activity_makeroom_periodtype_textview);
		mTv_roomAlarm = (TextView)findViewById(R.id.activity_makeroom_alarm_textview);
		mTv_roomAlarmTime = (TextView)findViewById(R.id.activity_makeroom_alarmtime_textview);
		mTv_roomPublic = (TextView)findViewById(R.id.activity_makeroom_public_textview);
		mTv_roomMaxMember = (TextView)findViewById(R.id.activity_makeroom_maxmember_textview);
		radioGroup = (RadioGroup)findViewById(R.id.makeroom_radio_group);
		necessaryView = findViewById(R.id.necessary_container);
		optionView = findViewById(R.id.option_container);
		optionView.setVisibility(View.GONE);
		radioGroup.setOnCheckedChangeListener( radioListener);
		
		if (mode == ROOM_MODIFY){
			findViewById(R.id.activity_makeroom_roomname_get_btn).setVisibility(View.GONE);
			findViewById(R.id.activity_makeroom_category_get_btn).setVisibility(View.GONE);
			findViewById(R.id.linear1).setVisibility(View.GONE);
			findViewById(R.id.linear2).setVisibility(View.GONE);
			deleteRoomBtn.setVisibility(View.VISIBLE);
			deleteRoomBtn.setOnClickListener(deleteRoomListener);
		}	
	}
	
	OnClickListener deleteRoomListener = new OnClickListener() {
		public void onClick(View v) {
			
			if ( curMemberCount != -1 && curMemberCount >1){
				AndroUtils.showToastMessage(MakeRoomActivity.this, R.string.no_delete_room,
						Toast.LENGTH_SHORT);
				return;
			}else {
				AlertDialog.Builder builder = new AlertDialog.Builder(MakeRoomActivity.this);
				builder.setTitle(R.string.delete_room_guide_title);
				builder.setMessage(R.string.delete_room_guide_message);
				builder.setCancelable(false);
				builder.setPositiveButton(R.string.confirm,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								deleteRoom(roomId);
								
							}
						});

				builder.setNegativeButton(R.string.cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {}
						});

				builder.create().show();
			}
		}
	};
	RadioGroup.OnCheckedChangeListener radioListener = new RadioGroup.OnCheckedChangeListener() {
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			if (group == radioGroup) {
				mainContentToggle(checkedId);
			}	
		}
	};
	
	@Override
	protected void onResume() {
		super.onResume();		
		waitProgressDialog = new SimpleProgressDialog(MakeRoomActivity.this, 
				getString(R.string.wait_title),getString(R.string.wait_message));
		waitProgressDialog.start();
		if (mode == ROOM_MODIFY){
			if (mRoomInfo == null){
				getRoomInfo();
			}
		}
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		SimpleProgressDialog.end(waitProgressDialog);
	}

	private void deleteRoom(int roomId) {
		if (!waitProgressDialog.isShowing()){
			waitProgressDialog.show();
		}
		roomService.setOnRoomCallback(this);
		roomService.deleteRoom(roomId);
	}
	
	
	private void mainContentToggle(int checkedId) {
		optionView.setVisibility(View.GONE);
		necessaryView.setVisibility(View.GONE);
		
		if ( checkedId== R.id.makeroom_necessary_radio_btn){
			necessaryView.setVisibility(View.VISIBLE);
		}
		if (checkedId == R.id.makeroom_option_radio_btn){
			optionView.setVisibility(View.VISIBLE);
		}
	}

	
	public void updateUi() {
		ImageLoader imageLoader=new ImageLoader(this.getApplicationContext());
		if (mode == ROOM_MODIFY){
			String imageUrl = null; 
			
			if (!CommonUtils.isNullOrEmpty(mRoomInfo.roomImagePath) && !"0".equals(mRoomInfo.roomImagePath)){
				imageUrl = Service.BASE_URL + mRoomInfo.roomImagePath;
			}
			
			imageLoader.displayImage(imageUrl, mIv_roomImage,ImageLoader.DEFAULT_ROOM_IMAGE);
			
			mTv_roomName.setText(mRoomInfo.roomTitle);
			mTv_roomPurpose.setText(mRoomInfo.roomPurpose);
			mTv_roomCategory.setText(mRoomInfo.category.categoryName);	
		}
		
		mTv_roomPeriod.setText(dateFormat1.format(mRoomInfo.startDate) + " ~ " + (mRoomInfo.endDate != null? dateFormat1.format(mRoomInfo.endDate) : ""));
		String displayperiodType = getDisplayStringFromPeriodType(mRoomInfo.periodType);
		Log.v("실천주기 데이타",mRoomInfo.periodType + "" );
		Log.v("실천주기 스트링",displayperiodType);
		
		mTv_rooomPeriodType.setText(displayperiodType);
		
		mTv_roomAlarm.setText(getResources().getStringArray(R.array.alarm_array)[mRoomInfo.alarmLevel]);
		mTv_roomAlarmTime.setText(getTimeToString(mRoomInfo.alarmHour, mRoomInfo.alarmMin, mRoomInfo.is24Time));
		mTv_roomPublic.setText(getResources().getStringArray(R.array.public_array)[mRoomInfo.publicLevel]);
		mTv_roomMaxMember.setText(mRoomInfo.maxMemberCount + " " + getResources().getString(R.string.unit_human));
	}
	
	private void initListener(){
		findViewById(R.id.activity_makeroom_image_get_btn).setOnClickListener(this);
		findViewById(R.id.activity_makeroom_roomname_get_btn).setOnClickListener(this);
		findViewById(R.id.activity_makeroom_purpose_get_btn).setOnClickListener(this);
		findViewById(R.id.activity_makeroom_category_get_btn).setOnClickListener(this);
		findViewById(R.id.activity_makeroom_period_get_btn).setOnClickListener(this);
		findViewById(R.id.activity_makeroom_periodtype_get_btn).setOnClickListener(this);
		findViewById(R.id.activity_makeroom_alarm_get_btn).setOnClickListener(this);
		findViewById(R.id.activity_makeroom_alarmtime_get_btn).setOnClickListener(this);
		findViewById(R.id.activity_makeroom_public_get_btn).setOnClickListener(this);
		findViewById(R.id.activity_makeroom_maxmember_get_btn).setOnClickListener(this);
		((Button)findViewById(R.id.submit_btn)).setOnClickListener(submitListener);
	}


	public String getTimeToString(int hour, int minute, boolean is24HourView){
		return is24HourView?  hour +  getResources().getString(R.string.simple_time) + minute + 
				getResources().getString(R.string.simple_min) :
					(hour > 12 ? getResources().getString(R.string.pm) + " " + (hour -12) +  getResources().getString(R.string.simple_time) + minute + 
				getResources().getString(R.string.simple_min):
					getResources().getString(R.string.am) + " " +   hour + getResources().getString(R.string.simple_time) + minute + 
					getResources().getString(R.string.simple_min));
	}
	
	private String getDisplayStringFromPeriodType(String periodType){
		if (periodType.charAt(0) == '1'){
			return getResources().getString(R.string.daily);
		}
	
		StringBuffer periodTypeBuffer = new StringBuffer();
	
		for (int i = 1; i <periodType.length(); i++){
			if ( periodType.charAt(i) == '1'){
				periodTypeBuffer.append(getResources().getStringArray(R.array.makeroom_simple_weekends_array)[i-1]);
				periodTypeBuffer.append(" ,");
			}
		}
		
		String result = periodTypeBuffer.toString();
		if (result.length() > 1){
			periodTypeBuffer.delete(periodTypeBuffer.length()-1,periodTypeBuffer.length());
		}
		
		return periodTypeBuffer.toString();
	}
	
	private OnClickListener submitListener = new OnClickListener() {
		public void onClick(View arg0) {
			if (checkFrom()){
				if (mode == ROOM_NEW){
					createRoom();
				}else if(mode == ROOM_MODIFY){
					modifyCheckRoom();
				}
			}	
		}
	};

	public boolean checkFrom(){
		boolean result = false;
		
		if(CommonUtils.isNullOrEmpty(mRoomInfo.roomTitle)) {
			Toast.makeText(MakeRoomActivity.this, getString(R.string.makeroom_title_insert), Toast.LENGTH_SHORT).show();
		}else if(CommonUtils.isNullOrEmpty(mRoomInfo.roomPurpose)) {
			Toast.makeText(MakeRoomActivity.this, getString(R.string.makeroom_purpose_insert), Toast.LENGTH_SHORT).show();
		}else if(mRoomInfo.category == null) {
			Toast.makeText(MakeRoomActivity.this, getString(R.string.makeroom_category_insert), Toast.LENGTH_SHORT).show();
		}else {
			result = true;
		}
		return result;
	}
	
	public void createRoom(){
		if (!waitProgressDialog.isShowing()){
			waitProgressDialog.show();
		}
		
		roomService.setOnRoomCallback(this);
		roomService.createRoom(mRoomInfo);
	}
	
	public void modifyCheckRoom(){
		if (!waitProgressDialog.isShowing()){
			waitProgressDialog.show();
		}
		
		roomService.setOnRoomCallback(this);
		roomService.modifyRoom(mRoomInfo);
	}
	
	
	public void onClick(View view) {
		if (isDialogOpend) return;
		switch(view.getId()){
		//방이미지  대화장 
		case R.id.activity_makeroom_image_get_btn:
			showSettingDialog(R.string.selectimage,android.R.drawable.ic_input_add, DIALOG_IMAGE_GET);
			break;	
			
		//방 타이틀  대화창 
		case R.id.activity_makeroom_roomname_get_btn:
			showSettingDialog(R.string.dialog_input_roomname_text,android.R.drawable.ic_input_add, DIALOG_ROOMNAME);
			break;		
			
		//방 목표 대화창 
		case R.id.activity_makeroom_purpose_get_btn:
			showSettingDialog(R.string.dialog_input_purpose_text,android.R.drawable.ic_input_add, DIALOG_PURPOSE);
			break;		
			
		//방 카테고리  대화창
		case R.id.activity_makeroom_category_get_btn:
			showSettingDialog(R.string.dialog_input_category_text,android.R.drawable.ic_input_add, DIALOG_LIST_CATEGORY);
			break;		
			
		//기간 대화창 
		case R.id.activity_makeroom_period_get_btn:
			showSettingDialog(R.string.dialog_input_period_text,android.R.drawable.ic_input_add, DIALOG_PERIOD);
			break;		
			
		//실천 주기 대화창
		case R.id.activity_makeroom_periodtype_get_btn:
			showSettingDialog(R.string.dialog_input_periodtype_text,android.R.drawable.ic_input_add, DIALOG_PERIODTYPE);
			break;		
			
		//알림 여부 대화창
		case R.id.activity_makeroom_alarm_get_btn:
			showSettingDialog(R.string.dialog_input_alarm_text,android.R.drawable.ic_input_add, DIALOG_ALARM);
			break;		
			
		//알람 시간 대화창
		case R.id.activity_makeroom_alarmtime_get_btn:
			showSettingDialog(R.string.dialog_input_maxmember_text,android.R.drawable.ic_input_add, DIALOG_TIME);
			break;		
			
		//공개 여부 대화창
		case R.id.activity_makeroom_public_get_btn:
			showSettingDialog(R.string.dialog_input_public_not_public,android.R.drawable.ic_input_add, DIALOG_LIST_PUBLIC);
			break;		
			
		//참여 인원 대화창 
		case R.id.activity_makeroom_maxmember_get_btn:
			showSettingDialog(R.string.dialog_input_maxmember_text,android.R.drawable.ic_input_add, DIALOG_MAXMEMBERS);
			break;
		}
	}

	private void showSettingDialog(int title, int drawableId, int type) {
		DialogFragment  newFragment = MakeRoomSettingDialogFragment.newInstance(title, drawableId, type);
		FragmentManager fm = getSupportFragmentManager(); 
		newFragment.show(fm,"dialog");
	}

	public void onRoomServiceCallback(BaseResponseObject object) {
		if (waitProgressDialog.isShowing()){
			waitProgressDialog.dismiss();
		}
		
		if (object.resultCode == Service.RESULT_FAIL) {
			return;
		}
		
		WitherestAlarms alarms;
		Alarm alarm;
		
		switch(object.requestType){
		case Service.REQUEST_TYPE_CREATE_CHECKROOM:
			
			if (object.resultCode == ErrorHandler.COMMON_ERROR || object.resultCode == ErrorHandler.NETWORK_DISABLE ||
					object.resultCode == ErrorHandler.PARSING_ERROR){
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(R.string.fatal_network_error);
				builder.setMessage(R.string.fatal_network_error_message);
				builder.setCancelable(false);
				builder.setPositiveButton(R.string.confirm,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								finish();
							}
						});
				builder.create().show();
				return;
			}else if (object.resultCode == ErrorHandler.CONNECTION_TIMEOUT || object.resultCode == ErrorHandler.READ_TIMEOUT){
				AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
				builder2.setTitle(R.string.tempo_network_error);
				builder2.setMessage(R.string.tempo_network_error_message);
				builder2.setCancelable(false);
				builder2.setPositiveButton(R.string.confirm,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								new Handler().postDelayed(new Runnable() {
									public void run() {
										createRoom();
									}
								}, 100);
							}
						});

				builder2.setNegativeButton(R.string.no,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
							}
						});

				builder2.create().show();
				return;
			}
			AndroUtils.showToastMessage(MakeRoomActivity.this, getResources().getString(R.string.createroom_ok), Toast.LENGTH_SHORT);
			CreateRoomResponseObject createObject  = (CreateRoomResponseObject)object;
			
			MainActivity.isMyCheckRoomMustReloaded = true;
			
			WitheState state = WitheState.getInstance();
			state.init();
			
			state.changeType = WitheState.CREATE_ROOM;
			state.haveChanged = true;
			state.mustOneLoaded = true;
			state.id = createObject.roomId;
			
			alarms = new WitherestAlarms(this);
			alarm = new Alarm();
			alarm.userId = Session.getInstance().user.id;
			alarm.roomId = createObject.roomId;
			alarm.roomName = mRoomInfo.roomTitle;
			alarm.roomPurpse = mRoomInfo.roomPurpose;
			alarm.alarmTime = mRoomInfo.alarmHour+":"+mRoomInfo.alarmMin;
			alarm.alarmEnabled =mRoomInfo.alarmLevel;
			alarm.userRoomTimeOption = Session.getInstance().user.isRoomTimeNotice ? 1: 0;
			alarms.registerAlarm(alarm, AlarmRegisterAction.INSERT_AFTER_ALARM_START);
			alarms.close();
			
			finish();
			break;
			
		case Service.REQUEST_TYPE_GET_ROOMINFO:
			if (object.resultCode == ErrorHandler.COMMON_ERROR || object.resultCode == ErrorHandler.NETWORK_DISABLE ||
					object.resultCode == ErrorHandler.PARSING_ERROR){
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(R.string.fatal_network_error);
				builder.setMessage(R.string.fatal_network_error_message);
				builder.setCancelable(false);
				builder.setPositiveButton(R.string.confirm,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								finish();
							}
						});
				builder.create().show();
				return;
			}else if (object.resultCode == ErrorHandler.CONNECTION_TIMEOUT || object.resultCode == ErrorHandler.READ_TIMEOUT){
				AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
				builder2.setTitle(R.string.tempo_network_error);
				builder2.setMessage(R.string.tempo_network_error_message);
				builder2.setCancelable(false);
				builder2.setPositiveButton(R.string.confirm,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								new Handler().postDelayed(new Runnable() {
									public void run() {
										getRoomInfo();
									}
								}, 100);
							}
						});

				builder2.setNegativeButton(R.string.no,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
							}
						});

				builder2.create().show();
				return;
			}
			
			mRoomInfo = ((RoomInfoResponseObject)object).roomInfo;
			updateUi();
			break;
			
		case Service.REQUEST_TYPE_MODIFY_ROOM:
			
			if (object.resultCode == ErrorHandler.COMMON_ERROR || object.resultCode == ErrorHandler.NETWORK_DISABLE ||
					object.resultCode == ErrorHandler.PARSING_ERROR){
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(R.string.fatal_network_error);
				builder.setMessage(R.string.fatal_network_error_message);
				builder.setCancelable(false);
				builder.setPositiveButton(R.string.confirm,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								finish();
							}
						});
				builder.create().show();
				return;
			}else if (object.resultCode == ErrorHandler.CONNECTION_TIMEOUT || object.resultCode == ErrorHandler.READ_TIMEOUT){
				AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
				builder2.setTitle(R.string.tempo_network_error);
				builder2.setMessage(R.string.tempo_network_error_message);
				builder2.setCancelable(false);
				builder2.setPositiveButton(R.string.confirm,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								new Handler().postDelayed(new Runnable() {
									public void run() {
										modifyCheckRoom();
									}
								}, 100);
							}
						});

				builder2.setNegativeButton(R.string.no,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
							}
						});

				builder2.create().show();
				return;
			}
			
			AndroUtils.showToastMessage(MakeRoomActivity.this, getResources().getString(R.string.modifyroom_ok), Toast.LENGTH_SHORT);
			MainActivity.isMyCheckRoomMustReloaded = true;
			
			WitheState state1 = WitheState.getInstance();
			state1.init();
			state1.changeType = WitheState.MODIFY_ROOM;
			state1.haveChanged = true;
			state1.mustOneModified = true;
			state1.id = roomId;
			
			alarms = new WitherestAlarms(this);
			alarm = new Alarm();
			alarm.userId = Session.getInstance().user.id;
			alarm.roomId =mRoomInfo.roomId;
			alarm.roomName = mRoomInfo.roomTitle;
			alarm.roomPurpse = mRoomInfo.roomPurpose;
			alarm.alarmTime = mRoomInfo.alarmHour+":"+mRoomInfo.alarmMin;
			alarm.alarmEnabled =mRoomInfo.alarmLevel;
			alarm.userRoomTimeOption = Session.getInstance().user.isRoomTimeNotice ? 1: 0;
			alarms.registerAlarm(alarm, AlarmRegisterAction.INSERT_AFTER_ALARM_START);
			
			if (alarms.isRegistered(mRoomInfo.roomId)){
				alarms.updateAlarm(alarm);
			}else {
				alarms.registerAlarm(alarm, AlarmRegisterAction.INSERT_AFTER_ALARM_START);
			}
			alarms.close();
			Intent i = new Intent();
			setResult(RESULT_OK, i);
			finish();
			break;
			
		case Service.REQUEST_TYPE_DELETE_ROOM:
			RoomActionResponseObject obj = (RoomActionResponseObject)object;
			WitheState state2 = WitheState.getInstance();
			state2.init();
			state2.changeType = WitheState.DELETE_ROOM;
			state2.haveChanged = true;
			state2.mustOneDeleted = true;
			state2.id = roomId;
			
			alarms = new WitherestAlarms(this);
			alarm = new Alarm();
			alarm.userId = Session.getInstance().user.id;
			alarm.roomId =mRoomInfo.roomId;
			alarm.roomName = mRoomInfo.roomTitle;
			alarm.roomPurpse = mRoomInfo.roomPurpose;
			alarm.alarmTime = mRoomInfo.alarmHour+":"+mRoomInfo.alarmMin;
			alarm.alarmEnabled =mRoomInfo.alarmLevel;
			alarm.userRoomTimeOption = Session.getInstance().user.isRoomTimeNotice ? 1: 0;
			
			if (alarms.isRegistered(mRoomInfo.roomId)){
				Log.v("알람 삭제", mRoomInfo.roomPurpose + " 방의 알람이 삭제됩니다");
				alarms.unregisterAlarm(alarm, AlarmCancelAction.CANCEL_AFTER_DELETE);
			}else {
				Log.v("알람 삭제 실패", mRoomInfo.roomPurpose + " 알람이 등록되어 있지 않습니다");
			}
			alarms.close();
			
			finish();
			
			MainActivity.isMyCheckRoomMustReloaded = true;
			Intent i1 = new Intent(this, MainActivity.class);
			i1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(i1);
			break;
		}	
	}

	public void getRoomInfo(){
		if (!waitProgressDialog.isShowing()){
			waitProgressDialog.show();
		}
		roomService.setOnRoomCallback(this);
		roomService.getRoomInfo(roomId);
	}
	
   public void getImageFromCamera() {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		tmpFile = Environment.getExternalStorageDirectory().getAbsolutePath() +  "/tmp.jpg";
		mImageCaptureUri = Uri.fromFile(new File(tmpFile));
		
		intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT,mImageCaptureUri);
		startActivityForResult(intent,PICK_FROM_CAMERA);
	}

	public  void getImageFromAlbum() {
		Intent i = new Intent(Intent.ACTION_GET_CONTENT);
		i.setType("image/*");
		startActivityForResult(i, PICK_FROM_ALBUM);
	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		if (resultCode != RESULT_OK)
			return;

		switch (requestCode) {
		case CROP_FROM_IMAGE:
			/*주석의 코드는 크랍이미지를 파일로 저장하지 않고, bundle 로 bitmap를 가져오는 경우 
			final Bundle extras = data.getExtras();
			Bitmap photo = null;
			if (extras != null) {
				photo = extras.getParcelable("data");
			}*/
			String filePath = getTempUri().getPath();
			Bitmap bitmap = BitmapFactory.decodeFile(filePath);
			Log.v("크랍이미지 정보" ,"width==>"+ bitmap.getWidth() + " , height ===> " + bitmap.getHeight());
			onRoomImageChange(bitmap, filePath);
			
			/*File f = new File(getTempUri().getPath());
			촬영된 원본 이미지를 삭제한다. 만약 이 촬영이미지를 서버에 업로드 하는 경우 삭제하면 안된다.
			혹은 삭제한다면 생성된 촬영 비트맵이나 crop된 이미지를 스트림으로 변환해야 한다.
			if (f.exists())
				f.delete();
			*/
			break;
			
		case PICK_FROM_ALBUM:
			mImageCaptureUri = data.getData();
			tmpImagePath = AndroUtils.getRealImagePath(this, mImageCaptureUri);
			tmpFile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/tmp"+ "." + CommonUtils.getFileExtension(tmpImagePath);
			
			Log.v("선택한 파일 이름", "" + tmpImagePath);
		    Intent i  = new Intent("com.android.camera.action.CROP");
			i.setDataAndType(mImageCaptureUri, "image/*");
			i.putExtra("outputX", 800);
			i.putExtra("outputY", 600);
			i.putExtra("aspectX", 1.3);
			i.putExtra("aspectY", 1);
			i.putExtra("scale", true);
			i.putExtra("output", getTempUri());
			startActivityForResult(i, CROP_FROM_IMAGE);
			break;
	

		case PICK_FROM_CAMERA:
			Intent intent = new Intent("com.android.camera.action.CROP");
			intent.setDataAndType(mImageCaptureUri, "image/*");
			intent.putExtra("outputX", 800);
			intent.putExtra("outputY", 600);
			intent.putExtra("aspectX", 1.3);
			intent.putExtra("aspectY", 1);
			intent.putExtra("scale", true);
			/*이 인텐트를 줄 경우 크롭화면에서 저장을 누를 경우 경우  비트앱을 Bundle를 통해 가져오겠다는 의미*/
			//intent.putExtra("return-data", true);
			//아래의 인텐트는 크롭화면에서 저장을 누를 경우 파일로 저장하겠다는 의미 로 지정한 Uril 크랍된 이미지가 저장된다.
			intent.putExtra("output", getTempUri());
			startActivityForResult(intent, CROP_FROM_IMAGE);
			break;
		}	
	}
	
	/** 임시 저장 파일의 경로를 반환 */
	private Uri getTempUri() {
        return Uri.fromFile(getTempFile());
	}
	
	/** 외장메모리에 임시 이미지 파일을 생성하여 그 파일의 경로를 반환  */
	private File getTempFile() {
        if (isSDCARDMounted()) {
            File f = new File(tmpFile);
            try {
                f.createNewFile();      
            } catch (IOException e) { }
            return f;
        } else
            return null;
    }
	
	/** SD카드가 마운트 되어 있는지 확인 */
    private boolean isSDCARDMounted() {
        String status = Environment.getExternalStorageState();
        if (status.equals(Environment.MEDIA_MOUNTED)){
        	Log.v("SD 카드 " ,"mounted");
            return true;
        }else {
        	Log.v("SD 카드 " ,"unmounted");
        	return false;
        }
    }
	
	public void onRoomImageChange(Bitmap bit, String path){
		mRoomInfo.roomImagePath = path;
		mIv_roomImage.setImageBitmap(bit);
	}
	
	public void onRoomNameChange(String val){
		mRoomInfo.roomTitle = val;
		mTv_roomName.setText(val);
	}
	
	public void onRoomPurposeChange(String val){
		mRoomInfo.roomPurpose = val;
		mTv_roomPurpose.setText(val);

	}

	public void onRoomMaxMemberChange(String val){
		mRoomInfo.maxMemberCount= Integer.parseInt(val.trim());
		mTv_roomMaxMember.setText(val + getResources().getString(R.string.unit_human));
	}
	
	public void onRoomCategoryChange(int pos){
		mRoomInfo.category = appCategories.get(pos);
		mTv_roomCategory.setText(appCategories.get(pos).categoryName);
	}
	
	public void onRoomPublicLevelChange(int pos){
		mRoomInfo.publicLevel = pos;
		mTv_roomPublic.setText(getResources().getStringArray(R.array.public_array)[pos]);
	}
	
	public void onRoomAlarmLevelChange(int pos){
		mRoomInfo.alarmLevel = pos;
		mTv_roomAlarm.setText(getResources().getStringArray(R.array.alarm_array)[pos]);
	}
	
	public void onRoomPeriodChange(String val){
		if (val.trim().length() < 1 || val == null || val =="") return;
		int period = Integer.parseInt(val);
		Calendar cal = Calendar.getInstance();
		cal.setTime(mRoomInfo.startDate);
		cal.add(Calendar.DATE, period);
		mRoomInfo.endDate = cal.getTime();
		
		mTv_roomPeriod.setText(dateFormat1.format(mRoomInfo.startDate) + " ~ " + dateFormat1.format(mRoomInfo.endDate ));
	}
	
	public void onRoomPeriodChange(int pos){
		int period = Integer.parseInt(getResources().getStringArray(R.array.makeroom_period_int_array)[pos]);
		Calendar cal = Calendar.getInstance();
		cal.setTime(mRoomInfo.startDate);
		cal.add(Calendar.DATE, period);
		mRoomInfo.endDate = cal.getTime();
		
		mTv_roomPeriod.setText(dateFormat1.format(mRoomInfo.startDate) + " ~ " + dateFormat1.format(mRoomInfo.endDate ));
	}
	
	public void onRoomPeriodTypeChange(){
		mRoomInfo.periodTypeMode = RoomInfo.SELECT_DAILY_MODE;
		mRoomInfo.periodType = "10000000";
	
		String s = getDisplayStringFromPeriodType(mRoomInfo.periodType);
		mTv_rooomPeriodType.setText(s);
	}
	
	public void onRoomPeriodTypeChange(boolean[] boolArr){
		mRoomInfo.periodTypeMode = RoomInfo.SELECT_DATE_MODE;
		mRoomInfo.periodType = getPeriodtype(boolArr);
		
		String diplayStr = getDisplayStringFromPeriodType(mRoomInfo.periodType);
		
		mTv_rooomPeriodType.setText(diplayStr);
	}
	
	public void onRoomAlarmTimeChange(int hour, int minute, boolean is24HourView){
		mRoomInfo.is24Time = is24HourView;
		mRoomInfo.alarmHour= hour;
		mRoomInfo.alarmMin = minute;
		String displayText  =getTimeToString(hour,minute, is24HourView);
		
	mTv_roomAlarmTime.setText(displayText);
	}

	
	private String getPeriodtype(boolean[] boolArr) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("0");
		for (int i = 0; i < boolArr.length; i++){
			 buffer.append(boolArr[i] == true?"1":"0");
		}
		return buffer.toString();
	}

	public static  class MakeRoomSettingDialogFragment extends DialogFragment {
        public static MakeRoomSettingDialogFragment newInstance(int title, int drawableId, int type) {
            MakeRoomSettingDialogFragment frag = new MakeRoomSettingDialogFragment();
            Bundle args = new Bundle();
            args.putInt("title", title);
            args.putInt("icon",drawableId);
            args.putInt("type", type);
            frag.setArguments(args);
            return frag;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
        	final LayoutInflater inflator  = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        	final LinearLayout common1 = (LinearLayout)inflator.inflate(R.layout.common_modify, null);
        	
        	final int title = getArguments().getInt("title");
            final int icon = getArguments().getInt("icon");
            final int type = getArguments().getInt("type");
            
            Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(title);
            builder.setIcon(icon);
            
            switch(type){
            case DIALOG_IMAGE_GET:
            	builder.setItems(R.array.profile_imget_get_mthod,new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,
							int which) {
						if (which == PICK_FROM_ALBUM) {
							((MakeRoomActivity)getActivity()).getImageFromAlbum();
						} else if (which == PICK_FROM_CAMERA) {
							((MakeRoomActivity)getActivity()).getImageFromCamera();
						}
					}
				});
            	break;
            	
            case DIALOG_ROOMNAME:
            	((EditText)common1.findViewById(R.id.common_edittext)).setText(((MakeRoomActivity)getActivity()).mRoomInfo.roomTitle);
            	builder.setView(common1);
            	break;
            	
            case DIALOG_MAXMEMBERS:
            	((EditText)common1.findViewById(R.id.common_edittext)).setInputType(InputType.TYPE_CLASS_NUMBER);
            	String val = String.valueOf(((MakeRoomActivity)getActivity()).mRoomInfo.maxMemberCount);
            	((EditText)common1.findViewById(R.id.common_edittext)).setText(val);
            	builder.setView(common1);
            	break;
            	
            case DIALOG_PURPOSE:
            	((EditText)common1.findViewById(R.id.common_edittext)).setText(((MakeRoomActivity)getActivity()).mRoomInfo.roomPurpose);
            	builder.setView(common1);
            	break;
            	
            case DIALOG_ALARM:
            	builder.setItems(R.array.alarm_array, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						((MakeRoomActivity)getActivity()).onRoomAlarmLevelChange(which);
					}
				});
            	break;
            	
            case DIALOG_LIST_CATEGORY:
            	String[] cateArr  = new String[appCategories.size()];
        		for (int i = 0; i <appCategories.size(); i++){
        			cateArr[i] = appCategories.get(i).categoryName;
        		}
            	builder.setItems(cateArr, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						((MakeRoomActivity)getActivity()).onRoomCategoryChange(which);	
					}
				});
            	break;
            	
            case DIALOG_LIST_PUBLIC:
            	
            	builder.setItems(R.array.public_array, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						((MakeRoomActivity)getActivity()).onRoomPublicLevelChange(which);
					}
				});
            	break;
            	
            case DIALOG_TIME:
            	Calendar cal = new GregorianCalendar();
            	int hour = cal.get(Calendar.HOUR_OF_DAY);
            	int min = cal.get(Calendar.MINUTE);
            	return new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
        			public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        				((MakeRoomActivity)getActivity()).onRoomAlarmTimeChange(hourOfDay, minute,view.is24HourView());
        			}
        		}, hour, min, ((MakeRoomActivity)getActivity()).mRoomInfo.is24Time);	
            
            case DIALOG_PERIOD:
            	final LinearLayout periodLayout = (LinearLayout)inflator.inflate(R.layout.activity_makeroom_period_select, null);
            	final RadioGroup group = (RadioGroup)periodLayout.findViewById(R.id.radiogroup_period);
            	final ListView list = (ListView)periodLayout.findViewById(R.id.makeroom_period_list);
            	final RelativeLayout directLayout = (RelativeLayout)periodLayout.findViewById(R.id.direct_input_layout);
            	final EditText periodText = (EditText)periodLayout.findViewById(R.id.periodeditText);
            	periodText.setInputType(InputType.TYPE_CLASS_NUMBER);
            	
            	final Button periodButton = (Button)periodLayout.findViewById(R.id.periodbutton);
            	periodButton.setOnClickListener(new OnClickListener() {
					public void onClick(View view) {
						dismiss();
						((MakeRoomActivity)getActivity()).onRoomPeriodChange(periodText.getText().toString());	
					}
				});
            	
            	group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						list.setVisibility(View.GONE);
						directLayout.setVisibility(View.GONE);
						switch(checkedId){
						case R.id.list_radioButton:
							list.setVisibility(View.VISIBLE);
							break;
						case R.id.directradioButton:
							directLayout.setVisibility(View.VISIBLE);
							break;
						}		
					}
				});
            	
            	ArrayAdapter<CharSequence> adpater = ArrayAdapter.createFromResource(getActivity(), R.array.makeroom_period_array,
            			android.R.layout.simple_list_item_1);
            	 list.setAdapter(adpater);
            	 list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

					public void onItemClick(AdapterView<?> arg0, View view,int position, long id) {
						dismiss();
						((MakeRoomActivity)getActivity()).onRoomPeriodChange(position);
					} 
				});
            	builder.setView(periodLayout);
            	break;
            
            case DIALOG_DATESELECT:
            	final boolean[] tmpArr = ((MakeRoomActivity)getActivity()).convertTypeToArray(((MakeRoomActivity)getActivity()).mRoomInfo.periodType);
            	
            	builder.setMultiChoiceItems(R.array.makeroom_weekends_array, tmpArr ,new DialogInterface.OnMultiChoiceClickListener() {
					public void onClick(DialogInterface dialog, int which, boolean isChecked) {
						tmpArr[which] = isChecked;
					}
				})
				.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						((MakeRoomActivity)getActivity()).onRoomPeriodTypeChange(tmpArr);
					}
				})
				.setNegativeButton(R.string.cancel, null);
            	break;
            	
            case DIALOG_PERIODTYPE:
            	builder.setItems(R.array.makeroom_periodtype_array,new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						if (which == 0){
							((MakeRoomActivity)getActivity()).onRoomPeriodTypeChange();
						}else {
							((MakeRoomActivity)getActivity()).showSettingDialog(R.string.request_select_day, 
									android.R.drawable.ic_input_add, DIALOG_DATESELECT);
						}
						
					}
				});
            	break;
            }
            
           if (type == DIALOG_ROOMNAME || type == DIALOG_PURPOSE ||  type == DIALOG_MAXMEMBERS){
	       builder.setPositiveButton(R.string.confirm,
	                    new DialogInterface.OnClickListener() {
	                         public void onClick(DialogInterface dialog, int whichButton) {
	                        	    String val =  ((EditText)common1.findViewById(R.id.common_edittext)).getText().toString();                       
	                        	    switch(type){
	                        	 	case DIALOG_ROOMNAME:
	                        	 		((MakeRoomActivity)getActivity()).onRoomNameChange(val);
	                        	 		break;
	                        	 	case DIALOG_PURPOSE:
	                        	 		((MakeRoomActivity)getActivity()).onRoomPurposeChange(val);
	                        	 		break;
	                        	 	case DIALOG_MAXMEMBERS:
	                        	 		((MakeRoomActivity)getActivity()).onRoomMaxMemberChange(val);
	                        	 		break;
	                        	 	}                  
	                         }
	                      }
	                    )
	                    .setNegativeButton(R.string.cancel,null); 
           }
          
           return builder.create();
        }

    	
    /*	private void getImageFromAlbumCrop(){
    		Intent intent = new Intent( Intent.ACTION_GET_CONTENT, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");     
            intent.putExtra("outputX", 800);
			intent.putExtra("outputY", 600);
			intent.putExtra("aspectX", 1.3);
			intent.putExtra("aspectY", 1);
			intent.putExtra("scale", true);
            intent.putExtra("crop", "true");        
            intent.putExtra(MediaStore.EXTRA_OUTPUT, getTempUri());     // 임시파일 생성
            intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
            getActivity().startActivityForResult(intent, CROP_PICK_FROM_ALBUM);
    	}*/
    	

         
    }

}
