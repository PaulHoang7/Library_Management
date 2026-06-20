package com.midletest.library.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class BookCoverService {
  private final RestTemplate restTemplate;
  private final ObjectMapper objectMapper;

  public BookCoverService(ObjectMapper objectMapper) {
    SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
    requestFactory.setConnectTimeout(2500);
    requestFactory.setReadTimeout(3500);
    this.restTemplate = new RestTemplate(requestFactory);
    this.objectMapper = objectMapper;
  }

  public String findCoverImage(String title, String author) {
    String normalizedTitle = normalize(title);
    String normalizedAuthor = normalize(author);
    if (normalizedTitle == null) {
      return null;
    }

    String googleCover = findFromGoogleBooks(normalizedTitle, normalizedAuthor);
    if (googleCover != null) {
      return googleCover;
    }

    return findFromOpenLibrary(normalizedTitle, normalizedAuthor);
  }

  private String findFromGoogleBooks(String title, String author) {
    try {
      URI uri =
          UriComponentsBuilder
              .fromUriString("https://www.googleapis.com/books/v1/volumes")
              .queryParam("q", buildGoogleQuery(title, author))
              .queryParam("maxResults", 1)
              .queryParam("printType", "books")
              .build(true)
              .toUri();

      String response = restTemplate.getForObject(uri, String.class);
      if (response == null || response.isBlank()) {
        return null;
      }

      JsonNode root = objectMapper.readTree(response);
      JsonNode items = root.path("items");
      if (!items.isArray() || items.isEmpty()) {
        return null;
      }

      JsonNode imageLinks = items.get(0).path("volumeInfo").path("imageLinks");
      String image = textOrNull(imageLinks.path("thumbnail"));
      if (image == null) {
        image = textOrNull(imageLinks.path("smallThumbnail"));
      }
      if (image == null) {
        return null;
      }
      return image.replace("http://", "https://");
    } catch (Exception ex) {
      return null;
    }
  }

  private String findFromOpenLibrary(String title, String author) {
    try {
      UriComponentsBuilder builder =
          UriComponentsBuilder
              .fromUriString("https://openlibrary.org/search.json")
              .queryParam("title", title)
              .queryParam("limit", 1);
      if (author != null) {
        builder.queryParam("author", author);
      }

      URI uri = builder.build(true).toUri();
      String response = restTemplate.getForObject(uri, String.class);
      if (response == null || response.isBlank()) {
        return null;
      }

      JsonNode root = objectMapper.readTree(response);
      JsonNode docs = root.path("docs");
      if (!docs.isArray() || docs.isEmpty()) {
        return null;
      }

      JsonNode doc = docs.get(0);
      if (doc.hasNonNull("cover_i")) {
        int coverId = doc.path("cover_i").asInt();
        if (coverId > 0) {
          return "https://covers.openlibrary.org/b/id/" + coverId + "-L.jpg";
        }
      }

      JsonNode isbns = doc.path("isbn");
      if (isbns.isArray() && !isbns.isEmpty()) {
        String isbn = textOrNull(isbns.get(0));
        if (isbn != null) {
          return "https://covers.openlibrary.org/b/isbn/" + isbn + "-L.jpg";
        }
      }
      return null;
    } catch (Exception ex) {
      return null;
    }
  }

  private String buildGoogleQuery(String title, String author) {
    List<String> terms = new ArrayList<>();
    terms.add("intitle:" + title);
    if (author != null) {
      terms.add("inauthor:" + author);
    }
    return String.join(" ", terms);
  }

  private String normalize(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }

  private String textOrNull(JsonNode node) {
    if (node == null || node.isMissingNode() || node.isNull()) {
      return null;
    }
    String value = node.asText(null);
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }
}
