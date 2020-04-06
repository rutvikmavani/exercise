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
            private Node<K,V> slotPrevNode;
            private MyHashMap<K,V> myHashMap;
            boolean canDelete;
            EntrySetIterator() {
                this.slotIndex = 0;
                this.slotNode = null;
                this.slotPrevNode = null;
                this.myHashMap = EntrySet.this.myHashMap;
                this.canDelete = false;
            }

            @Override
            public boolean hasNext() {
                int slotIndexCopy = this.slotIndex;
                Node<K,V> slotNodeCopy = this.slotNode;

                if (slotNodeCopy == null || slotNodeCopy.next == null) {

                    if (slotNodeCopy != null)
                        slotIndexCopy++;

                    while (slotIndexCopy < myHashMap.table.length && myHashMap.table[slotIndexCopy] == null)
                        slotIndexCopy++;

                    if (slotIndexCopy == myHashMap.table.length)
                        return false;

                    /* --------------- otherwise slot will have non null node --------------- */
                    return true;
                }

                /* slotNodeCopy.next is not null */
                return true;
            }

            @Override
            public Entry<K, V> next() {

                if (slotNode == null || slotNode.next == null) {

                    if (slotNode != null)
                        slotIndex++;

                    while (slotIndex < myHashMap.table.length && myHashMap.table[slotIndex] == null)
                        slotIndex++;

                    if (slotIndex == myHashMap.table.length)
                        throw new NoSuchElementException();

                    /* --------------- otherwise slot will have non null node --------------- */
                    slotNode = myHashMap.table[slotIndex];
                    slotPrevNode = null;
                    canDelete = true;
                    return slotNode;
                }
                slotPrevNode = slotNode;
                slotNode = slotNode.next;
                canDelete = true;
                return slotNode;
            }

            @Override
            public void remove() {
                if (!canDelete)
                    throw new IllegalStateException();

                if (slotPrevNode == null) {
                    /* ---------- remove head ----------- */
                    myHashMap.table[slotIndex] = slotNode.next;
                    slotNode = null;
                } else {
                    slotPrevNode.next = slotNode.next;
                    slotNode = slotPrevNode;
                    slotPrevNode = null;
                }
                canDelete = false;
                myHashMap.numberOfNodes--;
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
            private Node<K,V> slotPrevNode;
            private MyHashMap<K,V> myHashMap;
            boolean canDelete;
            KeySetIterator() {
                this.slotIndex = 0;
                this.slotNode = null;
                this.slotPrevNode = null;
                this.myHashMap = KeySet.this.myHashMap;
                this.canDelete = false;
            }

            @Override
            public boolean hasNext() {
                int slotIndexCopy = this.slotIndex;
                Node<K,V> slotNodeCopy = this.slotNode;

                if (slotNodeCopy == null || slotNodeCopy.next == null) {

                    if (slotNodeCopy != null)
                        slotIndexCopy++;

                    while (slotIndexCopy < myHashMap.table.length && myHashMap.table[slotIndexCopy] == null)
                        slotIndexCopy++;

                    if (slotIndexCopy == myHashMap.table.length)
                        return false;

                    /* --------------- otherwise slot will have non null node --------------- */
                    return true;
                }

                /* slotNodeCopy.next is not null */
                return true;
            }

            @Override
            public K next() {

                if (slotNode == null || slotNode.next == null) {

                    if (slotNode != null)
                        slotIndex++;

                    while (slotIndex < myHashMap.table.length && myHashMap.table[slotIndex] == null)
                        slotIndex++;

                    if (slotIndex == myHashMap.table.length)
                        throw new NoSuchElementException();

                    /* --------------- otherwise slot will have non null node --------------- */
                    slotNode = myHashMap.table[slotIndex];
                    slotPrevNode = null;
                    canDelete = true;
                    return slotNode.getKey();
                }
                slotPrevNode = slotNode;
                slotNode = slotNode.next;
                canDelete = true;
                return slotNode.getKey();
            }

            @Override
            public void remove() {
                if (!canDelete)
                    throw new IllegalStateException();

                if (slotPrevNode == null) {
                    /* ---------- remove head ----------- */
                    myHashMap.table[slotIndex] = slotNode.next;
                    slotNode = null;
                } else {
                    slotPrevNode.next = slotNode.next;
                    slotNode = slotPrevNode;
                    slotPrevNode = null;
                }
                canDelete = false;
                myHashMap.numberOfNodes--;
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
            private Node<K,V> slotPrevNode;
            private MyHashMap<K,V> myHashMap;
            boolean canDelete;
            ValueCollectionIterator() {
                this.slotIndex = 0;
                this.slotNode = null;
                this.slotPrevNode = null;
                this.myHashMap = ValueCollection.this.myHashMap;
                this.canDelete = false;
            }

            @Override
            public boolean hasNext() {
                int slotIndexCopy = this.slotIndex;
                Node<K,V> slotNodeCopy = this.slotNode;

                if (slotNodeCopy == null || slotNodeCopy.next == null) {

                    if (slotNodeCopy != null)
                        slotIndexCopy++;

                    while (slotIndexCopy < myHashMap.table.length && myHashMap.table[slotIndexCopy] == null)
                        slotIndexCopy++;

                    if (slotIndexCopy == myHashMap.table.length)
                        return false;

                    /* --------------- otherwise slot will have non null node --------------- */
                    return true;
                }

                /* slotNodeCopy.next is not null */
                return true;
            }

            @Override
            public V next() {

                if (slotNode == null || slotNode.next == null) {

                    if (slotNode != null)
                        slotIndex++;

                    while (slotIndex < myHashMap.table.length && myHashMap.table[slotIndex] == null)
                        slotIndex++;

                    if (slotIndex == myHashMap.table.length)
                        throw new NoSuchElementException();

                    /* --------------- otherwise slot will have non null node --------------- */
                    slotNode = myHashMap.table[slotIndex];
                    slotPrevNode = null;
                    canDelete = true;
                    return slotNode.getValue();
                }
                slotPrevNode = slotNode;
                slotNode = slotNode.next;
                canDelete = true;
                return slotNode.getValue();
            }

            @Override
            public void remove() {
                if (!canDelete)
                    throw new IllegalStateException();

                if (slotPrevNode == null) {
                    /* ---------- remove head ----------- */
                    myHashMap.table[slotIndex] = slotNode.next;
                    slotNode = null;
                } else {
                    slotPrevNode.next = slotNode.next;
                    slotNode = slotPrevNode;
                    slotPrevNode = null;
                }
                canDelete = false;
                myHashMap.numberOfNodes--;
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

    private int capacity;        // table size
    private float loadFactor;
    private int numberOfNodes;
    private Node<K,V> table[];

    private EntrySet entrySet = new EntrySet();
    private KeySet keySet = new KeySet();
    private ValueCollection valueCollection = new ValueCollection();

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
        table = (Node<K, V>[]) new Node[2* capacity];

        numberOfNodes = 0;
        capacity = capacity *2;
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
        System.out.println("Hash Table key-value:  | size : " + size() + " | capacity : " + capacity);
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
