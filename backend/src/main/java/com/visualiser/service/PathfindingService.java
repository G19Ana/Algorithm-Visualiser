package com.visualiser.service;

import com.visualiser.dto.Step;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PathfindingService {

    private static final int[][] DIRS = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

    public List<Step> run(String algo, int[][] grid, int sr, int sc, int er, int ec) {
        return switch (algo.toLowerCase()) {
            case "astar" -> astar(grid, sr, sc, er, ec);
            case "dfs" -> dfs(grid, sr, sc, er, ec);
            case "bfs" -> bfs(grid, sr, sc, er, ec);
            default -> dijkstra(grid, sr, sc, er, ec);
        };
    }

    private List<Step> dijkstra(int[][] grid, int sr, int sc, int er, int ec) {
        int rows = grid.length;
        int cols = grid[0].length;
        int[][] dist = new int[rows][cols];
        int[][][] parent = new int[rows][cols][2];
        for (int[] r : dist) Arrays.fill(r, Integer.MAX_VALUE);

        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a[0]));
        dist[sr][sc] = 0;
        pq.add(new int[]{0, sr, sc});

        List<Step> steps = new ArrayList<>();
        boolean found = false;

        while (!pq.isEmpty()) {
            int[] curr = pq.poll();
            int d = curr[0], r = curr[1], c = curr[2];
            if (d > dist[r][c]) continue;
            steps.add(Step.visit(r, c));

            if (r == er && c == ec) {
                found = true;
                break;
            }

            for (int[] dir : DIRS) {
                int nr = r + dir[0], nc = c + dir[1];
                if (nr < 0 || nr >= rows || nc < 0 || nc >= cols || grid[nr][nc] == 1) continue;
                if (dist[r][c] + 1 < dist[nr][nc]) {
                    dist[nr][nc] = dist[r][c] + 1;
                    parent[nr][nc] = new int[]{r, c};
                    pq.add(new int[]{dist[nr][nc], nr, nc});
                }
            }
        }

        if (found) backtrack(parent, sr, sc, er, ec, steps);
        return steps;
    }

    private List<Step> astar(int[][] grid, int sr, int sc, int er, int ec) {
        int rows = grid.length;
        int cols = grid[0].length;
        int[][] gScore = new int[rows][cols];
        int[][][] parent = new int[rows][cols][2];
        for (int[] r : gScore) Arrays.fill(r, Integer.MAX_VALUE);

        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a[0]));
        gScore[sr][sc] = 0;
        pq.add(new int[]{h(sr, sc, er, ec), sr, sc});

        List<Step> steps = new ArrayList<>();
        boolean found = false;

        while (!pq.isEmpty()) {
            int[] curr = pq.poll();
            int r = curr[1], c = curr[2];
            steps.add(Step.visit(r, c));

            if (r == er && c == ec) {
                found = true;
                break;
            }

            for (int[] dir : DIRS) {
                int nr = r + dir[0], nc = c + dir[1];
                if (nr < 0 || nr >= rows || nc < 0 || nc >= cols || grid[nr][nc] == 1) continue;
                int tentG = gScore[r][c] + 1;
                if (tentG < gScore[nr][nc]) {
                    gScore[nr][nc] = tentG;
                    parent[nr][nc] = new int[]{r, c};
                    pq.add(new int[]{tentG + h(nr, nc, er, ec), nr, nc});
                }
            }
        }

        if (found) backtrack(parent, sr, sc, er, ec, steps);
        return steps;
    }

    private List<Step> bfs(int[][] grid, int sr, int sc, int er, int ec) {
        int rows = grid.length;
        int cols = grid[0].length;
        boolean[][] visited = new boolean[rows][cols];
        int[][][] parent = new int[rows][cols][2];
        Queue<int[]> q = new LinkedList<>();

        visited[sr][sc] = true;
        q.add(new int[]{sr, sc});
        List<Step> steps = new ArrayList<>();
        boolean found = false;

        while (!q.isEmpty()) {
            int[] curr = q.poll();
            int r = curr[0], c = curr[1];
            steps.add(Step.visit(r, c));

            if (r == er && c == ec) {
                found = true;
                break;
            }

            for (int[] dir : DIRS) {
                int nr = r + dir[0], nc = c + dir[1];
                if (nr < 0 || nr >= rows || nc < 0 || nc >= cols || grid[nr][nc] == 1 || visited[nr][nc]) continue;
                visited[nr][nc] = true;
                parent[nr][nc] = new int[]{r, c};
                q.add(new int[]{nr, nc});
            }
        }

        if (found) backtrack(parent, sr, sc, er, ec, steps);
        return steps;
    }

    private List<Step> dfs(int[][] grid, int sr, int sc, int er, int ec) {
        int rows = grid.length;
        int cols = grid[0].length;
        boolean[][] visited = new boolean[rows][cols];
        int[][][] parent = new int[rows][cols][2];
        Stack<int[]> stack = new Stack<>();

        stack.push(new int[]{sr, sc});
        List<Step> steps = new ArrayList<>();
        boolean found = false;

        while (!stack.isEmpty()) {
            int[] curr = stack.pop();
            int r = curr[0], c = curr[1];
            if (visited[r][c]) continue;
            visited[r][c] = true;
            steps.add(Step.visit(r, c));

            if (r == er && c == ec) {
                found = true;
                break;
            }

            for (int[] dir : DIRS) {
                int nr = r + dir[0], nc = c + dir[1];
                if (nr < 0 || nr >= rows || nc < 0 || nc >= cols || grid[nr][nc] == 1 || visited[nr][nc]) continue;
                parent[nr][nc] = new int[]{r, c};
                stack.push(new int[]{nr, nc});
            }
        }

        if (found) backtrack(parent, sr, sc, er, ec, steps);
        return steps;
    }

    private int h(int r1, int c1, int r2, int c2) {
        return Math.abs(r1 - r2) + Math.abs(c1 - c2);
    }

    private void backtrack(int[][][] parent, int sr, int sc, int er, int ec, List<Step> steps) {
        int cr = er, cc = ec;
        List<Step> path = new ArrayList<>();
        while (cr != sr || cc != sc) {
            path.add(Step.path(cr, cc));
            int[] p = parent[cr][cc];
            cr = p[0];
            cc = p[1];
        }
        path.add(Step.path(sr, sc));
        Collections.reverse(path);
        steps.addAll(path);
    }
}
