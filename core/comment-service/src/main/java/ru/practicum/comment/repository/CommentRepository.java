package ru.practicum.comment.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.comment.dal.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    Page<Comment> findAllByEventIdAndApproved(Long eventId, Boolean approved, Pageable pageable);

    Page<Comment> findAllByAuthorId(Long userId, Pageable pageable);

    @Query("""
            SELECT c
            FROM Comment as c
            WHERE c.text ILIKE CONCAT('%', ?1, '%')
            """)
    Page<Comment> findByText(String text, Pageable pageable);
}