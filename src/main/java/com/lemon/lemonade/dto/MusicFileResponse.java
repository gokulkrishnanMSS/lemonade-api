package com.lemon.lemonade.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Song stored in SeaweedFS")
public record MusicFileResponse(
        @Schema(description = "File ID, the URL-safe Base64 of the song's key in the storage bucket", example = "WWVuIE1pbnVra2kubXAz")
        String id,
        @Schema(example = "song.mp3")
        String name,
        @Schema(example = "audio/mpeg")
        String mimeType,
        @Schema(description = "Size in bytes", example = "3456789")
        Long size,
        @Schema(description = "RFC 3339 last-modified timestamp")
        String modifiedTime
) {
}
