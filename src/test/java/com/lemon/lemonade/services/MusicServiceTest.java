package com.lemon.lemonade.services;

import com.lemon.lemonade.Exceptions.EntityNotFountException;
import com.lemon.lemonade.dto.MusicFileResponse;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class MusicServiceTest {

    // Real default methods, so the SDK's convenience overloads and paginator reach the stubbed calls
    private final S3Client s3 = mock(S3Client.class, CALLS_REAL_METHODS);
    private final MusicService musicService = new MusicService(s3, "music");

    @Test
    void listsOnlySongsNewestFirst() {
        doReturn(ListObjectsV2Response.builder().contents(
                        object("old.mp3", "2020-01-01T00:00:00Z"),
                        object("album/new.flac", "2024-01-01T00:00:00Z"),
                        object("notes.txt", "2024-01-01T00:00:00Z"),
                        object("._old.mp3", "2024-01-01T00:00:00Z")).build())
                .when(s3).listObjectsV2(any(ListObjectsV2Request.class));

        List<MusicFileResponse> files = musicService.listFiles();

        assertThat(files).extracting(MusicFileResponse::name).containsExactly("new.flac", "old.mp3");
        assertThat(files).extracting(MusicFileResponse::mimeType).containsExactly("audio/x-flac", "audio/mpeg");
        assertThat(files.getFirst().id()).isEqualTo(idOf("album/new.flac"));
        assertThat(files.getFirst().modifiedTime()).isEqualTo("2024-01-01T00:00:00Z");
    }

    @Test
    void rejectsCraftedAndNonSongIdsWithoutCallingStorage() {
        for (String fileId : List.of(idOf("../other-bucket/song.mp3"), idOf("/song.mp3"), idOf("a/./song.mp3"),
                idOf("notes.txt"), "not base64!")) {
            assertThatThrownBy(() -> musicService.download(fileId, null)).isInstanceOf(EntityNotFountException.class);
        }
        verifyNoInteractions(s3);
    }

    @Test
    void rejectsNonAudioUploads() {
        MockMultipartFile file = new MockMultipartFile("file", "notes.txt", "text/plain", "hi".getBytes());

        assertThatThrownBy(() -> musicService.upload(file)).isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(s3);
    }

    private static S3Object object(String key, String lastModified) {
        return S3Object.builder().key(key).size(100L).lastModified(Instant.parse(lastModified)).build();
    }

    private static String idOf(String key) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(key.getBytes(StandardCharsets.UTF_8));
    }
}
