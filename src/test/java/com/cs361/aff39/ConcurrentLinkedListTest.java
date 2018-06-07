package com.cs361.aff39;

import org.junit.Test;

import java.util.Iterator;
import java.util.NoSuchElementException;

@SuppressWarnings("ALL")
public class ConcurrentLinkedListTest {

    @Test
    public void addFrontTest() {
        ConcurrentLinkedList list = new ConcurrentLinkedList<String>();
        list.addFront("test");

        assert list.size() == 1;
        assert list.iterator().hasNext();
        assert list.iterator().next().equals("test");
    }

    @Test
    public void addTailTest() {
        ConcurrentLinkedList list = new ConcurrentLinkedList<String>();
        list.addTail("test");

        assert list.size() == 1;
        assert list.iterator().hasNext();
        assert list.iterator().next().equals("test");
    }


    @Test(expected = NoSuchElementException.class)
    public void emptyIteratorTest() {
        ConcurrentLinkedList list = new ConcurrentLinkedList<String>();

        assert list.size() == 0;
        assert !list.iterator().hasNext();

        list.iterator().next();
    }


    @Test
    public void insertMultipleTest() {
        ConcurrentLinkedList list = new ConcurrentLinkedList<String>();

        for (int i = 0; i < 100; i++) {
            list.addTail("" + i);
        }

        assert list.size() == 100;

        Iterator<String> itr = list.iterator();
        for (int i = 0; i < 100; i++) {
            String s = itr.next();
            assert s.equals("" + i);
        }
    }

    @Test
    public void addFrontAndTailTest() {
        ConcurrentLinkedList list = new ConcurrentLinkedList<String>();
        list.addFront("B");
        list.addTail("C");
        list.addFront("A");
        list.addTail("D");


        assert list.size() == 4;

        Iterator<String> itr = list.iterator();
        assert itr.next().equals("A");
        assert itr.next().equals("B");
        assert itr.next().equals("C");
        assert itr.next().equals("D");
        assert !itr.hasNext();
    }

    @Test
    public void deleteTest() {
        ConcurrentLinkedList list = new ConcurrentLinkedList<String>();
        list.addFront("A");
        list.addTail("B");
        list.addTail("C");

        Iterator<String> itr = list.iterator();
        assert itr.next().equals("A");
        assert itr.next().equals("B");
        itr.remove();
        assert itr.next().equals("C");

        itr = list.iterator();
        assert itr.next().equals("A");
        assert itr.next().equals("C");
    }

    @Test
    public void containsTest() {
        ConcurrentLinkedList list = new ConcurrentLinkedList<String>();
        list.addFront("A");
        list.addTail("B");
        list.addTail("C");

        assert list.contains("B");
    }

    @Test
    public void insertTest() {
        ConcurrentLinkedList list = new ConcurrentLinkedList<String>();
        list.addFront("A");
        list.addTail("B");
        list.addTail("D");

        list.insert("B", "C");

        Iterator<String> itr = list.iterator();
        assert itr.next().equals("A");
        assert itr.next().equals("B");
        assert itr.next().equals("C");
        assert itr.next().equals("D");
    }

}
