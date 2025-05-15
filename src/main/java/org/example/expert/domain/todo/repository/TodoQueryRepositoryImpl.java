package org.example.expert.domain.todo.repository;

import static org.example.expert.domain.manager.entity.QManager.manager;
import static org.example.expert.domain.todo.entity.QTodo.todo;
import static org.example.expert.domain.user.entity.QUser.user;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.example.expert.domain.todo.dto.request.TodoSearchRequest;
import org.example.expert.domain.todo.dto.response.TodoSearchResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

@RequiredArgsConstructor
public class TodoQueryRepositoryImpl implements TodoQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<Todo> findByIdWithUser(Long todoId) {
        Todo result = queryFactory
                .selectFrom(todo)
                .leftJoin(todo.user, user).fetchJoin()
                .where(todo.id.eq(todoId))
                .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public Page<TodoSearchResponse> searchTodos(Pageable pageable, TodoSearchRequest request) {

        var query = query(request)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        var todos = query.fetch();
        long totalSize = countQuery(request).fetch().get(0);

        return PageableExecutionUtils.getPage(todos, pageable, () -> totalSize);
    }

    private JPAQuery<TodoSearchResponse> query(TodoSearchRequest request) {
        return queryFactory.select(
                        Projections.constructor(
                                TodoSearchResponse.class,
                                todo.title,
                                todo.managers.size(),
                                todo.comments.size()
                        ))
                .from(todo)
                .leftJoin(todo.managers, manager)
                .leftJoin(manager.user)
                .leftJoin(todo.comments)
                .where(
                        titleContains(request.getTitle()),
                        startDayGoe(request.getStart()),
                        endDayLt(request.getEnd()),
                        nicknameContains(request.getNickname())
                )
                .groupBy(todo.id)
                .orderBy(todo.modifiedAt.desc());
    }

    private BooleanExpression titleContains(String title) {
        return Objects.nonNull(title) ? todo.title.contains(title) : null;
    }

    private BooleanExpression startDayGoe(LocalDate startDay) {
        return Objects.nonNull(startDay) ? todo.modifiedAt.goe(startDay.atStartOfDay()) : null;
    }

    private BooleanExpression endDayLt(LocalDate endDay) {
        return Objects.nonNull(endDay) ? todo.modifiedAt.lt(endDay.plusDays(1).atStartOfDay()) : null;
    }

    private BooleanExpression nicknameContains(String nickname) {
        return Objects.nonNull(nickname) ? manager.user.nickname.contains(nickname) : null;
    }

    private JPAQuery<Long> countQuery(TodoSearchRequest request) {
        return queryFactory.select(Wildcard.count)
                .from(todo)
                .leftJoin(todo.managers, manager)
                .leftJoin(manager.user)
                .where(
                        titleContains(request.getTitle()),
                        startDayGoe(request.getStart()),
                        endDayLt(request.getEnd()),
                        nicknameContains(request.getNickname())
                );
    }
}