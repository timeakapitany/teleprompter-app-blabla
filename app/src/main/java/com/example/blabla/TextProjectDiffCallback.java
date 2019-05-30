package com.example.blabla;

import androidx.recyclerview.widget.DiffUtil;

import java.util.List;

public class TextProjectDiffCallback extends DiffUtil.Callback {

    private List<TextProject> oldItems;
    private List<TextProject> newItems;

    public TextProjectDiffCallback(List<TextProject> oldItems, List<TextProject> newItems) {
        this.oldItems = oldItems;
        this.newItems = newItems;
    }

    @Override
    public int getOldListSize() {
        return oldItems.size();
    }

    @Override
    public int getNewListSize() {
        return newItems.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        TextProject oldItem = oldItems.get(oldItemPosition);
        TextProject newItem = newItems.get(newItemPosition);
        return (oldItem.getTextId().equals(newItem.getTextId()));
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        TextProject oldItem = oldItems.get(oldItemPosition);
        TextProject newItem = newItems.get(newItemPosition);
        return (oldItem.getTextTitle().equals(newItem.getTextTitle()) &&
                oldItem.getCreationDate().equals(newItem.getCreationDate()));
    }

}
