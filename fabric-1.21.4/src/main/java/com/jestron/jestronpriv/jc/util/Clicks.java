package com.jestron.jestronpriv.jc.util;

import java.util.ArrayDeque;

/** Son 1 saniyedeki sol / sağ tık sayısı (MouseHandler mixin'inden beslenir). */
public final class Clicks {
    private Clicks() {
    }

    private static final ArrayDeque<Long> LEFT = new ArrayDeque<>();
    private static final ArrayDeque<Long> RIGHT = new ArrayDeque<>();

    public static synchronized void press(int button) {
        long now = System.currentTimeMillis();
        if (button == 0) LEFT.addLast(now);
        else if (button == 1) RIGHT.addLast(now);
        trim(now);
    }

    private static void trim(long now) {
        while (!LEFT.isEmpty() && now - LEFT.peekFirst() > 1000) LEFT.pollFirst();
        while (!RIGHT.isEmpty() && now - RIGHT.peekFirst() > 1000) RIGHT.pollFirst();
        // aşırı uçta bellek sınırı
        while (LEFT.size() > 100) LEFT.pollFirst();
        while (RIGHT.size() > 100) RIGHT.pollFirst();
    }

    public static synchronized int left() {
        trim(System.currentTimeMillis());
        return LEFT.size();
    }

    public static synchronized int right() {
        trim(System.currentTimeMillis());
        return RIGHT.size();
    }
}
