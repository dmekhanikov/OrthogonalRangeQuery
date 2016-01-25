package ru.ifmo.ctd.mekhanikov.range.tree;

import ru.ifmo.ctd.mekhanikov.range.Point;
import ru.ifmo.ctd.mekhanikov.range.RangeQuery;
import ru.ifmo.ctd.mekhanikov.range.Rectangle;

import java.util.ArrayList;
import java.util.List;

public class RangeTree implements RangeQuery {

    private Node root;
    private int dim;

    public RangeTree() {
        this(2);
    }

    private RangeTree(int dim) {
        this.dim = dim;
        root = new Node(Double.NEGATIVE_INFINITY);
        root.left = new Node(Double.NEGATIVE_INFINITY);
        root.right = new Node(Double.POSITIVE_INFINITY);
        root.left.parent = root;
        root.right.parent = root;
        if (dim != 1) {
            root.nextDimTree = new RangeTree(dim - 1);
        }
    }

    @Override
    public void add(Point p) {
        double key = getKey(p);
        Node oldNode = root.find(key);
        if (oldNode.key != key) {
            insert(oldNode, p);
        } else {
            if (dim > 1 && oldNode.key != Double.POSITIVE_INFINITY ) {
                oldNode.nextDimTree.add(p);
            }
        }
        if (dim > 1) {
            addToParents(oldNode, p);
        }
    }

    private void addToParents(Node node, Point p) {
        for (Node curNode = node.parent; curNode != null; curNode = curNode.parent) {
            curNode.nextDimTree.add(p);
        }
    }

    @Override
    public void remove(Point p) {
    }

    @Override
    public List<Point> get(Rectangle query) {
        List<Point> result = new ArrayList<>();
        for (Node xNode = root.getMin().getSucc(); xNode.key != Double.POSITIVE_INFINITY; xNode = xNode.getSucc()) {
            for (Node yNode = xNode.nextDimTree.root.getMin().getSucc();
                    yNode.key != Double.POSITIVE_INFINITY; yNode = yNode.getSucc()) {
                Point point = new Point(xNode.key, yNode.key);
                if (query.contains(point)) {
                    result.add(point);
                }
            }
        }
        return result;
    }

    private void insert(Node oldNode, Point p) {
        double key = getKey(p);
        Node copiedNode = new Node(oldNode.key);
        Node newNode = new Node(key);
        if (oldNode.key < newNode.key) {
            validate(oldNode, copiedNode, newNode);
        } else {
            validate(oldNode, newNode, copiedNode);
        }
        if (dim > 1) {
            newNode.nextDimTree = new RangeTree(dim - 1);
            newNode.nextDimTree.add(p);
            copiedNode.nextDimTree = oldNode.nextDimTree;
            oldNode.nextDimTree = new RangeTree(dim - 1);
            oldNode.nextDimTree.add(p);
            for (Node yNode = oldNode.nextDimTree.root.getMin().getSucc();
                 yNode.key != Double.POSITIVE_INFINITY; yNode = yNode.getSucc()) {
                oldNode.nextDimTree.add(new Point(oldNode.key, yNode.key));
            }
        }
    }

    private double getKey(Point p) {
        if (dim == 2) {
            return p.getX();
        } else {
            return p.getY();
        }
    }

    private void validate(Node parent, Node left, Node right) {
        left.parent = parent;
        right.parent = parent;
        parent.left = left;
        parent.right = right;
        parent.key = left.key;
    }
}
