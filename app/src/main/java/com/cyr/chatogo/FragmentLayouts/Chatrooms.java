package com.cyr.chatogo.FragmentLayouts;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.cyr.chatogo.API.ServerAPI;
import com.cyr.chatogo.Activities.AuthenticationActivity;
import com.cyr.chatogo.Activities.MessageActivity;
import com.cyr.chatogo.Extras.MessageAdapter;
import com.cyr.chatogo.Models.Message;
import com.cyr.chatogo.R;

import java.util.ArrayList;

import rx.Subscription;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Chatrooms.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Chatrooms#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Chatrooms extends ListFragment implements ServerAPI.ChatroomMsgCallback{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private MessageAdapter adapter;
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private EditText msgText;
    private Button send;
    private ServerAPI serverApi;
    private Subscription sub;
    private ArrayList messageList;
    private Context context;

    private OnFragmentInteractionListener mListener;

    public Chatrooms() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Chatrooms.
     */
    // TODO: Rename and change types and number of parameters
    public static Chatrooms newInstance(String param1, String param2) {
        Chatrooms fragment = new Chatrooms();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        //Initialize variables
        context = getActivity().getApplicationContext();
        serverApi = ServerAPI.getInstance(context);
        serverApi.initToast();
        prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        editor = prefs.edit();
        messageList = new ArrayList();
        serverApi.getChatroomMessages();
        serverApi.addMessagesListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Get views
        View rowView = inflater.inflate(R.layout.fragment_chatrooms, container, false);
        msgText = (EditText)rowView.findViewById(R.id.text_box);
        send = (Button) rowView.findViewById(R.id.send);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                serverApi.sendMessage(msgText.getText().toString(), prefs.getString("chatroom","nearme"));
                msgText.setText("");
            }
        });

        // Inflate the layout for this fragment
        return rowView;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setListView(messageList);
        getListView().setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
        getListView().setStackFromBottom(true);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onChatroomsFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        serverApi.removeMessagesListener(this);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onChatroomsFragmentInteraction(Uri uri);
    }

    /**
     * Set up List View and Adapter
     * @param messageList messages to display
     */
    public void setListView(ArrayList<Message> messageList){
        if(adapter == null) {
            adapter = new MessageAdapter(getActivity(), messageList);
            setListAdapter(adapter);
        }else {
            adapter.clear();
            adapter.addAll(messageList);
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * Sever Api callback when a message is received
     * on chatroom
     * @param messages messages to display
     * @param code 200 if successful 400 otherwise
     */
    @Override
    public void onChatroomMsgReceived(ArrayList<Message> messages, int code) {
        if(code == 200) {
            if( mParam1.equals(prefs.getString("chatroom",""))) {
                setListView(messages);
            }
        }else {
            editor.putString("signin","");
            editor.commit();
            Intent intent = new Intent(getActivity(),AuthenticationActivity.class);
            startActivity(intent);
            getActivity().finish();
        }

    }


    /**
     * Listview click listener
     * @param l
     * @param v
     * @param position
     * @param id
     */
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        if(!prefs.getString("signin","").equals(((Message)l.getAdapter().getItem(position)).getUsername())) {
            Intent intent = new Intent(getActivity(), MessageActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            intent.putExtra("username", ((Message) l.getAdapter().getItem(position)).getUsername());
            startActivity(intent);
        }
    }
}
