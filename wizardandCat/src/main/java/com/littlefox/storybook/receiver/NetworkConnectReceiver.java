package com.littlefox.storybook.receiver;

import com.littlefox.storybook.lib.common.NetworkUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;

public class NetworkConnectReceiver extends BroadcastReceiver
{

	public interface NetworkConnectListener
	{
		public void connectStatus(int connectType);

	}
	
	private NetworkConnectListener mNetworkConnectListener;
	public NetworkConnectReceiver(NetworkConnectListener networkConnectListener)
	{
		mNetworkConnectListener = networkConnectListener;
	}
	
	
	@Override
	public void onReceive(Context context, Intent intent)
	{
		mNetworkConnectListener.connectStatus(NetworkUtil.getConnectivityStatus(context));
	}
	
	public void register(Context context)
	{
		context.registerReceiver(this, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
	}
	
	public void unregister(Context context)
	{
		context.unregisterReceiver(this);
	}

}
