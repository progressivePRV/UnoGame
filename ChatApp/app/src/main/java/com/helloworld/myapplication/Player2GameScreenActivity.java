package com.helloworld.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class Player2GameScreenActivity extends AppCompatActivity implements PlayerCardListAdapter.InteractWithPlayerCardList {


    private static final String TAG = "okay";
    private RecyclerView usersRecyclerView;
    private RecyclerView.Adapter usersAdapter;
    private RecyclerView.LayoutManager usersLayoutManager;
    ArrayList<UnoCardClass> thisPlayerCard = new ArrayList<>();
    private FirebaseFirestore db;
    private String chatRoomName;
    private GameDetailsClass gameDetailsClass;
    boolean isCardPicked = false;
    TextView player1Name;
    TextView player2Name;
    boolean isUno=false;

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

        findViewById(R.id.button_skipTurnPlayer2).setEnabled(false);

        player1Name=findViewById(R.id.textViewPlayer1Name);
        player2Name=findViewById(R.id.textViewPlayer2Name);
        player1Name.setText(gameDetailsClass.player1Name);
        player2Name.setText(gameDetailsClass.player2Name);

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

                    if(gameDetailsClass.turn.equals("player1")){
                        player1Name.setTextColor(Color.argb(255,34,177,76));
                        player1Name.setTypeface(null,Typeface.BOLD);

                        player2Name.setTextColor(Color.GRAY);
                        player2Name.setTypeface(Typeface.DEFAULT);

                        findViewById(R.id.button_skipTurnPlayer2).setEnabled(false);
                        findViewById(R.id.Uno_image_deck5).setEnabled(false);
                    }
                    else if(gameDetailsClass.turn.equals("player2")){
                        isCardPicked = false;

                        player1Name.setTextColor(Color.GRAY);
                        player1Name.setTypeface(Typeface.DEFAULT);

                        player2Name.setTextColor(Color.argb(255,34,177,76));
                        player2Name.setTypeface(null,Typeface.BOLD);

                        //findViewById(R.id.button_skipTurnPlayer2).setEnabled(true);
                        findViewById(R.id.Uno_image_deck5).setEnabled(true);
                    }

                    if(gameDetailsClass.player1Cards!=null && gameDetailsClass.player1Cards.size()==1){
                        if(!isUno){
                            isUno=true;
                            Toast.makeText(Player2GameScreenActivity.this, "Player 1: UNO!", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else if(gameDetailsClass.player1Cards!=null && gameDetailsClass.player1Cards.size()>1){
                        isUno=false;
                    }

                    if(gameDetailsClass.player2Cards!=null && gameDetailsClass.player2Cards.size()==0){
                        Toast.makeText(Player2GameScreenActivity.this, "Game Over. You won!!!", Toast.LENGTH_SHORT).show();
                    }
                    if(gameDetailsClass.player1Cards!=null && gameDetailsClass.player1Cards.size()==0){
                        Toast.makeText(Player2GameScreenActivity.this, "Game Over. Player 1 won", Toast.LENGTH_SHORT).show();
                    }

                    if(gameDetailsClass.discardCards!=null && gameDetailsClass.discardCards.size()>0){
                        UnoCardClass topDiscardCard = gameDetailsClass.discardCards.get(gameDetailsClass.discardCards.size()-1);
                        renderDiscardCard(topDiscardCard);
                    }

                } else {
                    System.out.print("Current data: null");
                }
            }
        });


        findViewById(R.id.Uno_image_deck5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(gameDetailsClass.deckCards!=null && gameDetailsClass.deckCards.size()>0 && !isCardPicked){
                    UnoCardClass topCard = gameDetailsClass.deckCards.get(0);
                    docRef.update("deckCards",FieldValue.arrayRemove(topCard)).addOnSuccessListener(Player2GameScreenActivity.this, new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            docRef.update("player2Cards",FieldValue.arrayUnion(topCard)).addOnSuccessListener(Player2GameScreenActivity.this, new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    isCardPicked = true;
                                    findViewById(R.id.button_skipTurnPlayer2).setEnabled(true);
                                    Toast.makeText(Player2GameScreenActivity.this, "New card picked", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(Player2GameScreenActivity.this, new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(Player2GameScreenActivity.this, "Card could not be picked", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }).addOnFailureListener(Player2GameScreenActivity.this, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(Player2GameScreenActivity.this, "Card could not be picked", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else{
                    //logic to be written
                    if(isCardPicked){
                        Toast.makeText(Player2GameScreenActivity.this, "Card already picked. Cannot pick another card", Toast.LENGTH_SHORT).show();
                    }
                    else if(gameDetailsClass.deckCards==null || gameDetailsClass.deckCards.size()==0){
                        reshuffleDeckAndPickCard(gameDetailsClass.discardCards);
                    }
                }

            }
        });

        findViewById(R.id.button_skipTurnPlayer2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                docRef.update("turn","player1").addOnSuccessListener(Player2GameScreenActivity.this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(Player2GameScreenActivity.this, "Turn Skipped", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(Player2GameScreenActivity.this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Player2GameScreenActivity.this, "Turn could not be skipped", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });


    }

    @Override
    public void PlayCard(int postion) {

        // what ever you want to do after user clicker on car

        if(gameDetailsClass.turn.equals("player2")){
            if(isCardValid(gameDetailsClass,postion)){
                UnoCardClass card = thisPlayerCard.remove(postion);
                usersAdapter.notifyDataSetChanged();


                DocumentReference docRef = db.collection("ChatRoomList")
                        .document(chatRoomName)
                        .collection("UnoGame")
                        .document(gameDetailsClass.player1Id);

                docRef.update("player2Cards", FieldValue.arrayRemove(card)).addOnSuccessListener(Player2GameScreenActivity.this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        docRef.update("discardCards",FieldValue.arrayUnion(card)).addOnSuccessListener(Player2GameScreenActivity.this, new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                if(card.number!=10){
                                    docRef.update("turn","player1").addOnSuccessListener(Player2GameScreenActivity.this, new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(Player2GameScreenActivity.this, "you played=>"+card, Toast.LENGTH_SHORT).show();
                                        }
                                    }).addOnFailureListener(Player2GameScreenActivity.this, new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(Player2GameScreenActivity.this, "Problem with played card", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                                else{
                                    findViewById(R.id.button_skipTurnPlayer2).setEnabled(false);
                                    Toast.makeText(Player2GameScreenActivity.this, "you played=>"+card+" You can play again", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }).addOnFailureListener(Player2GameScreenActivity.this, new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(Player2GameScreenActivity.this, "Problem with played card", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).addOnFailureListener(Player2GameScreenActivity.this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Player2GameScreenActivity.this, "Problem with played card", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            else{
                Toast.makeText(this, "This card cannot be played", Toast.LENGTH_SHORT).show();
            }
        }
        else{
            Toast.makeText(this, "Cannot play now, player 1 playing", Toast.LENGTH_SHORT).show();
        }




        //card is user played card

        //you need to block recycler view once player played

    }

    public void renderDiscardCard(UnoCardClass card){

        CardView cardView = findViewById(R.id.card_view_forDiscardCard);
        TextView tv = findViewById(R.id.tv_in_discardCard);

        switch (card.color){
            case "red":
                cardView.setBackgroundResource(R.drawable.uno_red);
                tv.setTextColor(Color.argb(255,237,28,36));
                break;
            case "green":
                cardView.setBackgroundResource(R.drawable.uno_green);
                tv.setTextColor(Color.argb(255,34,177,76));
                break;
            case "blue":
                cardView.setBackgroundResource(R.drawable.uno_blue);
                tv.setTextColor(Color.argb(255,63,72,204));
                break;
            case "yellow":
                cardView.setBackgroundResource(R.drawable.uno_yellow);
                tv.setTextColor(Color.argb(255,255,242,0));
                break;
            default:
                //cardView.setCardBackgroundColor(Color.BLACK);
                cardView.setBackgroundResource(R.drawable.black_square);
                tv.setTextColor(Color.WHITE);
        }

        if(card.color.equals("black") || card.number == 10){
            if (card.number <= 4){
                tv.setText("+4");
                //holder.tv.setTop(5);
                //tv.setPadding(0,10,0,0);
                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP,45);
            }else{
                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP,40);
                //tv.setPadding(0,30,0,0);
                tv.setText("skip");
            }
        }else{
            tv.setText(""+card.number);
        }
    }

    public void reshuffleDeckAndPickCard(ArrayList<UnoCardClass> discardCards){
        UnoCardClass topCard = discardCards.get(discardCards.size()-1);
        discardCards.remove(discardCards.size()-1);

        Collections.shuffle(discardCards);

        gameDetailsClass.deckCards = discardCards;
        gameDetailsClass.discardCards = new ArrayList<>(Arrays.asList(topCard));

        DocumentReference docRef = db.collection("ChatRoomList")
                .document(chatRoomName)
                .collection("UnoGame")
                .document(gameDetailsClass.player1Id);

        docRef.set(gameDetailsClass).addOnSuccessListener(Player2GameScreenActivity.this, new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(Player2GameScreenActivity.this, "Deck Refilled", Toast.LENGTH_SHORT).show();
                if(gameDetailsClass.deckCards!=null && gameDetailsClass.deckCards.size()>0 && !isCardPicked){
                    UnoCardClass topCard = gameDetailsClass.deckCards.get(0);
                    docRef.update("deckCards",FieldValue.arrayRemove(topCard)).addOnSuccessListener(Player2GameScreenActivity.this, new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            docRef.update("player2Cards",FieldValue.arrayUnion(topCard)).addOnSuccessListener(Player2GameScreenActivity.this, new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    isCardPicked = true;
                                    findViewById(R.id.button_skipTurnPlayer2).setEnabled(true);
                                    Toast.makeText(Player2GameScreenActivity.this, "New card picked", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(Player2GameScreenActivity.this, new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(Player2GameScreenActivity.this, "Card could not be picked", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }).addOnFailureListener(Player2GameScreenActivity.this, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(Player2GameScreenActivity.this, "Card could not be picked", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).addOnFailureListener(Player2GameScreenActivity.this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Player2GameScreenActivity.this, "Deck could not be refilled", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public boolean isCardValid(GameDetailsClass gameDetailsClass,int position){

        UnoCardClass playingCard = gameDetailsClass.player2Cards.get(position);
        UnoCardClass topCard = gameDetailsClass.discardCards.get(gameDetailsClass.discardCards.size()-1);

        if(topCard.number<=9 && !topCard.color.equals("black")){
            if(playingCard.number==10 && !playingCard.color.equals(topCard.color)){
                return false;
            }
            else if(!playingCard.color.equals("black") && playingCard.number!=topCard.number && !playingCard.color.equals(topCard.color)){
                return false;
            }
        }

        if(topCard.number==10){
            if(!playingCard.color.equals("black") && !playingCard.color.equals(topCard.color) && playingCard.number!=10){
                return false;
            }
        }

//        if(topCard.color.equals("black")){
//
//        }

        return true;
    }
}