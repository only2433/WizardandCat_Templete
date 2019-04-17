package com.littlefox.storybook.wizardandcat;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.GridLayoutManager.SpanSizeLookup;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ItemAnimator;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.support.v7.widget.SimpleItemAnimator;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.vending.billing.util.IabResult;
import com.google.gson.Gson;
import com.littlefox.library.view.animator.AnimationListener.Start;
import com.littlefox.library.view.animator.AnimationListener.Stop;
import com.littlefox.library.view.animator.ViewAnimator;
import com.littlefox.logmonitor.Log;
import com.littlefox.storybook.base.BaseActivity;
import com.littlefox.storybook.common.Feature;
import com.littlefox.storybook.common.InformationTemplete;
import com.littlefox.storybook.lib.adapter.StorybookListRecyclerAdapter;
import com.littlefox.storybook.lib.adapter.decoration.GridSpacingItemDecoration;
import com.littlefox.storybook.lib.adapter.listener.StorybookListItemListener;
import com.littlefox.storybook.lib.analytics.GoogleAnalyticsHelper;
import com.littlefox.storybook.lib.api.StorybookTempleteAPI;
import com.littlefox.storybook.lib.async.PaidInformationSendAsync;
import com.littlefox.storybook.lib.async.PromotionCodeUseAsync;
import com.littlefox.storybook.lib.async.PromotionInformationAsync;
import com.littlefox.storybook.lib.async.PromotionRestoreAsync;
import com.littlefox.storybook.lib.async.listener.AsyncListener;
import com.littlefox.storybook.lib.billing.IBillingStatusListener;
import com.littlefox.storybook.lib.billing.InAppPurchase;
import com.littlefox.storybook.lib.common.Common;
import com.littlefox.storybook.lib.common.CommonUtils;
import com.littlefox.storybook.lib.common.FileUtils;
import com.littlefox.storybook.lib.common.Font;
import com.littlefox.storybook.lib.common.NetworkUtil;
import com.littlefox.storybook.lib.dialog.AppraisalDialog;
import com.littlefox.storybook.lib.dialog.CreditDialog;
import com.littlefox.storybook.lib.dialog.DialogListener;
import com.littlefox.storybook.lib.dialog.FlexibleDialog;
import com.littlefox.storybook.lib.dialog.LoadingDialog;
import com.littlefox.storybook.lib.dialog.PaymentDialog;
import com.littlefox.storybook.lib.dialog.PromotionDialog;
import com.littlefox.storybook.lib.dialog.PromotionListDialog;
import com.littlefox.storybook.lib.dialog.RecommandAppScrollDialog;
import com.littlefox.storybook.lib.dialog.TempleteAlertDialog;
import com.littlefox.storybook.lib.download.ContinueDownloadAsync;
import com.littlefox.storybook.lib.download.DownloadPlayListener;
import com.littlefox.storybook.lib.download.ReturnIndexDownloadAsync;
import com.littlefox.storybook.lib.object.DisPlayMetricsObject;
import com.littlefox.storybook.lib.object.IACInformation;
import com.littlefox.storybook.lib.object.InitItemResult;
import com.littlefox.storybook.lib.object.PromotionBaseResult;
import com.littlefox.storybook.lib.object.PromotionRestoreResult;
import com.littlefox.storybook.lib.object.SharedVideoInfo;
import com.littlefox.storybook.receiver.NetworkConnectReceiver;
import com.littlefox.storybook.receiver.NetworkConnectReceiver.NetworkConnectListener;
import com.ssomai.android.scalablelayout.ScalableLayout;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;



public class MainListActivity extends BaseActivity {
	
	private static final int MAX_INTRODUCTION_PHONE_HEIGHT 	= 842;
	private static final int MAX_INTRODUCTION_TABLET_HEIGHT = 467;
	
	private static final int SPAN_COUNT_TABLET 	= 4;
	private static final int SPAN_COUNT_PHONE 	= 2;
	
	private static final int ASYNC_MESSAGE_PROMOTION_QUERY		 = 0;
	private static final int ASYNC_MESSAGE_PROMOTION_RESTORE	 = 1;
	
	private static final int MESSAGE_PROMOTION_QUERY		 				= 0;
	private static final int MESSAGE_PROGRESS_ING 			 				= 1;
	private static final int MESSAGE_PROGRESS_COMPLETE 		 				= 2;
	private static final int MESSAGE_SHOW_PROMOTION_DIALOG 	 				= 3;
	private static final int MESSAGE_DEVELOPER_INQUIRE		 				= 4;
	private static final int MESSAGE_CONTINUE_DOWNLOAD		 				= 5;
	private static final int MESSAGE_APPRAISAL_SHOW			 				= 6;
	private static final int MESSAGE_SHOW_SCROLL_MOVE_BUTTON 				= 7;
	private static final int MESSAGE_HIDE_SCROLL_MOVE_BUTTON 				= 8;
	private static final int MESSAGE_SHOW_SCROLL_INTRODUCTION_MENU_BUTTON 	= 9;
	private static final int MESSAGE_HIDE_SCROLL_INTRODUCTION_MENU_BUTTON 	= 10;
	private static final int MESSAGE_NETWORK_ERROR			 				= 11;
	private static final int MESSAGE_BGM					 				= 12;
	private static final int MESSAGE_RESTORE								= 13;


	private static final int MESSAGE_ANIMATION_END			 = 14;
	private static final int MESSAGE_LIST_CLICK_DOWNLOAD	 = 15;
	private static final int MESSAGE_SHOW_GRID_VIEW		     = 16;
	private static final int MESSAGE_INTRODUCTION_ANI		 = 17;
	private static final int MESSAGE_FORCE_SCROLL			 = 18;
	private static final int MESSAGE_SHOW_CREDIT_DIALOG		 = 19;
	
	private static final int DURATION_CONTINUE_DOWNLOAD		= 1500;
	private static final int DURATION_APPEAR				= 250;
	private static final int DURATION_BGM_START				= 1500;
	private static final int DURATION_ICON_VISIBLE			= 500;
	private static final int DURATION_ANIMATION				= 500;
	
	private static final int CODE_ALL_PAYMENT 				= 101;
	
	
	private SharedVideoInfo mVideoBase;
	private IACInformation mIACInformation;
	
	private LinearLayout _IntroductionLayout;

	private StorybookListRecyclerAdapter mStorybookListRecyclerAdapter;
	private RecyclerView _MainRecyclerView;


	private ImageView _ScrollMoveButton;
	private ImageView _MenuBackgroundMusicModeIcon;
	private ImageView _MenuActionModeIcon;
	
	private DrawerLayout _DrawerLayout;
	private LinearLayout _SlideMenuLayout;
	
	private ScalableLayout _MenuWriterLayout;
	private ScalableLayout _MenuOtherAppLayout;
	private ScalableLayout _MenuInputPromotionLayout;

	private ScalableLayout _MenuInquireDeveloperLayout;
	private ScalableLayout _MenuRestorePurchaseLayout;
	private ScalableLayout _MenuBackgroundMusicLayout;
	private ScalableLayout _MenuActionStoryModeLayout;
	

	private ImageView _RecommonadShowView;
	
	private LoadingDialog mProgressDialog;
	
	private String mPromotionInputCode = "";


	private int mCurrentMessage = -1;

	
	private int mCurrentDownloadPosition = -1;
	private ContinueDownloadAsync mContinueDownloadAsync;
	
	private Font mFont;
	
	private PromotionDialog mPromotionDialog = null;
	private PromotionListDialog mPromotionListDialog = null;
	private PromotionBaseResult mPromotionBaseResult;
	private PromotionRestoreResult mPromotionRestoreResult;
	private PromotionCodeUseAsync mPromotionCodeUseAsync;
	
	private PaymentDialog mPaymentDialog 				= null;
	private AppraisalDialog mAppraisalDialog 			= null;
	private CreditDialog mCreditDialog;
	private RecommandAppScrollDialog mRecommandAppInformationDialog = null;
	
	private InAppPurchase mInAppPurchase;
	private InitItemResult mInitItemResult;
	
	
	/** 현재 결제 하려는 아이템의 index */
	private int mCurrentPayingPosition = -1;
	
	/** 현재 InAppPurchase 의 상태 */
	private int mCurrentInAppStatus = -1;
	
	private NetworkConnectReceiver mNetworkConnectReceiver;
	
	private MediaPlayer mBGMPlayer = null;
	
	/** 현재 플레이중인 Bgm Index */
	private int mCurrentBgmPlayIndex = -1;
	 
	
	/**
	 * 이전의 연결상태를 기억해서 현재상태를 처리하기위해
	 */
	private int mBeforeConnectStatus = -1;

	private AnimationDrawable mRecommandAppIconDrawable;
	
	private DisplayMetrics mDisplayMetrics = null;
	
	private ScalableLayout _MenubarLayout;
	private ImageView _TopBarCoverImage;
	private ImageView _MenubarControlButton;
	
	private int mIntroductionPhoneMoveheight = 0;
	
	Handler mMainHandler = new Handler()
	{

		@Override
		public void handleMessage(Message msg)
		{
			switch(msg.what)
			{

			case MESSAGE_PROMOTION_QUERY:
				showIndeterminateDialog();
				requestPromotionInformation((String) msg.obj);
				break;
			case MESSAGE_PROGRESS_ING:
			/*	if(mCurrentProgressView != null)
				{
					mCurrentProgressView.setPercent(msg.arg1);
				}*/
				mVideoBase.getVideoInfoList().get(mCurrentDownloadPosition).setDownloadProgress(msg.arg1);
				mStorybookListRecyclerAdapter.downloadingProgress(mCurrentDownloadPosition);
				break;
			case MESSAGE_PROGRESS_COMPLETE:
				/**
				 * Syncronize 되는 상황을 피하기 위해 새로이 변수를 만들어 처리.
				 */
				if(isDownloadAvailable() == true)
				{
					mVideoBase.deleteQueueDownloadItem();
					
					int downloadCompleteItemIndex = mCurrentDownloadPosition + 1;
					mStorybookListRecyclerAdapter.notifyItemChanged(downloadCompleteItemIndex);
					
					if(mVideoBase.pullDownloadItem() != -1)
					{
						mCurrentDownloadPosition = mVideoBase.pullDownloadItem();
						int downloadingItemIndex = mCurrentDownloadPosition + 1;
						downloadStart();
						mStorybookListRecyclerAdapter.notifyItemChanged(downloadingItemIndex);
					}
					
					FileUtils.writeFile(mVideoBase, StorybookTempleteAPI.PATH_VIDEO_INFORMATION_ROOT+Common.FILE_NEW_VIDEO_INFORMATION);
				}
				else
				{
					showTempleteAlertDialog(TempleteAlertDialog.DIALOG_EVENT_DEFAULT, TempleteAlertDialog.DEFAULT_BUTTON_TYPE_1,
							getResources().getString(R.string.storage_available_error));
				}
				break;
			case MESSAGE_SHOW_PROMOTION_DIALOG:
				showPromotionDialog();
				break;
			case MESSAGE_DEVELOPER_INQUIRE:
				inquireForDeveloper();
				break;
			case MESSAGE_CONTINUE_DOWNLOAD:
				Log.i("isDownloadAvailable() : "+isDownloadAvailable());
				if(isDownloadAvailable() == true)
				{
					if(mVideoBase.pullDownloadItem() != -1)
					{
						mCurrentDownloadPosition = mVideoBase.pullDownloadItem();
						downloadStart();
					}
					mStorybookListRecyclerAdapter.notifyDataSetChanged();
					FileUtils.writeFile(mVideoBase, StorybookTempleteAPI.PATH_VIDEO_INFORMATION_ROOT+Common.FILE_NEW_VIDEO_INFORMATION);
				}
				else
				{
					showTempleteAlertDialog(TempleteAlertDialog.DIALOG_EVENT_DEFAULT, TempleteAlertDialog.DEFAULT_BUTTON_TYPE_1,
							getResources().getString(R.string.storage_available_error));
				}

				break;
			case MESSAGE_APPRAISAL_SHOW:				
				if(mVideoBase.isRateComplete() == false && mVideoBase.isAppraisalTiming(MainListActivity.this) == true)
				{
					showAppraisalDialog();
				}
				break;
			case MESSAGE_SHOW_SCROLL_MOVE_BUTTON:
				_ScrollMoveButton.startAnimation(CommonUtils.getInstance(MainListActivity.this).getAlphaAnimation(true,mScrollButtonMoveListener));
				_ScrollMoveButton.setVisibility(View.VISIBLE);
				break;
			case MESSAGE_HIDE_SCROLL_MOVE_BUTTON:
				_ScrollMoveButton.startAnimation(CommonUtils.getInstance(MainListActivity.this).getAlphaAnimation(false,mScrollButtonMoveListener));
				_ScrollMoveButton.setVisibility(View.GONE);
				break;
			case MESSAGE_SHOW_SCROLL_INTRODUCTION_MENU_BUTTON:
				_MenubarControlButton.startAnimation(CommonUtils.getInstance(MainListActivity.this).getAlphaAnimation(true,mIntroductionButtonMoveListener));
				_MenubarControlButton.setVisibility(View.VISIBLE);
				break;
			case MESSAGE_HIDE_SCROLL_INTRODUCTION_MENU_BUTTON:
				_MenubarControlButton.startAnimation(CommonUtils.getInstance(MainListActivity.this).getAlphaAnimation(false,mIntroductionButtonMoveListener));
				_MenubarControlButton.setVisibility(View.GONE);
				break;
			case MESSAGE_NETWORK_ERROR:
				showTempleteAlertDialog(TempleteAlertDialog.DIALOG_EVENT_DEFAULT, TempleteAlertDialog.DEFAULT_BUTTON_TYPE_1, getResources().getString(R.string.network_error));
				break;
			case MESSAGE_BGM:
				startBGMPlayer();
				break;
			case MESSAGE_RESTORE:
				showIndeterminateDialog();
				requestPromotionRestore();
				break;

			case MESSAGE_LIST_CLICK_DOWNLOAD:
				mVideoBase.addQueueItem(mCurrentDownloadPosition);
				downloadStart();
				mStorybookListRecyclerAdapter.notifyDataSetChanged();
				break;
			case MESSAGE_SHOW_GRID_VIEW:
				
				break;
			case MESSAGE_INTRODUCTION_ANI:
				hideRecommandIconAnimation(true);
				// TODO : Introduction 뷰 애니메이션 + 상단 Introduction 을 내리는 아이콘 애니메이션 동시에
				break;
			case MESSAGE_FORCE_SCROLL:
				//TODO : 리스트를 강제로 최상단으로 올린다.
				break;
			case MESSAGE_SHOW_CREDIT_DIALOG:
				showCreditDialog();
				break;
			}
		}
		
	};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Log.f("");
        if(Feature.IS_TABLET_MODE == true)
        {
        	setContentView(R.layout.main_activity_layout_tablet);
        }
        else
        {
        	setContentView(R.layout.main_activity_layout);
        }

        Log.i("");

        initDrawerLayout();
        InitFont();
        initView();
        makeIntroductionPeopleItem();
        

		Common.IS_VIBRATE_ENABLE = (Boolean) CommonUtils.getInstance(this).getSharedPreference(Common.PARAMS_ACTION_STORY_MODE, Common.TYPE_PARAMS_BOOLEAN, InformationTemplete.AVAILABLE_ACTION_STORY_MODE == true ? true : false);
		Common.IS_BACKGROUND_SOUND_ENABLE = (Boolean) CommonUtils.getInstance(this).getSharedPreference(Common.PARAMS_BACKGROUND_SOUND, Common.TYPE_PARAMS_BOOLEAN, true);
		_MenuBackgroundMusicModeIcon.setImageResource(getModeImage(Common.IS_BACKGROUND_SOUND_ENABLE));
		_MenuActionModeIcon.setImageResource(getModeImage(Common.IS_VIBRATE_ENABLE));
		
		if(FileUtils.checkFile(StorybookTempleteAPI.PATH_APP_ROOT + Common.FILE_IAC_INFORMATION) == true)
		{
			Log.i("file FILE_IAC_INFORMATION exist");
			String fileString = FileUtils.getStringFromFile(StorybookTempleteAPI.PATH_APP_ROOT+ Common.FILE_IAC_INFORMATION);
			Log.f("IAC Information : "+ fileString);
			try
			{
				mIACInformation = new Gson().fromJson(fileString, IACInformation.class);
				mIACInformation.awakeIACItem();
			}catch(Exception e)
			{
				
			}
		}
		else
		{
			mIACInformation = new IACInformation();
		}
		
		
		if(FileUtils.checkFile(StorybookTempleteAPI.PATH_VIDEO_INFORMATION_ROOT+Common.FILE_NEW_VIDEO_INFORMATION) == true)
    	{
    		Log.f("file FILE_VIDEO_INFORMATION exist");

    		String fileString = FileUtils.getStringFromFile(StorybookTempleteAPI.PATH_VIDEO_INFORMATION_ROOT + Common.FILE_NEW_VIDEO_INFORMATION);
    		Log.f("Video Information : "+ fileString);
    		try
			{
    			mVideoBase = new Gson().fromJson(fileString, SharedVideoInfo.class);
				mMainHandler.sendEmptyMessageDelayed(MESSAGE_SHOW_GRID_VIEW, 0);
			}
			catch (Exception e)
			{
				
			}
		
    		mInitItemResult = (InitItemResult) CommonUtils.getInstance(this).getPreferenceObject(Common.PARAMS_INIT_INFO, InitItemResult.class);
    		initRecyclerView();
    		initInAppPurchase();
    		setupInAppPurchaseListener();
    		mBeforeConnectStatus = NetworkUtil.getConnectivityStatus(this);
    	}
		else
		{
			Toast.makeText(this, getResources().getString(R.string.network_error), Toast.LENGTH_LONG).show();
			finish();
		}
		
		/**
		 * 이전 목록으로 가는 부분은 삭제 
		 */
		
		/*
			int moveScrollY = (Integer)CommonUtils.getSharedPreference(this, Common.PARAMS_BEFORE_WATCHED_MOVIE, Common.TYPE_PARAMS_INTEGER);
			

			Log.i("moveScrollY : "+moveScrollY);
			if(moveScrollY != -1)
			{
				showFlexibleDialog(FlexibleDialog.MODE_TEXT, FlexibleDialog.DIALOG_EVENT_BEFORE_WATCHED_MOVE, 
						getResources().getString(R.string.btn_move), getResources().getString(R.string.btn_ignore),getResources().getString(R.string.move_to_the_before_play));
			}
		*/
		
		
		mNetworkConnectReceiver = new NetworkConnectReceiver(onNetworkConnectListener);
		if(NetworkUtil.getConnectivityStatus(this) != NetworkUtil.TYPE_NOT_CONNECTED)
		{
    		mInAppPurchase.settingPurchaseInforamtionToGoogle();
    		
    		if(NetworkUtil.getConnectivityStatus(this) == NetworkUtil.TYPE_MOBILE)
    		{
    			if(mVideoBase.pullDownloadItem() != -1)
    			{
    				showTempleteAlertDialog(TempleteAlertDialog.DIALOG_EVENT_MOBILE_DOWNLOAD_SUBMIT, 
    						getResources().getString(R.string.btn_download_cancel), getResources().getString(R.string.btn_download_continue), getResources().getString(R.string.lte_3g_relate_question));
    			}
    		}
    		else
    		{
    			if(mVideoBase.pullDownloadItem() != -1)
    			{
        			mMainHandler.sendEmptyMessageDelayed(MESSAGE_CONTINUE_DOWNLOAD, DURATION_CONTINUE_DOWNLOAD);

    			}
    		}
    		
		}
		else
		{
			if(mVideoBase.pullDownloadItem() != -1)
			{
				showTempleteAlertDialog(TempleteAlertDialog.DIALOG_EVENT_DEFAULT , TempleteAlertDialog.DEFAULT_BUTTON_TYPE_1, getResources().getString(R.string.network_download_error));
			}
		}
		
		if(isVisibleIAC() == true)
		{
			showIACTempleteAlertDialog();
		}
		
		//showTestIACDialog();
		
		if(NetworkUtil.getConnectivityStatus(this) != NetworkUtil.TYPE_NOT_CONNECTED)
		{
			if(CommonUtils.getInstance(this).isAppVersionEqual(mInitItemResult.version.newest) == false)
			{
				if(mInitItemResult.version.update_type.equals(Common.CRITICAL_UPDATE_KEY))
				{
					showTempleteAlertDialog(TempleteAlertDialog.DIALOG_EVENT_UPDATE_APP , TempleteAlertDialog.DEFAULT_BUTTON_TYPE_1, getResources().getString(R.string.force_update_check),false);
				}
				else
				{
					showTempleteAlertDialog(TempleteAlertDialog.DIALOG_EVENT_UPDATE_APP , TempleteAlertDialog.DEFAULT_BUTTON_TYPE_2, getResources().getString(R.string.select_update_check),true);
				}
			}
		}
		
		if(Feature.IS_WEB_EXCUTE_WITH_COUPON == true)
		{
			showPromotionDialogToWeb();
		}
		
		if(isAllDownloadRecommandIcon())
		{
			//TODO: 아이콘 보여주기
			setRecommandDialogIconVisiblity(true);	
		}
		else
		{
			startDownloadRecommandIcon();
		}
	}
    
    
    /**
     * View의 사이즈는 Window  가 Focus  된 후에 알수 있다.
     */
    @Override
	public void onWindowFocusChanged(boolean hasFocus)
	{
		super.onWindowFocusChanged(hasFocus);
		

	}

    private void initInAppPurchase()
    {
    	mInAppPurchase = InAppPurchase.getInstance();
    	mInAppPurchase.init(this);
    }
    
    
    private void setupInAppPurchaseListener()
    {
    	mInAppPurchase.setOnBillingStatusListener(new IBillingStatusListener()
		{
			
			@Override
			public void onQueryInventoryFinished(IabResult result)
			{
				Log.f("");
				mCurrentInAppStatus = InAppPurchase.STATUS_QUERY_INVENTORY_FINISHED;
				preventCancelItem();
				
				for(int i = 0 ; i < mInAppPurchase.getOwnedPurchaseItemList().size(); i++)
				{
					Log.f("결제 한 아이템 : "+ mInAppPurchase.getOwnedPurchaseItemList().get(i));
				}
				
				if(Common.INIT_DATA_RESTORE)
				{
					Log.f("It has previous data. data is clear and restore.");
					mMainHandler.sendEmptyMessageDelayed(MESSAGE_RESTORE, DURATION_APPEAR);
				}
			}
			
			@Override
			public void onIabPurchaseFinished(IabResult result)
			{
				mCurrentInAppStatus = InAppPurchase.STATUS_PURCHASE_FINISHED;
				
				GoogleAnalyticsHelper.getInstance(MainListActivity.this).sendCurrentAppView(InformationTemplete.ANALYTICS_VIEW_PAID_ITEM);
				
				if(mCurrentPayingPosition == CODE_ALL_PAYMENT)
				{
					Log.f("All Payment Complete");
					GoogleAnalyticsHelper.getInstance(MainListActivity.this).sendCurrentAppView(InformationTemplete.ANALYTICS_ACTIVITY_MAIN+" : All Item "+ InformationTemplete.ANALYTICS_ACTION_PAID);
					mVideoBase.payAllItem(Common.ITEM_TYPE_PAID);
					mStorybookListRecyclerAdapter.notifyDataSetChanged();
					FileUtils.writeFile(mVideoBase, StorybookTempleteAPI.PATH_VIDEO_INFORMATION_ROOT+Common.FILE_NEW_VIDEO_INFORMATION);
					showTempleteAlertDialog(TempleteAlertDialog.DIALOG_EVENT_DOWNLOAD_ALL_ITEM,
							getResources().getString(R.string.btn_cancel),getResources().getString(R.string.btn_download),
							getResources().getString(R.string.product_download_all_item_message));
				}
				else
				{
					Log.f("One Payment Complete : "+ mInAppPurchase.getCurrentPaySku());
					GoogleAnalyticsHelper.getInstance(MainListActivity.this).sendCurrentAppView(InformationTemplete.ANALYTICS_ACTIVITY_MAIN+" : "+mInAppPurchase.getCurrentPaySku()+" Item "+ InformationTemplete.ANALYTICS_ACTION_PAID);
					mVideoBase.payPurchaseItem(mInAppPurchase.getCurrentPaySku());
					mStorybookListRecyclerAdapter.notifyDataSetChanged();
					FileUtils.writeFile(mVideoBase, StorybookTempleteAPI.PATH_VIDEO_INFORMATION_ROOT+Common.FILE_NEW_VIDEO_INFORMATION);
					showTempleteAlertDialog(TempleteAlertDialog.DIALOG_EVENT_DOWNLOAD_ONE_ITEM,
							getResources().getString(R.string.btn_cancel),getResources().getString(R.string.btn_download),
							getResources().getString(R.string.product_download_one_item_message));
					
				}
				
				requestPaidInformationSend(mInAppPurchase.getCurrentPaySku());
				
				Log.f("onIabPurchaseFinished success : " + result.isSuccess()+", getMessage : "+ result.getMessage());
				
				
			}
			
			@Override
			public void onConsumeFinished(IabResult result)
			{
				Log.i("onIabPurchaseFinished success : " + result.isSuccess()+", getMessage : "+ result.getMessage());
			}
			
			@Override
			public void inFailure(int status, String reason)
			{
				Log.f("status : "+ status +", reason : "+ reason);
				switch(status)
				{
				case 1:
					Toast.makeText(MainListActivity.this, reason, Toast.LENGTH_SHORT).show();
					break;
				}
			}
			
			@Override
			public void OnIabSetupFinished(IabResult result)
			{
				mCurrentInAppStatus = InAppPurchase.STATUS_SET_UP_FINISHED;
				Log.i("OnIabSetupFinished success : " + result.isSuccess()+", getMessage : "+ result.getMessage());
			}
		});
    }
    
    /**
     * 결제 취소한 아이템을 막기 위한 장치 : 결제 취소내역 복원
     */
    private void preventCancelItem()
    {
    	Log.f("");
    	mVideoBase.restoreCancelingPurchasedItem(mInAppPurchase.getOwnedPurchaseItemList());
    	mStorybookListRecyclerAdapter.notifyDataSetChanged();
    }
    
    private void initView()
    {
    	mDisplayMetrics = new DisplayMetrics();
    	getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
    	
        _IntroductionLayout = (LinearLayout)findViewById(R.id.base_introduction_layout);
        _MainRecyclerView 	= (RecyclerView)findViewById(R.id.main_listview);
        _RecommonadShowView = (ImageView)findViewById(R.id.recommand_show_image);
        
       _RecommonadShowView.setOnTouchListener(new OnTouchListener()
		{
			@Override
			public boolean onTouch(View v, MotionEvent event)
			{	
				Rect rect = new Rect();
				v.getGlobalVisibleRect(rect);

				if(rect.contains((int)event.getRawX(), (int)event.getRawY()) && event.getAction() == MotionEvent.ACTION_UP)
				{
					showRecommandAppListDialog();
					GoogleAnalyticsHelper.getInstance(MainListActivity.this).sendCurrentAppView(InformationTemplete.ANALYTICS_VIEW_RECOMMONAD_ICON_CLICK);
				}
				
				return true;
			}
		});
        
        
       _IntroductionLayout.setBackgroundColor(Color.parseColor(InformationTemplete.COLOR_INTRODUCTION_BACKGROUND));
        _MainRecyclerView.setBackgroundColor(Color.parseColor(InformationTemplete.COLOR_THUMBNAIL_BACKGROUND));
        

        _ScrollMoveButton 	= (ImageView)findViewById(R.id.move_scroll_button);
        _ScrollMoveButton.setOnClickListener(mOnClickListener);

        _MainRecyclerView = (RecyclerView)findViewById(R.id.main_listview);
        
        _RecommonadShowView.setBackgroundResource(R.drawable.recommand_frame_animation_icon);
        
        _MenubarLayout = (ScalableLayout)findViewById(R.id.drawer_menubar_layout);
        _TopBarCoverImage = (ImageView)findViewById(R.id.top_bg_cover_image);
        
        _TopBarCoverImage.setOnTouchListener(new OnTouchListener()
		{
			
			@Override
			public boolean onTouch(View v, MotionEvent event)
			{
				return true;
			}
		});
        _MenubarControlButton = (ImageView)findViewById(R.id.top_drawer_control_button);
        _MenubarControlButton.setOnClickListener(mOnClickListener);
        
        mRecommandAppIconDrawable = (AnimationDrawable)_RecommonadShowView.getBackground();
        mRecommandAppIconDrawable.start();
    }
    
    private void initRecyclerView()
    {
    	Log.i("");
    	String versionTitle = "Ver "+ CommonUtils.getInstance(this).getPackageVersionName()+"  "+mInitItemResult.device_id;
    	GridLayoutManager gridLayoutManager;

    	mStorybookListRecyclerAdapter = new StorybookListRecyclerAdapter(
    			this, 
    			versionTitle, 
    			InformationTemplete.COLOR_DEVICE_CODE_TEXT,
    			mVideoBase);
    	
    	mStorybookListRecyclerAdapter.setStorybookListItemListener(mStorybookListItemListener);
    	
    	if(CommonUtils.getInstance(this).isTablet())
    	{
    		gridLayoutManager = new GridLayoutManager(this, 4);
    	}
    	else
    	{
    		gridLayoutManager = new GridLayoutManager(this, 2);
    	}
    	
    	gridLayoutManager.setSpanSizeLookup(new SpanSizeLookup()
		{
			
			@Override
			public int getSpanSize(int position)
			{
				
				/**
				 * 테블릿은 4개의 썸네일이 보여야한다.
				 */
				if(position > 0 && position <= mVideoBase.getVideoInfoList().size())
				{
					return 1;
					
				}
				else
				{
					if(Feature.IS_TABLET_MODE)
					{
						return SPAN_COUNT_TABLET;
					}
					else
					{
						return SPAN_COUNT_PHONE;
					}
				}
				
				

			}
		});
    	
    	//mStorybookListRecyclerAdapter.setHasStableIds(true);
    	_MainRecyclerView.setLayoutManager(gridLayoutManager);
    	
    	if(Feature.IS_TABLET_MODE)
    	{
    		_MainRecyclerView.addItemDecoration(new GridSpacingItemDecoration(
    				SPAN_COUNT_TABLET, 
    				CommonUtils.getInstance(this).getPixel(45),
    				CommonUtils.getInstance(this).getPixel(30),
    				mVideoBase.getVideoInfoList().size()
    				));
    		
    	}
    	else
    	{
    		_MainRecyclerView.addItemDecoration(new GridSpacingItemDecoration(
    				SPAN_COUNT_PHONE, 
    				CommonUtils.getInstance(this).getPixel(64),
    				CommonUtils.getInstance(this).getPixel(46),
    				mVideoBase.getVideoInfoList().size()
    				));
    	}
    	ItemAnimator animator = _MainRecyclerView.getItemAnimator();
    	if (animator instanceof SimpleItemAnimator) {
    	  ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
    	}
    	
    	_MainRecyclerView.setAdapter(mStorybookListRecyclerAdapter);
    	
    	settingOnScrollListener();
    	
    }
    
    private void initDrawerLayout()
    {
        _DrawerLayout		= (DrawerLayout)findViewById(R.id.drawer_layout);
        _SlideMenuLayout	= (LinearLayout)findViewById(R.id.slide_menu_layout);
        _SlideMenuLayout.setOnTouchListener(new OnTouchListener()
		{
			@Override
			public boolean onTouch(View v, MotionEvent event)
			{
				if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE)
				{
					return true;
				}
				return false;
			}
		});

        _MenuWriterLayout				= (ScalableLayout)findViewById(R.id.menu_button_writer);
    	_MenuOtherAppLayout				= (ScalableLayout)findViewById(R.id.menu_button_other_app);            
    	_MenuInputPromotionLayout		= (ScalableLayout)findViewById(R.id.menu_button_input_promotion);      
    	_MenuInquireDeveloperLayout	= (ScalableLayout)findViewById(R.id.menu_button_developer_inquire);    
    	_MenuRestorePurchaseLayout 	= (ScalableLayout)findViewById(R.id.menu_button_restore);
    	_MenuBackgroundMusicLayout	= (ScalableLayout)findViewById(R.id.menu_button_music);
    	_MenuActionStoryModeLayout	= (ScalableLayout)findViewById(R.id.menu_button_action_mode);
    	
    	_MenuWriterLayout.setOnClickListener(mMenuClickListener);
    	_MenuOtherAppLayout.setOnClickListener(mMenuClickListener);
    	_MenuInputPromotionLayout.setOnClickListener(mMenuClickListener);
    	_MenuInquireDeveloperLayout.setOnClickListener(mMenuClickListener);
    	_MenuRestorePurchaseLayout.setOnClickListener(mMenuClickListener);
    	_MenuBackgroundMusicLayout.setOnClickListener(mMenuClickListener);
    	_MenuActionStoryModeLayout.setOnClickListener(mMenuClickListener);
    	
    	_MenuBackgroundMusicModeIcon  	= (ImageView)findViewById(R.id.menu_icon_music);
    	_MenuActionModeIcon				= (ImageView)findViewById(R.id.menu_icon_action_mode);
    	
    	_DrawerLayout.closeDrawer(_SlideMenuLayout);
    	_DrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    	
    	if(InformationTemplete.AVAILABLE_ACTION_STORY_MODE == false)
    	{
    		_MenuActionStoryModeLayout.setVisibility(View.INVISIBLE);
    	}
    }
    
    private void InitFont()
    {
    	mFont = Font.getInstance(this.getBaseContext());
    	
    	((TextView)findViewById(R.id.text_introduction)).setTypeface(mFont.getRobotoMedium());
    	((TextView)findViewById(R.id.menu_text_writer)).setTypeface(mFont.getRobotoMedium());
    	((TextView)findViewById(R.id.menu_text_input_promotion)).setTypeface(mFont.getRobotoMedium());
    	((TextView)findViewById(R.id.menu_text_developer_inquire)).setTypeface(mFont.getRobotoMedium());
    	((TextView)findViewById(R.id.menu_text_restore)).setTypeface(mFont.getRobotoMedium());
    	((TextView)findViewById(R.id.menu_text_other_app)).setTypeface(mFont.getRobotoMedium());
    	((TextView)findViewById(R.id.menu_text_music)).setTypeface(mFont.getRobotoMedium());
    	((TextView)findViewById(R.id.menu_text_action_mode)).setTypeface(mFont.getRobotoMedium());
    }
    
    int scrollY = 0;
    private void settingOnScrollListener()
    {
    	_MainRecyclerView.addOnScrollListener(new OnScrollListener()
		{

			@Override
			public void onScrollStateChanged(RecyclerView recyclerView, int newState)
			{
				super.onScrollStateChanged(recyclerView, newState);
			}

			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy)
			{
				super.onScrolled(recyclerView, dx, dy);
				scrollY += dy;
				if(_MenubarControlButton.getHeight() < scrollY)
				{
					setStatusIntroductionMoveButton(false);
				}
				else
				{
					setStatusIntroductionMoveButton(true);
				}
				
				
				if(_MenubarLayout.getHeight() < scrollY)
				{
					setStatusScrollMoveButton(true);
				}
				else
				{
					setStatusScrollMoveButton(false);
				}
			}
    		
		});
    }

    
    private void startBGMPlayer()
    {
    	String[] bgmList = null;
    	
    	if(mBGMPlayer == null)
    	{
    		try
    		{
        		AssetManager assetManager = getAssets();
        		bgmList = assetManager.list("mp3");
            	if(mCurrentBgmPlayIndex == -1)
            	{
            		
            		mCurrentBgmPlayIndex = new Random().nextInt(bgmList.length);
            		
            	}
    			AssetFileDescriptor descriptor = getAssets().openFd("mp3"+File.separator+bgmList[mCurrentBgmPlayIndex]);
    			mBGMPlayer = new MediaPlayer();
    			mBGMPlayer.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
    			mBGMPlayer.prepare();
    			mBGMPlayer.setVolume(1.0f, 1.0f);
    			mBGMPlayer.setLooping(true);
    			mBGMPlayer.start();
    		}
    		catch (IOException e)
    		{
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
    	}
    	else
    	{
    		mBGMPlayer.start();
    	}
    	
    }
    
    private void stopBGMPlayer()
    {
    	if(mBGMPlayer != null)
    	{
    		mBGMPlayer.pause();
    	}
    }
    
    private void releaseBgmPlayer()
    {
    	if(mBGMPlayer != null)
    	{
    		mBGMPlayer.pause();
    		mBGMPlayer.stop();
    		mBGMPlayer.release();
    		mBGMPlayer = null;
    	}
    }
    
    
    private void downloadStart()
    {
	//	View viewbase = _MainRecyclerView.getChildAt(mCurrentDownloadPosition+1);
	//	Log.i("Base View ID : "+viewbase.getId());
	//	mCurrentProgressView = (CircleProgressView)viewbase.findViewById(R.id.thumbnail_progress_image);
    	//현재 리스트에는 상단 탑바가 포함된 List 임으로 Position 값을 +1을 해야한다.
		startDownload(true);
    }
    
    private void setStatusScrollMoveButton(boolean isVisible)
    {
    	if(mMainHandler.hasMessages(MESSAGE_SHOW_SCROLL_MOVE_BUTTON) || mMainHandler.hasMessages(MESSAGE_HIDE_SCROLL_MOVE_BUTTON))
    	{
    		return;
    	}
    	if(isVisible && _ScrollMoveButton.getVisibility() == View.GONE)
    	{
    		mMainHandler.sendEmptyMessage(MESSAGE_SHOW_SCROLL_MOVE_BUTTON);
    	}
    	else if(!isVisible && _ScrollMoveButton.getVisibility() == View.VISIBLE)
    	{
    		mMainHandler.sendEmptyMessage(MESSAGE_HIDE_SCROLL_MOVE_BUTTON);
    	}
    }
    
    private void setStatusIntroductionMoveButton(boolean isVisible)
    {
    	if(mMainHandler.hasMessages(MESSAGE_SHOW_SCROLL_INTRODUCTION_MENU_BUTTON) || mMainHandler.hasMessages(MESSAGE_HIDE_SCROLL_INTRODUCTION_MENU_BUTTON))
    	{
    		return;
    	}
    	if(isVisible && _MenubarControlButton.getVisibility() == View.GONE)
    	{
    		mMainHandler.sendEmptyMessage(MESSAGE_SHOW_SCROLL_INTRODUCTION_MENU_BUTTON);
    	}
    	else if(!isVisible && _MenubarControlButton.getVisibility() == View.VISIBLE)
    	{
    		mMainHandler.sendEmptyMessage(MESSAGE_HIDE_SCROLL_INTRODUCTION_MENU_BUTTON);
    	}
    }
    
    @Override
    public void onBackPressed()
    {
    	 if(_DrawerLayout.isDrawerOpen(_SlideMenuLayout))
         {
    		 _DrawerLayout.closeDrawer(_SlideMenuLayout);
             return;
         }
    	 
    	 showTempleteAlertDialog(TempleteAlertDialog.DIALOG_EVENT_END_APP, TempleteAlertDialog.DEFAULT_BUTTON_TYPE_2, getResources().getString(R.string.meesage_is_end));
    	 
    	 
    	
    }
    
    @Override
	protected void onDestroy()
	{
    	Log.f("");
		super.onDestroy();
		
		if(mContinueDownloadAsync != null)
		{
			mContinueDownloadAsync.setCancel(true);
			mContinueDownloadAsync.cancel(true);
		}
		
		mMainHandler.removeCallbacksAndMessages(null);
		
		if(FileUtils.checkFile(StorybookTempleteAPI.PATH_VIDEO_INFORMATION_ROOT+Common.FILE_NEW_VIDEO_INFORMATION) == true)
    	{
			FileUtils.writeFile(mVideoBase, StorybookTempleteAPI.PATH_APP_ROOT+Common.FILE_NEW_VIDEO_INFORMATION);
    	}
		

		/**
		 * 앱종료시에 캡션에 대한 정보를 삭제한다.
		 */
		CommonUtils.getInstance(this).setSharedPreference(Common.PARAMS_CAPTION_STATUS, false);
		
		if(mRecommandAppIconDrawable != null)
		{
			mRecommandAppIconDrawable.stop();
		}
		
		
	}
    
    /**
     * IAC가 있을 경우 시작시 화면에 표시
     * @return TRUE : IAC 정보가 있음</p>FALSE : IAC 정보가 없거나 보여주지않아도 되는 상황
     */
    private boolean isVisibleIAC()
    {
    	try
    	{
    		if(mInitItemResult != null)
        	{
            	if(mIACInformation.isSleepItem(mInitItemResult.inapp.inapp_code) == false && mInitItemResult.inapp.inapp_code.equals("") == false)
            	{
            		return true;
            	}
        	}
    	}catch(NullPointerException e)
    	{
    		return false;
    	}
    	
    	
    	return false;
    }
    
    private void showIACTempleteAlertDialog()
    {
    	TempleteAlertDialog dialog = new TempleteAlertDialog(this, mInitItemResult.inapp.iac_title, mInitItemResult.inapp.iac_content);
    	dialog.setDialogMessageSubType(FlexibleDialog.DIALOG_EVENT_IAC);
    	dialog.setIconResource(R.drawable.ic_launcher);
    	if(mInitItemResult.inapp.btn2_useyn.equals("N"))
    	{
    		dialog.setButtonText(mInitItemResult.inapp.btn1_text);
    	}
    	else
    	{
    		dialog.setButtonText(mInitItemResult.inapp.btn1_text, mInitItemResult.inapp.btn2_text);
    	}
    	dialog.setDialogListener(mDialogListener);
    	dialog.show();
    }
    
    private void showTestIACDialog()
    {
		String test ="지금 카카오스토리에서 로켓걸 스토리북 앱 출시 기념 이벤트가 진행중입니다~ 로켓걸의 모든스토리를 무료로 다운로드 받을 수 있는 기회를 놓치지 마세요!";
    	TempleteAlertDialog dialog = new TempleteAlertDialog(this, "카카오 스토리 출시", test);
    	dialog.setDialogMessageSubType(FlexibleDialog.DIALOG_EVENT_IAC);
    	dialog.setIconResource(R.drawable.ic_launcher);
    	dialog.setButtonText("이동", "취소");
    	dialog.setDialogListener(mDialogListener);
    	dialog.show();
    }

	/**
	 * 프로모션 코드 정보를 위한 통신을 한다.
	 * @param promoCode
	 */
	private void requestPromotionInformation(String promoCode)
	{
		PromotionInformationAsync mPromotionInformationAsync = new PromotionInformationAsync(this, promoCode, mAsyncListener);
		mPromotionInformationAsync.execute();
		
		mCurrentMessage = ASYNC_MESSAGE_PROMOTION_QUERY;
	}
	
	private void requestPromotionRestore()
	{
		PromotionRestoreAsync mPromotionRestoreAsync = new PromotionRestoreAsync(this, mAsyncListener);
		mPromotionRestoreAsync.execute();
		mCurrentMessage = ASYNC_MESSAGE_PROMOTION_RESTORE;
	}
	
	private void requestPaidInformationSend(String paidItemCode)
	{
		Log.f("구입한 아이템 서버에 전달 : "+ paidItemCode);
		PaidInformationSendAsync mPaidInformationSendAsync = new PaidInformationSendAsync(this, paidItemCode);
		mPaidInformationSendAsync.execute();
	}
    
	/**
	 * 서버 통신 후에 정보를 메인에서 세팅한다.
	 * @param object 서버에서 받은 Object 정보
	 */
    private void setRunningEndInformation(Object object)
    {
    	Log.f("mCurrentMessage : "+mCurrentMessage);
    	switch(mCurrentMessage)
    	{
    	case ASYNC_MESSAGE_PROMOTION_QUERY:
    		hideIndeterminateDialog();
    		mPromotionBaseResult = (PromotionBaseResult) object;
    		checkPromotionResult();
    		break;
    	case ASYNC_MESSAGE_PROMOTION_RESTORE:
    		hideIndeterminateDialog();
    		mPromotionRestoreResult = (PromotionRestoreResult)object;
    		restore();
    		break;
    	}
    }
    
    
    
    private void inquireForDeveloper() {
    	Intent i;
    	i = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto",
				Common.DEVELOPER_EMAIL, null));
		
		i.putExtra(Intent.EXTRA_SUBJECT, "");
		
		i.putExtra(Intent.EXTRA_TEXT, "["+Build.BRAND.toString()+"]"+" Model: "+Build.MODEL+", OS: "+Build.VERSION.RELEASE+", Ver: "+ CommonUtils.getInstance(this).getPackageVersionName());
		String strTitle = getResources()
				.getString(R.string.app_name);
		Uri uri = Uri.parse("file://"+ Log.getLogfilePath());
		i.putExtra(Intent.EXTRA_STREAM, uri);
		startActivity(Intent.createChooser(i, strTitle));
	}
    
    /**
     * 프로모션 코드로 적용되었거나, 이전에 결제 된 내용을 복구한다.
     */
    private void restore()
    {
    	Log.f("");
    	List<String> restorePromotionList = new ArrayList<String>();    	
    	try
    	{
    		if(mPromotionRestoreResult.list.size() == 0 && mInAppPurchase.getOwnedPurchaseItemList() == null)
        	{
    			Log.f("Restore Fail");
        		showTempleteAlertDialog(TempleteAlertDialog.MODE_TEXT, TempleteAlertDialog.DEFAULT_BUTTON_TYPE_1, getResources().getString(R.string.paid_restore_fail));
        	}
        	else
        	{
            	for(int i = 0; i < mPromotionRestoreResult.list.size(); i++)
            	{
            		restorePromotionList.add(mPromotionRestoreResult.list.get(i).iap);
            	}
            	mVideoBase.payPurchaseItem(restorePromotionList, Common.ITEM_TYPE_PROMOTION);
            	mVideoBase.payPurchaseItem(mInAppPurchase.getOwnedPurchaseItemList(), Common.ITEM_TYPE_PAID);
            	
            	mStorybookListRecyclerAdapter.notifyDataSetChanged();
        		showTempleteAlertDialog(TempleteAlertDialog.MODE_TEXT, FlexibleDialog.DEFAULT_BUTTON_TYPE_1, getResources().getString(R.string.paid_restore_success));

        	}
    		
    	}catch(Exception e)
    	{
    		showTempleteAlertDialog(TempleteAlertDialog.MODE_TEXT, FlexibleDialog.DEFAULT_BUTTON_TYPE_1, getResources().getString(R.string.paid_restore_fail));
    	}
    	

    }
    
    private void checkPromotionResult()
    {
    	switch(Integer.valueOf(mPromotionBaseResult.getCodestate()))
    	{
    	case Common.PROMOTION_STATE_NORMAL:
    		if(Integer.valueOf(mPromotionBaseResult.getPromotype()) == Common.PROMOTION_PRODUCT_TYPE_ALL)
    		{
    			Log.f("PROMOTION_STATE_NORMAL");
    			setDownloadAvailableFromPromotionCode(Common.PROMOTION_PRODUCT_TYPE_ALL, null);
    		}
    		else if(Integer.valueOf(mPromotionBaseResult.getPromotype()) == Common.PROMOTION_PRODUCT_TYPE_FIX)
    		{
    			Log.f("PROMOTION_PRODUCT_TYPE_FIX Size : "+mPromotionBaseResult.list.size());
    			ArrayList<String> sendList = new ArrayList<String>();
    			for(int i = 0; i < mPromotionBaseResult.list.size(); i++)
    			{
    				Log.f("Promotion Free Code Item : "+ mPromotionBaseResult.list.get(i).iap);
    				sendList.add(mPromotionBaseResult.list.get(i).iap);
    			}
    			setDownloadAvailableFromPromotionCode(Common.PROMOTION_PRODUCT_TYPE_FIX, sendList);
    		}else if(Integer.valueOf(mPromotionBaseResult.getPromotype()) == Common.PROMOTION_PRODUCT_TYPE_FLUID)
    		{
    			Log.f("PROMOTION_PRODUCT_TYPE_FLUID");
    			showPromotionListDialog();
    		}

    		break;
    	case Common.PROMOTION_STATE_END_TIME:
    		Log.f("Promotion Code not valid.");
    		showTempleteAlertDialog(TempleteAlertDialog.DIALOG_EVENT_DEFAULT , TempleteAlertDialog.DEFAULT_BUTTON_TYPE_1, 
    				getResources().getString(R.string.promotion_not_valid));
    		break;
    	case Common.PROMOTION_STATE_USE_BEFORE:
    		Log.f("Promotion Code already used by someone.");
    		showTempleteAlertDialog(TempleteAlertDialog.DIALOG_EVENT_DEFAULT , TempleteAlertDialog.DEFAULT_BUTTON_TYPE_1, 
    				getResources().getString(R.string.promotion_already_used_error));
    		break;
    	case Common.PROMOTION_STATE_CODE_ERROR:
    		Log.f("Promotion Code Error");
    		showTempleteAlertDialog(TempleteAlertDialog.DIALOG_EVENT_DEFAULT , TempleteAlertDialog.DEFAULT_BUTTON_TYPE_1, 
    				getResources().getString(R.string.promotion_code_error));
    		break;
    	}
    }
    
    /**
     * 프로모션 코드에서 받은 정보를 파일에 갱신하고 다운로드 가능 하게 하는 메소드
     * @param type 각각의 프로모션 정보 타입
     * @param downloadIapList 다운로드 가능하게 할 리스트 
     */
    private void setDownloadAvailableFromPromotionCode(int type, List<String> downloadIapList)
    {
    	switch(type)
    	{
    	case Common.PROMOTION_PRODUCT_TYPE_ALL:
    		mVideoBase.payAllItem(Common.ITEM_TYPE_PROMOTION);
    		sendPromotionCodeUse(mPromotionInputCode,null);
    		break;
    	case Common.PROMOTION_PRODUCT_TYPE_FIX:
        	Log.f("type : "+type+", downloadIapSize : "+downloadIapList.size());
    		mVideoBase.payPurchaseItem(downloadIapList, Common.ITEM_TYPE_PROMOTION);
    		sendPromotionCodeUse(mPromotionInputCode,null);
    	case Common.PROMOTION_PRODUCT_TYPE_FLUID:
        	Log.f("type : "+type+", downloadIapSize : "+downloadIapList.size());
    		mVideoBase.payPurchaseItem(downloadIapList, Common.ITEM_TYPE_PROMOTION);
        	sendPromotionCodeUse(mPromotionInputCode,downloadIapList.get(0));
        	int position = mVideoBase.getVideoInfoPosition(downloadIapList.get(0));
        	if(position != -1)
        	{
        		Message msg = Message.obtain();
        		msg.what = MESSAGE_FORCE_SCROLL;
        		msg.arg1 = position;
        		mMainHandler.sendMessageDelayed(msg, 500);
        	}
    		break;
    	}
    	Toast.makeText(this, getResources().getString(R.string.promotion_used_go_download), Toast.LENGTH_SHORT).show();
    	mStorybookListRecyclerAdapter.notifyDataSetChanged();
    	FileUtils.writeFile(mVideoBase, StorybookTempleteAPI.PATH_VIDEO_INFORMATION_ROOT+Common.FILE_NEW_VIDEO_INFORMATION);
    	
    	
    }
    
    private void sendPromotionCodeUse(String promtionCode , String useProductCode)
    {
    	Log.f("promtionCode : "+promtionCode+", useProductCode : "+useProductCode);
    	if(mPromotionCodeUseAsync != null)
    	{
    		mPromotionCodeUseAsync.cancel(true);
    		mPromotionCodeUseAsync = null;
    	}
    	if(useProductCode == null)
    	{
    		mPromotionCodeUseAsync = new PromotionCodeUseAsync(this,promtionCode);
    	}
    	else
    	{
    		mPromotionCodeUseAsync = new PromotionCodeUseAsync(this,promtionCode,useProductCode);
    	}
    	
    	mPromotionCodeUseAsync.execute();
    }
    
    /**
     * GridView를 화면에 보여준다.
     */
    private void showMainListView()
    {
    	//TODO : 스크롤이 잘될수 있게 초기 스크롤 세팅
    }
    
    
    
    /**
     * Introduction 의 캐릭터를 화면에 추가
     */
    private void makeIntroductionPeopleItem()
    {
    	((TextView)findViewById(R.id.text_introduction)).setTextColor(Color.parseColor(InformationTemplete.COLOR_INTRODUCTION_TEXT));

    	LinearLayout addLayout = (LinearLayout)findViewById(R.id.add_introduction_laytout);
    	LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    	String[] peopleNameArray = getResources().getStringArray(R.array.peopleArray);
    	TypedArray idArray	= getResources().obtainTypedArray(R.array.idArray);
    	LinearLayout.LayoutParams params;
    	
    	if(Feature.IS_TABLET_MODE == true)
    	{
    		for(int i = 0; i < InformationTemplete.MAX_INTRODUCTION_CHARACTER_SIZE; i++)
        	{
        		View base = inflater.inflate(R.layout.introduction_people_item_tablet, null);
        		ImageView image = (ImageView)base.findViewById(R.id.introduction_item_image);
        		TextView text	= (TextView)base.findViewById(R.id.introduction_item_text);
        		text.setTextColor(Color.parseColor(InformationTemplete.COLOR_INTRODUCTION_TEXT));
        		text.setTypeface(mFont.getRobotoMedium());
        		image.setImageDrawable(getResources().getDrawable(idArray.getResourceId(i, -1)));
        		text.setText(peopleNameArray[i]);
        		params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        		params.topMargin = CommonUtils.getInstance(this).getPixel(74);
        		if(i == 0)
        		{
        			params.leftMargin = CommonUtils.getInstance(this).getPixel(56);
        		}
        		else if( i == InformationTemplete.MAX_INTRODUCTION_CHARACTER_SIZE - 1)
        		{
        			params.leftMargin = CommonUtils.getInstance(this).getPixel(20);
        			params.rightMargin = CommonUtils.getInstance(this).getPixel(56);
        		}
        		else
        		{
        			params.leftMargin = CommonUtils.getInstance(this).getPixel(20);
        		}
        		
        		base.setLayoutParams(params);
        		
        		addLayout.addView(base);
        	}
    	}
    	else
    	{
    		for(int i = 0; i < InformationTemplete.MAX_INTRODUCTION_CHARACTER_SIZE; i++)
        	{
        		View base = inflater.inflate(R.layout.introduction_people_item, null);
        		ImageView image = (ImageView)base.findViewById(R.id.introduction_item_image);
        		TextView text	= (TextView)base.findViewById(R.id.introduction_item_text);
        		text.setTextColor(Color.parseColor(InformationTemplete.COLOR_INTRODUCTION_TEXT));
        		text.setTypeface(mFont.getRobotoMedium());
        		image.setImageDrawable(getResources().getDrawable(idArray.getResourceId(i, -1)));
        		text.setText(peopleNameArray[i]);
        		params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        		
        		if(i == 0)
        		{
        			params.leftMargin = CommonUtils.getInstance(this).getPixel(50);
        		}
        		else if( i == InformationTemplete.MAX_INTRODUCTION_CHARACTER_SIZE - 1)
        		{
        			params.leftMargin = CommonUtils.getInstance(this).getPixel(70);
        			params.rightMargin = CommonUtils.getInstance(this).getPixel(50);
        		}
        		else
        		{
        			params.leftMargin = CommonUtils.getInstance(this).getPixel(70);
        		}
        		
        		base.setLayoutParams(params);
        		
        		addLayout.addView(base);
        	}
    	}

    } 
    
    private void startDownload(boolean isStart)
    {
    	Log.f("Start Download File : "+isStart);
    	Log.f("download Video Url : "+ mVideoBase.getVideoInfoList().get(mCurrentDownloadPosition).getVideoUrl());
    	Log.f("download Caption Url : " +mVideoBase.getVideoInfoList().get(mCurrentDownloadPosition).getCaptionUrl());
    	if(isStart == true)
    	{
    		if(mContinueDownloadAsync != null)
    		{
    			mContinueDownloadAsync.setCancel(true);
    			mContinueDownloadAsync.cancel(true);
    		}
    		
    		mContinueDownloadAsync = new ContinueDownloadAsync(MainListActivity.this,mVideoBase.getVideoInfoList().get(mCurrentDownloadPosition).getDownloadVideoUrl(), 
    				mVideoBase.getVideoInfoList().get(mCurrentDownloadPosition).getDownloadCaptionUrl(),
    				mVideoBase.getVideoInfoList().get(mCurrentDownloadPosition).getVideoUrl(),mVideoBase.getVideoInfoList().get(mCurrentDownloadPosition).getCaptionUrl(), mDownloadListner);
		mContinueDownloadAsync.execute();

    		
    	}
    	else
    	{
    		if(mContinueDownloadAsync != null)
    		{
    			mContinueDownloadAsync.setCancel(true);
    			mContinueDownloadAsync.cancel(true);
    		}
    	}

    }
    
    private void startMoviePlayer(int position)
    {
    	Intent intent = new Intent(this, Mp4PlayerActivity.class);
    	intent.putExtra("currentPlayIndex", position);
    	startActivityForResult(intent, Common.INTENT_RESULT_PLAY_EXIT);
    	
    	File file = new File(StorybookTempleteAPI.PATH_MP4+mVideoBase.getVideoInfoList().get(position).getVideoUrl());
    	Log.f("file Path : "+ file.getAbsolutePath());
    	if(file.exists() == true)
    	{
    		Log.f("File Download Complete");
    	}
    	else
    	{
    		Log.f("File Download Not Complete");
    	}
    }
    
    /**
     * 다운로드가 가능할 수 있는 데이터 저장소 인지 확인
     * @return
     */
    private boolean isDownloadAvailable()
    {
    	if(CommonUtils.getInstance(this).getAvailableStorageSize() > Common.AVAILABLE_DOWNLOAD_STORAGE_SIZE)
    	{
    		Log.f("Download Available Storage Size ");
    		return true;
    	}
    	else
    	{
    		Log.f("Storage Size limit Error. Download impossible");
    		return false;
    	}
    }
    
    private void showPromotionDialog()
    {
    	Log.f("");
    	if(mPromotionDialog != null)
    	{
    		mPromotionDialog.dismiss();
    		mPromotionDialog = null;
    	}
    	
		mPromotionDialog = new PromotionDialog(this, mDialogListener);
		mPromotionDialog.show();
    }
    
    /**
     * 웹에서 앱을 호출하여 실행시 웹에서 받아온 쿠폰을 화면에 보여주기위해 사용
     */
    private void showPromotionDialogToWeb()
    {
    	Log.f("");
    	if(mPromotionDialog != null)
    	{
    		mPromotionDialog.dismiss();
    		mPromotionDialog = null;
    	}
    	
		mPromotionDialog = new PromotionDialog(this, mDialogListener);
		mPromotionDialog.setPromotionInfo(Common.COUPON_TEXT_TO_WEB);
		mPromotionDialog.show();
    }
    
    private void showPromotionListDialog()
    {
    	Log.f("");
    	if(mPromotionListDialog != null)
    	{
    		mPromotionListDialog.dismiss();
    		mPromotionListDialog = null;
    	}
    	
    	for(int i = 0; i < mVideoBase.getVideoInfoList().size(); i++)
    	{
    		if(mVideoBase.isPurchaseItem(i) == true)
    		{
    			for(int j = 0 ; j < mPromotionBaseResult.list.size(); j++)
    			{
    				if(mVideoBase.getVideoInfoList().get(i).getPurchaseCode().equals(mPromotionBaseResult.list.get(j).iap))
    				{
    					mPromotionBaseResult.list.get(j).isAlreadyHave = true;
    					break;
    				}
    			}
    			
    		}
    	}
    	
    	mPromotionListDialog = new PromotionListDialog(this, mPromotionBaseResult.list, mDialogListener);
    	mPromotionListDialog.show();
    }
    
    private void showCreditDialog()
    {
    	Log.f("");
    	if(mCreditDialog != null)
    	{
    		mCreditDialog.dismiss();
    		mCreditDialog = null;
    	}
    	
    	if(Feature.IS_TABLET_MODE == true)
    	{
    		mCreditDialog = new CreditDialog(this, R.style.BackgroundDialog);
    	}
    	else
    	{
    		mCreditDialog = new CreditDialog(this, R.style.BackgroundDialog_Super);
    	}
    	mCreditDialog.show();
    }
    
    private void showPaymentDialog(String title)
    {
    	
    	if(mInAppPurchase.getInventory().getSkuDetails(InformationTemplete.ONE_PAY_ITEM_SKU) == null ||
    			mInAppPurchase.getInventory().getSkuDetails(InformationTemplete.ALL_PAY_ITEM_SKU) == null)
    	{
    		Log.f("인앱 결제 시스템이 아직 연결 되지 않음.");
    		return;
    		
    	}
    	if(mPaymentDialog != null)
    	{
    		mPaymentDialog.dismiss();
    		mPaymentDialog = null;
    	}
    	
    	mPaymentDialog = new PaymentDialog(this);
    	mPaymentDialog.initBGImage(getPaymentBackgroundByLocale());
    	mPaymentDialog.initPriceTitle(getPaymentTitleByLocale());
    	mPaymentDialog.setItemText(title, mDialogListener);
    	mPaymentDialog.setAppPrice(mInAppPurchase.getInventory().getSkuDetails(InformationTemplete.ONE_PAY_ITEM_SKU).getPrice(), 
    			mInAppPurchase.getInventory().getSkuDetails(InformationTemplete.ALL_PAY_ITEM_SKU).getPrice());
    	mPaymentDialog.show();
    }
    
    private Drawable getPaymentBackgroundByLocale()
    {
		if(Locale.getDefault().toString().equals(Locale.KOREA.toString()))
		{
			return getResources().getDrawable(R.drawable.popup_payment_kr);
		}
		else if(Locale.getDefault().toString().equals(Locale.JAPAN.toString()))
		{
			return getResources().getDrawable(R.drawable.popup_payment_jp);
		}
		else if(Locale.getDefault().toString().equals(Locale.SIMPLIFIED_CHINESE.toString()))
		{
			return getResources().getDrawable(R.drawable.popup_payment_cn);
		}
		else if(Locale.getDefault().toString().equals(Locale.TRADITIONAL_CHINESE.toString()))
		{
			return getResources().getDrawable(R.drawable.popup_payment_tw);
		}
		else
		{
			if (InformationTemplete.DISTINGUISH_STORY_WORD == Common.STORY_TITLE_EPISODE)
			{
				return getResources().getDrawable(R.drawable.popup_payment_en_episode);
			}
			else
			{
				return getResources().getDrawable(R.drawable.popup_payment_en_chapter);
			}
			
		}
    }
    
    private String getPaymentTitleByLocale()
    {
    	if (Locale.getDefault().toString().equals(Locale.KOREA.toString()))
		{
			return "총 " + InformationTemplete.MAX_AVAILABLE_FOR_PULCHASE_STORY_SIZE + "편";
		} else if (Locale.getDefault().toString().equals(Locale.JAPAN.toString()))
		{
			return "全 " + InformationTemplete.MAX_AVAILABLE_FOR_PULCHASE_STORY_SIZE + "話";
		} else if (Locale.getDefault().toString().equals(Locale.SIMPLIFIED_CHINESE.toString()))
		{
			return "共 " + InformationTemplete.MAX_AVAILABLE_FOR_PULCHASE_STORY_SIZE + "篇";
		} else if (Locale.getDefault().toString().equals(Locale.TRADITIONAL_CHINESE.toString()))
		{
			return "共 " + InformationTemplete.MAX_AVAILABLE_FOR_PULCHASE_STORY_SIZE + "篇";
		} else
		{
			if (InformationTemplete.DISTINGUISH_STORY_WORD == Common.STORY_TITLE_EPISODE)
			{
				return InformationTemplete.MAX_AVAILABLE_FOR_PULCHASE_STORY_SIZE + " Episode";
			} 
			else 
			{
				return InformationTemplete.MAX_AVAILABLE_FOR_PULCHASE_STORY_SIZE + " Chapter";
			}
		}
    }
    
    private void showRecommandAppListDialog()
    {
    	if(mRecommandAppInformationDialog != null)
    	{
    		mRecommandAppInformationDialog.dismiss();
    		mRecommandAppInformationDialog = null;
    	}
    	
    	mRecommandAppInformationDialog = new RecommandAppScrollDialog(this, mInitItemResult.banner_list);
    	mRecommandAppInformationDialog.show();
    }
  
    @Override
	protected void onStop()
	{
		super.onStop();

		FileUtils.writeFile(mVideoBase, StorybookTempleteAPI.PATH_VIDEO_INFORMATION_ROOT+Common.FILE_NEW_VIDEO_INFORMATION);
	}
    
    @Override
    public void onResume() {
    	Log.f("");
        super.onResume();
        
        
        
        mNetworkConnectReceiver.register(this);
        
        if(Common.IS_BACKGROUND_SOUND_ENABLE)
        {
        	 mMainHandler.sendEmptyMessageDelayed(MESSAGE_BGM, DURATION_BGM_START);
        }
       
    }
    
    @Override
    protected void onPause()
    {
    	Log.f("");
    	super.onPause();
    	mNetworkConnectReceiver.unregister(this);
    	if(mMainHandler.hasMessages(MESSAGE_BGM))
    	{
    		mMainHandler.removeMessages(MESSAGE_BGM);
    	}
    	releaseBgmPlayer();
    }
    
    

    @Override
	protected void onUserLeaveHint()
	{
    	Log.f("");
		super.onUserLeaveHint();
	}


    
    private void showTempleteAlertDialog(int type, int buttonType, String message)
    {
    	TempleteAlertDialog dialog = new TempleteAlertDialog(this, message);
    	dialog.setDialogMessageSubType(type);
    	dialog.setButtonText(buttonType);
    	dialog.setDialogListener(mDialogListener);
    	dialog.show();
    }
    
    private void showTempleteAlertDialog(int type, int buttonType, String message, boolean isCancelable)
    {
    	TempleteAlertDialog dialog = new TempleteAlertDialog(this, message);
    	dialog.setDialogMessageSubType(type);
    	dialog.setButtonText(buttonType);
    	dialog.setCancelable(isCancelable);
    	dialog.setDialogListener(mDialogListener);
    	dialog.show();
    }   
    
    private void showTempleteAlertDialog(int type,String firstButton, String secondButton, String message)
    {
    	TempleteAlertDialog dialog = new TempleteAlertDialog(this, message);
    	dialog.setDialogMessageSubType(type);
    	dialog.setButtonText(firstButton, secondButton);
    	dialog.setDialogListener(mDialogListener);
    	dialog.show();
    }
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    
	    
	    Log.f("onActivityResult(" + requestCode + "," + resultCode + "," + data);
	    
	    /**
	     * 이부분 선언으로 In App 에 관한 리스너 정보를 받을 수 있다.
	     */
	    if(mInAppPurchase.getInAppHelper().handleActivityResult(requestCode, resultCode, data) == false)
	    {
	    	super.onActivityResult(requestCode, resultCode, data);
	 	    
	 	    if(resultCode == RESULT_OK)
	 	    {
	 	    	switch (requestCode)
	 			{

	 				case Common.INTENT_RESULT_PLAY_EXIT:
	 					
	 					if(data != null)
	 					{
	 						ArrayList<Integer> mPlayCompleteList 	= data.getIntegerArrayListExtra(Common.INTENT_PARAMS_PLAY_COMPLETE_POSITION);
	 						ArrayList<Integer> mPlayedList			= data.getIntegerArrayListExtra(Common.INTENT_PARAMS_PLAYED_POSITION);
	 						
	 						for(int i = 0; i < mPlayedList.size(); i++)
	 						{
	 							mVideoBase.setPlayedItem(mPlayedList.get(i));
	 						}
	 						
	 						for(int i = 0 ; i < mPlayCompleteList.size(); i++)
	 						{
	 							mVideoBase.CompletePlayItem(mPlayCompleteList.get(i));
	 						}
	 						mStorybookListRecyclerAdapter.notifyDataSetChanged();
	 						
	 						mMainHandler.sendEmptyMessageDelayed(MESSAGE_APPRAISAL_SHOW, DURATION_APPEAR * 2);
	 						
	 						FileUtils.writeFile(mVideoBase, StorybookTempleteAPI.PATH_VIDEO_INFORMATION_ROOT+Common.FILE_NEW_VIDEO_INFORMATION);
	 					}

	 				break;

	 			}
	 	    }

	    } 
	}
	
	/**
	 * 모드가 On 이면 On Image 를 리턴 , Off 면 Off Image를 리턴
	 * @param isOn
	 * @return
	 */
	private int getModeImage(boolean isOn)
	{
		if(isOn == true)
		{
			return R.drawable.btn_on;
		}
		else
		{
			return R.drawable.btn_off;
		}
	}

	private void showAppraisalDialog()
	{
		if(mAppraisalDialog != null)
		{
			mAppraisalDialog.dismiss();
			mAppraisalDialog = null;
		}
		
		mAppraisalDialog = new AppraisalDialog(this);
		mAppraisalDialog.setDialogListener(mDialogListener);
		mAppraisalDialog.setCancelable(false);
		mAppraisalDialog.show();
	}
	

	private void showIndeterminateDialog()
	{
		if(mProgressDialog != null)
		{
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
		
		mProgressDialog = new LoadingDialog(this, LoadingDialog.TYPE_ROTATION);
		mProgressDialog.show();
		
	}
	
	private void hideIndeterminateDialog()
	{
		if(mProgressDialog != null)
		{
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
	}
	
	/**
	 * 다운로드를 강제로 실행하게 하는 메소드
	 * @param isAllDownload TRUE : 전체 다운로드</p>FALSE : 하나 다운로드
	 */
	private void forceStartDownloadItem(boolean isAllDownload)
	{
		if (isDownloadAvailable() == true)
		{
			
			GoogleAnalyticsHelper.getInstance(this).sendCurrentAppView(InformationTemplete.ANALYTICS_VIEW_DOWNLOAD_ITEM);
			if (isAllDownload == true)
			{
				Log.f("결제 후 바로 다운로드 모든 파일");
				GoogleAnalyticsHelper.getInstance(this).sendCurrentAppView(InformationTemplete.ANALYTICS_ACTIVITY_MAIN+" : "+"All Item "+InformationTemplete.ANALYTICS_ACTION_DOWNLOAD);
				for(int i = 0 ; i < mVideoBase.getVideoInfoList().size(); i++)
				{
					if(mVideoBase.isPlayAvailableItem(i) == false)
					{
						
						if(mVideoBase.pullDownloadItem() == -1)
						{
							mCurrentDownloadPosition = i;
							mVideoBase.addQueueItem(i);
						}
						else
						{
							if(mVideoBase.haveDownloadQueue(i) == false)
							{
								mVideoBase.addQueueItem(i);
							}
							else
							{
								mVideoBase.cancelQueueDownloadIdleItem(i);
							}
							
						}
					}
				}
				downloadStart();
			}
			else
			{
				Log.f("결제 후 바로 하나 파일 다운로드 : "+ mCurrentPayingPosition);
				GoogleAnalyticsHelper.getInstance(this).sendCurrentAppView(InformationTemplete.ANALYTICS_ACTIVITY_MAIN+" : "+mVideoBase.getVideoInfoList().get(mCurrentPayingPosition).getPurchaseCode()
						+" Item "+InformationTemplete.ANALYTICS_ACTION_DOWNLOAD);
				if(mVideoBase.pullDownloadItem() == -1)
				{
					mCurrentDownloadPosition = mCurrentPayingPosition;
					mVideoBase.addQueueItem(mCurrentDownloadPosition);
					downloadStart();
				}
				else
				{
					if(mVideoBase.haveDownloadQueue(mCurrentPayingPosition) == false)
					{
						mVideoBase.addQueueItem(mCurrentPayingPosition);
					}
					else
					{
						mVideoBase.cancelQueueDownloadIdleItem(mCurrentPayingPosition);
					}
					
				}
				
			}

			mStorybookListRecyclerAdapter.notifyDataSetChanged();
		}
		else
		{
			showTempleteAlertDialog(TempleteAlertDialog.DIALOG_EVENT_DEFAULT, TempleteAlertDialog.DEFAULT_BUTTON_TYPE_1, getResources().getString(R.string.storage_available_error));
		}

	}
	
	private void startDownloadRecommandIcon()
	{
		Log.i("");
		for(int i = 0; i < mInitItemResult.banner_list.size(); i++)
		{
			if(mInitItemResult.banner_list.get(i).isFileDownloadComplete() ==  false)
			{
				ReturnIndexDownloadAsync async = new ReturnIndexDownloadAsync(this, mInitItemResult.banner_list.get(i).getIconUrl(),
						mInitItemResult.banner_list.get(i).getFileDownloadPath(), i, mRecommandIconDownloadListener);
				async.execute();
			}
		}
	}
	
	private boolean isAllDownloadRecommandIcon()
	{
		boolean isComplete = true;
		for(int i = 0; i < mInitItemResult.banner_list.size(); i++)
		{
			if(mInitItemResult.banner_list.get(i).isFileDownloadComplete() == false)
			{
				isComplete = false;
				break;
			}
		}
		
		return isComplete;
	}
	
	private void setRecommandDialogIconVisiblity(boolean isVisible)
	{
		_RecommonadShowView.setVisibility(isVisible ? View.VISIBLE : View.GONE);
	}
	
	private void hideRecommandIconAnimation(boolean isHide)
	{
		ViewAnimator.animate(_RecommonadShowView)
				.translationY(isHide ? CommonUtils.getInstance(MainListActivity.this).getPixel(250) : 0)
				.duration(DURATION_ICON_VISIBLE)
				.start();
	}

	private OnClickListener mOnClickListener = new OnClickListener()
	{
		
		@Override
		public void onClick(View v)
		{
			switch(v.getId())
			{
			case R.id.move_scroll_button:
				_MainRecyclerView.smoothScrollToPosition(0);
				break;
			case R.id.top_drawer_control_button:
				
				

				DisPlayMetricsObject object = (DisPlayMetricsObject) CommonUtils.getInstance(MainListActivity.this).getPreferenceObject(Common.PARAMS_DISPLAY_METRICS, DisPlayMetricsObject.class);
		        
				mIntroductionPhoneMoveheight = _IntroductionLayout.getHeight();
				
				Log.f("object.heightPixel : "+object.heightPixel+", _IntroductionLayout.getHeight() : "+_IntroductionLayout.getHeight()+", getPixel height : " + CommonUtils.getInstance(MainListActivity.this).getHeightPixel(238));

				
				if(_IntroductionLayout.getVisibility() == View.INVISIBLE)
				{
					ViewAnimator.animate(_IntroductionLayout).translationY(-mIntroductionPhoneMoveheight, 0).interpolator(new LinearInterpolator()).duration(DURATION_ANIMATION)
					.andAnimate(_MenubarLayout).translationY(0, mIntroductionPhoneMoveheight).interpolator(new LinearInterpolator()).duration(DURATION_ANIMATION)
					.onStart(new Start()
					{

						@Override
						public void onStart()
						{
							_IntroductionLayout.setVisibility(View.VISIBLE);
						}
						
					})
					.onStop(new Stop()
					{
						
						@Override
						public void onStop()
						{
							_MenubarControlButton.setImageResource(R.drawable.gnb_down_btn);
							_TopBarCoverImage.setVisibility(View.VISIBLE);
							
						}
					})
					.andAnimate(_RecommonadShowView).translationY(CommonUtils.getInstance(MainListActivity.this).getPixel(250)).interpolator(new LinearInterpolator()).duration(DURATION_ANIMATION)
					.start();
				}
				else
				{
					ViewAnimator.animate(_IntroductionLayout).translationY(0, -mIntroductionPhoneMoveheight).interpolator(new LinearInterpolator()).duration(DURATION_ANIMATION)
					.andAnimate(_MenubarLayout).translationY(mIntroductionPhoneMoveheight, 0 ).interpolator(new LinearInterpolator()).duration(DURATION_ANIMATION)
					.onStart(new Start()
					{

						@Override
						public void onStart()
						{
							_TopBarCoverImage.setVisibility(View.GONE);
						}
						
					})
					.onStop(new Stop()
					{
						
						@Override
						public void onStop()
						{
							_MenubarControlButton.setImageResource(R.drawable.gnb_top_btn);
							_IntroductionLayout.setVisibility(View.INVISIBLE);
						}
					})
					.andAnimate(_RecommonadShowView).translationY(0).interpolator(new LinearInterpolator()).duration(DURATION_ANIMATION)
					.start();
				}
				
				

				
				break;
			}
		}
	};
	
	private StorybookListItemListener mStorybookListItemListener = new StorybookListItemListener()
	{
		
		@Override
		public void onThumbnailItemSelected(int position)
		{
			Log.f(" position : " +position);
			if(mVideoBase.isPurchaseItem(position) == false)
			{
				if(NetworkUtil.getConnectivityStatus(getApplicationContext()) == NetworkUtil.TYPE_NOT_CONNECTED)
				{
					mMainHandler.sendEmptyMessage(MESSAGE_NETWORK_ERROR);
				}
				else
				{
					Log.f("Not Purchased. show payment Popup");
					mCurrentPayingPosition = position;
					showPaymentDialog(mVideoBase.getVideoInfoList().get(mCurrentPayingPosition).getEpisodeTitle());
				}
				return;
			}
			
			
			
			if(mVideoBase.isPlayAvailableItem(position) == true)
			{
				Log.f("Video Play Available. PlayVideo : "+ position);
				CommonUtils.getInstance(MainListActivity.this).setSharedPreference(Common.PARAMS_BEFORE_WATCHED_MOVIE, position);
				startMoviePlayer(position);
				
			}
			else
			{
				if(NetworkUtil.getConnectivityStatus(getApplicationContext()) == NetworkUtil.TYPE_NOT_CONNECTED)
				{
					mMainHandler.sendEmptyMessage(MESSAGE_NETWORK_ERROR);
				}
				else 
				{
					if(isDownloadAvailable() == true)
					{
						if(mVideoBase.isDownloadingItem(position) == true)
						{
							Log.f("video downloading.. return.");
							return;
						}
						
						if(mVideoBase.pullDownloadItem() == -1)
						{
							mCurrentDownloadPosition = position;
							if(NetworkUtil.getConnectivityStatus(getApplicationContext()) == NetworkUtil.TYPE_MOBILE)
							{
								Log.f("download Possible. but not wifi. show warning popup.");
								showTempleteAlertDialog(TempleteAlertDialog.DIALOG_EVENT_MOBILE_DOWNLOAD_SUBMIT_ITEM_CLICK, 
										getResources().getString(R.string.btn_download_cancel),getResources().getString(R.string.btn_download_continue), getResources().getString(R.string.lte_3g_relate_question));
							}
							else
							{
								Log.f("download Possible. download start.");
								mVideoBase.addQueueItem(mCurrentDownloadPosition);
								downloadStart();
							}

						}
						else
						{
							if(mVideoBase.haveDownloadQueue(position) == false)
							{
								Log.f("download stack add : "+ position);
								mVideoBase.addQueueItem(position);
							}
							else
							{
								Log.f("download stack pull : "+ position);
								mVideoBase.cancelQueueDownloadIdleItem(position);
							}
							
						}
						
						/**
						 * 상단 뷰를 포함한 인덱스를 계산해야한다.
						 */
						int thumbnailIndex = position + 1;
						mStorybookListRecyclerAdapter.notifyItemChanged(thumbnailIndex);
					}
					else
					{
						showTempleteAlertDialog(TempleteAlertDialog.DIALOG_EVENT_DEFAULT, TempleteAlertDialog.DEFAULT_BUTTON_TYPE_1, 
								getResources().getString(R.string.storage_available_error));
					}

				}
			}	
		}
		
		@Override
		public void onMenuButtonClicked()
		{
			Log.f("");
			if(_DrawerLayout.isDrawerVisible(_SlideMenuLayout) == false)
			{
				_DrawerLayout.openDrawer(_SlideMenuLayout);
			}
		}
	};
	

	
	private OnClickListener mMenuClickListener = new OnClickListener()
	{

		@Override
		public void onClick(View v)
		{
			if(NetworkUtil.getConnectivityStatus(getApplicationContext()) == NetworkUtil.TYPE_NOT_CONNECTED)
			{
				_DrawerLayout.closeDrawer(_SlideMenuLayout);
				mMainHandler.sendEmptyMessageDelayed(MESSAGE_NETWORK_ERROR, DURATION_APPEAR);
				return;
			}
			switch(v.getId())
			{
			case R.id.menu_button_input_promotion:
				Log.f("Button Show Promotion Dialog");
				if(mInitItemResult != null)
				{
					mMainHandler.sendEmptyMessageDelayed(MESSAGE_SHOW_PROMOTION_DIALOG, DURATION_APPEAR);
				}
				
				break;
			case R.id.menu_button_other_app:
				Log.f("Button Show Other App");
				GoogleAnalyticsHelper.getInstance(MainListActivity.this).sendCurrentAppView(InformationTemplete.ANALYTICS_VIEW_MENU_OTHER);
				CommonUtils.getInstance(MainListActivity.this).startLinkMove(Common.INC_LINK);
				break;
			case R.id.menu_button_music:
				Common.IS_BACKGROUND_SOUND_ENABLE = !Common.IS_BACKGROUND_SOUND_ENABLE;
				Log.f("Button Enable Music : "+ Common.IS_BACKGROUND_SOUND_ENABLE);
				_MenuBackgroundMusicModeIcon.setImageResource(getModeImage(Common.IS_BACKGROUND_SOUND_ENABLE));
				CommonUtils.getInstance(MainListActivity.this).setSharedPreference(Common.PARAMS_BACKGROUND_SOUND, Common.IS_BACKGROUND_SOUND_ENABLE);
				
				if(Common.IS_BACKGROUND_SOUND_ENABLE)
				{
					startBGMPlayer();
				}
				else
				{
					stopBGMPlayer();
				}
				return;
				
			case R.id.menu_button_action_mode:
				Common.IS_VIBRATE_ENABLE = !Common.IS_VIBRATE_ENABLE;
				Log.f("Button Enable Action Story Mode : "+ Common.IS_VIBRATE_ENABLE);
				_MenuActionModeIcon.setImageResource(getModeImage(Common.IS_VIBRATE_ENABLE));
				CommonUtils.getInstance(MainListActivity.this).setSharedPreference(Common.PARAMS_ACTION_STORY_MODE, Common.IS_VIBRATE_ENABLE);
				return;
				
			/*case R.id.menu_button_share_facebook:
				if(mInitItemResult != null)
				{
					FacebookInstance.getInstance().login();
				}
				String[] imageUrls = {"http://img.littlefox.co.kr/static/kakao_story.png"};
				mKakaoHelper.setKakaoStoryUrlInfo("Rocket Girl","어린이 영어 교육 전문 회사 리틀팍스의 인기동화 Rockey Girl을 단행본으로 만나보세요?" , imageUrls);
				boolean result = mKakaoHelper.sendKakaoStoryLink(Common.APP_LINK);
				Log.i("result : "+result);
				break;
			case R.id.menu_button_star_give:
				GoogleAnalyticsHelper.getInstance().sendCurrentAppView(Common.ANALYTICS_VIEW_MENU_RATE);
				CommonUtils.startLinkMove(MainListActivity.this, Common.APP_LINK);
				break;*/
			case R.id.menu_button_developer_inquire:
				Log.f("Button Developer Inquire");
				GoogleAnalyticsHelper.getInstance(MainListActivity.this).sendCurrentAppView(InformationTemplete.ANALYTICS_VIEW_MENU_INQUIRE);
				mMainHandler.sendEmptyMessageDelayed(MESSAGE_DEVELOPER_INQUIRE, DURATION_APPEAR);
				break;
			case R.id.menu_button_restore:
				Log.f("Button Restore");
				GoogleAnalyticsHelper.getInstance(MainListActivity.this).sendCurrentAppView(InformationTemplete.ANALYTICS_VIEW_MENU_RESTORE);
				mMainHandler.sendEmptyMessageDelayed(MESSAGE_RESTORE, DURATION_APPEAR);
				break;
			case R.id.menu_button_writer:
				Log.f("Button Credit Dialog");
				GoogleAnalyticsHelper.getInstance(MainListActivity.this).sendCurrentAppView(InformationTemplete.ANALYTICS_VIEW_MENU_CREDIT);
				mMainHandler.sendEmptyMessageDelayed(MESSAGE_SHOW_CREDIT_DIALOG, DURATION_APPEAR);
				break;
		
			}
			_DrawerLayout.closeDrawer(_SlideMenuLayout);
		}
		
	};
	
	
	private DownloadPlayListener mDownloadListner = new DownloadPlayListener()
	{
		
		@Override
		public void setMaxProgress(int maxProgress){}
		@Override
		public void playVideo(){}
		@Override
		public void onCanceled(){}
	
		@Override
		public void downloadProgress(int progress)
		{
			try
			{
				Message message = Message.obtain();
				message.what = MESSAGE_PROGRESS_ING;
				message.arg1 = progress;
				mMainHandler.sendMessage(message);
			}catch(Exception e)
			{
				
			}

			
		}
		
		@Override
		public void downloadComplete()
		{
			try
			{
				Log.f("file Download Complete");
				mMainHandler.sendEmptyMessage(MESSAGE_PROGRESS_COMPLETE);
			}catch(Exception e)
			{
				
			}
			
		}
	};

	/**
	 * 상단으로 자동 스크롤이 끝난 후 버튼이 GONE 상태를 돌아가지 못하는 걸 해결하기 위한 Listener
	 */
	private AnimationListener mScrollButtonMoveListener = new AnimationListener()
	{
		
		@Override
		public void onAnimationStart(Animation animation){}
		
		@Override
		public void onAnimationRepeat(Animation animation){}
		
		@Override
		public void onAnimationEnd(Animation animation)
		{
			_ScrollMoveButton.clearAnimation();
		}
	};
	
	/**
	 * 상단으로 자동 스크롤이 끝난 후 버튼이 GONE 상태를 돌아가지 못하는 걸 해결하기 위한 Listener
	 */
	private AnimationListener mIntroductionButtonMoveListener = new AnimationListener()
	{
		
		@Override
		public void onAnimationStart(Animation animation){}
		
		@Override
		public void onAnimationRepeat(Animation animation){}
		
		@Override
		public void onAnimationEnd(Animation animation)
		{
			_MenubarControlButton.clearAnimation();
		}
	};
	
	private AsyncListener mAsyncListener = new AsyncListener()
	{
		@Override
		public void onRunningStart(){}

		@Override
		public void onRunningEnd(Object mObject)
		{
			try
			{
				setRunningEndInformation(mObject);		
			}catch(Exception e)
			{
				Log.f("Exception e : "+e.getMessage());
			}
			
		}

		@Override
		public void onRunningCanceled(){}
		@Override
		public void onRunningProgress(Integer progress){}
		@Override
		public void onErrorListener(String code, String message){}
	};
		
	private DialogListener mDialogListener = new DialogListener()
	{
		
		@Override
		public void onItemClick(int messageType, Object sendObject)
		{
			Log.f("messageType : "+messageType);
			switch(messageType)
			{
				case Common.DIALOG_MESSAGE_PROMOTION_CODE_CONFIRM:
					mPromotionInputCode = (String)sendObject;
					Message msg = Message.obtain();
					msg.what = MESSAGE_PROMOTION_QUERY;
					msg.obj = (String)sendObject;
					mMainHandler.sendMessage(msg);
					break;
				case Common.DIALOG_MESSAGE_PROMOTION_LIST_ITEM_CONFIRM:
					ArrayList<String> result = new ArrayList<String>();
					result.add(mPromotionBaseResult.list.get((Integer)sendObject).iap);
					setDownloadAvailableFromPromotionCode(Common.PROMOTION_PRODUCT_TYPE_FLUID, result);
					break;
				case Common.DIALOG_MESSAGE_PAYMENT_ALL_PAY_CLICK:
					mCurrentPayingPosition = CODE_ALL_PAYMENT;
					Log.f("전체 구매");
					mInAppPurchase.purchaseItem(MainListActivity.this, mVideoBase.getProductAllSku());
					break;
				case Common.DIALOG_MESSAGE_PAYMENT_ONE_PAY_CLICK:
					Log.f("한편 구입하면 전체 구입시 한편구입으로 인한 차감되지않는 경고의 팝업");
					showTempleteAlertDialog(TempleteAlertDialog.DIALOG_EVENT_ONE_ITEM_PAY_WARNING, 
							getResources().getString(R.string.btn_download_cancel),getResources().getString(R.string.btn_download_continue), getResources().getString(R.string.product_buy_item_warning));
					break;
				case Common.DIALOG_MESSAGE_APPRAISAL_NEXT_NOT_AGAIN:
					Log.f("다음에 별점 주기");
					mVideoBase.setInitPlayed();
					break;
				case Common.DIALOG_MESSAGE_APPRAISAL_GO_RATE:
					Log.f("별점 주기 완료");
					mVideoBase.setInitPlayed();
					mVideoBase.setRateComplete(true);
					GoogleAnalyticsHelper.getInstance(MainListActivity.this).sendCurrentAppView(InformationTemplete.ANALYTICS_VIEW_MENU_OTHER);
					CommonUtils.getInstance(MainListActivity.this).startLinkMove(InformationTemplete.APP_LINK);
					break;
			}
		}

		@Override
		public void onItemClick(int messageType, int subMessageType, Object sendObject)
		{
			Log.f("messageType : "+messageType+", subMessageType : "+subMessageType);
			switch(messageType)
			{
			case Common.DIALOG_MESSAGE_FLAXIBLE_FIRST_BUTTON_CLICK:
				
				if (subMessageType == TempleteAlertDialog.DIALOG_EVENT_IAC)
				{
					Log.f("mInitItemResult.inapp.get(0).btn1_mode : "+mInitItemResult.inapp.btn1_mode);
					Log.f("mInitItemResult.inapp.get(0).btn_link : "+mInitItemResult.inapp.btn_link);
					if (mInitItemResult.inapp.btn1_mode.equals(Common.IAC_LINK_MOVE))
					{
						CommonUtils.getInstance(MainListActivity.this).startLinkMove(mInitItemResult.inapp.btn_link);
					}
					else if (mInitItemResult.inapp.btn1_mode.equals(Common.IAC_NEVER_SEE_AGAIN))
					{
						mIACInformation.addIACSleep(mInitItemResult.inapp.inapp_code);
					}
				}
				else if(subMessageType == TempleteAlertDialog.DIALOG_EVENT_MOBILE_DOWNLOAD_SUBMIT)
				{
					mVideoBase.initDownloadItem();
					mStorybookListRecyclerAdapter.notifyDataSetChanged();
				}
				
				
				break;
			case Common.DIALOG_MESSAGE_FLAXIBLE_SECOND_BUTTON_CLICK:
				if (subMessageType == TempleteAlertDialog.DIALOG_EVENT_IAC)
				{
					if (mInitItemResult.inapp.btn2_mode.equals(Common.IAC_LINK_MOVE))
					{
						CommonUtils.getInstance(MainListActivity.this).startLinkMove(mInitItemResult.inapp.btn_link);
					}
					else if (mInitItemResult.inapp.btn2_mode.equals(Common.IAC_NEVER_SEE_AGAIN))
					{
						mIACInformation.addIACSleep(mInitItemResult.inapp.inapp_code);
						FileUtils.writeFile(mIACInformation, StorybookTempleteAPI.PATH_APP_ROOT + Common.FILE_IAC_INFORMATION);
					}
				}
				else if (subMessageType == TempleteAlertDialog.DIALOG_EVENT_ONE_ITEM_PAY_WARNING)
				{
					mInAppPurchase.purchaseItem(MainListActivity.this, mVideoBase.getVideoInfoList().get(mCurrentPayingPosition).getPurchaseCode());
				}
				else if(subMessageType == TempleteAlertDialog.DIALOG_EVENT_MOBILE_DOWNLOAD_SUBMIT)
				{
					mMainHandler.sendEmptyMessageDelayed(MESSAGE_CONTINUE_DOWNLOAD, DURATION_CONTINUE_DOWNLOAD);
				}
				else if(subMessageType == TempleteAlertDialog.DIALOG_EVENT_DOWNLOAD_ALL_ITEM)
				{
					forceStartDownloadItem(true);
				}
				else if(subMessageType == TempleteAlertDialog.DIALOG_EVENT_DOWNLOAD_ONE_ITEM)
				{
					forceStartDownloadItem(false);
				}
				else if(subMessageType == TempleteAlertDialog.DIALOG_EVENT_MOBILE_DOWNLOAD_SUBMIT_ITEM_CLICK)
				{
					mMainHandler.sendEmptyMessage(MESSAGE_LIST_CLICK_DOWNLOAD);
				}
				else if(subMessageType == TempleteAlertDialog.DIALOG_EVENT_END_APP)
				{
					finish();
				}
				else if(subMessageType == TempleteAlertDialog.DIALOG_EVENT_UPDATE_APP)
				{
					if(sendObject == null)
					{
						finish();
						CommonUtils.getInstance(MainListActivity.this).startLinkMove(InformationTemplete.APP_LINK);
					}
					else if(((String)sendObject).equals(Common.BACK_PRESSED))
					{
						finish();
					}
				}
				
				break;
				
			}
		}
	};
	
	private NetworkConnectListener onNetworkConnectListener = new NetworkConnectListener()
	{

		@Override
		public void connectStatus(int type)
		{	
			Log.f("mBeforeConnectStatus : "+mBeforeConnectStatus);
			Log.f("type : "+type);
			if(mBeforeConnectStatus == type)
			{
				return;
			}
			
			switch(type)
			{
			case NetworkUtil.TYPE_MOBILE:
				if(mVideoBase.pullDownloadItem() != -1)
				{
					Log.f("LTE로 변경됬을때 사용자에게 묻는 팝업  : "+mVideoBase.pullDownloadItem());
	    			showTempleteAlertDialog(TempleteAlertDialog.DIALOG_EVENT_MOBILE_DOWNLOAD_SUBMIT, 
	    					getResources().getString(R.string.btn_download_cancel),getResources().getString(R.string.btn_download_continue), getResources().getString(R.string.lte_3g_relate_question));
	    			if(mContinueDownloadAsync != null)
	    			{
	    				mContinueDownloadAsync.setCancel(true);
	    				mContinueDownloadAsync.cancel(true);
	    			}
				}
				break;
			case NetworkUtil.TYPE_WIFI:
				if(mVideoBase.pullDownloadItem() != -1)
				{
					Log.f("와이파이 재연결하여 다시 받기 : "+mVideoBase.pullDownloadItem());
					mMainHandler.sendEmptyMessageDelayed(MESSAGE_CONTINUE_DOWNLOAD, DURATION_CONTINUE_DOWNLOAD);
				}
				break;
			case NetworkUtil.TYPE_NOT_CONNECTED:
				if(mContinueDownloadAsync != null && mContinueDownloadAsync.getStatus() == AsyncTask.Status.RUNNING)
				{
					mContinueDownloadAsync.setCancel(true);
					mContinueDownloadAsync.cancel(true);
				}
				break;
			}
			
			if(type != NetworkUtil.TYPE_NOT_CONNECTED)
			{
				if(mCurrentInAppStatus != InAppPurchase.STATUS_QUERY_INVENTORY_FINISHED)
				{
					mInAppPurchase.settingPurchaseInforamtionToGoogle();
				}

			}
			mBeforeConnectStatus = type;	
		}
		
	};
	
	private AsyncListener mRecommandIconDownloadListener = new AsyncListener()
	{
		@Override
		public void onRunningStart()
		{}

		@Override
		public void onRunningEnd(Object mObject)
		{
			Log.i("onRunningEnd : " + (int) mObject);
			if((int) mObject != -1)
			{
				mInitItemResult.banner_list.get((int) mObject).setFileDownloadComplete(true);
				CommonUtils.getInstance(MainListActivity.this).setPreferenceObject(Common.PARAMS_INIT_INFO, mInitItemResult);
				
				if(isAllDownloadRecommandIcon())
				{
					//TODO : 파일을 다운로드 전부 하였으니깐 화면에 보여주면된다.
					setRecommandDialogIconVisiblity(true);	
				}
			}
			
		}

		@Override
		public void onRunningCanceled()
		{}

		@Override
		public void onRunningProgress(Integer progress)
		{}

		@Override
		public void onErrorListener(String code, String message)
		{}

	};

}
