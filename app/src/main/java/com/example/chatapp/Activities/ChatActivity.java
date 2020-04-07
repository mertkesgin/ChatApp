package com.example.chatapp.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.chatapp.Adapters.MessageAdapter;
import com.example.chatapp.Models.Messages;
import com.example.chatapp.R;
import com.example.chatapp.Utils.TimestampConverter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivity";

    private Toolbar toolbar;

    private String chatUserID;

    private TextView diplayName,lastSeen;
    private CircleImageView imgChatPP;
    private ImageView imgAdd,imgSend;
    private EditText editTextMessage;

    private RecyclerView rvMessages;
    private List<Messages> messagesList;
    private MessageAdapter adapter;

    private DatabaseReference mRef;
    private FirebaseUser currentUser;
    private StorageReference mStorageRef;

    private static final int GALLERY_PICK = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatUserID = getIntent().getStringExtra("userID");

        setupFirebase();
        setupToolbar();
        messagingProcess();
        retrievMessages();


    }

    private void retrievMessages() {
        rvMessages = findViewById(R.id.rvMessages);
        rvMessages.setHasFixedSize(true);
        rvMessages.setLayoutManager(new LinearLayoutManager(this));

        messagesList = new ArrayList<>();
        adapter = new MessageAdapter(messagesList,ChatActivity.this);
        rvMessages.setAdapter(adapter);

        mRef.child(getString(R.string.dbname_messages)).child(currentUser.getUid()).child(chatUserID).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Messages message = dataSnapshot.getValue(Messages.class);
                messagesList.add(message);
                adapter.notifyDataSetChanged();
                rvMessages.scrollToPosition(messagesList.size()-1);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void messagingProcess() {
        imgAdd = findViewById(R.id.img_add);
        imgSend = findViewById(R.id.img_send);
        editTextMessage = findViewById(R.id.editTextMessage);

        imgSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
    }

    private void sendMessage() {
        String message = editTextMessage.getText().toString().trim();
        if (!message.isEmpty()){

            DatabaseReference pushIDRef = mRef.child("messages").child(currentUser.getUid()).child(chatUserID).push();
            String pushID = pushIDRef.getKey();

            Map messageMap = new HashMap();
            messageMap.put("message",message);
            messageMap.put("from",currentUser.getUid());
            messageMap.put("seen","false");
            messageMap.put("type","text");
            messageMap.put("time",ServerValue.TIMESTAMP);

            Map messageUserMap = new HashMap();
            messageUserMap.put("messages/" + currentUser.getUid() + "/" + chatUserID + "/" + pushID,messageMap);
            messageUserMap.put("messages/" + chatUserID + "/" + currentUser.getUid() + "/" + pushID,messageMap);

            mRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                    editTextMessage.setText("");
                }
            });
        }
    }

    private void setupFirebase() {
        mRef = FirebaseDatabase.getInstance().getReference();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        mStorageRef = FirebaseStorage.getInstance().getReference();

        addChatToFirebase();
    }

    private void addChatToFirebase() {
        mRef.child(getString(R.string.dbname_chat)).child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild(chatUserID)){

                    Map chatStartMap = new HashMap();
                    chatStartMap.put("seen",false);
                    chatStartMap.put("timestamp", ServerValue.TIMESTAMP);

                    Map chatUserMap = new HashMap();
                    chatUserMap.put("chat/" + currentUser.getUid() + "/" + chatUserID,chatStartMap);
                    chatUserMap.put("chat/" + chatUserID + "/" + currentUser.getUid(),chatStartMap);

                    mRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setupToolbar() {
        Log.d(TAG,"setupToolbar");

        String toolbarTitle = getIntent().getStringExtra("toolbarTitle");

        imgChatPP = findViewById(R.id.img_chat_pp);
        diplayName = findViewById(R.id.chatName);
        lastSeen = findViewById(R.id.lastSeen);

        toolbar = findViewById(R.id.chatAppToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRef.child(getString(R.string.dbname_users)).child(chatUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String online = dataSnapshot.child("online").getValue().toString();
                String imgURL = dataSnapshot.child("image").getValue().toString();
                String name = dataSnapshot.child("displayName").getValue().toString();

                diplayName.setText(name);

                if (online.equals("true")){
                    lastSeen.setText("Online");
                }else {
                    long lastTime = Long.parseLong(online);
                    String lastSeenTime = TimestampConverter.getTimeAgo(lastTime,getApplicationContext());
                    lastSeen.setText(lastSeenTime);
                }

                Picasso.get().load(imgURL).into(imgChatPP);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (currentUser == null){

        }else {
            mRef.child(getString(R.string.dbname_users))
                    .child(currentUser.getUid()).child("online").setValue(true);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mRef.child(getString(R.string.dbname_users))
                .child(currentUser.getUid())
                .child("online").setValue(ServerValue.TIMESTAMP);
    }
}
