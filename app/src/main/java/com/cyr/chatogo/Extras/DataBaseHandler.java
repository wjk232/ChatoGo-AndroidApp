package com.cyr.chatogo.Extras;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.cyr.chatogo.Models.User;
import com.cyr.chatogo.Models.Message;

import java.util.ArrayList;


/**
 * Created by roger on 3/10/2017.
 */
public class DataBaseHandler extends SQLiteOpenHelper {
    private static DataBaseHandler mInstance = null;
    public static DataBaseHandler getInstance(Context ctx) {
        if (mInstance == null) {
            mInstance = new DataBaseHandler(ctx);
        }
        return mInstance;
    }
    // Database Version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "contacts_db";
    // User table name
    private static final String TABLE_MESSAGES = "messages";
    private static final String TABLE_USERS = "users";

    // Users Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_STATUS = "status";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_LOCATION = "location";
    private static final String KEY_IMAGE = "image";

    // Messages Table Columns names
    private static final String KEY_MSG_ID = "id";
    private static final String KEY_MSG_OWNER = "owner";
    private static final String KEY_MSG_CHATNAME = "chatname";
    private static final String KEY_MSG_USERNAME = "username";
    private static final String KEY_MSG_MESSAGE = "message";
    private static final String KEY_MSG_BT = "btime";

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private DataBaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        editor = prefs.edit();
        return;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_STATUS + " TEXT," + KEY_USERNAME + " TEXT,"
                + KEY_LOCATION + " TEXT," + KEY_IMAGE + " TEXT" +  ")";
        String CREATE_MESSAGES_TABLE = "CREATE TABLE " + TABLE_MESSAGES + "("
                + KEY_MSG_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"+ KEY_MSG_OWNER + " TEXT," + KEY_MSG_CHATNAME + " TEXT,"
                + KEY_MSG_USERNAME + " TEXT," + KEY_MSG_MESSAGE + " TEXT," + KEY_MSG_BT + " TEXT" + ")";

        db.execSQL(CREATE_USERS_TABLE);
        db.execSQL(CREATE_MESSAGES_TABLE);
        return;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);

        // Creating tables again
        onCreate(db);
        return;
    }

    /**
     * Adding new user
     * @param user
     */
    public void addUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ID, user.getId()); // User Id
        values.put(KEY_STATUS, user.getStatus()); // User status
        values.put(KEY_USERNAME, user.getUsername()); // User UserName
        values.put(KEY_LOCATION, user.getLocation()); // User Location
        values.put(KEY_IMAGE, user.getImage()); // User Image
        // Inserting Row
        db.insert(TABLE_USERS, null, values);
        return;
    }

    /**
     * Getting one user
     * @param username username
     * @return user
     */
    public User getUser(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        User user = null;

        Cursor cursor = db.query(TABLE_USERS, new String[]{KEY_ID,
                        KEY_STATUS, KEY_USERNAME, KEY_LOCATION, KEY_IMAGE}, KEY_USERNAME + "=?",
                new String[]{String.valueOf(username)}, null, null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            user = new User(Integer.parseInt(cursor.getString(0)),
                    cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4));
        }
       // return User
        return user;
    }

    /**
     * Getting All User
     * @return all users info
     */
    public ArrayList<User> getAllUsers() {
        ArrayList<User> contactsList = new ArrayList();
        // Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_USERS;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                User user = new User();
                user.setId(Integer.parseInt(cursor.getString(0)));
                user.setStatus(cursor.getString(1));
                user.setUsername(cursor.getString(2));
                user.setLocation(cursor.getString(3));
                user.setImage(cursor.getString(4));
                // Adding user to list
                contactsList.add(user);
            } while (cursor.moveToNext());
        }

        // return contact list
        return contactsList;
    }

    /**
     * Getting users Count
     * @return number of users
     */
    public int getUsersCount() {
        String countQuery = "SELECT * FROM " + TABLE_USERS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        // return count
        return cursor.getCount();
    }

   // Updating a User
    public int updateUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_ID, user.getId()); // User Id
        values.put(KEY_STATUS, user.getId()); // User Status
        values.put(KEY_USERNAME, user.getUsername()); // User userName
        values.put(KEY_LOCATION, user.getLocation()); // User location
        values.put(KEY_IMAGE, user.getImage()); // User Image

        // updating row
        return db.update(TABLE_USERS, values, KEY_USERNAME + " = ?",
                new String[]{String.valueOf(user.getUsername())});
    }

//
//    // Deleting a contact
//    public void deleteContact(ContactInfo contact) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        db.delete(TABLE_CONTACTS, KEY_ID + " = ?",
//                new String[] { String.valueOf(contact.getId()) });
//        return;
//    }
//    /**
//     * Remove all users and groups from database.
//     */
//    /*public void removeAllContacts()
//    {
//        // db.delete(String tableName, String whereClause, String[] whereArgs);
//        // If whereClause is null, it will delete all rows.
//        SQLiteDatabase db = this.getWritableDatabase(); // helper is object extends SQLiteOpenHelper
//        db.delete(TABLE_CONTACTS, null, null);
//        //db.delete(TAB_USERS_GROUP, null, null);
//    }*/


    /**
     * Adding new Message
     * @param message meesage to add
     * @return message id
     */
    public long addMessage(Message message) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(KEY_MSG_OWNER, message.getOwner()); // msg Onwer
        values.put(KEY_MSG_CHATNAME, message.getChatname()); // msg Chatname
        values.put(KEY_MSG_USERNAME, message.getUsername()); // msg UserName
        values.put(KEY_MSG_MESSAGE, message.getMessage()); // msg
        values.put(KEY_MSG_BT, message.getDate()); // msg bt
        // Inserting Row
        long id = db.insert(TABLE_MESSAGES, null, values);
        return id;
    }

    /**
     * Getting one message
     * @param username
     * @return
     */
    public Message getMessage(String username) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_MESSAGES, new String[]{KEY_MSG_ID,
                        KEY_MSG_OWNER, KEY_MSG_CHATNAME, KEY_MSG_USERNAME, KEY_MSG_MESSAGE, KEY_MSG_BT }, KEY_MSG_USERNAME + "=?",
                new String[]{String.valueOf(username)}, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        Message message = new Message(Integer.parseInt(cursor.getString(0)),
                cursor.getString(1), cursor.getString(2), cursor.getString(3),cursor.getString(4),cursor.getString(5));
        cursor.close();
        // return msg
        return message;
    }

    /**
     * Getting All msgs
     * @param username username messages
     * @return messages of username
     */
    public ArrayList<Message> getAllMessages(String username) {
        ArrayList<Message> messagesList = new ArrayList<Message>();
        // Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_MESSAGES + " WHERE owner="+"'"+prefs.getString("signin","") +"'"
                + " AND chatname=" + "'"+username+"'";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Message message = new Message();
                message.setId(Integer.parseInt(cursor.getString(0)));
                message.setOwner(cursor.getString(1));
                message.setChatname(cursor.getString(2));
                message.setUsername(cursor.getString(3));
                message.setMessage(cursor.getString(4));
                message.setDate(cursor.getString(5));
                // Adding contact to list
                messagesList.add(message);
            } while (cursor.moveToNext());
        }
        cursor.close();
        // return contact list
        return messagesList;
    }

    /**
     * Get distinct messages
     * @return distinct messages
     */
    public ArrayList<Message> getdistinctMessages(){
        ArrayList<Message> messagesList = new ArrayList<Message>();
        // Select All Query
        String selectQuery =
                "SELECT * FROM " + TABLE_MESSAGES + " WHERE owner=" + "'"+prefs.getString("signin","")+"'" + " GROUP BY chatname ORDER BY id DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Message message = new Message();
                message.setId(Integer.parseInt(cursor.getString(0)));
                message.setOwner(cursor.getString(1));
                message.setChatname(cursor.getString(2));
                message.setUsername(cursor.getString(3));
                message.setMessage(cursor.getString(4));
                message.setDate(cursor.getString(5));
                // Adding contact to list
                if(!message.getChatname().equals(prefs.getString("signin","")))
                    messagesList.add(message);
            } while (cursor.moveToNext());
        }
        cursor.close();
        // return contact list
        return messagesList;

    }

    /**
     * Getting messages Count
     * @return number of messages
     */
    public int getMessagesCount() {
        String countQuery = "SELECT * FROM " + TABLE_MESSAGES;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        // return count
        return cursor.getCount();
    }

    /**
     * Delete messages = username
     * @param  username
     */
    public void deleteMessages(String username) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_MESSAGES,"owner=? AND chatname=?",new String[]{prefs.getString("signin",""),username});
        return;
    }

    /**
     * Remove all messages
     */
    public void removeAllMessages()
    {
        SQLiteDatabase db = this.getWritableDatabase(); // helper is object extends SQLiteOpenHelper
        db.delete(TABLE_MESSAGES, null, null);
        return;
    }
}
