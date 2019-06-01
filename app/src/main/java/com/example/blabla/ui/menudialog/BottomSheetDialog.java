package com.example.blabla.ui.menudialog;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.MenuRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.blabla.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class BottomSheetDialog extends BottomSheetDialogFragment {

    private int menuRes;
    private View.OnClickListener onClickListener;
    private String title;

    public BottomSheetDialog(@MenuRes int menuRes) {
        this.menuRes = menuRes;
    }

    public BottomSheetDialog() {
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = View.inflate(getContext(), R.layout.fragment_bottom_sheet_menu, null);
        TextView titleView = view.findViewById(R.id.bottom_sheet_title);
        titleView.setText(title);
        setupMenu(view);
        return view;
    }

//    @Override
//    public void onSaveInstanceState(@NonNull Bundle outState) {
//        outState.putInt("menu", menuRes);
//        outState.putString("title", title);
//        super.onSaveInstanceState(outState);
//    }

    @Override
    public void onDestroyView() {
        Dialog dialog = getDialog();
        if (dialog != null && getRetainInstance()) {
            dialog.setDismissMessage(null);
        }
        super.onDestroyView();
    }

//    @Override
//    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
//        super.onViewStateRestored(savedInstanceState);
//        menuRes = savedInstanceState.getInt("menu");
//        title = savedInstanceState.getString("title");
//    }

    private void setupMenu(View view) {
        LinearLayout layout = view.findViewById(R.id.menu_container);
        PopupMenu popupMenu = new PopupMenu(getContext(), view);
        Menu menu = popupMenu.getMenu();
        MenuInflater inflater = new MenuInflater(getContext());
        inflater.inflate(menuRes, menu);
        for (int i = 0; i < menu.size(); i++) {
            MenuItem menuItem = menu.getItem(i);
            View menuItemView = View.inflate(getContext(), R.layout.item_bottom_sheet_menu, null);
            ((ImageView) menuItemView.findViewById(R.id.menu_icon)).setImageDrawable(menuItem.getIcon());
            ((TextView) menuItemView.findViewById(R.id.menu_title)).setText(menuItem.getTitle());
            menuItemView.setId(menuItem.getItemId());
            menuItemView.setOnClickListener(onClickListener);
            layout.addView(menuItemView);
        }
    }

    public void setTitle(String title) {
        this.title = title;
    }

    private static class SavedState extends View.BaseSavedState {
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel source) {
                return new SavedState(source);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        int menu;
        String title;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        protected SavedState(Parcel in) {
            super(in);
            this.menu = in.readInt();
            this.title = in.readString();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.menu);
            dest.writeString(this.title);
        }
    }
}
