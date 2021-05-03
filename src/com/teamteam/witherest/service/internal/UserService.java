package com.teamteam.witherest.service.internal;


import java.util.HashMap;
import java.util.Map;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;
import android.os.Message;
import android.util.Log;
import com.teamteam.witherest.common.CommonUtils;
import com.teamteam.witherest.model.Category;
import com.teamteam.witherest.model.Session;
import com.teamteam.witherest.service.callback.UserServiceCallback;
import com.teamteam.witherest.service.callback.object.BaseResponseObject;


public class UserService extends Service{

	private static final String USER_ID = "user_id";
	private static final String USER_PWD ="user_pwd";
	private static final String USER_NAME = "user_name";
	private static final String USER_GCM_ID ="gcm_id";
	private static final String USER_INTEREST_CATEGORY = "int_cat";
	private static final String USER_ALARM  = "alarm";
	private static final String USER_PURPOSE="stu_msg";
	private static final String USER_EMAIL ="email";
	private static final String USER_NAME_MODIFY="name";
	private static final String USER_IMAGE ="image";
	
	private UserServiceCallback callback;
	
	public UserService(HttpClient httpClient, ServiceHandler  handler) {
		super(httpClient, handler);
	}
	
	public void setOnUserCallback(UserServiceCallback callback){
		this.callback = callback;
		handler.setUserServiceCallback(callback);
	}
	
	public void login(String id, String pw, String gcmId){
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put(USER_ID,id);
		paramMap.put(USER_PWD, pw);
		paramMap.put(USER_GCM_ID, gcmId);
		paramMap.put(REQUEST_TYPE_STRING,String.valueOf(REQUEST_TYPE_LOGIN));
		try {
			sendPost(LOGIN_URL, paramMap);
		} catch (InterruptedException e) {
		 }
	}
	

	public void checkIdDuplicated(String id){
		 Map<String, String> paramMap = new HashMap<String, String>();
			try {
				paramMap.put(USER_ID,id);
				paramMap.put(REQUEST_TYPE_STRING, String.valueOf(REQUEST_TYPE_DUPL_CHECK));
				sendPost(DUPL_CHECK_URL , paramMap);
			} catch (InterruptedException e) {
			}
	}
	
	public void join(String id, String nickName, String pw, Category myCategory){
		 Map<String, String> paramMap = new HashMap<String, String>();
		try {
			
		Log.v("회원 가입 정보", "===================================");
		Log.v("유저 이름", nickName+ "  ---");
		Log.v("유저 이메일", id+ "  ---");
		Log.v("유저 카테고리", myCategory.categoryName+ "  ---");
			
		paramMap.put(USER_ID,id);
		paramMap.put(USER_PWD, pw);  // 현재 로그인 시와 회원 가입시 패스워드 파라미터가 틀림, 수정이 요구됨
		paramMap.put(USER_NAME, nickName);
		paramMap.put(USER_INTEREST_CATEGORY, String.valueOf(myCategory.categoryId));
		paramMap.put(REQUEST_TYPE_STRING,String.valueOf(REQUEST_TYPE_JOIN));
		sendPost(JOIN_URL, paramMap);
		} catch (InterruptedException e) {
		}
	}
	
	public void logout(int id, String gcmId){
		
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put(USER_ID,String.valueOf(id));
		paramMap.put(USER_GCM_ID, gcmId);
		paramMap.put(REQUEST_TYPE_STRING,String.valueOf(REQUEST_TYPE_LOGOUT));
		try {
			sendPost(LOGOUT_URL, paramMap);
		} catch (InterruptedException e) {}	
	}
	
	public void withDraw(){
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put(USER_ID,String.valueOf(Session.getInstance().user.userIndex));
		paramMap.put(REQUEST_TYPE_STRING,String.valueOf(REQUEST_TYPE_WITHDRAW));
		try {
			sendPost(WITHDRAW_URL, paramMap);
		} catch (InterruptedException e) {
		 }
	}

	public void updateAlarm(String alarm){
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put(USER_ID,String.valueOf(Session.getInstance().user.userIndex));
		paramMap.put(USER_ALARM, alarm);
		paramMap.put(REQUEST_TYPE_STRING,String.valueOf(REQUEST_TYPE_ALARM_UPDATE));
		try {
			sendPost(ALARM_UPDATE_URL, paramMap);
		} catch (InterruptedException e) {
		 }
	}

	
	public void modifyProfile(String categories, String purpose, String email, String name, String imagePath){
		if (CommonUtils.isNullOrEmpty(imagePath) || "0".equals(imagePath) || imagePath.startsWith("/assets")){
			modifyProfileWithoutImage( categories, purpose,email, name);
		}else {
			modifyProfileWithImage( categories, purpose,email, name, imagePath);
		}
	}

	private void modifyProfileWithoutImage(String categories, String purpose , String email,
			String name) {
		
		Log.v("유저 이름", name+ "  ---");
		Log.v("유저 이메일", email+ "  ---");
		Log.v("유저 목표문구", purpose+ "  ---");
		Log.v("유저 카테고리", categories+ "  ---");
		
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put(USER_ID,String.valueOf(Session.getInstance().user.userIndex));
		
		if (categories !=null){
			paramMap.put(USER_INTEREST_CATEGORY, categories);
		}
		
		if (CommonUtils.isNullOrEmpty(purpose)){
			purpose = "";
		}
		paramMap.put(USER_PURPOSE, purpose);
		paramMap.put(USER_EMAIL, email);
		
		if (!CommonUtils.isNullOrEmpty(name)){
			paramMap.put(USER_NAME_MODIFY, name);
		}
		
		paramMap.put(REQUEST_TYPE_STRING,String.valueOf(REQUEST_TYPE_MODIFY_PROFILE));
		
		try {
			sendPost(MODYFY_PROFILE_URL, paramMap);
		} catch (InterruptedException e) {
			Log.v("modifuProfile요청 에러",e.getMessage());
		 }
		
	}

	private void modifyProfileWithImage(String categories, String purpose,String email,String name,String imagePath) {
		Map<String, String> paramMap = new HashMap<String, String>();
		Log.v("유저 이름", name+ "  ---");
		Log.v("유저 이메일", email+ "  ---");
		Log.v("유저 목표문구", purpose+ "  ---");
		Log.v("유저 카테고리", categories+ "  ---");
		Log.v("유저 프로파일 이미지", imagePath);
		
		paramMap.put(USER_ID,String.valueOf(Session.getInstance().user.userIndex));
		
		if (categories !=null){
			paramMap.put(USER_INTEREST_CATEGORY, categories);
		}
		
		if (CommonUtils.isNullOrEmpty(purpose)){
			purpose = "";
		}
		paramMap.put(USER_PURPOSE, purpose);
		
		paramMap.put(USER_EMAIL, email);
		
		if (!CommonUtils.isNullOrEmpty(name)){
			paramMap.put(USER_NAME_MODIFY, name);
		}
	
		paramMap.put(REQUEST_TYPE_STRING,String.valueOf(REQUEST_TYPE_MODIFY_PROFILE));
		sendMultipart(MODYFY_PROFILE_URL,imagePath,paramMap);
	}




}
