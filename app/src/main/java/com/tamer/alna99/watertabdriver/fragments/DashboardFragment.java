package com.tamer.alna99.watertabdriver.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.tamer.alna99.watertabdriver.R;

public class DashboardFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        SwitchCompat switchCompat = view.findViewById(R.id.switchStatus);
        ImageView imageView = view.findViewById(R.id.status_image_view);
        TextView statusTv = view.findViewById(R.id.status_tv);
        switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    imageView.setImageResource(R.drawable.ic_unlocked);
                    statusTv.setText(getString(R.string.online));
                } else {
                    imageView.setImageResource(R.drawable.ic_locked);
                    statusTv.setText(getString(R.string.offline));
                }
            }
        });
        return view;
    }
}