package com.cyr.chatogo.Activities;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.cyr.chatogo.API.ServerAPI;
import com.cyr.chatogo.Extras.MessageAdapter;
import com.cyr.chatogo.Extras.DataBaseHandler;
import com.cyr.chatogo.Models.User;
import com.cyr.chatogo.Models.Message;
import com.cyr.chatogo.R;

import java.util.ArrayList;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;

public class MessageActivity extends AppCompatActivity implements ServerAPI.UserInfoCallback{

    private ServerAPI serverApi;
    private DataBaseHandler dataBaseHandler;
    private User userInfo;
    private MessageAdapter adapter;
    private ListView listView;
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private String message;
    private EditText msgText;
    private Context context;
    private String username = "guest";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        //Getting username
        username = getIntent().getStringExtra("username");
        setTitle(username);

        //variable initialization
        context = getApplicationContext();
        prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        editor = prefs.edit();
        serverApi = ServerAPI.getInstance(context);
        serverApi.initToast();
        dataBaseHandler = DataBaseHandler.getInstance(context);
        msgText = (EditText)findViewById(R.id.text_box);
        listView = (ListView) findViewById(R.id.list);
        listView.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
        listView.setStackFromBottom(true);
        setListView(dataBaseHandler.getAllMessages(username));

        //Request users info if not available or remove notification
        if((userInfo = dataBaseHandler.getUser(username)) == null)
            serverApi.getUserInfo("GetUser", username);
        else
            cancelNotificaton();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_delete, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id){
            case R.id.action_delete:
                dataBaseHandler.deleteMessages(username);
                setListView(dataBaseHandler.getAllMessages(username));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        serverApi.setUserInfoListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        serverApi.setUserInfoListener(null);
    }

    /**
     * On message send click:
     * Sends message request to Server Api
     * @param view
     */
    public void onMessageSend(View view){
        if((message = msgText.getText().toString()).trim().length() > 0) {
            System.out.println(msgText.getText().toString().length() + "  ");
            msgText.setText("");
            if (userInfo != null && dataBaseHandler.getUser(userInfo.getUsername()) == null)
                dataBaseHandler.addUser(userInfo);
            serverApi.sendMessageToUser(message, username);
        }
    }

    /**
     * Set up List View and Adapter
     * @param messageList messages to display
     */
    public void setListView(ArrayList<Message> messageList){
        if(adapter == null) {
            adapter = new MessageAdapter(this, messageList);
            listView.setAdapter(adapter);
        }else {
            adapter.clear();
            adapter.addAll(messageList);
            adapter.notifyDataSetChanged();
        }
    }

    public void cancelNotificaton(){
        //Cancel notifications
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(userInfo.getId());
    }
    /**
     * Server Api callback on user
     * info received
     * @param userInfo user info email,.....
     */
    @Override
    public void onUserInfo(User userInfo) {
        this.userInfo = userInfo;
        cancelNotificaton();
    }

    /**
     * Firebase listener when user
     * has replied
     * @param message
     */
    @Override
    public void onUserMsgReplied(Message message) {
        dataBaseHandler.addMessage(message);
        rx.Observable.just(0)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Integer integer) {
                        setListView(dataBaseHandler.getAllMessages(username));
                    }
                });

    }

    /**
     * Server Api callback on message send
     * @param content message text
     */
    @Override
    public void onNotifyMsgSend(int code,String content) {
        if(code == 200) {
            Long timeStampLong = System.currentTimeMillis() / 1000;
            String timeStamp = timeStampLong.toString();
            dataBaseHandler.addMessage(new Message(0, prefs.getString("signin", "guest"), username, prefs.getString("signin", "guest"), content, timeStamp));
            setListView(dataBaseHandler.getAllMessages(username));
        }else
            Toast.makeText(context,"User unavailable: Message " + content + " not sent",Toast.LENGTH_SHORT).show();
    }
}
