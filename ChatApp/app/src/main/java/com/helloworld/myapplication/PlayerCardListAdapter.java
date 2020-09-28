package com.helloworld.myapplication;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class PlayerCardListAdapter extends RecyclerView.Adapter<PlayerCardListAdapter.MyViewHolder> {
    private static final String TAG = "okay";
    private ArrayList<UnoCardClass> mDataset;
    public static InteractWithPlayerCardList interact;

    // Provide a suitable constructor (depends on the kind of dataset)
    public PlayerCardListAdapter(ArrayList<UnoCardClass> myDataset, Context ctx) {
        mDataset = myDataset;
        interact = (InteractWithPlayerCardList) ctx;
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
        Log.d(TAG, "onBindViewHolder: called in player1GameScreenActivity");

        UnoCardClass card = mDataset.get(position);
        Log.d(TAG, "onBindViewHolder: for card values=>"+card);
        switch (card.color){
            case "red":
                //holder.cardView.setCardBackgroundColor(Color.RED);
                holder.cardView.setBackgroundResource(R.drawable.uno_red);
                holder.tv.setTextColor(Color.argb(255,237,28,36));
                break;
            case "green":
                //holder.cardView.setCardBackgroundColor(Color.GREEN);
                holder.cardView.setBackgroundResource(R.drawable.uno_green);
                holder.tv.setTextColor(Color.argb(255,34,177,76));
                break;
            case "blue":
                //holder.cardView.setCardBackgroundColor(Color.BLUE);
                holder.cardView.setBackgroundResource(R.drawable.uno_blue);
                holder.tv.setTextColor(Color.argb(255,63,72,204));
                break;
            case "yellow":
                //holder.cardView.setCardBackgroundColor(Color.YELLOW);
                holder.cardView.setBackgroundResource(R.drawable.uno_yellow);
                holder.tv.setTextColor(Color.argb(255,255,242,0));
                break;
            default:
                holder.cardView.setCardBackgroundColor(Color.BLACK);
                holder.tv.setTextColor(Color.WHITE);
        }
        if(card.color.equals("black") || card.number == 10){
            if (card.number <= 4){
                holder.tv.setText("+4");
                //holder.tv.setTop(5);
                holder.tv.setPadding(0,10,0,0);
                holder.tv.setTextSize(TypedValue.COMPLEX_UNIT_SP,30);
            }else{
                holder.tv.setTextSize(TypedValue.COMPLEX_UNIT_SP,20);
                holder.tv.setPadding(0,30,0,0);
                holder.tv.setText("skip");
            }
        }else{
            holder.tv.setText(""+card.number);
        }

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                interact.PlayCard(position);
            }
        });
//        holder.imageButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
////                interact.getDetails(mDataset.get(position),position);
//            }
//        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }


    public static class MyViewHolder extends RecyclerView.ViewHolder{
        // each data item is just a string in this case
        //ImageButton imageButton;
        TextView tv;
        ConstraintLayout playerCardConstratint;
        CardView cardView;
        public MyViewHolder(View view) {
            super(view);
            //imageButton = view.findViewById(R.id.playerCardButton);
            tv = view.findViewById(R.id.tv_in_playerCardList);
            playerCardConstratint = view.findViewById(R.id.playerCardConstraint);
            cardView = view.findViewById(R.id.card_view_forPlayerCardList);
        }

    }

    public interface InteractWithPlayerCardList{
        //        public void selectedItem(TodoClass todoClass);
        // public void getDetails(String meetingPlace, int position);
        public void PlayCard(int postion);
    }
}
