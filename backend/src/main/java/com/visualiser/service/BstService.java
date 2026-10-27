package com.visualiser.service;

import com.visualiser.dto.Step;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BstService {

    public static class Node {
        public int val;
        public Node left, right;
        public Node(int val) { this.val = val; }
    }

    public List<Step> insert(Node root, int val) {
        List<Step> steps = new ArrayList<>();
        if (root == null) {
            steps.add(Step.bstAdd(val, -1));
            return steps;
        }

        Node curr = root;
        Node parent = null;
        while (curr != null) {
            steps.add(Step.bstVisit(curr.val));
            parent = curr;
            if (val < curr.val) curr = curr.left;
            else if (val > curr.val) curr = curr.right;
            else return steps;
        }

        steps.add(Step.bstAdd(val, parent.val));
        return steps;
    }

    public List<Step> traverse(Node root, String order) {
        List<Step> steps = new ArrayList<>();
        switch (order.toLowerCase()) {
            case "preorder" -> pre(root, steps);
            case "postorder" -> post(root, steps);
            default -> in(root, steps);
        }
        return steps;
    }

    private void in(Node n, List<Step> steps) {
        if (n == null) return;
        in(n.left, steps);
        steps.add(Step.bstVisit(n.val));
        in(n.right, steps);
    }

    private void pre(Node n, List<Step> steps) {
        if (n == null) return;
        steps.add(Step.bstVisit(n.val));
        pre(n.left, steps);
        pre(n.right, steps);
    }

    private void post(Node n, List<Step> steps) {
        if (n == null) return;
        post(n.left, steps);
        post(n.right, steps);
        steps.add(Step.bstVisit(n.val));
    }
}
