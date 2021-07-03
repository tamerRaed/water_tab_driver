package com.tamer.alna99.watertabdriver.view.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.tamer.alna99.watertabdriver.R;
import com.tamer.alna99.watertabdriver.model.AnswerInterface;

public class BottomSheetFragment extends BottomSheetDialogFragment {
    AnswerInterface answerInterface;

    public BottomSheetFragment(AnswerInterface answerInterface) {
        this.answerInterface = answerInterface;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.answer_order_event, container, false);
        Button rejection = view.findViewById(R.id.btn_rejection);
        Button accept = view.findViewById(R.id.btn_accept);
        rejection.setOnClickListener(view1 -> {
            answerInterface.answer(0);
            this.dismiss();

        });
        accept.setOnClickListener(view12 -> {
            answerInterface.answer(1);
            this.dismiss();
        });

        return view;
    }
}
