package com.visualiser.service;

import com.visualiser.dto.Step;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SortingService {

    public List<Step> run(String algo, int[] arr) {
        int[] a = arr.clone();
        List<Step> steps = new ArrayList<>();
        switch (algo.toLowerCase()) {
            case "quick" -> quickSort(a, 0, a.length - 1, steps);
            case "merge" -> mergeSort(a, 0, a.length - 1, steps);
            case "heap" -> heapSort(a, steps);
            default -> bubbleSort(a, steps);
        }
        return steps;
    }

    private void bubbleSort(int[] a, List<Step> steps) {
        int n = a.length;
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                steps.add(Step.cmp(j, j + 1));
                if (a[j] > a[j + 1]) {
                    steps.add(Step.swap(j, j + 1));
                    int t = a[j];
                    a[j] = a[j + 1];
                    a[j + 1] = t;
                }
            }
        }
    }

    private void quickSort(int[] a, int low, int high, List<Step> steps) {
        if (low >= high) return;
        int p = partition(a, low, high, steps);
        quickSort(a, low, p - 1, steps);
        quickSort(a, p + 1, high, steps);
    }

    private int partition(int[] a, int low, int high, List<Step> steps) {
        int pivot = a[high];
        int i = low - 1;
        for (int j = low; j < high; j++) {
            steps.add(Step.cmp(j, high));
            if (a[j] < pivot) {
                i++;
                steps.add(Step.swap(i, j));
                int t = a[i];
                a[i] = a[j];
                a[j] = t;
            }
        }
        steps.add(Step.swap(i + 1, high));
        int t = a[i + 1];
        a[i + 1] = a[high];
        a[high] = t;
        return i + 1;
    }

    private void mergeSort(int[] a, int l, int r, List<Step> steps) {
        if (l >= r) return;
        int m = l + (r - l) / 2;
        mergeSort(a, l, m, steps);
        mergeSort(a, m + 1, r, steps);
        merge(a, l, m, r, steps);
    }

    private void merge(int[] a, int l, int m, int r, List<Step> steps) {
        int n1 = m - l + 1;
        int n2 = r - m;
        int[] L = new int[n1];
        int[] R = new int[n2];

        for (int i = 0; i < n1; i++) L[i] = a[l + i];
        for (int j = 0; j < n2; j++) R[j] = a[m + 1 + j];

        int i = 0, j = 0, k = l;
        while (i < n1 && j < n2) {
            steps.add(Step.cmp(l + i, m + 1 + j));
            if (L[i] <= R[j]) {
                steps.add(Step.set(k, L[i]));
                a[k++] = L[i++];
            } else {
                steps.add(Step.set(k, R[j]));
                a[k++] = R[j++];
            }
        }
        while (i < n1) {
            steps.add(Step.set(k, L[i]));
            a[k++] = L[i++];
        }
        while (j < n2) {
            steps.add(Step.set(k, R[j]));
            a[k++] = R[j++];
        }
    }

    private void heapSort(int[] a, List<Step> steps) {
        int n = a.length;
        for (int i = n / 2 - 1; i >= 0; i--) heapify(a, n, i, steps);
        for (int i = n - 1; i > 0; i--) {
            steps.add(Step.swap(0, i));
            int t = a[0];
            a[0] = a[i];
            a[i] = t;
            heapify(a, i, 0, steps);
        }
    }

    private void heapify(int[] a, int n, int i, List<Step> steps) {
        int largest = i;
        int l = 2 * i + 1;
        int r = 2 * i + 2;

        if (l < n) {
            steps.add(Step.cmp(l, largest));
            if (a[l] > a[largest]) largest = l;
        }
        if (r < n) {
            steps.add(Step.cmp(r, largest));
            if (a[r] > a[largest]) largest = r;
        }
        if (largest != i) {
            steps.add(Step.swap(i, largest));
            int t = a[i];
            a[i] = a[largest];
            a[largest] = t;
            heapify(a, n, largest, steps);
        }
    }
}
