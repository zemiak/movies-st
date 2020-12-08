package com.zemiak.movies.serie;

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

import org.junit.jupiter.api.Test;

public class SerieServiceTest {
    AssuredRequests req;

    public SerieServiceTest() {
        req = new AssuredRequests();
    }

    @Test
    public void exists() {
        List<Serie> series = req.get("/series/paged?page=0&pageSize=10").jsonPath().getList("$", Serie.class);
        assertFalse(series.isEmpty(), "Series are not empty");
    }

    private JsonObject getHelloWorldSerie() {
        return Json.createObjectBuilder().add("name", "Hello, World").add("fileName", "hello-world.m4v")
                .add("created", DateFormatter.format(LocalDateTime.now().minusYears(20)))
                .add("pictureFileName", "u-a.jpg").add("genre", MagicNumbers.GENRE_NONE).build();
    }

    @Test
    public void create() {
        JsonObject serie = getHelloWorldSerie();
        Long id = req.post("/series", serie).as(Long.class);
        assertTrue(null != id, "Create serie returns ID");

        Serie entity = req.get("/series/" + String.valueOf(id)).jsonPath().getObject("$", Serie.class);
        assertEquals(id, entity.id, "Serie ID must be the same as created");
        assertEquals(serie.getString("name"), entity.name, "Name must be the same as created");
        assertEquals(serie.getString("created"), DateFormatter.format(entity.created).toString(),
                "created must be the same as created");
        assertEquals(serie.getString("pictureFileName"), entity.pictureFileName,
                "pictureFileName must be the same as created");
    }

    @Test
    public void find() {
        Long id = MagicNumbers.SERIE_NONE;
        Serie entity = req.get("/series/" + String.valueOf(id)).jsonPath().getObject("$", Serie.class);
        assertEquals(id, entity.id, "Serie ID must be the same as specified");
        assertEquals("Not defined", entity.name, "Name must be Not defined");
    }

    @Test
    public void remove() {
        JsonObject serie = getHelloWorldSerie();
        Long id = req.post("/series", serie).as(Long.class);
        assertTrue(null != id, "Create serie returns ID");

        req.delete("/series/" + String.valueOf(id), Status.NO_CONTENT.getStatusCode());
        req.get("/series/" + String.valueOf(id), Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void update() {
        Long id = MagicNumbers.SERIE_NONE;
        Serie entity = req.get("/series/" + String.valueOf(id)).jsonPath().getObject("$", Serie.class);
        assertEquals(id, entity.id, "Serie ID must be the same as specified");
        assertEquals("Not defined", entity.name, "Name must be Not defined");

        entity.name = "Some";
        JsonObject json = entity.toJson();

        req.put("/series", json, Status.NO_CONTENT.getStatusCode());

        entity = req.get("/series/" + String.valueOf(id)).jsonPath().getObject("$", Serie.class);
        assertEquals("Some", entity.name, "Updated name must be: Some");

        entity.name = "Not defined";
        req.put("/series", entity.toJson(), Status.NO_CONTENT.getStatusCode());
    }

    @Test
    public void search() throws UnsupportedEncodingException {
        String text = "not";
        List<GuiDTO> series = req.get("/series/search/" + URLEncoder.encode(text, "UTF-8")).jsonPath().getList("$",
                GuiDTO.class);
        assertFalse(series.isEmpty());
        assertEquals("Not defined", series.get(0).title, "One Not defined should be found");
    }

    @Test
    public void createMustFailIfIDIsNotEmpty() {
        JsonObject serie = Json.createObjectBuilder().add("id", 42).add("name", "Hello, World")
                .add("fileName", "hello-world.m4v")
                .add("created", DateFormatter.format(LocalDateTime.now().minusYears(20)))
                .add("pictureFileName", "u-a.jpg").add("genre", MagicNumbers.GENRE_NONE).build();
        req.post("/series", serie, Status.NOT_ACCEPTABLE.getStatusCode());
    }

    @Test
    public void updateMustFailIfIDIsEmpty() {
        JsonObject serie = getHelloWorldSerie();
        req.put("/series", serie, Status.NOT_ACCEPTABLE.getStatusCode());
    }

    @Test
    public void findMustFailIfEntityDoesNotExist() {
        Long id = 42000l;
        req.get("/series/" + String.valueOf(id), Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void deleteMustFailIfEntityDoesNotExist() {
        Long id = 42000l;
        req.delete("/series/" + String.valueOf(id), Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void updateMustFailIfEntityDoesNotExist() {
        Long id = 42000l;
        JsonObject serie = Json.createObjectBuilder().add("id", id).add("name", "Hello, World")
                .add("fileName", "hello-world.m4v")
                .add("created", DateFormatter.format(LocalDateTime.now().minusYears(20)))
                .add("pictureFileName", "u-a.jpg").add("genre", 0l).build();
        req.put("/series", serie, Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void searchMustReturnEmptyListOnNonExistingCriteria() throws UnsupportedEncodingException {
        String text = "Does Not Exist";
        List<GuiDTO> series = req.get("/series/search/" + URLEncoder.encode(text, "UTF-8")).jsonPath().getList("$",
                GuiDTO.class);
        assertTrue(series.isEmpty());
    }

    @Test
    public void removeMustFailIfMoviesWithSerieExist() {
        Long idThatIsReferencedInMovies = MagicNumbers.SERIE_SCOOBYDOO;
        List<GuiDTO> movies = req.get("/series/browse?id=" + String.valueOf(idThatIsReferencedInMovies)).jsonPath()
                .getList("$", GuiDTO.class);
        assertFalse(movies.isEmpty(), "ScoobyDoo contains pisodes");
        req.delete("/series/" + String.valueOf(idThatIsReferencedInMovies), Status.NOT_ACCEPTABLE.getStatusCode());
    }
}
