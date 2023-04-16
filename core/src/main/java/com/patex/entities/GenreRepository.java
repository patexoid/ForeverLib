package com.patex.entities;

import org.springframework.data.repository.Repository;

import java.util.Optional;

public interface GenreRepository extends Repository<GenreEntity, Long> {


    Optional<GenreEntity> findByName(String genre);
}
