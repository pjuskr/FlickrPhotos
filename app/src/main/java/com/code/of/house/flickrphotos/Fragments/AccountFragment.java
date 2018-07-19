package com.code.of.house.flickrphotos.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.code.of.house.flickrphotos.FlickrAPiManager;
import com.code.of.house.flickrphotos.R;

public class AccountFragment extends Fragment {

    ImageView profilePicture;
    TextView username;
    Button button;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        profilePicture = view.findViewById(R.id.account_icon);
        username = view.findViewById(R.id.account_name);
        button = view.findViewById(R.id.account_logout);

        profilePicture.setImageBitmap(FlickrAPiManager.flickrUser.user_icon);
        username.setText(FlickrAPiManager.flickrUser.username);

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public static AccountFragment newInstance(){
        AccountFragment fragment = new AccountFragment();
        return fragment;
    }
}
