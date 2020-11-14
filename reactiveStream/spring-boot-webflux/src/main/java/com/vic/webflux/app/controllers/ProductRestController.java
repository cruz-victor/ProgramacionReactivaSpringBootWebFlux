package com.vic.webflux.app.controllers;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vic.webflux.app.models.dao.IProductoDao;
import com.vic.webflux.app.models.documents.Producto;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("api/productos")
public class ProductRestController {
	@Autowired
	private IProductoDao iProductoDao;
	
	@GetMapping
	public Flux<Producto> index(){
		System.out.println("Inicio index");
		Flux<Producto> productos=iProductoDao.findAll()
				.map(producto->{
					producto.setNombre(producto.getNombre().toUpperCase());
					return producto;
				})
				.repeat(10)
				//.delayElements(Duration.ofSeconds(1))
				.doOnNext(prod->System.out.println(prod.getNombre()));
		System.out.println("Fin index");
		
		
		return productos;
	}
	
	@GetMapping("/{id}")
	public Mono<Producto> show(@PathVariable String id){
		Flux<Producto> productos=iProductoDao.findAll();
		
		Mono<Producto> producto=productos.filter(p->p.getId().equals(id))
				.next()
				.doOnNext(prod->System.out.println(prod.getNombre()));
		
		return producto;
	}
	

}
