package com.cyr.chatogo.Extras;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cyr.chatogo.Models.Message;
import com.cyr.chatogo.R;

import java.io.Serializable;
import java.util.List;

/**
 * Custom Adapter:
 * Created by roger on 3/10/2017.
 */
public class MessageAdapter extends ArrayAdapter<Message> implements Serializable{
    private Activity context;
    private List<Message> messages;
    private SharedPreferences prefs;

    public MessageAdapter(Activity context, List<Message> messages) {
        super(context, R.layout.chat_text_view, messages);
        this.context = context;
        this.messages = messages;
        prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        return;
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        View rowView = view;

        if (messages.get(position).getUsername().equals(prefs.getString("signin", "")))
            rowView = context.getLayoutInflater().inflate(R.layout.chat_text_view_inverse, null, false);
        else
            rowView = context.getLayoutInflater().inflate(R.layout.chat_text_view, null, false);

        if(messages.get(position) != null) {
            TextView usernameText = (TextView) rowView.findViewById(R.id.user_name);
            TextView messageText = (TextView) rowView.findViewById(R.id.text_message);

            usernameText.setText(messages.get(position).getUsername());
            messageText.setText(messages.get(position).getMessage());
        }
        return rowView;
    }

}
