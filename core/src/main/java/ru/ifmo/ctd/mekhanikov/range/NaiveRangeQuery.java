package ru.ifmo.ctd.mekhanikov.range;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class NaiveRangeQuery implements RangeQuery {
    private List<Point> points = new ArrayList<>();

    public void add(Point p) {
        points.add(p);
    }

    public void remove(Point p) {
        points.remove(p);
    }

    public List<Point> get(Rectangle query) {
        return points.stream().filter(query::contains).collect(Collectors.toList());
    }

    @Override
    public int getCount(Rectangle query) {
        return (int) points.stream().filter(query::contains).count();
    }
}
