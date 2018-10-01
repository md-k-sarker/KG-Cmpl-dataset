package org.dase.synthetic;
/*
Written by sarker.
Written at 9/28/18.
*/

enum SyntheticType {

    Class_SubSumption(1, "class_subsumption"), Property_SubSumption(2, "property_subsumption");

    private String name;

    private int id;

    private SyntheticType(int id, String name) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

}
