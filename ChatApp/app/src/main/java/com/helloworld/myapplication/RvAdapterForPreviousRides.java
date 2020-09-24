package com.helloworld.myapplication;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.zip.Inflater;

public class RvAdapterForPreviousRides extends RecyclerView.Adapter<RvAdapterForPreviousRides.ViewHolder> {

    private static final String TAG = "okay";
    ArrayList<PreviousRide> previousRides =  new ArrayList<>();
    private DatabaseReference dbRef;

    public RvAdapterForPreviousRides(ArrayList<PreviousRide> previousRides) {
        this.previousRides = previousRides;
        dbRef = FirebaseDatabase.getInstance().getReference("users");
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ConstraintLayout cl = (ConstraintLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.previous_ride_listview,parent,false);
        ViewHolder viewHolder =  new ViewHolder(cl);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: view holder created for position=>"+position);
        PreviousRide pr = previousRides.get(position);
        GetInfoOfUserAndSetIt(pr.driverID,holder.driverName,holder.driverProfile);
        GetInfoOfUserAndSetIt(pr.riderID,holder.riderName,holder.riderProfile);
        holder.toLocation.setText(pr.toLocation);
        holder.fromLocation.setText(pr.fromLocation);
        holder.date.setText(pr.dateAndTime.toString());
    }

    @Override
    public int getItemCount() {
        return previousRides.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView fromLocation,toLocation,date,driverName,riderName;
        ImageView driverProfile, riderProfile;
        public ViewHolder(@NonNull View view) {
            super(view);
            date = view.findViewById(R.id.date_inPRLV);
            fromLocation = view.findViewById(R.id.from_location_inPRLV);
            toLocation = view.findViewById(R.id.to_location_inPRLV2);
            driverName = view.findViewById(R.id.drivers_name_inPRLV2);
            riderName = view.findViewById(R.id.riders_name_inPRLV);
            driverProfile = view.findViewById(R.id.driver_imag_inPRLV);
            riderProfile = view.findViewById(R.id.rider_image_inPRLV);
        }
    }

    void GetInfoOfUserAndSetIt(final String uid, final TextView nametv, final ImageView iv){
        dbRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserProfile u  = snapshot.getValue(UserProfile.class);
                nametv.setText(u.firstName+" "+u.lastName);
                Picasso.get().load(u.profileImage).into(iv, new Callback() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "onSuccess: piccaso");
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.d(TAG, "onError: piccaso in previous ride for user=>"+uid);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "onCancelled: error while litening for info for user =>"+uid);
            }
        });
    }

}
