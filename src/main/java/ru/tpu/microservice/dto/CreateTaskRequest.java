package ru.tpu.microservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import ru.tpu.microservice.domain.TaskPriority;
import ru.tpu.microservice.domain.TaskStatus;

public record CreateTaskRequest(

        @NotBlank(message = "Поле 'title' обязательно и не может быть пустым")
        @Size(max = 120, message = "Поле 'title' не должно превышать 120 символов")
        String title,

        @Size(max = 1000, message = "Поле 'description' не должно превышать 1000 символов")
        String description,

        TaskStatus status,

        TaskPriority priority
) {
}
