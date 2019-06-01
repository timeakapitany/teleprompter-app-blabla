package com.example.blabla.ui.main;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.blabla.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TextProjectViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.creationDate)
    TextView creationDate;
    @BindView(R.id.more)
    ImageView more;

    public TextProjectViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }
}
