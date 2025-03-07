package com.techsol.models;

import java.util.ArrayList;
import java.util.List;

public class Element {
    private String name;
    private String type;
    private String minOccurs;
    private String maxOccurs;
    private boolean hasParent;
    List<Element> children = new ArrayList<>();

    public Element(String name, String type, String minOccurs, String maxOccurs, boolean hasParent) {
        this.name = name;
        this.type = type;
        this.minOccurs = minOccurs;
        this.maxOccurs = maxOccurs;
        this.hasParent = hasParent;
    }

    public Element(String type, String minOccurs, String maxOccurs) {
        this.type = type;
        this.minOccurs = minOccurs;
        this.maxOccurs = maxOccurs;
    }

    public Element() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMinOccurs() {
        return minOccurs;
    }

    public void setMinOccurs(String minOccurs) {
        this.minOccurs = minOccurs;
    }

    public String getMaxOccurs() {
        return maxOccurs;
    }

    public void setMaxOccurs(String maxOccurs) {
        this.maxOccurs = maxOccurs;
    }

    public List<Element> getChildren() {
        return children;
    }

    public void setChildren(List<Element> children) {
        this.children = children;
    }

    public boolean isComplex() {
        return "complex".equals(type);
    }

    public void addChild(Element child) {
        children.add(child);
    }

    public boolean isHasParent() {
        return hasParent;
    }

    public void setHasParent(boolean hasParent) {
        this.hasParent = hasParent;
    }

    @Override
    public String toString() {
        return "Element [name=" + name + ", type=" + type + ", minOccurs=" + minOccurs + ", maxOccurs=" + maxOccurs
                + ", children=" + children + "]";
    }
}
