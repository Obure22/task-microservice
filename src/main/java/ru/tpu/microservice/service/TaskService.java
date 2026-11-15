package ru.tpu.microservice.service;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.tpu.microservice.domain.Task;
import ru.tpu.microservice.domain.TaskPriority;
import ru.tpu.microservice.domain.TaskStatus;
import ru.tpu.microservice.dto.CreateTaskRequest;
import ru.tpu.microservice.dto.PatchTaskRequest;
import ru.tpu.microservice.dto.TaskResponse;
import ru.tpu.microservice.dto.UpdateTaskRequest;
import ru.tpu.microservice.exception.TaskNotFoundException;
import ru.tpu.microservice.repository.TaskRepository;

import java.util.List;

@Service
public class TaskService {

    private final TaskRepository repository;

    public TaskService(TaskRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> findAll(TaskStatus status) {
        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        List<Task> tasks = status == null
                ? repository.findAll(sort)
                : repository.findAllByStatus(status, sort);

        return tasks.stream().map(TaskResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public TaskResponse findById(Long id) {
        return TaskResponse.from(getTask(id));
    }

    @Transactional
    public TaskResponse create(CreateTaskRequest request) {
        Task task = new Task();
        task.setTitle(request.title().trim());
        task.setDescription(request.description());
        task.setStatus(request.status() == null ? TaskStatus.NEW : request.status());
        task.setPriority(request.priority() == null ? TaskPriority.MEDIUM : request.priority());

        return TaskResponse.from(repository.saveAndFlush(task));
    }

    @Transactional
    public TaskResponse update(Long id, UpdateTaskRequest request) {
        Task task = getTask(id);
        task.setTitle(request.title().trim());
        task.setDescription(request.description());
        task.setStatus(request.status() == null ? TaskStatus.NEW : request.status());
        task.setPriority(request.priority() == null ? TaskPriority.MEDIUM : request.priority());

        return TaskResponse.from(repository.saveAndFlush(task));
    }

    @Transactional
    public TaskResponse patch(Long id, PatchTaskRequest request) {
        Task task = getTask(id);

        if (request.title() != null) {
            task.setTitle(request.title().trim());
        }
        if (request.description() != null) {
            task.setDescription(request.description());
        }
        if (request.status() != null) {
            task.setStatus(request.status());
        }
        if (request.priority() != null) {
            task.setPriority(request.priority());
        }

        return TaskResponse.from(repository.saveAndFlush(task));
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(getTask(id));
    }

    private Task getTask(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
    }
}
