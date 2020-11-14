package com.vic.webflux.app.controllers;

import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Date;
import java.util.UUID;

import javax.sound.midi.Patch;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.thymeleaf.spring5.context.webflux.ReactiveDataDriverContextVariable;

import com.vic.webflux.app.models.documents.Categoria;
import com.vic.webflux.app.models.documents.Producto;
import com.vic.webflux.app.models.services.IProductoService;


import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SessionAttributes("producto")
@Controller
public class ProductoController {
	// Normalmente el el controlador no existe la instruccion subscribe. La vista se
	// subscribe automaticamente
	@Autowired
	private IProductoService iProductoService;

	@Value("${config.uploads.path}")
	private String path;

	@ModelAttribute("categorias")
	public Flux<Categoria> categorias() {
		return iProductoService.findAllCategoria();
	}
	
	@GetMapping("/uploads/img/{nombreFoto:.+}")
	public Mono<ResponseEntity<Resource>> verFoto(@PathVariable String nombreFoto) throws MalformedURLException{
		Path ruta=Paths.get(path).resolve(nombreFoto).toAbsolutePath();
		
		Resource imagen=new UrlResource(ruta.toUri());
		
		return Mono.just(
				ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\""+imagen.getFilename()+"\"")
				.body(imagen)
				);
	}

	@GetMapping("/ver/{id}")
	public Mono<String> ver(Model model, @PathVariable String id){
		return iProductoService.findById(id)
				.doOnNext(p->{
					model.addAttribute("producto",p);
					model.addAttribute("titulo","Detalle producto");										
				}).switchIfEmpty(Mono.just(new Producto()))
				.flatMap(p->{
					if(p.getId()==null) {
						return Mono.error(new InterruptedException("No existe el producto"));
					}
					return Mono.just(p);
				}).then(Mono.just("ver"))
				.onErrorResume(ex->Mono.just("redirect:/listar?error=no+existe+el+producto"));
	
	}
	
	@GetMapping({ "/listar", "/" })
	public Mono<String> listar(Model model) {
		// Por debajo funciona con el stack reactiva
		// Theamilef es el observador
		// Se obtiene un observable
		Flux<Producto> productos = iProductoService.findAllConNombreUpperCase();

		productos.subscribe(producto -> System.out.println(producto.getNombre()));
		// Pasar los productos la vista
		model.addAttribute("productos", productos);
		model.addAttribute("titulo", "Listado de productos");

		System.out.println("Retornando la lista de productos");

		return Mono.just("listar");
	}

	// Pasar el objeto producto a la vista para que se asigne al formulario
	// Metodo para crear un nuevo producto
	@GetMapping("/form")
	public Mono<String> crear(Model model) {
		model.addAttribute("producto", new Producto());
		model.addAttribute("titulo", "Formulario de producto");
		model.addAttribute("boton", "Crear");
		return Mono.just("form");
	}

	@GetMapping("/form/{id}")
	public Mono<String> editar(@PathVariable String id, Model model) {
		Mono<Producto> productoMono = iProductoService.findById(id).doOnNext(p -> {
			System.out.println("Producto:" + p.getNombre());
		}).defaultIfEmpty(new Producto());

		model.addAttribute("titulo", "Editar producto");
		model.addAttribute("producto", productoMono);
		model.addAttribute("boton", "Editar");

		return Mono.just("form");
	}

	@GetMapping("/form-v2/{id}")
	public Mono<String> editarv2(@PathVariable String id, Model model) {
		// Vamos cambiando la direccion del fluejo segun el caso
		return iProductoService.findById(id).doOnNext(p -> {
			System.out.println("Producto:" + p.getNombre());
			// En este caso no se guarda en la sesion
			model.addAttribute("titulo", "Editar producto");
			model.addAttribute("producto", p);
			model.addAttribute("boton", "Editar");
		}).defaultIfEmpty(new Producto()).flatMap(p -> {
			if (p.getId() == null) {
				return Mono.error(new InterruptedException("No existe el producto"));
			}
			return Mono.just(p);
		}).then(Mono.just("form"))// Rotornar el nombre de la vista
				.onErrorResume(ex -> Mono.just("redirect:/listar?error=no+existe+el+producto"));// En caso de falla
	}

	// El objeto producto es bidireccion controlador-vista-controlador
	@PostMapping("/form")
	public Mono<String> guardar(@Valid Producto producto, BindingResult result, Model model, @RequestPart FilePart file,
			SessionStatus status) {
		// BindeingResult siempre va al lado de @Valid
		if (result.hasErrors()) {
			model.addAttribute("titulo", "Errores en el formulario del producto");
			model.addAttribute("boton", "Guardar");
			return Mono.just("form");
		} else {
			status.setComplete();// eliminar el objeto de la sesion

			Mono<Categoria> categoria = iProductoService.findCategoriaById(producto.getCategoria().getId());

			return categoria.flatMap(c -> {
				if (producto.getCreateAt() == null) {
					producto.setCreateAt(new Date());
				}

				if (!file.filename().isEmpty()) {
					producto.setFoto(UUID.randomUUID().toString() + "-"
							+ file.filename().replace(" ", "").replace(":", "").replace("\\", ""));
				}
				producto.setCategoria(c);
				return iProductoService.save(producto);
			}).doOnNext(p -> {
				System.out.println(
						"Categoria asignada: " + p.getCategoria().getNombre() + " Id cat: " + p.getCategoria().getId());
				System.out.println("Producto guardado: " + p.getNombre() + " Id prod: " + p.getId());
			}).flatMap(p->{
				if(!file.filename().isEmpty()) {
					return file.transferTo(new File(path + p.getFoto()));
				}
				return Mono.empty();
			}).thenReturn("redirect:/listar?success=producto+guardado+con+exito");
		}
	}

	@GetMapping("/eliminar/{id}")
	public Mono<String> eliminar(@PathVariable String id) {
		return iProductoService.findById(id).defaultIfEmpty(new Producto()).flatMap(p -> {
			if (p.getId() == null) {
				return Mono.error(new InterruptedException("No existe el producto a eliminar"));
			}
			return Mono.just(p);
		}).flatMap(p -> {
			System.out.println("Eliminando producto:" + p.getNombre());
			System.out.println("Eliminando producto id:" + p.getId());
			return iProductoService.delete(p);
		}).then(Mono.just("redirect:/listar?success=producto+eliminado+con+exito"))// Entonces. Rotornar el nombre de la
																					// vista
				.onErrorResume(ex -> Mono.just("redirect:/listar?error=no+existe+el+producto+a+eliminar"));// En caso de
																											// falla
	}

	// El buffer se mide por cantidad de elementos
	@GetMapping({ "/listar-datadriver" })
	public String listarDataDriver(Model model) {
		// Por debajo funciona con el stack reactiva
		// Theamilef es el observador
		// Se obtiene un observable
		Flux<Producto> productos = iProductoService.findAllConNombreUpperCase().delayElements(Duration.ofSeconds(1));

		productos.subscribe(producto -> System.out.println(producto.getNombre()));
		// Pasar los productos la vista
		model.addAttribute("productos", new ReactiveDataDriverContextVariable(productos, 2)); // Tamano del canal = 2
																								// elementos
		model.addAttribute("titulo", "Listado de productos");

		System.out.println("Retornando la lista de productos");

		return "listar";
	}

	// El buffer se mide en byte (chunck= tamano del buffer), se utliza cuando el
	// stream es grande
	@GetMapping({ "/listar-full" })
	public String listarFull(Model model) {
		// Por debajo funciona con el stack reactiva
		// Theamilef es el observador
		// Se obtiene un observable
		Flux<Producto> productos = iProductoService.findAllConNombreUpperCaseRepeat();// Repetie el Flujo 5000 veces

		// Pasar los productos la vista
		model.addAttribute("productos", productos);
		model.addAttribute("titulo", "Listado de productos");

		System.out.println("Retornando la lista de productos");

		return "listar";
	}

	@GetMapping({ "/listar-chuncked" })
	public String listarChuncked(Model model) {
		// Por debajo funciona con el stack reactiva
		// Theamilef es el observador
		// Se obtiene un observable
		Flux<Producto> productos = iProductoService.findAllConNombreUpperCaseRepeat();// Repetie el Flujo 5000 veces

		// Pasar los productos la vista
		model.addAttribute("productos", productos);
		model.addAttribute("titulo", "Listado de productos");

		System.out.println("Retornando la lista de productos");

		return "listar-chuncked";
	}
}
