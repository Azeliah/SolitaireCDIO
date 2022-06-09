package com.cdio.solitaire.imageanalysis;

import org.opencv.core.Mat;
import org.opencv.core.Point;

// ContentNode class containing a Mat content image and a position Point
public class ContentNode {
    Mat content;
    Point position;

    public ContentNode(Mat content, Point position) {
        this.content = content;
        this.position = position;
    }
}
