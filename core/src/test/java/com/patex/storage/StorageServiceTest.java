package com.patex.storage;

import org.junit.Test;

import static org.mockito.Mockito.*;

public class StorageServiceTest {


    public static final String BUCKET = "test";

    @Test
    public void testFileUnique() {
        FileStorage fileStorage = mock(FileStorage.class);
        String unique = "unique.txt";

        when(fileStorage.exists(BUCKET,unique)).thenReturn(Boolean.FALSE);
        StorageService storageService = new StorageService(fileStorage);

        byte[] content = {};
        storageService.save(content, BUCKET,unique);
        verify(fileStorage, times(1)).save(content, BUCKET,unique);
    }

    @Test
    public void testFileExists() {
        FileStorage fileStorage = mock(FileStorage.class);
        String exists = "exist.txt";
        String exists1 = "exist_1_.txt";

        when(fileStorage.exists(BUCKET, exists)).thenReturn(Boolean.TRUE);
        StorageService storageService = new StorageService(fileStorage);

        byte[] content = {};
        storageService.save(content, BUCKET, exists);
        verify(fileStorage, times(1)).save(content, BUCKET, exists1);
    }

    @Test
    public void testFileExistsTwice() {
        FileStorage fileStorage = mock(FileStorage.class);
        String exists = "exist.txt";
        String exists1 = "exist_1_.txt";
        String exists2 = "exist_2_.txt";

        when(fileStorage.exists(BUCKET, exists)).thenReturn(Boolean.TRUE);
        when(fileStorage.exists(BUCKET, exists1)).thenReturn(Boolean.TRUE);
        StorageService storageService = new StorageService(fileStorage);

        byte[] content = {};
        storageService.save(content, BUCKET, exists);
        verify(fileStorage, times(1)).save(content, BUCKET, exists2);
    }

}
