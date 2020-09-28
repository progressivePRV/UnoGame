package com.helloworld.myapplication;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;

public class Player2GameScreenActivity extends AppCompatActivity implements PlayerCardListAdapter.InteractWithPlayerCardList {


    private static final String TAG = "okay";
    private RecyclerView usersRecyclerView;
    private RecyclerView.Adapter usersAdapter;
    private RecyclerView.LayoutManager usersLayoutManager;
    ArrayList<UnoCardClass> thisPlayerCard = new ArrayList<>();
    private FirebaseFirestore db;
    private String chatRoomName;
    private GameDetailsClass gameDetailsClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player2_game_screen);

        //Adding listener to the document for playing
        usersRecyclerView = (RecyclerView) findViewById(R.id.playerCardRecyclerView);
        usersLayoutManager = new LinearLayoutManager(Player2GameScreenActivity.this,
                LinearLayoutManager.HORIZONTAL, false);
        usersRecyclerView.setLayoutManager(usersLayoutManager);

        usersAdapter = new PlayerCardListAdapter(thisPlayerCard, Player2GameScreenActivity.this);
        usersRecyclerView.setAdapter(usersAdapter);

        db = FirebaseFirestore.getInstance();

        chatRoomName = getIntent().getExtras().getString("chatRoomName");
        gameDetailsClass = (GameDetailsClass)getIntent().getExtras().getSerializable("gameDetails");

        final String documentId = gameDetailsClass.player1Id;

        DocumentReference docRef = db.collection("ChatRoomList")
                .document(chatRoomName)
                .collection("UnoGame")
                .document(documentId);

        docRef.addSnapshotListener(Player2GameScreenActivity.this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.d("demo:", error+"");
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    //this is to check if someone has accepted the request for the game
                    gameDetailsClass = snapshot.toObject(GameDetailsClass.class);
                    thisPlayerCard.clear();
                    thisPlayerCard.addAll(gameDetailsClass.player2Cards);
                    Log.d(TAG, "onEvent: in Player2GameScreenActivity adater item count=>"+usersAdapter.getItemCount());
                    usersAdapter.notifyDataSetChanged();
                } else {
                    System.out.print("Current data: null");
                }
            }
        });


    }

    @Override
    public void PlayCard(int postion) {

        // what ever you want to do after user clicker on car

        UnoCardClass card = thisPlayerCard.remove(postion);
        Toast.makeText(this, "you played=>"+card, Toast.LENGTH_SHORT).show();
        usersAdapter.notifyDataSetChanged();


        //card is user played card

        //you need to block recycler view once player played

    }
}