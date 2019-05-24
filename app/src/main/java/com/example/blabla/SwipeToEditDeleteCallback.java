package com.example.blabla;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

public class SwipeToEditDeleteCallback extends ItemTouchHelper.SimpleCallback {

    TextProjectAdapter textAdapter;
    RecyclerView textRecyclerView;

    public SwipeToEditDeleteCallback(TextProjectAdapter adapter, RecyclerView recyclerView) {
        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        textAdapter = adapter;
        textRecyclerView = recyclerView;
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        int position = viewHolder.getAdapterPosition();
        if (direction == ItemTouchHelper.LEFT) {
            textAdapter.deleteItem(position);
            undoDeleteSnackbar();
        } else {
            Intent intent = SettingsActivity.newIntent(textRecyclerView.getContext(), textAdapter.getTextProject(position));
            textRecyclerView.getContext().startActivity(intent);
            //TODO: open settings activity
        }
    }

    private void undoDeleteSnackbar() {
        Snackbar snackbar = Snackbar.make(textRecyclerView,
                R.string.snackbar, Snackbar.LENGTH_LONG);
        snackbar.setAction(R.string.snackbar_undo_delete, v -> {
            textAdapter.undoDeleteItem();
        });
        snackbar.show();
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        final ColorDrawable deleteBackground = new ColorDrawable(Color.RED);
        final ColorDrawable editBackground = new ColorDrawable(Color.BLUE);
        View view = viewHolder.itemView;
        Drawable deleteIcon = ContextCompat.getDrawable(recyclerView.getContext(), R.drawable.ic_delete);
        Drawable editIcon = ContextCompat.getDrawable(recyclerView.getContext(), R.drawable.ic_edit);

        int editMargin = (view.getHeight() - editIcon.getIntrinsicHeight()) / 2;
        int editTop = view.getTop() + (view.getHeight() - editIcon.getIntrinsicHeight()) / 2;
        int editBottom = editTop + editIcon.getIntrinsicHeight();
        int editLeft = view.getLeft() + editMargin;
        int editRight = editLeft + editIcon.getIntrinsicWidth();

        int deleteMargin = (view.getHeight() - deleteIcon.getIntrinsicHeight()) / 2;
        int deleteTop = view.getTop() + (view.getHeight() - deleteIcon.getIntrinsicHeight()) / 2;
        int deleteBottom = deleteTop + deleteIcon.getIntrinsicHeight();
        int deleteRight = view.getRight() - deleteMargin;
        int deleteLeft = deleteRight - deleteIcon.getIntrinsicWidth();

        if (dX > 0) {
            editBackground.setBounds(0, view.getTop(), view.getLeft() + (int) dX, view.getBottom());
            editIcon.setBounds(editLeft, editTop, editRight, editBottom);
            editBackground.draw(c);
            editIcon.draw(c);
        } else if (dX < 0) {
            deleteBackground.setBounds(view.getRight() + (int) dX, view.getTop(), view.getRight(), view.getBottom());
            deleteIcon.setBounds(deleteLeft, deleteTop, deleteRight, deleteBottom);
            deleteBackground.draw(c);
            deleteIcon.draw(c);
        }
    }
}
