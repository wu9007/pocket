package org.hv.pocket.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author wujianchuan
 */
public class LruCache<K, V> implements Cache<K, V> {
    private final Logger logger = LoggerFactory.getLogger(LruCache.class);

    private AtomicInteger elementCount;
    private Node head;
    private Node tail;
    private ConcurrentHashMap<K, Node> hashMap;
    private int maxSize;

    public LruCache(int maxSize) {
        this.maxSize = maxSize;
        this.hashMap = new ConcurrentHashMap<>(this.maxSize * 4 / 3);
        this.elementCount = new AtomicInteger(0);
    }

    @Override
    public void put(K key, V value) {
        Node node = new Node(key, value);
        this.hashMap.put(node.k, node);
        if (this.elementCount.getAndIncrement() == 0) {
            this.head = node;
            this.tail = node;
        } else {
            if (this.elementCount.get() >= maxSize) {
                this.hashMap.remove(this.tail.k);
                Node newTail = this.tail.pre;
                newTail.next = null;
                this.tail = newTail;
                this.elementCount.decrementAndGet();
            }
            synchronized (this) {
                Node newNext = this.head;
                node.next = newNext;
                newNext.pre = node;
                this.head = node;
            }
        }
        logger.info("Element Count: {}", this.elementCount.get());
    }

    @Override
    public V get(K key) {
        Node node = this.hashMap.getOrDefault(key, null);
        if (node == null) {
            return null;
        }
        if (this.head == null || this.tail == null) {
            return null;
        }
        if (this.head.k.equals(node.k)) {
            return node.v;
        }
        synchronized (this) {
            if (node.k.equals(this.tail.k)) {
                Node newTail = node.pre;
                newTail.next = null;
                this.tail = newTail;
            } else {
                Node pre = node.pre;
                Node next = node.next;
                pre.next = next;
                next.pre = pre;
            }
            node.pre = null;
            node.next = this.head;
            this.head.pre = node;
            this.head = node;
        }
        return node.v;
    }

    @Override
    public synchronized void remove(K key) {
        Node node = this.hashMap.getOrDefault(key, null);
        if (node != null) {
            if (this.elementCount.get() > 1) {
                if (node.k.equals(this.head.k)) {
                    Node newHead = node.next;
                    newHead.pre = null;
                    this.head = newHead;
                } else if (node.k.equals(this.tail.k)) {
                    Node newTail = node.pre;
                    newTail.next = null;
                    this.tail = newTail;
                } else {
                    Node pre = node.pre;
                    Node next = node.next;
                    pre.next = next;
                    next.pre = pre;
                }
            }
            this.hashMap.remove(key);
            logger.info("Element Count: {}", this.elementCount.decrementAndGet());
        }
    }

    @Override
    public void clear() {
        this.head = null;
        this.tail = null;
        this.hashMap.clear();
        this.elementCount.set(0);
    }

    private class Node {
        Node pre;
        Node next;
        K k;
        V v;

        Node(K k, V v) {
            this.k = k;
            this.v = v;
        }
    }
}
