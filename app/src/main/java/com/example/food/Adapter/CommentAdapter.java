package com.example.food.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.food.Model.Comment;
import com.example.food.R;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {
    private Context context;
    private List<Comment> comments;

    public CommentAdapter(Context context, List<Comment> comments) {
        this.context = context;
        this.comments = comments;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Comment comment = comments.get(position);
        
        holder.userName.setText(comment.getUserName());
        holder.commentText.setText(comment.getCommentText());
        holder.userRating.setRating(comment.getRating());

        // Format timestamp
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        holder.commentTime.setText(sdf.format(comment.getTimestamp()));

        // Load user avatar
        if (comment.getUserPhoto() != null && !comment.getUserPhoto().isEmpty()) {
            Glide.with(context)
                .load(comment.getUserPhoto())
                .placeholder(R.drawable.intro_pic)
                .error(R.drawable.intro_pic)
                .into(holder.userAvatar);
        } else {
            holder.userAvatar.setImageResource(R.drawable.intro_pic);
        }
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public void updateComments(List<Comment> newComments) {
        this.comments = newComments;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView userAvatar;
        TextView userName;
        RatingBar userRating;
        TextView commentText;
        TextView commentTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userAvatar = itemView.findViewById(R.id.userAvatar);
            userName = itemView.findViewById(R.id.userName);
            userRating = itemView.findViewById(R.id.userRating);
            commentText = itemView.findViewById(R.id.commentText);
            commentTime = itemView.findViewById(R.id.commentTime);
        }
    }
} 