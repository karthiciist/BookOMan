package com.example.gaayathri.bookoman;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;

import ss.com.bannerslider.Slider;


public class HomeFragment extends Fragment {

    private FirebaseAuth firebaseAuth;
    private Slider slider;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view;

        view = inflater.inflate(R.layout.fragment_home, container, false);

        // Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

        // Slider banner
        Slider.init(new PicassoImageLoadingService(getActivity()));
        slider = view.findViewById(R.id.banner_slider_home);
        slider.setAdapter(new MainSliderAdapter());

        return view;

    }


}
