package com.example.gaayathri.bookoman;

import java.util.ArrayList;

public class Team {

    public String Name;
    public String Image;
    public ArrayList<String> players=new ArrayList<String>();

    public Team(String Name)
    {
        this.Name=Name;
    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return Name;
    }

}
