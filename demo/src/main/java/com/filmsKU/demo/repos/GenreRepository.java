package com.filmsKU.demo.repos;

import com.filmsKU.demo.models.Genre;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GenreRepository extends JpaRepository<Genre, Long> {
    List<Genre> findAllByIdIn(List<Long> ids);
}
