package com.example.blabla;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 11;
    private static final String TAG = "tag";
    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private TextProjectAdapter textProjectAdapter;
    SharedPreferences sharedPreferences;
    private ListenerRegistration registration;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        sharedPreferences = getSharedPreferences("blabla", Context.MODE_PRIVATE);
        firebaseAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                userSignIn();
            }
        });
        userSignIn();

        setupRecyclerView();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            TextProject dummy = createDummyTextProject();
            Intent intent = CreateTextActivity.newIntent(getApplicationContext(), dummy);
            startActivity(intent);
        });


        firebaseAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    registration.remove();
                }
            }
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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
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
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        DividerItemDecoration decorator = new DividerItemDecoration(this, LinearLayout.VERTICAL);
        decorator.setDrawable(ContextCompat.getDrawable(this, R.drawable.divider));
        recyclerView.addItemDecoration(decorator);
        textProjectAdapter = new TextProjectAdapter();
        textProjectAdapter.setOnTextProjectClickListener((textProject, view, position) -> {
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(R.menu.menu_bottom_sheet);
            bottomSheetDialog.setOnClickListener(v -> {
                handleBottomSheetMenuClick(textProject, v.getId(), position);
                bottomSheetDialog.dismiss();
            });
            bottomSheetDialog.show(this.getSupportFragmentManager(), null);
        });
        recyclerView.setAdapter(textProjectAdapter);
        listenDatabaseChanges();
        ItemTouchHelper itemTouchHelper = new
                ItemTouchHelper(new SwipeToEditDeleteCallback(textProjectAdapter, recyclerView));
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void handleBottomSheetMenuClick(TextProject textProject, @IdRes int id, int position) {
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
                textProjectAdapter.deleteItem(position);
        }
    }

    private List<TextProject> getMockList(int size) {
        List<TextProject> list = new ArrayList<>();
        TextProject textProject;
        for (int i = 0; i < size; i++) {
            textProject = new TextProject();
            textProject.setTextId(i + "");
            textProject.setTextTitle("Test Text" + i);
            textProject.setCreationDate(new Timestamp(new Date()));
            textProject.setScrollSpeed(0);
            textProject.setMirrorMode(true);
            textProject.setTextSize(5);
            textProject.setTextColor("#75a478");
            textProject.setBackgroundColor("#ffa06d");
            list.add(textProject);
        }
        return list;
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


    private void listenDatabaseChanges() {
        String userId = firebaseAuth.getCurrentUser().getUid();

        final Query collectionReference = db.collection("users").document(userId).collection("textprojects").orderBy("creationDate", Query.Direction.DESCENDING);

        registration = collectionReference.addSnapshotListener((queryDocumentSnapshots, e) -> {
            List<TextProject> list = new ArrayList<>();
            if (e != null) {
                Log.w(TAG, "Listen failed.", e);
                return;
            }

            if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                Log.d(TAG, "Data exists ");
                for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                    list.add(document.toObject(TextProject.class));
                }
                textProjectAdapter.updateList(list);

            } else {
                Log.d(TAG, "Current data: null");
            }

        });

    }


}
