package com.patex.opds.entities;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Created by Alexey on 12.03.2016.
 */

@Repository
public interface SavedBookRepository extends CrudRepository<SavedBook, Long> {

    List<SavedBook> findSavedBooksByExtLibrary(ExtLibrary library);

    List<SavedBook> findSavedBooksByExtLibraryAndExtIdIn(ExtLibrary library, Collection<String> extIds);

    Optional<SavedBook> findSavedBooksByExtLibraryAndExtId(ExtLibrary library, String extId);

}
