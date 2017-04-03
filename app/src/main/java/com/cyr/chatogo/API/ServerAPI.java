package com.cyr.chatogo.API;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Base64;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;
import com.cyr.chatogo.Extras.DataBaseHandler;
import com.cyr.chatogo.Extras.UsefulFunctions;
import com.cyr.chatogo.Models.User;
import com.cyr.chatogo.Models.Message;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;

import javax.crypto.Cipher;

/**
 * Created by roger on 3/10/2017.
 */
public class ServerAPI {
    private Context context;
    private ArrayList<ChatroomMsgCallback> chatroomMsgCallbacks;
    private AuthenticationCallback authenticationCallback;
    private UserInfoCallback userInfoCallback;
    private UsersCallback usersCallback;
    private LogoutCallback logoutCallback;
    private AdrressCallback adrressCallback;
    private DataBaseHandler dataBaseHandler;
    private UsefulFunctions usefulFunctions;
    private Toast toast;
    private static ServerAPI ourInstance;
    private SharedPreferences prefs;
    private RequestQueue requestQueue;
    private ArrayList<Message> messages;
    private ArrayList<User> users;

    private String makeURL(String... args){
        return "http://192.241.128.43/"+ TextUtils.join("/",args);
    }


    public static ServerAPI getInstance(Context context) {
        if(ourInstance==null){
            ourInstance = new ServerAPI(context);
        }
        return ourInstance;
    }

    private ServerAPI(Context context) {
        //Initialize variables
        this.context = context;
        prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        requestQueue = Volley.newRequestQueue(context);
        requestQueue.start();
        messages = new ArrayList<>();
        users = new ArrayList<>();
        chatroomMsgCallbacks = new ArrayList<>();
        dataBaseHandler = DataBaseHandler.getInstance(context);
        usefulFunctions = UsefulFunctions.ourInstance(context);
    }

    public void initToast(){
        if(this.toast == null)
            this.toast = Toast.makeText(context,"No Internet Connection!",Toast.LENGTH_SHORT);
    }
    private String getChatroom() {
        return prefs.getString("chatroom","nearme");
    }

    public void addMessagesListener(ChatroomMsgCallback messagesCallback) {
        this.chatroomMsgCallbacks.add(messagesCallback);
    }
    public void removeMessagesListener(ChatroomMsgCallback messagesCallback) {
        this.chatroomMsgCallbacks.remove(messagesCallback);
    }

    public ArrayList<ChatroomMsgCallback> getChatroomMsgCallbacks() {
        return this.chatroomMsgCallbacks;
    }

    public void setUserInfoListener(UserInfoCallback userInfoCallback) {
        this.userInfoCallback = userInfoCallback;
    }

    public UserInfoCallback getUserInfoCallback() {
        return userInfoCallback;
    }

    public void setUsersListener(UsersCallback usersCallback) {
        this.usersCallback = usersCallback;
    }

    public void setAuthenticationListener(AuthenticationCallback authenticationCallback) {
        this.authenticationCallback = authenticationCallback;
    }

    public void setAdrressCallback(AdrressCallback adrressCallback) {
        this.adrressCallback = adrressCallback;
    }

    public void setLogoutCallback(LogoutCallback logoutCallback) {
        this.logoutCallback = logoutCallback;
    }

    /**
     *  User Request:
     *  Get single user information
     *  such as email,username......
     * @param option
     * @param username
     */
    public void getUserInfo(final String option, String username){
        if(usefulFunctions.isDeviceOnline()) {
            String url = null;
            try {
                url = makeURL("api","users", URLEncoder.encode(username,"utf-8") + "?api_token=" + prefs.getString("token", ""));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            JsonRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        User user = new User(response.getInt("id"),
                                            response.getString("status"),
                                            response.getString("username"),
                                            response.getString("location"),
                                            response.getString("profile_pic"));

                        if(option.equals("Update"))
                            dataBaseHandler.updateUser(user);
                        else if(option.equals("Add"))
                            dataBaseHandler.addUser(user);
                        else if(option.equals("Request"))
                            usersCallback.onUserRequest(user);
                        else
                            userInfoCallback.onUserInfo(user);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    System.out.println("That didn't work! " + error.getMessage());
                }
            });
            jsonRequest.setRetryPolicy(new DefaultRetryPolicy(
                    3000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            // Add the request to the RequestQueue.
            requestQueue.add(jsonRequest);
        }else
            toast.show();
    }

    /**
     *  Notify Request:
     *  Notify users on info updates
     */
    public void notifyUsers(){
        if(usefulFunctions.isDeviceOnline()) {
            String url = null;
            try {
                url = makeURL("api","firebase","notify" + "?api_token=" + prefs.getString("token", "")
                        +"&username="+ URLEncoder.encode(prefs.getString("signin",""),"utf-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            JsonRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    System.out.println("It worked! ");
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    System.out.println("That didn't work! " + error);
                }
            });
            jsonRequest.setRetryPolicy(new DefaultRetryPolicy(
                    3000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            // Add the request to the RequestQueue.
            requestQueue.add(jsonRequest);
        }else
            toast.show();
    }

    /**
     *  Users Request:
     *  Get all users info
     *  such as email,username......
     */
    public void getAllUsers(){
        if(usefulFunctions.isDeviceOnline()) {
            final String url = makeURL("api","users" + "?api_token=" + prefs.getString("token", ""));
            JsonArrayRequest jsonRequest = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    try {
                        users.clear();
                        for (int i = response.length()-1; i >= 0 ; i--) {
                            users.add(new User( response.getJSONObject(i).getInt("id"),
                                                response.getJSONObject(i).getString("status"),
                                                response.getJSONObject(i).getString("username"),
                                                response.getJSONObject(i).getString("location"),
                                                response.getJSONObject(i).getString("profile_pic")));
                        }

                        usersCallback.onUsersReceived(users);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    System.out.println("That didn't work! " + error);
                    usersCallback.onUsersReceived(users);
                }
            });
            jsonRequest.setRetryPolicy(new DefaultRetryPolicy(
                    3000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            // Add the request to the RequestQueue.
            requestQueue.add(jsonRequest);
        }else
            toast.show();
    }

    /**
     * Messages Request:
     * Get massages depending on location
     */
    public void getChatroomMessages(){
        if(usefulFunctions.isDeviceOnline()) {
            String url = null;
            try {
                url = makeURL("api","messages", getChatroom() + "?api_token=" + prefs.getString("token", "")
                        + "&clientID=" + FirebaseInstanceId.getInstance().getToken() + "&username=" + URLEncoder.encode(prefs.getString("signin", ""),"utf-8")
                        + "&location=" + dataBaseHandler.getUser(prefs.getString("signin", "")).getLocation());
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            JsonRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    messages.clear();
                    try {
                        if(response.getInt("code") == 200) {
                            JSONArray jsonArray = response.getJSONArray("messages");
                            for (int i = jsonArray.length()-1; i >=0 ; i--) {
                                messages.add(new Message(jsonArray.getJSONObject(i).getInt("id"), "", "",
                                                        jsonArray.getJSONObject(i).getString("username"),
                                                        jsonArray.getJSONObject(i).getString("message"),
                                                        jsonArray.getJSONObject(i).getString("created_at")));
                            }
                        }
                        for (int i = 0; i < chatroomMsgCallbacks.size(); i++)
                            chatroomMsgCallbacks.get(i).onChatroomMsgReceived(messages, response.getInt("code"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    System.out.println("That didn't work! " + error);
                }
            });
            jsonRequest.setRetryPolicy(new DefaultRetryPolicy(
                    3000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            // Add the request to the RequestQueue.
            requestQueue.add(jsonRequest);
        }else
            toast.show();
    }

    /**
     * Create Request:
     * Creates new user in server database
     * @param username
     * @param password
     */
    public void createUser(final String username, String password, String image, String location){
        if(usefulFunctions.isDeviceOnline()) {
            try {
                JSONObject json = new JSONObject();
                String refreshedToken = FirebaseInstanceId.getInstance().getToken();
                json.put("username", username);
                json.put("password", password);
                json.put("profile_pic",image);
                json.put("location", location);
                json.put("clientID", refreshedToken);
                String url = makeURL("api","register");
                JsonRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, url, json, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        User userInfo = null;
                        JSONObject user;
                        try {
                            if(response.getInt("code") == 201) {
                                user = response.getJSONObject("user");
                                userInfo = new User(user.getInt("id"),
                                                    user.getString("status"),
                                                    user.getString("username"),
                                                    user.getString("location"),
                                                    user.getString("profile_pic"));
                            }
                            authenticationCallback.onUserCreate(response.getInt("code"), response.getString("message"), userInfo);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println("That didn't work! " + error);
                        authenticationCallback.onUserCreate(400, error.getMessage(), null);
                    }
                });
                // Add the request to the RequestQueue.
                requestQueue.add(jsonRequest);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else
            toast.show();
    }

    /**
     * Update Request:
     * Updates user on server
     * @param image
     */
    public void updateUser(String image, String location){
        if(usefulFunctions.isDeviceOnline()) {
            try {
                JSONObject json = new JSONObject();
                json.put("api_token", prefs.getString("token", ""));
                json.put("profile_pic", image);
                json.put("location", location);
                String url = null;
                try {
                    url = makeURL("api","users", URLEncoder.encode(prefs.getString("signin",""),"utf-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                JsonRequest jsonRequest = new JsonObjectRequest(Request.Method.PUT, url, json, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject user = response.getJSONObject("user");
                            dataBaseHandler.updateUser(new User(user.getInt("id"),
                                                                user.getString("status"),
                                                                user.getString("username"),
                                                                user.getString("location"),
                                                                user.getString("profile_pic")));
                            usersCallback.onUsersProfileUpdate(response.getInt("code"),response.getString("message"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println("That didn't work! " + error);
                        usersCallback.onUsersProfileUpdate(400,error.getMessage());
                    }
                });
                // Add the request to the RequestQueue.
                requestQueue.add(jsonRequest);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else
            toast.show();
    }

    /**
     * Removes user from server
     */
    public void removeUser(){
        if(usefulFunctions.isDeviceOnline()) {
            try {
                JSONObject json = new JSONObject();
                String refreshedToken = FirebaseInstanceId.getInstance().getToken();
                json.put("api_token", prefs.getString("token", ""));
                json.put("clientID", refreshedToken);
                String url = makeURL("api","users",dataBaseHandler.getUser(prefs.getString("signin","guest")).getId()+"");
                JsonRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, url, json, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println("Response is: " + response.toString());
                        try {
                            Toast.makeText(context,response.getString("message"),Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println("That didn't work! " + error);
                    }
                });
                // Add the request to the RequestQueue.
                requestQueue.add(jsonRequest);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else
            toast.show();
    }

    /**
     * Signin Request:
     * User signin
     * @param username user username
     * @param password user password
     */
    public void userLogin(String username, String password){
        if(usefulFunctions.isDeviceOnline()) {
            try {
                JSONObject json = new JSONObject();
                String refreshedToken = FirebaseInstanceId.getInstance().getToken();
                json.put("username", username);
                json.put("password", password);
                json.put("clientID", refreshedToken);
                String url = makeURL("api","login");
                JsonRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, url, json, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        User userInfo = null;
                        JSONObject user;
                        try {
                            if(response.getInt("code") == 200) {
                                user = response.getJSONObject("user");
                                userInfo = new User(user.getInt("id"), user.getString("status"), user.getString("username"),
                                        user.getString("location"), user.getString("profile_pic"));
                                authenticationCallback.onUserLogin(response.getInt("code"), response.getString("message"),userInfo,response.getString("token"));
                            }else
                                authenticationCallback.onUserLogin(response.getInt("code"), response.getString("message"),userInfo,null);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println("That didn't work! " + error);
                        authenticationCallback.onUserLogin(400, error.getMessage(), null, null);
                    }
                });
                // Add the request to the RequestQueue.
                requestQueue.add(jsonRequest);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else
            toast.show();
    }

    /**
     * Send Message Request:
     * Send message to chatrooms(nearme,region)
     * @param message
     * @param chatroom
     */
    public void sendMessage(final String message, String chatroom){
        if(usefulFunctions.isDeviceOnline()) {
            try {
                JSONObject json = new JSONObject();
                json.put("api_token", prefs.getString("token", ""));
                json.put("chatname", chatroom);
                json.put("username", prefs.getString("signin", ""));
                json.put("message", message);
                json.put("location", dataBaseHandler.getUser(prefs.getString("signin", "")).getLocation());
                String url = makeURL("api","firebase","messagechatroom");

                JsonRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, url, json, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println("Response is: " + response.toString());

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println("That didn't work! " + error);
                    }
                });
                jsonRequest.setRetryPolicy(new DefaultRetryPolicy(
                        3000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                // Add the request to the RequestQueue.
                requestQueue.add(jsonRequest);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else
            toast.show();
    }

    /**
     * Send Message Request:
     * Send messages to users
     * @param message
     * @param usernameTo
     */
    public void sendMessageToUser(final String message, String usernameTo){
        if(usefulFunctions.isDeviceOnline()) {
            try {
                JSONObject json = new JSONObject();
                json.put("api_token", prefs.getString("token", ""));
                json.put("username", prefs.getString("signin", "guest"));
                json.put("usernameTo", usernameTo);
                json.put("message", message);
                String url = makeURL("api","firebase","messageuser");
                JsonRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, url, json, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            userInfoCallback.onNotifyMsgSend(response.getInt("code"),response.getString("content"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println("That didn't work! " + error);
                    }
                });
                jsonRequest.setRetryPolicy(new DefaultRetryPolicy(
                        3000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                // Add the request to the RequestQueue.
                requestQueue.add(jsonRequest);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else
            toast.show();
    }

    /**
     *  Request Logout
     */
    public void logout(){
        if(usefulFunctions.isDeviceOnline()) {
            String url = null;
            try {
                url = makeURL("api","logout?username=" + URLEncoder.encode(prefs.getString("signin",""),"utf-8") + "&api_token=" + prefs.getString("token", ""));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            JsonRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                try {
                    logoutCallback.onUserLogout(response.getInt("code"), response.getString("message"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                logoutCallback.onUserLogout(400, error.getMessage());
                }
            });
            jsonRequest.setRetryPolicy(new DefaultRetryPolicy(
                    3000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            // Add the request to the RequestQueue.
            requestQueue.add(jsonRequest);
        }else
            toast.show();
    }

    /**
     *  Method is used for getting
     *  well-structured location address string.
     *  ex.. San Antonio,Texas US
     * @param location city,state
     */
    public void getAddress(String location){
        if(usefulFunctions.isDeviceOnline()) {
            String url ="https://maps.googleapis.com/maps/api/geocode/json?address=" + location + "&key=AIzaSyA3Oan0nAtLzyhKg-c3VvNhyDNjH16miA0";
            JsonRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    System.out.println(response.toString());
                    String location = "";
                    try {
                        if(response.getString("status").equals("OK")){
                            JSONArray address_components = response.getJSONArray("results").getJSONObject(0).getJSONArray("address_components");
                            for (int i = 0; i < address_components.length(); i++) {
                                if (address_components.getJSONObject(i).getString("types").contains("locality"))
                                    location += address_components.getJSONObject(i).getString("long_name") + ",";
                                else if (address_components.getJSONObject(i).getString("types").contains("administrative_area_level_1"))
                                    location += address_components.getJSONObject(i).getString("long_name") + " ";
                                else if (address_components.getJSONObject(i).getString("types").contains("country"))
                                    location += address_components.getJSONObject(i).getString("short_name");
                            }
                            adrressCallback.onAddressReceived(200, location, null);
                        }else
                            adrressCallback.onAddressReceived(400,null,response.getString("status"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    adrressCallback.onAddressReceived(400,null,error.getMessage());
                }
            });
            jsonRequest.setRetryPolicy(new DefaultRetryPolicy(
                    3000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            // Add the request to the RequestQueue.
            requestQueue.add(jsonRequest);
        }else
            toast.show();
    }

    /**
     * This method is used to notify
     * the user on replied
     * @param message
     */
    public void onReplied(Message message){
        if(userInfoCallback != null) {
            userInfoCallback.onUserMsgReplied(message);
        }else{
            dataBaseHandler.addMessage(message);
        }
    }

    /**
     *  Server Api Callbacks
     */
    public interface ChatroomMsgCallback {
        void onChatroomMsgReceived(ArrayList<Message> messages, int code);
    }

    public interface UserInfoCallback{
        void onUserInfo(User userInfo);
        void onUserMsgReplied(Message message);
        void onNotifyMsgSend(int code, String content);
    }

    public interface AuthenticationCallback {
        void onUserLogin(int code, String status, User user, String token);
        void onUserCreate(int code, String status, User user);
    }
    public interface LogoutCallback {
        void onUserLogout(int code, String status);
    }
    public interface UsersCallback {
        void onUserRequest(User user);
        void onUsersReceived(ArrayList<User> users);
        void onUsersProfileUpdate(int code, String message);
    }

    /**
     * Callback for getting the address
     */
    public interface AdrressCallback {
        void onAddressReceived(int code, String location, String message);
    }
}
