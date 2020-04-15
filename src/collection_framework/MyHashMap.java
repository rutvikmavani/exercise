package collection_framework;

import java.util.*;

public class MyHashMap<K,V> implements Map<K,V> {

    private static class Node<K,V> implements Map.Entry<K,V> {
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

    private class EntrySet implements Set<Entry<K,V> > {

        private class EntrySetIterator implements Iterator<Entry<K,V> > {

            private int slotIndex;
            private Node<K,V> slotNode;
            private Node<K,V> slotNextNode;

            EntrySetIterator() {
                slotIndex = 0;
                slotNode = null;
                slotNextNode = null;
                while(slotIndex < MyHashMap.this.table.length && MyHashMap.this.table[slotIndex] == null)
                    slotIndex++;
                if (slotIndex == MyHashMap.this.table.length)
                    slotNextNode = null;
                else
                    slotNextNode = MyHashMap.this.table[slotIndex];
            }

            @Override
            public boolean hasNext() {
                return (slotNextNode != null);
            }

            @Override
            public Entry<K, V> next() {
                if (slotNextNode == null)
                    throw new NoSuchElementException();

                slotNode = slotNextNode;
                if (slotNextNode.next == null) {
                    slotIndex++;
                    while(slotIndex < MyHashMap.this.table.length && MyHashMap.this.table[slotIndex] == null)
                        slotIndex++;
                    if (slotIndex == MyHashMap.this.table.length)
                        slotNextNode = null;
                    else
                        slotNextNode = MyHashMap.this.table[slotIndex];
                }
                else
                    slotNextNode = slotNextNode.next;
                return slotNode;
            }

            @Override
            public void remove() {
                if (slotNode == null) {
                    throw new IllegalStateException();
                }
                K keyRef = slotNode.getKey();
                MyHashMap.this.remove(keyRef);
                slotNode = null;
            }
        }


        private MyHashMap<K,V> myHashMap;
        EntrySet() {
            this.myHashMap = MyHashMap.this;
        }

        @Override
        public int size() {
            return myHashMap.size();
        }

        @Override
        public boolean isEmpty() {
            return (myHashMap.size() == 0);
        }

        @Override
        public boolean contains(Object o) {
            return myHashMap.containsKey(o);
        }

        @Override
        public Iterator<Entry<K, V>> iterator() {
            return new EntrySetIterator();
        }

        @Override
        public Object[] toArray() {
            return new Object[0];
        }

        @Override
        public <T> T[] toArray(T[] a) {
            return null;
        }

        @Override
        public boolean add(Entry<K, V> kvEntry) {
            return false;
        }

        @Override
        public boolean remove(Object o) {
            return (myHashMap.remove(o) != null);
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return false;
        }

        @Override
        public boolean addAll(Collection<? extends Entry<K, V>> c) {
            return false;
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            return false;
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            return false;
        }

        @Override
        public void clear() {

        }
    }

    private class KeySet implements Set<K> {

        private class KeySetIterator implements Iterator<K> {

            private int slotIndex;
            private Node<K,V> slotNode;
            private Node<K,V> slotNextNode;
            KeySetIterator() {
                slotIndex = 0;
                slotNode = null;
                slotNextNode = null;
                while(slotIndex < MyHashMap.this.table.length && MyHashMap.this.table[slotIndex] == null)
                    slotIndex++;
                if (slotIndex == MyHashMap.this.table.length)
                    slotNextNode = null;
                else
                    slotNextNode = MyHashMap.this.table[slotIndex];
            }

            @Override
            public boolean hasNext() {
                return (slotNextNode != null);
            }

            @Override
            public K next() {
                if (slotNextNode == null)
                    throw new NoSuchElementException();

                slotNode = slotNextNode;
                if (slotNextNode.next == null) {
                    slotIndex++;
                    while(slotIndex < MyHashMap.this.table.length && MyHashMap.this.table[slotIndex] == null)
                        slotIndex++;
                    if (slotIndex == MyHashMap.this.table.length)
                        slotNextNode = null;
                    else
                        slotNextNode = MyHashMap.this.table[slotIndex];
                }
                else
                    slotNextNode = slotNextNode.next;
                return slotNode.getKey();
            }

            @Override
            public void remove() {
                if (slotNode == null) {
                    throw new IllegalStateException();
                }
                K keyRef = slotNode.getKey();
                MyHashMap.this.remove(keyRef);
                slotNode = null;
            }
        }

        private MyHashMap<K,V> myHashMap;
        KeySet() {
            this.myHashMap = MyHashMap.this;
        }
        @Override
        public int size() {
            return myHashMap.numberOfNodes;
        }

        @Override
        public boolean isEmpty() {
            return (myHashMap.numberOfNodes == 0);
        }

        @Override
        public boolean contains(Object o) {
            return myHashMap.containsKey(o);
        }

        @Override
        public Iterator<K> iterator() {
            return new KeySetIterator();
        }

        @Override
        public Object[] toArray() {
            return new Object[0];
        }

        @Override
        public <T> T[] toArray(T[] a) {
            return null;
        }

        @Override
        public boolean add(K k) {
            return false;
        }

        @Override
        public boolean remove(Object o) {
            return myHashMap.remove(o) != null;
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return false;
        }

        @Override
        public boolean addAll(Collection<? extends K> c) {
            return false;
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            return false;
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            return false;
        }

        @Override
        public void clear() {

        }
    }

    private class ValueCollection implements Collection<V> {

        private class ValueCollectionIterator implements Iterator<V> {

            private int slotIndex;
            private Node<K,V> slotNode;
            private Node<K,V> slotNextNode;
            ValueCollectionIterator() {
                slotIndex = 0;
                slotNode = null;
                slotNextNode = null;
                while(slotIndex < MyHashMap.this.table.length && MyHashMap.this.table[slotIndex] == null)
                    slotIndex++;
                if (slotIndex == MyHashMap.this.table.length)
                    slotNextNode = null;
                else
                    slotNextNode = MyHashMap.this.table[slotIndex];
            }

            @Override
            public boolean hasNext() {
                return (slotNextNode != null);
            }

            @Override
            public V next() {

                if (slotNextNode == null)
                    throw new NoSuchElementException();

                slotNode = slotNextNode;
                if (slotNextNode.next == null) {
                    slotIndex++;
                    while(slotIndex < MyHashMap.this.table.length && MyHashMap.this.table[slotIndex] == null)
                        slotIndex++;
                    if (slotIndex == MyHashMap.this.table.length)
                        slotNextNode = null;
                    else
                        slotNextNode = MyHashMap.this.table[slotIndex];
                }
                else
                    slotNextNode = slotNextNode.next;
                return slotNode.getValue();
            }

            @Override
            public void remove() {
                if (slotNode == null) {
                    throw new IllegalStateException();
                }
                K keyRef = slotNode.getKey();
                MyHashMap.this.remove(keyRef);
                slotNode = null;
            }
        }

        private MyHashMap<K,V> myHashMap;
        ValueCollection() {
            this.myHashMap = MyHashMap.this;
        }

        @Override
        public int size() {
            return myHashMap.numberOfNodes;
        }

        @Override
        public boolean isEmpty() {
            return (myHashMap.numberOfNodes == 0);
        }

        @Override
        public boolean contains(Object o) {
            return myHashMap.containsValue(o);
        }

        @Override
        public Iterator<V> iterator() {
            return new ValueCollectionIterator();
        }

        @Override
        public Object[] toArray() {
            return new Object[0];
        }

        @Override
        public <T> T[] toArray(T[] a) {
            return null;
        }

        @Override
        public boolean add(V v) {
            return false;
        }

        @Override
        public boolean remove(Object o) {
            return false;
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return false;
        }

        @Override
        public boolean addAll(Collection<? extends V> c) {
            return false;
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            return false;
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            return false;
        }

        @Override
        public void clear() {

        }
    }

    static private final int DEFAULT_CAPACITY = 16;
    static private final float DEFAULT_LOADFACTOR = 0.75f;

    private int numberOfSlots;        // table size
    private float loadFactor;
    private int numberOfNodes;
    private Node<K,V> table[];

    private EntrySet entrySet = new EntrySet();
    private KeySet keySet = new KeySet();
    private ValueCollection valueCollection = new ValueCollection();

    /* constructors */

    public MyHashMap() {
        this.numberOfSlots = DEFAULT_CAPACITY;
        this.loadFactor = DEFAULT_LOADFACTOR;
        this.numberOfNodes = 0;
        table = (Node<K,V>[]) new Node[numberOfSlots];
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
                slot = Math.abs(key.hashCode()) % numberOfSlots;
            Node<K, V> headNode = table[slot];
            Node<K, V> newHeadNode = new Node<>(key, value, headNode);
            table[slot] = newHeadNode;
            numberOfNodes++;
            if (numberOfNodes > (int) (loadFactor * numberOfSlots))
                rehash();
            return null;
        }

        return oldNode.setValue(value);

    }

    @Override
    public V remove(Object key) {
        int slot = 0;
        if (key != null)
            slot = Math.abs(key.hashCode()) % numberOfSlots;
        Node<K,V> prev = table[slot];
        if (prev == null) {
            return null;
        }

        if ((prev.getKey() == null && key == null) || (prev.getKey() != null && prev.getKey().equals(key))) {
            table[slot] = prev.next;
            numberOfNodes--;
            return prev.getValue();
        }
        Node<K,V> curr = prev.next;
        while (curr != null) {
            if ((curr.getKey() == null && key == null) || (curr.getKey() != null && curr.getKey().equals(key))) {
                prev.next = curr.next;
                numberOfNodes--;
                return curr.getValue();
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
        return keySet;
    }

    @Override
    public Collection values() {
        return valueCollection;
    }

    @Override
    public Set<Entry<K,V>> entrySet() {
        return entrySet;
    }

    /* helper methods */
    private Node<K,V> findNodeByKey(Object key) {
        int slot = 0;
        if (key != null)
            slot = Math.abs(key.hashCode()) % numberOfSlots;
        Node<K,V> start = table[slot];
        while (start != null) {
            if ((key == null && start.getKey() == null) || (key != null && key.equals(start.getKey()))) {
                return start;
            }
            start = start.next;
        }
        return null;
    }

    public void removeValue(V value) {
        for(int i=0;i<table.length;i++) {
            Node<K,V> prev = null;
            Node<K,V> curr = table[i];
            while(curr != null) {
                if ((value == null && curr.getValue() == null) || (value != null && value.equals(curr.getValue()) ) ) {
                    if (prev == null) {
                        table[i] = curr.next;
                    } else {
                        prev.next = curr.next;
                    }
                    curr = curr.next;
                    continue;
                }
                prev = curr;
                curr = curr.next;
            }
        }
    }

    private void rehash() {
        Node<K, V>[] oldTable = table;
        table = (Node<K, V>[]) new Node[2 * numberOfSlots];

        numberOfNodes = 0;
        numberOfSlots = numberOfSlots * 2;
        for (Node<K,V> start: oldTable) {
            while (start != null)
            {
                put(start.getKey(),start.getValue());
                start = start.next;
            }
        }
    }

    /* content display method */

    public void display() {
        System.out.println("Hash Table key-value:  | size : " + size() + " | capacity : " + numberOfSlots);
        int count = 0;
        for (Node<K,V> start: table) {
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
