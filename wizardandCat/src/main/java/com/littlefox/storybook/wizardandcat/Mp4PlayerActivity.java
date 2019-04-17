package com.littlefox.storybook.wizardandcat;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.littlefox.logmonitor.Log;
import com.littlefox.storybook.base.BaseActivity;
import com.littlefox.storybook.common.Feature;
import com.littlefox.storybook.common.InformationTemplete;
import com.littlefox.storybook.lib.analytics.GoogleAnalyticsHelper;
import com.littlefox.storybook.lib.api.StorybookTempleteAPI;
import com.littlefox.storybook.lib.common.Common;
import com.littlefox.storybook.lib.common.CommonUtils;
import com.littlefox.storybook.lib.common.FileUtils;
import com.littlefox.storybook.lib.common.Font;
import com.littlefox.storybook.lib.common.NetworkUtil;
import com.littlefox.storybook.lib.dialog.DialogListener;
import com.littlefox.storybook.lib.dialog.LoadingDialog;
import com.littlefox.storybook.lib.dialog.TempleteAlertDialog;
import com.littlefox.storybook.lib.object.CaptionInformation;
import com.littlefox.storybook.lib.object.SharedVideoInfo;
import com.littlefox.storybook.lib.object.VibratorBaseObject;
import com.littlefox.storybook.lib.object.VibratorObject;
import com.littlefox.storybook.lib.object.VideoInformation;
import com.ssomai.android.scalablelayout.ScalableLayout;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class Mp4PlayerActivity extends BaseActivity implements OnBufferingUpdateListener, OnCompletionListener, OnPreparedListener, OnVideoSizeChangedListener, SurfaceHolder.Callback
{
	private static final int SECOND = 1000;
	private static final int DURATION_DISABLE_CAPTION = 3000;
	private static final int DURATION_DISAPPEAR_AUTOMATIC_HIDE = 3000;
	private static final int DURATION_SHOW_MENU_ANI = 500;
	private static final int DURATION_HIDE_MENU_ANI = 500;
	
	private static final int MESSAGE_PLAY_VIDEO 			= 0;
	private static final int MESSAGE_PROGRESS				= 1;
	private static final int MESSAGE_PROGRESS_END 			= 2;
	private static final int MESSAGE_NETWORK_DISCONNECT 	= 3;
	private static final int MESSAGE_NETWORK_RECONNECT 		= 4;
	private static final int MESSAGE_HIDE_MENU				= 5;
	private static final int MESSAGE_SHOW_MENU				= 6;
	private static final int MESSAGE_HIDE_CAPTION			= 7;
	private static final int MESSAGE_SHOW_CAPTION			= 8;
	private static final int MESSAGE_HIDE_PLAY_BUTTON		= 9;
	private static final int MESSAGE_SHOW_PLAY_BUTTON		= 10;
	private static final int MESSAGE_NEXT_PLAY_OPTION_SHOW 	= 11;
	private static final int MESSAGE_NEXT_PLAY_OPTION_HIDE 	= 12;
	private static final int MESSAGE_PAUSE					= 13;


	private static final int LOCAL_VIDEO 		= 0;
	private static final int STREAM_VIDEO 		= 1;
	
	private static final int CAPTION_END = 10000;

	
	private static final int CHECK_MAX_COUNT = 2;
	
	private static final String MEDIA = "media";
	private static final String TAG = "PlayerActivity";
	private int mVideoWidth;
	private int mVideoHeight;
	private int mCurrentPosition = 0;
	private long mSeekMax = 0;
	private MediaPlayer mMediaPlayer;
	private SurfaceView _Preview;
	private SurfaceHolder mHolder;
	private boolean isVideoSizeKnown = false;
	private boolean isVideoReadyToBePlayed = false;
	private boolean isPlayReadyComplete = false;
	private boolean isNearbyEndPoint = false;
	private boolean isPlayToPaused = false;
	

	private Timer mUiCurrentTimer;
	private String mFileUrl;
	
	private RelativeLayout _TopMenuLayout;
	private ScalableLayout _NextPlayLayout;
	private ScalableLayout _CaptionViewLayout;
	private ScalableLayout _CaptionButtonLayout;
	private ScalableLayout _CloseButtonLayout;
	private SeekBar _PlayerSeekBar = null;
	private TextView _mCurrentPlayTimeText;
	private TextView _mTotalPlayTimeText;
	private TextView _TitleText;

	private TextView _CaptionText;
	private ImageView _BackButton;
	private ImageView _PlayButton;
	private ImageView _CaptionButton;
	private ImageView _ReplayButton;
	private ImageView _NextPlayButton;
	private ImageView _AllPlayButton;
	
	private TextView _ReplayButtonText;
	private TextView _NextButtonText;
	private TextView _AllPlayButtonText;
	
	/** 이어서 보기 위한 포지션 */
	private int mCurrentContinuePosition = 0;
	private int mCurrentCaptionIndex = 0;
	private CaptionInformation mCaptionInformation = null;
	
	private boolean isCaptionComplete = false;
	private int mCompleteCheckCount = 0;
	private String mTitle;
	private SharedVideoInfo mVideoBase;
	/** 현재 플레이 되는 리스트의 현재 인덱스 */
	private int mCurrentPlayPosition = -1;
	
	private boolean isCaptionVisible = false;
	
	private Font mFont;
	
	/** 남은 편 모두 보기를 체크 */
	private boolean isAllPlayCheck = false;
	
	private LoadingDialog mLoadingDialog = null;
	
	private ArrayList<Integer> mPlayCompleteList 	= new ArrayList<Integer>();
	private ArrayList<Integer> mPlayedList			= new ArrayList<Integer>();			
	
	/**
	 * 메뉴바 사라지는 애니메이션 도중엔 메뉴바 클릭을 막는다.
	 */
	private boolean isPreventMenuClick = false;
	
	private int mVibratorPlayIndex  = 0;
	
	private Vibrator mVibrator;
	
	/**
	 * 진동 정보에 관련된 해쉬맵 </p> Key : 해당 무비의 index , Object : 진동정보를 담고 있는 Object
	 */
	private HashMap<Integer, VibratorObject> mVibratorListMap = null;
	private VibratorObject mVibratorObject = null;
	
	private boolean isPlayComplete = false;
	
	class UiTimerTask extends TimerTask
	{

		@Override
		public void run()
		{
			Mp4PlayerActivity.this.updateUI();
		}
		
	}
	
	Handler mPlayerHandler = new Handler()
	{
		public void handleMessage(Message msg)
		{
			long nIdx;
			
			switch(msg.what)
			{
			case MESSAGE_PLAY_VIDEO:
				playVideo(STREAM_VIDEO);
				break;
			 
			case MESSAGE_PROGRESS:
				isNearbyEndPoint = false;
				nIdx = msg.getData().getLong("progress");
				
				mCurrentPosition = (int) nIdx;
				_PlayerSeekBar.setSecondaryProgress((int) nIdx);
				break;
			case MESSAGE_PROGRESS_END:
				isNearbyEndPoint = true;
				nIdx = msg.getData().getLong("progress_end");
				Log.i("MESSAGE_PROGRESS_END : "+ nIdx);
				mCurrentPosition = (int) nIdx;			
				_PlayerSeekBar.setSecondaryProgress((int) nIdx);
				break;
			case MESSAGE_SHOW_MENU:
				Log.i("MESSAGE_SHOW_MENU : "+MESSAGE_SHOW_MENU);
				_TopMenuLayout.startAnimation(CommonUtils.getInstance(Mp4PlayerActivity.this).getAnimationShowTop(DURATION_SHOW_MENU_ANI));
				_TopMenuLayout.setVisibility(View.VISIBLE);
				break;
			case MESSAGE_HIDE_MENU:
				_TopMenuLayout.startAnimation(CommonUtils.getInstance(Mp4PlayerActivity.this).getAnimationHideTop(DURATION_HIDE_MENU_ANI));
				_TopMenuLayout.setVisibility(View.GONE);
				break;
			case MESSAGE_SHOW_CAPTION:
				_CaptionViewLayout.startAnimation(CommonUtils.getInstance(Mp4PlayerActivity.this).getAnimationShowBottom(DURATION_SHOW_MENU_ANI));
				_CaptionViewLayout.setVisibility(View.VISIBLE);
				break;
			case MESSAGE_HIDE_CAPTION:
				_CaptionViewLayout.startAnimation(CommonUtils.getInstance(Mp4PlayerActivity.this).getAnimationHideBottom(DURATION_SHOW_MENU_ANI));
				_CaptionViewLayout.setVisibility(View.GONE);
				break;
			case MESSAGE_SHOW_PLAY_BUTTON:
				_PlayButton.startAnimation(CommonUtils.getInstance(Mp4PlayerActivity.this).getAlphaAnimation(true,mAnimationListener));
				_PlayButton.setVisibility(View.VISIBLE);
				
				break;
			case MESSAGE_HIDE_PLAY_BUTTON:
				_PlayButton.startAnimation(CommonUtils.getInstance(Mp4PlayerActivity.this).getAlphaAnimation(false,mAnimationListener));
				_PlayButton.setVisibility(View.GONE);
				
				break;
			case MESSAGE_NEXT_PLAY_OPTION_SHOW:
				_NextPlayLayout.startAnimation(CommonUtils.getInstance(Mp4PlayerActivity.this).getAlphaAnimation(true,mNextLayoutListener));
				_NextPlayLayout.setVisibility(View.VISIBLE);
				break;
			case MESSAGE_NEXT_PLAY_OPTION_HIDE:
				_NextPlayLayout.startAnimation(CommonUtils.getInstance(Mp4PlayerActivity.this).getAlphaAnimation(false,mNextLayoutListener));
				_NextPlayLayout.setVisibility(View.GONE);
				break;
			case MESSAGE_PAUSE:
				mMediaPlayer.pause();
				break;
			}

		}
	};

	protected void updateUI()
	{
		_PlayerSeekBar.post(new Runnable()
		{
			public void run()
			{
				int pos = 0;
				
				try
				{
					if (mMediaPlayer != null && mMediaPlayer.isPlaying())
					{
						
						mCompleteCheckCount = 0;						
						pos = (int) (mSeekMax * (mMediaPlayer.getCurrentPosition() / SECOND)) / (mMediaPlayer.getDuration() / SECOND);
						_PlayerSeekBar.setProgress(pos);
						if (isNearbyEndPoint == true)
						{
							pos = pos + 300;
						}

						if (mCurrentPosition <= pos && isPlayReadyComplete == false)
						{
							setPlayerPlayStatus();
						}
						
						Object position = mMediaPlayer.getCurrentPosition();
						
						synchronized (position)
						{
							_mCurrentPlayTimeText.setText(CommonUtils.getInstance(Mp4PlayerActivity.this).getTime((Integer) position));
						}
						
						
						if(Common.IS_VIBRATE_ENABLE == true && mVibratorObject != null)
						{
							playVibrator();
						}
						
						checkCaptionText(true);
					}
					else if(mMediaPlayer.isPlaying() == false)
					{
						mCompleteCheckCount++;
						
						if(mCompleteCheckCount == CHECK_MAX_COUNT)
						{
							Log.i("Success onComplete ");
							pos = (int) (mSeekMax * (mMediaPlayer.getDuration() / SECOND)) / (mMediaPlayer.getDuration() / SECOND);
							_PlayerSeekBar.setProgress(pos);
							_mCurrentPlayTimeText.setText(CommonUtils.getInstance(Mp4PlayerActivity.this).getTime(mMediaPlayer.getDuration()));
							enableTimer(false);
							checkCaptionText(false);
						}
					}
				}catch(NullPointerException e)
				{
					Log.f("Error : "+e.getMessage());
				}
				
			}
		});
	}

	/**
	 * 
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle icicle)
	{
		Log.f("");
		super.onCreate(icicle);
		
		Log.i("Feature.IS_TABLET_MODE : "+ Feature.IS_TABLET_MODE);
		Log.i("Feature.IS_MINIMUM_SUPPORT_TABLET_RADIO_DISPLAY : "+ Feature.IS_MINIMUM_SUPPORT_TABLET_RADIO_DISPLAY);
		if(Feature.IS_TABLET_MODE && Feature.IS_MINIMUM_SUPPORT_TABLET_RADIO_DISPLAY)
		{
			setContentView(R.layout.player_not_support_display);
		}
		else
		{
			setContentView(R.layout.player);
		}
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		init();
	}
	
	private void init()
	{
		/** 홈버튼으로 나가있을때 앱을 인스톨시에 이전에 플레이한 정보가 남아있어서 문제가 발생하는것을 고치기 위해 */
		CommonUtils.getInstance(this).setSharedPreference(Common.PARAMS_CURRENT_PLAY_POSITION, -1);
		
        mPlayCompleteList 	= new ArrayList<Integer>();
        mPlayedList			= new ArrayList<Integer>();
        CommonUtils.getInstance(this).getWindowInfo();
        _PlayButton = (ImageView)findViewById(R.id.btn_play_pause);
        LayoutParams params = new LayoutParams(CommonUtils.getInstance(this).getPixel(446), CommonUtils.getInstance(this).getPixel(446));
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        _PlayButton.setLayoutParams(params);
   
        ScalableLayout topSeekLayout = (ScalableLayout)findViewById(R.id.top_seekbar_layout);
		params = (LayoutParams) topSeekLayout.getLayoutParams();
		params.topMargin = CommonUtils.getInstance(this).getPixel(100);
        topSeekLayout.setLayoutParams(params);
        
        initFont();
        
		_Preview = (SurfaceView) findViewById(R.id.sfview);
		_Preview.setOnTouchListener(mMenuControlTouchListener);
		mHolder = _Preview.getHolder();
		mHolder.addCallback(this);

		_TopMenuLayout 			= (RelativeLayout)findViewById(R.id.ll_player_top);
		/** TopBar의 메뉴버튼 나머지 영역을 터치시 VideoView 에 이벤트가 전달됨을 막기 위해 */
		_TopMenuLayout.setOnTouchListener(new OnTouchListener(){

			@Override
			public boolean onTouch(View v, MotionEvent event)
			{
				return true;
			}
			
		});
		_CaptionButtonLayout	= (ScalableLayout)findViewById(R.id.imv_caption_layout);
		_CloseButtonLayout		= (ScalableLayout)findViewById(R.id.imv_play_close_layout);
		
		_CaptionButtonLayout.setOnClickListener(mButtonClickListener);
		_CloseButtonLayout.setOnClickListener(mButtonClickListener);
		
		_NextPlayLayout			= (ScalableLayout)findViewById(R.id.replay_layout);
		_NextPlayLayout.setVisibility(View.GONE);
		_CaptionViewLayout 		= (ScalableLayout)findViewById(R.id.caption_layout);
		
		_mCurrentPlayTimeText 	= (TextView)findViewById(R.id.tv_curtime_view);
		_mTotalPlayTimeText 	= (TextView)findViewById(R.id.tv_totaltime_view);
		_TitleText				= (TextView)findViewById(R.id.tv_top_title);
		
		_BackButton				= (ImageView)findViewById(R.id.imv_play_close);
		//_BackButton.setOnClickListener(mButtonClickListener);
		
		_PlayButton.setOnClickListener(mButtonClickListener);
		_CaptionButton			= (ImageView)findViewById(R.id.imv_caption);
		//_CaptionButton.setOnClickListener(mButtonClickListener);
		
		_ReplayButtonText 		= (TextView)findViewById(R.id.replay_button_text);
		_NextButtonText			= (TextView)findViewById(R.id.next_play_button_text);
		_AllPlayButtonText			= (TextView)findViewById(R.id.all_play_button_text);
		_ReplayButton			= (ImageView)findViewById(R.id.replay_button);
		_ReplayButton.setOnClickListener(mButtonClickListener);
		_NextPlayButton			= (ImageView)findViewById(R.id.next_play_button);
		_NextPlayButton.setOnClickListener(mButtonClickListener);
		_AllPlayButton			= (ImageView)findViewById(R.id.all_play_button);
		_AllPlayButton.setOnClickListener(mButtonClickListener);
		_PlayerSeekBar 		= (SeekBar) findViewById(R.id.seekbar_play);

		_PlayerSeekBar.setThumbOffset(CommonUtils.getInstance(this).getPixel(0));
		_PlayerSeekBar.setProgress(0);
		_PlayerSeekBar.setPadding(0,0,0,0);
		_PlayerSeekBar.setSecondaryProgress(0);
		_PlayerSeekBar.setOnSeekBarChangeListener(seekBarChangeListener);

		LayerDrawable layerDrawable = (LayerDrawable)getResources().getDrawable(R.drawable.seekbar_thumb);
		GradientDrawable rectDrawable = (GradientDrawable)layerDrawable.findDrawableByLayerId(R.id._thumbRect);
		GradientDrawable circleDrawable = (GradientDrawable)layerDrawable.findDrawableByLayerId(R.id._thumbCircle);
		rectDrawable.setSize(CommonUtils.getInstance(this).getPixel(60), CommonUtils.getInstance(this).getPixel(60));
		circleDrawable.setSize(CommonUtils.getInstance(this).getPixel(55), CommonUtils.getInstance(this).getPixel(55));

		
		_CaptionText = (TextView)findViewById(R.id.caption_text);
		
		mCurrentPlayPosition = getIntent().getIntExtra("currentPlayIndex", -1);
		
		initCaptionScaleSize();
		mVibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);

		setupVibratorInformation();
		settingVideoBaseInformation();
		
		if((Boolean) CommonUtils.getInstance(this).getSharedPreference(Common.PARAMS_CAPTION_STATUS, Common.TYPE_PARAMS_BOOLEAN) == true)
		{
			isCaptionVisible = true;
		}

		checkPlay(StorybookTempleteAPI.PATH_MP4+ mVideoBase.getVideoInfoList().get(mCurrentPlayPosition).getVideoUrl(),
				mVideoBase.getVideoInfoList().get(mCurrentPlayPosition).getTitle());
		
	}
	
	
    private void initCaptionScaleSize()
    {
    	if(InformationTemplete.IS_1LINE_CAPTION_MODE)
    	{
    		ImageView _CaptionBackGround		= (ImageView)findViewById(R.id.caption_background);
    		_CaptionViewLayout.setScaleHeight(100);
    		_CaptionViewLayout.moveChildView(_CaptionBackGround, 0, 0, 1920, 100);
    		_CaptionViewLayout.moveChildView(_CaptionText, 0, 0, 1920, 100);
    		_CaptionViewLayout.setScale_TextSize(_CaptionText, 52);
    	}
    }

	
	/**
	 * 현재 미디어 플레이 시간을 비교하여 제일 최근의 진동 플레이 시간으로 맞춘다.
	 * @return
	 */
	private int getVibratorCurrentIndex()
	{
		int result = 0;
		for(int i = 0 ; i < mVibratorObject.vibrator_list.size(); i++)
		{
			float visibleTime = Float.parseFloat(mVibratorObject.vibrator_list.get(i).start_time);
			if( visibleTime <= (float)mMediaPlayer.getCurrentPosition()/1000.0f)
			{
				result++;
			}
		}
		
		return result;
	}
	
	
    private void setupVibratorInformation()
    {
		try
		{
			VibratorBaseObject mVibratorBaseObject = (VibratorBaseObject) CommonUtils.getInstance(this).convertObjectFromFile(StorybookTempleteAPI.PATH_VIBRATOR_ROOT+ Common.FILE_VIBRATOR_INFORMATION, VibratorBaseObject.class);
			
			mVibratorListMap = new HashMap<Integer, VibratorObject>();
			for(int i = 0 ; i < mVibratorBaseObject.movie_list.size() ; i ++)
			{
				mVibratorListMap.put(mVibratorBaseObject.movie_list.get(i).index, mVibratorBaseObject.movie_list.get(i));
			}
		}catch(Exception e)
		{
			Log.f("Message : "+ e.getMessage());
			e.printStackTrace();
		}
    }
    
    private VibratorObject getCurrentVibratorObject(int currentIndex)
    {
    	if(mVibratorListMap.containsKey(currentIndex))
    	{
    		return mVibratorListMap.get(currentIndex);
    	}
    	
    	return null;
    }
    
    private void showTempleteAlertDialog(int type, int buttonType, String message)
    {
    	TempleteAlertDialog dialog = new TempleteAlertDialog(this, message);
    	dialog.setDialogMessageSubType(type);
    	dialog.setButtonText(buttonType);
    	dialog.setDialogListener(mDialogListener);
    	dialog.show();
    }
	
	/**
	 * 밀리세컨드 단위로 체크하여 진동 시간이 되면 해당 패턴에 맞게 진동을 준다.
	 */
	private void playVibrator()
	{	
		if(mVibratorPlayIndex >= mVibratorObject.vibrator_list.size())
		{
			return;
		}
		
		try
		{
			float visibleTime = Float.parseFloat(mVibratorObject.vibrator_list.get(mVibratorPlayIndex).start_time);

			if( visibleTime <= (float)mMediaPlayer.getCurrentPosition()/1000.0f)
			{
				String[] pattenList = mVibratorObject.vibrator_list.get(mVibratorPlayIndex).vibrate_pattern.split(",");
				
				
				
				long[] patten_vibrator = new long[pattenList.length+1];
				patten_vibrator[0]=0;
				for(int i = 0; i < pattenList.length; i++)
				{
					patten_vibrator[i+1] = Long.parseLong(pattenList[i]);
				}
				
				Log.f("Vibartor Status : [position] = "+ mVibratorPlayIndex+", [patten] = "+ mVibratorObject.vibrator_list.get(mVibratorPlayIndex).vibrate_pattern);
				mVibrator.vibrate(patten_vibrator, -1);
				
				mVibratorPlayIndex++;
			}
		}catch(ArrayIndexOutOfBoundsException e)
		{
			Log.f("Message : "+ e.getMessage());
			return;
		}

	}
	
	private void cancleVibrator()
	{
		if(mVibrator == null)
		{
			return;
		}
		
		if(mVibrator.hasVibrator())
		{
			mVibrator.cancel();
		}
	}
	
	private void checkPlay(String filePath, String title)
	{
		Log.f("filePath : "+filePath+", title : "+title);
		isPlayReadyComplete = false;
		mCurrentCaptionIndex = 0;
		mFileUrl = filePath;		
		mTitle	= title;
		
		if(_NextPlayLayout.getVisibility() == View.VISIBLE)
		{
			setNextLayout(false);
		}
		
	

		showLoadingDialog();
		isPlayReadyComplete = true;
		
		
		File captionFile = new File(StorybookTempleteAPI.PATH_JSON+ mVideoBase.getVideoInfoList().get(mCurrentPlayPosition).getCaptionUrl());
		if(captionFile.exists() == true)
		{
			mCaptionInformation = (CaptionInformation) CommonUtils.getInstance(this).convertObjectFromFile(captionFile.getAbsolutePath(), CaptionInformation.class);
			isCaptionComplete = false;
		}
		else
		{
			Log.f("mCaptionInformation not exist");
		}
		
		
		mPlayerHandler.sendEmptyMessageDelayed(MESSAGE_PLAY_VIDEO,1000);
		setTitle(mTitle);
		setVisibleCaption(isCaptionVisible);

		GoogleAnalyticsHelper.getInstance(this).sendCurrentAppView(InformationTemplete.ANALYTICS_ACTIVITY_PLAYER+" : "+mVideoBase.getVideoInfoList().get(mCurrentPlayPosition).getPurchaseCode()+" Item "+ InformationTemplete.ANALYTICS_ACTION_PLAY);
		if(mPlayedList.contains(mCurrentPlayPosition) == false)
		{
			mPlayedList.add(mCurrentPlayPosition);
		}
	}
	
	private void initFont()
	{
		mFont = Font.getInstance(this);
		((TextView)findViewById(R.id.tv_top_title)).setTypeface(mFont.getRobotoMedium());
		((TextView)findViewById(R.id.tv_curtime_view)).setTypeface(mFont.getRobotoBold());
		((TextView)findViewById(R.id.tv_totaltime_view)).setTypeface(mFont.getRobotoBold());
		((TextView)findViewById(R.id.caption_text)).setTypeface(mFont.getRobotoBold());
		((TextView)findViewById(R.id.replay_button_text)).setTypeface(mFont.getRobotoBold());
		((TextView)findViewById(R.id.next_play_button_text)).setTypeface(mFont.getRobotoBold());
		((TextView)findViewById(R.id.all_play_button_text)).setTypeface(mFont.getRobotoBold());
		
	}
	
	
	
	private void settingVideoBaseInformation()
	{
		
		String fileString = FileUtils.getStringFromFile(StorybookTempleteAPI.PATH_VIDEO_INFORMATION_ROOT + Common.FILE_NEW_VIDEO_INFORMATION);
		try
		{
			mVideoBase = new Gson().fromJson(fileString, SharedVideoInfo.class);
		}catch(Exception e)
		{
			Log.f("Error : "+e.getMessage());
		}
	}
	
	

	/**
	 * 100 밀리 세컨드 단위로 UI 를 체크한다.
	 * @param isStart
	 */
	private void enableTimer(boolean isStart)
	{
		if(isStart)
		{
			if(mUiCurrentTimer == null)
			{
				mUiCurrentTimer = new Timer();
				mUiCurrentTimer.schedule(new UiTimerTask(), 100, 100);
			}
		}
		else
		{
			if(mUiCurrentTimer != null)
			{
				mUiCurrentTimer.cancel();
				mUiCurrentTimer = null;
			}
		}
	}
	
	/**
	 * 자막을 검사하여 현재 플레이시간에 따라 보여지게한다.
	 * @param isTextVisible TRUE: 자막 보이기,  FALSE: 자막 사라짐
	 */
	private void checkCaptionText(boolean isTextVisible)
	{
		if(isTextVisible == true)
		{
			if(mCurrentCaptionIndex >= mCaptionInformation.caption.size() || mCurrentCaptionIndex == CAPTION_END)
			{
				_CaptionViewLayout.setVisibility(View.GONE);
				return;
			}
			
			if(isTimeForCaption() == true)
			{
				_CaptionText.setText(mCaptionInformation.caption.get(mCurrentCaptionIndex).text);
			}
			
		}
		else
		{			
			_CaptionText.setText("");
		}
		
		if(isTextVisible == true)
		{
			if(isTimeForCaption() == true)
			{
				mCurrentCaptionIndex++;
			}
		}
		
	}
	
	/**
	 * 캡션에 대한 정보 처리 타이밍인지 확인 하는 메소드
	 * @return
	 */
	private boolean isTimeForCaption()
	{
		try
		{
			if(mCurrentCaptionIndex >= mCaptionInformation.caption.size())
			{
				return false;
			}

			float visibleTime = Float.valueOf((mCaptionInformation.caption.get(mCurrentCaptionIndex).start_time));
			if( visibleTime <= (float)mMediaPlayer.getCurrentPosition()/1000.0f)
			{
				return true;
			}
			
		}catch(ArrayIndexOutOfBoundsException e)
		{
			return false;
		}

		
		return false;
	}
	
	private int getCaptionCurrentIndex()
	{
		float currentMediaTime = (float)mMediaPlayer.getCurrentPosition()/1000.0f;
		float startTime = 0L;

		startTime = Float.valueOf((mCaptionInformation.caption.get(0).start_time));
		if(startTime > (float)mMediaPlayer.getCurrentPosition()/1000.0f)
		{
			return 0;
		}
		
		for(int i = 0; i < mCaptionInformation.caption.size(); i++)
		{
			startTime 	= Float.valueOf((mCaptionInformation.caption.get(i).start_time));
			if( startTime >= currentMediaTime)
			{
				return i;
			}
		}
		
		
		return -1;
	}
	
	
	private void showLoadingDialog()
	{
		if(mLoadingDialog != null)
		{
			mLoadingDialog.dismiss();
			mLoadingDialog = null;
		}
		
		mLoadingDialog = new LoadingDialog(this, LoadingDialog.TYPE_ROTATION);
		mLoadingDialog.show();
	}
	
	private void hideLoadingDialog()
	{
		if(mLoadingDialog != null)
		{
			mLoadingDialog.dismiss();
			mLoadingDialog = null;
		}
	}

	public void playVideo(Integer Media)
	{
		doCleanUp();
		releaseMediaPlayer();
		try
		{
			// Create a new media player and set the listeners
			
			mMediaPlayer = new MediaPlayer();
			FileInputStream fis = new FileInputStream(mFileUrl);
			FileDescriptor fd = fis.getFD();
			mMediaPlayer.setDataSource(fd);
			fis.close();
			mMediaPlayer.prepareAsync();

			mMediaPlayer.setDisplay(mHolder);	

			mMediaPlayer.setOnPreparedListener(this);
			
			mMediaPlayer.setOnBufferingUpdateListener(this);
			mMediaPlayer.setOnCompletionListener(this);

			mMediaPlayer.setOnErrorListener(new OnErrorListener()
			{

				public boolean onError(MediaPlayer mp, int what, int extra)
				{

					Log.f( "what : " + what + " , " + "extra :" + extra);
					
					return false;
				}
			});

			mMediaPlayer.setOnInfoListener(new OnInfoListener()
			{

				public boolean onInfo(MediaPlayer mp, int what, int extra)
				{

					return false;
				}

			});

			mMediaPlayer.setOnSeekCompleteListener(new OnSeekCompleteListener()
			{

				public void onSeekComplete(MediaPlayer mp)
				{
					mMediaPlayer.start();
				}

			});

		}
		catch (Exception e)
		{
			Log.f("Error : " + e.getMessage());
			e.printStackTrace();
		}
		
		CommonUtils.getInstance(this).setSharedPreference(Common.PARAMS_BEFORE_WATCHED_MOVIE, mCurrentPlayPosition);
	}
	
	/**
	 * 동영상 플레이가 끝나고 다음 보기 레이아웃을 보여줄것인지, 가릴것인지 결정
	 * @param isVisible
	 */
	private void setNextLayout(boolean isVisible)
	{
		if(isVisible == true)
		{
			
			if(getNextPlayIndex(mCurrentPlayPosition) == -1)
			{
				Log.f("남은 편 모두 보기 끝");
				((ScalableLayout)_NextPlayLayout).moveChildView(_ReplayButton, 762, 22, 396, 138);
				((ScalableLayout)_NextPlayLayout).moveChildView(_ReplayButtonText, 882, 22, 212, 138);
				_NextPlayButton.setVisibility(View.GONE);
				_AllPlayButton.setVisibility(View.GONE);
				_NextButtonText.setVisibility(View.GONE);
				_AllPlayButtonText.setVisibility(View.GONE);
			}
			else
			{
                Log.f("한편 보기 끝");
				((ScalableLayout)_NextPlayLayout).moveChildView(_ReplayButton, 336, 22, 362, 138);
				((ScalableLayout)_NextPlayLayout).moveChildView(_ReplayButtonText, 456, 22, 212, 138);
				_NextPlayButton.setVisibility(View.VISIBLE);
				_AllPlayButton.setVisibility(View.VISIBLE);
				_NextButtonText.setVisibility(View.VISIBLE);
				_AllPlayButtonText.setVisibility(View.VISIBLE);
			}
			mPlayerHandler.sendEmptyMessageDelayed(MESSAGE_NEXT_PLAY_OPTION_SHOW, 500);
		}
		else
		{
			mPlayerHandler.sendEmptyMessage(MESSAGE_NEXT_PLAY_OPTION_HIDE);
		}
	}
	
	/**
	 * 다음에 플레이할 인덱스를 리턴한다. 플레이할 리스트가 없으면 -1 을 리턴
	 * @param currentPosition
	 * @return
	 */
	private int getNextPlayIndex(int currentPosition)
	{
		if(currentPosition +1 >= mVideoBase.getVideoInfoList().size())
		{
			return -1;
		}
		
		for(int i = currentPosition + 1; i < mVideoBase.getVideoInfoList().size(); i++)
		{
			if(mVideoBase.getVideoInfoList().get(i).getStatus() == VideoInformation.STATUS_DOWNLOAD_COMPLETE || mVideoBase.getVideoInfoList().get(i).getStatus() == VideoInformation.STATUS_PLAY_COMPLETE)
			{
				return i;
			}
		}
		
		return -1;
	}
	
	private void setTitle(String title)
	{
		_TitleText.setText(title);
	}

	private void initTimeText()
	{
		_mCurrentPlayTimeText.setText(CommonUtils.getInstance(this).getTime(0));
		_mTotalPlayTimeText.setText(CommonUtils.getInstance(this).getTime(mMediaPlayer.getDuration()));
	}

	private void setSurfaceBackground(Drawable dw, int w, int h)
	{
		if (w != 0)
		{
			ViewGroup.LayoutParams Param = _Preview.getLayoutParams();
			Param.height = CommonUtils.getInstance(this).getRatioViewHeight(w, h, _Preview.getWidth(), _Preview.getHeight()) + 30;
			_Preview.setLayoutParams(Param);
		}

		_Preview.setBackgroundDrawable(dw);
	}
	
	public void onBufferingUpdate(MediaPlayer arg0, int percent)
	{
		Log.d(TAG, "onBufferingUpdate percent:" + percent);

	}
	
	@Override
	public void onCompletion(MediaPlayer arg0)
	{
		Log.f("onCompletion called ");
		
		if(_PlayButton.getVisibility() == View.VISIBLE)
		{
			setVisiblePlayButton(false);
		}
		
		enableTimer(false);
		checkCaptionText(false);
		
		if(mPlayCompleteList.contains(mCurrentPlayPosition) == false)
		{
			mPlayCompleteList.add(mCurrentPlayPosition);
		}
		
		
		if(isAllPlayCheck == true && getNextPlayIndex(mCurrentPlayPosition) != -1)
		{
			
			mCurrentPlayPosition = getNextPlayIndex(mCurrentPlayPosition);
			Log.f("남은편 모두보기 클릭. 다음 포지션 : "+ mCurrentPlayPosition);
			checkPlay(
					StorybookTempleteAPI.PATH_MP4+ mVideoBase.getVideoInfoList().get(mCurrentPlayPosition).getVideoUrl(),
					mVideoBase.getVideoInfoList().get(mCurrentPlayPosition).getTitle());
		}
		else
		{
			
			if(isCaptionVisible == true)
			{
				mPlayerHandler.sendEmptyMessage(MESSAGE_HIDE_CAPTION);
			}
			if(isPlayComplete == false)
			{
				setNextLayout(true);
			}
			
		}
		
		isPlayComplete = true;
		
	}

	public void onVideoSizeChanged(MediaPlayer mp, int width, int height)
	{
		Log.f("onVideoSizeChanged called");
		if (width == 0 || height == 0)
		{
			Log.f("invalid video width(" + width + ") or height(" + height + ")");
			return;
		}
		isVideoSizeKnown = true;
		mVideoWidth = width;
		mVideoHeight = height;
		if (isVideoReadyToBePlayed && isVideoSizeKnown)
		{
			startVideoPlayback();
		}
	}

	@Override
	public void onPrepared(MediaPlayer mediaplayer)
	{
		Log.f("onPrepared called");
		if(_NextPlayLayout.getVisibility() != View.VISIBLE)
		{
			isPlayComplete = false;
		}
		
		Log.f("mCurrentPlayPosition : "+mCurrentPlayPosition);
		try
		{
			mVibratorObject = getCurrentVibratorObject(mCurrentPlayPosition);
			mVibratorPlayIndex = 0;
		}catch(Exception e)
		{
			Log.f("Error : "+e.getMessage());
		}
		
		
		mCurrentContinuePosition = (Integer) CommonUtils.getInstance(this).getSharedPreference(Common.PARAMS_CURRENT_PLAY_POSITION, Common.TYPE_PARAMS_INTEGER);
		
		Log.f("CurrentContinuePosition : "+mCurrentContinuePosition);
		mSeekMax = mMediaPlayer.getDuration();
		_PlayerSeekBar.setMax((int)mSeekMax);
		
		
		if(isPlayComplete == true)
		{
			mMediaPlayer.seekTo((int) mSeekMax);
			mMediaPlayer.pause();
			hideLoadingDialog();
		}
		else
		{
			if(mCurrentContinuePosition == -1)
			{
				mMediaPlayer.seekTo(0);
				mMediaPlayer.start();
			}
			else
			{
				
				int pos = (int) (mSeekMax * (mCurrentContinuePosition / SECOND)) / (mMediaPlayer.getDuration() / SECOND);
				_PlayerSeekBar.setProgress(pos);
				mMediaPlayer.seekTo(mCurrentContinuePosition);
				mMediaPlayer.start();
			}

			
			setSurfaceBackground(null,0,0);
			initTimeText();
			enableTimer(true);
			hideLoadingDialog();
			isVideoReadyToBePlayed = true;
			if (isVideoReadyToBePlayed && isVideoSizeKnown)
			{
				startVideoPlayback();
			}
		}
	
	}

	@Override
	public void surfaceChanged(SurfaceHolder surfaceholder, int i, int j, int k)
	{
		Log.f("surfaceChanged called");

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder surfaceholder)
	{
		Log.f("surfaceDestroyed called");

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder)
	{
		Log.f("surfaceCreated called");

		mHolder = holder;
		

	}

	@Override
	public void onResume()
	{
		Log.f("");
		super.onResume();
		
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		registerReceiver(mNetworkChangeReceiver, intentFilter);
		
		
		if(isPlayToPaused == true && isPlayComplete == false)
		{
			Log.f("플레이 도중 Pause 되어 Resume");
			setStatusPlay();
			isPlayToPaused = false;
			showLoadingDialog();
			mPlayerHandler.sendEmptyMessageDelayed(MESSAGE_PLAY_VIDEO,1000);
		}
		else if(isPlayComplete == true)
		{
			Log.f("플레이 다하고 난 후 Pause 된후 Resume");
			showLoadingDialog();
			mPlayerHandler.sendEmptyMessageDelayed(MESSAGE_PLAY_VIDEO,1000);
		}
	}

	@Override
	protected void onPause()
	{
		Log.f("");
		super.onPause();
		
		isPlayToPaused = true;

		if (_PlayerSeekBar.getSecondaryProgress() == _PlayerSeekBar.getMax() || isPlayComplete == true)
		{
			CommonUtils.getInstance(this).setSharedPreference(Common.PARAMS_CURRENT_PLAY_POSITION, -1);
		}
		else
		{
			CommonUtils.getInstance(this).setSharedPreference(Common.PARAMS_CURRENT_PLAY_POSITION, mMediaPlayer.getCurrentPosition());
		}
		
		unregisterReceiver(mNetworkChangeReceiver);
		
		if(_PlayButton.getVisibility() == View.VISIBLE && _TopMenuLayout.getVisibility() == View.VISIBLE)
		{
			mPlayerHandler.sendEmptyMessage(MESSAGE_HIDE_MENU);
			mPlayerHandler.sendEmptyMessage(MESSAGE_HIDE_PLAY_BUTTON);
		}
		
		
		releaseMediaPlayer();
		doCleanUp();

		enableTimer(false);
	}
	
	@Override
	protected void onUserLeaveHint()
	{
		Log.f("");
		super.onUserLeaveHint();
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
		CommonUtils.getInstance(this).setSharedPreference(Common.PARAMS_CURRENT_PLAY_POSITION, -1);
		isPlayToPaused = false;
		
		releaseMediaPlayer();
		doCleanUp();
		cancleVibrator();
		if(mPlayerHandler != null)
		{
			mPlayerHandler.removeCallbacksAndMessages(null);
			mPlayerHandler = null;
		}
		
		

	}
	

	private void setPlayerPlayStatus()
	{
		try
		{
			if(mMediaPlayer.isPlaying() == true)
			{
				Log.f("Player Pause");
				mMediaPlayer.pause();
				_PlayButton.setImageDrawable(getResources().getDrawable(R.drawable.selector_player_play_button));
				
				enableTimer(false);
			}
			else
			{
				Log.f("Player Play");
				mMediaPlayer.start();
				_PlayButton.setImageDrawable(getResources().getDrawable(R.drawable.selector_player_pause_button));
				
				if(mUiCurrentTimer == null)
				{
					mUiCurrentTimer = new Timer();
					mUiCurrentTimer.schedule(new UiTimerTask(), 0, 100);
				}
				
			}
		}catch(Exception e)
		{
			Log.f("Error : "+ e.getMessage());
		}

	}
	
	private void setVisiblePlayButton(boolean isShow)
	{
		Log.f("isShow : "+isShow);
		if(mPlayerHandler.hasMessages(MESSAGE_SHOW_PLAY_BUTTON) || mPlayerHandler.hasMessages(MESSAGE_HIDE_PLAY_BUTTON))
		{
			return;
		}
		
		if(isShow)
		{
			mPlayerHandler.sendEmptyMessage(MESSAGE_SHOW_PLAY_BUTTON);
		}
		else
		{
			mPlayerHandler.sendEmptyMessage(MESSAGE_HIDE_PLAY_BUTTON);
		}
	}
	
	private void setVisibleMenu(boolean isShow)
	{
		Log.f("isShow : "+isShow);
		if(mPlayerHandler.hasMessages(MESSAGE_SHOW_MENU) || mPlayerHandler.hasMessages(MESSAGE_HIDE_MENU))
		{
			return;
		}
		
		if(isShow)
		{
			mPlayerHandler.sendEmptyMessage(MESSAGE_SHOW_MENU);
		}
		else
		{
			mPlayerHandler.sendEmptyMessage(MESSAGE_HIDE_MENU);
		}
	}
	
	private void setVisibleCaption(boolean isShow)
	{
		Log.f("isShow : "+isShow);
		if(mPlayerHandler.hasMessages(MESSAGE_SHOW_CAPTION) || mPlayerHandler.hasMessages(MESSAGE_HIDE_CAPTION))
		{
			return;
		}
		
		if(isShow == true)
		{
			_CaptionButton.setImageDrawable(getResources().getDrawable(R.drawable.btn_text_down));
			mPlayerHandler.sendEmptyMessage(MESSAGE_SHOW_CAPTION);
		}
		else
		{
			_CaptionButton.setImageDrawable(getResources().getDrawable(R.drawable.btn_text_normal));
			mPlayerHandler.sendEmptyMessage(MESSAGE_HIDE_CAPTION);
		}
	}

	private void releaseMediaPlayer()
	{
		Log.f("");
		if (mMediaPlayer != null)
		{
			mMediaPlayer.pause();
			mMediaPlayer.stop();
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
	}

	private void doCleanUp()
	{
		Log.f("");
		mVideoWidth = 0;
		mVideoHeight = 0;
		isVideoReadyToBePlayed = false;
		isVideoSizeKnown = false;
	}

	private void startVideoPlayback()
	{
		Log.f("");
		mHolder.setFixedSize(mVideoWidth, mVideoHeight);
		mMediaPlayer.start();
	}
	
	

	@Override
	public void onBackPressed()
	{
		Log.f("");
		Intent intent = new Intent();
		intent.putIntegerArrayListExtra(Common.INTENT_PARAMS_PLAY_COMPLETE_POSITION, mPlayCompleteList);
		intent.putIntegerArrayListExtra(Common.INTENT_PARAMS_PLAYED_POSITION, mPlayedList);
		setResult(RESULT_OK,intent);
		
		super.onBackPressed();
	}


	/**
	 * 자동으로 3초 후에 메뉴를 올린다.
	 */
	private void hideMenuToAuto()
	{
		removeHideMenuMessageToAuto();

		mPlayerHandler.sendEmptyMessageDelayed(MESSAGE_HIDE_MENU, DURATION_DISAPPEAR_AUTOMATIC_HIDE);
		mPlayerHandler.sendEmptyMessageDelayed(MESSAGE_HIDE_PLAY_BUTTON, DURATION_DISAPPEAR_AUTOMATIC_HIDE);
	}
	
	private void removeHideMenuMessageToAuto()
	{
		if(mPlayerHandler.hasMessages(MESSAGE_HIDE_MENU))
		{
			mPlayerHandler.removeMessages(MESSAGE_HIDE_MENU);
		}
		
		if(mPlayerHandler.hasMessages(MESSAGE_HIDE_PLAY_BUTTON))
		{
			mPlayerHandler.removeMessages(MESSAGE_HIDE_PLAY_BUTTON);
		}
	}
	
	/**
	 * 멈춰있을 때 Seek 하거나 Home 으로 나갓다가 다시 들어올시에 자동 재생을 할때 아이콘을 정상적으로 표시해주기위해 사용
	 */
	private void setStatusPlay()
	{
		try
		{
			if(mMediaPlayer.isPlaying() == false)
			{
				_PlayButton.setImageDrawable(getResources().getDrawable(R.drawable.selector_player_pause_button));
			}
		}catch(NullPointerException e)
		{
			_PlayButton.setImageDrawable(getResources().getDrawable(R.drawable.selector_player_pause_button));
		}
		
	}
	
	
	
	private BroadcastReceiver mNetworkChangeReceiver = new BroadcastReceiver(){

		@Override
		public void onReceive(Context context, Intent intent)
		{
			if(NetworkUtil.getConnectivityStatus(context) == NetworkUtil.TYPE_NOT_CONNECTED)
			{
				mPlayerHandler.sendEmptyMessage(MESSAGE_NETWORK_DISCONNECT);
			}
			else
			{
				mPlayerHandler.sendEmptyMessage(MESSAGE_NETWORK_RECONNECT);
			}
			
		}
		
	};
	
	private OnClickListener mButtonClickListener = new OnClickListener()
	{

		@Override
		public void onClick(View v)
		{
			switch(v.getId())
			{
			case R.id.imv_play_close_layout:
				
				if(isPreventMenuClick == true)
				{
					return;
				}
				Log.f("Button Player Close");
				Intent intent = new Intent();
				intent.putIntegerArrayListExtra(Common.INTENT_PARAMS_PLAY_COMPLETE_POSITION, mPlayCompleteList);
				intent.putIntegerArrayListExtra(Common.INTENT_PARAMS_PLAYED_POSITION, mPlayedList);
				setResult(RESULT_OK,intent);
				finish();
				break;
			case R.id.btn_play_pause:
				if(isPreventMenuClick == true)
				{
					return;
				}
				Log.f("Button Player Pause");
				setPlayerPlayStatus();
				hideMenuToAuto();
				cancleVibrator();
				break;
			case R.id.imv_caption_layout:
				if(isPreventMenuClick == true)
				{
					return;
				}
				isCaptionVisible = !isCaptionVisible;
				Log.f("Button Caption Enable : "+ isCaptionVisible);
				CommonUtils.getInstance(Mp4PlayerActivity.this).setSharedPreference(Common.PARAMS_CAPTION_STATUS, isCaptionVisible);
				setVisibleCaption(isCaptionVisible);
				Log.i("");
				hideMenuToAuto();
				break;
			case R.id.next_play_button:
				mCurrentPlayPosition = getNextPlayIndex(mCurrentPlayPosition);
				Log.f("Button Next Play : "+ mCurrentPlayPosition);
				checkPlay(StorybookTempleteAPI.PATH_MP4+ mVideoBase.getVideoInfoList().get(mCurrentPlayPosition).getVideoUrl(),
						mVideoBase.getVideoInfoList().get(mCurrentPlayPosition).getTitle());
				break;
			case R.id.replay_button:
				Log.f("Button RePlay : "+ mCurrentPlayPosition);
				checkPlay(StorybookTempleteAPI.PATH_MP4+ mVideoBase.getVideoInfoList().get(mCurrentPlayPosition).getVideoUrl(),
						mVideoBase.getVideoInfoList().get(mCurrentPlayPosition).getTitle());
				break;
			case R.id.all_play_button:
				isAllPlayCheck = true;
				mCurrentPlayPosition = getNextPlayIndex(mCurrentPlayPosition);
				Log.f("Button All Play : "+ mCurrentPlayPosition);
				checkPlay(StorybookTempleteAPI.PATH_MP4+ mVideoBase.getVideoInfoList().get(mCurrentPlayPosition).getVideoUrl(),
						mVideoBase.getVideoInfoList().get(mCurrentPlayPosition).getTitle());
				break;
			
			}

		}
		
	};
	
	private OnTouchListener mMenuControlTouchListener = new OnTouchListener()
	{

		@Override
		public boolean onTouch(View v, MotionEvent event)
		{
			if(MotionEvent.ACTION_DOWN == event.getAction())
			{
				
				if(_NextPlayLayout.getVisibility() == View.VISIBLE)
				{
					return false;
				}
				 
				if(_TopMenuLayout.getVisibility() == View.VISIBLE && _PlayButton.getVisibility() == View.VISIBLE)
				{
					Log.i("");
					isPreventMenuClick = true;
					removeHideMenuMessageToAuto();
				}
				
				if(_TopMenuLayout.getVisibility() == View.VISIBLE)
				{
					setVisibleMenu(false);
					
				}
				else
				{
					setVisibleMenu(true);
					
				}
				
				if(_PlayButton.getVisibility() == View.VISIBLE)
				{
					setVisiblePlayButton(false);
				}
				else
				{
					setVisiblePlayButton(true);
				}
				
				if(_TopMenuLayout.getVisibility() == View.GONE && _PlayButton.getVisibility() == View.GONE)
				{
					Log.i("");
					isPreventMenuClick = true;
					hideMenuToAuto();
					
				}
				
				
				
			}
			return true;
		}
		
	};
	
	
	
	private SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener()
	{
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
		{
		}

		public void onStartTrackingTouch(SeekBar seekBar)
		{
			if(isPreventMenuClick == true)
			{
				return;
			}
			
			enableTimer(false);
			_CaptionText.setText("");
			removeHideMenuMessageToAuto();
			cancleVibrator();
		}

		public void onStopTrackingTouch(SeekBar seekBar)
		{
			if(isPreventMenuClick == true)
			{
				return;
			}
			
			
			int position;

			
			if (seekBar.getProgress() > mCurrentPosition && isPlayReadyComplete == false)
			{
				int pos = mCurrentPosition - SECOND;
				position = (int) ((pos * (mMediaPlayer.getDuration() / SECOND)) / mSeekMax) * SECOND;
				mMediaPlayer.seekTo(position);
			}
			else
			{
				if (seekBar.getProgress() > mCurrentPosition - 100)
				{
					position = (int) (((seekBar.getProgress() - 100) * (mMediaPlayer.getDuration() / SECOND)) / mSeekMax) * SECOND;
				}
				else
				{
					position = (int) ((seekBar.getProgress() * (mMediaPlayer.getDuration() / SECOND)) / mSeekMax) * SECOND;
				}

				mMediaPlayer.seekTo(position);
			}
		//	_CaptionText.setText("");
			/**
			 * Seek 할시에 말하는 도중에 중간에 멈추면 글이 긴시간동안 안보이는 경우를 대비하여 추가 : 삭제가능성있는 코드
			 */
			mCurrentCaptionIndex = 0;
			if(getCaptionCurrentIndex() != -1)
			{
				mCurrentCaptionIndex = getCaptionCurrentIndex();
				
				if(isCaptionVisible == true && _CaptionViewLayout.getVisibility() == View.GONE)
				{
					mPlayerHandler.sendEmptyMessage(MESSAGE_SHOW_CAPTION);
				}
			}
			else 
			{
				mCurrentCaptionIndex = CAPTION_END;
			}
			
			if(Common.IS_VIBRATE_ENABLE == true && mVibratorObject != null)
			{
				mVibratorPlayIndex= getVibratorCurrentIndex();
			}
			
			
			setStatusPlay();

			enableTimer(true);
			hideMenuToAuto();
			
			if(_NextPlayLayout.getVisibility() == View.VISIBLE)
			{
				setNextLayout(false);
			}
		}
	};
	
	/**
	 * 플레이버튼이 GONE 된 상태에서도 플레이버튼이 눌러지는 현상 방지
	 */
	private AnimationListener mAnimationListener = new AnimationListener()
	{
		@Override
		public void onAnimationStart(Animation animation)
		{
			isPreventMenuClick = true;
		}
		@Override
		public void onAnimationEnd(Animation animation)
		{
			_PlayButton.clearAnimation();
			_TopMenuLayout.clearAnimation();
			isPreventMenuClick = false;
		}

		@Override
		public void onAnimationRepeat(Animation animation)
		{}
		
	};
	
	
	
	
	private AnimationListener mNextLayoutListener = new AnimationListener()
	{
		@Override
		public void onAnimationStart(Animation animation){}
		
		@Override
		public void onAnimationRepeat(Animation animation){}
		
		@Override
		public void onAnimationEnd(Animation animation)
		{
			_NextPlayLayout.clearAnimation();
		}
	};

	private DialogListener mDialogListener = new DialogListener()
	{
		@Override
		public void onItemClick(int messageType, Object sendObject){}

		@Override
		public void onItemClick(int messageButtonType, int subMessageType, Object sendObject){}
		
	};
	
	
}