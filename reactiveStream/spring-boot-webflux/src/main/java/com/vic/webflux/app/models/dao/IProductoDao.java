package com.vic.webflux.app.models.dao;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.vic.webflux.app.models.documents.Producto;

//No tiene la anotacion @Component. Al heredar de Repository por defecto se vuelve un componente de spring
public interface IProductoDao extends ReactiveMongoRepository<Producto, String> {

}
