package com.applex.zephyr_task_1.Adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.applex.zephyr_task_1.Models.FormModel;
import com.applex.zephyr_task_1.R;

import java.util.ArrayList;

public class RecyclerAdapter extends RecyclerView.Adapter <RecyclerAdapter.ProgrammingViewHolder> {

    private final ArrayList<FormModel> mList;

    public RecyclerAdapter(ArrayList<FormModel> list) {
        this.mList = list;
    }

    @NonNull
    @Override
    public ProgrammingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_items, parent, false);
        return new ProgrammingViewHolder(v);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ProgrammingViewHolder holder, int position) {
        FormModel currentItem = mList.get(position);

        if(currentItem.getName() != null && !currentItem.getName().isEmpty()) {
            holder.name.setText(currentItem.getName());
        }
        else {
            holder.name_layout.setVisibility(View.GONE);
        }

        holder.age.setText(String.valueOf(currentItem.getAge()));

        if(currentItem.getAddress() != null && !currentItem.getAddress().isEmpty()) {
            holder.address.setText(currentItem.getAddress());
        }
        else {
            holder.address_layout.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public static class ProgrammingViewHolder extends RecyclerView.ViewHolder {

        TextView name, age, address;
        LinearLayout name_layout, age_layout, address_layout;

        private ProgrammingViewHolder(@NonNull View view) {
            super(view);
            name = view.findViewById(R.id.name);
            age = view.findViewById(R.id.age);
            address = view.findViewById(R.id.address);
            name_layout = view.findViewById(R.id.name_layout);
            age_layout = view.findViewById(R.id.age_layout);
            address_layout = view.findViewById(R.id.address_layout);
        }
    }
}