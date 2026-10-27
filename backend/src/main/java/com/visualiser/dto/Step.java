package com.visualiser.dto;

import java.util.List;

public record Step(String type, int x, int y, int i, int j, int val, int parent) {
    public static Step visit(int r, int c) { return new Step("visit", r, c, -1, -1, -1, -1); }
    public static Step path(int r, int c) { return new Step("path", r, c, -1, -1, -1, -1); }
    public static Step cmp(int i, int j) { return new Step("cmp", -1, -1, i, j, -1, -1); }
    public static Step swap(int i, int j) { return new Step("swap", -1, -1, i, j, -1, -1); }
    public static Step set(int i, int val) { return new Step("set", -1, -1, i, -1, val, -1); }
    public static Step bstVisit(int val) { return new Step("bst_visit", -1, -1, -1, -1, val, -1); }
    public static Step bstAdd(int val, int parent) { return new Step("bst_add", -1, -1, -1, -1, val, parent); }
    public static Step bstDel(int val) { return new Step("bst_del", -1, -1, -1, -1, val, -1); }
}
