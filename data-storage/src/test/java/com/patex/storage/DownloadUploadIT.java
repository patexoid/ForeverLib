package com.patex.storage;

import com.google.common.io.Files;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.patex.storage.server.FileServer;
import com.patex.storage.server.FileStorage;
import com.patex.storage.server.LocalFileStorage;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import reactor.netty.DisposableServer;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class DownloadUploadIT {

    private static DisposableServer server;

    @BeforeClass
    public static void setUp() {
        Injector injector = Guice.createInjector(new GuicePropertyModule(),
                binder -> binder.bind(FileStorage.class).toInstance(new TempLocalStorage()));
        FileServer fileServer = injector.getInstance(FileServer.class);
        server = fileServer.startServer()
                .bindNow();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        server.disposeNow();
    }

    @Test
    public void shouldDownloadUpload() throws Exception {
        HttpClient client = HttpClientBuilder.create().build();

        HttpPost post = new HttpPost("http://localhost:8090/upload/test");
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        byte[] data = {1, 2, 3};
        HttpEntity entity = builder.addBinaryBody("testName", new ByteArrayInputStream(data), ContentType.DEFAULT_BINARY, "testFileName").build();
        post.setEntity(entity);
        HttpResponse uploadResponse = client.execute(post);
        assertEquals(200, uploadResponse.getStatusLine().getStatusCode());
        String result = new BufferedReader(new InputStreamReader(uploadResponse.getEntity().getContent()))
                .lines().collect(Collectors.joining("\n"));
        assertEquals("testFileName", result);


        HttpGet get = new HttpGet("http://localhost:8090/download/test/testFileName");
        HttpResponse downloadResponse = client.execute(get);
        assertEquals(200, downloadResponse.getStatusLine().getStatusCode());
        downloadResponse.getEntity().getContent();
        assertArrayEquals(data,inputStreamToByteArray(downloadResponse.getEntity().getContent()));
    }

    private byte[] inputStreamToByteArray(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[1024];
        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }

        buffer.flush();
        return buffer.toByteArray();
    }

    static class TempLocalStorage extends LocalFileStorage {

        public TempLocalStorage() {
            super(Files.createTempDir().getAbsolutePath());
        }
    }
}
