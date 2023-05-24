package com.filmsKU.demo.services;

import com.filmsKU.demo.models.Genre;
import com.filmsKU.demo.repos.GenreRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class GenreService {
    private final GenreRepository repository;

    public GenreService(GenreRepository repository) {
        this.repository = repository;
    }

    public List<Genre> findAllByIdsIn(List<Long> ids) {
        return repository.findAllByIdIn(ids);
    }

    public List<Genre> findAll() {
        return repository.findAll();
    }

    public Optional<Genre> findById(Long id) {
        return repository.findById(id);
    }
}
