package com.example.gaayathri.bookoman;

import android.app.FragmentTransaction;
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

        final ArrayList<Team> team = getData();

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

                Toast.makeText(getActivity(), team.get(groupPos).players.get(childPos), Toast.LENGTH_SHORT).show();

                return false;
            }
        });

        return view;
    }

    private ArrayList<Team> getData ()
    {

        Team t1 = new Team("Architecture");

        t1.players.add("All Architecture books");
        t1.players.add("Landscape Architecture");
        t1.players.add("Urban Planner");
        t1.players.add("Restoration Architecture");
        t1.players.add("Research Architecture");
        t1.players.add("Lighting Architecture");
        t1.players.add("Political Architecture");

        Team t2 = new Team("Arts");
        t2.players.add("All Arts Books");

        Team t3 = new Team("Commerce");
        t3.players.add("All Commerce Books");

        Team t4 = new Team("Computer Applications");
        t4.players.add("All Computer_Applications Books");

        Team t5 = new Team("Education");
        t5.players.add("All Education Books");

        Team t6 = new Team("Engineering");
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

        Team t7 = new Team("Law");
        t7.players.add("All Law Books");

        Team t8 = new Team("Literature");
        t8.players.add("English");
        t8.players.add("Tamil");
        t8.players.add("Telugu");
        t8.players.add("Malayalam");
        t8.players.add("Others");

        Team t9 = new Team("Medical");
        t9.players.add("All Medicine Books");
        t9.players.add("General Medicine");
        t9.players.add("Dental");
        t9.players.add("Pharmacy");
        t9.players.add("Nursing");
        t9.players.add("Siddha & Ayurvedha");

        Team t10 = new Team("Science");
        t10.players.add("Physics");
        t10.players.add("Chemistry");
        t10.players.add("Mathematics");
        t10.players.add("Life Science");

        Team t11 = new Team("Others");
        t11.players.add("All Other Books");

        ArrayList<Team> allTeams = new ArrayList<Team>();
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
