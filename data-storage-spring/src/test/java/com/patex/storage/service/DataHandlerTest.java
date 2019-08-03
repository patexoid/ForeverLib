package com.patex.storage.service;

import com.google.common.cache.LoadingCache;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.locks.ReentrantLock;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DataHandlerTest {


    public static final String BUCKET = "bucket";
    @InjectMocks
    DataHandler dataHandler;
    @Mock
    private FileStorage fileStorage;
    @Mock
    private LoadingCache<String, ReentrantLock> lockStorage;

    @Before
    public void setUp() {
        when(lockStorage.getUnchecked(any())).thenReturn(mock(ReentrantLock.class));
    }

    @Test
    public void shouldAddSufixIfExist() {
        String fileName = "id.txt";
        when(fileStorage.exists(BUCKET, fileName)).thenReturn(true);

        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn(fileName);
        dataHandler.save(file, BUCKET);

        verify(fileStorage).save(any(),eq(BUCKET),eq("id_1.txt"));
    }

    @Test
    public void shouldAddSufix2IfExist() {
        String fileName = "id.txt";
        when(fileStorage.exists(BUCKET, fileName)).thenReturn(true);
        when(fileStorage.exists(BUCKET, "id_1.txt")).thenReturn(true);

        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn(fileName);
        dataHandler.save(file, BUCKET);

        verify(fileStorage).save(any(),eq(BUCKET),eq("id_2.txt"));
    }
}
