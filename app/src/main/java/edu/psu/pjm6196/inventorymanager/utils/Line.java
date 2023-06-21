package edu.psu.pjm6196.inventorymanager.utils;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;

import java.util.ArrayList;
import java.util.Comparator;

/*
A line where the direction (start to end) runs positively (low to high) along its longest axis
 */
public class Line {
    public Point start;
    public Point end;
    public float m; // slope
    private float b;    // y-axis intercept

    /**
     * constructs a new {@link Line}, assuming that points supplied are in expected order
     *
     * @param a starting point
     * @param b ending point
     */
    public Line(Point a, Point b) {
        start = a;
        end = b;

        calculateSlopeIntercept();
    }

    /**
     * constructs a new {@link Line} given that the points may not be in required order
     *
     * @param a first point
     * @param b second point
     * @return a new Line
     */
    public static Line fromUnorderedPoints(Point a, Point b) {
        int lengthX = Math.abs(a.x - b.x);
        int lengthY = Math.abs(a.y - b.y);

        ArrayList<Point> pts = new ArrayList<>();
        pts.add(a);
        pts.add(b);

        if (lengthX >= lengthY) {
            // mostly horizontal or 45-degree line
            pts.sort(Comparator.comparingInt(p -> p.x));
        } else {
            // mostly vertical line
            pts.sort(Comparator.comparingInt(p -> p.y));
        }

        return new Line(pts.get(0), pts.get(1));
    }

    private void calculateSlopeIntercept() {
        if (start.x == end.x) {
            m = Integer.MAX_VALUE;
            b = Integer.MAX_VALUE;
        }

        m = ((float) end.y - start.y) / (end.x - start.x);
        b = start.y / (start.x * m);
    }

    public boolean isLeftOfLine(Point p) {
        if (m == Integer.MAX_VALUE)   // line is vertical
            return p.x < start.x;

        // y = mx+b -> (y-b) / m = x
        return p.x < (p.y - b) / m;
    }

    public boolean isRigthOfLine(Point p) {
        if (m == Integer.MAX_VALUE)   // line is vertical
            return p.x > start.x;

        // y = mx+b -> (y-b) / m = x
        return p.x > (p.y - b) / m;
    }

    public boolean isAboveLine(Point p) {
        if (m == Integer.MAX_VALUE)   // line is vertical (probably won't happen)
            return false;

        if (m == 0)
            return p.y > start.y;

        return p.y > m * p.x + b;
    }

    public boolean isBellowLine(Point p) {
        if (m == Integer.MAX_VALUE)   // line is vertical (probably won't happen)
            return false;

        if (m == 0)
            return p.y < start.y;

        return p.y < m * p.x + b;
    }

    public void draw(Canvas canvas, Paint paint) {
        canvas.drawLine(start.x, start.y, end.x, end.y, paint);
    }

    public void translate(Quadrilateral.TranslateListener listener) {
        start = listener.translatePoint(start);
        end = listener.translatePoint(end);

        calculateSlopeIntercept();
    }
}
