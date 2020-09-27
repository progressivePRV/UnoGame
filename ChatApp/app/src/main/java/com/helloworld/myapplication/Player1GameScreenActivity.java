package com.helloworld.myapplication;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;

public class Player1GameScreenActivity extends AppCompatActivity {

    private RecyclerView mainRecyclerView;
    private RecyclerView.Adapter mainAdapter;
    private RecyclerView.LayoutManager mainLayoutManager;
    private RecyclerView usersRecyclerView;
    private RecyclerView.Adapter usersAdapter;
    private RecyclerView.LayoutManager usersLayoutManager;
    ArrayList<UnoCardClass> unoCardClassArrayList = new ArrayList<>();
    private FirebaseFirestore db;
    private String chatRoomName;
    private GameDetailsClass gameDetailsClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player1_game_screen);

        //Adding listener to the document for playing
        usersRecyclerView = (RecyclerView) findViewById(R.id.playerCardRecyclerView);
        usersLayoutManager = new LinearLayoutManager(Player1GameScreenActivity.this,
                LinearLayoutManager.HORIZONTAL, false);
        usersRecyclerView.setLayoutManager(usersLayoutManager);

        usersAdapter = new PlayerCardListAdapter(unoCardClassArrayList, Player1GameScreenActivity.this);
        usersRecyclerView.setAdapter(usersAdapter);

        db = FirebaseFirestore.getInstance();

        chatRoomName = getIntent().getExtras().getString("chatRoomName");
        gameDetailsClass = (GameDetailsClass)getIntent().getExtras().getSerializable("gameDetails");

        final String documentId = gameDetailsClass.player1Id;

        DocumentReference docRef = db.collection("ChatRoomList")
                .document(chatRoomName)
                .collection("UnoGame")
                .document(documentId);

        docRef.addSnapshotListener(Player1GameScreenActivity.this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.d("demo:", error+"");
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    //this is to check if someone has accepted the request for the game
                    gameDetailsClass = snapshot.toObject(GameDetailsClass.class);
                    unoCardClassArrayList = new ArrayList<>();
                    unoCardClassArrayList = gameDetailsClass.player1Cards;

                    usersAdapter.notifyDataSetChanged();
                } else {
                    System.out.print("Current data: null");
                }
            }
        });

    }
}