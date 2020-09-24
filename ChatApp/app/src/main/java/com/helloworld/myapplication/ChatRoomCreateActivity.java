package com.helloworld.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ChatRoomCreateActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private EditText chatRoomName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room_create);

        Toolbar t = findViewById(R.id.toolbar_for_sidebar);
        t.setTitleTextColor(Color.WHITE);
        setSupportActionBar(t);
        setTitle("Create Chatroom");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        db = FirebaseFirestore.getInstance();

        findViewById(R.id.newChatRoomCreateButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chatRoomName = findViewById(R.id.newChatRoomName);
                if(checkValidations(chatRoomName)){
                    db.collection("ChatRoomList").document(chatRoomName.getText().toString())
                            .get()
                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    if(!documentSnapshot.exists()){
                                        //Sending the new chatroomname to the chatRoom Activity so that we can add it to the Firebase there
//                                        Intent intent = new Intent();
//                                        intent.putExtra("chatRoomName",chatRoomName.getText().toString());
//                                        setResult(200, intent);
//                                        finish();
                                        createNewChatRoom(chatRoomName.getText().toString());
                                    }else{
                                        AlertDialog.Builder builder1 = new AlertDialog.Builder(ChatRoomCreateActivity.this);
                                        builder1.setMessage("A ChatRoom with same name already exists, Please give a different ChatRoom name!");

                                        builder1.setPositiveButton(
                                                "OK",
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        dialog.cancel();
                                                    }
                                                });

                                        AlertDialog alert11 = builder1.create();
                                        alert11.show();
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });
                }else{
                    Toast.makeText(ChatRoomCreateActivity.this, "Chat Room name cannot be empty!", Toast.LENGTH_SHORT).show();
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

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    public void createNewChatRoom(String chatRoomName){
        Map<String, Object> docData = new HashMap<>();
        db.collection("ChatRoomList").document(chatRoomName).set(docData)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d("demo", "Yes it is a success");
                        Toast.makeText(ChatRoomCreateActivity.this, "ChatRoom successfully added", Toast.LENGTH_SHORT).show();
//                        globalChatRoomList.add(chatRoomName);
//                            mainAdapter.notifyDataSetChanged();
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ChatRoomCreateActivity.this, "Some error occured in creating chatroom. Please try again", Toast.LENGTH_SHORT).show();
            }
        });
    }
}