package com.littlefox.storybook.fcm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.littlefox.logmonitor.Log;
import com.littlefox.storybook.wizardandcat.IntroActivity;
import com.littlefox.storybook.wizardandcat.R;


public class LittlefoxFirebaseMessagingService extends FirebaseMessagingService
{
    final String TAG = "LittlefoxFirebaseMessagingService";

	public static final String PARAMS_FIREBASE_PUSH_TOKEN			= "firebase_push_token";


	@Override
	public void onNewToken(String token)
	{
		super.onNewToken(token);
		Log.f("token : "+ token);
	}



	/**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {


        Log.i(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0)
        {
            Log.i(TAG, "Message data payload: " + remoteMessage.getData());

            showNotification(remoteMessage.getData().get("msg"));
        }
        else
        {
            Log.f("Data Empty.");
        }
    }

    private void showNotification(String message)
    {

        final String CHANNEL_ID = "littlefox_channel_id";
        final String CHANNEL_NAME = "littlefox_channel_name";

        Log.f("message : "+message);
		Intent intent = new Intent(this, IntroActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		NotificationCompat.Builder notificationBuilder = null;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
		{
			NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME,
					NotificationManager.IMPORTANCE_DEFAULT);
			notificationChannel.enableLights(true);
			notificationChannel.setLightColor(Color.BLUE);
			notificationManager.createNotificationChannel(notificationChannel);

			notificationBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(this, CHANNEL_ID);
		}
		else
		{
			notificationBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(this);
		}

		Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		Notification notification = notificationBuilder
				.setSmallIcon(R.mipmap.ic_launcher)
				.setContentTitle(getResources().getString(R.string.app_name))
				.setContentText(message)
				.setAutoCancel(true)
				.setSound(soundUri)
				.setContentIntent(pendingIntent)
				.build();

		notificationManager.notify(0, notification);
	}

}