package ru.practicum.comment.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.comment.dto.CommentCountDto;
import ru.practicum.comment.model.Comment;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    Page<Comment> findAllByEventId(Long eventId, Pageable pageable);

    @Query("select new ru.practicum.comment.dto.CommentCountDto(c.event.id, count(c.id)) " +
            "from Comment as c where c.event.id in (:listEventId) group by c.event.id")
    List<CommentCountDto> findAllCommentCount(@Param("listEventId") List<Long> listEventId);

    @Query("select c from Comment as c where lower(c.text) like %:text%")
    Page<Comment> findAllByText(@Param("text") String text, Pageable pageable);

    Page<Comment> findAllByAuthorId(Long userId, Pageable pageable);

    Optional<Comment> findByEventIdAndId(Long eventId, Long commentId);

}

//public interface CommentRepository extends JpaRepository<Comment, Long> {
//
//    Page<Comment> findAllByEventId(Long eventId, Pageable pageable);
//
//    @Query("select new ru.practicum.comment.dto.CommentCountDto(c.event.id, count(c.id)) " +
//            "from Comment as c " +
//            "where c.event.id in ?1 " +
//            "group by c.event.id")
//    List<CommentCountDto> findAllCommentCount(List<Long> listEventId);
//
//    @Query("select c " +
//            "from Comment as c " +
//            "where c.text ilike concat('%', ?1, '%')")
//    Page<Comment> findAllByText(String text, Pageable pageable);
//
//    Page<Comment> findAllByAuthorId(Long userId, Pageable pageable);
//
//    Optional<Comment> findByEventIdAndId(Long eventId, Long commentId);
//
//}