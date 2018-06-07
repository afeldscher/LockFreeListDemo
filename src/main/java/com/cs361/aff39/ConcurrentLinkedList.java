package com.cs361.aff39;


import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Adam Feldscher
 * aff39
 * CS-361 Honors Project
 * Concurrent Linked List Implementation, loosely based on the Java ConcurrentLinkedQueue Class
 * Lock-Free Linked List
 * Java 8
 */
public class ConcurrentLinkedList<E> {

    /*
     * There is no possibility of an ABA problem because all nodes are being garbage collected.
     * This means that it is impossible to get a repeated pointer
     */

    private volatile Node<E> head;
    private volatile Node<E> tail;


    // Setup Unsafe Properties for List Class. Needed for CAS
    private static final Unsafe UNSAFE;
    private static final long headOffset;
    private static final long tailOffset;

    static {
        try {
            UNSAFE = getUnsafe();
            Class k = ConcurrentLinkedList.class;
            headOffset = UNSAFE.objectFieldOffset(k.getDeclaredField("head"));
            tailOffset = UNSAFE.objectFieldOffset(k.getDeclaredField("tail"));
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    /**
     * Default Constructor for empty list
     */
    public ConcurrentLinkedList() {
        head = new Node<>(null);
        tail = head;
    }

    private boolean casTail(Node<E> compare, Node<E> val) {
        return UNSAFE.compareAndSwapObject(this, tailOffset, compare, val);
    }

    private boolean casHead(Node<E> compare, Node<E> val) {
        return UNSAFE.compareAndSwapObject(this, headOffset, compare, val);
    }


    //==============================
    //-------List Operations--------
    //==============================

    /**
     * @param val Adds value to the front of the list
     */
    public void addFront(E val) {
        addFront(new Node<>(val));
    }

    /**
     * @param newHead Adds the new node to the front of the list
     */
    private void addFront(Node<E> newHead) {
        //CAS Loop
        while (true) {
            Node<E> localHead = head;
            newHead.next = localHead;
            if (casHead(localHead, newHead)) {
                return;
            }
        }
    }


    /**
     * @param val Adds value to the end of the list
     */
    public void addTail(E val) {
        addTail(new Node<>(val));
    }

    /**
     * @param newTail Adds the new node to the front of the list
     */
    private void addTail(Node<E> newTail) {
        //CAS Loop
        while (true) {
            Node<E> localTail = tail;
            if (localTail.casNext(null, newTail) && casTail(localTail, newTail)) {
                return;
            }
        }
    }


    /**
     * Inserts a node in the list after the preceding value
     */
    public void insert(E precedingVal, E newVal) {
        Node<E> newNode = new Node<>(newVal);

        //CAS Loop
        while (true) {
            Node<E> p = head;
            while (p != null && (p.item == null || !p.item.equals(precedingVal))) {
                p = p.next;
            }

            //Preceding element not in the list
            if (p == null) {
                throw new NoSuchElementException();
            }

            Node<E> precedingNode = p;
            Node<E> newNext = precedingNode.next;
            newNode.next = newNext;
            if (precedingNode.casNext(newNext, newNode)) {
                return;
            }
        }
    }


    /**
     * @return Size of list
     * NOTE: This is not a constant time operation
     */
    public int size() {
        int count = 0;
        Node<E> p = head;
        while (p != null) {
            if (p.item != null) {
                count++;
            }
            p = p.next;
        }
        return count;
    }


    /**
     * @return true if the value is contained within the list
     */
    public boolean contains(E val) {
        Node<E> p = head;
        while (p != null) {
            if (p.item != null && p.item.equals(val)) {
                return true;
            }
            p = p.next;
        }
        return false;
    }


    //====================
    //------Iterator------
    //====================
    public Iterator<E> iterator() {
        return new Itr();
    }

    private class Itr implements Iterator<E> {
        private Node<E> nextNode;
        private Node<E> currentNode;

        Itr() {
            currentNode = head;
            nextNode = head;
        }

        public boolean hasNext() {
            if (nextNode == null) {
                return false;
            } else {
                //there may be a next node
                //need to check if there are null nodes
                Node<E> p = nextNode;
                while (p != null) {
                    if (p.item != null) {  //found a non null node
                        return true;
                    }
                    p = p.next;
                }
                //couldn't find anything and hit the end
                return false;
            }
        }

        public E next() {
            if (nextNode == null) throw new NoSuchElementException();
            return advance();
        }

        private E advance() {
            //CAS Loop
            while (true) {
                E nextItem = nextNode.item;

                //if non null item, advance and exit
                if (nextItem != null) {
                    currentNode = nextNode;
                    nextNode = nextNode.next;
                    return nextItem;
                } else {
                    //item is null, need to advance until we hit the end of the list, or find a non-null item

                    Node<E> p = currentNode.next;
                    Node<E> origNext = currentNode.next;
                    while (p != null && p.item == null) {
                        p = p.next;
                    }

                    if (p == null) {
                        //hit end of list
                        nextNode = null;
                        throw new NoSuchElementException();
                    } else {
                        //there was a section of deleted nodes from current node to p
                        //remove these bad nodes by switching current.next to p
                        if (currentNode.casNext(origNext, p)) {
                            currentNode = p;
                            nextNode = p.next;
                            return p.item;
                        }
                        //otherwise, CAS failed (someone else changed the list)
                        //it will go back to the top of the method and try again.
                    }
                }
            }
        }


        public void remove() {
            Node<E> l = currentNode;
            if (l == null) {
                throw new IllegalStateException();
            }
            // rely on a future traversal to delete blank.
            l.item = null;
            currentNode = null;
        }

    }


    /**
     * Node class to be used in the list
     */
    private static class Node<E> {

        /**
         * Setting up Unsafe CAS for the Node Class
         */
        private static final Unsafe UNSAFE;
        private static final long itemOffset;
        private static final long nextOffset;

        static {
            try {
                UNSAFE = getUnsafe();
                Class k = Node.class;
                itemOffset = UNSAFE.objectFieldOffset(k.getDeclaredField("item"));
                nextOffset = UNSAFE.objectFieldOffset(k.getDeclaredField("next"));
            } catch (Exception e) {
                throw new Error(e);
            }
        }

        volatile E item;
        volatile Node<E> next;

        Node(E item) {
            this.item = item;
        }

        void setNext(Node<E> next) {
            this.next = next;
        }

        boolean casItem(E compare, E val) {
            return UNSAFE.compareAndSwapObject(this, itemOffset, compare, val);
        }

        boolean casNext(Node<E> compare, Node<E> val) {
            return UNSAFE.compareAndSwapObject(this, nextOffset, compare, val);
        }
    }

    @SuppressWarnings("restriction")
    private static Unsafe getUnsafe() {
        try {

            Field singleoneInstanceField = Unsafe.class.getDeclaredField("theUnsafe");
            singleoneInstanceField.setAccessible(true);
            return (Unsafe) singleoneInstanceField.get(null);
        } catch (IllegalArgumentException | IllegalAccessException | SecurityException | NoSuchFieldException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return null;
    }


}
