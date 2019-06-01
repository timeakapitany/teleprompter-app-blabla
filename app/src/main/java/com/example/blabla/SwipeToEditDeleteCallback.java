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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import timber.log.Timber;

public class SwipeToEditDeleteCallback extends ItemTouchHelper.SimpleCallback {

    private final TextProjectAdapter textAdapter;
    private final RecyclerView textRecyclerView;
    private final Drawable deleteIcon;
    private final Drawable editIcon;
    private final ColorDrawable deleteBackground;
    private final ColorDrawable editBackground;
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public SwipeToEditDeleteCallback(TextProjectAdapter adapter, RecyclerView recyclerView) {
        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        textAdapter = adapter;
        textRecyclerView = recyclerView;
        deleteIcon = ContextCompat.getDrawable(recyclerView.getContext(), R.drawable.ic_delete);
        editIcon = ContextCompat.getDrawable(recyclerView.getContext(), R.drawable.ic_edit);
        deleteBackground = new ColorDrawable(Color.RED);
        editBackground = new ColorDrawable(recyclerView.getContext().getResources().getColor(R.color.colorPrimaryDark));
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
        }
    }

    private void undoDeleteSnackbar() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Snackbar snackbar = Snackbar.make(textRecyclerView,
                R.string.snackbar, Snackbar.LENGTH_LONG);
        Snackbar.Callback callback = new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar transientBottomBar, int event) {
                TextProject deletedText = textAdapter.getDeletedItem();
                if (deletedText != null) {
                    storageRef.child(userId).child(deletedText.getTextReference()).delete()
                            .addOnFailureListener(e -> Timber.d("onFailure: "))
                            .addOnSuccessListener(aVoid -> Timber.d("onSuccess: "));
                    db.collection("users")
                            .document(userId)
                            .collection("textprojects")
                            .document(deletedText.getTextId())
                            .delete()
                            .addOnSuccessListener(aVoid -> Timber.d("onSuccess: "))
                            .addOnFailureListener(e -> Timber.d("onFailure: "));
                }
                super.onDismissed(transientBottomBar, event);
            }
        };
        snackbar.setAction(R.string.snackbar_undo_delete, v -> {
            snackbar.removeCallback(callback);
            textAdapter.undoDeleteItem();
        });

        snackbar.addCallback(callback);
        snackbar.show();
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        View view = viewHolder.itemView;

        if (dX > 0) {
            int editMargin = (view.getHeight() - editIcon.getIntrinsicHeight()) / 2;
            int editTop = view.getTop() + (view.getHeight() - editIcon.getIntrinsicHeight()) / 2;
            int editBottom = editTop + editIcon.getIntrinsicHeight();
            int editLeft = view.getLeft() + editMargin;
            int editRight = editLeft + editIcon.getIntrinsicWidth();
            editBackground.setBounds(0, view.getTop(), view.getLeft() + (int) dX, view.getBottom());
            editIcon.setBounds(editLeft, editTop, editRight, editBottom);
            editBackground.draw(c);
            editIcon.draw(c);
        } else if (dX < 0) {
            int deleteMargin = (view.getHeight() - deleteIcon.getIntrinsicHeight()) / 2;
            int deleteTop = view.getTop() + (view.getHeight() - deleteIcon.getIntrinsicHeight()) / 2;
            int deleteBottom = deleteTop + deleteIcon.getIntrinsicHeight();
            int deleteRight = view.getRight() - deleteMargin;
            int deleteLeft = deleteRight - deleteIcon.getIntrinsicWidth();
            deleteBackground.setBounds(view.getRight() + (int) dX, view.getTop(), view.getRight(), view.getBottom());
            deleteIcon.setBounds(deleteLeft, deleteTop, deleteRight, deleteBottom);
            deleteBackground.draw(c);
            deleteIcon.draw(c);
        }
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);


    }

    @Override
    public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);
        viewHolder.itemView.setTranslationX(0f);
    }
}
