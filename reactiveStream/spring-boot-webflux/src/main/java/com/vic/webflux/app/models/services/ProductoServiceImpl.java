package com.vic.webflux.app.models.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vic.webflux.app.models.dao.ICategoriaDao;
import com.vic.webflux.app.models.dao.IProductoDao;
import com.vic.webflux.app.models.documents.Categoria;
import com.vic.webflux.app.models.documents.Producto;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service //corresponde al tipo services, es una fachada
public class ProductoServiceImpl implements IProductoService {

	//Porque en el productoService tenemos la categoria?
	//Por que los 2 daos son parte del dominio (logia del negocio) del problema.
	//Si hay 2 daos que no tienen nada que ver, es conveniente tener service separada
	@Autowired
	private IProductoDao iProductoDao; //Desacoplamos
	
	@Autowired ICategoriaDao iCategoriaDao;
	
	@Override
	public Flux<Producto> findAll() {
		// TODO Auto-generated method stub		
		return iProductoDao.findAll();
	}
	
	@Override
	public Flux<Producto> findAllConNombreUpperCase() {
		// TODO Auto-generated method stub		
		return iProductoDao.findAll().map(producto->{
			producto.setNombre(producto.getNombre().toUpperCase());
			return producto;
		});
	}

	@Override
	public Flux<Producto> findAllConNombreUpperCaseRepeat() {
		// TODO Auto-generated method stub		
		return iProductoDao.findAll().map(producto->{
			producto.setNombre(producto.getNombre().toUpperCase());
			return producto;
		}).repeat(5000);
	}
	
	@Override
	public Mono<Producto> findById(String id) {
		// TODO Auto-generated method stub
		return iProductoDao.findById(id);
	}

	@Override
	public Mono<Producto> save(Producto producto) {
		// TODO Auto-generated method stub
		return iProductoDao.save(producto);
	}

	@Override
	public Mono<Void> delete(Producto producto) {
		// TODO Auto-generated method stub
		return iProductoDao.delete(producto);
	}

	@Override
	public Flux<Categoria> findAllCategoria() {
		return iCategoriaDao.findAll();
	}

	@Override
	public Mono<Categoria> findCategoriaById(String id) {
		return iCategoriaDao.findById(id);
	}

	@Override
	public Mono<Categoria> saveCategoria(Categoria categoria) {
		return iCategoriaDao.save(categoria);
	}

}
