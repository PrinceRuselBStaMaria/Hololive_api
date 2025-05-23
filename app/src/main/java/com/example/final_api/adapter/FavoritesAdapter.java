package com.example.final_api.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.final_api.FavoritesActivity;
import com.example.final_api.R;
import com.example.final_api.VTuberDetailActivity;
import com.example.final_api.model.VTuber;

import java.util.ArrayList;
import java.util.List;

public class FavoritesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_VTUBER = 1;
    private static final int TYPE_TEXT = 2;

    private final Context context;
    private final List<FavoritesActivity.FavoriteItem> favorites;

    public FavoritesAdapter(Context context) {
        this.context = context;
        this.favorites = new ArrayList<>();
    }

    public void updateData(List<FavoritesActivity.FavoriteItem> favorites) {
        this.favorites.clear();
        this.favorites.addAll(favorites);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (favorites.get(position) instanceof FavoritesActivity.VTuberFavorite) {
            return TYPE_VTUBER;
        } else {
            return TYPE_TEXT;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_VTUBER) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_favorite_vtuber, parent, false);
            return new VTuberViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_favorite_text, parent, false);
            return new TextViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        FavoritesActivity.FavoriteItem favorite = favorites.get(position);

        if (holder instanceof VTuberViewHolder) {
            VTuberViewHolder vtuberHolder = (VTuberViewHolder) holder;
            FavoritesActivity.VTuberFavorite vtuberFav = (FavoritesActivity.VTuberFavorite) favorite;
            vtuberHolder.bind(vtuberFav);
        } else if (holder instanceof TextViewHolder) {
            TextViewHolder textHolder = (TextViewHolder) holder;
            textHolder.bind(favorite);
        }
    }

    @Override
    public int getItemCount() {
        return favorites.size();
    }

    // ViewHolder for VTuber favorites
    class VTuberViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imgThumbnail;
        private final TextView tvName;
        private final TextView tvOrganization;

        public VTuberViewHolder(@NonNull View itemView) {
            super(itemView);
            imgThumbnail = itemView.findViewById(R.id.img_favorite_vtuber);
            tvName = itemView.findViewById(R.id.tv_favorite_vtuber_name);
            tvOrganization = itemView.findViewById(R.id.tv_favorite_vtuber_org);
        }

        public void bind(FavoritesActivity.VTuberFavorite vtuberFavorite) {
            tvName.setText(vtuberFavorite.getName());
            tvOrganization.setText(vtuberFavorite.getOrganization());

            // Load thumbnail
            Glide.with(context)
                    .load(vtuberFavorite.getThumbnailUrl())
                    .placeholder(R.drawable.placeholder_vtuber)
                    .into(imgThumbnail);

            // Set click listener to open VTuberDetailActivity
            itemView.setOnClickListener(v -> {
                // Create VTuber object from VTuberFavorite
                VTuber vtuber = new VTuber();
                vtuber.setName(vtuberFavorite.getName());
                vtuber.setChannelId(vtuberFavorite.getChannelId());
                vtuber.setThumbnailUrl(vtuberFavorite.getThumbnailUrl());
                vtuber.setOrg(vtuberFavorite.getOrganization());

                // Open VTuberDetailActivity
                Intent intent = new Intent(context, VTuberDetailActivity.class);
                intent.putExtra(VTuberDetailActivity.EXTRA_VTUBER, vtuber);
                context.startActivity(intent);
            });
        }
    }

    // ViewHolder for text-based favorites
    class TextViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName;

        public TextViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_favorite_name);
        }

        public void bind(FavoritesActivity.FavoriteItem favorite) {
            tvName.setText(favorite.getName());
        }
    }
}