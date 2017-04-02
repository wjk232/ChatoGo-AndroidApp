package com.cyr.chatogo.Activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.cyr.chatogo.API.ServerAPI;
import com.cyr.chatogo.Extras.DataBaseHandler;
import com.cyr.chatogo.Extras.UsefulFunctions;
import com.cyr.chatogo.Models.User;
import com.cyr.chatogo.R;
import com.google.firebase.messaging.FirebaseMessaging;
import java.io.IOException;
import java.io.InputStream;

public class AuthenticationActivity extends AppCompatActivity implements ServerAPI.AuthenticationCallback , ServerAPI.AdrressCallback{

    private ServerAPI serverApi;
    private Toast toast;
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private DataBaseHandler dataBaseHandler;
    private UsefulFunctions usefulFunctions;
    private Context context;
    private EditText usernameEditView;
    private EditText passwordEditView;
    private EditText locationEditView;
    private Button signInButton;
    private Button registerButton;
    private View progressView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        //UI variables
        usernameEditView = (EditText) findViewById(R.id.username);
        passwordEditView = (EditText) findViewById(R.id.password);
        locationEditView = (EditText) findViewById(R.id.location);
        signInButton = (Button) findViewById(R.id.login_button);
        registerButton = (Button) findViewById(R.id.register_button);
        progressView = findViewById(R.id.login_progress);

        //Initialize variables
        context = getApplicationContext();
        serverApi = ServerAPI.getInstance(context);
        serverApi.initToast();
        dataBaseHandler = DataBaseHandler.getInstance(context);
        usefulFunctions = UsefulFunctions.ourInstance(getApplicationContext());
        toast = Toast.makeText(context, "", Toast.LENGTH_SHORT);
        prefs = getSharedPreferences("settings", Context.MODE_PRIVATE);
        editor = prefs.edit();

        //If user is already signed in go to main activity
        if (prefs.getString("signin", "").length() >= 1) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
        editor.putString("chatroom", "nearme");
        editor.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        serverApi.setAuthenticationListener(this);
        serverApi.setAdrressCallback(this);
        //get permissions
        getPermission();
    }

    @Override
    protected void onStop() {
        super.onStop();
        serverApi.setAuthenticationListener(null);
        serverApi.setAdrressCallback(null);
    }

    @Override
    public void onBackPressed() {
        if(signInButton.getVisibility() == View.GONE){
            signInButton.setVisibility(View.VISIBLE);
            locationEditView.setVisibility(View.GONE);
        }else
            super.onBackPressed();
    }

    /**
     * On signin click
     * @param view
     */
    public void onLogIn(View view){
        if (usefulFunctions.isDeviceOnline()) {
            if (checkInput("login")) {
                //Disable buttons
                signInButton.setClickable(false);
                registerButton.setClickable(false);
                progressView.setVisibility(View.VISIBLE);
                //Login Request
                serverApi.userLogin(usernameEditView.getText().toString(), passwordEditView.getText().toString());
            }
        }else{
            toast.setText("No Internet Connection!");
            toast.show();
        }
    }

    /**
     * On register click:
     * Send user info to server for
     * registration
     * @param view
     */
    public void onRegister(View view){
        if (signInButton.getVisibility() == View.GONE) {
            if (usefulFunctions.isDeviceOnline()) {
                if (checkInput("register")) {
                    String []locationparse = locationEditView.getText().toString().split(" ");
                    String location = "";
                    //Disable buttons
                    signInButton.setClickable(false);
                    registerButton.setClickable(false);
                    progressView.setVisibility(View.VISIBLE);
                    //Request address
                    for(int i = 0;i < locationparse.length;i++)
                        location += locationparse[i] + "+";
                    serverApi.getAddress(location);
                }
            } else{
                toast.setText("No Internet Connection!");
                toast.show();
            }
        } else {
            signInButton.setVisibility(View.GONE);
            locationEditView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Check if input is valid
     * email@mail.com .....
     * @return true if valid false otherwise
     */
    public boolean checkInput(String option){
        if(usernameEditView.getText().toString().length() < 1) {
            toast.setText("Username is required");
            toast.show();
            return false;
        }else if(passwordEditView.getText().toString().length() < 1) {
            toast.setText("Password is required");
            toast.show();
            return false;
        }else if(option.equals("register") && locationEditView.getText().toString().length() < 1){
            toast.setText("Location is required");
            toast.show();
            return false;
        }else if(option.equals("register") && usernameEditView.getText().toString().contains(" ")) {
            toast.setText("Username must not contain spaces");
            toast.show();
            return false;
        }else
            return true;
    }

    /**
     * Server Api callback on user creation
     * @param code 200 if successful 400 otherwise
     * @param status message status ex successful
     * @param user user info email,username.....
     */
    @Override
    public void onUserCreate(int code, String status, User user) {
        registerButton.setClickable(true);
        signInButton.setClickable(true);
        progressView.setVisibility(View.GONE);
        if(code == 201){
            Log.d("LOG","User create status " + status);
            dataBaseHandler.addUser(user);
            locationEditView.setVisibility(View.GONE);
            signInButton.setVisibility(View.VISIBLE);
            signInButton.setClickable(true);
        }else{
            Log.d("LOG","User create status " + status);
            usernameEditView.setText("");
        }

        passwordEditView.setText("");
        locationEditView.setText("");
        toast.setText(status);
        toast.show();
    }

    /**
     * Server Api callback on user Signin
     * @param code 200 if successful 400 otherwise
     * @param status message status ex. successful
     * @param user user info email,username.....
     * @param token token
     */
    @Override
    public void onUserLogin(int code, String status, User user, String token) {
        progressView.setVisibility(View.GONE);
        if(code == 200){
            //If user not in local database add it
            if(dataBaseHandler.getUser(user.getUsername()) == null){
                dataBaseHandler.addUser(user);
            }
            editor.putString("signin", user.getUsername());
            editor.putString("token",token);
            editor.commit();
            usernameEditView.setText("");
            //subcribing to topics(chatrooms)
            FirebaseMessaging.getInstance().subscribeToTopic("nearme");
            FirebaseMessaging.getInstance().subscribeToTopic("region");
            FirebaseMessaging.getInstance().subscribeToTopic("broadcast");
            //MainActivity intent
            Intent intent = new Intent(this,MainActivity.class);
            startActivity(intent);
            finish();
        }else{
            signInButton.setClickable(true);
            registerButton.setClickable(true);
        }

        passwordEditView.setText("");
        toast.setText(status);
        toast.show();
    }

    /**
     * Server Api callback on getting
     * well-structured location address string
     * @param code 200 if successful 400 otherwise
     * @param location location address string
     * @param message error message
     */
    @Override
    public void onAddressReceived(int code, String location, String message) {
        if(code == 200){
            serverApi.createUser(Character.toUpperCase(usernameEditView.getText().toString().charAt(0)) + usernameEditView.getText().toString().substring(1),
                    passwordEditView.getText().toString(), getImage(), location);
        }else{
            signInButton.setClickable(true);
            registerButton.setClickable(true);
            progressView.setVisibility(View.GONE);
            toast.setText(message);
            toast.show();
        }
    }

    /**
     * Get default image
     * @return image as string
     */
    public String getImage(){
        InputStream is;
        byte[] buffer = new byte[0];
        try {
            is = context.getAssets().open("images/contacts.png");
            buffer = new byte[is.available()];
            is.read(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Base64.encodeToString(buffer,Base64.DEFAULT).trim();
    }

    /**
     * Check if User granted the permissions
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 200: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        editor.putString("permission", "true");
                        editor.commit();
                    } else {
                        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                            editor.putString("permission", "false");
                            editor.commit();
                        }
                    }
                }
                return;
            }
            default:
                return;
        }
    }

    /**
     * Get Permission from user for application use:
     * permission to access files on device
     * and location
     */
    public void getPermission(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED && prefs.getString("permission", "").equals("")) {

            // Show information about permission
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                toast.setText("Permission Needed to Add Files from Device.");
                toast.show();
            }
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    200);
        }
    }
}
