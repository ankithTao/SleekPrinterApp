package com.seaory.seaorydemo;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class CardViewAdapter extends RecyclerView.Adapter<CardViewAdapter.ImageViewHolder> {

    private List<CardViewItem> imageItems;
    private Context context;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public CardViewAdapter(List<CardViewItem> imageItems, Context context) {
        this.imageItems = imageItems;
        this.context = context;
    }

    @Override
    public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.selectable_card_layout, parent, false);
        view.setBackgroundColor(Color.TRANSPARENT);
        return new ImageViewHolder(view, mListener);
    }

    @Override
    public void onBindViewHolder(ImageViewHolder holder, int position) {
        CardViewItem item = imageItems.get(position);

        holder.imageView.setImageBitmap(item.getBitmapImage());
        holder.textView.setText(item.getText());

        // Set the status label based on the order status
        if (item.getOrderStatus().equals("paid")) {
            holder.statusLabel.setText("Paid");
            holder.statusLabel.setBackgroundColor(Color.GREEN);
            holder.statusLabel.setVisibility(View.VISIBLE);
        } else if (item.getOrderStatus().equals("pending")) {
            holder.statusLabel.setText("Unpaid");
            holder.statusLabel.setBackgroundColor(Color.YELLOW);
            holder.statusLabel.setVisibility(View.VISIBLE);
        } else {
            holder.statusLabel.setVisibility(View.GONE);
        }

        // Grey out the card if shippingStatus is "delivered"
        if (item.getShippingStatus().equals("delivered")) {
            holder.itemView.setAlpha(0.5f);
        } else {
            holder.itemView.setAlpha(1f);
        }

        if (item.isSelected()) {
            GradientDrawable borderDrawable = new GradientDrawable();
            borderDrawable.setStroke(16, Color.RED); // Set the border width and color
            holder.imageView.setBackground(borderDrawable);
        } else {
            holder.imageView.setBackground(null);
        }
    }

    @Override
    public int getItemCount() {
        return imageItems.size();
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textView;
        TextView statusLabel;

        public ImageViewHolder(View itemView, final OnItemClickListener listener) {
            super(itemView);
            imageView = itemView.findViewById(R.id.cardImageView);
            textView = itemView.findViewById(R.id.cardLabel);
            statusLabel = itemView.findViewById(R.id.statusLabel);

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(position);
                        }
                    }
                }
            });
        }
    }
}
