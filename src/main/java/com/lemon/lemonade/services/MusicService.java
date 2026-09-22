package com.lemon.lemonade.services;

import com.lemon.lemonade.Exceptions.EntityNotFountException;
import com.lemon.lemonade.dto.MusicDownload;
import com.lemon.lemonade.dto.MusicFileResponse;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/** Stores songs in a SeaweedFS bucket through its S3 API. */
@Service
public class MusicService {

    private static final int MAX_FILES = 1000;

    private final S3Client s3;
    private final String bucket;
    private volatile boolean bucketReady;

    public MusicService(S3Client s3, @Value("${seaweedfs.s3.bucket}") String bucket) {
        this.s3 = s3;
        this.bucket = bucket;
    }

    /** Lists the 1000 most recently modified songs in the bucket. */
    public List<MusicFileResponse> listFiles() {
        try {
            return s3.listObjectsV2Paginator(request -> request.bucket(bucket)).contents().stream()
                    .filter(object -> audioType(object.key()).isPresent())
                    .sorted(Comparator.comparing(S3Object::lastModified).reversed())
                    .limit(MAX_FILES)
                    .map(object -> toResponse(object.key(), object.size(), object.lastModified()))
                    .toList();
        } catch (NoSuchBucketException e) {
            // Nothing has been uploaded yet
            return List.of();
        }
    }

    /** Stores a song under its file name, replacing any song already stored with that name. */
    public MusicFileResponse upload(MultipartFile file) throws IOException {
        String key = fileName(file.getOriginalFilename() != null ? file.getOriginalFilename() : "");
        MediaType type = audioType(key)
                .orElseThrow(() -> new IllegalArgumentException("Only audio files can be uploaded, got: " + key));

        ensureBucket();
        try (InputStream content = file.getInputStream()) {
            s3.putObject(request -> request.bucket(bucket).key(key).contentType(type.toString()),
                    RequestBody.fromInputStream(content, file.getSize()));
        }
        HeadObjectResponse stored = s3.headObject(request -> request.bucket(bucket).key(key));
        return toResponse(key, stored.contentLength(), stored.lastModified());
    }

    /**
     * Opens a song by the ID returned from {@link #listFiles()}.
     *
     * @param range an HTTP Range header (e.g. "bytes=1000-"), passed on to SeaweedFS so players can seek
     */
    public MusicDownload download(String fileId, @Nullable String range) {
        EntityNotFountException notFound = new EntityNotFountException("File not found: " + fileId);
        String key = decodeKey(fileId).orElseThrow(() -> notFound);
        MediaType type = audioType(key).orElseThrow(() -> notFound);
        // S3 can't answer multi-range requests ("bytes=0-10,20-30"), so those get the whole song
        String singleRange = range != null && !range.contains(",") ? range : null;

        try {
            ResponseInputStream<GetObjectResponse> content =
                    s3.getObject(request -> request.bucket(bucket).key(key).range(singleRange));
            GetObjectResponse object = content.response();
            return new MusicDownload(fileName(key), type.toString(), object.contentLength(), object.contentRange(), content);
        } catch (NoSuchKeyException | NoSuchBucketException e) {
            throw notFound;
        }
    }

    private void ensureBucket() {
        if (bucketReady) {
            return;
        }
        try {
            s3.headBucket(request -> request.bucket(bucket));
        } catch (NoSuchBucketException e) {
            s3.createBucket(request -> request.bucket(bucket));
        }
        bucketReady = true;
    }

    // IDs are just encoded keys, so reject crafted ones like "../other-bucket/song.mp3"
    private static Optional<String> decodeKey(String fileId) {
        try {
            String key = new String(Base64.getUrlDecoder().decode(fileId), StandardCharsets.UTF_8);
            boolean unsafe = key.isBlank() || key.startsWith("/")
                    || Arrays.stream(key.split("/")).anyMatch(part -> part.equals(".") || part.equals(".."));
            return unsafe ? Optional.empty() : Optional.of(key);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    // Hidden files are skipped: macOS leaves "._song.mp3" metadata files next to real songs on some drives
    private static Optional<MediaType> audioType(String key) {
        String name = fileName(key);
        if (name.isEmpty() || name.startsWith(".")) {
            return Optional.empty();
        }
        return MediaTypeFactory.getMediaType(name)
                .filter(type -> type.getType().equals("audio"));
    }

    // Browsers may send a full path ("C:\fakepath\song.mp3"), and keys may contain folders
    private static String fileName(String path) {
        return path.substring(Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\')) + 1);
    }

    private static MusicFileResponse toResponse(String key, Long size, Instant lastModified) {
        return new MusicFileResponse(
                Base64.getUrlEncoder().withoutPadding().encodeToString(key.getBytes(StandardCharsets.UTF_8)),
                fileName(key),
                audioType(key).map(MediaType::toString).orElse(null),
                size,
                lastModified.toString());
    }
}
