package com.tamer.alna99.watertabdriver.view.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.tamer.alna99.watertabdriver.R;
import com.tamer.alna99.watertabdriver.model.SharedPrefs;

import java.util.Objects;

public class MyProfileFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_profile, container, false);
        TextView name = view.findViewById(R.id.my_profile_username);
        TextView rate = view.findViewById(R.id.my_profile_rate);
        TextView email = view.findViewById(R.id.my_profile_email);
        TextView phone = view.findViewById(R.id.my_profile_phone);

        if (SharedPrefs.getUserId(Objects.requireNonNull(getContext())) != null) {
            name.setText(SharedPrefs.getUserName(getContext()));
//            phone.setText(SharedPrefs.getUserPhone(getContext()));
            phone.setText("0592899024");
            email.setText(SharedPrefs.getUserEmail(getContext()));
            rate.setText(String.valueOf(SharedPrefs.getUserRate(getContext())));
        }
        return view;
    }
}