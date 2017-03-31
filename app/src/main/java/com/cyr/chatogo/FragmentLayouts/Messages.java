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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.cyr.chatogo.API.ServerAPI;
import com.cyr.chatogo.Activities.MessageActivity;
import com.cyr.chatogo.Extras.DataBaseHandler;
import com.cyr.chatogo.Extras.UsefulFunctions;
import com.cyr.chatogo.Models.Message;
import com.cyr.chatogo.R;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Messages.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Messages#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Messages extends ListFragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private OnFragmentInteractionListener mListener;
    private ArrayAdapter adapter;
    private SharedPreferences prefs;
    private DataBaseHandler dataBaseHandler;
    private Context context;

    public Messages() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Messages.
     */
    // TODO: Rename and change types and number of parameters
    public static Messages newInstance(String param1, String param2) {
        Messages fragment = new Messages();
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
        dataBaseHandler = DataBaseHandler.getInstance(context);
        prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        setListView(dataBaseHandler.getdistinctMessages());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rowView = inflater.inflate(R.layout.fragment_messages, container, false);

        // Inflate the layout for this fragment
        return rowView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onMessagesFragmentInteraction(uri);
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
        void onMessagesFragmentInteraction(Uri uri);
    }

    /**
     * Set up List View and Adapter
     * @param messageList
     */
    public void setListView(final ArrayList<Message> messageList){
        if(adapter == null) {
            adapter = new ArrayAdapter<Message>(getActivity(), R.layout.message_view, messageList) {
                @Override
                public View getView(final int position, View convertView, ViewGroup parent) {
                    View rowView = convertView;
                    if (rowView == null) {
                        rowView = getActivity().getLayoutInflater().inflate(R.layout.message_view, parent, false);
                    }

                    //image and text for each file
                    final TextView msgTextView = (TextView) rowView.findViewById(R.id.message);
                    final TextView textView = (TextView) rowView.findViewById(R.id.message_username);
                    final ImageView imageView = (ImageView) rowView.findViewById(R.id.message_pic);

                    textView.setText(messageList.get(position).getChatname());
                    if(messageList.get(position).getUsername().equals(prefs.getString("signin","")))
                        msgTextView.setText("Me: " + messageList.get(position).getMessage());
                    else
                        msgTextView.setText(messageList.get(position).getMessage());

                    if(dataBaseHandler.getUser(getItem(position).getChatname())!= null
                            && dataBaseHandler.getUser(getItem(position).getChatname()).getImage().length() > 0 ) {

                        imageView.setImageBitmap(UsefulFunctions.ourInstance(context).stringToBitmap(
                                dataBaseHandler.getUser(getItem(position).getChatname()).getImage()));
                    }else
                        imageView.setImageResource(R.drawable.contacts);

                    return rowView;
                }
            };
            setListAdapter(adapter);
        }else {
            adapter.clear();
            adapter.addAll(messageList);
            adapter.notifyDataSetChanged();
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

        if(!prefs.getString("signin","guest").equals(((Message)l.getAdapter().getItem(position)).getChatname())) {
            Intent intent = new Intent(getActivity(), MessageActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            intent.putExtra("username", ((Message) l.getAdapter().getItem(position)).getChatname());
            startActivity(intent);
        }
    }
}
