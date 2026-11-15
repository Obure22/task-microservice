package ru.tpu.microservice.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import ru.tpu.microservice.domain.TaskPriority;
import ru.tpu.microservice.domain.TaskStatus;

public record PatchTaskRequest(

        @Pattern(regexp = ".*\\S.*", message = "Поле 'title' не может быть пустым")
        @Size(max = 120, message = "Поле 'title' не должно превышать 120 символов")
        String title,

        @Size(max = 1000, message = "Поле 'description' не должно превышать 1000 символов")
        String description,

        TaskStatus status,

        TaskPriority priority
) {
}
