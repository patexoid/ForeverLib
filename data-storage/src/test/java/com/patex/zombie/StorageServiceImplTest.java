package com.patex.zombie;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class StorageServiceImplTest {

    @Mock
    FileStorage fileStorage;

    @InjectMocks
    StorageServiceImpl storageService;

    @Test
    public void testFileUnique() {
        String unique = "unique.txt";

        when(fileStorage.exists(unique)).thenReturn(Boolean.FALSE);

        byte[] content = {};
        storageService.save(content, unique);
        verify(fileStorage, Mockito.times(1)).save(content, unique);
    }

    @Test
    public void testFileExists() {
        String exists = "exist.txt";
        String exists1 = "exist_1_.txt";

        when(fileStorage.exists(exists)).thenReturn(Boolean.TRUE);

        byte[] content = {};
        storageService.save(content, exists);
        verify(fileStorage, Mockito.times(1)).save(content, exists1);
    }

    @Test
    public void testFileExistsTwice() {
        String exists = "exist.txt";
        String exists1 = "exist_1_.txt";
        String exists2 = "exist_2_.txt";

        when(fileStorage.exists(exists)).thenReturn(Boolean.TRUE);
        when(fileStorage.exists(exists1)).thenReturn(Boolean.TRUE);

        byte[] content = {};
        storageService.save(content, exists);
        verify(fileStorage, Mockito.times(1)).save(content, exists2);
    }

}
