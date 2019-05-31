package com.example.blabla;

import android.os.Bundle;
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

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class BottomSheetDialog extends BottomSheetDialogFragment {

    private int menuRes;
    private View.OnClickListener onClickListener;
    private String title;

    public BottomSheetDialog(@MenuRes int menuRes) {
        this.menuRes = menuRes;
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
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
}
