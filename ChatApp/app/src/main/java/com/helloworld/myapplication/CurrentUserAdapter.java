package com.helloworld.myapplication;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

public class CurrentUserAdapter  extends RecyclerView.Adapter<CurrentUserAdapter.MyViewHolder> {
    private ArrayList<UserProfile> mDataset;
//    public static InteractWithRecyclerView interact;

    // Provide a suitable constructor (depends on the kind of dataset)
    public CurrentUserAdapter(ArrayList<UserProfile> myDataset, Context ctx) {
        mDataset = myDataset;
//        interact = (InteractWithRecyclerView) ctx;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public CurrentUserAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                                int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.current_users_list, parent, false);
        CurrentUserAdapter.MyViewHolder vh = new CurrentUserAdapter.MyViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(CurrentUserAdapter.MyViewHolder holder, final int position) {
        UserProfile userProfile = mDataset.get(position);
        holder.name_of_Cuser.setText(userProfile.firstName);
        Picasso
                .get()
                .load(userProfile.profileImage)
                .into(holder.viewersListImage, new Callback() {
                    @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(Exception e) {
                            //Toast.makeText(DailyForeCastAdapter.this, "No Image found", Toast.LENGTH_SHORT).show();
                        }
                    });
//        holder.option.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.d("Demo","Selected Choice is : "+mDataset.get(position));
////                interact.selectedItem(mDataset.get(position));
//            }
//        });
//
        holder.constraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Demo","Selected Position is :" + mDataset.get(position));
//                interact.getDetails(mDataset.get(position), position);
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
        ImageView viewersListImage;
        ConstraintLayout constraintLayout;
        TextView name_of_Cuser;
        public MyViewHolder(View view) {
            super(view);
            viewersListImage = view.findViewById(R.id.viewersListImage);
            constraintLayout = view.findViewById(R.id.freshCurrentUsersConstaintLayout);
            name_of_Cuser = view.findViewById(R.id.name_of_Cusers);
        }

    }

//    public interface InteractWithRecyclerView{
//        //        public void selectedItem(int position);
//        public void getDetails(DailyForeCast dailyForeCast, int position);
//    }


}
