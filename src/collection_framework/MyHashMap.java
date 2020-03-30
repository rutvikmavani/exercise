package collection_framework;

import java.util.*;

public class MyHashMap<K,V> implements Map {

    static class Node<K,V> implements Map.Entry<K,V> {
        private final K key;
        private V value;
        private Node<K,V> next;

        public Node(K key,V value,Node<K,V> next)
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
        public final V setValue(V newValue) {
            V oldValue = value;
            value = newValue;
            return oldValue;
        }

        @Override
        public final int hashCode() {
            return Objects.hashCode(key);
        }

    }

    /*-------------------------------------------------------------------------------------*/

    static final int DEFAULT_CAPACITY = 1<<4;
    static final float DEFAULT_LOADFACTOR = 0.75f;

    // tableSize
    private int capcity;
    private float loadFactor;
    private int numberOfNodes;
    private ArrayList<Node<K,V> > table;
    public MyHashMap() {
        this.capcity = DEFAULT_CAPACITY;
        this.loadFactor = DEFAULT_LOADFACTOR;
        this.numberOfNodes = 0;
        table = new ArrayList<Node<K,V> >(capcity);
        for (int i = 0;i < this.capcity;i++) {
            table.add(null);
        }
    }

    public MyHashMap(int capcity) {
        this.capcity = nearestTwoPower(capcity);
        this.loadFactor = DEFAULT_LOADFACTOR;
        this.numberOfNodes = 0;
        table = new ArrayList<Node<K,V> >(capcity);
        for (int i = 0;i < this.capcity;i++) {
            table.add(null);
        }
    }

    public MyHashMap(float loadFactor) {
        this.capcity = DEFAULT_CAPACITY;
        this.loadFactor = loadFactor;
        this.numberOfNodes = 0;
        table = new ArrayList<Node<K,V> >(capcity);
        for (int i = 0;i < this.capcity;i++) {
            table.add(null);
        }
    }

    public MyHashMap(int capcity, float loadFactor) {
        this.capcity = nearestTwoPower(capcity);
        this.loadFactor = loadFactor;
        this.numberOfNodes = 0;
        table = new ArrayList<Node<K,V> >(capcity);
        for (int i = 0;i < this.capcity;i++) {
            table.add(null);
        }
    }

    private int nearestTwoPower(int n)
    {
        n = n - 1;
        n = n | (n >> 1);
        n = n | (n >> 2);
        n = n | (n >> 4);
        n = n | (n >> 8);
        n = n | (n >> 16);
        return n + 1;
    }

    /*-------------------------------------------------------------------------------------*/

    @Override
    public int size() {
        return numberOfNodes;
    }

    @Override
    public boolean isEmpty() {
        return (numberOfNodes == 0);
    }


    // helper method
    public Node<K,V> findNodeByKey(Object key) {
        if (key == null) return null;
        int slot = key.hashCode()%capcity;

        Node start = table.get(slot);
        while (start != null)
        {
            if (key.equals(start.getKey())) {
                return start;
            }
            start = start.next;
        }
        return null;
    }

    @Override
    public boolean containsKey(Object key) {
        if (findNodeByKey(key) != null) return true;
        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        return false;
    }

    @Override
    public Object get(Object key) {
        Node<K,V> node = findNodeByKey(key);
        if (node != null) {
            return node.getValue();
        }
        return null;
    }

    @Override
    public Object put(Object key, Object value) {

        Node<K,V> oldNode = findNodeByKey(key);

        if (oldNode == null) {
            int slot = key.hashCode() % capcity;
            Node headNode = table.get(slot);
            Node newHeadNode = new Node(key, value, headNode);
            table.set(slot, newHeadNode);
            numberOfNodes++;
            if (numberOfNodes > (int)(loadFactor * capcity))
                rehash();
            return null;
        }

        V oldValue = oldNode.getValue();
        oldNode.setValue((V) value);

        return oldValue;
    }

    private void rehash() {
        ArrayList<Node<K,V> > oldTable = table;
        table = new ArrayList<Node<K,V> >(2*capcity );
        for (int i = 0;i<2*capcity;i++)
            table.add(null);

        numberOfNodes = 0;
        capcity = capcity*2;
        for (Node<K,V> node: oldTable) {
            Node<K,V> start = node;
            while (start != null)
            {
                put(start.getKey(),start.getValue());
                start = start.next;
            }
        }
    }

    /*private void rehashDecrese() {
        ArrayList<Node<K,V> > oldTable = table;
        table = new ArrayList<Node<K,V> >(capcity/2);
        for (int i = 0;i<capcity/2;i++)
            table.add(null);

        numberOfNodes = 0;
        capcity = capcity/2;
        for (Node<K,V> node: oldTable) {
            Node<K,V> start = node;
            while (start != null)
            {
                put(start.getKey(),start.getValue());
                start = start.next;
            }
        }
    }*/

    @Override
    public Object remove(Object key) {
        int slot = key.hashCode()%capcity;
        Node<K,V> prev = table.get(slot);
        if (prev == null)
            return null;
        if (prev.getKey().equals(key)) {
            table.set(slot,prev.next);
            numberOfNodes--;
            //if (2*numberOfNodes < (int)(loadFactor*capcity))
            //    rehashDecrese();
            return prev;
        }
        Node curr = prev.next;
        while (curr != null) {
            if (curr.getKey().equals(key)) {
                prev.next = curr.next;
                numberOfNodes--;
                //if (2*numberOfNodes < (int)(loadFactor*capcity))
                //    rehashDecrese();
                return  curr;
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
    public Set<Entry> entrySet() {
        return null;
    }

    public void display() {
        System.out.println("Hash Table key-value:  | size : " + size() + " | capacity : " + capcity);
        int count = 0;
        for (Node<K,V> node: table) {
            Node<K,V> start = node;
            System.out.print(count + " : ");
            while (start != null)
            {
                System.out.print("(" + start.key + " " + start.value + ") ");
                start = start.next;
            }
            count++;
            System.out.println();
        }
    }
}
