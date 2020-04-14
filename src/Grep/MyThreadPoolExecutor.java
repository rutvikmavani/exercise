package Grep;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MyThreadPoolExecutor implements ExecutorService {

    private class ExecutorThreadRunnable implements Runnable {

        @Override
        public void run() {
            try {
                while (true) {
                    lock.lock();

                    while (taskQueue.size() == 0) {
                        if (shutDown) {
                            lock.unlock();
                            return;
                        }
                        empty.await();
                    }

                    Runnable task = taskQueue.poll();

                    lock.unlock();
                    task.run();
                }
            }
            catch (InterruptedException e) {

            }
        }
    }

    private Queue<Runnable> taskQueue;
    private ExecutorThreadRunnable executorThreadRunnable[];
    private Thread threads[];
    private Lock lock = new ReentrantLock();    // multiple producer
    private Condition empty = lock.newCondition();
    private volatile boolean shutDown = false;


    MyThreadPoolExecutor(int numberOfThreads) {
        taskQueue = new ArrayDeque<>(100000);

        executorThreadRunnable = new ExecutorThreadRunnable[numberOfThreads];
        threads = new Thread[numberOfThreads];

        for(int i=0;i<numberOfThreads;i++) {
            executorThreadRunnable[i] = new ExecutorThreadRunnable();
            threads[i] = new Thread(executorThreadRunnable[i]);
            threads[i].start();
        }
    }

    @Override
    public void shutdown() {
        lock.lock();
        if (shutDown)
            return;
        shutDown = true;
        empty.signalAll();
        lock.unlock();
    }

    @Override
    public List<Runnable> shutdownNow() {
        return null;
    }

    @Override
    public boolean isShutdown() {
        return shutDown;
    }

    @Override
    public boolean isTerminated() {

        for(int i=0;i<threads.length;i++) {
            if (threads[i].getState() != Thread.State.TERMINATED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return false;
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return null;
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
        if (command == null)
            throw new NullPointerException();
        lock.lock();
        if (shutDown)
            throw new RejectedExecutionException();
        taskQueue.add(command);
        empty.signal();
        lock.unlock();
    }
}
