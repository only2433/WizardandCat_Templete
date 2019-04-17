package com.littlefox.storybook.wizardandcat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings.Secure;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.facebook.appevents.AppEventsLogger;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.gson.Gson;
import com.littlefox.library.system.root.RootCheck;
import com.littlefox.logmonitor.Log;
import com.littlefox.storybook.base.BaseActivity;
import com.littlefox.storybook.common.Feature;
import com.littlefox.storybook.common.InformationTemplete;
import com.littlefox.storybook.fcm.LittlefoxFirebaseMessagingService;
import com.littlefox.storybook.lib.api.StorybookTempleteAPI;
import com.littlefox.storybook.lib.async.InitAppAsync;
import com.littlefox.storybook.lib.async.VideoInformationAsync;
import com.littlefox.storybook.lib.async.listener.AsyncListener;
import com.littlefox.storybook.lib.common.Common;
import com.littlefox.storybook.lib.common.CommonUtils;
import com.littlefox.storybook.lib.common.FileUtils;
import com.littlefox.storybook.lib.common.Font;
import com.littlefox.storybook.lib.common.NetworkUtil;
import com.littlefox.storybook.lib.dialog.LoadingDialog;
import com.littlefox.storybook.lib.download.DownloadAsync;
import com.littlefox.storybook.lib.download.ReturnIndexDownloadAsync;
import com.littlefox.storybook.lib.enc.SimpleCrypto;
import com.littlefox.storybook.lib.object.DisPlayMetricsObject;
import com.littlefox.storybook.lib.object.InitItemResult;
import com.littlefox.storybook.lib.object.SharedVideoInfo;
import com.littlefox.storybook.lib.object.VideoBaseResult;
import com.littlefox.storybook.lib.object.VideoInformation;
import com.ssomai.android.scalablelayout.ScalableLayout;

import java.util.ArrayList;


public class IntroActivity extends BaseActivity
{

	private static final int PERMISSION_REQUEST_STORAGE = 1001;

	private static final int DURATION_WAIT_LOADING = 5000;

	public static final int MESSAGE_INIT 						= 0;
	public static final int MESSAGE_VIDEO_INFO 					= 1;
	public static final int MESSAGE_SETTING_COMPLETE			= 2;
	public static final int MESSAGE_PROGRAM_END					= -100;

	private VideoView _VideoView;
	private ImageView _SkipButton;
	private TextView _SkipText;
	private ScalableLayout _SkipBaseLayout;

	private int mCurrentMessage;
	private VideoInformationAsync mVideoInformationAsync = null;
	private InitItemResult mInitItemResult = null;
	private InitAppAsync mInitAppAsync;
	private SharedVideoInfo mVideoBase = null;
	private boolean isIntroPlayEnd = false;
	private LoadingDialog mProgressDialog = null;

	private int mCurrentPlayPosition = -1;
	private DownloadAsync mDownloadAsync;

	private ArrayList<Integer> mChangeThumbnailIndexList = new ArrayList<Integer>();

	private Handler mMainHandler = new Handler(){

		@Override
		public void handleMessage(Message msg)
		{
			switch(msg.what)
			{
				case MESSAGE_INIT:
					requestInit();
					break;
				case MESSAGE_VIDEO_INFO:
					requestVideoInformation();
					break;
				case MESSAGE_SETTING_COMPLETE:
					isIntroPlayEnd = true;
					sendCompleteEvent();
					break;
				case MESSAGE_PROGRAM_END:
					finish();
					Toast.makeText(getApplicationContext(), getResources().getString(R.string.network_error), Toast.LENGTH_SHORT).show();
					break;
			}
		}

	};
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);


		Log.init(InformationTemplete.LOG_FILE);
		CommonUtils.getInstance(this).getWindowInfo();
		CommonUtils.getInstance(this).showDeviceInfo();
		initSetting();
		initStorybookTempleteInformation();


		if(Feature.IS_TABLET_MODE == true && Feature.IS_MINIMUM_SUPPORT_TABLET_RADIO_DISPLAY == true)
		{
			setContentView(R.layout.intro_main_not_support_display);
		}
		else
		{
			setContentView(R.layout.intro_main);
		}

		Uri uri = getIntent().getData();

		if(uri != null)
		{
			Feature.IS_WEB_EXCUTE_WITH_COUPON = true;
			Common.COUPON_TEXT_TO_WEB = uri.getQueryParameter("param1");
		}
		else
		{
			Feature.IS_WEB_EXCUTE_WITH_COUPON = false;
		}


		if(RootCheck.checkRootingDevice() == true)
		{
			Toast.makeText(this, getResources().getString(R.string.root_not_play), Toast.LENGTH_LONG).show();
			finish();
		}

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		checkMacAddress();

		if(Build.VERSION.SDK_INT >= Common.MALSHMALLOW)
		{
			checkPermission();
		}
		else
		{
			settingLogFile();
			init();
		}

	}

	/**
	 * Permission check.
	 */
	private void checkPermission()
	{
		if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
				|| checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
		{
			// Should we show an explanation?
			if (shouldShowRequestPermissionRationale(android.Manifest.permission.WRITE_EXTERNAL_STORAGE))
			{
				// Explain to the user why we need to write the permission.
				Toast.makeText(this, "Read/Write external storage", Toast.LENGTH_SHORT).show();
			}

			requestPermissions(new String[] { android.Manifest.permission.READ_EXTERNAL_STORAGE,
					android.Manifest.permission.WRITE_EXTERNAL_STORAGE }, PERMISSION_REQUEST_STORAGE);

			// MY_PERMISSION_REQUEST_STORAGE is an
			// app-defined int constant

		} else
		{
			settingLogFile();
			init();

		}
	}

	private void initStorybookTempleteInformation()
	{
		DisplayMetrics displayMetrics  = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		DisPlayMetricsObject object = new DisPlayMetricsObject(displayMetrics.widthPixels, displayMetrics.heightPixels);

		StorybookTempleteAPI.getInstance(this).init
				(InformationTemplete.APP_NAME,
						InformationTemplete.PACKAGE_NAME,
						InformationTemplete.HTTP_HEADER_APP_NAME,
						InformationTemplete.ONE_PAY_ITEM_SKU,
						InformationTemplete.ALL_PAY_ITEM_SKU,
						InformationTemplete.IN_APP_BILLING_KEY,
						InformationTemplete.GOOGLE_ANALYTICS_PROPERTY_ID, object, Feature.IS_TABLET_MODE);

		if(InformationTemplete.IS_VIBRATOR_TEST)
		{
			StorybookTempleteAPI.PATH_VIBRATOR_ROOT = StorybookTempleteAPI.PATH_EXTERNAL_VIBRATOR_ROOT;
		}
		else
		{
			StorybookTempleteAPI.PATH_VIBRATOR_ROOT = StorybookTempleteAPI.PATH_APP_ROOT;
		}
	}

	private void initSetting()
	{

		Feature.IS_TABLET_MODE = CommonUtils.getInstance(this).isTablet();

		if((float) CommonUtils.getInstance(this).getDisplayWidth() / (float) CommonUtils.getInstance(this).getDisplayHeight() < Common.MINIMUM_TABLET_DISPLAY_RADIO)
		{
			Log.f("4 : 3 비율 ");
			Feature.IS_MINIMUM_SUPPORT_TABLET_RADIO_DISPLAY = true;
		}
		else
		{
			Log.f("16 : 9 비율 ");
			Feature.IS_MINIMUM_SUPPORT_TABLET_RADIO_DISPLAY = false;
		}

	}

	private void init()
	{
		_SkipBaseLayout = (ScalableLayout)findViewById(R.id.skip_base_layout);
		_VideoView	= (VideoView)findViewById(R.id.intro_video_view);
		_SkipButton = (ImageView)findViewById(R.id.skip_btn);
		_SkipText 	= (TextView)findViewById(R.id.skip_text);
		_SkipButton.setVisibility(View.GONE);
		_SkipText.setVisibility(View.GONE);
		_SkipButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v)
			{
				_SkipButton.setEnabled(false);
				isIntroPlayEnd = true;
				showIndeterminateDialog();
				startMainActivity();
			}

		});
		adjustSkipLayout();
		initFont();
		setupCallbackVideoListener();
		initVideoView();

		initFCMInstance();

		/**
		 * video_information.txt에 있는 정보는 이제 더이상 사용하지 않는다. 전체 삭제를 해야한다.
		 */
		if(FileUtils.checkFile(StorybookTempleteAPI.PATH_APP_ROOT+ Common.FILE_NOT_USE_VIDEO_INFORMATION))
		{
			Common.INIT_DATA_RESTORE = true;
			FileUtils.clearApplicationData(this);
		}
		else
		{
			Common.INIT_DATA_RESTORE = false;
		}

		if(NetworkUtil.getConnectivityStatus(this) == NetworkUtil.TYPE_NOT_CONNECTED)
		{
			if(FileUtils.checkFile(StorybookTempleteAPI.PATH_VIDEO_INFORMATION_ROOT+ Common.FILE_NEW_VIDEO_INFORMATION) == true && FileUtils.checkFile(StorybookTempleteAPI.PATH_APP_ROOT+ Common.FILE_VIBRATOR_INFORMATION) == true)
			{
				Log.f("Network Not Connected : Init Information Download Complete");
				_SkipButton.setVisibility(View.VISIBLE);
				_SkipText.setVisibility(View.VISIBLE);
				isIntroPlayEnd = true;
			}
			else
			{
				Log.f("Network Not Connected : Init Information Data Not Exist");
				Toast.makeText(this, getResources().getString(R.string.network_error), Toast.LENGTH_LONG).show();
				finish();
			}

		}
		else
		{
			mMainHandler.sendEmptyMessage(MESSAGE_INIT);
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		switch (requestCode) {
			case PERMISSION_REQUEST_STORAGE:
				if (grantResults[0] == PackageManager.PERMISSION_GRANTED
						&& grantResults[1] == PackageManager.PERMISSION_GRANTED) {

					settingLogFile();
					init();
					// permission was granted, yay! do the
					// calendar task you need to do.

				} else {

					Log.d("Permission always deny");
					finish();
					// permission denied, boo! Disable the
					// functionality that depends on this permission.
				}
				break;
		}
	}

	private void settingLogFile()
	{
		long logfileSize = Log.getLogfileSize();
		Log.f("Log file Size : " + logfileSize);
		if(logfileSize > Common.MAXIMUM_LOG_FILE_SIZE || logfileSize == 0L)
		{
			Log.initWithDeleteFile(InformationTemplete.LOG_FILE);
		}

	}

	private void settingThumbnailImageList()
	{
		String thumbnailName = "";
		int id = 0;
		String path = "";
		int thumbnailDownloadIndex = 0;
		for(int i = 1 ; i <= mVideoBase.getVideoInfoList().size() ; i++)
		{
			if(i < 10)
			{
				thumbnailName = "thumbnail_0"+i;
			}
			else
			{
				thumbnailName = "thumbnail_"+i;

			}

			if(FileUtils.checkFile(CommonUtils.getInstance(this).getThumbnailPath(i)) == false)
			{
				id = CommonUtils.getInstance(this).getDrawableResourceFromString(thumbnailName);
				path = CommonUtils.getInstance(this).getThumbnailPath(i);

				Log.i("save Thumbnail ID : " + id + ", path : "+ path);
				CommonUtils.getInstance(this).saveFileFromDrawable(path, id);
			}

		}
		if(mChangeThumbnailIndexList.size() > 0)
		{
			for(int i = 0 ; i < mChangeThumbnailIndexList.size() ; i++)
			{
				thumbnailDownloadIndex = mChangeThumbnailIndexList.get(i);
				startThumbnailDownload(thumbnailDownloadIndex);
			}
		}
		else
		{
			mMainHandler.sendEmptyMessage(MESSAGE_SETTING_COMPLETE);
		}

	}

	private void startThumbnailDownload(int downloadItemIndex)
	{
		int thumbnailIndex = downloadItemIndex + 1;
		ReturnIndexDownloadAsync returnIndexDownloadAsync = new ReturnIndexDownloadAsync(this,
				mVideoBase.getVideoInfoList().get(downloadItemIndex).getDownloadThumbnailUrl(),
				CommonUtils.getInstance(this).getThumbnailPath(thumbnailIndex+1),
				downloadItemIndex, mThumbnailDownloadListener);
		returnIndexDownloadAsync.execute();

	}

	private void adjustSkipLayout()
	{
		LayoutParams params = (LayoutParams) _SkipBaseLayout.getLayoutParams();
		params.bottomMargin = CommonUtils.getInstance(this).getHeightPixel(76);
		_SkipBaseLayout.setLayoutParams(params);
	}

	private void initFont()
	{
		_SkipText.setTypeface(Font.getInstance(this).getRobotoBold());
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

	private void checkMacAddress()
	{
		String macAddress = Secure.getString(getContentResolver(), Secure.ANDROID_ID);

		if(macAddress == null)
		{
			finish();
			Toast.makeText(this, getResources().getString(R.string.mac_address_error), Toast.LENGTH_LONG).show();
		}
	}

	private void initVideoView()
	{
		Uri video = Uri.parse("android.resource://" + getPackageName()+"/" + R.raw.intro_movie);
		_VideoView.setVideoURI(video);
		_VideoView.start();
	}

	private void setupCallbackVideoListener()
	{
		_VideoView.setOnErrorListener(new OnErrorListener(){

			@Override
			public boolean onError(MediaPlayer mp, int what, int extra)
			{
				return false;
			}

		});

		_VideoView.setOnPreparedListener(new OnPreparedListener(){

			@Override
			public void onPrepared(MediaPlayer mp)
			{

			}

		});

		_VideoView.setOnCompletionListener(new OnCompletionListener(){

			@Override
			public void onCompletion(MediaPlayer mp)
			{
				if(isIntroPlayEnd == false)
				{
					Log.f("Init Information Download or Vibrator Information Download fail");
					try
					{
						if(mProgressDialog.isShowing() == false)
						{
							showIndeterminateDialog();
						}
					}catch(NullPointerException e)
					{
						e.printStackTrace();
					}

					mMainHandler.sendEmptyMessageDelayed(MESSAGE_PROGRAM_END, DURATION_WAIT_LOADING);
				}
				else
				{
					startMainActivity();
				}

			}

		});
	}

	@Override
	protected void onResume()
	{
		Log.f("");
		super.onResume();
		AppEventsLogger.activateApp(this);
		resumeVideo();

	}

	@Override
	protected void onPause()
	{
		Log.f("");
		super.onPause();
		AppEventsLogger.deactivateApp(this);
		pauseVideo();
	}

	@Override
	protected void onStop()
	{
		Log.f("");
		super.onStop();
	}

	@Override
	protected void onDestroy()
	{
		Log.f("");
		super.onDestroy();
		hideIndeterminateDialog();
		mMainHandler.removeCallbacksAndMessages(null);
		release();
	}

	private void release()
	{
		Log.f("");
		if(mVideoInformationAsync != null)
		{
			mVideoInformationAsync.cancel(true);
		}

		if(mInitAppAsync != null)
		{
			mInitAppAsync.cancel(true);
		}

		if(mDownloadAsync != null)
		{
			mDownloadAsync.cancel(true);
		}

	}

	/**
	 * 초기 정보값을 보내기 위해 서버 통신을 한다.
	 */
	private void requestInit()
	{
		Log.f("");
		if(mInitAppAsync != null)
		{
			mInitAppAsync.cancel(true);
			mInitAppAsync = null;
		}
		mInitAppAsync = new InitAppAsync(this, mAsyncListener);
		mInitAppAsync.execute();

		mCurrentMessage = MESSAGE_INIT;
	}


	/**
	 * Video Information 정보를 받기위해 서버통신을 한다.
	 */
	private void requestVideoInformation()
	{
		Log.f("");
		if (mVideoInformationAsync != null)
		{
			mVideoInformationAsync.cancel(true);
			mVideoInformationAsync = null;
		}
		mVideoInformationAsync = new VideoInformationAsync(this, mAsyncListener);
		mVideoInformationAsync.execute();

		mCurrentMessage = MESSAGE_VIDEO_INFO;
	}

	private void initFCMInstance()
	{
		Log.f("");
		FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(this, new OnSuccessListener<InstanceIdResult>() {
			@Override
			public void onSuccess(InstanceIdResult instanceIdResult)
			{
				Log.f("token : "+ instanceIdResult.getToken());
				CommonUtils.getInstance(IntroActivity.this).setSharedPreference(LittlefoxFirebaseMessagingService.PARAMS_FIREBASE_PUSH_TOKEN, instanceIdResult.getToken());
			}
		});
	}


	private void setRunningEndInformation(Object object)
	{
		Log.f("Current Message : "+mCurrentMessage);
		switch(mCurrentMessage)
		{
			case MESSAGE_INIT:
				Log.f("Video Init Response Complete");
				mInitItemResult = (InitItemResult) object;

				checkThumbnailDownloadComplete();

				if(isExcuteDownloadVibratorFile(Integer.valueOf(mInitItemResult.vibrator.version)) == true)
				{
					mDownloadAsync = new DownloadAsync(this, mInitItemResult.vibrator.url, StorybookTempleteAPI.PATH_VIBRATOR_ROOT+ Common.FILE_VIBRATOR_INFORMATION, mVibratorDownloadListener);
					mDownloadAsync.execute();
				}
				else
				{

					mMainHandler.sendEmptyMessage(MESSAGE_VIDEO_INFO);
				}

				break;
			case MESSAGE_VIDEO_INFO:
				Log.f("Video Information Response Complete");
				processVideoInformation(object);
				//TODO 파일저장
				settingThumbnailImageList();
				break;

		}
	}

	private void checkThumbnailDownloadComplete()
	{
		InitItemResult initItemResult = (InitItemResult) CommonUtils.getInstance(this).getPreferenceObject(Common.PARAMS_INIT_INFO, InitItemResult.class);

		if(initItemResult != null)
		{
			for(int i = 0 ; i < initItemResult.banner_list.size(); i++)
			{
				mInitItemResult.banner_list.get(i).setFileDownloadComplete(initItemResult.banner_list.get(i).isFileDownloadComplete());
			}
		}

		CommonUtils.getInstance(this).setPreferenceObject(Common.PARAMS_INIT_INFO, mInitItemResult);
	}

	private void sendCompleteEvent()
	{
		if(isIntroPlayEnd == true)
		{
			if(mMainHandler.hasMessages(MESSAGE_PROGRAM_END))
			{
				mMainHandler.removeMessages(MESSAGE_PROGRAM_END);
				startMainActivity();
			}
			else
			{
				_SkipButton.setVisibility(View.VISIBLE);
				_SkipText.setVisibility(View.VISIBLE);
			}
		}
	}

	/**
	 * Vibrator 파일을 API SERVER 통신하여 다운로드 해야하는 지 여부를 알려준다.
	 * @param serverVersion
	 * @return TRUE : 다운로드 해야한다. ( 파일이 없거나 , 파일버젼이 업데이트 되었거나 ) </P> FALSE : 다운로드 무시 (서버버젼과 같다.)
	 */
	private boolean isExcuteDownloadVibratorFile(int serverVersion)
	{
		int localVersion = (Integer) CommonUtils.getInstance(this).getSharedPreference(Common.PARAMS_VIBRATOR_VERSION, Common.TYPE_PARAMS_INTEGER);

		if(FileUtils.checkFile(StorybookTempleteAPI.PATH_APP_ROOT+ Common.FILE_VIBRATOR_INFORMATION) == false)
		{
			Log.f("Vibrator Information Not Have !!");
			return true;
		}

		if(localVersion < serverVersion)
		{
			Log.f("Vibrator Information Update !!");
			CommonUtils.getInstance(this).setSharedPreference(Common.PARAMS_VIBRATOR_VERSION, serverVersion);
			return true;
		}

		return false;

	}


	private void resumeVideo()
	{
		try
		{
			if(_VideoView.isPlaying() == false)
			{
				_VideoView.seekTo(mCurrentPlayPosition);
				_VideoView.start();
			}
		}catch(NullPointerException e)
		{
			e.printStackTrace();
		}

	}

	private void pauseVideo()
	{
		try
		{
			if(_VideoView.isPlaying())
			{
				mCurrentPlayPosition = _VideoView.getCurrentPosition();
				_VideoView.pause();
			}
		}catch(NullPointerException e)
		{
			e.printStackTrace();
		}

	}

	/**
	 * 서버에서 받은 Video 정보를 어플리케이션에서 사용할 수 있게 변형
	 *
	 * @param object
	 * @return
	 */
	private SharedVideoInfo makeVideoBase(Object object)
	{
		Log.f("");
		VideoBaseResult result = (VideoBaseResult) object;

		SharedVideoInfo videoBase = new SharedVideoInfo();

		videoBase.setProductAllSku(result.product_all);
		int movieCount = -1;
		int freeMovieCount = 0;
		for (int i = 0; i < result.list.size(); i++)
		{
			movieCount = i + 1;
			videoBase.addVideoInformation(new VideoInformation(result.list.get(i).fc_id, result.list.get(i).iap,
					result.list.get(i).url, result.list.get(i).title, result.list.get(i).change_date));
			String index = movieCount < 10 ? String.valueOf("0" + movieCount) : String.valueOf(movieCount);

			if (SimpleCrypto.getMD5Hash("0" + index + "F").equals(result.list.get(i).type))
			{
				freeMovieCount++;
				videoBase.getVideoInfoList().get(i).setStatus(VideoInformation.STATUS_DOWNLOAD_AVAILABLE);
				videoBase.getVideoInfoList().get(i).setFreeItem(true);
			}
		}

		CommonUtils.getInstance(this).setSharedPreference(Common.PARAMS_FREE_ITEM_COUNT, freeMovieCount);

		return videoBase;
	}

	private void processVideoInformation(Object object)
	{
		try
		{
			if(FileUtils.checkFile(StorybookTempleteAPI.PATH_VIDEO_INFORMATION_ROOT + Common.FILE_NEW_VIDEO_INFORMATION))
			{
				SharedVideoInfo serverVideoInfo = makeVideoBase(object);
				String fileString = FileUtils.getStringFromFile(StorybookTempleteAPI.PATH_VIDEO_INFORMATION_ROOT + Common.FILE_NEW_VIDEO_INFORMATION);
				mVideoBase = new Gson().fromJson(fileString, SharedVideoInfo.class);
				mChangeThumbnailIndexList = mVideoBase.processChangeDateItem(serverVideoInfo);
			}
			else
			{
				mVideoBase = makeVideoBase(object);
			}


		}
		catch (Exception e)
		{
			Log.f("Error : " + e.getMessage());
		}


		FileUtils.writeFile(mVideoBase, StorybookTempleteAPI.PATH_VIDEO_INFORMATION_ROOT+ Common.FILE_NEW_VIDEO_INFORMATION);
	}

	private void startMainActivity()
	{
		Intent intent = new Intent(this, MainListActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		overridePendingTransition(R.anim.push_up_in, R.anim.push_up_out);
		finish();

	}

	private AsyncListener mAsyncListener = new AsyncListener()
	{

		@Override
		public void onRunningStart(){}

		@Override
		public void onRunningProgress(Integer progress){}

		@Override
		public void onRunningEnd(Object mObject)
		{
			setRunningEndInformation(mObject);
		}

		@Override
		public void onRunningCanceled(){}

		@Override
		public void onErrorListener(String code, String message){}
	};

	private AsyncListener mThumbnailDownloadListener = new AsyncListener()
	{
		@Override
		public void onRunningStart(){}

		@Override
		public void onRunningEnd(Object mObject)
		{
			Log.f("onRunningEnd : " + (int) mObject);
			if((int) mObject != -1)
			{
				mChangeThumbnailIndexList.remove(mObject);
			}

			if(mChangeThumbnailIndexList.size() <= 0)
			{
				mMainHandler.sendEmptyMessage(MESSAGE_SETTING_COMPLETE);
			}
		}

		@Override
		public void onRunningCanceled(){}

		@Override
		public void onRunningProgress(Integer progress){}

		@Override
		public void onErrorListener(String code, String message){}

	};

	private AsyncListener mVibratorDownloadListener = new AsyncListener()
	{
		@Override
		public void onRunningStart(){}
		@Override
		public void onRunningEnd(Object mObject)
		{
			Log.f("Vibrator File Download Complete");
			mMainHandler.sendEmptyMessage(MESSAGE_VIDEO_INFO);
		}
		@Override
		public void onRunningCanceled(){}
		@Override
		public void onRunningProgress(Integer progress){}
		@Override
		public void onErrorListener(String code, String message){}

	};


}
