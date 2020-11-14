package com.webflux.client.app.services;

import org.springframework.http.codec.multipart.FilePart;

import com.webflux.client.app.dto.Producto;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IProductoService {
	public Flux<Producto> findAll();
	public Mono<Producto> findById(String id);
	public Mono<Producto> save(Producto producto);
	public Mono<Producto> update(Producto producto, String id);
	public Mono<Void> delete(String id);
	public Mono<Producto> upload(FilePart file, String id);
}
