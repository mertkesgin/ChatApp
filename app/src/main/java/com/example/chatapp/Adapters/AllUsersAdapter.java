package com.example.chatapp.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.Models.User;
import com.example.chatapp.Activities.ProfileActivity;
import com.example.chatapp.R;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AllUsersAdapter extends RecyclerView.Adapter<AllUsersAdapter.ViewHolder> {

    private Context mContext;
    private List<User> userList;

    public AllUsersAdapter(Context mContext, List<User> userList) {
        this.mContext = mContext;
        this.userList = userList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_users_card_desing,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final User user = userList.get(position);
        if (!user.getImage().equals("default")){
            Picasso.get().load(user.getImage()).into(holder.img_pp);
        }else {
            holder.img_pp.setImageResource(R.drawable.profile_photo);
        }
        holder.userStatus.setText(user.getStatus());
        holder.userDisplayName.setText(user.getDisplayName());
        holder.userCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, ProfileActivity.class);
                intent.putExtra("userID",user.getUserID());
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        CardView userCard;
        TextView userStatus,userDisplayName;
        CircleImageView img_pp;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userDisplayName = itemView.findViewById(R.id.user_display_name);
            userStatus = itemView.findViewById(R.id.user_status);
            img_pp = itemView.findViewById(R.id.img_pp);
            userCard = itemView.findViewById(R.id.user_card);
        }
    }
}

