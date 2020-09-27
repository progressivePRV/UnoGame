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
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

public class GameRequestScreenActivity extends AppCompatActivity {

    private String chatRoomName;
    private GameDetailsClass gameDetailsClass;
    private UserProfile user;
    private ProgressDialog progressDialog;
    private FirebaseFirestore db;
    boolean isYesClicked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_request_screen);
//        intent.putExtra("chatRoomName",chatRoomName);
//        intent.putExtra("GameDetailsClass", added);
//        intent.putExtra("userProfile",user);
        db = FirebaseFirestore.getInstance();

        chatRoomName = getIntent().getExtras().getString("chatRoomName");
        gameDetailsClass = (GameDetailsClass) getIntent().getExtras().getSerializable("GameDetailsClass");
        user = (UserProfile) getIntent().getExtras().getSerializable("userProfile");

        attachListenerToGame();

        //if the player selected no then he is added to the game rejected player list
        findViewById(R.id.buttonPlayNo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addRejectedPlayer();
            }
        });

        //if the player selects yes then the value should be updated in the firestore and then the player should go to the screen eventually
        //lets see..
        findViewById(R.id.buttonPlayYes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isYesClicked = true;
                gameDetailsClass.player2Id = user.uid;
                gameDetailsClass.gameState = "IN_PROGRESS";
                showProgressBarDialog();
                db.collection("ChatRoomList")
                        .document(chatRoomName)
                        .collection("UnoGame")
                        .document(gameDetailsClass.player1Id)
                        .set(gameDetailsClass)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    hideProgressBarDialog();
                                } else {
                                    hideProgressBarDialog();
                                    Toast.makeText(GameRequestScreenActivity.this, "Some issue occured in game", Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        hideProgressBarDialog();
                        Toast.makeText(GameRequestScreenActivity.this, "Some error occured. Please try again", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            }
        });


        //The request will be there only for 20 seconds. If no then the player will be sent to the rejected players.
        Handler handler=new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(!isYesClicked){
                    Toast.makeText(GameRequestScreenActivity.this, "Ride rejected", Toast.LENGTH_SHORT).show();
                    addRejectedPlayer();
                }
            }
        },20000);

    }

        public void attachListenerToGame(){
            //Adding snapshot listener for accepted players so if the user has accepted to the firestore then the game should progress
            DocumentReference docRef = db.collection("ChatRoomList")
                    .document(chatRoomName)
                    .collection("UnoGame")
                    .document(gameDetailsClass.player1Id);

            docRef.addSnapshotListener(GameRequestScreenActivity.this, new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException error) {
                    if (error != null) {
                        Log.d("demo:", error+"");
                        return;
                    }

                    if (snapshot != null && snapshot.exists()) {
                        //this is to check if someone has accepted the request for the game
                        gameDetailsClass = snapshot.toObject(GameDetailsClass.class);
                        if(gameDetailsClass.gameState.equals("IN_PROGRESS")){
                            if(gameDetailsClass.player2Id.equals(user.uid)){
                                if(gameDetailsClass.deckCards.size() > 0){
                                    hideProgressBarDialog();
                                    Toast.makeText(GameRequestScreenActivity.this, "Game will be started soon.. Preparing the game", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(GameRequestScreenActivity.this, Player1GameScreenActivity.class);
                                    intent.putExtra("chatRoomName",chatRoomName);
                                    intent.putExtra("gameDetails",gameDetailsClass);
                                    startActivity(intent);
                                }
                            }else{
                                Toast.makeText(GameRequestScreenActivity.this, "This game is not available anymore", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        }
                    } else {
                        System.out.print("Current data: null");
                    }
                }
            });
        }



    //addRejectedPlayer adds the rejected player to the rejectedPlayers to the firestore list
    public void addRejectedPlayer(){
        DocumentReference rejectReference =  db.collection("ChatRoomList")
                .document(chatRoomName)
                .collection("UnoGame")
                .document(gameDetailsClass.player1Id);

        //gameDetailsClass.rejectedPlayers
        rejectReference.update("rejectedPlayers",
                FieldValue.arrayUnion(user.uid))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(GameRequestScreenActivity.this, "Some error occured. Please try again", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //for showing the progress dialog
    public void showProgressBarDialog()
    {
        progressDialog = new ProgressDialog(GameRequestScreenActivity.this);
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