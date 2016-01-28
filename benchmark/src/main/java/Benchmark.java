import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.RefineryUtilities;
import ru.ifmo.ctd.mekhanikov.range.NaiveRangeQuery;
import ru.ifmo.ctd.mekhanikov.range.Point;
import ru.ifmo.ctd.mekhanikov.range.RangeQuery;
import ru.ifmo.ctd.mekhanikov.range.Rectangle;
import ru.ifmo.ctd.mekhanikov.range.tree.RangeTree;

import java.util.*;

public class Benchmark {

    private long getExecutionTime(Runnable runnable) {
        long startTime = System.currentTimeMillis();
        runnable.run();
        return System.currentTimeMillis() - startTime;
    }

    private void runQueries(RangeQuery rangeQuery, List<Rectangle> queries) {
        queries.forEach(rangeQuery::getCount);
    }

    private List<Point> generatePoints(int count) {
        Random random = new Random(System.currentTimeMillis());
        List<Point> points = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            points.add(new Point(random.nextDouble(), random.nextDouble()));
        }
        return points;
    }

    private void warmUp() {
        System.out.println("Warming up...");
        List<Point> points = generatePoints(100000);
        List<Rectangle> queries = generateQueries(10000);
        RangeQuery rangeQuery = new RangeTree();
        points.forEach(rangeQuery::add);
        queries.forEach(rangeQuery::getCount);
    }

    private Map<Integer, Long> benchmark(String name, RangeQuery rangeQuery, int start, int end, int step) {
        warmUp();
        System.out.println(name + ":");
        Map<Integer, Long> result = new TreeMap<>();
        List<Rectangle> queries = generateQueries(10000);
        generatePoints(start - step).forEach(rangeQuery::add);
        for (int pointsCount = start; pointsCount <= end; pointsCount += step) {
            generatePoints(step).forEach(rangeQuery::add);
            System.gc();
            long executionTime = getExecutionTime(() -> runQueries(rangeQuery, queries));
            System.out.println(pointsCount + ": " + executionTime);
            result.put(pointsCount, executionTime);
        }
        return result;
    }

    private List<Rectangle> generateQueries(int count) {
        Random random = new Random(System.currentTimeMillis());
        List<Rectangle> queries = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Point bl = new Point(random.nextDouble(), random.nextDouble());
            Point tr = new Point(bl.getX() + random.nextDouble(), bl.getY() + random.nextDouble());
            queries.add(new Rectangle(bl, tr));
        }
        return queries;
    }

    private void addPoints(Map<Integer, Long> data, DefaultCategoryDataset dataset, String name) {
        for (Map.Entry<Integer, Long> entry : data.entrySet()) {
            dataset.addValue(entry.getValue(), name, entry.getKey());
        }
    }

    private void doMain() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        String rangeTreeName = "Range tree";
        Map<Integer, Long> rangeTreeTime = benchmark(rangeTreeName, new RangeTree(), 5000, 100000, 5000);
        addPoints(rangeTreeTime, dataset, rangeTreeName);

        String naiveMethodName = "Naive range query";
        Map<Integer, Long> naiveMethodTime = benchmark(naiveMethodName, new NaiveRangeQuery(), 5000, 100000, 5000);
        addPoints(naiveMethodTime, dataset, naiveMethodName);

        ChartWindow chartWindow = new ChartWindow(dataset, "Query time", "Query time");
        chartWindow.pack();
        RefineryUtilities.centerFrameOnScreen(chartWindow);
        chartWindow.setVisible(true);
    }

    public static void main(String... args) {
        new Benchmark().doMain();
    }
}