package com.mikea.gae.rx;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;

import java.io.IOException;
import java.io.Serializable;

/**
 * @author mike.aizatsky@gmail.com
 */
public class RxTasks {
    public static <T extends Serializable> void enqueue(String queueName, RxTask<T> value) throws IOException {
        Queue queue = QueueFactory.getQueue(queueName);
        TaskOptions taskOptions = TaskOptions.Builder.withUrl(RxModule.RX_TASKS_URL_BASE).payload(
                value.toPayLoad()
        );
        queue.add(taskOptions);
    }
}
