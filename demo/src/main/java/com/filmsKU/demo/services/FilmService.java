package com.filmsKU.demo.services;

import com.filmsKU.demo.models.Film;
import com.filmsKU.demo.models.Genre;
import com.filmsKU.demo.models.User;
import com.filmsKU.demo.repos.FilmRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class FilmService {
    private final FilmRepository repository;
    private final UserService userService;

    public FilmService(FilmRepository repository, UserService userService) {
        this.repository = repository;
        this.userService = userService;
    }

    public List<Film> findAll() {
        return repository.findAll();
    }

    public Optional<Film> findById(Long id) {
        return repository.findById(id);
    }

    public List<Film> findBySearch(String search) {
        return repository.findAllByNameContainsIgnoreCase(search);
    }

    public List<Film> findTop40() {
        return repository.findAll();
    }

    public List<Film> findByGenre(Genre genres) {
        return repository.findAllByGenresContaining(genres);
    }

    public void saveAll(List<Film> films) {
        repository.saveAll(films);
    }

    public void save(Film film) {
        repository.save(film);
    }

    @Transactional
    public void delete(Film film) {
        List<User> userList = userService.findAll();
        for (User u : userList) {
            u.removeWatchedFilm(film);
            u.removeStarredFilm(film);
            u.removeLikedFilm(film);
            userService.saveUser(u);
        }
        repository.delete(film);
    }
}
