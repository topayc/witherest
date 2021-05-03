package com.teamteam.witherest.service.internal;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;

import android.os.Message;
import android.util.Log;

import com.teamteam.witherest.common.CommonUtils;
import com.teamteam.witherest.model.Session;
import com.teamteam.witherest.service.callback.ArticleServiceCallback;
import com.teamteam.witherest.service.callback.UserServiceCallback;
import com.teamteam.witherest.service.callback.object.BaseResponseObject;

public class ArticleService extends Service {
	
	private static final String USER_ID = "user_id";
	private static final String ROOM_ID = "room_id";
	private static final String CONTENT = "content";
	private static final String ARTICLE_ID="article_id";
	private static final String PARENT_ID="parent_id";

	private ArticleServiceCallback callback;
	
	public ArticleService(HttpClient httpClient, ServiceHandler  handler) {
		super(httpClient, handler);
	}
	
	public void setOnArticleCallback(ArticleServiceCallback callback){
		this.callback = callback;
		handler.setArticleServiceCallback(callback);
	}

	public void createNotice(String notice, int ownerId, int roomId) {
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put(USER_ID,CommonUtils.int2string(ownerId));
		paramMap.put(ROOM_ID, CommonUtils.int2string(roomId));
		paramMap.put(CONTENT, notice);
		paramMap.put(REQUEST_TYPE_STRING,String.valueOf(REQUEST_TYPE_CREATE_NOTICE));
		
		try {
			sendPost(CREATE_NOTICE_URL, paramMap);
		} catch (InterruptedException e) {
		 }
		
	}

	public void submitNewComment(String comment, int roomId) {
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put(USER_ID, CommonUtils.int2string(Session.getInstance().user.userIndex));
		paramMap.put(CONTENT,comment);
		paramMap.put(ROOM_ID, CommonUtils.int2string(roomId));
		paramMap.put(REQUEST_TYPE_STRING,String.valueOf(REQUEST_TYPE_SUBMIT_NEW_COMMENT));
		
		try {
			sendPost(SUBMIT_NEW_COMMENT_URL , paramMap);
		} catch (InterruptedException e) {
		 }
	}

	public void submitReplyComment(String comment, int roomId, int parentId){
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put(USER_ID, CommonUtils.int2string(Session.getInstance().user.userIndex));
		paramMap.put(CONTENT,comment);
		paramMap.put(ROOM_ID, CommonUtils.int2string(roomId));
		paramMap.put(ARTICLE_ID, CommonUtils.int2string(parentId));
		paramMap.put(REQUEST_TYPE_STRING,String.valueOf(REQUEST_TYPE_SUBMIT_REPLY_COMMENT));
		
		try {
			sendPost(SUBMIT_REPLY_COMMENT_URL , paramMap);
		} catch (InterruptedException e) {}
	}
	
	public void deleteComment(int articleId){
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put(USER_ID, CommonUtils.int2string(Session.getInstance().user.userIndex));
		paramMap.put(ARTICLE_ID, CommonUtils.int2string(articleId));
		paramMap.put(REQUEST_TYPE_STRING,String.valueOf(REQUEST_TYPE_DELETE_COMMENT));
		Log.v("댓글 삭제 유저 아이디", CommonUtils.int2string(Session.getInstance().user.userIndex) + "");
		Log.v("댓글 삭제 글 아이디 ", CommonUtils.int2string(articleId) + "");
		
		try {
			sendPost(DELETE_COMMENT_URL , paramMap);
		} catch (InterruptedException e) {}
	}

	public void getReplysById(int roomId, int messageId) {
		Map<String, String> paramMap = new HashMap<String, String>();
		if (Session.getInstance().sessionStatus == Session.AUTHORIZED){
			paramMap.put(USER_ID,CommonUtils.int2string(Session.getInstance().user.userIndex));
		}
		paramMap.put(ROOM_ID, CommonUtils.int2string(roomId));
		paramMap.put(PARENT_ID, CommonUtils.int2string(messageId));
		paramMap.put(REQUEST_TYPE_STRING,String.valueOf(REQUEST_TYPE_GET_REPLYS_BY_ID));
		
		try {
			sendPost(GET_REPLYS_BY_ID , paramMap);
		} catch (InterruptedException e) {}
		
	}



		
	
	
}