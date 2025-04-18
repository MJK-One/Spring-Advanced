package org.example.expert.domain.todo.repository;

import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TodoRepository extends JpaRepository<Todo, Long> {
    // N+1 문제 getTodos
    @EntityGraph(attributePaths = "user")
    Page<Todo> findAllByOrderByModifiedAtDesc(Pageable pageable);

    // N+1 문제 getTodo
    @EntityGraph(attributePaths = "user")
    @Query("SELECT t FROM Todo t where t.id = :todoId")
    Optional<Todo> findByIdWithUser(@Param("todoId") Long todoId);

    int countById(Long todoId);
}
