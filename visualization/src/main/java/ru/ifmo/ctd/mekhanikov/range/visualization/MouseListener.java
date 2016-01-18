package ru.ifmo.ctd.mekhanikov.range.visualization;

import ru.ifmo.ctd.mekhanikov.range.Point;
import ru.ifmo.ctd.mekhanikov.range.Rectangle;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

public abstract class MouseListener implements java.awt.event.MouseListener, MouseMotionListener {

    private Point lastPress;

    @Override
    public void mouseClicked(MouseEvent e) {
        Point p = new Point(e.getX(), e.getY());
        switch (e.getButton()) {
            case MouseEvent.BUTTON1:
                leftClick(p);
                break;
            case MouseEvent.BUTTON3:
                if (e.getClickCount() == 1) {
                    rightClick(p);
                } else if (e.getClickCount() == 2) {
                    rightDoubleClick();
                }
                break;
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        lastPress = new Point(e.getX(), e.getY());
    }

    @Override
    public void mouseReleased(MouseEvent e) {}
    @Override
    public void mouseEntered(MouseEvent e) {}
    @Override
    public void mouseExited(MouseEvent e) {}

    @Override
    public void mouseDragged(MouseEvent e) {
        Rectangle rect = createRectangle(lastPress, new Point(e.getX(), e.getY()));
        area(rect);
    }

    private Rectangle createRectangle(Point p1, Point p2) {
        Point bl = new Point(
                Math.min(p1.getX(), p2.getX()),
                Math.min(p1.getY(), p2.getY()));
        Point tr = new Point(
                Math.max(p1.getX(), p2.getX()),
                Math.max(p1.getY(), p2.getY()));
        return new Rectangle(bl, tr);
    }

    @Override
    public void mouseMoved(MouseEvent e) {}

    public abstract void leftClick(Point p);

    public abstract void rightClick(Point p);

    public abstract void rightDoubleClick();

    public abstract void area(Rectangle area);
}
