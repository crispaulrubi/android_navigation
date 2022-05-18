package com.example.sceneviewtest;

import java.io.Serializable;

public class Conversion implements Serializable {

    private String unit_one;
    private float value_one;
    private String unit_two;
    private float value_two;

    public String getUnit_one() {
        return unit_one;
    }

    public float getValue_one() {
        return value_one;
    }

    public String getUnit_two() {
        return unit_two;
    }

    public float getValue_two() {
        return value_two;
    }

    @Override
    public String toString() {
        return "Conversion{" +
                "unit_one='" + unit_one + '\'' +
                ", value_one=" + value_one +
                ", unit_two='" + unit_two + '\'' +
                ", value_two=" + value_two +
                '}';
    }
}
