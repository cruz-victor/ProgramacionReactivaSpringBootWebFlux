package com.vic.reactor.app;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.vic.reactor.app.model.Comentarios;
import com.vic.reactor.app.model.Usuario;
import com.vic.reactor.app.model.UsuarioComentarios;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SpringBootApplication
//Para que la aplicacion sea de consola implementar CommandLineRunner
public class SpringBootReactorApplication implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(SpringBootReactorApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(SpringBootReactorApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		//ejemploIterable();
		//ejemploFlatMap();
		//ejemploToString();
		//ejemploCollectList();
		//ejemploUsuarioComentarioFlatMap();
		//ejemploUsuarioComentarioZipWith();
		//ejemploUsuarioComentarioZipWithFormaDos();
		//ejemploZipWithRangos();
		//ejemploInterval();
		//ejemploDelayElementos();
		//ejemploIntervalInfinito();
		//ejemploIntervalDesdeCreate();
		ejemploContraPresion();
	}
		
	public void ejemploContraPresion() throws Exception {
		Flux.range(1, 20)
		.log()
		.limitRate(5) //Recibe de 5 en 5 elementos en el suscriptor. Por defecto es unbounded
		.subscribe(next->System.out.println(next.toString()));
		/*.subscribe(new Subscriber() {
			private Subscription s;
			private Integer limite=5;
			private Integer consumido=0;
			@Override
			public void onSubscribe(Subscription s) {
				this.s=s;
				s.request(limite);//Pedir de 5 en 5								
			}

			@Override
			public void onNext(Object t) {
				System.out.println(t.toString());
				consumido++;
				if(consumido==limite) {
					consumido=0;
					s.request(limite); //Pedir otros 5
				}
			}

			@Override
			public void onError(Throwable t) {
				
			}

			@Override
			public void onComplete() {
				// TODO Auto-generated method stub
				
			}
		});*/
		
		System.out.println("Finalizo el main");
	}
	
	public void ejemploIntervalDesdeCreate() throws Exception {
		Flux.create(emiter->{
			Timer timer=new Timer();
			timer.schedule(new TimerTask() {
				private Integer contador=0;
				@Override
				public void run() {
					// TODO Auto-generated method stub
					emiter.next(++contador);
					if(contador==10) {
						timer.cancel();
						emiter.complete();
					}
					
					 if(contador==5) { timer.cancel(); emiter.error(new
					 InterruptedException("Error, se ha detenido el flux en 5!")); }
					
					
				}
			}, 1000, 1000);			
		})
		.subscribe(
				next->System.out.println(next.toString()), //onNext. Tarea a hacer en siguiente
				error->System.out.println(error.getMessage()),//onError. Tarea a hacer en error
				()->System.out.println("Hemos termindo")//onComplete. Tarea a hacer cuando complete
				);
		
		System.out.println("Finalizo el main");
	}
	
	
	public void ejemploIntervalInfinito() throws Exception {
		CountDownLatch latch =new CountDownLatch(1);//Contador para bloquear
		
		Flux.interval(Duration.ofSeconds(1))//Emitira un valor cada 1 segundo infinitamente
		.doOnTerminate(latch::countDown)//Decrementa el contador a 0. Se ejecuta al finalizar el flujo. En este carro cuando se genere el error
		.flatMap(i->{ //Se crear otro flujo simple
			if(i>5) {
				return Flux.error(new InterruptedException("Solo hasta 5!"));
			}
			return Flux.just(i);
		})
		.retry(2) //Reintenta ejecutar el flujo 2 veces mas. No importa el orden del OPERADOR
		.map(i->"Hola "+i)		
		.subscribe(
				s->System.out.println(s), //Observador
				e->System.out.println(e.getMessage())//Manejo Error
				);
		
		latch.await();//Espera a que decremente el contador. Libre el Thread
	}
	
	public void ejemploDelayElementos() throws Exception {
		Flux<Integer> rango=Flux.range(1, 12)
				.delayElements(Duration.ofSeconds(1))
				.doOnNext(i->System.out.println(i.toString()));
		
		//rango.subscribe();
		rango.blockLast();
		
		//Thread.sleep(13000);
		
		System.out.println("Finalizo el main");
	}
	
	public void ejemploInterval() throws Exception {
		Flux<Integer> rango=Flux.range(1, 12);
		Flux<Long> retraso=Flux.interval(Duration.ofSeconds(1));
		
		rango.zipWith(retraso,(ran, ret)->ran)
		.doOnNext(i->System.out.println(i.toString())) //hace alguna tarea
		//.subscribe();//No bloquea
		.blockLast();//Bloquea hasta el ultimo elemento
		System.out.println("Finalizo el main");
	}
	
	public void ejemploZipWithRangos() throws Exception {
		Flux<Integer> rangos=Flux.range(0, 4);
		
		Flux.just(1,2,3,4)
		.map(i->i*2)
		.zipWith(rangos, (uno,dos)->String.format("Primer flux: %d, Segundo flux:%d", uno, dos))
		.subscribe(texto-> System.out.println(texto));
	}

	public void ejemploUsuarioComentarioZipWithFormaDos() throws Exception {
		Mono<Usuario> usuarioMono=Mono.fromCallable(()->new Usuario("victor","cruz"));
		
		Mono<Comentarios> comentariosMono=Mono.fromCallable(()->{
			Comentarios comentarios=new Comentarios();
			comentarios.addComentario("Hola victor, que tal");
			comentarios.addComentario("Manana voy a la playa");
			comentarios.addComentario("Estoy tomando el curso spring reactor");
			return comentarios;
		});
		
		Mono<UsuarioComentarios> usuarioComentarios=usuarioMono
				.zipWith(comentariosMono)
				.map(tuple->{
					Usuario u=tuple.getT1();
					Comentarios c=tuple.getT2();
					
					return new UsuarioComentarios(u, c);					
				});
				
		usuarioComentarios.subscribe(uc->System.out.println(uc.toString()));
	}
	
	public void ejemploUsuarioComentarioZipWith() throws Exception {
		Mono<Usuario> usuarioMono=Mono.fromCallable(()->new Usuario("victor","cruz"));
		
		Mono<Comentarios> comentariosMono=Mono.fromCallable(()->{
			Comentarios comentarios=new Comentarios();
			comentarios.addComentario("Hola victor, que tal");
			comentarios.addComentario("Manana voy a la playa");
			comentarios.addComentario("Estoy tomando el curso spring reactor");
			return comentarios;
		});
		
		Mono<UsuarioComentarios> usuarioComentarios=usuarioMono.zipWith(comentariosMono, (usu, com)-> new UsuarioComentarios(usu, com));
		
		usuarioComentarios.subscribe(uc->System.out.println(uc.toString()));
	}

	
	public void ejemploUsuarioComentarioFlatMap() throws Exception {
		Mono<Usuario> usuarioMono=Mono.fromCallable(()->new Usuario("victor","cruz"));
		
		Mono<Comentarios> comentariosUsuarioMono=Mono.fromCallable(()->{
			Comentarios comentarios=new Comentarios();
			comentarios.addComentario("Hola victor, que tal");
			comentarios.addComentario("Manana voy a la playa");
			comentarios.addComentario("Estoy tomando el curso spring reactor");
			return comentarios;
		});
		
		usuarioMono.flatMap(usu-> comentariosUsuarioMono.map(com->new UsuarioComentarios(usu, com)))
		.subscribe(usuarioComentario->System.out.println(usuarioComentario.toString()));
	}


	//Convertir el flux en un mono
	//Enves de emitir cada elemento de la lista, vamos a emitir la lista completa
	public void ejemploCollectList() throws Exception {
		// Flujo 0
		List<Usuario> usuariosList = new ArrayList<>();
		usuariosList.add(new Usuario("Andres","Guzman"));
		usuariosList.add(new Usuario("Pedro","Fulano"));
		usuariosList.add(new Usuario("Victor","Cruz"));
		usuariosList.add(new Usuario("Diego","Simione"));
		usuariosList.add(new Usuario("Juan","Paten"));
		usuariosList.add(new Usuario("Bruce","Lee"));
		usuariosList.add(new Usuario("Bruce","Willis"));

		Flux.fromIterable(usuariosList)
		.collectList() //Convierte a un mono que contiene un solo objeto con una lista
		.subscribe(lista->System.out.println(lista.toString()));		
	}


	
	public void ejemploToString() throws Exception {
		// Flujo 0
		List<Usuario> usuariosList = new ArrayList<>();
		usuariosList.add(new Usuario("Andres","Guzman"));
		usuariosList.add(new Usuario("Pedro","Fulano"));
		usuariosList.add(new Usuario("Victor","Cruz"));
		usuariosList.add(new Usuario("Diego","Simione"));
		usuariosList.add(new Usuario("Juan","Paten"));
		usuariosList.add(new Usuario("Bruce","Lee"));
		usuariosList.add(new Usuario("Bruce","Willis"));

		// Observable
		// Flujo 1
		Flux.fromIterable(usuariosList)
				.map(usuario->usuario.getNombre().concat(" ").concat(usuario.getApellidos().toUpperCase()))
				.flatMap(nombre->{ //Convierte a otro flujo Mono o Flux, que es observable. Aplana y lo uno en el mismo flujo
					if(nombre.contains("Bruce")) {
						System.out.println(Mono.just(nombre)+"+1");
						return Mono.just(nombre);
					}else {
						return Mono.empty();
					}
				})
				.map(nombre -> {					
					System.out.println(nombre.toLowerCase() + "+2");
					return nombre.toLowerCase();
				})
				.subscribe(usuario -> System.out.println(usuario.toString() + "+3")); // Observador=Consumidor
	}

	
	public void ejemploFlatMap() throws Exception {
		// Flujo 0
		List<String> usuariosList = new ArrayList<>();
		usuariosList.add("Andres Guzman");
		usuariosList.add("Pedro Fulano");
		usuariosList.add("Victor Cruz");
		usuariosList.add("Diego Simione");
		usuariosList.add("Juan Paten");
		usuariosList.add("Bruce Lee");
		usuariosList.add("Bruce Willis");

		// Observable
		// Flujo 1
		Flux.fromIterable(usuariosList)
				.map(nombre -> new Usuario(nombre.split(" ")[0].toUpperCase(), nombre.split(" ")[1].toUpperCase()))
				.flatMap(usuario->{ //Convierte a otro flujo Mono o Flux, que es observable. Aplana y lo uno en el mismo flujo
					if(usuario.getNombre().equalsIgnoreCase("bruce")) {
						System.out.println(Mono.just(usuario));
						return Mono.just(usuario);
					}else {
						return Mono.empty();
					}
				})
				.map(usuario -> {
					String nombre = usuario.getNombre().toLowerCase();
					usuario.setNombre(nombre);
					System.out.println(usuario.getNombre() + "+2");
					return usuario;
				})
				.subscribe(usuario -> System.out.println(usuario.toString() + "+3")); // Observador=Consumidor
	}

	public void ejemploIterable() throws Exception {
		// Flujo 0
		List<String> usuariosList = new ArrayList<>();
		usuariosList.add("Andres Guzman");
		usuariosList.add("Pedro Fulano");
		usuariosList.add("Victor Cruz");
		usuariosList.add("Diego Simione");
		usuariosList.add("Juan Paten");
		usuariosList.add("Bruce Lee");
		usuariosList.add("Bruce Willis");

		// Observable
		// Flujo 1
		Flux<String> nombres = Flux.fromIterable(usuariosList);

		// Flux<String> nombres=Flux.just("Andres Guzman","Pedro Fulano","Victor
		// Cruz","Diego Simione","Juan Paten","Bruce Lee","Bruce Willis");

		// Flujo 2
		Flux<Usuario> usuarios = nombres
				.map(nombre -> new Usuario(nombre.split(" ")[0].toUpperCase(), nombre.split(" ")[1].toUpperCase()))
				.filter(usuario -> usuario.getNombre().toLowerCase().equals("bruce")).doOnNext(usuario -> {
					if (usuario == null) {
						throw new RuntimeException("Nombres no puede ser vacios");
					}
					System.out.println(usuario.getNombre().concat(" ").concat(usuario.getApellidos()) + "+1");
				}).map(usuario -> {
					String nombre = usuario.getNombre().toLowerCase();
					usuario.setNombre(nombre);
					System.out.println(usuario.getNombre() + "+2");
					return usuario;
				});

		usuarios.subscribe(usuario -> System.out.println(usuario.toString() + "+3"), // Observador=Consumidor
				error -> System.out.println(error.getMessage()), new Runnable() {

					@Override
					public void run() {
						System.out.println("Ha finalizado la ejecucion del observable con exito!");
					}
				});
	}
}
