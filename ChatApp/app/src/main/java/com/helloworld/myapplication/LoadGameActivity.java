package com.helloworld.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;

public class LoadGameActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private ProgressDialog progressDialog;
    private String chatRoomName;
    private GameDetailsClass gameDetailsClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_game);
        db = FirebaseFirestore.getInstance();

        chatRoomName = getIntent().getExtras().getString("chatRoomName");
        gameDetailsClass = (GameDetailsClass) getIntent().getExtras().getSerializable("gameDetails");


        //When the buttonStartGame is clicked then in the firestore the details
        // will be stored and the game state will be displayed as requested.
        findViewById(R.id.buttonStartGame).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgressBarDialog();
                db.collection("ChatRoomList")
                        .document(chatRoomName)
                        .collection("UnoGame")
                        .document(gameDetailsClass.player1Id)
                        .set(gameDetailsClass)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    attachListenerToGame();
                                    Handler handler=new Handler();
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(LoadGameActivity.this, "Sorry No Players found at this time. Please try again later", Toast.LENGTH_SHORT).show();

                                            //have to delete that request from the firestore
                                            //deleteGameRequest

                                            finish();
                                        }
                                    },30000);
                                }
                                else {
                                    hideProgressBarDialog();
                                    Toast.makeText(LoadGameActivity.this, "Some issue occured in game", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        hideProgressBarDialog();
                        Toast.makeText(LoadGameActivity.this, "Some error occured. Please try again", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    public void attachListenerToGame(){
        //Adding snapshot listener for accepted players so if the user has accepted to the firestore then the game should progress
        DocumentReference docRef = db.collection("ChatRoomList")
                .document(chatRoomName)
                .collection("UnoGame")
                .document(gameDetailsClass.player1Id);

        docRef.addSnapshotListener(LoadGameActivity.this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.d("demo:", error+"");
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    //this is to check if someone has accepted the request for the game
                    gameDetailsClass = snapshot.toObject(GameDetailsClass.class);
                    if(gameDetailsClass.gameState.equals("IN_PROGRESS")&& gameDetailsClass.player2Id!=null){
                        Toast.makeText(LoadGameActivity.this, "Game will be started soon.. Preparing the game", Toast.LENGTH_SHORT).show();
                        if(gameDetailsClass.deckCards.size() == 0) {
                            startGame();
                        }
                    }
                } else {
                    System.out.print("Current data: null");
                }
            }
        });
    }

    public void startGame(){
        showProgressBarDialog();
        UnoCardClass unoCardClass;
        ArrayList<UnoCardClass> unoCardClassArrayList = new ArrayList<>();
        //for red color
        for(int i=1; i<=9; i++){
            unoCardClass = new UnoCardClass();
            unoCardClass.color = "red";
            unoCardClass.number = i;
            unoCardClassArrayList.add(unoCardClass);
        }
        //for yellow color
        for(int i=1; i<=9; i++){
            unoCardClass = new UnoCardClass();
            unoCardClass.color = "yellow";
            unoCardClass.number = i;
            unoCardClassArrayList.add(unoCardClass);
        }
        //for green color
        for(int i=1; i<=9; i++){
            unoCardClass = new UnoCardClass();
            unoCardClass.color = "green";
            unoCardClass.number = i;
            unoCardClassArrayList.add(unoCardClass);
        }
        //for blue color
        for(int i=1; i<=9; i++){
            unoCardClass = new UnoCardClass();
            unoCardClass.color = "blue";
            unoCardClass.number = i;
            unoCardClassArrayList.add(unoCardClass);
        }

        gameDetailsClass.deckCards = unoCardClassArrayList;

        db.collection("ChatRoomList")
                .document(chatRoomName)
                .collection("UnoGame")
                .document(gameDetailsClass.player1Id)
                .set(gameDetailsClass)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            hideProgressBarDialog();

                            //here the deck is ready. So both the palyers have to go to the gameScreenactivtiy
                            Intent intent = new Intent(LoadGameActivity.this, GameScreenActivity.class);
                            intent.putExtra("gameDetails",gameDetailsClass);
                            startActivity(intent);
                        }
                        else {
                            hideProgressBarDialog();
                            Toast.makeText(LoadGameActivity.this, "Some issue occured in game", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                hideProgressBarDialog();
                Toast.makeText(LoadGameActivity.this, "Some error occured. Please try again", Toast.LENGTH_SHORT).show();
            }
        });

    }

    //for showing the progress dialog
    public void showProgressBarDialog()
    {
        progressDialog = new ProgressDialog(LoadGameActivity.this);
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