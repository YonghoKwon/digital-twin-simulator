package com.dt.digitaltwinsimulator.logic;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TaskCancellationLogicTest {

    @Test
    void requestCancellationDoesNotCreateMissingTask() {
        TaskCancellationLogic logic = new TaskCancellationLogic();

        boolean requested = logic.requestCancellation("missing-task");

        assertThat(requested).isFalse();
        assertThat(logic.getRunningTaskIds()).isEmpty();
    }

    @Test
    void requestCancellationMarksExistingTask() {
        TaskCancellationLogic logic = new TaskCancellationLogic();
        logic.registerTask("task-1");

        boolean requested = logic.requestCancellation("task-1");

        assertThat(requested).isTrue();
        assertThat(logic.isCancellationRequested("task-1")).isTrue();
    }

    @Test
    void requestAllCancellationMarksAllTasks() {
        TaskCancellationLogic logic = new TaskCancellationLogic();
        logic.registerTask("task-1");
        logic.registerTask("task-2");

        int count = logic.requestAllCancellation();

        assertThat(count).isEqualTo(2);
        assertThat(logic.isCancellationRequested("task-1")).isTrue();
        assertThat(logic.isCancellationRequested("task-2")).isTrue();
    }
}
