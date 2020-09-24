package com.helloworld.myapplication;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link frag_previousRide#newInstance} factory method to
 * create an instance of this fragment.
 */
public class frag_previousRide extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = "okay";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public frag_previousRide() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment frag_previousRide.
     */
    // TODO: Rename and change types and number of parameters
    public static frag_previousRide newInstance(String param1, String param2) {
        frag_previousRide fragment = new frag_previousRide();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
//      variales for this farg
    private ProgressDialog progressDialog;
    private FirebaseFirestore db;
    FirebaseAuth  mAuth;
    ArrayList<PreviousRide> prevRides =  new ArrayList<>();
    RecyclerView rv;
    RecyclerView.Adapter rvAdapter;
    RecyclerView.LayoutManager rvLayoutManager;

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
        View view = inflater.inflate(R.layout.fragment_frag_previous_ride, container, false);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        Log.d(TAG, "onCreateView: called in frag previous ride");
        
        rv = view.findViewById(R.id.rv_for_previousRides);
        rv.setHasFixedSize(true);
        rvAdapter = new RvAdapterForPreviousRides(prevRides);
        rvLayoutManager = new LinearLayoutManager(getContext());
        rv.setLayoutManager(rvLayoutManager);
        rv.setAdapter(rvAdapter);
        getPreviousRides();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
//        getPreviousRides();
    }

    //adding function to get data of previous rides
    void getPreviousRides(){
        Log.d(TAG, "getPreviousRides: called in farg previous ride");
        CollectionReference colRef = db.collection("Users")
                .document(mAuth.getUid())
                .collection("Previous Rides");

        colRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.d(TAG, "listen:error", error);
                    return;
                }

                for (DocumentChange dc : value.getDocumentChanges()) {
                    switch (dc.getType()) {
                        case ADDED:
                            Log.d(TAG, "read previous ride in frag_previousRide: " + dc.getDocument().getData());
                            PreviousRide obj = dc.getDocument().toObject(PreviousRide.class);
                            prevRides.add(obj);
                            break;
                        case MODIFIED:
                            Log.d(TAG, "Modified previous ride in frag_previousRide: " + dc.getDocument().getData());
                            break;
                        case REMOVED:
                            Log.d(TAG, "Removed previous ride in frag_previousRide: " + dc.getDocument().getData());
                            break;
                    }
                    Log.d(TAG, "onEvent: count previous rides=>"+prevRides.size());
                    rvAdapter.notifyDataSetChanged();
                }
            }
        });



    }

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