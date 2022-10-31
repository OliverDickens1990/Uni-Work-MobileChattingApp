package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DownloadManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.chatapp.Utills.Friends;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

public class FriendActivity extends AppCompatActivity {
    FirebaseRecyclerOptions<Friends> options;
    FirebaseRecyclerAdapter<Friends, FriendMyViewHlder> adapter;

    Toolbar toolbar;
    RecyclerView recyclerView;
    FirebaseAuth mAuth;
    DatabaseReference mRef;
    FirebaseUser mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend);

        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Friends");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mRef = FirebaseDatabase.getInstance().getReference().child("Friends");


        LoadFriends("");


    }

    private void LoadFriends(String s) {
        Query query = mRef.child(mUser.getUid()).orderByChild("username").startAt(s).endAt(s + "\uf8ff");
        options = new FirebaseRecyclerOptions.Builder<Friends>().setQuery(query, Friends.class).build();
        adapter = new FirebaseRecyclerAdapter<Friends, FriendMyViewHlder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FriendMyViewHlder holder, final int position, @NonNull Friends model) {
                Picasso.get().load(model.getProfileImageUrl()).into(holder.profileImageUrl);
                holder.username.setText(model.getUsername());
                holder.profession.setText(model.getProfession());


                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent=new Intent(FriendActivity.this,ChatActivity.class);
                        intent.putExtra("OtherUserID",getRef(position).getKey().toString());
                        startActivity(intent);
                    }
                });

            }

            @NonNull
            @Override
            public FriendMyViewHlder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_view_friend, parent, false);

                return new FriendMyViewHlder(view);
            }
        };
        adapter.startListening();
        recyclerView.setAdapter(adapter);

    }
}