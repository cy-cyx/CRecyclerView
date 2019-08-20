package com.example.myapplication;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;

/**
 * create by cy
 * time : 2019/6/3
 * version : 1.0
 * Features :
 */
public class MyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;

    public MyAdapter(Context context) {
        this.context = context;
    }

    String[] strings = new String[]{"1","2","3","4"};

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RecyclerView.ViewHolder(new TextView(context)) {
        };
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((TextView) holder.itemView).setText(strings[position]);
        ViewGroup.LayoutParams vl = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 1000);
        holder.itemView.setLayoutParams(vl);
    }

    @Override
    public int getItemCount() {
        return strings.length;
    }
}
