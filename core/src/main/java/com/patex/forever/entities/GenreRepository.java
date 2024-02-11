package com.patex.forever.entities;

import org.springframework.data.repository.Repository;

import java.util.Optional;

public interface GenreRepository extends Repository<GenreEntity, Long> {


    Optional<GenreEntity> findByName(String genre);
}
