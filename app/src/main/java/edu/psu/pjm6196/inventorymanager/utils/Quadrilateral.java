package edu.psu.pjm6196.inventorymanager.utils;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;

import com.google.mlkit.vision.barcode.common.Barcode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class Quadrilateral {
    private Point topLeft;
    private Point topRight;
    private Point bottomLeft;
    private Point bottomRight;

    public Quadrilateral(Point[] points) {
        assert points.length == 4;

        ArrayList<Point> pts = new ArrayList<>(Arrays.asList(points));
        pts.sort((a, b) -> {
            // sort by Y-values and then X-values

            // comparator returns
            //  -1 -> <
            //   0 -> ==
            //   1 -> >
            if (a.y == b.y)
                return a.x - b.x;

            return a.y - b.y;
        });

        setPoints(pts.get(0), pts.get(1), pts.get(2), pts.get(3));
    }

    public Quadrilateral(Barcode barcode) {
        this(Objects.requireNonNull(barcode.getCornerPoints()));
    }

    public void setPoints(Point tl, Point tr, Point bl, Point br) {
        topLeft = tl;
        topRight = tr;
        bottomLeft = bl;
        bottomRight = br;
    }

    public void draw(Canvas canvas, Paint paint) {
        canvas.drawLine(topLeft.x, topLeft.y, topRight.x, topRight.y, paint);
        canvas.drawLine(topRight.x, topRight.y, bottomRight.x, bottomRight.y, paint);
        canvas.drawLine(bottomRight.x, bottomRight.y, bottomLeft.x, bottomLeft.y, paint);
        canvas.drawLine(bottomLeft.x, bottomLeft.y, topLeft.x, topLeft.y, paint);
    }

    public void drawTopText(Canvas canvas, Paint paint, String text) {
        Path path = new Path();
        path.moveTo(topLeft.x, topLeft.y);
        path.lineTo(topRight.x, topRight.y);

        canvas.drawTextOnPath(text, path, 0, 0, paint);
    }

    public void translate(TranslateListener listener) {
        topLeft = listener.translatePoint(topLeft);
        topRight = listener.translatePoint(topRight);
        bottomLeft = listener.translatePoint(bottomLeft);
        bottomRight = listener.translatePoint(bottomRight);
    }

    public interface TranslateListener {
        Point translatePoint(Point point);
    }
}
