package ru.ifmo.ctd.mekhanikov.range.tree;

import ru.ifmo.ctd.mekhanikov.range.Point;
import ru.ifmo.ctd.mekhanikov.range.RangeQuery;
import ru.ifmo.ctd.mekhanikov.range.Rectangle;

import java.util.ArrayList;
import java.util.List;

public class RangeTree implements RangeQuery {

    private static final double ALPHA = 0.1;

    private Node root;
    private int dim;

    public RangeTree() {
        this(2);
    }

    private RangeTree(int dim) {
        this.dim = dim;
        root = new Node(Double.NEGATIVE_INFINITY);
        addBorders();
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
                oldNode.nextDimTree.add(p);
            } else {
                oldNode.points.add(p);
            }
        }
        if (dim > 1) {
            addToParents(oldNode, p);
        }
        balance(oldNode);
    }

    @Override
    public void remove(Point p) {
        double key = getKey(p);
        Node node = root.find(key);
        if (node.key != key) {
            return;
        }
        if (dim == 1) {
            node.points.remove(p);
            if (node.points.isEmpty()) {
                remove(node);
            }
        } else {
            node.nextDimTree.remove(p);
            removeFromParents(node, p);
            if (node.nextDimTree.root.size == 3) {
                remove(node);
            }
        }
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
                result.addAll(getAll(yRoot));
            }
        }
        return result;
    }

    private void balance(Node node) {
        Node target = null;
        for (Node curNode = node; curNode != null; curNode = curNode.parent) {
            if (!curNode.isLeaf() && (
                    curNode.left.size * ALPHA > curNode.right.size || curNode.right.size * ALPHA > curNode.left.size)) {
                target = curNode;
            }
        }
        if (target != null) {
            rebuild(target);
            addBorders();
        }
    }

    private void rebuild(Node root) {
        List<Point> points = getAll(root);
        Node newRoot = build(points);
        validateRefs(root, newRoot.left, newRoot.right);
        root.size = root.left.size + root.right.size + 1;
        root.key = newRoot.key;
    }

    private Node build(List<Point> points) {
        RangeTree nextDimTree = null;
        if (dim > 1) {
            nextDimTree = new RangeTree(dim - 1);
            nextDimTree.root = root.nextDimTree.build(points);
            nextDimTree.addBorders();
        }
        points.sort((a, b) -> Double.compare(getKey(a), getKey(b)));
        int count = 0;
        Point prev = null;
        for (Point p : points) {
            if (prev == null || getKey(prev) != getKey(p)) {
                count++;
            }
            prev = p;
        }
        Node root;
        if (count == 1) {
            root = new Node(getKey(points.get(0)));
            if (dim > 1) {
                root.nextDimTree = nextDimTree;
            } else {
                root.points = new ArrayList<>();
                root.points.addAll(points);
            }
            return root;
        }
        int mid = count / 2;
        count = 0;
        prev = null;
        for (int i = 0; i < points.size(); i++) {
            Point p = points.get(i);
            if (prev == null || getKey(prev) != getKey(p)) {
                count++;
            }
            if (count == mid) {
                root = new Node(getKey(p));
                if (dim > 1) {
                    root.nextDimTree = nextDimTree;
                }
                while (getKey(points.get(i)) == getKey(p)) {
                    i++;
                }
                Node left = build(points.subList(0, i));
                Node right = build(points.subList(i, points.size()));
                validateRefs(root, left, right);
                root.key = left.getMax().key;
                root.size = left.size + right.size + 1;
                return root;
            }
            prev = p;
        }
        throw new AssertionError("Should not get here");
    }

    private void addBorders() {
        Node max = root.getMax();
        if (max.key != Double.POSITIVE_INFINITY) {
            Node newNode = new Node(Double.POSITIVE_INFINITY);
            Node copiedNode = copyNode(max);
            validateRefs(max, copiedNode, newNode);
            validateSizes(max);
        }
        Node min = root.getMin();
        if (min.key != Double.NEGATIVE_INFINITY) {
            Node newNode = new Node(Double.NEGATIVE_INFINITY);
            Node copiedNode = copyNode(min);
            validateRefs(min, newNode, copiedNode);
            validateSizes(min);
            min.key = Double.NEGATIVE_INFINITY;
        }
    }

    private List<Point> getAll(Node node) {
        while (node.nextDimTree != null) {
            node = node.nextDimTree.root;
        }
        List<Point> result = new ArrayList<>();
        double maxKey = node.getMax().key;
        for (Node curNode = node.getMin(); curNode != null && curNode.key <= maxKey; curNode = curNode.getSucc()) {
            if (curNode.points != null) {
                result.addAll(curNode.points);
            }
        }
        return result;
    }

    private void insert(Node oldNode, Point p) {
        double key = getKey(p);
        Node copiedNode = copyNode(oldNode);
        Node newNode = new Node(key);
        if (oldNode.key < newNode.key) {
            validateRefs(oldNode, copiedNode, newNode);
            oldNode.key = copiedNode.key;
        } else {
            validateRefs(oldNode, newNode, copiedNode);
            oldNode.key = newNode.key;
        }
        validateSizes(oldNode);
        if (dim > 1) {
            newNode.nextDimTree = new RangeTree(dim - 1);
            newNode.nextDimTree.add(p);
            oldNode.nextDimTree.add(p);
        } else {
            newNode.points = new ArrayList<>();
            newNode.points.add(p);
        }
    }

    private Node copyNode(Node oldNode) {
        Node copiedNode = new Node(oldNode.key);
        copiedNode.size = oldNode.size;
        if (dim > 1) {
            copiedNode.nextDimTree = oldNode.nextDimTree;
            oldNode.nextDimTree = new RangeTree(dim - 1);
            if (copiedNode.nextDimTree != null) {
                for (Point p : getAll(copiedNode.nextDimTree.root)) {
                    oldNode.nextDimTree.add(p);
                }
            }
        } else {
            copiedNode.points = oldNode.points;
            oldNode.points = null;
        }
        return copiedNode;
    }

    private void remove(Node node) {
        Node parent = node.parent;
        Node sibling;
        if (node.key <= parent.key) {
            sibling = parent.right;
        } else {
            sibling = parent.left;
        }
        double newKey = node.getPred().key;
        parent.key = sibling.key;
        parent.nextDimTree = sibling.nextDimTree;
        validateRefs(parent, sibling.left, sibling.right);
        parent.size = sibling.size;
        parent.points = sibling.points;
        validateSizes(parent);
        validateKeys(parent.parent, node.key, newKey);
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

    private void addToParents(Node node, Point p) {
        for (Node curNode = node.parent; curNode != null; curNode = curNode.parent) {
            curNode.nextDimTree.add(p);
        }
    }

    private void removeFromParents(Node node, Point p) {
        for (Node curNode = node.parent; curNode != null; curNode = curNode.parent) {
            curNode.nextDimTree.remove(p);
        }
    }

    private void validateSizes(Node node) {
        while (node != null) {
            if (!node.isLeaf()) {
                node.size = node.left.size + node.right.size + 1;
            }
            node = node.parent;
        }
    }

    private void validateKeys(Node node, double oldKey, double newKey) {
        while (node != null) {
            if (node.key == oldKey) {
                node.key = newKey;
            }
            node = node.parent;
        }
    }

    private double getKey(Point p) {
        if (dim == 2) {
            return p.getX();
        } else {
            return p.getY();
        }
    }

    private void validateRefs(Node parent, Node left, Node right) {
        if (left != null) {
            left.parent = parent;
        }
        if (right != null) {
            right.parent = parent;
        }
        if (parent != null) {
            parent.left = left;
            parent.right = right;
        }
    }
}
