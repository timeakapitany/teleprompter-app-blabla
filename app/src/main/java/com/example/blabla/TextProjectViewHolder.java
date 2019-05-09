package com.example.blabla;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TextProjectViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.creationDate)
    TextView creationDate;

    public TextProjectViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }
}
