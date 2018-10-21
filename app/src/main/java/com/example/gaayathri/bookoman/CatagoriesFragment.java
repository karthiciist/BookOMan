package com.example.gaayathri.bookoman;

import android.content.res.Resources;
import android.graphics.Point;
import android.os.Bundle;
import android.app.Fragment;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.Toast;

import java.util.ArrayList;

public class CatagoriesFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view;

        view = inflater.inflate(R.layout.fragment_catagories, container, false);

        getActivity().setTitle("Categories");

        //THE EXPANDABLE
        ExpandableListView elv = view.findViewById(R.id.expandableListView2);

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        Resources r = getResources();
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                50, r.getDisplayMetrics());
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
            elv.setIndicatorBounds(width - px, width);
        } else {
            elv.setIndicatorBoundsRelative(width - px, width);
        }

        final ArrayList<Degree> team = getData();

        //CREATE AND BIND TO ADAPTER
        CustomAdapter adapter = new CustomAdapter((HomeActivity) getActivity(), team);
        elv.setAdapter(adapter);


        //Put the value
        final CatagoriesExpandedFragment catagoriesExpandedFragment = new CatagoriesExpandedFragment ();
        final Bundle args = new Bundle();


        //SET ONCLICK LISTENER
        elv.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPos,
                                        int childPos, long id) {

                args.putString("category", team.get(groupPos).players.get(childPos));
                catagoriesExpandedFragment.setArguments(args);

                getFragmentManager().beginTransaction().replace(R.id.frm, catagoriesExpandedFragment).addToBackStack(null).commit();

                //Toast.makeText(getActivity(), team.get(groupPos).players.get(childPos), Toast.LENGTH_SHORT).show();

                return false;
            }
        });

        return view;
    }

    private ArrayList<Degree> getData ()
    {

        Degree t1 = new Degree("Architecture");

        t1.players.add("All Architecture books");
        t1.players.add("Landscape Architecture");
        t1.players.add("Urban Planner");
        t1.players.add("Restoration Architecture");
        t1.players.add("Research Architecture");
        t1.players.add("Lighting Architecture");
        t1.players.add("Political Architecture");

        Degree t2 = new Degree("Arts");
        t2.players.add("All Arts Books");

        Degree t3 = new Degree("Commerce");
        t3.players.add("All Commerce Books");

        Degree t4 = new Degree("Computer Applications");
        t4.players.add("All Computer Applications Books");

        Degree t5 = new Degree("Education");
        t5.players.add("All Education Books");

        Degree t6 = new Degree("Engineering");
        t6.players.add("All Engineering books");
        t6.players.add("CSE");
        t6.players.add("ECE");
        t6.players.add("EEE");
        t6.players.add("E&I");
        t6.players.add("Information Technology");
        t6.players.add("ICE");
        t6.players.add("Mechanical Engineering");
        t6.players.add("Mechatronics");
        t6.players.add("Production Engineering");

        Degree t7 = new Degree("Law");
        t7.players.add("All Law Books");

        Degree t8 = new Degree("Literature");
        t8.players.add("All Literature books");
        t8.players.add("English");
        t8.players.add("Tamil");
        t8.players.add("Telugu");
        t8.players.add("Malayalam");
        t8.players.add("Others");

        Degree t9 = new Degree("Medical");
        t9.players.add("All Medicine Books");
        t9.players.add("General Medicine");
        t9.players.add("Dental");
        t9.players.add("Pharmacy");
        t9.players.add("Nursing");
        t9.players.add("Siddha & Ayurvedha");

        Degree t10 = new Degree("Science");
        t10.players.add("All Science Books");
        t10.players.add("Physics");
        t10.players.add("Chemistry");
        t10.players.add("Mathematics");
        t10.players.add("Life Science");

        Degree t11 = new Degree("Others");
        t11.players.add("All Other Books");

        ArrayList<Degree> allTeams = new ArrayList<Degree>();
        allTeams.add(t1);
        allTeams.add(t2);
        allTeams.add(t3);
        allTeams.add(t4);
        allTeams.add(t5);
        allTeams.add(t6);
        allTeams.add(t7);
        allTeams.add(t8);
        allTeams.add(t9);
        allTeams.add(t10);
        allTeams.add(t11);

        return allTeams;
    }


}
