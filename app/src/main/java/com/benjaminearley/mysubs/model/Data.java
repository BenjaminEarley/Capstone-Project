package com.benjaminearley.mysubs.model;

import java.util.ArrayList;
import java.util.List;


public class Data {

    private String modhash;
    private List<Child> children = new ArrayList<Child>();
    private String after;
    private Object before;

    /**
     * @return The modhash
     */
    public String getModhash() {
        return modhash;
    }

    /**
     * @param modhash The modhash
     */
    public void setModhash(String modhash) {
        this.modhash = modhash;
    }

    /**
     * @return The children
     */
    public List<Child> getChildren() {
        return children;
    }

    /**
     * @param children The children
     */
    public void setChildren(List<Child> children) {
        this.children = children;
    }

    /**
     * @return The after
     */
    public String getAfter() {
        return after;
    }

    /**
     * @param after The after
     */
    public void setAfter(String after) {
        this.after = after;
    }

    /**
     * @return The before
     */
    public Object getBefore() {
        return before;
    }

    /**
     * @param before The before
     */
    public void setBefore(Object before) {
        this.before = before;
    }

}
