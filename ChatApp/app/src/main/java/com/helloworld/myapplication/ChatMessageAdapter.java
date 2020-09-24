package com.helloworld.myapplication;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ChatMessageAdapter extends RecyclerView.Adapter<ChatMessageAdapter.MyViewHolder> {
    private ArrayList<ChatMessageDetails> mDataset;
    FirebaseAuth mAuth;
    DatabaseReference mDatabase;
    FirebaseStorage storage;
    private String name ;
    public static InteractWithRecyclerView interact;

    // Provide a suitable constructor (depends on the kind of dataset)
    public ChatMessageAdapter(ArrayList<ChatMessageDetails> myDataset, Context ctx) {
        mDataset = myDataset;
        interact = (InteractWithRecyclerView) ctx;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ChatMessageAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                         int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.chat_room_message_list, parent, false);
        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        mAuth=FirebaseAuth.getInstance();
        final ChatMessageDetails chatMessageDetails = mDataset.get(position);
        holder.chatRoomMessage.setText(chatMessageDetails.Message);

        //For getting the user name
        mDatabase = FirebaseDatabase.getInstance().getReference("users");

        name = "";
    // Attach a listener to read the data at our posts reference
        mDatabase.child(chatMessageDetails.Uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                final DataSnapshot snap = snapshot;

                name = snap.child("firstName").getValue(String.class);

                Log.d("name", name);
                holder.ChatRoomUserName.setText(name);
                holder.chatRoomMessageDate.setText(chatMessageDetails.date);
                holder.NumberOfLikes.setText(chatMessageDetails.likedUsers.size()+"");
                Picasso.get()
                        .load(snap.child("profileImage").getValue(String.class))
                        .into(holder.ProfileImageChatRoom, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError(Exception e) {

                            }
                        });

                if(mAuth.getUid().equals(chatMessageDetails.Uid)){
                    holder.ButtonMessageDelete.setVisibility(ImageButton.VISIBLE);
                }else{
                    holder.ButtonMessageDelete.setVisibility(ImageButton.INVISIBLE);
                }

                if(chatMessageDetails.likedUsers.containsKey(mAuth.getUid())){
                    holder.ButtonMessageFavouriteON.setVisibility(ImageButton.VISIBLE);
                    holder.ButtonMessageFavorites.setVisibility(ImageButton.INVISIBLE);
                }else{
                    holder.ButtonMessageFavouriteON.setVisibility(ImageButton.INVISIBLE);
                    holder.ButtonMessageFavorites.setVisibility(ImageButton.VISIBLE);
                }

                holder.ButtonMessageFavorites.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        chatMessageDetails.likedUsers.put(mAuth.getUid(),true);
                        interact.getDetails(chatMessageDetails);
                    }
                });

                holder.ButtonMessageFavouriteON.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        chatMessageDetails.likedUsers.remove(mAuth.getUid());
                        interact.getDetails(chatMessageDetails);
                    }
                });

                holder.ButtonMessageDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        interact.getItemPosition(position, chatMessageDetails);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
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
        ImageButton ButtonMessageFavorites, ButtonMessageDelete, ButtonMessageFavouriteON;
        ImageView ProfileImageChatRoom;
        TextView ChatRoomUserName;
        TextView chatRoomMessage;
        TextView chatRoomMessageDate, NumberOfLikes;
        public MyViewHolder(View view) {
            super(view);
            ProfileImageChatRoom = view.findViewById(R.id.ProfileImageChatRoom);
            ButtonMessageFavorites = view.findViewById(R.id.ButtonMessageFavorites);
            ButtonMessageDelete = view.findViewById(R.id.ButtonMessageDelete);
            ChatRoomUserName = view.findViewById(R.id.ChatRoomUserName);
            chatRoomMessage = view.findViewById(R.id.chatRoomMessage);
            chatRoomMessageDate = view.findViewById(R.id.chatRoomMessageDate);
            ButtonMessageFavouriteON = view.findViewById(R.id.ButtonMessageFavouriteON);
            NumberOfLikes = view.findViewById(R.id.NumberOfLikes);
        }
    }

    public interface InteractWithRecyclerView{
        public void getDetails(ChatMessageDetails chatMessageDetails);
        public void getItemPosition(int position, ChatMessageDetails chatMessageDetails);
    }
}

