package ru.ifmo.ctd.mekhanikov.range;

import java.util.List;

public interface RangeQuery {
    void add(Point p);
    void remove(Point p);
    List<Point> get(Rectangle query);
}
