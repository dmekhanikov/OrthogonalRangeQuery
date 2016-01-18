package ru.ifmo.ctd.mekhanikov.range;

public class Rectangle {
    private Point bottomLeft;
    private Point topRight;

    public Rectangle(Point bottomLeft, Point topRight) {
        this.bottomLeft = bottomLeft;
        this.topRight = topRight;
    }

    public Point getBottomLeft() {
        return bottomLeft;
    }

    public Point getTopRight() {
        return topRight;
    }

    public double getWidth() {
        return topRight.getX() - bottomLeft.getX();
    }

    public double getHeight() {
        return topRight.getY() - bottomLeft.getY();
    }

    public boolean contains(Point p) {
        return p.getX() >= bottomLeft.getX() && p.getX() <= topRight.getX() &&
                p.getY() >= bottomLeft.getY() && p.getY() <= topRight.getY();
    }
}
