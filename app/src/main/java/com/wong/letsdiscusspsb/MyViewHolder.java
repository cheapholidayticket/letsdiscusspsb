package com.wong.letsdiscusspsb;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyViewHolder extends RecyclerView.ViewHolder {

    //intialize variables in single view post
    CircleImageView profileImage;
    ImageView postImage;
    ImageView likeImage;
    ImageView commentImage;
    TextView userName;
    TextView timeAgo;
    TextView postDescription;
    TextView likeCounter;
    TextView commentCounter;

    public MyViewHolder(@NonNull View itemView) {
        super(itemView);

        profileImage = itemView.findViewById(R.id.profileImagePost);
        postImage = itemView.findViewById(R.id.postImage);
        userName = itemView.findViewById(R.id.profileUserNamePost);
        timeAgo = itemView.findViewById(R.id.timeAgo);
        postDescription = itemView.findViewById(R.id.postDescription);
        likeImage = itemView.findViewById(R.id.likeImage);
        commentImage = itemView.findViewById(R.id.commentImage);
        likeCounter = itemView.findViewById(R.id.likeCounter);
        likeImage = itemView.findViewById(R.id.likeImage);


    }
}
