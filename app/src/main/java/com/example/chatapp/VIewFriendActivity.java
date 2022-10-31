package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class VIewFriendActivity extends AppCompatActivity {

    DatabaseReference mUserRef, requestRef, friendRef;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    String prfileImageUrl, username, city, country;

    CircleImageView profileImage;
    TextView Username, address;
    Button btnPerform, btnDecline;
    String CurrentState = "nothing_happen";
    String profession;
    String userID;

    String URL = "https://fcm.googleapis.com/fcm/send";
    RequestQueue requestQueue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_v_iew_friend);
        userID = getIntent().getStringExtra("userKey");

        requestQueue = Volley.newRequestQueue(this);
        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userID);
        requestRef = FirebaseDatabase.getInstance().getReference().child("Requests");
        friendRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        btnPerform = findViewById(R.id.btnPerform);
        btnDecline = findViewById(R.id.btnDecline);

        profileImage = findViewById(R.id.profileImage);
        Username = findViewById(R.id.username);
        address = findViewById(R.id.address);

        LoadUser();
        btnPerform.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PerformAction(userID);

            }
        });
        CheckUserExistance(userID);
        btnDecline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Unfriend(userID);
            }
        });


    }

    private void Unfriend(final String userID) {
        if (CurrentState.equals("friend")) {
            friendRef.child(mUser.getUid()).child(userID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        friendRef.child(userID).child(mUser.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(VIewFriendActivity.this, "You are Unfriend", Toast.LENGTH_SHORT).show();
                                    CurrentState = "nothing_happen";
                                    btnPerform.setText("Send Friend Request");
                                    btnDecline.setVisibility(View.GONE);
                                }
                            }
                        });
                    }
                }
            });
        }
        if (CurrentState.equals("he_sent_pending")) {
            HashMap hashMap = new HashMap();
            hashMap.put("status", "decline");
            requestRef.child(userID).child(mUser.getUid()).updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(VIewFriendActivity.this, "You have Decline Friend", Toast.LENGTH_SHORT).show();
                        CurrentState = "he_sent_decline";
                        btnPerform.setVisibility(View.GONE);
                        btnDecline.setVisibility(View.GONE);
                    }
                }
            });
        }
    }

    private void CheckUserExistance(String userID) {
        friendRef.child(mUser.getUid()).child(userID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    CurrentState = "friend";
                    btnPerform.setText("Send SMS");
                    btnDecline.setText("Unfriend");
                    btnDecline.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        friendRef.child(userID).child(mUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    CurrentState = "friend";
                    btnPerform.setText("Send SMS");
                    btnDecline.setText("Unfriend");
                    btnDecline.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        requestRef.child(mUser.getUid()).child(userID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (snapshot.child("status").getValue().toString().equals("pending")) {
                        CurrentState = "I_sent_pending";
                        btnPerform.setText("Cancel Friend Request");
                        btnDecline.setVisibility(View.GONE);
                    }
                    if (snapshot.child("status").getValue().toString().equals("decline")) {
                        CurrentState = "I_sent_decline";
                        btnPerform.setText("Cancel Friend Request");
                        btnDecline.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        requestRef.child(userID).child(mUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (snapshot.child("status").getValue().toString().equals("pending")) {
                        CurrentState = "he_sent_pending";
                        btnPerform.setText("Accept Friend Request");
                        btnDecline.setText("Decline Friend");
                        btnDecline.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        if (CurrentState.equals("nothing_happen")) {
            CurrentState = "nothing_happen";
            btnPerform.setText("Send Friend Request");
            btnDecline.setVisibility(View.GONE);
        }
    }


    private void PerformAction(final String userID) {
        if (CurrentState.equals("nothing_happen")) {
            HashMap hashMap = new HashMap();
            hashMap.put("status", "pending");
            requestRef.child(mUser.getUid()).child(userID).updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        sendNotification("request");
                        Toast.makeText(VIewFriendActivity.this, "You have sent Friend Request", Toast.LENGTH_SHORT).show();
                        btnDecline.setVisibility(View.GONE);
                        CurrentState = "I_sent_pending";
                        btnPerform.setText("Cencel Friend Request");


                    } else {
                        Toast.makeText(VIewFriendActivity.this, "" + task.getException().toString(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        if (CurrentState.equals("I_sent_pending") || CurrentState.equals("I_sent_decline")) {
            requestRef.child(mUser.getUid()).child(userID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(VIewFriendActivity.this, "You have cancelled Friend Reuest", Toast.LENGTH_SHORT).show();
                        CurrentState = "nothing_happen";
                        btnPerform.setText("Send Friend Reuest");
                        btnDecline.setVisibility(View.GONE);
                    } else {
                        Toast.makeText(VIewFriendActivity.this, "" + task.getException().toString(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        if (CurrentState.equals("he_sent_pending")) {
            requestRef.child(userID).child(mUser.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        final HashMap hashMap = new HashMap();
                        hashMap.put("status", "friend");
                        hashMap.put("username", username);
                        hashMap.put("profileImageUrl", prfileImageUrl);
                        hashMap.put("prdfession", profession);
                        friendRef.child(mUser.getUid()).child(userID).updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener() {
                            @Override
                            public void onComplete(@NonNull Task task) {
                                if (task.isSuccessful()) {
                                    friendRef.child(userID).child(mUser.getUid()).updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener() {
                                        @Override
                                        public void onComplete(@NonNull Task task) {
                                            Toast.makeText(VIewFriendActivity.this, "You added friend", Toast.LENGTH_SHORT).show();
                                            CurrentState = "friend";
                                            btnPerform.setText("sSend SMS");
                                            btnDecline.setText("UnFriend");
                                            btnDecline.setVisibility(View.VISIBLE);
                                        }
                                    });
                                }
                            }
                        });
                    }
                }
            });
        }
        if (CurrentState.equals("friend")) {
            Intent intent = new Intent(VIewFriendActivity.this, ChatActivity.class);
            intent.putExtra("OtherUserID", userID);
            startActivity(intent);
        }
    }


    private void LoadUser() {
        mUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    prfileImageUrl = snapshot.child("profileImage").getValue().toString();
                    username = snapshot.child("username").getValue().toString();
                    city = snapshot.child("city").getValue().toString();
                    country = snapshot.child("country").getValue().toString();
                    country = snapshot.child("country").getValue().toString();
                    profession = snapshot.child("profession").getValue().toString();

                    Picasso.get().load(prfileImageUrl).into(profileImage);
                    Username.setText(username);
                    address.setText(city + ", " + country);

                } else {
                    Toast.makeText(VIewFriendActivity.this, "Data not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(VIewFriendActivity.this, "" + error.getMessage().toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendNotification(String sms) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("to", "/topics/" + userID);

            JSONObject jsonObject1 = new JSONObject();
            jsonObject1.put("title", "Request from " + username);
            jsonObject1.put("body", sms);


            JSONObject jsonObject2 = new JSONObject();
            jsonObject2.put("userID", mUser.getUid());
            jsonObject2.put("type", "request");


            jsonObject.put("notification", jsonObject1);
            jsonObject.put("data", jsonObject2);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL, jsonObject, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {

                    Map<String, String> map = new HashMap<>();
                    map.put("content-type", "application/json");
                    map.put("authorization", "key=AAAAeHe-0gI:APA91bFkyyo7fPdJZn3fzZzRw4tQeK0IKt-q3TUqE_byJ3zy9-xcNS97411JIeKyHyvm9PFPfFvepCvkKigiLI_uJHiyzD9sRYvPb49gcIogTQsPGzvVqPudVNMPieIFBQ0qZ7kmhOJT");
                    return map;
                }
            };
            requestQueue.add(request);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}