package com.vic.reactor.app.model;

public class UsuarioComentarios {
	private Usuario usuario;
	private Comentarios comentario;
	
	public UsuarioComentarios(Usuario usuario, Comentarios comentario) {
		this.usuario = usuario;
		this.comentario = comentario;
	}

	@Override
	public String toString() {
		return "UsuarioComentarios [USUARIO=" + usuario + ", COMENTARIO=" + comentario + "]";
	}	
}
