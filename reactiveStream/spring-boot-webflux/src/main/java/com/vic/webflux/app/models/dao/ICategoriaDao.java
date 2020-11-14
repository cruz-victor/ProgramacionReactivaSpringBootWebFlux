package com.vic.webflux.app.models.dao;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.vic.webflux.app.models.documents.Categoria;

public interface ICategoriaDao extends ReactiveMongoRepository<Categoria, String> {

}
