package com.helloworld.myapplication;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class rvAdapterForUsersList extends RecyclerView.Adapter<rvAdapterForUsersList.ViewHolder> {

    private static final String TAG = "okay";
    public static ToInteractWithUserListFrag interact;
    Context ctx;
    ArrayList<UserProfile> users =  new ArrayList<>();

    public rvAdapterForUsersList(Context ctx, ArrayList<UserProfile> users) {
        this.ctx = ctx;
        this.users = users;
    }

    @NonNull
    @Override
    public rvAdapterForUsersList.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ConstraintLayout cl = (ConstraintLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_for_users_list,parent,false);
//        LinearLayout ll = (LinearLayout)
        ViewHolder view = new ViewHolder(cl);
        return view;
    }

    @Override
    public void onBindViewHolder(@NonNull rvAdapterForUsersList.ViewHolder holder, final int position) {
        interact = (ToInteractWithUserListFrag) ctx;
        UserProfile u = users.get(position);
        Log.d(TAG, "onBindViewHolder: printing user :"+u.firstName);
        String name = u.firstName + " " + u.lastName;
        holder.tvUserName.setText(name);
        Log.d(TAG, "onBindViewHolder: pofileImage text:"+u.profileImage);
        if(u.profileImage!=null && u.profileImage!=""){
            Picasso.get().load(u.profileImage).into(holder.ivProfileImage, new Callback() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "onSuccess: piccaso");
                }

                @Override
                public void onError(Exception e) {
                    Log.d(TAG, "onError: piccaso in users list");
                }
            });
        }

        holder.cl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                interact.seeUserProfile(users.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName;
        ImageView ivProfileImage;
        ConstraintLayout cl;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cl = itemView.findViewById(R.id.cl_for_userslist);
            tvUserName = itemView.findViewById(R.id.username_for_userslist);
            ivProfileImage = itemView.findViewById(R.id.profileImg_for_userslist);
        }

    }

    public interface ToInteractWithUserListFrag{
        void seeUserProfile(UserProfile u);
    }
}
