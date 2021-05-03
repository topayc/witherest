package com.teamteam.witherest;

import java.util.ArrayList;

import com.teamteam.customComponent.PageInfo;
import com.teamteam.customComponent.ReplyDialogFragment;
import com.teamteam.customComponent.ScrollRefreshPager;
import com.teamteam.customComponent.ScrollRefreshPager.OnScrollPagingListener;
import com.teamteam.customComponent.ScrollRefreshPager.OnScrollRefreshListener;
import com.teamteam.customComponent.SimpleProgressDialog;
import com.teamteam.customComponent.WriteNoticeDialogFragment;
import com.teamteam.customComponent.popup.ActionItem;
import com.teamteam.customComponent.popup.QuickAction;
import com.teamteam.customComponent.widget.RefreshListView;
import com.teamteam.customComponent.widget.RefreshListView.OnRefreshListener;

import com.teamteam.witherest.CheckRoomListFragment.RoomListViewHolder;
import com.teamteam.witherest.cacheload.ImageLoader;
import com.teamteam.witherest.common.AndroUtils;
import com.teamteam.witherest.common.CommonUtils;
import com.teamteam.witherest.model.Session;
import com.teamteam.witherest.service.callback.ArticleServiceCallback;
import com.teamteam.witherest.service.callback.RoomServiceCallback;
import com.teamteam.witherest.service.callback.object.ArticleActionResponseObject;
import com.teamteam.witherest.service.callback.object.BaseResponseObject;
import com.teamteam.witherest.service.callback.object.RoomBoardResponseObject;
import com.teamteam.witherest.service.callback.object.RoomBoardResponseObject.Message;
import com.teamteam.witherest.service.internal.ArticleService;
import com.teamteam.witherest.service.internal.ErrorHandler;
import com.teamteam.witherest.service.internal.RoomService;
import com.teamteam.witherest.service.internal.Service;
import com.teamteam.witherest.service.internal.ServiceManager;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class RoomBoardFragment extends Fragment implements RoomServiceCallback, ArticleServiceCallback{
	private Activity activity;
	private int roomId;
	public   int page;                                // 페이지 번호
	public static final int PAGING_SIZE  = 10;
	private int totalRecordCount;                  // 전체 레코드 갯수
	private int curRecordCount;                  // 현재 읽어들인 레코드 갯수 
	public PageInfo pageInfo;

	
	public  String roomManagerNotice;
	boolean hasRoomManagerNotice;
	public int ownerId;

	public RefreshListView roomBoardListView;
	public View headerView;
	
	public ImageView roomManagerImageView;
	public TextView roomManagerNameView;
	public TextView roomManagerNoticeView;
	public TextView roomManagerPurposeView;
	public EditText writeEdit;
	
	public ActionItem modifyItem;
	public ActionItem deleteItem;
	
	public boolean isFirstLoading;
	
	public SimpleProgressDialog waitProgressDialog;
	public ImageLoader imageLoader;
	
	public RoomBoardResponseObject roomBoardResponseObject;
	public RoomBoardAdaper roomBoardAdapter;
	public RoomService roomService;
	public ScrollRefreshPager pager; 
	
	public static final String TAG = "RoomBoardFragment";
	
	public int selectionPosition;
	public int deletePosition = -1;
	
	public int requestPage; 
	
	public static final int ACTION_ID_MODIFY =1;
	public static final int ACTION_ID_DELETE =2;
	
	public static RoomBoardFragment newInstance(int roomId,int page){
		RoomBoardFragment fragment = new RoomBoardFragment();
		fragment.setInitialData(roomId,1,0,0);
	    /*Bundle args = new Bundle();
        args.putInt("roomId",roomId);
        args.putInt("page",page);
        fragment.setArguments(args);*/
        return fragment;
	}
	
	private void setInitialData(int roomId, int page, int totalRecordCount,int curRecordCount ) {
		this.roomId = roomId;
		this.page = page;
		this.totalRecordCount = totalRecordCount;
		this.curRecordCount = curRecordCount;
		
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.activity = activity;
		imageLoader = new ImageLoader(getActivity());
		//imageLoader.clearCache();
		Log.v(TAG, " onAttach 호출");

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		roomService = ServiceManager.getServiceManager().getRoomService();
		/*roomId = getArguments().getInt("roomId");
		page = getArguments().getInt("page");
		totalRecordCount = 0;
		curRecordCount = 0;
		Log.v(TAG, " onCreate 호출");*/
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.v(TAG, " onCreateView 호출");
		super.onCreateView(inflater, container, savedInstanceState);
		LinearLayout view = (LinearLayout)inflater.inflate(R.layout.fragment_roomboard ,null);
		roomBoardListView = (RefreshListView)view.findViewById(R.id.roomboard_listview);
		writeEdit = (EditText)view.findViewById(R.id.roomboard_write_edittext);
		 Button writeBtn = (Button)view.findViewById(R.id.roomboard_write_btn);
		
		//신규 글 올리기 리스너 설정
		writeBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				checkBeforSubmit();
			}
		});
		
		headerView = inflater.inflate(R.layout.roomboard_list_header_view, null);
		roomManagerImageView = (ImageView)headerView.findViewById(R.id.notice_row_image);
		roomManagerNameView = (TextView)headerView.findViewById(R.id.roomboard_name_textview);
		roomManagerPurposeView = (TextView)headerView.findViewById(R.id.roomboard_purpose_textview);
		roomManagerNoticeView = (TextView)headerView.findViewById(R.id.roomboard_notice_textview);
		headerView.findViewById(R.id.roomboard_write_notice_btn).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				makeRoomNotice();			
			}
		});
		setListClickActionItem();
		return view;
	}
	
	public void checkBeforSubmit(){
		int userType = ((RoomDetailActivity)getActivity()).behaviorType;
		
		if (CommonUtils.isNullOrEmpty(writeEdit.getText().toString().trim())){
			AndroUtils.showToastMessage(getActivity(), getActivity().getResources().getString(R.string.no_text),
					Toast.LENGTH_SHORT);
			return;
		}
		if (userType == RoomDetailActivity.MEMBER_JOINED || userType ==RoomDetailActivity.OWNER){
			AndroUtils.hideSoftKeyboard(getActivity(),writeEdit);
			submitNewWriting(writeEdit.getText().toString());
		}else {
			AndroUtils.showToastMessage(getActivity(), getActivity().getResources().getString(R.string.not_user_not_join_alert), 
					Toast.LENGTH_SHORT);
			return;
		}
	}
	
	private void setListClickActionItem() {
		modifyItem	= new ActionItem(ACTION_ID_MODIFY, getActivity().getResources().getString(R.string.modify), getResources().getDrawable(R.drawable.comment_comment_icon_bg));
		deleteItem 	= new ActionItem(ACTION_ID_DELETE , getActivity().getResources().getString(R.string.delete), getResources().getDrawable(R.drawable.comment_trash_icon_bg));
	}
        
    private void showReplyDialog(Message message) {
    	DialogFragment  newFragment = ReplyDialogFragment.newInstance(message.messageId,
    			RoomDetailActivity.roomWithFragment.roomWithResponse.checkRoomWith.roomId);
		FragmentManager fm = getFragmentManager(); 
		newFragment.show(fm,"noticeDialog");		
	}
        
	private void submitNewWriting(String writing) {
		if (!((RoomDetailActivity)getActivity()).waitProgressDialog.isShowing()){
			((RoomDetailActivity)getActivity()).waitProgressDialog.show();
		}
		
		ArticleService articleService = ServiceManager.getServiceManager().getArticleService();
		articleService.setOnArticleCallback(this);
		articleService.submitNewComment(writing,RoomDetailActivity.roomWithFragment.roomWithResponse.checkRoomWith.roomId);
	}
	
	private void makeRoomNotice() {
		if (!isRoomOwner()){
			AndroUtils.showToastMessage(getActivity(),getActivity().getResources().getString(R.string.you_not_manager) , Toast.LENGTH_SHORT);
			return;
		}
		 showNoticeWriteDialog();
	}
	
	private void showNoticeWriteDialog() {
		DialogFragment  newFragment = WriteNoticeDialogFragment.newInstance( RoomDetailActivity.roomWithFragment.roomWithResponse.checkRoomWith.roomOwner
				, (hasRoomManagerNotice?roomManagerNotice:""));
		FragmentManager fm = getFragmentManager(); 
		newFragment.show(fm,"noticeDialog");
		
	}

	private boolean isRoomOwner() {
		int roomOwner = RoomDetailActivity.roomWithFragment.roomWithResponse.checkRoomWith.roomOwner;
		if (Session.getInstance().sessionStatus == Session.NOT_AUTHORIZED) return false;
		if (roomOwner == Session.getInstance().user.userIndex){
			return true;
		}else {
			return false;
		}	
	}
	
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Log.v(TAG, " onActivityCreate 호출");
		roomBoardListView.addHeaderView(headerView);
		pager = new ScrollRefreshPager(getActivity(), roomBoardListView);
		pager.setPagingListener(pagingListener);
		pager.setRefreshListerner(refreshListener);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		Log.v(TAG, " onResume 호출");
		Log.v("룸보드프래그먼트", "onResume() 호출");
		if (roomBoardResponseObject == null){
			isFirstLoading = true;
			Log.v("(roomBoardResponseObject", "널임");
			getRoomBoard(roomId, page);
		}else {
			Log.v("(roomBoardResponseObject", "널이 아님");
			updateUi(roomBoardResponseObject);
		}
	}
	
	@Override
	public void onPause() {
		super.onPause();
		Log.v(TAG, " onPause 호출");
		selectionPosition = roomBoardListView.getFirstVisiblePosition();
		if (pager.isPagerSettingsCompleted()){
			page = pager.getCurrentPage();
		}
		
		Log.v(TAG, "onPause() 호출");
		Log.v(TAG,"포지션 ==>" + selectionPosition);
		Log.v(TAG,"pager.getCurrentPage() ==>" + page );
	}
	
	private void updateUi(RoomBoardResponseObject obj) {
		//roomBoardList.removeHeaderView(headerView);
		Log.v("룸보드 글 숫자", obj.totalBoardCount+ "!!");
		
		roomBoardResponseObject = obj;
		
		roomManagerNameView.setText(obj.roomManagerName);
		String imageUrl = null;
		
		if (!CommonUtils.isNullOrEmpty(obj.roomManageImagePath) && !"0".equals(obj.roomManageImagePath)){
			imageUrl = Service.BASE_URL+ obj.roomManageImagePath;
		}
		imageLoader.displayImage(imageUrl, roomManagerImageView, ImageLoader.DEFAULT_PROFILE_IMAGE);
		
		roomManagerPurposeView.setText(CommonUtils.isNullOrEmpty(obj.roomManagerPurpose)?
				getActivity().getResources().getString(R.string.no_purpose) : obj.roomManagerPurpose );
		
		if (!CommonUtils.isNullOrEmpty(obj.roomManagerNotice)){
			hasRoomManagerNotice = true;
			roomManagerNotice = obj.roomManagerNotice;
		}else {
			hasRoomManagerNotice = false;
			roomManagerNotice = getActivity().getResources().getString(R.string.no_manager_notice);
		}
		
		roomManagerNoticeView.setText(roomManagerNotice);
		

		roomBoardAdapter = new RoomBoardAdaper(getActivity(),imageLoader, R.layout.roomboard_list_item_row, 
				roomBoardResponseObject.messageList);
		pageInfo = new PageInfo(roomId, page, obj.totalBoardCount);
		if (pager.pagerActionType == ScrollRefreshPager.PAGER_ACTION_REFRESH){
			pageInfo.setPage(1);
		}
		pager.setPageInfo(pageInfo);
		pager.setAdapter(roomBoardAdapter);
		

		if (isFirstLoading || pager.pagerActionType ==ScrollRefreshPager.PAGER_ACTION_REFRESH ){
			 isFirstLoading = false;
			 pager.pagerActionType = ScrollRefreshPager.PAGER_ACTION_NON;
		}else {
			pager.setSelection(selectionPosition);
		}
		pager.pagerActionType = ScrollRefreshPager.PAGER_ACTION_NON;
	}
	
	private void addItemToList(RoomBoardResponseObject responseObject) {
		this.page = pager.getCurrentPage();
		Log.v("addItemToList", page  + ":" + pager.getCurrentPage());

		ArrayList<Message> originalMessageList = roomBoardResponseObject.messageList;
		ArrayList<Message> messageList = responseObject.messageList;
		
		for (int i = 0; i < messageList.size(); i++){
			Message message = messageList.get(i);
			originalMessageList.add(message);
		}

		pager.notifyDataSetChanged();
		pager.pagerActionType = ScrollRefreshPager.PAGER_ACTION_NON;
		
	}


	//리스트뷰의 하단 스크롤을 통한 데이타 가져오기 
	OnScrollPagingListener pagingListener = new OnScrollPagingListener() {
		public void onScrollPaging() {
			pager.pagerActionType = ScrollRefreshPager.PAGER_ACTION_MORE;
			Log.v("하단 스크롤 페이징", "(pager.getCurPage()==>"+pager.getCurrentPage());
			Log.v("하단 스크롤 페이징", "page==>"+(pager.getCurrentPage()+1));
			
			pager.pagerActionType = ScrollRefreshPager.PAGER_ACTION_MORE;
			requestPage = pager.getCurrentPage()+1;
			getRoomBoard(roomId, pager.getCurrentPage()+1);
		}
	};
	
	//리스트뷰의 상단 스크롤을 통한 데이타 갱신 
	OnScrollRefreshListener refreshListener = new OnScrollRefreshListener() {
		public void onScrollRefresh() {
			pager.pagerActionType = ScrollRefreshPager.PAGER_ACTION_REFRESH;
			Log.v("상단 스크롤 갱신", "page==>"+ 1);
			
			pager.pagerActionType = ScrollRefreshPager.PAGER_ACTION_REFRESH;
			getRoomBoard(roomId, 1);
			requestPage = 1;
			
		}
	};
	
	
	public int getCurRecordCount(){
		return page * PAGING_SIZE;
	}
	
	private void getRoomBoard(int roomId, int page) {
		if (roomBoardResponseObject == null){
			if (!((RoomDetailActivity)getActivity()).waitProgressDialog.isShowing()){
				((RoomDetailActivity)getActivity()).waitProgressDialog.show();
			}
	   }
		roomService.setOnRoomCallback(this);
		Log.v("룸보드 가져오기 파라미터" , "roomId--> " + roomId + " , "+ "page--> "+ page);
		roomService.getRoomBoard(roomId,page);
	}
	
	public void onRoomServiceCallback(BaseResponseObject object) {
		if (((RoomDetailActivity)getActivity()).waitProgressDialog.isShowing()){
			((RoomDetailActivity)getActivity()).waitProgressDialog.dismiss();
		}
		
		//액비비티가 죽을 예정이거나 이미 죽었다면 리턴한다.
		//아래의 코드는 통신과 관련한 코드 부분에서 모두 필요하다.
		if (getActivity() == null || getActivity().isFinishing()){
			return;
		}
		
		if (object.resultCode == Service.RESULT_FAIL) {
		/*	if (pager.pagerActionType == ScrollRefreshPager.PAGER_ACTION_REFRESH){
				pager.onRefreshingComplete(false, 0);
			}else if (pager.pagerActionType == ScrollRefreshPager.PAGER_ACTION_MORE){
				pager.onPagingComplete(false, 0);
			}else if (pager.pagerActionType == ScrollRefreshPager.PAGER_ACTION_DEFAULT){
				return;
			}*/
			return;
		}
		
		switch(object.requestType){
		case Service.REQUEST_TYPE_GET_ROOM_BOARD:
			if (object.resultCode == ErrorHandler.COMMON_ERROR || object.resultCode == ErrorHandler.NETWORK_DISABLE ||
					object.resultCode == ErrorHandler.PARSING_ERROR){
				
				if (pager.pagerActionType ==  ScrollRefreshPager.PAGER_ACTION_REFRESH){
					if (pager.isPagerSettingsCompleted()){
						pager.onRefreshingComplete(false,roomBoardResponseObject != null?
								roomBoardResponseObject.totalBoardCount:0);
					}	
				}else if (pager.pagerActionType == ScrollRefreshPager.PAGER_ACTION_MORE){
					if (pager.isPagerSettingsCompleted()){
						pager.removeScrollListener();
						pager.onPagingComplete(false,roomBoardResponseObject != null?
								roomBoardResponseObject.totalBoardCount:0);
						
					}
				}
				
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				builder.setTitle(R.string.fatal_network_error);
				builder.setMessage(R.string.fatal_network_error_message);
				builder.setCancelable(false);
				builder.setPositiveButton(R.string.confirm,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								
							}
						});
				builder.create().show();
				pager.restoreScrollListener();
				
				return;
			}else if (object.resultCode == ErrorHandler.CONNECTION_TIMEOUT || object.resultCode == ErrorHandler.READ_TIMEOUT){
				
				AlertDialog.Builder builder2 = new AlertDialog.Builder(getActivity());
				builder2.setTitle(R.string.tempo_network_error);
				builder2.setMessage(R.string.tempo_network_error_message);
				builder2.setCancelable(false);
				builder2.setPositiveButton(R.string.confirm,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								new Handler().postDelayed(new Runnable() {
									public void run() {
									 getRoomBoard(roomId, requestPage);
									}
								}, 100);
							}
						});

				builder2.setNegativeButton(R.string.no,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								if (pager.pagerActionType ==  ScrollRefreshPager.PAGER_ACTION_REFRESH){
									if (pager.isPagerSettingsCompleted()){
										pager.onRefreshingComplete(false,roomBoardResponseObject != null?
												roomBoardResponseObject.totalBoardCount:0);
									}
									
									}else if (pager.pagerActionType == ScrollRefreshPager.PAGER_ACTION_MORE){
										if (pager.isPagerSettingsCompleted()){
											pager.onPagingComplete(false,roomBoardResponseObject != null?
													roomBoardResponseObject.totalBoardCount:0);
										}
									}
								}
						});

				builder2.create().show();
				return;
			}
			
			RoomBoardResponseObject obj = (RoomBoardResponseObject)object;
			
			if (pager.pagerActionType == ScrollRefreshPager.PAGER_ACTION_REFRESH ){
				pager.onRefreshingComplete(true,roomBoardResponseObject.totalBoardCount);
				updateUi(obj);
			
			}else if(pager.pagerActionType == ScrollRefreshPager.PAGER_ACTION_MORE){
				pager.onPagingComplete(true, obj.totalBoardCount);
				addItemToList(obj);
			
			}else if (pager.pagerActionType == ScrollRefreshPager.PAGER_ACTION_NON){
				updateUi(obj);
			}
			break;
		}			
	}	


	public void createNotice(String notice, int ownerId) {
		this.roomManagerNotice = notice;
		this.ownerId = ownerId;
		if (!((RoomDetailActivity)getActivity()).waitProgressDialog.isShowing()){
			((RoomDetailActivity)getActivity()).waitProgressDialog.show();
		}
		
		ArticleService articleService = ServiceManager.getServiceManager().getArticleService();
		articleService.setOnArticleCallback(this);
		articleService.createNotice(notice, ownerId,RoomDetailActivity.roomWithFragment.roomWithResponse.checkRoomWith.roomId);
	}
	
	private void deleteMessageFromList(ArticleActionResponseObject o2) {
		int index = findPositionById(o2.messageId);
		roomBoardResponseObject.messageList.remove(index);
		refreshList();
		
	}
	
	private void addMessageAtList(ArticleActionResponseObject o1) {
		Message message = new Message();
		message.messageId = o1.messageId;
		message.writeId = Session.getInstance().user.userIndex;
		message.writerNickname = Session.getInstance().user.nickName;
		message.writeTime="방금";
		message.isReply = false;
		message.message = writeEdit.getText().toString().trim();
		message.article_type = 0;
		message.replyCount = 0;
		message.parentId = 0;
		message.message= writeEdit.getText().toString().trim();
		message.writerImagePath = Session.getInstance().user.profileImagePath;
		
		roomBoardResponseObject.messageList.add(0,message);
		resetEdit();
		refreshList();
		roomBoardListView.setSelection(1);
	}
	
	private void refreshList() {
		pager.notifyDataSetChanged();	
		pager.setSelection(selectionPosition);
	}

	
	private void resetEdit() {
		writeEdit.setText("");
		
	}

	public int findPositionById(int id ){
		ArrayList<Message> messageList = roomBoardResponseObject.messageList;
		int index = -1;
		
		for (int i = 0; i < messageList.size(); i++){
			Message message = messageList.get(i);
			if (message.messageId == id){
				index = i;
				break;
			}
		}
		return index;
	}
	
	public void onArticleServiceCallback(BaseResponseObject object) {
		if (((RoomDetailActivity)getActivity()).waitProgressDialog.isShowing()){
			((RoomDetailActivity)getActivity()).waitProgressDialog.dismiss();
		}
		
		if (object.resultCode == Service.RESULT_FAIL) {
			return;
		}
		
		switch(object.requestType){
		case Service.REQUEST_TYPE_CREATE_NOTICE:
			if (object.resultCode == ErrorHandler.COMMON_ERROR || object.resultCode == ErrorHandler.NETWORK_DISABLE ||
					object.resultCode == ErrorHandler.PARSING_ERROR){
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				builder.setTitle(R.string.fatal_network_error);
				builder.setMessage(R.string.fatal_network_error_message);
				builder.setCancelable(false);
				builder.setPositiveButton(R.string.confirm,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								
							}
						});
				builder.create().show();
				return;
			}else if (object.resultCode == ErrorHandler.CONNECTION_TIMEOUT || object.resultCode == ErrorHandler.READ_TIMEOUT){
				AlertDialog.Builder builder2 = new AlertDialog.Builder(getActivity());
				builder2.setTitle(R.string.tempo_network_error);
				builder2.setMessage(R.string.tempo_network_error_message);
				builder2.setCancelable(false);
				builder2.setPositiveButton(R.string.confirm,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								new Handler().postDelayed(new Runnable() {
									public void run() {
										createNotice(roomManagerNotice, ownerId);
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
			
		
			updateNotice();
			break;
			
		case Service.REQUEST_TYPE_SUBMIT_NEW_COMMENT:
			if (object.resultCode == ErrorHandler.COMMON_ERROR || object.resultCode == ErrorHandler.NETWORK_DISABLE ||
					object.resultCode == ErrorHandler.PARSING_ERROR){
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				builder.setTitle(R.string.fatal_network_error);
				builder.setMessage(R.string.fatal_network_error_message);
				builder.setCancelable(false);
				builder.setPositiveButton(R.string.confirm,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								
							}
						});
				builder.create().show();
				return;
			}else if (object.resultCode == ErrorHandler.CONNECTION_TIMEOUT || object.resultCode == ErrorHandler.READ_TIMEOUT){
				AlertDialog.Builder builder2 = new AlertDialog.Builder(getActivity());
				builder2.setTitle(R.string.tempo_network_error);
				builder2.setMessage(R.string.tempo_network_error_message);
				builder2.setCancelable(false);
				builder2.setPositiveButton(R.string.confirm,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								new Handler().postDelayed(new Runnable() {
									public void run() {
										checkBeforSubmit();
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
			
			ArticleActionResponseObject o1 = (ArticleActionResponseObject)object;
			addMessageAtList(o1);
			break;
			
		case Service.REQUEST_TYPE_DELETE_COMMENT:
			if (object.resultCode == ErrorHandler.COMMON_ERROR || object.resultCode == ErrorHandler.NETWORK_DISABLE ||
					object.resultCode == ErrorHandler.PARSING_ERROR){
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				builder.setTitle(R.string.fatal_network_error);
				builder.setMessage(R.string.fatal_network_error_message);
				builder.setCancelable(false);
				builder.setPositiveButton(R.string.confirm,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								
							}
						});
				builder.create().show();
				return;
			}else if (object.resultCode == ErrorHandler.CONNECTION_TIMEOUT || object.resultCode == ErrorHandler.READ_TIMEOUT){
				AlertDialog.Builder builder2 = new AlertDialog.Builder(getActivity());
				builder2.setTitle(R.string.tempo_network_error);
				builder2.setMessage(R.string.tempo_network_error_message);
				builder2.setCancelable(false);
				builder2.setPositiveButton(R.string.confirm,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								new Handler().postDelayed(new Runnable() {
									public void run() {
										if (deletePosition !=-1){
											deleteMyComment(roomBoardResponseObject.messageList.get(deletePosition));
										}
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
			
			ArticleActionResponseObject o2 = (ArticleActionResponseObject)object;
			deletePosition = -1;
			deleteMessageFromList(o2);
			break;
		}	
		
	}


	public void updateNotice(){
		roomManagerNoticeView.setText(roomManagerNotice );
	}

	private void deleteMyComment(Message message) {
		if (!((RoomDetailActivity)getActivity()).waitProgressDialog.isShowing()){
			((RoomDetailActivity)getActivity()).waitProgressDialog.show();
		}
		
		selectionPosition = pager.getSelection();
		ArticleService articleService = ServiceManager.getServiceManager().getArticleService();
		articleService.setOnArticleCallback(this);	
		articleService.deleteComment(message.messageId);
	}
	
	//댓글을 서버로 등록한다.
	public void submitReplyComment(String comment, int parentId, int roomId) {
		if (!((RoomDetailActivity)getActivity()).waitProgressDialog.isShowing()){
			((RoomDetailActivity)getActivity()).waitProgressDialog.show();
		}
		
		ArticleService articleService = ServiceManager.getServiceManager().getArticleService();
		articleService.setOnArticleCallback(this);	
		articleService.submitReplyComment(comment, roomId, parentId);
		
	}

	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if ( resultCode != Activity.RESULT_OK) return;
		
		switch (requestCode) {
		case RoomDetailActivity.ACTION_VIEW_CONTENT:
			int parentMessageId =data.getIntExtra("parentMessageId", -1);
			int increase= data.getIntExtra("increase", 0);
			
			//메시지 아이디가 -1 이면 잘못된 것임 ,그냥 리턴
			if (parentMessageId == -1)
				return;
			/*increase 가 0 이면 댓글을 삭제하거나 추가 하지 않은 경우이거나, 삭제한 갯수와 추가한 갯수가 같기 때문에
			부모글에 대한 댓글의 수는 변함이 없는 경우이기 때문에 그냥 리턴함 */
			if (increase == 0)
				return;
			increaseReplyCount(parentMessageId,increase);
			break;
		}
	}


	private void increaseReplyCount(int messageId, int increase) {
		ArrayList<Message> messageList = roomBoardResponseObject.messageList;
		int updateIndex = -1;
		for (int i = 0; i <messageList.size(); i++){
			Message message = messageList.get(i);
			if (message.messageId == messageId){
				updateIndex = i;
				break;
			}
		}
		
		if (updateIndex !=-1 ){
			messageList.get(updateIndex).replyCount = messageList.get(updateIndex).replyCount +increase;
			refreshList();
		}
	}
	
	public class RoomBoardAdaper extends BaseAdapter{
		
		Context con;
		int resId;
		ArrayList<Message> messageList;
		LayoutInflater inflater;
		ImageLoader imageLoader;
		
		public RoomBoardAdaper(Context con, ImageLoader imageLoader, int resId, ArrayList<Message> messageList){
			this.con = con;
			this.imageLoader = imageLoader;
			this.resId = resId;
			this.messageList = messageList;
			this.inflater = (LayoutInflater) con.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		public int getCount() {
			return messageList.size();
		}
		
		public Object getItem(int position) {
			return messageList.get(position);
		}
		
		public long getItemId(int position) {
			return position;
		}
		public View getView(int position, View convertView, ViewGroup parent) {
			final int  pos = position;
			
			RoomBoardViewHolder viewHolder = null; 
			if (convertView == null){
				convertView = inflater.inflate(resId, parent, false);
				viewHolder = new RoomBoardViewHolder();
				viewHolder.writerImageView = (ImageView) convertView.findViewById(R.id.roomboard_row_image);
				viewHolder.wirterNameView =   (TextView)convertView.findViewById(R.id.roomboard_textview1);
				viewHolder.messageView =  (TextView)convertView.findViewById(R.id.roomboard_textview2);
				viewHolder.replyCoutView = (TextView)convertView.findViewById(R.id.reply_count);
				viewHolder.writeTimeView = (TextView)convertView.findViewById(R.id.roomboard_textview3);
				convertView.setTag(viewHolder);
			}else {
				viewHolder = (RoomBoardViewHolder)convertView.getTag();
			}

			String url = null;
			if (!CommonUtils.isNullOrEmpty(messageList.get(position).writerImagePath) &&
					!"0".equals(messageList.get(position).writerImagePath)){
				 url = Service.BASE_URL+messageList.get(position).writerImagePath;
			}
			
			imageLoader.displayImage(url, viewHolder.writerImageView, ImageLoader.DEFAULT_PROFILE_IMAGE); 
			
			viewHolder.wirterNameView.setText(messageList.get(position).writerNickname);
			viewHolder.messageView.setText(messageList.get(position).message);
			viewHolder.writeTimeView.setText(messageList.get(position).writeTime);
			viewHolder.replyCoutView.setText(CommonUtils.int2string(messageList.get(position).replyCount));
			
			final TextView anchorView = viewHolder.messageView;
			final View  container = convertView.findViewById(R.id.roomboard_contaniner);
			container.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					Message message = messageList.get(pos);
					
					Intent i = new Intent(getActivity(), BoardContentActivity.class);
					i.putExtra("messageId",message.messageId );
					i.putExtra("writerId",message.writeId);
					i.putExtra("writerImagePath", message.writerImagePath);
					i.putExtra("writerNickname", message.writerNickname);
					i.putExtra("writeTime", message.writeTime);
					i.putExtra("message",message.message);
					i.putExtra("replyCount", message.replyCount);
					i.putExtra("roomId",RoomDetailActivity.roomWithFragment.roomWithResponse.checkRoomWith.roomId);
					i.putExtra("userType", ((RoomDetailActivity)getActivity()).behaviorType);
					
					getActivity().startActivityForResult(i, RoomDetailActivity.ACTION_VIEW_CONTENT);
				}
			});
			final QuickAction listQuickAction = new QuickAction(activity, QuickAction.VERTICAL);
			//listQuickAction.addActionItem(modifyItem);
			listQuickAction.addActionItem(deleteItem);
			container.setOnLongClickListener(new OnLongClickListener() {
				public boolean onLongClick(View v) {	
					if (Session.getInstance().sessionStatus != Session.AUTHORIZED){
						return true;
					}
					if (Session.getInstance().user.userIndex !=messageList.get(pos).writeId){
						return true;
					}
				
					listQuickAction.show(anchorView);
					return true;
					}
				});
				
		        listQuickAction.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {			
					public void onItemClick(QuickAction source, int actionPos, int actionId) {				
						ActionItem actionItem = listQuickAction.getActionItem(actionPos);
						if (actionId == ACTION_ID_MODIFY) {
								
						} else if (actionId == ACTION_ID_DELETE) {
							deletePosition = pos;
							deleteMyComment(messageList.get(pos));
					    }
					}
		        });		
		        
		/*    리스트 아이템이 이제는 상단, 미들, 하단별로 별도의 이미지를 사용하지 않기 때문에 주석처리함
			if (position == 0){
				convertView.setBackgroundResource(R.drawable.roomboard_comment_top_bg2);
			}else if (position == getCount()-1){
				convertView.setBackgroundResource(R.drawable.roomboard_comment_bottom_bg2);
			}else {
				convertView.setBackgroundResource(R.drawable.roomboard_comment_center_bg2);
			}
			*/
			return convertView;
		}
	}
	public class RoomBoardViewHolder{
		public ImageView writerImageView;
		public TextView wirterNameView;
		public TextView messageView;
		public TextView replyCoutView;
		public TextView writeTimeView;
	}
}
