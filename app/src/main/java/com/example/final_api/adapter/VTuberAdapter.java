package com.example.final_api.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.final_api.R;
import com.example.final_api.model.VTuber;

import java.util.ArrayList;
import java.util.List;

public class VTuberAdapter extends RecyclerView.Adapter<VTuberAdapter.ViewHolder> {
    
    private List<VTuber> vtubers;
    private Context context;
    private OnVTuberClickListener listener;
    
    // Add this interface for click events
    public interface OnVTuberClickListener {
        void onVTuberClick(VTuber vtuber);
    }
    
    // Update constructor to accept click listener
    public VTuberAdapter(Context context, OnVTuberClickListener listener) {
        this.context = context;
        this.vtubers = new ArrayList<>();
        this.listener = listener;
    }
    
    // If you already have instances of the adapter without listener, add this constructor
    public VTuberAdapter(Context context) {
        this(context, null);
    }
    
    // Set listener later if needed
    public void setOnVTuberClickListener(OnVTuberClickListener listener) {
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_vtuber_profile, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        VTuber vtuber = vtubers.get(position);
        
        // Set basic info
        holder.tvName.setText(vtuber.getName());
        holder.tvChannelId.setText(vtuber.getChannelId());
        
        // Set description (if available)
        if (vtuber.getDescription() != null && !vtuber.getDescription().isEmpty()) {
            holder.tvDescription.setText(vtuber.getDescription());
            holder.tvDescription.setVisibility(View.VISIBLE);
        } else {
            holder.tvDescription.setVisibility(View.GONE);
        }
        
        // Load thumbnail with Glide
        Glide.with(context)
                .load(vtuber.getThumbnailUrl())
                .placeholder(R.drawable.placeholder_vtuber)
                .into(holder.imgVtuber);
                
        // Set subscriber count
        if (holder.tvSubscriberCount != null && vtuber.getSubscriberCount() > 0) {
            String subCount = formatSubscriberCount(vtuber.getSubscriberCount());
            holder.tvSubscriberCount.setText(subCount);
        } else if (holder.tvSubscriberCount != null) {
            holder.tvSubscriberCount.setText("N/A");
        }
        
        // Set generation
        if (holder.tvGeneration != null) {
            String generation = formatGeneration(vtuber.getSuborg(), vtuber);
            holder.tvGeneration.setText(generation);
        }
        
        // Set debut date
        if (holder.tvDebutDate != null) {
            if (vtuber.getDebutDate() != null && !vtuber.getDebutDate().isEmpty()) {
                holder.tvDebutDate.setText(formatDebutDate(vtuber.getDebutDate()));
            } else {
                // Try guessing based on generation
                String generation = formatGeneration(vtuber.getSuborg(), vtuber);
                if (generation.contains("Myth")) {
                    holder.tvDebutDate.setText("September 2020");
                } else if (generation.contains("Council")) {
                    holder.tvDebutDate.setText("August 2021");
                } else {
                    holder.tvDebutDate.setText("Unknown");
                }
            }
        }
        
        // Set languages if available
        if (holder.languagesContainer != null) {
            holder.languagesContainer.removeAllViews(); // Clear previous languages
            
            List<String> languages = vtuber.getLanguages();
            if (languages != null && !languages.isEmpty()) {
                for (String language : languages) {
                    TextView languageTag = createLanguageTag(language);
                    holder.languagesContainer.addView(languageTag);
                }
            } else {
                // Guess languages based on name/generation
                String subOrg = vtuber.getSuborg() != null ? vtuber.getSuborg().toLowerCase() : "";
                String name = vtuber.getName().toLowerCase();
                
                if (subOrg.contains("en") || name.contains("gura") || name.contains("fauna") || 
                    name.contains("kronii") || name.contains("mumei") || name.contains("baelz")) {
                    holder.languagesContainer.addView(createLanguageTag("English"));
                } else if (subOrg.contains("id") || name.contains("risu") || 
                          name.contains("moona") || name.contains("ollie")) {
                    holder.languagesContainer.addView(createLanguageTag("English"));
                    holder.languagesContainer.addView(createLanguageTag("Indonesian"));
                } else {
                    holder.languagesContainer.addView(createLanguageTag("Japanese"));
                }
            }
        }
        
        // Add this at the end of onBindViewHolder
        holder.bind(vtuber, listener);
    }
    
    private TextView createLanguageTag(String language) {
        TextView tag = new TextView(context);
        tag.setText(language);
        tag.setBackgroundResource(R.drawable.bg_language_tag);
        
        // Set different colors based on language
        int colorResId;
        switch (language.toLowerCase()) {
            case "english":
                colorResId = android.R.color.holo_blue_light;
                break;
            case "japanese":
                colorResId = android.R.color.holo_red_light;
                break;
            case "indonesian":
                colorResId = android.R.color.holo_orange_light;
                break;
            default:
                colorResId = android.R.color.holo_purple;
                break;
        }
        
        tag.getBackground().setTint(context.getResources().getColor(colorResId));
        tag.setTextColor(context.getResources().getColor(android.R.color.white));
        tag.setPadding(24, 12, 24, 12);
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 16, 0);
        tag.setLayoutParams(params);
        
        return tag;
    }
    
    private String formatSubscriberCount(long count) {
        if (count >= 1000000) {
            return String.format("%.1fM", count / 1000000.0);
        } else if (count >= 1000) {
            return String.format("%.1fK", count / 1000.0);
        } else {
            return String.valueOf(count);
        }
    }
    
    private String formatGeneration(String suborg, VTuber vtuber) {
        if (suborg == null || suborg.isEmpty()) {
            // Now vtuber is accessible as a parameter
            String name = vtuber.getName().toLowerCase();
            if (name.contains("council") || name.contains("irys") || 
                name.contains("fauna") || name.contains("kronii") || 
                name.contains("mumei") || name.contains("baelz") || 
                name.contains("sana")) {
                return "Hololive EN - Council";
            } else if (name.contains("myth") || name.contains("gura") || 
                      name.contains("amelia") || name.contains("kiara") || 
                      name.contains("ina") || name.contains("calliope")) {
                return "Hololive EN - Myth";
            } else if (name.contains("indonesia") || name.contains("risu") || 
                      name.contains("moona") || name.contains("iofi") || 
                      name.contains("ollie") || name.contains("anya")) {
                return "Hololive Indonesia";
            }
            return "Hololive";
        }
        
        // Format nicely based on suborg value
        String suborgLower = suborg.toLowerCase();
        if (suborgLower.contains("council") || suborgLower.contains("en-council")) {
            return "Hololive EN - Council";
        } else if (suborgLower.contains("myth") || suborgLower.contains("en-myth")) {
            return "Hololive EN - Myth";  
        } else if (suborgLower.contains("en")) {
            return "Hololive English";
        } else if (suborgLower.contains("jp")) {
            return "Hololive Japan";
        } else if (suborgLower.contains("id")) {
            return "Hololive Indonesia";
        } else if (suborgLower.contains("hololive-")) {
            return "Hololive " + suborg.replace("hololive-", "");
        }
        return suborg;
    }
    
    private String formatDebutDate(String rawDate) {
        if (rawDate == null || rawDate.isEmpty()) {
            return "Unknown";
        }
        
        // The API might return date in different formats
        // This handles simple date strings without complex parsing
        try {
            // If it's already in a nice format, just return it
            if (rawDate.contains(" ")) {
                return rawDate;
            }
            
            // If it's in ISO format like "2020-09-13"
            if (rawDate.contains("-")) {
                String[] parts = rawDate.split("-");
                if (parts.length >= 3) {
                    int year = Integer.parseInt(parts[0]);
                    int month = Integer.parseInt(parts[1]);
                    int day = Integer.parseInt(parts[2]);
                    
                    String monthName = "";
                    switch (month) {
                        case 1: monthName = "January"; break;
                        case 2: monthName = "February"; break;
                        case 3: monthName = "March"; break;
                        case 4: monthName = "April"; break;
                        case 5: monthName = "May"; break;
                        case 6: monthName = "June"; break;
                        case 7: monthName = "July"; break;
                        case 8: monthName = "August"; break;
                        case 9: monthName = "September"; break;
                        case 10: monthName = "October"; break;
                        case 11: monthName = "November"; break;
                        case 12: monthName = "December"; break;
                    }
                    
                    return monthName + " " + day + ", " + year;
                }
            }
            
            // If we can't parse it, just return the original
            return rawDate;
        } catch (Exception e) {
            return rawDate; // Return original if parsing fails
        }
    }
    
    @Override
    public int getItemCount() {
        return vtubers.size();
    }
    
    public void updateData(List<VTuber> newData) {
        this.vtubers.clear();
        if (newData != null) {
            this.vtubers.addAll(newData);
        }
        notifyDataSetChanged();
    }
    
    // Update the ViewHolder class to handle clicks
    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgVtuber;
        TextView tvName, tvChannelId, tvDescription;
        TextView tvSubscriberCount, tvGeneration, tvDebutDate;
        LinearLayout languagesContainer;
        
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgVtuber = itemView.findViewById(R.id.img_vtuber);
            tvName = itemView.findViewById(R.id.tv_name);
            tvChannelId = itemView.findViewById(R.id.tv_channel_id);
            tvDescription = itemView.findViewById(R.id.tv_description);
            tvSubscriberCount = itemView.findViewById(R.id.tv_subscriber_count);
            tvGeneration = itemView.findViewById(R.id.tv_generation);
            tvDebutDate = itemView.findViewById(R.id.tv_debut_date);
            languagesContainer = itemView.findViewById(R.id.languages_container);
        }
        
        // Add a method to bind click listener
        void bind(final VTuber vtuber, final OnVTuberClickListener listener) {
            if (listener != null) {
                itemView.setOnClickListener(v -> listener.onVTuberClick(vtuber));
            }
        }
    }
}