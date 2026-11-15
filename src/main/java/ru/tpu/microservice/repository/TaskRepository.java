package ru.tpu.microservice.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.tpu.microservice.domain.Task;
import ru.tpu.microservice.domain.TaskStatus;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findAllByStatus(TaskStatus status, Sort sort);
}
