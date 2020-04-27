package collection_framework;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MyConcurrentHashMap<K,V> implements Map<K,V> {

    private static class Node<K,V> implements Map.Entry<K,V> {
        private K key;
        private V value;
        private Node<K,V> next;
        private int hash;

        Node(K key, V value, Node<K,V> next) {
            this.key = key;
            this.value = value;
            this.next = next;
            this.hash = (key != null) ? Math.abs(key.hashCode()) : 0;
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
    private class Segment<K,V> {
        private int numberOfNodes;
        private int numberOfSlots;
        private Node<K,V> table[];
        Segment() {
            this.numberOfNodes = 0;
            this.numberOfSlots = DEFAULT_NUMBER_OF_SLOTS_PER_SEGMENT;
            table = new Node[numberOfSlots];
        }
        Segment(int numberOfNodes,int numberOfSlots,Node<K,V> table[]) {
            this.numberOfNodes = numberOfNodes;
            this.numberOfSlots = numberOfSlots;
            this.table = table;
        }

        public Node<K,V> get(Object key,int hash) {
            int slot = (hash / concurrencyLevel) % numberOfSlots;
            Node<K,V> node = table[slot];
            while (node != null) {
                if (areEqual(key,node.key))
                    return node;
                node = node.next;
            }
            return null;
        }

        public Segment<K,V> put(K key,V value,int hash) {
            int slot = (hash / concurrencyLevel) % numberOfSlots;
            Node<K,V> newHeadNode = new Node<>(key,value,table[slot]);
            table[slot] = newHeadNode;
            numberOfNodes++;
            if (numberOfNodes > (int)(loadFactor * numberOfSlots))
                return rehash();
            return null;
        }

        public V remove(Object key,int hash) {
            int slot = (hash / concurrencyLevel) % numberOfSlots;
            Node<K,V> prevNode = null;
            Node<K,V> node = table[slot];
            while (node != null) {
                if (areEqual(node.key,key))
                    break;
                prevNode = node;
                node = node.next;
            }
            if (node == null)
                return null;

            if (prevNode == null) {
                table[slot] = node.next;
            }
            else {
                prevNode.next = node.next;
            }
            numberOfNodes--;
            return node.value;
        }

        private Segment<K,V> rehash() {
            int newNumberOfSlots = numberOfSlots * 2;
            Node<K, V>[] newTable = new Node[newNumberOfSlots];
            Node<K, V>[] oldNodes = new Node[numberOfNodes];
            int count = 0;
            for(int i=0;i<table.length;i++) {
                Node<K,V> node = table[i];
                while (node != null) {
                    oldNodes[count] = node;
                    node = node.next;
                    count++;
                }
            }

            for (int i=0;i<oldNodes.length;i++) {
                int slot = (oldNodes[i].hash / concurrencyLevel) % newNumberOfSlots;
                oldNodes[i].next = newTable[slot];
                newTable[slot] = oldNodes[i];
            }
            return new Segment<>(numberOfNodes,newNumberOfSlots,newTable);
        }

        public void display() {
            for(int i=0;i<numberOfSlots;i++) {
                Node<K,V> node = table[i];
                System.out.print(i + " : ");
                while (node != null) {
                    System.out.print(" (" + node.key + " " + node.value + ") ");
                    node = node.next;
                }
                System.out.println();
            }
        }
    }

    private final static int DEFAULT_CONCURRENCY_LEVEL = 16;
    private final static float DEFAULT_LOAD_FACTOR = 0.75f;
    private final static int DEFAULT_NUMBER_OF_SLOTS_PER_SEGMENT = 16;

    private volatile Segment<K,V> segments[];
    private volatile float loadFactor;
    private volatile int concurrencyLevel;
    private Lock locks[];

    MyConcurrentHashMap() {
        loadFactor = DEFAULT_LOAD_FACTOR;
        concurrencyLevel = DEFAULT_CONCURRENCY_LEVEL;
        segments = new Segment[concurrencyLevel];
        locks = new Lock[concurrencyLevel];
        for (int i = 0; i< segments.length; i++) {
            segments[i] = new Segment<K,V>();
            locks[i] = new ReentrantLock();
        }
    }

    @Override
    public int size() {
        int sum = 0;
        for (int i=0;i<segments.length;i++) {
            sum += segments[i].numberOfNodes;
        }
        return sum;
    }

    @Override
    public boolean isEmpty() {
        return (size() == 0);
    }

    @Override
    public boolean containsKey(Object key) {
        return get(key) != null;
    }

    @Override
    public boolean containsValue(Object value) {
        return false;
    }

    /*------------------------------- helper methods --------------------------------*/
    private boolean areEqual(Object o1,Object o2) {
        if ((o1 == null && o2 == null) || (o1 != null && o1.equals(o2)))
            return true;
        return false;
    }

    private int hashCodeFromObject(Object o) {
        return (o != null) ? Math.abs(o.hashCode()) : 0;
    }
    /*-------------------------------------------------------------------------------*/


    @Override
    public V get(Object key) {
        int hash = hashCodeFromObject(key);
        int segmentId = hash % concurrencyLevel;
        // reference to avoid conflict from rehash()
        Segment<K,V> segment = segments[segmentId];
        Node<K,V> node = segment.get(key,hash);
        if (node == null)
            return null;
        return node.value;
    }

    @Override
    public V put(K key, V value) {
        int hash = hashCodeFromObject(key);
        int segmentId = hash % concurrencyLevel;

        locks[segmentId].lock();
        try {

            Node<K,V> node = segments[segmentId].get(key, hash);
            if (node == null) {
                // if rehash() happens newSegment will have non null value
                Segment<K,V> newSegment = segments[segmentId].put(key,value,hash);
                if (newSegment != null)
                    segments[segmentId] = newSegment;

                return null;
            }

            return node.setValue(value);
        }
        finally {
            locks[segmentId].unlock();
        }
    }

    @Override
    public V remove(Object key) {
        int hash = hashCodeFromObject(key);
        int segmentId = hash % concurrencyLevel;

        locks[segmentId].lock();
        try {
            return segments[segmentId].remove(key,hash);
        }
        finally {
            locks[segmentId].unlock();
        }
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        for(Map.Entry<? extends K, ? extends V> entry: m.entrySet()) {
            put(entry.getKey(),entry.getValue());
        }
    }

    @Override
    public void clear() {
        for(int i=0;i<segments.length;i++) {
            locks[i].lock();
            try {
                Arrays.fill(segments[i].table, null);
            }
            finally {
                locks[i].unlock();
            }
        }
    }






    /*---------------- classes for iterator ---------------*/
    private class EntrySet implements Set<Map.Entry<K,V>> {

        private class EntrySetIterator implements Iterator<Map.Entry<K,V> > {

            int segmentIndex;
            int slotIndex;
            Node<K,V> node;
            Node<K,V> nextNode;

            EntrySetIterator() {
                this.segmentIndex = 0;
                this.slotIndex = 0;
                this.node = null;
                this.nextNode = null;
                while (segmentIndex < segments.length) {
                    while (slotIndex < segments[segmentIndex].table.length && segments[segmentIndex].table[slotIndex] == null) {
                        slotIndex++;
                    }
                    if (slotIndex == segments[segmentIndex].table.length) {
                        segmentIndex++;
                        slotIndex = 0;
                    }
                    else {
                        nextNode = segments[segmentIndex].table[slotIndex];
                        break;
                    }
                }
            }

            @Override
            public boolean hasNext() {
                return (nextNode != null);
            }

            @Override
            public Entry<K, V> next() {
                if (nextNode == null)
                    throw new NoSuchElementException();
                node = nextNode;
                if(nextNode.next == null) {
                    slotIndex++;
                    while (segmentIndex < segments.length) {
                        while (slotIndex < segments[segmentIndex].table.length && segments[segmentIndex].table[slotIndex] == null) {
                            slotIndex++;
                        }
                        if (slotIndex == segments[segmentIndex].table.length) {
                            segmentIndex++;
                            slotIndex = 0;
                        }
                        else {
                            nextNode = segments[segmentIndex].table[slotIndex];
                            break;
                        }
                    }
                    if (segmentIndex == segments.length)
                        nextNode = null;
                }
                else {
                    nextNode = nextNode.next;
                }
                return node;
            }

            @Override
            public void remove() {
                if (node == null)
                    throw new IllegalStateException();
                MyConcurrentHashMap.this.remove(node.getKey());
                node = null;
            }
        }

        // copy of segments
        Segment<K,V>[] segments;
        EntrySet() {
            segments = new Segment[concurrencyLevel];
            for(int i=0;i<MyConcurrentHashMap.this.segments.length;i++) {
                segments[i] = MyConcurrentHashMap.this.segments[i];
            }
        }

        @Override
        public int size() {
            int sum = 0;
            for (int i=0;i<segments.length;i++) {
                sum += segments[i].numberOfNodes;
            }
            return sum;
        }

        @Override
        public boolean isEmpty() {
            return size() == 0;
        }

        @Override
        public boolean contains(Object o) {
            return false;
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
            return false;
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

            int segmentIndex;
            int slotIndex;
            Node<K,V> node;
            Node<K,V> nextNode;

            KeySetIterator() {
                this.segmentIndex = 0;
                this.slotIndex = 0;
                this.node = null;
                this.nextNode = null;
                while (segmentIndex < segments.length) {
                    while (slotIndex < segments[segmentIndex].table.length && segments[segmentIndex].table[slotIndex] == null) {
                        slotIndex++;
                    }
                    if (slotIndex == segments[segmentIndex].table.length) {
                        segmentIndex++;
                        slotIndex = 0;
                    }
                    else {
                        nextNode = segments[segmentIndex].table[slotIndex];
                        break;
                    }
                }
            }

            @Override
            public boolean hasNext() {
                return (nextNode != null);
            }

            @Override
            public K next() {
                if (nextNode == null)
                    throw new NoSuchElementException();
                node = nextNode;
                if(nextNode.next == null) {
                    slotIndex++;
                    while (segmentIndex < segments.length) {
                        while (slotIndex < segments[segmentIndex].table.length && segments[segmentIndex].table[slotIndex] == null) {
                            slotIndex++;
                        }
                        if (slotIndex == segments[segmentIndex].table.length) {
                            segmentIndex++;
                            slotIndex = 0;
                        }
                        else {
                            nextNode = segments[segmentIndex].table[slotIndex];
                            break;
                        }
                    }
                    if (segmentIndex == segments.length)
                        nextNode = null;
                }
                else {
                    nextNode = nextNode.next;
                }
                return node.getKey();
            }

            @Override
            public void remove() {
                if (node == null)
                    throw new IllegalStateException();
                MyConcurrentHashMap.this.remove(node.getKey());
                node = null;
            }
        }

        // copy of segments
        Segment<K,V>[] segments;
        KeySet() {
            segments = new Segment[concurrencyLevel];
            for(int i=0;i<MyConcurrentHashMap.this.segments.length;i++) {
                segments[i] = MyConcurrentHashMap.this.segments[i];
            }
        }

        @Override
        public int size() {
            int sum = 0;
            for (int i=0;i<segments.length;i++) {
                sum += segments[i].numberOfNodes;
            }
            return sum;
        }

        @Override
        public boolean isEmpty() {
            return (size() == 0);
        }

        @Override
        public boolean contains(Object o) {
            return false;
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
            return false;
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

            int segmentIndex;
            int slotIndex;
            Node<K,V> node;
            Node<K,V> nextNode;

            ValueCollectionIterator() {
                this.segmentIndex = 0;
                this.slotIndex = 0;
                this.node = null;
                this.nextNode = null;
                while (segmentIndex < segments.length) {
                    while (slotIndex < segments[segmentIndex].table.length && segments[segmentIndex].table[slotIndex] == null) {
                        slotIndex++;
                    }
                    if (slotIndex == segments[segmentIndex].table.length) {
                        segmentIndex++;
                        slotIndex = 0;
                    }
                    else {
                        nextNode = segments[segmentIndex].table[slotIndex];
                        break;
                    }
                }
            }

            @Override
            public boolean hasNext() {
                return nextNode != null;
            }

            @Override
            public V next() {
                if (nextNode == null)
                    throw new NoSuchElementException();
                node = nextNode;
                if(nextNode.next == null) {
                    slotIndex++;
                    while (segmentIndex < segments.length) {
                        while (slotIndex < segments[segmentIndex].table.length && segments[segmentIndex].table[slotIndex] == null) {
                            slotIndex++;
                        }
                        if (slotIndex == segments[segmentIndex].table.length) {
                            segmentIndex++;
                            slotIndex = 0;
                        }
                        else {
                            nextNode = segments[segmentIndex].table[slotIndex];
                            break;
                        }
                    }
                    if (segmentIndex == segments.length)
                        nextNode = null;
                }
                else {
                    nextNode = nextNode.next;
                }
                return node.getValue();
            }

            @Override
            public void remove() {
                if (node == null)
                    throw new IllegalStateException();
                MyConcurrentHashMap.this.remove(node.getKey());
                node = null;
            }
        }

        // copy of segments
        Segment<K,V>[] segments;
        ValueCollection() {
            segments = new Segment[concurrencyLevel];
            for(int i=0;i<MyConcurrentHashMap.this.segments.length;i++) {
                segments[i] = MyConcurrentHashMap.this.segments[i];
            }
        }

        @Override
        public int size() {
            int sum = 0;
            for (int i=0;i<segments.length;i++) {
                sum += segments[i].numberOfNodes;
            }
            return sum;
        }

        @Override
        public boolean isEmpty() {
            return size() == 0;
        }

        @Override
        public boolean contains(Object o) {
            return false;
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

    /*-----------------------------------------------------*/



    @Override
    public Set<K> keySet() {
        return new KeySet();
    }

    @Override
    public Collection<V> values() {
        return new ValueCollection();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return new EntrySet();
    }

    public void display() {
        for(int i=0;i<segments.length;i++) {
            System.out.println("segemnt number : " + i);
            segments[i].display();
        }
    }
}
