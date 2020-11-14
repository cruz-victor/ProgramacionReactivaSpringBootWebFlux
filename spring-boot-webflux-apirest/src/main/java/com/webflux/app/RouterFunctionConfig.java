package com.webflux.app;

import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.PUT;
import static org.springframework.web.reactive.function.server.RequestPredicates.DELETE;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.webflux.app.handler.ProductoHandler;



@Configuration
public class RouterFunctionConfig {
	
	
//	@Bean
//	public RouterFunction<ServerResponse> routes(){
//		return route(GET("/api/v2/productos").or(GET("/api/v3/productos")), request -> {
//			return ServerResponse.ok()
//					.contentType(MediaType.APPLICATION_JSON_UTF8)
//					.body(iProductoService.findAll(),Producto.class);
//		});		
//	}
	
	@Bean
	public RouterFunction<ServerResponse> routes(ProductoHandler handler){
		//return route(GET("/api/v2/productos").or(GET("/api/v3/productos")), request -> handler.listar(request));
		return route(GET("/api/v2/productos").or(GET("/api/v3/productos")), handler::listar)
				//.andRoute(GET("/api/v2/productos/{id}").and(contentType(MediaType.APPLICATION_JSON)), handler::ver)  //Valida el contentType
				.andRoute(GET("/api/v2/productos/{id}"), handler::ver)  //Valida el contentType
				.andRoute(POST("/api/v2/productos"), handler::crear)
				.andRoute(PUT("/api/v2/productos/{id}"), handler::editar)
				.andRoute(DELETE("/api/v2/productos/{id}"), handler::eliminar)
				.andRoute(POST("/api/v2/productos/upload/{id}"), handler::upload)
				.andRoute(POST("/api/v2/productos/crear"), handler::crearConFoto);
	}
}
