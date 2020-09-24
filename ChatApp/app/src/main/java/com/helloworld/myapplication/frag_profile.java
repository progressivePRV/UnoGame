package com.helloworld.myapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link frag_profile#newInstance} factory method to
 * create an instance of this fragment.
 */
public class frag_profile extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public frag_profile() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment frag_profile.
     */
    // TODO: Rename and change types and number of parameters
    public static frag_profile newInstance(String param1, String param2) {
        frag_profile fragment = new frag_profile();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    FirebaseAuth mAuth;
    DatabaseReference mDatabase;
    UserProfile u;
    private ProgressDialog progressDialog;
    Button editProfileBtn;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        editProfileBtn =  view.findViewById(R.id.buttoneditProfile);
        editProfileBtn.setClickable(false);
        mAuth = FirebaseAuth.getInstance();

        final TextView textViewFirstName = view.findViewById(R.id.textViewFirstName);
        final TextView textViewLastName = view.findViewById(R.id.textViewLastName);
        final TextView textViewGender = view.findViewById(R.id.textViewGender);
        final TextView textViewEmail = view.findViewById(R.id.textViewEmail);
        final TextView textViewCity = view.findViewById(R.id.textViewCity);
        final ImageView profileImage = view.findViewById(R.id.imageViewProfileImage);

            showProgressBarDialog();
            mDatabase = FirebaseDatabase.getInstance().getReference("users");
            mDatabase.child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    u = snapshot.getValue(UserProfile.class);
                    u.uid = snapshot.getKey();
                    Picasso.get().load(u.profileImage).into(profileImage);
                    textViewFirstName.setText(u.firstName);
                    textViewLastName.setText(u.lastName);
                    textViewGender.setText(u.gender);
                    textViewEmail.setText(u.email);
                    textViewCity.setText(u.city);
                    hideProgressBarDialog();
                    editProfileBtn.setClickable(true);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    hideProgressBarDialog();
                    Log.d("demo","An error occured");
                    Toast.makeText(getContext(), "Failed to Load Profile", Toast.LENGTH_SHORT).show();
                }
            });

        view.findViewById(R.id.buttoneditProfile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    Intent editProfileIntent = new Intent(getContext(),EditProfileActivity.class);
                    editProfileIntent.putExtra("user",u);
                    startActivity(editProfileIntent);

            }
        });

        return view;
    }

    //for showing the progress dialog
    public void showProgressBarDialog()
    {
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    //for hiding the progress dialog
    public void hideProgressBarDialog()
    {
        progressDialog.dismiss();
    }

}