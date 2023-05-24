package com.filmsKU.demo.dataLoader;

import com.filmsKU.demo.DTOS.GenresDTO;
import com.filmsKU.demo.models.Film;
import com.filmsKU.demo.models.Role;
import com.filmsKU.demo.models.User;
import com.filmsKU.demo.repos.FilmRepository;
import com.filmsKU.demo.repos.GenreRepository;
import com.filmsKU.demo.repos.RoleRepository;
import com.filmsKU.demo.repos.UserRepository;
import jakarta.transaction.Transactional;
import java.util.Set;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class DataLoader implements ApplicationRunner {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final FilmRepository filmRepository;
    private final GenreRepository genreRepository;

    public DataLoader(RoleRepository roleRepository, UserRepository userRepository, FilmRepository filmRepository,
                      GenreRepository genreRepository) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.filmRepository = filmRepository;
        this.genreRepository = genreRepository;
    }

    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.findAll().size() == 0 || roleRepository.findAll().size() < 2) {
            roleRepository.save(new Role("ROLE_MANAGER"));
            roleRepository.save(new Role("ROLE_USER"));

            User defaultUserManager = new User();
            defaultUserManager.setUsername("admin");
            defaultUserManager.setPassword("$2a$10$9iAsr0jJ3g4JP1yXJUkkpOAcD2jLioMIQ8l0WlX4x1fg33bUK61FG");

            defaultUserManager.setRoles(Set.of(roleRepository.findByName("ROLE_MANAGER")));
            userRepository.save(defaultUserManager);

            String uri =
                "https://api.themoviedb.org/3/genre/movie/list?api_key=5ab23f336df091b003798f4db9bffd0b&language=en-US";
            RestTemplate restTemplate = new RestTemplate();
            GenresDTO genresDTO = restTemplate.getForObject(uri, GenresDTO.class);
            genreRepository.saveAll(genresDTO.getGenres());

            Film film = Film.builder()
                .name("Basic film")
                .description("Basic description")
                .urlToImage("https://image.tmdb.org/t/p/w780//r2J02Z2OpNTctfOSN1Ydgii51I3.jpg")
                .build();
            filmRepository.save(film);
        }
    }
}