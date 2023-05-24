package com.filmsKU.demo.controllers;

import com.filmsKU.demo.DTOS.CommentDTO;
import com.filmsKU.demo.DTOS.FilmApiDTO;
import com.filmsKU.demo.DTOS.FilmFromApiDTO;
import com.filmsKU.demo.models.Comment;
import com.filmsKU.demo.models.Film;
import com.filmsKU.demo.models.Genre;
import com.filmsKU.demo.models.User;
import com.filmsKU.demo.services.CommentService;
import com.filmsKU.demo.services.FilmService;
import com.filmsKU.demo.services.GenreService;
import com.filmsKU.demo.services.UserService;
import jakarta.transaction.Transactional;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

@Controller
public class MainController {
    private final UserService userService;
    private final FilmService filmService;
    private final GenreService genreService;
    private final CommentService commentService;

    public MainController(UserService userService, FilmService filmService, GenreService genreService,
                          CommentService commentService) {
        this.userService = userService;
        this.filmService = filmService;
        this.genreService = genreService;
        this.commentService = commentService;
    }

    @GetMapping({"/", "/home"})
    public String homePage(Principal principal, Model model) {
        List<Film> filmList = filmService.findTop40();
        List<Genre> genresList = genreService.findAll();
        model.addAttribute("films", filmList);
        model.addAttribute("genres", genresList);
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/login-error")
    public String loginError(Model model) {
        model.addAttribute("loginError", true);
        return "login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("userData", new User());
        return "register";
    }

    @PostMapping("/register")
    public String createUser(Model model, @ModelAttribute(name = "userData") User user) {
        User foundPerson = userService.findByUsername(user.getUsername()).orElse(null);

        if (foundPerson != null || user.getUsername().length() < 4 || user.getPassword().length() < 4) {
            model.addAttribute("registerError", true);
            return "register";
        }

        userService.addUser(user);
        return "redirect:/login";
    }

    @GetMapping("/logout")
    public String logout() {
        return "logout";
    }

    @GetMapping("/accessDenied")
    public String accessDenied() {
        return "redirect:/";
    }

    @GetMapping("/film-page/{id}")
    public String getPageOfFilm(@PathVariable("id") long id, Model model, Principal principal) {
        List<Genre> genresList = genreService.findAll();
        User user = userService.findByUsername(principal.getName()).orElse(null);
        Film film = filmService.findById(id).orElse(null);
        if (user == null || film == null) {
            return "redirect:/";
        }
        List<CommentDTO> comments = new ArrayList<>();
        for (Comment c : film.getComments()
        ) {
            comments.add(
                CommentDTO.builder()
                    .username(userService.findById(c.getUserId()).orElse(new User()).getUsername())
                    .comment(c.getComment())
                    .build()
            );
        }

        model.addAttribute("isStarred", user.getStarredFilms().contains(film));
        model.addAttribute("isWatched", user.getWatchedFilms().contains(film));
        model.addAttribute("isLiked", user.getLikedFilms().contains(film));
        model.addAttribute("film", film);
        model.addAttribute("comments", comments);
        model.addAttribute("genres", genresList);
        return "film-page";
    }

    @GetMapping("/search")
    public String searchFilms(@RequestParam String search, Model model) {
        // TODO: ADD COMMENTS
        List<Genre> genresList = genreService.findAll();
        List<Film> films = filmService.findBySearch(search);
        model.addAttribute("films", films);
        model.addAttribute("genres", genresList);
        return "index";
    }

    @GetMapping("/films-by-genre/{id}")
    public String filmsByGenres(@PathVariable("id") Long id, Model model) {
        List<Genre> genresList = genreService.findAll();
        Genre genre = genreService.findById(id).orElse(null);
        if (genre == null) {
            return "redirect:/";
        }
        List<Film> films = filmService.findByGenre(genre);
        if (films == null) {
            return "redirect:/";
        }
        model.addAttribute("films", films);
        model.addAttribute("genres", genresList);
        return "index";
    }

    @GetMapping("/api/get-new-films")
    public String searchFilms(@RequestParam String page) {
        //https://image.tmdb.org/t/p/w780/posterLink
        String uri =
            "https://api.themoviedb.org/3/movie/popular?api_key=5ab23f336df091b003798f4db9bffd0b&language=en-US&page=" +
                page;
        RestTemplate restTemplate = new RestTemplate();
        FilmFromApiDTO filmsResponse = restTemplate.getForObject(uri, FilmFromApiDTO.class);

        List<Film> filmList = new ArrayList<>(20);
        if (filmsResponse == null || filmsResponse.getResults() == null) {
            return "redirect:/";
        }
        for (FilmApiDTO dto : filmsResponse.getResults()
        ) {
            if (!dto.isAdult()) {
                Film convertedFilm = Film.builder()
                    .genres(genreService.findAllByIdsIn(dto.getGenre_ids()))
                    .name(dto.getTitle())
                    .description(dto.getOverview())
                    .urlToImage("https://image.tmdb.org/t/p/w780/" + dto.getPoster_path())
                    .build();
                if (filmService.findBySearch(convertedFilm.getName()).isEmpty()) {
                    filmList.add(convertedFilm);
                }
            }
        }
        filmService.saveAll(filmList);
        return "redirect:/";
    }

    @PostMapping("/add-comment")
    @Transactional
    public String addComment(@RequestParam String comment, @RequestParam Long filmId, Model model,
                             Principal principal) {
        User user = userService.findByUsername(principal.getName()).orElse(null);
        Film film = filmService.findById(filmId).orElse(null);
        if (user == null || film == null) {
            return "redirect:/film-page/" + filmId;
        }
        Comment newComment = Comment.builder()
            .comment(comment)
            .userId(user.getId())
            .build();
        commentService.save(newComment);

        film.addComment(newComment);

        filmService.save(film);

        model.addAttribute("film", film);
        return "redirect:/film-page/" + filmId;
    }

    @PostMapping("/update-film")
    public String updateFilm(Film film, Model model,
                             Principal principal) {
        Film filmFromDb = filmService.findById(film.getId()).orElse(null);
        if (filmFromDb == null) {
            return "redirect:/film-page/" + film.getId();
        }
        filmFromDb.setName(film.getName());
        filmFromDb.setDescription(film.getDescription());
        filmFromDb.setGenres(film.getGenres());
        filmFromDb.setUrlToImage(film.getUrlToImage());

        filmService.save(filmFromDb);

        model.addAttribute("film", film);
        return "redirect:/film-page/" + film.getId();
    }

    @PostMapping("/star-film")
    public String starFilm(@RequestParam Long filmIdStarred, Principal principal) {
        User user = userService.findByUsername(principal.getName()).orElse(null);
        Film film = filmService.findById(filmIdStarred).orElse(null);
        if (user == null || film == null) {
            return "redirect:/";
        }
        if (user.getStarredFilms().contains(film)) {
            user.removeStarredFilm(film);
        } else {
            user.addStarredFilm(film);
        }
        userService.saveUser(user);

        return "redirect:/film-page/" + film.getId();
    }

    @PostMapping("/watched-film")
    public String watchedFilm(@RequestParam Long filmIdWatched, Principal principal) {
        User user = userService.findByUsername(principal.getName()).orElse(null);
        Film film = filmService.findById(filmIdWatched).orElse(null);
        if (user == null || film == null) {
            return "redirect:/";
        }
        if (user.getWatchedFilms().contains(film)) {
            user.removeWatchedFilm(film);
        } else {
            user.addWatchedFilm(film);
        }
        userService.saveUser(user);

        return "redirect:/film-page/" + film.getId();
    }

    @PostMapping("/like-film")
    public String likeFilm(@RequestParam Long filmIdLiked, Principal principal) {
        User user = userService.findByUsername(principal.getName()).orElse(null);
        Film film = filmService.findById(filmIdLiked).orElse(null);
        if (user == null || film == null) {
            return "redirect:/";
        }
        if (user.getLikedFilms().contains(film)) {
            user.removeLikedFilm(film);
        } else {
            user.addLikedFilm(film);
        }
        userService.saveUser(user);

        return "redirect:/film-page/" + film.getId();
    }

    @PostMapping("/add-film")
    public String addFilm(@RequestParam String name, @RequestParam String description, @RequestParam String urlToImage,
                          Principal principal) {
        if (name.isEmpty() || description.isEmpty() || urlToImage.isEmpty()) {
            return "redirect:/";
        }
        Film film = Film.builder()
            .name(name)
            .description(description)
            .urlToImage(urlToImage)
            .build();
        filmService.save(film);

        Long filmId = filmService.findBySearch(name).get(0).getId();
        return "redirect:/film-page/" + filmId;
    }

    @PostMapping("/delete-film/{id}")
    public String deleteFilm(@PathVariable("id") long id) {
        filmService.findById(id).ifPresent(filmService::delete);
        return "redirect:/";
    }

    @GetMapping("/already-watched-films")
    public String watchedFilms(Principal principal, Model model) {
        User user = userService.findByUsername(principal.getName()).orElse(null);
        if(user == null || user.getWatchedFilms().isEmpty()){
            return "index";
        }
        model.addAttribute("films", user.getWatchedFilms());
        return "index";
    }

    @GetMapping("/starred-films")
    public String starredFilms(Principal principal, Model model) {
        User user = userService.findByUsername(principal.getName()).orElse(null);
        if(user == null || user.getStarredFilms().isEmpty()){
            return "index";
        }
        model.addAttribute("films", user.getStarredFilms());
        return "index";
    }

    @GetMapping("/liked-films")
    public String likedFilms(Principal principal, Model model) {
        User user = userService.findByUsername(principal.getName()).orElse(null);
        if(user == null || user.getLikedFilms().isEmpty()){
            return "index";
        }
        model.addAttribute("films", user.getLikedFilms());
        return "index";
    }

}
