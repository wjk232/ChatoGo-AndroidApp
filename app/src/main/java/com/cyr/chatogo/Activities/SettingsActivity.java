package com.cyr.chatogo.Activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cyr.chatogo.API.ServerAPI;
import com.cyr.chatogo.Extras.DataBaseHandler;
import com.cyr.chatogo.Extras.UsefulFunctions;
import com.cyr.chatogo.Models.User;
import com.cyr.chatogo.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class SettingsActivity extends AppCompatActivity implements ServerAPI.UsersCallback,ServerAPI.AdrressCallback{

    private ServerAPI serverApi;
    private DataBaseHandler dataBaseHandler;
    private SharedPreferences prefs;
    private Context context;
    private ImageView profileImage;
    private TextView usernameTextView;
    private EditText locationEditView;
    private Toast toast;
    private User user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //Initialize variables
        context = getApplicationContext();
        prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        dataBaseHandler = DataBaseHandler.getInstance(context);
        serverApi = ServerAPI.getInstance(context);
        serverApi.initToast();
        //UI variables
        profileImage = (ImageView)findViewById(R.id.settings_image);
        usernameTextView = (TextView)findViewById(R.id.settings_username);
        locationEditView = (EditText)findViewById(R.id.settings_location);
        toast = Toast.makeText(context,"",Toast.LENGTH_SHORT);
        displayData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        serverApi.setUsersListener(this);
        serverApi.setAdrressCallback(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        serverApi.setUsersListener(null);
        serverApi.setAdrressCallback(null);
    }

    /**
     * Display profile image and usernameTextView
     */
    public void displayData(){
        user = dataBaseHandler.getUser(prefs.getString("signin",""));
        if(!user.getImage().equals("none"))
            profileImage.setImageBitmap(UsefulFunctions.ourInstance(context).stringToBitmap(user.getImage()));
        usernameTextView.setText(user.getUsername());
        locationEditView.setHint(user.getLocation());
    }

    /**
     * On profile image click:
     * Method to get a picture from device
     * @param view
     */
    public void onImageClick(View view){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
            intent.setType("image/*");
            startActivityForResult(intent, 400);
        }
    }

    /**
     * On button change click:
     * Method to request address
     * @param view
     */
    public void onLocationChange(View view){
        if(!locationEditView.getText().toString().contains(user.getLocation()))
            serverApi.getAddress(locationEditView.getText().toString());
    }

    /**
     * Getting file for profile picture
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data != null) {
            String path;

            if((path = UsefulFunctions.ourInstance(this.getBaseContext()).getPath(context, data.getData())) != null) {
                final File file = new File(path);
                if (file != null) {
                    byte[] buffer = new byte[0];

                    ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
                    BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                    Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(),bmOptions);
                    int width = bitmap.getWidth();
                    int height = bitmap.getHeight();
                    int newWidth =(width-height)/2;
                    int newHeight = (height-width)/2;

                    if (width > height)
                        bitmap = Bitmap.createBitmap(bitmap, newWidth,0,height, height);
                    else if(height > width)
                        bitmap = Bitmap.createBitmap(bitmap, 0, newHeight,width, width);
                    else
                        bitmap = Bitmap.createBitmap(bitmap, newWidth, newHeight,width, height);

                    bitmap = Bitmap.createScaledBitmap(bitmap,400,350,true);
                    bitmap.compress(Bitmap.CompressFormat.JPEG,80, byteArrayOutputStream);
                    buffer = byteArrayOutputStream.toByteArray();

                    try {
                        byteArrayOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    serverApi.updateUser(Base64.encodeToString(buffer,Base64.DEFAULT).trim(),user.getLocation());
                }

            }else
                Toast.makeText(context,"Unsupported File Type",Toast.LENGTH_SHORT).show();
        }
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
        if(code == 200)
            serverApi.updateUser(user.getImage(),location);
        else{
            toast.setText(message);
            toast.show();
        }
    }

    /**
     * Server callback on user
     * profile pic update
     * @param code 200 if successful 400 otherwise
     * @param message message from server
     */
    @Override
    public void onUsersProfileUpdate(int code, String message) {
        Toast.makeText(context,message,Toast.LENGTH_SHORT).show();
        if(code == 200){
            serverApi.notifyUsers();
            displayData();
        }else{
            toast.setText(message);
            toast.show();
        }
    }

    @Override
    public void onUserRequest(User user) {
        //do nothing
    }

    @Override
    public void onUsersReceived(ArrayList<User> users) {
        //do nothing
    }
}
