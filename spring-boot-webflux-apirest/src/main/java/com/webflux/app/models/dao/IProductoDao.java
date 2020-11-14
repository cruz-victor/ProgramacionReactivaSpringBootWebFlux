package com.webflux.app.models.dao;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.webflux.app.models.documents.Categoria;
import com.webflux.app.models.documents.Producto;

import reactor.core.publisher.Mono;


//No tiene la anotacion @Component. Al heredar de Repository por defecto se vuelve un componente de spring
public interface IProductoDao extends ReactiveMongoRepository<Producto, String> {

	public Mono<Producto> findByNombre(String nombre);
	
	@Query("{'nombre':?0}")
	public Mono<Producto> obtenerPorNombre(String nombre);
	
}
