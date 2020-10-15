package com.wong.letsdiscusspsb;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.wong.letsdiscusspsb.Utils.CommentsVariables;
import com.wong.letsdiscusspsb.Utils.PostsVariables;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.os.Build.VERSION_CODES.M;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final int REQUEST_CODE = 101;
    Toolbar toolbar;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    DatabaseReference mUserRef;
    DatabaseReference postRef;
    DatabaseReference likeRef;
    DatabaseReference commentRef;
    String profileImageUrlData;
    String userNameData;
    CircleImageView profileImageHeader;
    TextView userNameHeader;
    ImageView addImagePost;
    ImageView sendImagePost;
    EditText inputPostDescription;
    Uri imageUri;
    ProgressDialog mLoadingBar;
    StorageReference postImageRef;
    FirebaseRecyclerAdapter<PostsVariables, MyViewHolder> adapter;
    FirebaseRecyclerOptions<PostsVariables> options;
    RecyclerView recyclerView;
    FirebaseRecyclerOptions<CommentsVariables> CommentOptions;
    FirebaseRecyclerAdapter<CommentsVariables, CommentViewHolder> CommentAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Let's Discuss");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);

        addImagePost = findViewById(R.id.addImagePost);
        sendImagePost = findViewById(R.id.sendPostImage);
        inputPostDescription = findViewById(R.id.inputAddPost);
        mLoadingBar = new ProgressDialog(this);
        recyclerView = findViewById(R.id.recyclerViewMainActivity);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        postRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        likeRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        commentRef = FirebaseDatabase.getInstance().getReference().child("Comments");
        postImageRef = FirebaseStorage.getInstance().getReference().child("PostImages");

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.naView);

        View view = navigationView.inflateHeaderView(R.layout.drawer_header);
        profileImageHeader = view.findViewById(R.id.profileImageHeader);
        userNameHeader = view.findViewById(R.id.userNameHeader);
        navigationView.setNavigationItemSelectedListener(this);

        sendImagePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddPost();
            }
        });

        addImagePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_CODE);
            }
        });

        LoadPost();
    }

    private void LoadPost() {
        //initialize options & adapter
        options = new FirebaseRecyclerOptions.Builder<PostsVariables>()
                .setQuery(postRef, PostsVariables.class).build();
        adapter = new FirebaseRecyclerAdapter<PostsVariables, MyViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final MyViewHolder holder, int position, @NonNull PostsVariables model) {
                holder.postDescription.setText(model.getPostDesc());
                final String timeAgo = calculateTimeAgo(model.getDate());
                holder.timeAgo.setText(timeAgo);
                holder.userName.setText(model.getUsername());
                Picasso.get().load(model.getPostImageUrl()).into(holder.postImage);
                Picasso.get().load(model.getUserProfileImage()).into(holder.profileImage);

                //image count likes & comment reference
                final String postKey = getRef(position).getKey();
                holder.countLikes(postKey, mUser.getUid(), likeRef);
                holder.countComments(postKey, mUser.getUid(), commentRef);

                //like image count
                holder.likeImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        likeRef.child(postKey).child(mUser.getUid())
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.exists()) //user already like the comment, remove like
                                        {
                                            likeRef.child(postKey).child(mUser.getUid()).removeValue();
                                            holder.likeImage.setColorFilter(Color.GRAY);
                                        } else {
                                            likeRef.child(postKey).child(mUser.getUid()).setValue("like");
                                            holder.likeImage.setColorFilter(Color.BLUE);
                                        }
                                        notifyDataSetChanged();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Toast.makeText(MainActivity.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();

                                    }
                                });


                    }
                });
                LoadComments(postKey);

                holder.commentSend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String comment = holder.inputComment.getText().toString();
                        if (comment.isEmpty()) {
                            Toast.makeText(MainActivity.this, "Comments cannot be empty",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            AddComment(holder, postKey, commentRef, mUser.getUid(), comment, commentDate);
                        }

                    }
                });
            }

            //load post Comments
            private void LoadComments(String postKey) {
                MyViewHolder.recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));

                CommentOptions = new FirebaseRecyclerOptions.Builder<CommentsVariables>()
                        .setQuery(commentRef.child(postKey), CommentsVariables.class).build();
                CommentAdapter = new FirebaseRecyclerAdapter<CommentsVariables, CommentViewHolder>(CommentOptions) {
                    @Override
                    protected void onBindViewHolder(@NonNull CommentViewHolder holder,
                                                    int position, @NonNull CommentsVariables model) {
                        Log.d("comment_data", "onBindViewHolder: " + model.getComment());
                        Picasso.get().load(model.getProfileImageUrl()).into(holder.profileImage);
                        holder.username.setText(model.getUsername());
                        holder.comment.setText(model.getComment());
                    }

                    @NonNull
                    @Override
                    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                        View view = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.single_view_comment, parent, false);
                        return new CommentViewHolder(view);
                    }
                };
                CommentAdapter.startListening();
                MyViewHolder.recyclerView.setAdapter(CommentAdapter);
            }

            @NonNull
            @Override
            public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                //initialize singleview layout in recycler
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.single_view_post, parent, false);
                return new MyViewHolder(view);
            }
        };
        adapter.startListening();
        recyclerView.setAdapter(adapter);
    }

    Date date = new Date();
    SimpleDateFormat formatter = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
    final String commentDate = formatter.format(date);

    private void AddComment(final MyViewHolder holder, String postKey,
                            DatabaseReference commentRef, String Uid, String comment, String commentDate) {

        HashMap hashMap = new HashMap();
        hashMap.put("username", userNameData);
        hashMap.put("profileImageUrl", profileImageUrlData);
        hashMap.put("comment", comment);
        hashMap.put("date", commentDate);

        commentRef.child(postKey).push().updateChildren(hashMap)
                .addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {

                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Comments Added", Toast.LENGTH_SHORT).show();
                            adapter.notifyDataSetChanged();
                            holder.inputComment.setText(null); //clear input fields
                        } else {
                            Toast.makeText(MainActivity.this, "" + task.getException().toString(),
                                    Toast.LENGTH_SHORT).show();
                        }

                        //todo pushID new one for 2nd and more comments

                    }
                });
    }

    private String calculateTimeAgo(String date) {

        //timeAgo https://stackoverflow.com/questions/35858608/how-to-convert-time-to-time-ago-in-android

        SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");

        try {
            long time = sdf.parse(date).getTime();
            long now = System.currentTimeMillis();
            CharSequence ago =
                    DateUtils.getRelativeTimeSpanString(time, now, DateUtils.MINUTE_IN_MILLIS);
            return ago + "";
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            addImagePost.setImageURI(imageUri);
        }
    }

    private void AddPost() {
        final String postDesc = inputPostDescription.getText().toString();

        if (postDesc.isEmpty() || postDesc.length() < 2) {
            inputPostDescription.setError("Comments cannot be empty");
        } else if (imageUri == null) {
            Toast.makeText(this, "Select an Image", Toast.LENGTH_SHORT).show();
        } else {
            mLoadingBar.setTitle("Adding Post");
            mLoadingBar.setCanceledOnTouchOutside(false);
            mLoadingBar.show();

            Date date = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
            final String strDate = formatter.format(date);

            postImageRef.child(mUser.getUid() + strDate).putFile(imageUri)
                    .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()) {
                                postImageRef.child(mUser.getUid() + strDate).getDownloadUrl()
                                        .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {

                                                HashMap hashMap = new HashMap();
                                                hashMap.put("date", strDate);
                                                hashMap.put("postImageUrl", uri.toString());
                                                hashMap.put("postDesc", postDesc);
                                                hashMap.put("userProfileImage", profileImageUrlData);
                                                hashMap.put("username", userNameData);

                                                postRef.child(mUser.getUid() + strDate).updateChildren(hashMap)
                                                        .addOnCompleteListener(new OnCompleteListener() {
                                                            @Override
                                                            public void onComplete(@NonNull Task task) {

                                                                if (task.isSuccessful()) {
                                                                    mLoadingBar.dismiss();
                                                                    Toast.makeText(MainActivity.this, "Posted Successfully", Toast.LENGTH_SHORT).show();
                                                                    addImagePost.setImageResource(R.drawable.ic_add_post);
                                                                    inputPostDescription.setText("");
                                                                } else {
                                                                    mLoadingBar.dismiss();
                                                                    Toast.makeText(MainActivity.this,
                                                                            "" + task.getException().toString(), Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        });
                                            }
                                        });
                            } else {
                                mLoadingBar.dismiss();
                                Toast.makeText(MainActivity.this, "" + task.getException().toString(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    //check if user exits
    @Override
    protected void onStart() {
        super.onStart();
        if (mUser == null) //user logged out
        {
            UserToLoginActivity();
        } else //user exists
        {
            //get data from firebase
            mUserRef.child(mUser.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    //if user has setup users
                    if (snapshot.exists()) {
                        profileImageUrlData = snapshot.child("profileImage").getValue().toString();
                        userNameData = snapshot.child("username").getValue().toString();
                        //Toast.makeText(MainActivity.this, profileImageUrlData, Toast.LENGTH_SHORT).show();
                        Picasso.get().load(profileImageUrlData).into(profileImageHeader);
                        userNameHeader.setText(userNameData);
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(MainActivity.this, "Data unavailable", Toast.LENGTH_SHORT).show();

                }
            });
        }
    }

    private void UserToLoginActivity() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.home:
                Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
                break;

            case R.id.profile:
                Toast.makeText(this, "Profile", Toast.LENGTH_SHORT).show();
                break;

            case R.id.logOut:
                mAuth.signOut();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
                break;

            case R.id.message:
                Toast.makeText(this, "This feature will be available in the next update", Toast.LENGTH_SHORT).show();
                break;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            drawerLayout.openDrawer(GravityCompat.START);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}