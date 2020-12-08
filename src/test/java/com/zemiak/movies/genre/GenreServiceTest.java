package com.zemiak.movies.genre;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.List;

import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.core.Response.Status;

import com.zemiak.movies.AssuredRequests;
import com.zemiak.movies.MagicNumbers;
import com.zemiak.movies.strings.DateFormatter;
import com.zemiak.movies.ui.GuiDTO;

public class GenreServiceTest {
    AssuredRequests req;

    public GenreServiceTest() {
        req = new AssuredRequests();
    }

    // @Test
    public void exists() {
        List<Genre> genres = req.get("/genres/paged?page=0&pageSize=10").jsonPath().getList("$", Genre.class);
        assertFalse(genres.isEmpty(), "Genres are not empty");
    }

    // @Test
    public void create() {
        JsonObject genre = getHelloWorldGenre();
        Long id = req.post("/genres", genre).as(Long.class);
        assertTrue(null != id, "Create genre returns ID");

        Genre entity = req.get("/genres/" + String.valueOf(id)).jsonPath().getObject("$", Genre.class);

        String actualDate = DateFormatter.format(entity.created);

        assertEquals(id, entity.id, "Genre ID must be the same as created");
        assertEquals(genre.getString("name"), entity.name, "Name must be the same as created");
        assertEquals(genre.getString("created"), actualDate, "created must be the same as created");
        assertEquals(genre.getString("pictureFileName"), entity.pictureFileName,
                "pictureFileName must be the same as created");
    }

    // @Test
    public void find() {
        Long id = MagicNumbers.GENRE_NONE;
        Genre entity = req.get("/genres/" + String.valueOf(id)).jsonPath().getObject("$", Genre.class);
        assertEquals(id, entity.id, "Genre ID must be the same as specified");
        assertEquals("None", entity.name, "Name must be None");
    }

    // @Test
    public void remove() {
        JsonObject genre = getHelloWorldGenre();

        Long id = req.post("/genres", genre).as(Long.class);
        assertTrue(null != id, "Create genre returns ID");

        req.delete("/genres/" + String.valueOf(id), Status.NO_CONTENT.getStatusCode());
        req.get("/genres/" + String.valueOf(id), Status.NOT_FOUND.getStatusCode());
    }

    private JsonObject getHelloWorldGenre() {
        return Json.createObjectBuilder().add("name", "Hello, World").add("fileName", "hello-world.m4v")
                .add("created", DateFormatter.format(LocalDateTime.now().minusYears(20)))
                .add("protectedGenre", MagicNumbers.GENRE_NONE)
                .add("pictureFileName", "hello-world.jpg").build();
    }

    // @Test
    public void update() {
        Long id = MagicNumbers.GENRE_NONE;
        Genre entity = req.get("/genres/" + String.valueOf(id)).jsonPath().getObject("$", Genre.class);
        assertEquals(id, entity.id, "Genre ID must be the same as specified");
        assertEquals("None", entity.name, "Name must be None");

        entity.name = "Some";
        req.put("/genres", entity.toJson(), Status.NO_CONTENT.getStatusCode());

        entity = req.get("/genres/" + String.valueOf(id)).jsonPath().getObject("$", Genre.class);
        assertEquals("Some", entity.name, "Updated name must be: Some");

        entity.name = "None";
        req.put("/genres", entity.toJson(), Status.NO_CONTENT.getStatusCode());
    }

    // @Test
    public void search() throws UnsupportedEncodingException {
        String text = "On";
        List<GuiDTO> genres = req.get("/genres/search/" + URLEncoder.encode(text, "UTF-8")).jsonPath().getList("$",
                GuiDTO.class);
        assertFalse(genres.isEmpty());
        assertEquals("None", genres.get(0).title, "One None should be found");
    }

    // @Test
    public void createMustFailIfIDIsNotEmpty() {
        JsonObject genre = Json.createObjectBuilder().add("id", 42).add("name", "Hello, World")
                .add("fileName", "hello-world.m4v")
                .add("created", DateFormatter.format(LocalDateTime.now().minusYears(20)))
                .add("pictureFileName", "hello-world.jpg").build();
        req.post("/genres", genre, Status.NOT_ACCEPTABLE.getStatusCode());
    }

    // @Test
    public void updateMustFailIfIDIsEmpty() {
        JsonObject genre = getHelloWorldGenre();
        req.put("/genres", genre, Status.NOT_ACCEPTABLE.getStatusCode());
    }

    // @Test
    public void findMustFailIfEntityDoesNotExist() {
        Long id = 42000l;
        req.get("/genres/" + String.valueOf(id), Status.NOT_FOUND.getStatusCode());
    }

    // @Test
    public void deleteMustFailIfEntityDoesNotExist() {
        Long id = 42000l;
        req.delete("/genres/" + String.valueOf(id), Status.NOT_FOUND.getStatusCode());
    }

    // @Test
    public void updateMustFailIfEntityDoesNotExist() {
        Long id = 42000l;
        JsonObject genre = Json.createObjectBuilder().add("id", id).add("name", "Hello, World")
                .add("fileName", "hello-world.m4v")
                .add("created", DateFormatter.format(LocalDateTime.now().minusYears(20)))
                .add("pictureFileName", "hello-world.jpg").build();
        req.put("/genres", genre, Status.NOT_FOUND.getStatusCode());
    }

    // @Test
    public void searchMustReturnEmptyListOnNonExistingCriteria() throws UnsupportedEncodingException {
        String text = "Does Not Exist";
        List<GuiDTO> genres = req.get("/genres/search/" + URLEncoder.encode(text, "UTF-8")).jsonPath().getList("$",
                GuiDTO.class);
        assertTrue(genres.isEmpty());
    }

    // @Test
    public void removeMustFailIfSeriesWithGenreExist() {
        Long idThatIsReferencedInSeries = MagicNumbers.GENRE_NONE;
        req.delete("/genres/" + String.valueOf(idThatIsReferencedInSeries), Status.NOT_ACCEPTABLE.getStatusCode());
    }

    // @Test
    public void removeMustFailIfMoviesWithGenreExist() {
        Long idThatIsReferencedInMoviesButNotInSeries = 16l;
        req.delete("/genres/" + String.valueOf(idThatIsReferencedInMoviesButNotInSeries),
                Status.NOT_ACCEPTABLE.getStatusCode());
    }
}
