package ru.ifmo.ctd.mekhanikov.range.visualization;

import ru.ifmo.ctd.mekhanikov.range.Point;
import ru.ifmo.ctd.mekhanikov.range.RangeQuery;
import ru.ifmo.ctd.mekhanikov.range.Rectangle;
import ru.ifmo.ctd.mekhanikov.range.tree.RangeTree;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main extends Frame {

    private List<Point> points = new ArrayList<>();
    private Rectangle rectangle;
    private RangeQuery rangeQuery = new RangeTree();

    public Main() {
        super("Range Query Visualization");
        setSize(800, 600);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });
        MouseListener mouseListener = new MouseListener() {
            @Override
            public void leftClick(Point p) {
                addPoint(p);
                repaint();
            }

            @Override
            public void rightClick(Point p) {
                removePoint(p);
                repaint();
            }

            @Override
            public void rightDoubleClick() {
                rangeQuery = new RangeTree();
                points.clear();
                rectangle = null;
                repaint();
            }

            @Override
            public void area(Rectangle area) {
                rectangle = area;
                repaint();
            }
        };
        addMouseListener(mouseListener);
        addMouseMotionListener(mouseListener);
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        Set<Point> marked = new HashSet<>();
        if (rectangle != null) {
            g2.drawRect((int) rectangle.getBottomLeft().getX(), (int) rectangle.getBottomLeft().getY(),
                    (int) rectangle.getWidth(), (int) rectangle.getHeight());
            marked.addAll(rangeQuery.get(rectangle));
        }
        for (Point p : points) {
            if (marked.contains(p)) {
                g2.setColor(Color.RED);
            } else {
                g2.setColor(Color.BLACK);
            }
            drawPoint(g2, p);
        }
    }

    private void addPoint(Point p) {
        points.add(p);
        rangeQuery.add(p);
    }

    private void removePoint(Point p) {
        double minDist = Double.POSITIVE_INFINITY;
        Point argMin = null;
        for (Point point : points) {
            double dist = point.distTo(p);
            if (dist < minDist) {
                minDist = dist;
                argMin = point;
            }
        }
        if (minDist < 10) {
            points.remove(argMin);
            rangeQuery.remove(argMin);
        }
    }

    private void drawPoint(Graphics2D g2, Point p) {
        g2.fillOval((int) p.getX(), (int) p.getY(), 6, 6);
    }

    public static void main(String... args) {
        Main main = new Main();
        main.setVisible(true);
    }
}
