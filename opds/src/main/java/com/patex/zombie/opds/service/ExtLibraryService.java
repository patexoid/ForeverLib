package com.patex.zombie.opds.service;

import com.patex.zombie.opds.entity.ExtLibrary;
import com.patex.zombie.opds.entity.ExtLibraryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ExtLibraryService {

    private final ExtLibraryRepository repo;

    @Autowired
    public ExtLibraryService(ExtLibraryRepository repo) {
        this.repo = repo;
    }

    public Page<ExtLibrary> findAll(Pageable pageable) {
        return repo.findAll(pageable);
    }

    public ExtLibrary save(ExtLibrary entity) {
        return repo.save(entity);
    }

    public ExtLibrary findOne(Long id) {
        return repo.findById(id).get();
    }
}
