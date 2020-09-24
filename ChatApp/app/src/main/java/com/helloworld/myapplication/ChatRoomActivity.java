package com.helloworld.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class ChatRoomActivity extends AppCompatActivity implements ChatMessageAdapter.InteractWithRecyclerView{

    private static final String TAG = "okay";
    FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String chatRoomName;
    private RecyclerView mainRecyclerView;
    private RecyclerView.Adapter mainAdapter;
    private RecyclerView.LayoutManager mainLayoutManager;
    private RecyclerView usersRecyclerView;
    private RecyclerView.Adapter usersAdapter;
    private RecyclerView.LayoutManager usersLayoutManager;
    ArrayList<ChatMessageDetails> chatMessageDetailsArrayList = new ArrayList<>();
    ArrayList<UserProfile> currentUserList = new ArrayList<>();
    DatabaseReference mDatabase;
    FirebaseStorage storage;
    StorageReference storageReference;
    UserProfile user;
    private EditText enterMessageText;
    private ProgressDialog progressDialog;

    //For deleting the users from the current user list of the chatroom
    @Override
    public void onBackPressed() {
        mAuth=FirebaseAuth.getInstance();
        db.collection("ChatRoomList").document(chatRoomName).collection("CurrentViewers")
                .document(mAuth.getUid())
                .delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ChatRoomActivity.this, "Some error occured. Please press the back button again!", Toast.LENGTH_SHORT).show();
            }
        });
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume: in chatroomActivity");
        super.onResume();
        addingListenerRequestedRide();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        Log.d(TAG, "onCreate: chatroomActivity is called");

        chatRoomName = getIntent().getExtras().getString("chatRoomName");
        user = (UserProfile) getIntent().getSerializableExtra("user");

        Toolbar t = findViewById(R.id.toolbar_for_chatroom);
        t.setTitleTextColor(Color.WHITE);
        setSupportActionBar(t);
        setTitle(chatRoomName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        db = FirebaseFirestore.getInstance();

        //For the chat Messages
        mainRecyclerView = (RecyclerView) findViewById(R.id.recyclerViewChatRoomMessages);
        mainLayoutManager = new LinearLayoutManager(ChatRoomActivity.this);
        mainRecyclerView.setLayoutManager(mainLayoutManager);

        mainAdapter = new ChatMessageAdapter(chatMessageDetailsArrayList, ChatRoomActivity.this);
        mainRecyclerView.setAdapter(mainAdapter);

        //Adding snapshot listener to the firestore for the chatmessages
        db.collection("ChatRoomList").document(chatRoomName).collection("Messages").addSnapshotListener(new EventListener<QuerySnapshot>() {
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
                            Log.d("TAG", "New Msg: " + dc.getDocument().toObject(ChatMessageDetails.class));
                            ChatMessageDetails added = dc.getDocument().toObject(ChatMessageDetails.class);
                            chatMessageDetailsArrayList.add(dc.getDocument().toObject(ChatMessageDetails.class));

                            break;
                        case MODIFIED:
                            //There is a modification when the user sets the favorites
                            break;
                        case REMOVED:
                            Log.d("TAG", "Removed Msg: " + dc.getDocument().toObject(ChatMessageDetails.class));
                            //Functionality to be completed yet
                            int i = 0;
                            ChatMessageDetails deletedChatMessage = dc.getDocument().toObject(ChatMessageDetails.class);
                            for (ChatMessageDetails chatMessageDetails : chatMessageDetailsArrayList) {
                                if (chatMessageDetails.Uid.equals(deletedChatMessage.Uid) &&
                                        chatMessageDetails.date.equals(deletedChatMessage.date) &&
                                        chatMessageDetails.Message.equals(deletedChatMessage.Message)) {
                                    chatMessageDetailsArrayList.remove(i);
                                    break;
                                } else {
                                    i++;
                                }
                            }
                            break;
                    }
                }

                //For Sorting
                Collections.sort(chatMessageDetailsArrayList, new Comparator<ChatMessageDetails>() {
                    @Override
                    public int compare(ChatMessageDetails o1, ChatMessageDetails o2) {
                        return o1.date.compareTo(o2.date);
                    }
                });

                mainAdapter.notifyDataSetChanged();
                mainRecyclerView.scrollToPosition(chatMessageDetailsArrayList.size() - 1);
            }
        });


        //For the current Users
        usersRecyclerView = (RecyclerView) findViewById(R.id.recyclerViewFreshCurrentUsers);
        usersLayoutManager = new LinearLayoutManager(ChatRoomActivity.this,
                LinearLayoutManager.HORIZONTAL, false);
        usersRecyclerView.setLayoutManager(usersLayoutManager);

        usersAdapter = new CurrentUserAdapter(currentUserList, ChatRoomActivity.this);
        usersRecyclerView.setAdapter(usersAdapter);

        //Adding snapshot to the current users list in the chatroom
        db.collection("ChatRoomList").document(chatRoomName)
                .collection("CurrentViewers").addSnapshotListener(new EventListener<QuerySnapshot>() {
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
                            Log.d("TAG", "New Msg: " + dc.getDocument().getString("firstName"));
                            UserProfile user = new UserProfile(dc.getDocument().getString("firstName"),
                                    dc.getDocument().getString("lastName"),
                                    dc.getDocument().getString("gender"),
                                    dc.getDocument().getString("email"),
                                    dc.getDocument().getString("city"),
                                    dc.getDocument().getString("profileImage"),
                                    dc.getDocument().getString("uid"));
                            currentUserList.add(user);
                            break;
                        case MODIFIED:
                            break;
                        case REMOVED:
                            int i = 0;
                            UserProfile deletedUserProfile = dc.getDocument().toObject(UserProfile.class);
                            for (UserProfile userProfile : currentUserList) {
                                if (userProfile.uid.equals(deletedUserProfile.uid)) {
                                    currentUserList.remove(i);
                                    break;
                                } else {
                                    i++;
                                }
                            }
                            break;
                    }
                }
                usersAdapter.notifyDataSetChanged();
            }
        });


        findViewById(R.id.SendMessageButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enterMessageText = findViewById(R.id.enterMessageText);
                Log.d(TAG, "onClick: send button in chatroom activity clicked with text in textview =" + enterMessageText.getText());
                if (checkValidations(enterMessageText)) {
                    Log.d(TAG, "onClick: in chatroom activity send text passed validatioins");
                    //Profile is taken from the realtime database in the below code.
                    mAuth = FirebaseAuth.getInstance();
                    if (mAuth.getCurrentUser() != null) {
                        showProgressBarDialog();

                        //Setting up all the details of the user for the message
//                        user = (UserProfile) getIntent().getSerializableExtra("user");

                        //For getting the date
                        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                        LocalDateTime now = LocalDateTime.now();

                        //Loading everything into the ChatMessageDetails
                        final ChatMessageDetails chatMessageDetails = new ChatMessageDetails();
                        chatMessageDetails.firstname = user.firstName;
                        chatMessageDetails.Message = enterMessageText.getText().toString();
                        chatMessageDetails.Uid = mAuth.getUid();
                        chatMessageDetails.date = dtf.format(now);
                        chatMessageDetails.likedUsers = new HashMap<String,Boolean>();
                        chatMessageDetails.imageUrl = user.profileImage;

                        //Document ID is now the user ID plus the message date. so that this can be used when updating the liked user field.
                        String documentID = chatMessageDetails.Uid + "" + chatMessageDetails.date;
                        //Storing all the info along with the message in the firestore
                        db.collection("ChatRoomList").document(chatRoomName).collection("Messages")
                                .document(documentID)
                                .set(chatMessageDetails)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
//                                                Toast.makeText(ChatRoomActivity.this, "Message sent!", Toast.LENGTH_SHORT).show();
                                        Log.d("demo", chatMessageDetails.toString());
                                        enterMessageText.setText("");
//                                                mainAdapter.notifyDataSetChanged();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(ChatRoomActivity.this, "Some error occured. Please try again", Toast.LENGTH_SHORT).show();
                            }
                        });
                        hideProgressBarDialog();

                    } else {
                        Toast.makeText(ChatRoomActivity.this, "User Not Logged In", Toast.LENGTH_SHORT).show();
                        Log.d("demo", "User not logged in");
                    }
                } else {
                    //The message string is empty!
                    Toast.makeText(ChatRoomActivity.this, "Message cannot be empty!", Toast.LENGTH_SHORT).show();
                }
            }
        });


        //Adding Snapshot listener to the Requested Rides User Document for Drivers
        //Hope it works!!
        //addingListenerRequestedRide("onCreate");


    }

    public void addingListenerRequestedRide(){

        //Adding snapshot to the requested rides in the chatroom
        db.collection("ChatRoomList")
                .document(chatRoomName)
                .collection("Requested Rides")
                .addSnapshotListener(ChatRoomActivity.this, new EventListener<QuerySnapshot>() {
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
                                    //Toast.makeText(ChatRoomActivity.this, "It comes to addded", Toast.LENGTH_SHORT).show();
                                    RequestedRides added = dc.getDocument().toObject(RequestedRides.class);
                                    mAuth = FirebaseAuth.getInstance();
                                    if(added.rideStatus.equals("REQUESTED") && (added.rejectedRides!=null && !added.rejectedRides.contains(mAuth.getCurrentUser().getUid()))){
                                        Intent intent = new Intent(ChatRoomActivity.this, DriverMapsActivity.class);
                                        intent.putExtra("chatRoomName",chatRoomName);
                                        intent.putExtra("requestedRides", added);
                                        intent.putExtra("userProfile",user);
                                        startActivity(intent);
                                    }
                                    break;
                                case MODIFIED:
                                    //Toast.makeText(ChatRoomActivity.this, "It comes to modified", Toast.LENGTH_SHORT).show();
                                    break;
                                case REMOVED:
                                    //Toast.makeText(ChatRoomActivity.this, "It comes to deleted", Toast.LENGTH_SHORT).show();
                                    break;
                            }
                        }
                    }
                });
    }


    //For checking the empty strings
    public boolean checkValidations(EditText editText){
        if(editText.getText().toString().trim().equals("")){
            editText.setError("Cannot be empty");
            return false;
        }else{
            return true;
        }
    }

    //for showing the progress dialog
    public void showProgressBarDialog()
    {
        progressDialog = new ProgressDialog(ChatRoomActivity.this);
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
    public void getDetails(ChatMessageDetails chatMessageDetails) {
        String documentID = chatMessageDetails.Uid+""+chatMessageDetails.date;
        db.collection("ChatRoomList").document(chatRoomName).collection("Messages")
                .document(documentID)
                .update("likedUsers",chatMessageDetails.likedUsers)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
//                        Toast.makeText(ChatRoomActivity.this, "Message liked!", Toast.LENGTH_SHORT).show();
                        mainAdapter.notifyDataSetChanged();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ChatRoomActivity.this, "Some error occured. Please try again", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void getItemPosition(int position, final ChatMessageDetails chatMessageDetails) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(ChatRoomActivity.this);
        builder1.setMessage("Are you sure you want to delete this message?");
        builder1.setPositiveButton(
                "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String documentID = chatMessageDetails.Uid+""+chatMessageDetails.date;
                        db.collection("ChatRoomList").document(chatRoomName).collection("Messages")
                                .document(documentID)
                                .delete()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Toast.makeText(ChatRoomActivity.this, "Message Deleted Successfully!", Toast.LENGTH_SHORT).show();
//                                        mainAdapter.notifyDataSetChanged();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(ChatRoomActivity.this, "Some error occured. Please try again", Toast.LENGTH_SHORT).show();
                            }
                        });
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chatroom_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.request_ride:
                //add your code here to goto next activity
                //Toast.makeText(this, "why you want to request ride, walk!!!", Toast.LENGTH_LONG).show();
                Intent i =  new Intent(this,AskForARide.class);
                i.putExtra("chatRoomName",chatRoomName);
                i.putExtra("user",user);
                startActivity(i);

            default:
                Log.d(TAG, "onOptionsItemSelected: default case called in chatroom activity");
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}