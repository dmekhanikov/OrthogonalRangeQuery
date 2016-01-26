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
        copiedNode.size = oldNode.size;
        if (oldNode.key < newNode.key) {
            validate(oldNode, copiedNode, newNode);
            oldNode.key = copiedNode.key;
        } else {
            validate(oldNode, newNode, copiedNode);
            oldNode.key = newNode.key;
        }
        validateSizes(oldNode);
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
        validate(parent, sibling.left, sibling.right);
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

    private void validate(Node parent, Node left, Node right) {
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
