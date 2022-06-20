package com.cdio.solitaire.imageanalysis;

import org.opencv.core.Point;

// ContourNode class containing an index, an area and a center Point
public class ContourNode {
    int index;
    double area;
    Point center;

    public ContourNode(int index, double area, Point center) {
        this.index = index;
        this.area = area;
        this.center = center;
    }
}
