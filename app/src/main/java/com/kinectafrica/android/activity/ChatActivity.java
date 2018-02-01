package com.kinectafrica.android.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.common.collect.Lists;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kinectafrica.android.R;
import com.kinectafrica.android.adapter.recycler.ChatRecyclerAdapter;
import com.kinectafrica.android.model.Message;
import com.kinectafrica.android.model.MessageThread;
import com.kinectafrica.android.model.User;
import com.kinectafrica.android.utility.Utils;

import java.util.List;
import java.util.Map;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ChatActivity extends AppCompatActivity {

    DatabaseReference database;

    FirebaseAuth firebaseAuth;

    ChatRecyclerAdapter recyclerAdapter;
    RecyclerView recyclerView;

    ImageButton btnSend;
    EditText editMessage;

    Map<String, Object> messageThreadValues;

    MessageThread messageThread;

    User user;

    boolean isEnabled = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        firebaseAuth = FirebaseAuth.getInstance();

        recyclerView = findViewById(R.id.recycler_messages);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);

        if (firebaseAuth.getCurrentUser() == null)
            finish();
        else {
            database = FirebaseDatabase.getInstance().getReference();
            database.child("messageThreads").child(getIntent().getStringExtra("threadId")).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(final DataSnapshot dataSnapshot) {
                    database.child("users").child(firebaseAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot1) {
                            if (dataSnapshot.getValue(MessageThread.class) != null) {
                                isEnabled = true;
                                user = dataSnapshot1.getValue(User.class);

                                messageThread = dataSnapshot.getValue(MessageThread.class);
                                messageThreadValues = messageThread != null ? messageThread.toMap() : null;

                                if (messageThread != null && messageThread.getUserId1().equals(dataSnapshot1.getKey())) {
                                    FirebaseDatabase.getInstance().getReference().child("users").child(messageThread.getUserId2())
                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    User user = dataSnapshot.getValue(User.class);
                                                    if (getSupportActionBar() != null)
                                                        getSupportActionBar().setTitle(user != null ? user.getDisplayName() : null);
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {
                                                    Toast.makeText(ChatActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                } else {
                                    FirebaseDatabase.getInstance().getReference().child("users").child(messageThread.getUserId1())
                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    User user = dataSnapshot.getValue(User.class);
                                                    if (getSupportActionBar() != null)
                                                        getSupportActionBar().setTitle(user != null ? user.getDisplayName() : null);
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {
                                                    Toast.makeText(ChatActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                }

                                List<Message> reverseMessages = Lists.reverse(messageThread.getMessages());

                                recyclerAdapter = new ChatRecyclerAdapter(firebaseAuth.getCurrentUser().getUid(), reverseMessages);
                                recyclerView.setAdapter(recyclerAdapter);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Snackbar.make(recyclerView, databaseError.getMessage(), Snackbar.LENGTH_LONG).show();
                        }
                    });
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Snackbar.make(recyclerView, databaseError.getMessage(), Snackbar.LENGTH_LONG).show();
                }
            });

            editMessage = findViewById(R.id.edit_message);
            btnSend = findViewById(R.id.btn_send_message);
            btnSend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!editMessage.getText().toString().trim().isEmpty() && isEnabled) {
                        final String msg = editMessage.getText().toString();
                        editMessage.setText("");

                        Message message = new Message(firebaseAuth.getCurrentUser().getUid(), msg);

                        List<Message> messages = messageThread.getMessages();
                        messages.add(message);
                        messageThreadValues.put("messages", messages);
                        database.child("messageThreads").child(getIntent().getStringExtra("threadId")).updateChildren(messageThreadValues);

                        if (firebaseAuth.getCurrentUser().getUid().equals(messageThread.getUserId1())) {
                            database.child("users").child(messageThread.getUserId2())
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            User receiver = dataSnapshot.getValue(User.class);
                                            if (receiver != null) {
                                                Utils.INSTANCE.sendFCM(ChatActivity.this, receiver.getFcmId(), receiver.getDisplayName() + ": " + msg,
                                                        "chat", getIntent().getStringExtra("threadId"));
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                        } else {
                            database.child("users").child(messageThread.getUserId1())
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            User receiver = dataSnapshot.getValue(User.class);
                                            if (receiver != null) {
                                                Utils.INSTANCE.sendFCM(ChatActivity.this, receiver.getFcmId(), receiver.getDisplayName() + ": " + msg,
                                                        "chat", getIntent().getStringExtra("threadId"));
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                        }
                    }
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (firebaseAuth.getCurrentUser() == null)
            finish();
        else {

            if (id == android.R.id.home) {
                Intent home_intent;
                home_intent = new Intent(this, MainActivity.class);
                home_intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(home_intent);
                finish();
                return true;
            }

            if (id == R.id.action_view_profile) {
                if (isEnabled) {
                    if (!messageThread.getUserId1().equals(firebaseAuth.getCurrentUser().getUid())) {
                        Intent intent = new Intent(ChatActivity.this, ProfileActivity.class);
                        intent.putExtra("userId", messageThread.getUserId1());
                        startActivity(intent);
                        finish();
                    } else {
                        Intent intent = new Intent(ChatActivity.this, ProfileActivity.class);
                        intent.putExtra("userId", messageThread.getUserId2());
                        startActivity(intent);
                        finish();
                    }
                    return true;
                } else {
                    return false;
                }
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
