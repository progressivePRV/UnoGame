package com.helloworld.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class Player1GameScreenActivity extends AppCompatActivity implements PlayerCardListAdapter.InteractWithPlayerCardList{

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
    String colorName;
    private ProgressDialog progressDialog;
    boolean isWildCard=false;
    // handler addded for monitoring if user plays in 1 minute/30 second or not
//    private Runnable gameCountDownRunnable;
//    private Handler gameCountDownHandler;

    //CountDownTimer
    private CountDownTimer countDownTimer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player1_game_screen);
        Log.d(TAG, "onCreate: called in Player1GameScreenActivity");

        /// initializing handler
//        InitializeHandler();

        //Adding listener to the document for playing
        usersRecyclerView = (RecyclerView) findViewById(R.id.playerCardRecyclerView);
        usersLayoutManager = new LinearLayoutManager(Player1GameScreenActivity.this,
                LinearLayoutManager.HORIZONTAL, false);
        usersRecyclerView.setLayoutManager(usersLayoutManager);

        usersAdapter = new PlayerCardListAdapter(thisPlayerCard, Player1GameScreenActivity.this);
        usersRecyclerView.setAdapter(usersAdapter);
        //usersRecyclerView.setItemAnimator(new Slide());

        db = FirebaseFirestore.getInstance();

        chatRoomName = getIntent().getExtras().getString("chatRoomName");
        gameDetailsClass = (GameDetailsClass)getIntent().getExtras().getSerializable("gameDetails");

        final String documentId = gameDetailsClass.player1Id;

        countDownTimer = new CountDownTimer(60*1000, 1000) {
            @Override
            public void onTick(long l) {
                long minute = l/1000/60;
                long second = (l - minute*60*1000)/1000;
                Log.d("Demo", "onTick: "+ minute+ " "+second);
                TextView timer = findViewById(R.id.timeRemainingTextPlayer1);
                Log.d("demo","timer value : "+minute+" : "+second);
                timer.setText(minute+" : "+second);
                timer.setTextColor(Color.BLACK);
                if(second <= 15){
                    timer.setTextColor(Color.RED);
                }
            }

            @Override
            public void onFinish() {
                Toast.makeText(Player1GameScreenActivity.this, "This game is done as you have not moved in 1 minute", Toast.LENGTH_SHORT).show();
                onBackUpdate();
                //update it in the firestore and then finish the activity
            }
        };

        findViewById(R.id.button_skipTurnPlayer1).setEnabled(false);

        player1Name=findViewById(R.id.textViewPlayer1Name);
        player2Name=findViewById(R.id.textViewPlayer2Name);
        player1Name.setText(gameDetailsClass.player1Name);
        player2Name.setText(gameDetailsClass.player2Name);

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
                    // before adding new card in empty object it needed to be clean and then added

                    if(gameDetailsClass.gameState.equals("COMPLETED")){
                        //It means that the game state has been completed and the user has to go back to the previous activity
                        finish();
                    }

                    if(gameDetailsClass.gameState.equals("PLAYER1_GAVEUP")){
                        finish();
                    }

                    if(gameDetailsClass.gameState.equals("PLAYER2_GAVEUP")){
                        Toast.makeText(Player1GameScreenActivity.this, "Player 2 has given up the game.. You Won!", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    Log.d(TAG, "onEvent: in player1GameScreenActivity, player1 got cards=>"+gameDetailsClass.player1Cards);
                    thisPlayerCard.clear();
                    thisPlayerCard.addAll(gameDetailsClass.player1Cards);
                    usersAdapter.notifyDataSetChanged();
                    Log.d(TAG, "onEvent: in player1GameScreenActivity, adapter has items=>>"+usersAdapter.getItemCount());


                    if(gameDetailsClass.turn.equals("player2")){
                        player1Name.setTextColor(Color.GRAY);
                        player1Name.setTypeface(Typeface.DEFAULT);

                        player2Name.setTextColor(Color.argb(255,34,177,76));
                        player2Name.setTypeface(null,Typeface.BOLD);

                        findViewById(R.id.button_skipTurnPlayer1).setEnabled(false);
                        findViewById(R.id.Uno_image_deck5).setEnabled(false);
                    }
                    else if(gameDetailsClass.turn.equals("player1")){
                        //starting the countDownTimer;
                        countDownTimer.start();

                        isCardPicked = false;

                        player1Name.setTextColor(Color.argb(255,34,177,76));
                        player1Name.setTypeface(null,Typeface.BOLD);

                        player2Name.setTextColor(Color.GRAY);
                        player2Name.setTypeface(Typeface.DEFAULT);

                        if(gameDetailsClass.plusFourCurrentColor!=null && !gameDetailsClass.plusFourCurrentColor.isEmpty() && !isWildCard){
                            findViewById(R.id.Uno_image_deck5).setEnabled(false);
                            findViewById(R.id.button_skipTurnPlayer1).setEnabled(true);
                        }
                        else{
                            findViewById(R.id.Uno_image_deck5).setEnabled(true);
                            findViewById(R.id.button_skipTurnPlayer1).setEnabled(false);
                        }

                    }

                    if(gameDetailsClass.plusFourCurrentColor!=null && !gameDetailsClass.plusFourCurrentColor.isEmpty()){
                        isWildCard=true;
                        TextView wildCardColor = findViewById(R.id.textViewWildColor);
                        wildCardColor.setText("Wild Card Color: "+gameDetailsClass.plusFourCurrentColor);
                        //displayPickedColor(gameDetailsClass.plusFourCurrentColor);
                    }
                    else{
                        isWildCard=false;
                        TextView wildCardColor = findViewById(R.id.textViewWildColor);
                        wildCardColor.setText("");
                    }

                    if(gameDetailsClass.player2Cards!=null && gameDetailsClass.player2Cards.size()==1){
                        if(!isUno){
                            isUno=true;
                            Toast.makeText(Player1GameScreenActivity.this, "Player 2: UNO!", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else if(gameDetailsClass.player2Cards!=null && gameDetailsClass.player2Cards.size()>1){
                        isUno=false;
                    }

                    if(gameDetailsClass.player1Cards!=null && gameDetailsClass.player1Cards.size()==0){
                        Toast.makeText(Player1GameScreenActivity.this, "Game Over. You won!!!", Toast.LENGTH_SHORT).show();
                        updateWinner();
                    }
                    if(gameDetailsClass.player2Cards!=null && gameDetailsClass.player2Cards.size()==0){
                        Toast.makeText(Player1GameScreenActivity.this, "Game Over. Player 2 won", Toast.LENGTH_SHORT).show();
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
                    docRef.update("deckCards",FieldValue.arrayRemove(topCard)).addOnSuccessListener(Player1GameScreenActivity.this, new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            docRef.update("player1Cards",FieldValue.arrayUnion(topCard)).addOnSuccessListener(Player1GameScreenActivity.this, new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    isCardPicked = true;
                                    findViewById(R.id.button_skipTurnPlayer1).setEnabled(true);
                                    Toast.makeText(Player1GameScreenActivity.this, "New card picked", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(Player1GameScreenActivity.this, new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(Player1GameScreenActivity.this, "Card could not be picked", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }).addOnFailureListener(Player1GameScreenActivity.this, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(Player1GameScreenActivity.this, "Card could not be picked", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else{
                    //logic to be written
                    if(isCardPicked){
                        Toast.makeText(Player1GameScreenActivity.this, "Card already picked. Cannot pick another card", Toast.LENGTH_SHORT).show();
                    }
                    else if(gameDetailsClass.deckCards==null || gameDetailsClass.deckCards.size()==0){
                        reshuffleDeckAndPickCard(gameDetailsClass.discardCards);
                    }
                }

            }
        });

        findViewById(R.id.button_skipTurnPlayer1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                docRef.update("turn","player2").addOnSuccessListener(Player1GameScreenActivity.this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        countDownTimer.cancel();
                        Toast.makeText(Player1GameScreenActivity.this, "Turn Skipped", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(Player1GameScreenActivity.this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Player1GameScreenActivity.this, "Turn could not be skipped", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    //here since the player1  has won the player 1 will update the firebase game as game completed.
    public void updateWinner(){
        //update the state of the game to completed
//        showProgressBarDialog();
        db.collection("ChatRoomList")
                .document(chatRoomName)
                .collection("UnoGame")
                .document(gameDetailsClass.player1Id)
                .update("gameState","COMPLETED")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
//                            hideProgressBarDialog();
                        } else {
//                            hideProgressBarDialog();
                            Toast.makeText(Player1GameScreenActivity.this, "Some issue occured in game", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                hideProgressBarDialog();
                Toast.makeText(Player1GameScreenActivity.this, "Some error occured. Please try again", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    //for showing the progress dialog
    public void showProgressBarDialog()
    {
        progressDialog = new ProgressDialog(Player1GameScreenActivity.this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    //for hiding the progress dialog
    public void hideProgressBarDialog()
    {
        progressDialog.dismiss();
    }

    @Override
    public void PlayCard(int postion) {
        // what ever you want to do after user clicker on car

        if(gameDetailsClass.turn.equals("player1")){
            if(isCardValid(gameDetailsClass,postion)){
                UnoCardClass card = thisPlayerCard.remove(postion);
                countDownTimer.cancel();
                usersAdapter.notifyDataSetChanged();

                DocumentReference docRef = db.collection("ChatRoomList")
                        .document(chatRoomName)
                        .collection("UnoGame")
                        .document(gameDetailsClass.player1Id);

                docRef.update("player1Cards",FieldValue.arrayRemove(card),"plusFourCurrentColor",null).addOnSuccessListener(Player1GameScreenActivity.this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //Toast.makeText(Player1GameScreenActivity.this, "you played=>"+card, Toast.LENGTH_SHORT).show();
                        docRef.update("discardCards",FieldValue.arrayUnion(card)).addOnSuccessListener(Player1GameScreenActivity.this, new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                if(card.number!=10 && !card.color.equals("black")){
                                    docRef.update("turn","player2").addOnSuccessListener(Player1GameScreenActivity.this, new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(Player1GameScreenActivity.this, "you played=>"+card, Toast.LENGTH_SHORT).show();
                                            countDownTimer.cancel();
                                        }
                                    }).addOnFailureListener(Player1GameScreenActivity.this, new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(Player1GameScreenActivity.this, "Problem with played card", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                                else if(card.number==10){

                                    findViewById(R.id.button_skipTurnPlayer1).setEnabled(false);
                                    Toast.makeText(Player1GameScreenActivity.this, "you played=>"+card+" You can play again", Toast.LENGTH_SHORT).show();
                                    countDownTimer.cancel();
//                                    countDownTimer.start();


                                }
                                else if(card.color.equals("black")){
                                    setColorAndTransferTurn(docRef,gameDetailsClass);
                                }
                            }
                        }).addOnFailureListener(Player1GameScreenActivity.this, new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(Player1GameScreenActivity.this, "Problem with played card", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).addOnFailureListener(Player1GameScreenActivity.this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Player1GameScreenActivity.this, "Problem with played card", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            else{
                Toast.makeText(this, "This card cannot be played", Toast.LENGTH_SHORT).show();
            }
        }
        else{
            Toast.makeText(this, "Cannot play now, player 2 playing", Toast.LENGTH_SHORT).show();
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
                //tv.setTop(100);
                tv.setPadding(0,85,0,0);
                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP,85);
            }else{
                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP,60);
                //tv.setTop(100);
                tv.setPadding(0,110,0,0);
                tv.setText("skip");
            }
        }else{
            tv.setText(""+card.number);
            tv.setPadding(0,0,0,0);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP,130);
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

        docRef.set(gameDetailsClass).addOnSuccessListener(Player1GameScreenActivity.this, new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(Player1GameScreenActivity.this, "Deck Refilled", Toast.LENGTH_SHORT).show();

                if(gameDetailsClass.deckCards!=null && gameDetailsClass.deckCards.size()>0 && !isCardPicked){
                    UnoCardClass topCard = gameDetailsClass.deckCards.get(0);
                    docRef.update("deckCards",FieldValue.arrayRemove(topCard)).addOnSuccessListener(Player1GameScreenActivity.this, new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            docRef.update("player1Cards",FieldValue.arrayUnion(topCard)).addOnSuccessListener(Player1GameScreenActivity.this, new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    isCardPicked = true;
                                    findViewById(R.id.button_skipTurnPlayer1).setEnabled(true);
                                    Toast.makeText(Player1GameScreenActivity.this, "New card picked", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(Player1GameScreenActivity.this, new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(Player1GameScreenActivity.this, "Card could not be picked", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }).addOnFailureListener(Player1GameScreenActivity.this, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(Player1GameScreenActivity.this, "Card could not be picked", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            }
        }).addOnFailureListener(Player1GameScreenActivity.this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Player1GameScreenActivity.this, "Deck could not be refilled", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public boolean isCardValid(GameDetailsClass gameDetailsClass,int position){

        UnoCardClass playingCard = gameDetailsClass.player1Cards.get(position);
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

        if(topCard.color.equals("black")){
            if(!playingCard.color.equals("black") && !playingCard.color.equals(gameDetailsClass.plusFourCurrentColor)){
                return false;
            }
        }

        return true;
    }

    public void setColorAndTransferTurn(DocumentReference docRef,GameDetailsClass gameDetailsClass){
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(Player1GameScreenActivity.this);
        builderSingle.setTitle("Select One Color");
        builderSingle.setCancelable(false);

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(Player1GameScreenActivity.this, android.R.layout.simple_list_item_1);
        arrayAdapter.add("red");
        arrayAdapter.add("green");
        arrayAdapter.add("blue");
        arrayAdapter.add("yellow");

        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                colorName = arrayAdapter.getItem(which);

                gameDetailsClass.plusFourCurrentColor=colorName;
                gameDetailsClass.turn="player2";

                if(gameDetailsClass.deckCards.size()<4){
                    UnoCardClass topCard=gameDetailsClass.discardCards.get(gameDetailsClass.discardCards.size()-1);
                    gameDetailsClass.discardCards.remove(gameDetailsClass.discardCards.size()-1);
                    Collections.shuffle(gameDetailsClass.discardCards);
                    gameDetailsClass.deckCards.addAll(gameDetailsClass.discardCards);
                    gameDetailsClass.discardCards=new ArrayList<>(Arrays.asList(topCard));
                }

                if(gameDetailsClass.deckCards.size()>=4){
                    for(int i=0;i<4;i++){
                        gameDetailsClass.player2Cards.add(gameDetailsClass.deckCards.get(0));
                        gameDetailsClass.deckCards.remove(0);
                    }
                }

                docRef.set(gameDetailsClass).addOnSuccessListener(Player1GameScreenActivity.this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(Player1GameScreenActivity.this, "You selected "+colorName+" color", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        countDownTimer.cancel();
                    }
                }).addOnFailureListener(Player1GameScreenActivity.this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Player1GameScreenActivity.this, "Could not change color on card", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });
        builderSingle.show();
    }

//    public void InitializeHandler(){
//        gameCountDownRunnable = new Runnable() {
//            @Override
//            public void run() {
//                Toast.makeText(Player1GameScreenActivity.this, "No mover for 30 seconds game is going to finished", Toast.LENGTH_SHORT).show();
//                //have to delete that request from the firestore
//                //deleteGameRequest
//
//                finish();
//            }
//        };
//        gameCountDownHandler = new Handler();
//        //gameCountDownHandler.postDelayed(gameCountDownRunnable,30000);
//    }
//
//    public void startgameCountDownHandler(){
//        gameCountDownHandler.postDelayed(gameCountDownRunnable,60000);
//    }
//
//    public void stopgameCountDownHandler(){
//        gameCountDownHandler.removeCallbacks(gameCountDownRunnable);
//    }

//    public void displayPickedColor(String color){
//        AlertDialog.Builder builderSingle = new AlertDialog.Builder(Player1GameScreenActivity.this);
//        builderSingle.setMessage("Color "+color+" was selected on wild card draw 4");
//
//        builderSingle.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
//            }
//        });
//
//        builderSingle.show();
//    }

    public void onBackUpdate(){
        db.collection("ChatRoomList").document(chatRoomName)
                .collection("UnoGame")
                .document(gameDetailsClass.player1Id)
                .update("gameState","PLAYER1_GAVEUP")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(Player1GameScreenActivity.this, "you have given up on this game. Now going back to chatRoom Page", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Player1GameScreenActivity.this, "Some error occured. Please try again", Toast.LENGTH_SHORT).show();
            }
        });

    }

    //OnBackPressed function when the Payer 1 is selecting.
    @Override
    public void onBackPressed() {

        AlertDialog.Builder builder1 = new AlertDialog.Builder(Player1GameScreenActivity.this);
        builder1.setMessage("Are you sure you want to give up this game? The other player will win this game");
        builder1.setPositiveButton(
                "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        onBackUpdate();
                    }
                });

        builder1.setNegativeButton(
                "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert11 = builder1.create();
        alert11.show();
    }

    @Override
    protected void onStop() {
        super.onStop();
        countDownTimer.cancel();
    }
}