package com.cyr.chatogo.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.util.LruCache;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cyr.chatogo.API.ServerAPI;
import com.cyr.chatogo.Extras.DataBaseHandler;
import com.cyr.chatogo.Extras.UsefulFunctions;
import com.cyr.chatogo.Models.User;
import com.cyr.chatogo.R;

import java.util.ArrayList;

public class ContactsActivity extends AppCompatActivity implements ServerAPI.UsersCallback {

    private ServerAPI serverApi;
    private LruCache<String, Bitmap> memoryCache;
    private DataBaseHandler dataBaseHandler;
    private ArrayList<User> usersList;
    private ArrayList<String> usernameList;
    private EditText searchText;
    private ArrayAdapter<User> adapter;
    private ListView listView;
    private TextView title;
    private ProgressBar usersProgress;
    private Context context;
    private SharedPreferences prefs;
    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        //Initialize variables
        context = getApplicationContext();
        prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        serverApi = ServerAPI.getInstance(context);
        serverApi.initToast();
        serverApi.setUsersListener(this);
        serverApi.getAllUsers();
        searchText = (EditText) findViewById(R.id.search_box);
        listView = (ListView)findViewById(R.id.users_list);
        usersProgress = (ProgressBar)findViewById(R.id.users_progress);
        title = (TextView)findViewById(R.id.title);
        usernameList = new ArrayList<>();
        setTextListener();
        listViewListener();

        if(!UsefulFunctions.ourInstance(context).isDeviceOnline())
            usersProgress.setVisibility(View.GONE);
    }

    /**
     * On search click:
     * Request user from server
     * @param view
     */
    public void onSearch(View view){
        if(searchText.getText().toString().length() > 1){
            serverApi.getUserInfo("Request",searchText.getText().toString());
        }
    }

    /**
     * Adds text change listener
     * to searchtext
     */
    public void setTextListener(){
        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(count == 0){
                    title.setVisibility(View.VISIBLE);
                    adapter.clear();
                    adapter.addAll(new ArrayList<User>(usersList));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        serverApi.setUsersListener(null);
    }

    /**
     * Set up List View and Adapter
     */
    public void setListView(final ArrayList<User> usersList){
        if(adapter == null) {
            adapter = new ArrayAdapter<User>(this, R.layout.user_view,usersList) {
                @Override
                public View getView(final int position, View convertView, ViewGroup parent) {
                    View rowView = convertView;
                    if (rowView == null) {
                        rowView = getLayoutInflater().inflate(R.layout.user_view, parent, false);
                    }

                    //image and text for each file
                    //final TextView statusTextView = (TextView) rowView.findViewById(R.id.user_status);
                    final TextView usernameTextView = (TextView) rowView.findViewById(R.id.user_username);
                    final TextView locationTextView = (TextView) rowView.findViewById(R.id.user_location);
                    final ImageView imageView = (ImageView)rowView.findViewById(R.id.user_pic);

                    if(getItem(position).getImage().length() > 0 && !getItem(position).getImage().equals("none"))
                        imageView.setImageBitmap(UsefulFunctions.ourInstance(context).stringToBitmap(getItem(position).getImage()));
                    locationTextView.setText(getItem(position).getLocation());
                    usernameTextView.setText(getItem(position).getUsername());
                    //statusTextView.setText(getItem(position).getStatus());

                    return rowView;
                }
            };
            listView.setAdapter(adapter);
        }else {
            adapter.clear();
            adapter.addAll(usersList);
            adapter.notifyDataSetChanged();
        }
    }

    public void listViewListener(){
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(!prefs.getString("signin","guest").equals((adapter.getItem(position)).getUsername())) {
                    Intent intent = new Intent(context, MessageActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    intent.putExtra("username", adapter.getItem(position).getUsername());
                    startActivity(intent);
                }
            }
        });
    }

    /**
     *  Server Api callback on
     *  request user info
     * @param user user information(username, image....)
     */
    @Override
    public void onUserRequest(User user) {
        title.setVisibility(View.GONE);
        adapter.clear();
        adapter.add(user);
    }

    /**
     * Server Api callback on users info received
     * @param users all users
     */
    @Override
    public void onUsersReceived(ArrayList<User> users) {
        usersProgress.setVisibility(View.GONE);
        usersList = users;
        setListView(new ArrayList<User>(usersList));
    }

    @Override
    public void onUsersProfileUpdate(int code, String message) {
        //do nothing
    }
}
