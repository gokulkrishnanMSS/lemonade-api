package com.lemon.lemonade.controllers;

import com.lemon.lemonade.dto.MusicDownload;
import com.lemon.lemonade.dto.MusicFileResponse;
import com.lemon.lemonade.services.MusicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

// Kept at the URLs the Drive version used so existing clients keep working; /auth/** is public in SecurityConfiguration.
@RestController
@RequestMapping("/auth/files")
@RequiredArgsConstructor
@Tag(name = "Music", description = "Songs stored in SeaweedFS")
public class MusicController {

    private final MusicService musicService;

    @GetMapping
    @Operation(summary = "List songs", description = "Lists the most recently modified songs")
    public List<MusicFileResponse> listFiles() {
        return musicService.listFiles();
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a song", description = "Stores an audio file under its file name, replacing any song with the same name")
    @ApiResponse(responseCode = "201", description = "Song stored")
    public ResponseEntity<MusicFileResponse> upload(
            @Parameter(description = "Audio file, e.g. an .mp3") @RequestPart("file") MultipartFile file) throws IOException {
        MusicFileResponse stored = musicService.upload(file);
        return ResponseEntity.created(URI.create("/auth/files/" + stored.id())).body(stored);
    }

    // Streams straight to the servlet response: Spring's automatic Range handling only works for seekable
    // resources, so the Range header is passed on to SeaweedFS and its partial response is relayed as-is.
    @GetMapping("/{fileId}")
    @Operation(summary = "Get a song", description = "Downloads a song. Supports Range requests, so players can seek.")
    @ApiResponse(responseCode = "200", description = "Song content",
            content = @Content(mediaType = "audio/*", schema = @Schema(type = "string", format = "binary")))
    @ApiResponse(responseCode = "206", description = "Requested byte range of the song")
    public void getFile(
            @PathVariable String fileId,
            @Parameter(hidden = true) @RequestHeader(value = HttpHeaders.RANGE, required = false) String range,
            HttpServletResponse response) throws IOException {
        try (MusicDownload download = musicService.download(fileId, range)) {
            response.setStatus(download.contentRange() != null ? HttpStatus.PARTIAL_CONTENT.value() : HttpStatus.OK.value());
            response.setContentType(download.mimeType());
            response.setContentLengthLong(download.contentLength());
            response.setHeader(HttpHeaders.ACCEPT_RANGES, "bytes");
            if (download.contentRange() != null) {
                response.setHeader(HttpHeaders.CONTENT_RANGE, download.contentRange());
            }
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                    .filename(download.name(), StandardCharsets.UTF_8)
                    .build()
                    .toString());
            download.content().transferTo(response.getOutputStream());
        }
    }
}
