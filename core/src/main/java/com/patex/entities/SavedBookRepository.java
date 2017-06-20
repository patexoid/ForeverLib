package com.patex.entities;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

/**
 * Created by Alexey on 12.03.2016.
 */

@Repository
public interface SavedBookRepository extends CrudRepository<SavedBook, Long> {

    List<SavedBook> findSavedBooksByExtLibrary(ExtLibrary library);

    List<SavedBook> findSavedBooksByExtLibraryAndExtIdIn(ExtLibrary library, Collection<String> urls);

}
