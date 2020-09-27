package com.helloworld.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class PlayerCardListAdapter extends RecyclerView.Adapter<PlayerCardListAdapter.MyViewHolder> {
    private ArrayList<UnoCardClass> mDataset;
//    public static InteractWithRecyclerView interact;

    // Provide a suitable constructor (depends on the kind of dataset)
    public PlayerCardListAdapter(ArrayList<UnoCardClass> myDataset, Context ctx) {
        mDataset = myDataset;
//        interact = (InteractWithRecyclerView) ctx;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public PlayerCardListAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                         int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.player_card_list, parent, false);
        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {

        holder.imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                interact.getDetails(mDataset.get(position),position);
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }


    public static class MyViewHolder extends RecyclerView.ViewHolder{
        // each data item is just a string in this case
        ImageButton imageButton;
        public MyViewHolder(View view) {
            super(view);
            imageButton = view.findViewById(R.id.playerCardButton);
        }

    }

//    public interface InteractWithRecyclerView{
//        //        public void selectedItem(TodoClass todoClass);
//        public void getDetails(String meetingPlace, int position);
//    }
}
