package com.example.android.album;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.airbnb.lottie.LottieAnimationView;

public class AddFragment extends Fragment {

    Button newEvent;
    LottieAnimationView animationView;
    private static final int RC_CONGRATS =  2;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.add_fragment,null);
        newEvent = view.findViewById(R.id.new_event);

        animationView = view.findViewById(R.id.congrats);

        newEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(getActivity(), NewEventActivity.class);
//                startActivityForResult(intent,RC_CONGRATS);
            }
        });
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_CONGRATS) {

            if(resultCode == Activity.RESULT_OK){
                //Get data of whether the createEvent button is clicked
                boolean result = data.getBooleanExtra("selection_of_create_event_button",false);
                if(result){
                    //If it's clicked, play animation
                    animationView.playAnimation();
                }
            }

        }

    }
}
