package com.cs361.aff39;

import java.util.Iterator;

public class Main {

    public static void main(String argv[]) {
        ConcurrentLinkedList<String> list = new ConcurrentLinkedList<>();

        Thread t1 = new Thread(() -> {
            int i = 0;
            while (true) {
                list.addTail("a" + i);
                i++;
            }
        });

        Thread t2 = new Thread(() -> {
            int i = 0;
            while (true) {
                list.addTail("b" + i);
                i++;
            }
        });

        Thread t3 = new Thread(() -> {
            Iterator<String> itr = list.iterator();
            while (true) {
                if (itr.hasNext()) {
                    System.out.println(itr.next());
                }
            }
        });

        t1.start();
        t2.start();
        t3.start();

        try {
            t1.join();
            t2.join();
            t3.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
