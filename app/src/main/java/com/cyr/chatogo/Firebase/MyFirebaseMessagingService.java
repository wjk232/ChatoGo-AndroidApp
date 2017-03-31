/**
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cyr.chatogo.Firebase;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.cyr.chatogo.API.ServerAPI;
import com.cyr.chatogo.Activities.MessageActivity;
import com.cyr.chatogo.Extras.DataBaseHandler;
import com.cyr.chatogo.Extras.UsefulFunctions;
import com.cyr.chatogo.Models.Message;
import com.cyr.chatogo.Models.User;
import com.cyr.chatogo.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONObject;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.internal.util.UtilityFunctions;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        ServerAPI serverApi = ServerAPI.getInstance(getApplicationContext());
        DataBaseHandler dataBaseHandler = DataBaseHandler.getInstance(getApplicationContext());
        SharedPreferences prefs = getApplication().getSharedPreferences("settings", Context.MODE_PRIVATE);

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Long timeStampLong = System.currentTimeMillis()/1000;
            String timeStamp = timeStampLong.toString();
            //If there was a profile update
            if(remoteMessage.getData().get("type").equals("update")) {
                if(dataBaseHandler.getUser(remoteMessage.getData().get("username")) != null
                        && !remoteMessage.getData().get("username").equals(prefs.getString("signin","")))
                    serverApi.getUserInfo("Update",remoteMessage.getData().get("username"));
            }
            //User replied message
            else if(remoteMessage.getData().get("type").equals("user")){
                //Insert user info if not available in local database
                if (dataBaseHandler.getUser(remoteMessage.getData().get("username")) == null)
                    serverApi.getUserInfo("Add", remoteMessage.getData().get("username"));

                //Send message to onreplied listener
                serverApi.onReplied(new Message(0, prefs.getString("signin", "guest"), remoteMessage.getData().get("username"),
                        remoteMessage.getData().get("username"), remoteMessage.getData().get("message"), timeStamp));

                //Send notification on listener null
                if (serverApi.getUserInfoCallback() == null)
                    sendNotification(Integer.parseInt(remoteMessage.getData().get("id")),remoteMessage.getData().get("message"), remoteMessage.getData().get("username"));
            }
            //New messages on chatroom
            else {
                //If user is signed in
                if(prefs.getString("signin","").length() > 0 && serverApi.getChatroomMsgCallbacks().size() > 0) {
                    String[] location = remoteMessage.getData().get("location").split(",");
                    String[] userLocation = dataBaseHandler.getUser(prefs.getString("signin", "")).getLocation().split(",");

                    //If user has same location sync chatroom
                    if(userLocation[0].contains(location[0]) || userLocation[1].contains(location[1]))
                        serverApi.getChatroomMessages();

                }
            }
        }else if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     * @param messageBody FCM message body received.
     */
    private void sendNotification(int id, String messageBody, String username) {
        Intent intent = new Intent(this, MessageActivity.class);
        intent.putExtra("username",username);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.chatogo_icon)
                .setContentTitle(username)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(id /* ID of notification */, notificationBuilder.build());
    }

}
