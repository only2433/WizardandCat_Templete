package com.littlefox.storybook.common;

import java.io.File;

import com.littlefox.storybook.lib.common.Common;

import android.os.Environment;

public class InformationTemplete
{
	/**
	 * 상단 소개에  보일 캐릭터 최대 개수
	 * 해당부분을 수정하면 arrays.xml의 peopleArray(캐릭터 이름) , idArray(이미지 파일 이름) 를 같이 수정해주어야한다.
	 */
	public static final int MAX_INTRODUCTION_CHARACTER_SIZE = 7;
	
	
	public static final String COLOR_THUMBNAIL_BACKGROUND 		= "#fff97f";
	public static final String COLOR_INTRODUCTION_BACKGROUND 	= "#aa33e6";
	public static final String COLOR_DEVICE_CODE_TEXT			= "#bfba60";
	public static final String COLOR_INTRODUCTION_TEXT			= "#ffffff";
	
	/**
	 * 앱 이름
	 */
	public static final String APP_NAME = "WizardAndCat";
	/**
	 * 어플리케이션의 패키지명 : 반드시 변경
	 */
	public static final String PACKAGE_NAME 				= "com.littlefox.storybook.wizardandcat";
	
	/**
	 * 영상이 저장될 위치 : 반드시 변경
	 */
	public static final String MP4_FILE_PATH 				= Environment.getExternalStorageDirectory() + File.separator + "Littlefox"+ File.separator+"Storybook"+File.separator+"WizardAndCat"+File.separator;
	
	/**
	 * 로그 파일 저장 명  : 반드시 변경
	 */
	public static final String LOG_FILE						= "story_book_wizard_and_cat.txt";
	
	/**
	 * API 서버와 통신하기 위한 기본 URI
	 */
	public static final String BASE_URI 					= "https://app.littlefox.com/api_storybook/";
	
	/** 
	 * 앱 별점 주기 링크 : 뒷부분 패키지 변경 해야함
	 */
	public static final String APP_LINK = "https://play.google.com/store/apps/details?id=com.littlefox.storybook.wizardandcat";
	
	
	/**
	 * 인앱 아이템 가격
	 */
	public static final String PRICE_ONE_ITEM = "$ 0.99";
	public static final String PRICE_ALL_ITEM = "$ 5.99";
	
	/**
	 * API 서버 통신을 위한 Header 값
	 */
	public static final String HTTP_HEADER_APP_NAME = "LF_App_Storybook_WizardandCat";
	
	/**
	 * Google Analytics : 등록하면 변경해야함
	 */
	public static final String GOOGLE_ANALYTICS_PROPERTY_ID = "UA-37277849-26";
	
	/**
	 * 하나 아이템의 SKU
	 */
	public static final String ONE_PAY_ITEM_SKU = "lf_sb_wac_1";
	
	/**
	 * 전체 아이템의 SKU
	 */
	public static final String ALL_PAY_ITEM_SKU = "lf_sb_wac_all";
	
	/**
	 * 결제 관련 IN APP BILLING KEY : 앱을 베타테스트로 Publish 한 후에 해당 어플리케이션 정보에서 Services & APIs 항목의  Licensing & IN-APP Billing 의 키를 복사하여 바꾼다.
	 */
	public static final String IN_APP_BILLING_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAuCsEyk+5Q99nvDQ6VnMLHSEBxkLw2fMCB8dNqiU7Dorvg63ribXlxxKE/5UewCw12NQmW85+kypLM2XhtxT3X9vdu0aTKus0gtpYlpdGReRILkt6Rb/UDnT2OW1UlHsPFp9gNXfVg6V3B3LMgw9Osh4NKsWYWl/BoZaAH4u5j3U0GFGX6Xz5AK+Nw7o8gHm4Nkl1bNC8uVS3Z81FzZgZ3bjYVQe4hOPx++rKi3h2Bjz0mJDMLKZTGulBFhyE5FvBs6VI+LosC+3Yb5+l/V6G03MAQIJNnEhxmtTj37lc5ivrNQIAozFJrHU9w8rFgq0NN5VrLzU2Jy8AYe5JI9YJfQIDAQAB";

	
	
	// -------------- APP을 Google API Console 에 등록하여 해당 키와 아이디를 얻어낸다. 개발자에게 요청 ------------------------ //
	/**
	 * 서버 어플리케이션의 서버키 
	 */
	public static final String SERVER_KEY = "AIzaSyCB1EK9_S8gKO73xR2Ct6K_dOxGA4W5uYI";
											 
	
	/**
	 * Google API Console 에 등록한 Project ID
	 */
	public static final String SENDER_ID = "779281957676";
											 
	
	/** Google Analytics */
	public static final String ANALYTICS_ACTIVITY_INTRO  	= "Intro Activity";
	public static final String ANALYTICS_ACTIVITY_MAIN		= "Main Activity";
	public static final String ANALYTICS_ACTIVITY_PLAYER 	= "Player Activity";
	
	public static final String ANALYTICS_ACTION_PLAY		= "Play Video";
	public static final String ANALYTICS_ACTION_DOWNLOAD	= "Download Video";
	public static final String ANALYTICS_ACTION_PAID		= "Paid Video";
	
	public static final String ANALYTICS_ACTION_MENU_HOME 		= "LittleFox Home";
	public static final String ANALYTICS_ACTION_MENU_RATE 		= "Rate";
	public static final String ANALYTICS_ACTION_MENU_OTHER 		= "Other App";
	public static final String ANALYTICS_ACTION_MENU_PROMO_CODE = "Promo Code";
	public static final String ANALYTICS_ACTION_MENU_HELP 		= "Help";
	public static final String ANALYTICS_ACTION_MENU_RESTORE 	= "Restore";
	
	public static final String ANALYTICS_VIEW_DOWNLOAD_ITEM					= "컨텐츠 다운로드";
	public static final String ANALYTICS_VIEW_PAID_ITEM						= "컨텐츠 구매";
	public static final String ANALYTICS_VIEW_PLAY_VIDEO					= "컨텐츠 플레이";
	public static final String ANALYTICS_VIEW_MENU 							= "메뉴";
	public static final String ANALYTICS_VIEW_MENU_LITTLEFOX_HOME 			= "리틀팍스 홈";
	public static final String ANALYTICS_VIEW_MENU_OTHER 					= "다른앱 보기";
	public static final String ANALYTICS_VIEW_MENU_PROMO_CODE				= "프로모션 코드 사용";
	public static final String ANALYTICS_VIEW_MENU_CREDIT 					= "제작자 소개";
	public static final String ANALYTICS_VIEW_MENU_RATE 					= "별점 주기";
	public static final String ANALYTICS_VIEW_MENU_RESTORE 					= "구매 복원";
	public static final String ANALYTICS_VIEW_MENU_INQUIRE 					= "문의 하기";
	public static final String ANALYTICS_VIEW_RECOMMONAD_ICON_CLICK			= "추천 앱 클릭";
	
	public static final String ANALYTICS_CATEGORY_RECOMMONAD_ICON		= "추천 앱";
	
	public static final String ANALYTICS_ACTION_POPULAR_TAB				= "인기순";
	public static final String ANALYTICS_ACTION_NEWEST_TAB				= "최신순";
	public static final String ANALYTICS_ACTION_NAME_TAB				= "이름순";
	
	public static final String ANALYTICS_ACTION_EXCUTE 			= "실행";
	public static final String ANALYTICS_ACTION_APP_STORE_MOVE 	= "앱스토어 이동";
	
	/**
	 * 액션 스토리 모드를 테스트 하려면 true 아니면  false </p>해당 파일은 /Littlefox/Vibrator/vibrator_information.txt 경로에 넣어서 테스트 가능하다. 
	 */
	public static final boolean IS_VIBRATOR_TEST = false;
	
	/**
	 * 액션 스토리 모드를 사용하는 단행본인지, 아닌지 의 구분.</p>false로 할 경우 레이아웃에 노출되지 않으며, 실행되지 않는다.
	 */
	public static final boolean AVAILABLE_ACTION_STORY_MODE = false;
	
	/** 에피소드 인지 챕터 인지 구분 . 각각의 단행본 마다 그에 따른 구분이 필요함.*/
	public static final int DISTINGUISH_STORY_WORD = Common.STORY_TITLE_EPISODE;
	
	/** 동화의 총 몇편인지를 나타내는 변수. 구매 다이얼로그 메인 화면에 쓰인다. */
	public static final int MAX_AVAILABLE_FOR_PULCHASE_STORY_SIZE = 48;
	
	/** 기본은 2줄로 나오도록 표시. 1줄만 보여주어야 하는 단행본인지 의 여부</p>true 1줄 </p>false 2줄 */
	public static final boolean IS_1LINE_CAPTION_MODE = true;

}
