package com.teamteam.witherest;

public class WitheState {
	
	private static WitheState witheState = null;
	public static final int INVALID_ID = -1;
	
	public static final int CREATE_ROOM = 1;
	public static final int JOIN_ROOM = 2;
	public static final int LEAVE_ROOM = 3;
	public static final int MODIFY_ROOM = 4;
	public static final int DELETE_ROOM = 5;
	
	public boolean haveChanged;
	public int changeType;
	public  boolean mustAllLoaded;
	public  boolean mustOneLoaded;
	
	public boolean mustAllModified;
	public boolean mustOneModified;
	
	public boolean mustAllDeleted;
	public boolean mustOneDeleted;
	
	public Object object;
	public  int id;
	
	
	private WitheState(){
		mustAllLoaded = false;
		mustOneLoaded = false;
		id = INVALID_ID;
		object = null;
	}
	
	public static WitheState getInstance(){
		if (witheState == null){
			witheState = new WitheState();
		}
		return witheState;
	}
		
	public void init() {
		haveChanged = false;
		mustAllLoaded = false;
		mustOneLoaded = false;
		id = INVALID_ID;
		object = null;
	}
}
