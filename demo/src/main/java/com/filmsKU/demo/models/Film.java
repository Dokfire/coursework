package com.filmsKU.demo.models;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "films")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class Film {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name; // title
    @Column(length = 1024)
    private String description; // overview
    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    @JoinTable(name = "films_genres",
        joinColumns = @JoinColumn(name = "film_id"),
        inverseJoinColumns = @JoinColumn(name = "genre_id"))
    private List<Genre> genres = new ArrayList<>(); // genres_id

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    @JoinTable(name = "films_comments",
        joinColumns = @JoinColumn(name = "film_id"),
        inverseJoinColumns = @JoinColumn(name = "comment_id"))
    private List<Comment> comments = new ArrayList<>(); // genres_id
    @Column(length = 1024)
    private String urlToImage; // poster_path

    public String getShortDescription() {
        if (description == null || description.isEmpty()) {
            return "";
        }
        if (description.length() <= 125) {
            return description;
        } else {
            return description.substring(0, 125) + "...";
        }
    }

    public String getShortGenres() {
        if (genres.isEmpty()) {
            return "No genres";
        }
        StringBuilder sb = new StringBuilder();
        if (genres.size() <= 2) {
            for (Genre g : genres
            ) {
                sb.append(g.getName()).append(" ");
            }
        } else {
            for (int i = 0; i < 3; i++) {
                sb.append(genres.get(i).getName()).append(" ");
            }
        }
        return sb.toString();
    }

    public void addComment(Comment comment) {
        comments.add(comment);
    }
}
