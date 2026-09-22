package com.lemon.lemonade.dto;

import org.jspecify.annotations.Nullable;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

/**
 * An open stream to a song's content, or to the requested byte range of it; the caller must close it.
 *
 * @param contentRange the Content-Range header value when only part of the song was requested
 */
public record MusicDownload(
        String name,
        String mimeType,
        long contentLength,
        @Nullable String contentRange,
        InputStream content
) implements Closeable {

    @Override
    public void close() throws IOException {
        content.close();
    }
}
