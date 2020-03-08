package com.patex.entities;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by potekhio on 17-Mar-16.
 */
@Repository
public interface FileResourceRepository extends CrudRepository<FileResourceEntity,Long> {

}
