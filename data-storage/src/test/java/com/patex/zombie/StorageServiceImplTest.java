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

        String[] path = {"u", "n", "i", "unique.txt"};
        when(fileStorage.exists(path)).thenReturn(Boolean.FALSE);

        byte[] content = {};
        storageService.save(content, true, unique);
        verify(fileStorage, Mockito.times(1)).save(content, path);
    }
    @Test
    public void testFileShort() {
        String[] shortName = {"a","filename.txt"};

        String[] path = {"a", "_", "_","a", "filename.txt"};
        when(fileStorage.exists(path)).thenReturn(Boolean.FALSE);

        byte[] content = {};
        storageService.save(content, true, shortName);
        verify(fileStorage, Mockito.times(1)).save(content, path);
    }

    @Test
    public void testFileExists() {
        String exists = "exist.txt";
        String[] existsPath = {"e", "x", "i", "exist.txt"};
        String[] existsPath1 = {"e", "x", "i", "exist_1_.txt"};

        when(fileStorage.exists(existsPath)).thenReturn(Boolean.TRUE);

        byte[] content = {};
        storageService.save(content, true, exists);
        verify(fileStorage, Mockito.times(1)).save(content, existsPath1);
    }

    @Test
    public void testFileExistsTwice() {
        String exists = "exist.txt";
        String[] existsPath = {"e", "x", "i", "exist.txt"};
        String[] existsPath1 = {"e", "x", "i", "exist_1_.txt"};
        String[] existsPath2 = {"e", "x", "i", "exist_2_.txt"};

        when(fileStorage.exists(existsPath)).thenReturn(Boolean.TRUE);
        when(fileStorage.exists(existsPath1)).thenReturn(Boolean.TRUE);

        byte[] content = {};
        storageService.save(content, true, exists);
        verify(fileStorage, Mockito.times(1)).save(content, existsPath2);
    }


    @Test
    public void testFilePathLvel() {
        byte[] content = {};
    }

}
