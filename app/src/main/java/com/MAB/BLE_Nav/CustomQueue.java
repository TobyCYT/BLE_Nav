package com.MAB.BLE_Nav;

import java.util.Objects;

public class CustomQueue {
    private final Integer front;
    private Integer rear;
    private static Integer capacity;
    private final Integer[] queue;

    CustomQueue(Integer c)
    {
        front = rear = 0;
        capacity = c;
        queue = new Integer[capacity];
    }

    // function to insert an element
    // at the rear of the queue
    public void queueEnqueue(Integer data)
    {
        // check queue is full or not
        if (Objects.equals(capacity, rear)) {
            this.queueDequeue();
        }
        queue[rear] = data;
        rear++;
    }

    //remove an element from the queue
    public void queueDequeue()  {
        // check if queue is empty
        if (!front.equals(rear)) {

            // shift elements to the right by one place up till rear

            if (rear - 1 >= 0) System.arraycopy(queue, 1, queue, 0, rear - 1);


            // set queue[rear] to 0
            if (rear < capacity)
                queue[rear] = 0;

            // decrement rear
            rear--;
        }
    }

    //clear queue
    public void clearQueue(){
        if (!front.equals(rear)){
            for (Integer i = 0; i < rear; i++){
                queueDequeue();
            }
        }
    }

    //apply filter
    public Double filtered(String x){
        Double avg = 0.0;
        Double kalman = 0.0;
        if (x.equals("mean")){
            if (rear == 0) return 0.0;

            for (Integer i = 0; i < rear; i++) {
                avg += queue[i];
            }
            avg /= rear;
            return avg;

        }else if(x.equals("kalman")){
            return kalman;
        }
        return -1.0;
    }

    //print all element
    public String printE(){
        if (rear == 0) return "Empty";
        StringBuilder e = new StringBuilder();
        for (Integer i = 0; i < rear; i++) {
            e.append(queue[i]).append(" ");
        }
        return e.toString();
    }
}
