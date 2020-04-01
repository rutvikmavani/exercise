package collection_framework;

import java.util.*;

public class MyHashMap<K,V> implements Map<K,V> {

    static class Node<K,V> implements Map.Entry<K,V> {
        private K key;
        private V value;
        private Node<K,V> next;


        Node(K key,V value,Node<K,V> next)
        {
            this.key = key;
            this.value = value;
            this.next = next;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V newValue) {
            V oldValue = value;
            value = newValue;
            return oldValue;
        }

    }

    static private final int DEFAULT_CAPACITY = 16;
    static private final float DEFAULT_LOADFACTOR = 0.75f;

    private int capacity;        // table size
    private float loadFactor;
    private int numberOfNodes;
    private Node<K,V> table[];

    /* constructors */

    public MyHashMap() {
        this.capacity = DEFAULT_CAPACITY;
        this.loadFactor = DEFAULT_LOADFACTOR;
        this.numberOfNodes = 0;
        table = (Node<K,V>[]) new Node[capacity];
    }


    @Override
    public int size() {
        return numberOfNodes;
    }

    @Override
    public boolean isEmpty() {
        return (numberOfNodes == 0);
    }


    @Override
    public boolean containsKey(Object key) {
        if (findNodeByKey(key) == null) return false;
        return true;
    }

    @Override
    public boolean containsValue(Object value) {

        for (Node<K,V> start : table) {
            while (start != null) {
                if ((value == null && start.getValue() == null) || (value != null && value.equals(start.getValue())))
                    return true;
                start = start.next;
            }
        }
        return false;
    }

    @Override
    public V get(Object key) {
        Node<K,V> node = findNodeByKey(key);
        if (node != null) {
            return node.getValue();
        }
        return null;
    }

    @Override
    public V put(K key, V value) {

        Node<K,V> oldNode = findNodeByKey(key);

        if (oldNode == null) {
            int slot = 0;
            if (key != null)
                slot = Math.abs(key.hashCode()) % capacity;
            Node<K,V> headNode = table[slot];
            Node<K,V> newHeadNode = new Node<>(key, value, headNode);
            table[slot] = newHeadNode;
            numberOfNodes++;
            if (numberOfNodes > (int)(loadFactor * capacity))
                rehash();
            return null;
        }

        return oldNode.setValue(value);

    }

    @Override
    public V remove(Object key) {
        int slot = 0;
        if (key != null)
            slot = Math.abs(key.hashCode()) % capacity;
        Node<K,V> prev = table[slot];
        if (prev == null)
            return null;
        if (prev.getKey().equals(key)) {
            table[slot] = prev.next;
            numberOfNodes--;
            return prev.getValue();
        }
        Node<K,V> curr = prev.next;
        while (curr != null) {
            if (curr.getKey().equals(key)) {
                prev.next = curr.next;
                numberOfNodes--;
                return  curr.getValue();
            }
            prev = prev.next;
            curr = curr.next;
        }
        return null;
    }

    @Override
    public void putAll(Map m) {

    }

    @Override
    public void clear() {

    }

    @Override
    public Set keySet() {
        return null;
    }

    @Override
    public Collection values() {
        return null;
    }

    @Override
    public Set<Entry<K,V>> entrySet() {
        Set<Entry<K,V> > entries = new HashSet<>();
        for(Node start : table) {
            while (start != null) {
                entries.add(start);
                start = start.next;
            }
        }
        return entries;
    }

    /* helper methods */
    private Node<K,V> findNodeByKey(Object key) {
        int slot = 0;
        if (key != null)
            slot = Math.abs(key.hashCode()) % capacity;
        Node<K,V> start = table[slot];
        while (start != null) {
            if ((key == null && start.getKey() == null) || (key != null && key.equals(start.getKey()))) {
                return start;
            }
            start = start.next;
        }
        return null;
    }

    private void rehash() {
        Node<K, V>[] oldTable = table;
        table = (Node<K, V>[]) new Node[2* capacity];

        numberOfNodes = 0;
        capacity = capacity *2;
        for (Node<K,V> node: oldTable) {
            Node<K,V> start = node;
            while (start != null)
            {
                put(start.getKey(),start.getValue());
                start = start.next;
            }
        }
    }

    /* content display method */

    public void display() {
        System.out.println("Hash Table key-value:  | size : " + size() + " | capacity : " + capacity);
        int count = 0;
        for (Node<K,V> node: table) {
            Node<K,V> start = node;
            System.out.print(count + " : ");
            while (start != null) {
                System.out.print("(" + start.key + " " + start.value + ") ");
                start = start.next;
            }
            count++;
            System.out.println();
        }
    }

}
