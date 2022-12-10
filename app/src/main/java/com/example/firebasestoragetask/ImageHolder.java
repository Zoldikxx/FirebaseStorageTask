package com.example.firebasestoragetask;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ImageHolder extends RecyclerView.ViewHolder {

    ImageView igView;
    Button delete;

    public ImageHolder(@NonNull View itemView) {
        super(itemView);
        igView = itemView.findViewById(R.id.m_image);
        delete = itemView.findViewById(R.id.deleteButton);
    }
}
