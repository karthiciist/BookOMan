package com.example.gaayathri.bookoman;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class FirstFragment extends Fragment {
    Button button;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view;

        view=inflater.inflate(R.layout.fragment_first, container, false);

        getActivity().onBackPressed();

        return view;

    }

}
