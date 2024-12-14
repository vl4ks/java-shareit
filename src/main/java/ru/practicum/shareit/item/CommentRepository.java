package ru.practicum.shareit.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.item.model.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByItemId(Long itemId);

    @Query("select c from Comment c " +
            "where c.item.id in " +
            "(select i.id from Item i " +
            "where i.owner.id = :ownerId)")
    List<Comment> findCommentsByOwnerId(@Param("ownerId") Long ownerId);

}

