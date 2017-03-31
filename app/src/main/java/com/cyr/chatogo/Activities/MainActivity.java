package com.cyr.chatogo.Activities;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.cyr.chatogo.API.ServerAPI;
import com.cyr.chatogo.FragmentLayouts.Chatrooms;
import com.cyr.chatogo.FragmentLayouts.Messages;
import com.cyr.chatogo.R;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends AppCompatActivity implements
        Chatrooms.OnFragmentInteractionListener,Messages.OnFragmentInteractionListener, ServerAPI.LogoutCallback{

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private TabLayout tabLayout;
    private ServerAPI serverApi;
    private Context context;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        context = getApplicationContext();
        serverApi = ServerAPI.getInstance(context);
        serverApi.initToast();
        prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        editor = prefs.edit();
        tabListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        serverApi.setLogoutCallback(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        serverApi.setLogoutCallback(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        Intent intent;
        //noinspection SimplifiableIfStatement
        switch (id){
            case R.id.action_contacts:
                intent = new Intent(this,ContactsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                return true;
            case R.id.action_settings:
                intent = new Intent(this,SettingsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                return true;
            case R.id.action_signout:
                //Unsubscribe from topics(chatrooms)
                FirebaseMessaging.getInstance().unsubscribeFromTopic("nearme");
                FirebaseMessaging.getInstance().unsubscribeFromTopic("region");
                FirebaseMessaging.getInstance().unsubscribeFromTopic("broadcast");
                //Request logout
                serverApi.logout();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return Chatrooms.newInstance("nearme",null);
                case 1:
                    return Chatrooms.newInstance("region",null);
                case 2:
                    return Messages.newInstance(null,null);
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "NearMe";
                case 1:
                    return "Region";
                case 2:
                    return "Messages";
                default:
                    return null;
            }
        }
    }

    /**
     * Adds a tab listener to change chatroom name
     * on appropriate tab
     */
    public void tabListener(){
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if(tab.getText().toString().contains("NearMe")) {
                    editor.putString("chatroom","nearme");
                    editor.commit();
                    serverApi.getChatroomMessages();
                }
                if(tab.getText().toString().contains("Region")){
                    editor.putString("chatroom","region");
                    editor.commit();
                    serverApi.getChatroomMessages();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    /**
     * Sever Api callback for logging out
     * @param code 200 if successful 400 otherwise
     * @param status message status ex. logout successful
     */
    @Override
    public void onUserLogout(int code, String status) {
        Intent intent;
        if(code == 200){
            //Deleting username and token
            editor.putString("token","");
            editor.putString("signin","");
            editor.commit();
            intent = new Intent(this,AuthenticationActivity.class);
            startActivity(intent);
            finish();
        }

        Toast.makeText(context,status,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMessagesFragmentInteraction(Uri uri) {

    }

    @Override
    public void onChatroomsFragmentInteraction(Uri uri) {
    }
}
