package com.wong.letsdiscusspsb;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentViewHolder extends RecyclerView.ViewHolder {

    CircleImageView profileImage;
    TextView username;
    TextView comment;

    public CommentViewHolder(@NonNull View itemView) {
        super(itemView);

        profileImage = itemView.findViewById(R.id.profileImageComment);
        username = itemView.findViewById(R.id.userNameComment);
        comment = itemView.findViewById(R.id.commentText);


    }
}
