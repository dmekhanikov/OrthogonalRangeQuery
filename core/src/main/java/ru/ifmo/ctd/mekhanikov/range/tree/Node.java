package ru.ifmo.ctd.mekhanikov.range.tree;

import ru.ifmo.ctd.mekhanikov.range.Point;

import java.util.List;

class Node {
    double key;
    Node parent;
    Node left;
    Node right;
    RangeTree nextDimTree;
    List<Point> points;
    int size = 1;

    Node(double key) {
        this.key = key;
    }

    boolean isLeaf() {
        return left == null && right == null;
    }

    // returns node that has a key >= value
    Node find(double value) {
        if (isLeaf()) {
            return this;
        } else if (value <= key) {
            return left.find(value);
        } else {
            return right.find(value);
        }
    }

    Node getPred() {
        Node node = parent;
        while (node != null && key <= node.key) {
            node = node.parent;
        }
        if (node == null || node.left == null) {
            return null;
        }
        node = node.left;
        return node.getMax();
    }

    Node getSucc() {
        Node node = parent;
        while (node != null && key > node.key) {
            node = node.parent;
        }
        if (node == null || node.right == null) {
            return null;
        }
        node = node.right;
        return node.getMin();
    }

    Node getMax() {
        Node node = this;
        while (!node.isLeaf()) {
            node = node.right;
        }
        return node;
    }

    Node getMin() {
        Node node = this;
        while (!node.isLeaf()) {
            node = node.left;
        }
        return node;
    }

    int getHeight() {
        int height = 0;
        for (Node node = this; node != null; node = node.parent) {
            height++;
        }
        return height;
    }
}
