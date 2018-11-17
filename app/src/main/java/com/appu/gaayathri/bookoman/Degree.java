package com.appu.gaayathri.bookoman;

import java.util.ArrayList;

public class Degree {

    public String Name;
    public String Image;
    public ArrayList<String> players=new ArrayList<String>();

    public Degree(String Name)
    {
        this.Name=Name;
    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return Name;
    }

}
