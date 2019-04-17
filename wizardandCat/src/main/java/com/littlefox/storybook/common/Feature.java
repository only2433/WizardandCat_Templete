package com.littlefox.storybook.common;

public class Feature {

	public static final int APP_STORE_GOOGLE 	= 0;
	public static final int APP_STORE_SAMSUNG 	= 1;
	public static final int APP_STORE_CHINA 	= 2;
	
	public static int CURRENT_APP_STORE = APP_STORE_GOOGLE;
	
	public static final boolean USE_GOOGLE_ANALYTICS = false;
	
	public static boolean IS_TABLET_MODE = false;
	
	/**
	 * 모바일웹에서 실행하여 실행되면 쿠폰다이얼로그에 쿠폰번호가 입력되게 
	 */
	public static boolean IS_WEB_EXCUTE_WITH_COUPON = false;
	
	/**
	 * 4:3 의 비율인 디스플레이 인지 확인하는 변수 .<p>TRUE = 4:3</p><p>FALSE = 16:9</p>
	 */
	public static boolean IS_MINIMUM_SUPPORT_TABLET_RADIO_DISPLAY = false;
}
