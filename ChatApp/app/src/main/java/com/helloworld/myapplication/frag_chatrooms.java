package com.helloworld.myapplication;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link frag_chatrooms#newInstance} factory method to
 * create an instance of this fragment.
 */
public class frag_chatrooms extends Fragment implements ChatRoomAdapter.InteractWithRecyclerView{

    private static final String TAG = "okay";
    private FirebaseFirestore db;
    FirebaseAuth mAuth;
    private RecyclerView mainRecyclerView;
    private RecyclerView.Adapter mainAdapter;
    private RecyclerView.LayoutManager mainLayoutManager;
    private ProgressDialog progressDialog;
    DatabaseReference mDatabase;
    FirebaseStorage storage;
    StorageReference storageReference;
    UserProfile user;
    ArrayList<String> globalChatRoomList = new ArrayList<>();
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public frag_chatrooms() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment frag_chatrooms.
     */
    // TODO: Rename and change types and number of parameters
    public static frag_chatrooms newInstance(String param1, String param2) {
        frag_chatrooms fragment = new frag_chatrooms();
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chatrooms, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.d(TAG, "onViewCreated: fragment chatroom is called on screen");
        Button chatRoomAdd = getView().findViewById(R.id.ButtonChatRoomAdd);
        chatRoomAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ChatRoomCreateActivity.class);
                startActivity(intent);
            }
        });


        // to get user from sidebaractivity and passing to new chatroom and enter a specific chatroom




        globalChatRoomList = new ArrayList<>();

        db = FirebaseFirestore.getInstance();
        mainRecyclerView = (RecyclerView) getView().findViewById(R.id.chatRoomRecyclerView);
        mainLayoutManager = new LinearLayoutManager(getActivity());
        mainRecyclerView.setLayoutManager(mainLayoutManager);
        // specify an adapter
        mainAdapter = new ChatRoomAdapter(globalChatRoomList, frag_chatrooms.this);
        mainRecyclerView.setAdapter(mainAdapter);


        //Adding snapshot listener to the firestore
        db.collection("ChatRoomList").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshots,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("TAG", "listen:error", e);
                    return;
                }

                for (DocumentChange dc : snapshots.getDocumentChanges()) {
                    switch (dc.getType()) {
                        case ADDED:
                            globalChatRoomList.add(dc.getDocument().getId());
                            break;
                    }
                }
                mainAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void getDetails(final String chatRoom, int position) {
        Log.d("demo", "It is getting the chatRoom details to go to the next Activity");
        mAuth= FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser()!=null){
            showProgressBarDialog();

                    Log.d(TAG, "onDataChange: frag chatroom  got user from sidebar activity");
                    SidebarActivity sidebarActivity = (SidebarActivity) getActivity();
                    user = sidebarActivity.u;

                    //Storing all the info along with the message in the firestore
                    db.collection("ChatRoomList").document(chatRoom)
                            .collection("CurrentViewers")
                            .document(user.uid)
                            .set(user)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
//                                     Toast.makeText(ChatRoomActivity.this, "Message sent!", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(getActivity(), ChatRoomActivity.class);
                                    intent.putExtra("chatRoomName",chatRoom);
                                    intent.putExtra("user",user);
                                    startActivity(intent);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getActivity(), "Error occurred in setting up the chat room. Please try again!", Toast.LENGTH_SHORT).show();
                        }
                    });
                    hideProgressBarDialog();

        }else{
            Toast.makeText(getActivity(), "User Not Logged In", Toast.LENGTH_SHORT).show();
            Log.d("demo","User not logged in");
        }
    }

    //for showing the progress dialog
    public void showProgressBarDialog()
    {
        progressDialog = new ProgressDialog(getActivity());
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