package org.example.chapter07.스레드간협력;

import java.util.LinkedList;
import java.util.Queue;

public class ProducerConsumerExample {

    public static void main(String[] args) {
        SharedQueue queue = new SharedQueue();

        new Thread(() -> {
            for (int i = 0; i < 20; i++) {
                try {
                    queue.produce(i);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }, "생산자").start();

        new Thread(() -> {
            for (int i = 0; i < 20; i++) {
                try {
                    queue.consume();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }, "소비자").start();
    }
}


class SharedQueue {

    private Queue<Integer> queue = new LinkedList<>();
    private final int CAPACITY = 5;
    private final Object lock = new Object(); // 모니터. (뮤텍스 락이자 condition variable)

    // synchronized 메서드나 블럭 둘 다 가능. 이번에는 블럭으로 진행한다.
    public void produce(int item) throws InterruptedException {
        // queue에 대한 배타적 접근
        synchronized (lock) {
            while (queue.size() == CAPACITY) {
                System.out.println("큐가 가득 찼습니다. 생산 중지.");
                // 나머지 임계 영역을 수행할 조건이 될 때까지 TCB가 Waiting 상태로 전환되고 Wait Set에 등록됨
                // 락을 반환함
                lock.wait();
            }
            queue.add(item);
            System.out.println("생산 : " + item);
            // lock condition variable에 대해 Waiting 상태에 있는 스레드 모두를 Entry Set으로 등록시키고 Blocked 상태로 전환시킴
            lock.notifyAll();
        }
    }

    public void consume() throws InterruptedException {
        // queue에 대한 배타적 접근
        synchronized (lock) {
            while (queue.isEmpty()) {
                System.out.println("큐가 비어 있습니다. 소비 중지.");
                // 나머지 임계 영역을 수행할 조건이 될 때까지 TCB가 Waiting 상태로 전환되고 Wait Set에 등록됨
                // 락을 반환함
                lock.wait();
            }
            Integer item = queue.poll();
            System.out.println("소비 : " + item);
            // lock condition variable에 대해 Waiting 상태에 있는 스레드 모두를 Entry Set으로 등록시키고 Blocked 상태로 전환시킴
            lock.notifyAll();
        }
    }
}
