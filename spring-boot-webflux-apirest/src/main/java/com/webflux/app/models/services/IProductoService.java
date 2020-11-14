package com.webflux.app.models.services;


import org.springframework.data.mongodb.repository.Query;

import com.webflux.app.models.documents.Categoria;
import com.webflux.app.models.documents.Producto;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IProductoService {
	public Flux<Producto> findAll();
	public Mono<Producto> findById(String id);
	public Mono<Producto> save(Producto producto);
	public Mono<Void> delete(Producto producto);
	public Flux<Producto> findAllConNombreUpperCase();
	public Flux<Producto> findAllConNombreUpperCaseRepeat();
	
	public Flux<Categoria> findAllCategoria();
	public Mono<Categoria> findCategoriaById(String id);
	public Mono<Categoria> saveCategoria(Categoria categoria);
	
	public Mono<Producto> findByNombre(String nombre);	
	public Mono<Producto> obtenerPorNombre(String nombre);
	public Mono<Categoria> findCategoriaByNombre(String nombre);
}
