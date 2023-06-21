package edu.psu.pjm6196.inventorymanager.barcodescanner.utils;

public interface TransformationHandler {
    float scale(float pixel);

    float translateX(float x);

    float translateY(float y);
}
