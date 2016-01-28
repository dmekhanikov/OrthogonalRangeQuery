package ru.ifmo.ctd.mekhanikov.range;

import org.junit.Assert;
import org.junit.Test;
import ru.ifmo.ctd.mekhanikov.range.tree.RangeTree;

import java.util.*;
import java.util.stream.Collectors;

public class RangeQueryTest extends Assert {

    private static void test(List<Point> points, List<Rectangle> queries) {
        RangeQuery rangeQuery = new RangeTree();
        points.forEach(rangeQuery::add);
        executeQueries(rangeQuery, points, queries);
        List<Point> removed = points.subList(points.size() / 2, points.size());
        removed.forEach(rangeQuery::remove);
        executeQueries(rangeQuery, points.subList(0, points.size() / 2), queries);
        removed.forEach(rangeQuery::add);
        executeQueries(rangeQuery, points, queries);
    }

    private static void executeQueries(RangeQuery rangeQuery, List<Point> points, List<Rectangle> queries) {
        for (Rectangle query : queries) {
            assertEquals(getCount(points, query), rangeQuery.getCount(query));
            Set<Point> expected = getResult(points, query);
            Set<Point> actual = rangeQuery.get(query).stream().collect(Collectors.toSet());
            assertEquals(expected, actual);
        }
    }

    private static Set<Point> getResult(List<Point> points, Rectangle query) {
        return points.stream().filter(query::contains).collect(Collectors.toSet());
    }

    private static int getCount(List<Point> points, Rectangle query) {
        return (int) points.stream().filter(query::contains).count();
    }

    @Test
    public void horizontalLine() {
        double y = 39.0;
        List<Point> points = Arrays.asList(
                new Point(-42.0, y),
                new Point(-37.0, y),
                new Point(-15.0, y),
                new Point(0.0, y),
                new Point(0.1, y),
                new Point(0.5, y),
                new Point(5.0, y),
                new Point(100000.0, y),
                new Point(100020.0, y)
        );
        List<Rectangle> queries = Arrays.asList(
                new Rectangle(new Point(-42.0, y), new Point(100020.0, y)),
                new Rectangle(new Point(-42.0, y - 1), new Point(100020.0, y + 1)),
                new Rectangle(new Point(-42.0, y + 1), new Point(100020.0, y + 2)),
                new Rectangle(new Point(0, -100), new Point(200000.0, 100)),
                new Rectangle(new Point(-1000.0, -100), new Point(0.1, 100)),
                new Rectangle(new Point(-17, -100), new Point(20, 100))
        );

        test(points, queries);
    }

    @Test
    public void verticalLine() {
        double x = 39.0;
        List<Point> points = Arrays.asList(
                new Point(x, -42.0),
                new Point(x, -37.0),
                new Point(x, -15.0),
                new Point(x, 0.0),
                new Point(x, 0.1),
                new Point(x, 0.5),
                new Point(x, 5.0),
                new Point(x, 100000.0),
                new Point(x, 100020.0)
        );
        List<Rectangle> queries = Arrays.asList(
                new Rectangle(new Point(x, -42.0), new Point(x, 100020.0)),
                new Rectangle(new Point(x - 1, -42.0), new Point(x + 1, 100020.0)),
                new Rectangle(new Point(x + 1, -42.0), new Point(x + 2, 100020.0)),
                new Rectangle(new Point(-100, 0), new Point(100, 200000.0)),
                new Rectangle(new Point(-100, -1000.0), new Point(100, 0.1)),
                new Rectangle(new Point(-100, -17), new Point(100, 20))
        );

        test(points, queries);
    }

    @Test
    public void random() {
        int pointsCount = 10000;
        int queriesCount = 1000;
        Random random = new Random(System.currentTimeMillis());
        List<Point> points = new ArrayList<>();
        for (int i = 0; i < pointsCount; i++) {
            points.add(new Point(random.nextDouble(), random.nextDouble()));
        }

        List<Rectangle> queries = new ArrayList<>();
        for (int i = 0; i < queriesCount; i++) {
            Point bl = new Point(random.nextDouble(), random.nextDouble());
            Point tr = new Point(bl.getX() + random.nextDouble(), bl.getY() + random.nextDouble());
            queries.add(new Rectangle(bl, tr));
        }

        test(points, queries);
    }
}
