package com.webflux.client.app.handler;

import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.webflux.client.app.dto.Producto;
import com.webflux.client.app.services.IProductoService;

import reactor.core.publisher.Mono;

@Component
public class ProductoHandler {
	@Autowired
	private IProductoService iProductoService;

	public Mono<ServerResponse> listar(ServerRequest request) {
		return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON_UTF8)// tipo de contenido del response
				.body(iProductoService.findAll(), Producto.class);
	}

//	public Mono<ServerResponse> ver(ServerRequest request) {
//		String id = request.pathVariable("id");
//
//		return iProductoService.findById(id)
//				.flatMap(p -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON_UTF8).syncBody(p))
//				.switchIfEmpty(ServerResponse.notFound().build()).onErrorResume(error -> {
//					WebClientResponseException errorResponse = (WebClientResponseException) error;
//					if (errorResponse.getStatusCode() == HttpStatus.NOT_FOUND) {
//						return ServerResponse.notFound().build(); // Notfound no tiene body ni contendio, se cambia por build
//
//					}
//					return Mono.error(errorResponse);
//				});
//	}
	

	public Mono<ServerResponse> ver(ServerRequest request) {
		String id = request.pathVariable("id");

		return errorHandler(iProductoService.findById(id)
				.flatMap(p -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON_UTF8).syncBody(p))
				.switchIfEmpty(ServerResponse.notFound().build()));				
	}


	public Mono<ServerResponse> crear(ServerRequest request) {
		Mono<Producto> producto = request.bodyToMono(Producto.class);

		return producto.flatMap(p -> {
			if (p.getCreateAt() == null) {
				p.setCreateAt(new Date());
			}
			return iProductoService.save(p);
		}).flatMap(p -> ServerResponse.created(URI.create("/api/client/".concat(p.getId())))
				.contentType(MediaType.APPLICATION_JSON_UTF8).syncBody(p)).onErrorResume(error -> {
					WebClientResponseException errorResponse = (WebClientResponseException) error;
					if (errorResponse.getStatusCode() == HttpStatus.BAD_REQUEST) {
						return ServerResponse.badRequest().contentType(MediaType.APPLICATION_JSON_UTF8)
								.syncBody(errorResponse.getResponseBodyAsString());
					}
					return Mono.error(errorResponse);
				});
	}

//	public Mono<ServerResponse> editar(ServerRequest request) {
//		Mono<Producto> producto = request.bodyToMono(Producto.class);
//		String id = request.pathVariable("id");
//
//		return producto.flatMap(p -> ServerResponse.created(URI.create("/api/client/".concat(id)))
//				.contentType(MediaType.APPLICATION_JSON_UTF8)
//				.body(iProductoService.update(p, id), Producto.class));				
//	}

	public Mono<ServerResponse> editar(ServerRequest request) {
		Mono<Producto> producto = request.bodyToMono(Producto.class);
		String id = request.pathVariable("id");

		return errorHandler(
				producto
				.flatMap(p->iProductoService.update(p, id))
				.flatMap(p -> ServerResponse
						.created(URI.create("/api/client/".concat(p.getId())))
						.contentType(MediaType.APPLICATION_JSON_UTF8)
						.syncBody(p)//opcion 2	//.body(BodyInserters.fromObject(p)))//opcion 1				
				)
				);				
	}

	public Mono<ServerResponse> eliminar(ServerRequest request) {
		String id = request.pathVariable("id");

		return errorHandler(iProductoService.delete(id).then(ServerResponse.noContent().build()));				
	}

	public Mono<ServerResponse> upload(ServerRequest request) {
		String id = request.pathVariable("id");

		return errorHandler(request.multipartData().map(multipart -> multipart.toSingleValueMap().get("file")).cast(FilePart.class)
				.flatMap(file -> iProductoService.upload(file, id))
				.flatMap(p -> ServerResponse.created(URI.create("/api/client/".concat(p.getId())))
						.contentType(MediaType.APPLICATION_JSON_UTF8).syncBody(p)));							
	}
	
	public Mono<ServerResponse> errorHandler(Mono<ServerResponse> response) {
		return response.onErrorResume(error -> {
			WebClientResponseException errorResponse = (WebClientResponseException) error;
			if (errorResponse.getStatusCode() == HttpStatus.NOT_FOUND) {
				Map<String, Object> body=new HashMap<>();
				body.put("error", "No existe el producto".concat(errorResponse.getMessage()));
				body.put("fecha", new Date());
				body.put("status", errorResponse.getStatusCode().value());
				
				return ServerResponse.status(HttpStatus.NOT_FOUND).syncBody(body);

			}
			return Mono.error(errorResponse);
		});	
	}
}
