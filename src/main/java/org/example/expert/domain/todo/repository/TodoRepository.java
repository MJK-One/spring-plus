package org.example.expert.domain.todo.repository;

import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

public interface TodoRepository extends JpaRepository<Todo, Long>, TodoQueryRepository {


    @Query("SELECT t FROM Todo t " +
            "LEFT JOIN FETCH t.user " +
            "WHERE (:weather IS NULL OR t.weather LIKE %:weather%)" +
            "AND (:startDay IS NULL OR DATE_FORMAT(t.modifiedAt, '%Y-%m-%d') >= :startDay)" +
            "AND (:endDay IS NULL OR DATE_FORMAT(t.modifiedAt, '%Y-%m-%d') <= :endDay)" +
            "ORDER BY t.modifiedAt DESC")
    Page<Todo> findAllByOrderByModifiedAtDesc(
            @Param("weather") String weather,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable
    );
}
