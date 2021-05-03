package com.teamteam.customComponent.disable;

import com.teamteam.witherest.R;
import com.teamteam.witherest.RoomDetailActivity;
import com.teamteam.witherest.R.drawable;
import com.teamteam.witherest.R.id;
import com.teamteam.witherest.R.layout;
import com.teamteam.witherest.cacheload.ImageLoader;
import com.teamteam.witherest.common.CommonUtils;
import com.teamteam.witherest.service.callback.object.CategoryRoomListResponseObject;
import com.teamteam.witherest.service.callback.object.MyCheckResponseObject.CheckRoom;
import com.teamteam.witherest.service.internal.Service;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class PopRoomListFragment extends ListFragment{
	private static final String KEY_CONTENT = "Fragment:Content";
	
	public int page;
	public int curRecordCount;
	public int totalRecordCount;
	
	public int listType;;
	public String title;
	
	public Activity act;
	public ImageLoader imageLoader;
	public CategoryRoomListResponseObject mRoomListResponse;
	public View fragmentView;
	public PopRoomListFragmentAdapter adapter;
	
	public static PopRoomListFragment newInstance(String title, int page, int listType){
		PopRoomListFragment fragment = new PopRoomListFragment();
		fragment.setInitalData(title, page, listType);

		Bundle args = new Bundle();
	    args.putString("title", title);
        args.putInt("listType", listType);
        args.putInt("page", page);
        fragment.setArguments(args);
        return fragment;
	}
	
	public void setInitalData(String title, int page, int listType){
		this.title = title;
		this.page = page;
		this.listType = listType;
	}
	
	public void setImageLoader(ImageLoader imageLoader){
		this.imageLoader = imageLoader;
	}
	
	public void setRoomListObject(CategoryRoomListResponseObject object, int listType){
		Log.v(title + " 룸리스트  세팅 " , getArguments().getInt("type") + " ");
		this.mRoomListResponse = object;
		setListAdapter(new PopRoomListFragmentAdapter(getActivity(),
					R.layout.fragment_pop_room_list_item, mRoomListResponse, imageLoader));	
	}
	
	
    @Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		this.act = activity;
	    Log.v(getArguments().getString("title") + " 리스트프래그먼트 " + getArguments().getInt("type"),"onAttach) 호출");
	  
	     /*  프래그먼트 각각이 ImageLoader 를 생성해서 별도의 캐시를 관리하고자 하면 
	     아래의 코드의 주석을 푼다 
	     imageLoader = new ImageLoader(getActivity().getApplicationContext());*/
	}


	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(title + " 리스트프래그먼트 " + getArguments().getInt("type"),"onCreate() 호출");
    }
    

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		 Log.v(title + " 리스트프래그먼트 " + getArguments().getInt("type"),"onCreateView() 호출");
		fragmentView= inflater.inflate(R.layout.fragment_pop_room_list, null);
		return fragmentView;
		
	}

	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		/*Intent i = new Intent(getActivity(), RoomDetailActivity.class);
		getActivity().startActivity(i);*/
	}

	@Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
	
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		 Log.v(title + " 리스트프래그먼트 " + getArguments().getInt("type"),"onActivityCreated() 호출");
		super.onActivityCreated(savedInstanceState);
	}
	
	@Override
	public void onResume() {
		 Log.v(title + " 리스트프래그먼트 " + getArguments().getInt("type"),"onResume() 호출");
		super.onResume();	
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	
	public class PopRoomListFragmentAdapter extends BaseAdapter{
		public Context  context ; 
		public int resId ;
		public LayoutInflater inflate;
		public CategoryRoomListResponseObject roomListRespose;
		public ImageLoader imageLoader; 
	
		public PopRoomListFragmentAdapter (Activity act, int layoutRes,CategoryRoomListResponseObject roomListRespose,
				ImageLoader imageLoader){
			
			this.context = act;
			this.resId = layoutRes;
			this.roomListRespose = roomListRespose;
		
			inflate = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			
			this.imageLoader=imageLoader;
			
			Log.v(title + "리스트 갯수", roomListRespose.roomList.size() + "개");
			
			for (CheckRoom room : roomListRespose.roomList){
				String tmp;
				if (CommonUtils.isNullOrEmpty(room.roomImagePath)){
				tmp ="이미지가 없습니다.";
				}
				else {
					tmp = room.roomImagePath;
				}
				Log.v(room.roomId +":" +  room.roomTitle, tmp );
			}
		}
		
		public int getCount() {
			return roomListRespose.roomList.size();
		}

		public Object getItem(int position) {
			return roomListRespose.roomList.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			
			final int pos = position;
			if (convertView == null){
				convertView = inflate.inflate(resId,parent, false);
			}
			Log.v( title + " 리스트 포지션" + position, roomListRespose.roomList.get(position).roomTitle+"");
			
			ImageView roomImage = (ImageView)convertView.findViewById(R.id.pop_list_image);
			
			TextView roomTitle = (TextView)convertView.findViewById(R.id.pop_list_title_textview);
			TextView roomPurpose = (TextView)convertView.findViewById(R.id.pop_list_purpose_textview);
			TextView roomTerm = (TextView)convertView.findViewById(R.id.pop_list_term_textview);
			TextView rooJoinState = (TextView)convertView.findViewById(R.id.pop_list_joincount_textview);
			
			roomTitle.setText(roomListRespose.roomList.get(position).roomTitle);
			roomPurpose.setText(roomListRespose.roomList.get(position).roomPurpose);
			roomTerm.setText(roomListRespose.roomList.get(position).startDate + " ~ " + roomListRespose.roomList.get(position).endDate);
			rooJoinState.setText(roomListRespose.roomList.get(position).curMemberCount + " / " + roomListRespose.roomList.get(position).maxMemberCount );
			
			convertView.findViewById(R.id.pop_list_contaniner).setOnClickListener(new View.OnClickListener() {
				public void onClick(View arg0) {
					Intent i = new Intent(context, RoomDetailActivity.class);
					i.putExtra("roomId",roomListRespose.roomList.get(pos).roomId );
					i.putExtra("roomTitle", roomListRespose.roomList.get(pos).roomTitle);
					context.startActivity(i);	
				}
			});
			
			 roomImage.setImageResource(R.drawable.stub);
			if ( CommonUtils.isNullOrEmpty(roomListRespose.roomList.get(position).roomImagePath)){
			
				 Log.v(title +" [" + roomListRespose.roomList.get(position).roomTitle +  "] 다운로드 이미지 URL" ,
						 Service.BASE_URL+" "+roomListRespose.roomList.get(position).roomImagePath +"--->  not exist");
				
				
			}else if( !CommonUtils.isNullOrEmpty(roomListRespose.roomList.get(position).roomImagePath)) {
				 Log.v(title +" [" + roomListRespose.roomList.get(position).roomTitle +  "] Image URL" ,
						 Service.BASE_URL+roomListRespose.roomList.get(position).roomImagePath );
				imageLoader.displayImage(Service.BASE_URL+roomListRespose.roomList.get(position).roomImagePath, roomImage, ImageLoader.DEFAULT_ROOM_IMAGE);
			}	
			return convertView;
		}
	}
}
