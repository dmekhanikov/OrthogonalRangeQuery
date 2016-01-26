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
            if (dim > 1) {
                if (oldNode.key != Double.POSITIVE_INFINITY) {
                    oldNode.nextDimTree.add(p);
                }
            } else {
                oldNode.points.add(p);
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
        Point bl = query.getBottomLeft();
        Point tr = query.getTopRight();
        List<Node> xSubtrees = getSubtrees(bl.getX(), tr.getX());
        for (Node xRoot : xSubtrees) {
            List<Node> ySubtrees = xRoot.nextDimTree.getSubtrees(bl.getY(), tr.getY());
            for (Node yRoot : ySubtrees) {
                double maxKey = yRoot.getMax().key;
                for (Node yNode = yRoot.getMin(); yNode.key <= maxKey; yNode = yNode.getSucc()) {
                    result.addAll(yNode.points);
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
            if (copiedNode.key != Double.POSITIVE_INFINITY) {
                for (Node yNode = copiedNode.nextDimTree.root.getMin().getSucc();
                     yNode.key != Double.POSITIVE_INFINITY; yNode = yNode.getSucc()) {
                    for (Point copiedPoint : yNode.points) {
                        oldNode.nextDimTree.add(copiedPoint);
                    }
                }
            }
        } else {
            copiedNode.points = oldNode.points;
            oldNode.points = null;
            newNode.points = new ArrayList<>();
            newNode.points.add(p);
        }
    }

    private List<Node> getSubtrees(double a, double b) {
        Node pred = root.find(a).getPred();
        Node succ = root.find(b);
        if (succ.key == b) {
            succ = succ.getSucc();
        }
        Node left = pred.parent;
        Node right = succ.parent;
        int leftHeight = left.getHeight();
        int rightHeight = right.getHeight();
        List<Node> result = new ArrayList<>();
        while (leftHeight > rightHeight) {
            if (pred.key <= left.key) {
                result.add(left.right);
            }
            left = left.parent;
            leftHeight--;
        }
        while (leftHeight < rightHeight) {
            if (succ.key > right.key) {
                result.add(right.left);
            }
            right = right.parent;
            rightHeight--;
        }
        while (left != right) {
            if (pred.key <= left.key) {
                result.add(left.right);
            }
            if (succ.key > right.key) {
                result.add(right.left);
            }
            left = left.parent;
            right = right.parent;
        }
        return result;
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
