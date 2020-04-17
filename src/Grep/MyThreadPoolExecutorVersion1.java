package Grep;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


// only fixed thread number of threads are created
public class MyThreadPoolExecutorVersion1 implements ExecutorService {

    private class ExecutorThreadRunnable implements Runnable {

        @Override
        public void run() {
            terminated.lock();
            activeThreads++;
            terminated.unlock();
            try {
                while (true) {
                    lock.lock();

                    while (taskQueue.size() == 0 || Thread.currentThread().isInterrupted()) {
                        if (shutDown) {
                            return;
                        }
                        empty.await();
                    }

                    Runnable task = taskQueue.poll();

                    lock.unlock();
                    task.run();
                }
            }
            catch (Exception e) {

            }
            finally {
                terminated.lock();
                activeThreads--;
                terminated.unlock();
                lock.unlock();
            }
        }
    }

    private Queue<Runnable> taskQueue;
    private ExecutorThreadRunnable executorThreadRunnable[];
    private Thread threads[];

    private Lock lock = new ReentrantLock();    // multiple producer
    private Condition empty = lock.newCondition();
    private volatile boolean shutDown = false;

    private volatile int activeThreads = 0;

    private Lock terminated = new ReentrantLock();
    private Condition allDone = terminated.newCondition();


    MyThreadPoolExecutorVersion1(int numberOfThreads) {
        taskQueue = new LinkedList<>();

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
        try {
            if (shutDown)
                return;
            shutDown = true;
            empty.signalAll();
        }
        finally {
            lock.unlock();
        }
    }

    @Override
    public List<Runnable> shutdownNow() {
        lock.lock();
        try {
            if (shutDown)
                return null;
            shutDown = true;
            for (Thread thread : threads) {
                thread.interrupt();
            }
            List<Runnable> list = new ArrayList<>();
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
        return shutDown;
    }

    @Override
    public boolean isTerminated() {
        return activeThreads == 0;
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        terminated.lock();
        if (activeThreads == 0)
            return true;
        try {
            return allDone.await(timeout, unit);
        }
        finally {
            terminated.unlock();
        }
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
