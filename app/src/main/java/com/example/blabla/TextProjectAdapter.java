package com.example.blabla;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;


class TextProjectAdapter extends ListAdapter<TextProject, TextProjectViewHolder> {

    private int deletedItemPosition;
    private TextProject deletedItem;
    private OnTextProjectClickListener onTextProjectClickListener;

    public void setOnTextProjectClickListener(OnTextProjectClickListener onTextProjectClickListener) {
        this.onTextProjectClickListener = onTextProjectClickListener;
    }



    protected TextProjectAdapter() {
        super(new DiffUtil.ItemCallback<TextProject>() {
            @Override
            public boolean areItemsTheSame(@NonNull TextProject oldItem, @NonNull TextProject newItem) {
                return (oldItem.getTextId().equals(newItem.getTextId()));
            }

            @Override
            public boolean areContentsTheSame(@NonNull TextProject oldItem, @NonNull TextProject newItem) {
                return (oldItem.getTextTitle().equals(newItem.getTextTitle()) &&
                        oldItem.getCreationDate().equals(newItem.getCreationDate()));
            }
        });
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
        holder.creationDate.setText(item.getCreationDate().toDate().toString());
        final Context context = holder.title.getContext();
        holder.itemView.setOnClickListener(v -> {
            Intent intent = PlayTextActivity.newIntent(context, item);
            context.startActivity(intent);
        });

        holder.itemView.findViewById(R.id.more).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onTextProjectClickListener.onTextProjectClicked(item, v, holder.getAdapterPosition());
            }
        });

    }

    public void deleteItem(int position) {
        deletedItemPosition = position;
        deletedItem = getItem(position);
        //TODO: remove from text list
        notifyItemRemoved(position);
    }


    public void undoDeleteItem() {
        //TODO: add item back to list
        notifyItemInserted(deletedItemPosition);
    }

    public TextProject getTextProject(int position) {
        return getItem(position);
    }


}
