package com.webflux.app.models.dao;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.webflux.app.models.documents.Categoria;

import reactor.core.publisher.Mono;



public interface ICategoriaDao extends ReactiveMongoRepository<Categoria, String> {

	public Mono<Categoria> findCategoriaByNombre(String nombre);
}
