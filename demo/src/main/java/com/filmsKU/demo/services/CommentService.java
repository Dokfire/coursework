package com.filmsKU.demo.services;

import com.filmsKU.demo.models.Comment;
import com.filmsKU.demo.repos.CommentRepository;
import org.springframework.stereotype.Service;

@Service
public class CommentService {
    private final CommentRepository repository;

    public CommentService(CommentRepository repository) {
        this.repository = repository;
    }

    public void save(Comment comment) {
        repository.save(comment);
    }
}
