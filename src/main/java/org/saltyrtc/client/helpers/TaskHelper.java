/*
 * Copyright (c) 2016 Threema GmbH / SaltyRTC Contributors
 *
 * Licensed under the Apache License, Version 2.0, <see LICENSE-APACHE file>
 * or the MIT license <see LICENSE-MIT file>, at your option. This file may not be
 * copied, modified, or distributed except according to those terms.
 */

package org.saltyrtc.client.helpers;

import org.saltyrtc.client.tasks.Task;

import java.util.ArrayList;
import java.util.List;

/**
 * Utilities for working with tasks.
 */
public class TaskHelper {

    /**
     * Iterate over all registered tasks, return list of task names.
     */
    public static List<String> getTaskNames(Task[] tasks) {
        // Sane languages could do this by calling .map
        // with an anonymous function, but unfortunately Java ain't :(
        List<String> taskNames = new ArrayList<>();
        for (Task task : tasks) {
            taskNames.add(task.getName());
        }
        return taskNames;
    }

    /**
     * Choose the first task in our own list of supported tasks that is also contained in the list
     * of supported tasks provided by the peer.
     *
     * @return The selected task, or null if no common task can be found.
     */
    public static Task chooseCommonTask(Task[] ourTasks, List<String> theirTasks) {
        for (Task task : ourTasks) {
            if (theirTasks.contains(task.getName())) {
                return task;
            }
        }
        return null;
    }

}
