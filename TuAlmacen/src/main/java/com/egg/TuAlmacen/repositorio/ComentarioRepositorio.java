package com.egg.TuAlmacen.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.egg.TuAlmacen.entidad.Comentario;



@Repository
public interface ComentarioRepositorio extends JpaRepository<Comentario,String>{

}
