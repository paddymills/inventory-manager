package edu.psu.pjm6196.inventorymanager.utils;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;

import com.google.mlkit.vision.barcode.common.Barcode;

import java.util.Objects;

public class Quadrilateral {
    private final Line top;
    private final Line bottom;
    private final Line left;
    private final Line right;

    public Quadrilateral(Barcode barcode) {
        Point[] pts = Objects.requireNonNull(barcode.getCornerPoints());

        top = new Line(pts[0], pts[1]);
        bottom = new Line(pts[3], pts[2]);
        left = new Line(pts[3], pts[0]);
        right = new Line(pts[2], pts[1]);
    }

    public Point getAnchor() {
        return top.start;
    }

    public void draw(Canvas canvas, Paint paint) {
        top.draw(canvas, paint);
        bottom.draw(canvas, paint);
        left.draw(canvas, paint);
        right.draw(canvas, paint);
    }

    public void drawTopText(Canvas canvas, Paint paint, String text) {
        Path path = new Path();
        path.moveTo(top.start.x, top.start.y);
        path.lineTo(top.end.x, top.end.y);

        canvas.drawTextOnPath(text, path, 0, 0, paint);
    }

    public boolean isPointWithin(Point point) {
        return
            top.isAboveLine(point) &&
                bottom.isBellowLine(point) &&
                left.isLeftOfLine(point) &&
                right.isRigthOfLine(point);
    }

    public void translate(TranslateListener listener) {
        top.translate(listener);
        bottom.translate(listener);
        left.translate(listener);
        right.translate(listener);
    }

    public interface TranslateListener {
        Point translatePoint(Point point);
    }
}
