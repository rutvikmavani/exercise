package Grep;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MyThreadPoolExecutorVersion2 implements ExecutorService {

    private int corePoolSize;
    private int maximumPoolSize;
    private long keepAliveTime;
    private TimeUnit unit;

    private Lock lock = new ReentrantLock();
    private Condition condition = lock.newCondition();
    private Condition allThreadEnded = lock.newCondition();

    private BoundedQueue<Runnable> taskQueue = new BoundedQueue<>(100);
    private volatile boolean allowCoreThreadTimeOut = false;
    private volatile boolean shutdown = false;
    private Set<Thread> threadSet = new HashSet<>();

    MyThreadPoolExecutorVersion2(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit) {
        this.corePoolSize = corePoolSize;
        this.maximumPoolSize = maximumPoolSize;
        this.keepAliveTime = keepAliveTime;
        this.unit = unit;
    }

    @Override
    public void shutdown() {
        lock.lock();
        try {
            if (shutdown)
                return;
            shutdown = true;
            condition.signalAll();
        }
        finally {
            lock.unlock();
        }
    }

    @Override
    public List<Runnable> shutdownNow() {
        lock.lock();
        try {
            List<Runnable> list = new ArrayList<>();
            if (shutdown)
                return null;
            shutdown = true;
            for (Thread thread : threadSet) {
                thread.interrupt();
            }
            while (!taskQueue.isEmpty()) {
                list.add(taskQueue.poll());
            }
            return list;
        }
        finally {
            lock.unlock();
        }
    }

    @Override
    public boolean isShutdown() {
        return shutdown;
    }

    @Override
    public boolean isTerminated() {
        return (threadSet.size() == 0);
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        lock.lock();
        if (threadSet.size() == 0)
            return true;
        try {
            return allThreadEnded.await(timeout, unit);
        }
        finally {
            lock.unlock();
        }
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        lock.lock();
        try {
            FutureImpl<T> futureImpl = new FutureImpl<T>();
            if (threadSet.size() < corePoolSize) {
                CallInfo<T> callInfo = new CallInfo<T>(true,task,futureImpl);
                Thread thread = new Thread(callInfo);
                threadSet.add(thread);
                thread.start();
            }
            else if (!taskQueue.isFull()) {
                CallInfoShort<T> callInfoShort = new CallInfoShort<T>(task,futureImpl);
                taskQueue.add(callInfoShort);
                condition.signal();
            }
            else if (threadSet.size() < maximumPoolSize) {
                CallInfo<T> callInfo = new CallInfo<T>(false,task,futureImpl);
                Thread thread = new Thread(callInfo);
                threadSet.add(thread);
                thread.start();
            }
            else {
                throw new RejectedExecutionException();
            }
            return futureImpl;
        }
        finally {
            lock.unlock();
        }
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        return null;
    }

    @Override
    public Future<?> submit(Runnable task) {
        return null;
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        return null;
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        return null;
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        return null;
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return null;
    }

    @Override
    public void execute(Runnable command) {
        lock.lock();
        try {
            if (threadSet.size() < corePoolSize) {
                RunInfo runInfo = new RunInfo(true,command);
                Thread thread = new Thread(runInfo);
                threadSet.add(thread);
                thread.start();
            }
            else if (!taskQueue.isFull()) {
                taskQueue.add(command);
                condition.signal();
            }
            else if (threadSet.size() < maximumPoolSize) {
                RunInfo runInfo = new RunInfo(false,command);
                Thread thread = new Thread(runInfo);
                threadSet.add(thread);
                thread.start();
            }
            else {
                throw new RejectedExecutionException();
            }
        }
        finally {
            lock.unlock();
        }
    }

    class RunInfo implements Runnable {

        private boolean isCoreThread;
        private Runnable firstTaskRunnable;

        RunInfo(boolean isCoreThread,Runnable runnable) {
            this.isCoreThread = isCoreThread;
            this.firstTaskRunnable = runnable;
        }

        @Override
        public void run() {
            //execute First Task
            if (firstTaskRunnable != null) {
                firstTaskRunnable.run();
                firstTaskRunnable = null;
            }

            // loop which get task from queue
            try {
                while (true) {
                    lock.lock();
                    while (taskQueue.isEmpty() || Thread.currentThread().isInterrupted()) {
                        if (shutdown)
                            return;
                        if (isCoreThread && !allowCoreThreadTimeOut) {
                            condition.await();
                        }
                        else {
                            // remove extra threads if time out exceeds
                            if (condition.await(keepAliveTime,unit)) {
                                return;
                            }
                        }
                    }
                    Runnable task = taskQueue.poll();
                    lock.unlock();
                    // execute task of queue
                    task.run();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                Thread thread = Thread.currentThread();
                threadSet.remove(thread);
                if (threadSet.size() == 0)
                    allThreadEnded.signalAll();
                lock.unlock();
            }
        }
    }

    class CallInfo<V> implements Runnable {

        boolean isCoreThread;
        Callable<V> callable;
        FutureImpl<V> futureImpl;

        CallInfo(boolean isCoreThread, Callable<V> callable, FutureImpl<V> futureImpl) {
            this.isCoreThread = isCoreThread;
            this.callable = callable;
            this.futureImpl = futureImpl;
        }

        @Override
        public void run() {
            if (callable != null) {
                try {
                    executeSendResultAndSignal(callable,futureImpl);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                callable = null;
            }
            try {
                while (true) {
                    lock.lock();
                    while (taskQueue.isEmpty() || Thread.currentThread().isInterrupted()) {
                        if (shutdown)
                            return;
                        if (isCoreThread && !allowCoreThreadTimeOut) {
                            condition.await();
                        }
                        else {
                            // remove extra threads if time out exceeds
                            if (condition.await(keepAliveTime,unit)) {
                                return;
                            }
                        }
                    }
                    Runnable task = taskQueue.poll();
                    lock.unlock();
                    // execute task of queue
                    task.run();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                Thread thread = Thread.currentThread();
                threadSet.remove(thread);
                if (threadSet.size() == 0)
                    allThreadEnded.signalAll();
                lock.unlock();
            }
        }
    }

    private <V> void executeSendResultAndSignal(Callable<V> callable,FutureImpl<V> futureImpl) throws Exception {
        futureImpl.futureLock.lock();
        if (futureImpl.isDone())
            return;
        try {
            futureImpl.result = callable.call();
            futureImpl.isDone = true;
            futureImpl.futureLockCondition.signal();
        }
        finally {
            futureImpl.futureLock.unlock();
        }
    }

    class CallInfoShort<V> implements Runnable {
        Callable<V> callable;
        FutureImpl<V> futureImpl;

        CallInfoShort(Callable<V> callable, FutureImpl<V> futureImpl) {
            this.callable = callable;
            this.futureImpl = futureImpl;
        }

        @Override
        public void run() {
            try {
                executeSendResultAndSignal(callable,futureImpl);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class FutureImpl<V> implements Future<V> {

        boolean isDone = false;
        boolean isCancelled = false;
        ReentrantLock futureLock = new ReentrantLock();
        Condition futureLockCondition = futureLock.newCondition();
        V result;

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            futureLock.lock();
            try {
                if (isCancelled)
                    return false;
                isCancelled = true;
                isDone = true;
                return false;
            }
            finally {
                futureLock.unlock();
            }
        }

        @Override
        public boolean isCancelled() {
            return isCancelled;
        }

        @Override
        public boolean isDone() {
            return isDone;
        }

        @Override
        public V get() throws InterruptedException, ExecutionException {
            futureLock.lock();
            try {
                if (isDone)
                    return result;
                futureLockCondition.await();
                return result;
            }
            finally {
                futureLock.unlock();
            }
        }

        @Override
        public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            futureLock.lock();
            try {
                if (isDone)
                    return result;
                futureLockCondition.await(timeout,unit);
                return result;
            }
            finally {
                futureLock.unlock();
            }
        }
    }

    private class BoundedQueue<E> {
        E arr[];
        int capacity;
        int size;
        int front;
        int rear;
        BoundedQueue(int capacity) {
            this.capacity = capacity;
            this.arr = (E[]) new Object[capacity];
            this.size = 0;
            this.front = 0;
            this.rear = 0;
        }

        public void add(E element) {
            arr[front] = element;
            front = (front + 1)%capacity;
            size++;
        }

        public E peek() {
            return arr[rear];
        }

        public E poll() {
            E head = arr[rear];
            rear = (rear + 1)%capacity;
            size--;
            return head;
        }

        public int size() {
            return size;
        }

        public boolean isEmpty() {
            return (size == 0);
        }

        public boolean isFull() {
            return (size == capacity);
        }
    }
}
