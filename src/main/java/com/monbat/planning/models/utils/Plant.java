package com.monbat.planning.models.utils;

public enum Plant {
    MONBAT(1000), START(1100);

    private final int id;
    Plant(int id) { this.id = id; }
    public int getValue() { return id; }
}
