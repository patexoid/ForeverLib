package com.patex.storage;

import org.junit.Test;

import static org.mockito.Mockito.*;

public class StorageServiceTest {


    @Test
    public void testFileUnique() {
        FileStorage fileStorage = mock(FileStorage.class);
        String unique = "unique.txt";

        when(fileStorage.exists(unique)).thenReturn(Boolean.FALSE);
        StorageService storageService = new StorageService(fileStorage);

        byte[] content = {};
        storageService.save(content, unique);
        verify(fileStorage, times(1)).save(content, unique);
    }

    @Test
    public void testFileExists() {
        FileStorage fileStorage = mock(FileStorage.class);
        String exists = "exist.txt";
        String exists1 = "exist_1_.txt";

        when(fileStorage.exists(exists)).thenReturn(Boolean.TRUE);
        StorageService storageService = new StorageService(fileStorage);

        byte[] content = {};
        storageService.save(content, exists);
        verify(fileStorage, times(1)).save(content, exists1);
    }

    @Test
    public void testFileExistsTwice() {
        FileStorage fileStorage = mock(FileStorage.class);
        String exists = "exist.txt";
        String exists1 = "exist_1_.txt";
        String exists2 = "exist_2_.txt";

        when(fileStorage.exists(exists)).thenReturn(Boolean.TRUE);
        when(fileStorage.exists(exists1)).thenReturn(Boolean.TRUE);
        StorageService storageService = new StorageService(fileStorage);

        byte[] content = {};
        storageService.save(content, exists);
        verify(fileStorage, times(1)).save(content, exists2);
    }

}
