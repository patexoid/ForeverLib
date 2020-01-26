package com.patex.zombie.core.storage.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.InputStream;

@RequiredArgsConstructor
@Getter
public class FileData {

    private final InputStream inputStream;

    private final long size;
}
