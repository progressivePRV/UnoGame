package com.helloworld.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ChatRoomAdapter extends RecyclerView.Adapter<ChatRoomAdapter.MyViewHolder> {
    private ArrayList<String> mDataset;
    public static InteractWithRecyclerView interact;

    // Provide a suitable constructor (depends on the kind of dataset)
    public ChatRoomAdapter(ArrayList<String> myDataset, frag_chatrooms ctx) {
        mDataset = myDataset;
        interact = (InteractWithRecyclerView) ctx;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ChatRoomAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                         int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_chatrooms_list, parent, false);
        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {

        holder.chatRoomNameText.setText(mDataset.get(position));

        holder.chatRoomListConstraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                interact.getDetails(mDataset.get(position),position);
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }


    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder{
        // each data item is just a string in this case
        TextView chatRoomNameText;
        ConstraintLayout chatRoomListConstraintLayout;
        public MyViewHolder(View view) {
            super(view);
            chatRoomNameText = view.findViewById(R.id.chatRoomNameText);
            chatRoomListConstraintLayout = view.findViewById(R.id.chatRoomListConstraintLayout);
        }

    }

    public interface InteractWithRecyclerView{
        public void getDetails(String chatRoom, int position);
    }
}
