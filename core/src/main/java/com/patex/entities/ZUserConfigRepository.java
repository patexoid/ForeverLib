package com.patex.entities;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by Alexey on 25.03.2017.
 */
@Repository
public interface ZUserConfigRepository extends CrudRepository<ZUserConfigEntity, String> {

}
