package com.filmsKU.demo.repos;

import com.filmsKU.demo.models.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}
