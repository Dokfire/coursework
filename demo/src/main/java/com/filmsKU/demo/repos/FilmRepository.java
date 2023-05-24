package com.filmsKU.demo.repos;

import com.filmsKU.demo.models.Film;
import com.filmsKU.demo.models.Genre;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FilmRepository extends JpaRepository<Film, Long> {

    List<Film> findAllByNameContainsIgnoreCase(String search);

    List<Film> findAllByGenresContaining(Genre genres);
}
