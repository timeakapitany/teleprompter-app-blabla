package com.example.blabla.ui.main;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupMenu;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.blabla.R;
import com.example.blabla.model.TextProject;
import com.example.blabla.ui.create.CreateTextActivity;
import com.example.blabla.ui.play.PlayTextActivity;
import com.example.blabla.ui.settings.SettingsActivity;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 11;
    private static final String TAG = "tag";
    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private TextProjectAdapter textProjectAdapter;
    SharedPreferences sharedPreferences;
    private ListenerRegistration registration;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    private final StorageReference storageRef = storage.getReference();

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.fab)
    FloatingActionButton fab;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        sharedPreferences = getSharedPreferences("blabla", Context.MODE_PRIVATE);
        setupRecyclerView();

        firebaseAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    if (registration != null) {
                        registration.remove();
                    }
                    userSignIn();
                } else {
                    listenDatabaseChanges();
                }
            }
        });

        fab.setOnClickListener(view -> {
            TextProject dummy = createDummyTextProject();
            Intent intent = CreateTextActivity.newIntent(getApplicationContext(), dummy);
            startActivity(intent);
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            TextProject dummy = createDummyTextProject();
            Intent intent = SettingsActivity.newIntent(this, dummy);
            this.startActivity(intent);
            return true;
        }
        if (id == R.id.action_log_out) {
            userSignOut();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                getSharedPreferences("blabla", Context.MODE_PRIVATE).edit().putString("UserId", user.getUid()).apply();
            } else {
                //TODO
            }
        }
    }

    private void userSignIn() {
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build());

        if (firebaseAuth.getCurrentUser() == null) {
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(providers)
                            .build(),
                    RC_SIGN_IN);
        }
    }

    private void userSignOut() {
        firebaseAuth.signOut();
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        DividerItemDecoration decorator = new DividerItemDecoration(this, LinearLayout.VERTICAL);
        decorator.setDrawable(ContextCompat.getDrawable(this, R.drawable.divider));
        recyclerView.addItemDecoration(decorator);
        textProjectAdapter = new TextProjectAdapter();
        textProjectAdapter.setOnTextProjectClickListener((textProject, view, position) -> {
            showPopupMenu(textProject, view, position);

        });
        recyclerView.setAdapter(textProjectAdapter);
        ItemTouchHelper itemTouchHelper = new
                ItemTouchHelper(new SwipeToEditDeleteCallback());
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void showPopupMenu(TextProject project, View view, int position) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        Menu menu = popupMenu.getMenu();
        MenuInflater inflater = new MenuInflater(this);
        inflater.inflate(R.menu.menu_bottom_sheet, menu);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                handleMenuClick(project, item.getItemId(), position);
                popupMenu.dismiss();
                return true;
            }
        });

        popupMenu.show();

    }

    private void handleMenuClick(TextProject textProject, @IdRes int id, int position) {
        Intent intent;
        switch (id) {
            case R.id.action_play:
                intent = PlayTextActivity.newIntent(this, textProject);
                this.startActivity(intent);
                break;
            case R.id.action_edit:
                intent = SettingsActivity.newIntent(this, textProject);
                this.startActivity(intent);
                break;
            case R.id.action_delete:
                deleteItem(position);
        }
    }


    private TextProject createDummyTextProject() {
        TextProject dummy = new TextProject();
        dummy.setTextId(null);
        dummy.setBackgroundColor(sharedPreferences.getString(getString(R.string.preference_background_color), getString(R.string.default_background)));
        dummy.setTextColor(sharedPreferences.getString(getString(R.string.preference_text_color), getString(R.string.default_text_color)));
        dummy.setTextSize(sharedPreferences.getInt(getString(R.string.preference_text_size), getResources().getInteger(R.integer.default_text_size)));
        dummy.setScrollSpeed(sharedPreferences.getInt(getString(R.string.preference_scroll_speed), getResources().getInteger(R.integer.default_scroll_speed)));
        dummy.setMirrorMode(sharedPreferences.getBoolean(getString(R.string.preference_mirror_mode), getResources().getBoolean(R.bool.default_mirror)));
        return dummy;
    }

    private void deleteItem(int position) {
        textProjectAdapter.deleteItem(position);
        undoDeleteSnackbar();
    }


    private void undoDeleteSnackbar() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Snackbar snackbar = Snackbar.make(recyclerView,
                R.string.snackbar, Snackbar.LENGTH_LONG);
        Snackbar.Callback callback = new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar transientBottomBar, int event) {
                TextProject deletedText = textProjectAdapter.getDeletedItem();
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
            textProjectAdapter.undoDeleteItem();
        });

        snackbar.addCallback(callback);
        snackbar.show();
    }

    private void listenDatabaseChanges() {
        String userId = firebaseAuth.getCurrentUser().getUid();

        final Query collectionReference = db.collection("users").document(userId).collection("textprojects").orderBy("creationDate", Query.Direction.DESCENDING);

        registration = collectionReference.addSnapshotListener((queryDocumentSnapshots, e) -> {
            List<TextProject> list = new ArrayList<>();
            if (e != null) {
                Timber.w(e);
                return;
            }

            if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                Timber.d("Data exists ");
                for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                    list.add(document.toObject(TextProject.class));
                }
                textProjectAdapter.updateList(list);

            } else {
                Timber.d("Current data: null");
            }

        });

    }


    public class SwipeToEditDeleteCallback extends ItemTouchHelper.SimpleCallback {

        private final Drawable deleteIcon;
        private final Drawable editIcon;
        private final ColorDrawable deleteBackground;
        private final ColorDrawable editBackground;


        SwipeToEditDeleteCallback() {
            super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
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
                deleteItem(position);
            } else {
                Intent intent = SettingsActivity.newIntent(recyclerView.getContext(), textProjectAdapter.getTextProject(position));
                recyclerView.getContext().startActivity(intent);
            }
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

}
