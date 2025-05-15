package org.example.expert.domain.todo.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class TodoSearchRequest {
    private String title;
    private LocalDate start;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDate end;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private String nickname;
}
