package com.example.blabla.ui.main;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.blabla.R;
import com.example.blabla.model.TextProject;
import com.example.blabla.ui.play.PlayTextActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

class TextProjectAdapter extends RecyclerView.Adapter<TextProjectViewHolder> {

    private int deletedItemPosition;
    private TextProject deletedItem;
    private OnTextProjectClickListener onTextProjectClickListener;
    private List<TextProject> items = new ArrayList<>();
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-YYYY");

    public void setOnTextProjectClickListener(OnTextProjectClickListener onTextProjectClickListener) {
        this.onTextProjectClickListener = onTextProjectClickListener;
    }

    public TextProject getDeletedItem() {
        return deletedItem;
    }

    @NonNull
    @Override
    public TextProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_textproject, parent, false);
        return new TextProjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TextProjectViewHolder holder, int position) {
        final TextProject item = getItem(position);
        holder.title.setText(item.getTextTitle());
        String date = dateFormat.format(item.getCreationDate().toDate());
        holder.creationDate.setText(date);
        final Context context = holder.title.getContext();
        holder.itemView.setOnClickListener(v -> {
            Intent intent = PlayTextActivity.newIntent(context, item);
            context.startActivity(intent);
        });

        holder.more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onTextProjectClickListener.onTextProjectClicked(item, v, holder.getAdapterPosition());
            }
        });
    }

    private TextProject getItem(int position) {
        return items.get(position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void deleteItem(int position) {
        deletedItemPosition = position;
        deletedItem = getItem(position);
        items.remove(position);
        notifyItemRemoved(position);
    }

    public void undoDeleteItem() {
        items.add(deletedItemPosition, deletedItem);
        notifyItemInserted(deletedItemPosition);
    }

    public TextProject getTextProject(int position) {
        return getItem(position);
    }

    public void updateList(List<TextProject> newList) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new TextProjectDiffCallback(items, newList));
        items = newList;
        diffResult.dispatchUpdatesTo(this);
    }


}
