package com.teamteam.customComponent;

import com.teamteam.customComponent.widget.RefreshListView;
import com.teamteam.witherest.RoomBoardFragment.RoomBoardAdaper;
import com.teamteam.witherest.R;

import com.teamteam.witherest.service.callback.object.RoomBoardResponseObject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;

public class RoomBoardScrollPager {
	private int totalRecordCount;
	private int page;
	private int pagingSize;
	
	public RoomBoardResponseObject roomBoardResponseObject;
	public RoomBoardAdaper roomBoardAdaper;
	
	private ListView listView;
	private View footerView;
	private View loadFooter;
	private int footerRes = R.layout.footer;
	
	private LayoutInflater inflater;
	
	private Context context;
	private boolean isLoading = true;
	
	public static final int PAGING_SIZE = 10;
	
	public RoomBoardScrollPager(Context context, ListView listView, int page, int pagingSize, int totalRecordCount){
		this.context = context;
		inflater = (LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		footerView = inflater.inflate(footerRes, null, false);
		loadFooter = (ProgressBar)footerView.findViewById(R.id.iv_list_footer_loading);
		this.page = page;
		this.pagingSize = pagingSize;
		this.totalRecordCount= totalRecordCount;
		this.listView = listView;
		
		init();
	}
	
	private void init() {
		if (this.totalRecordCount > getCurRecordCount()){
			((RefreshListView)listView).addFooterView(footerView);
		}
	}
	
	public void setAdapterData(Object object){
		roomBoardResponseObject = (RoomBoardResponseObject)object;
	}
	
	public void setAdapter(Object object){
		roomBoardAdaper= (RoomBoardAdaper)object;
	}
	
	public void populate(){
		listView.setAdapter(roomBoardAdaper);
	}
	
	public int getCurRecordCount(){
		return page * PAGING_SIZE;
	}

	public int getTotalRecordCount() {return totalRecordCount;}
	public void setTotalRecordCount(int totalRecordCount) {this.totalRecordCount = totalRecordCount;}

	public int getPage() {return page;}
	public void setPage(int page) {this.page = page;}

	public int getPagingSize() {return pagingSize;}
	public void setPagingSize(int pagingSize) {this.pagingSize = pagingSize;}

	public boolean isLoading() {return isLoading;}
	public void setLoading(boolean isLoading) {this.isLoading = isLoading;}


	
	
}
