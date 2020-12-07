package com.egg.TuAlmacen.service;

import com.egg.TuAlmacen.entidad.Comentario;
import com.egg.TuAlmacen.entidad.Pedido;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.egg.TuAlmacen.entidad.Usuario;
import com.egg.TuAlmacen.enums.Rol;
import com.egg.TuAlmacen.error.ErrorService;
import com.egg.TuAlmacen.repositorio.PedidoRepositorio;
import com.egg.TuAlmacen.repositorio.UsuarioRepositorio;

@Service
public class UsuarioService implements UserDetailsService {

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    @Autowired
    private NotificacionMail notificacionMail;

    @Autowired
    private PedidoRepositorio pedidoRepositorio;
    
    @Autowired
    private ComentarioService comentarioService;
    
    public Long count() {

        return usuarioRepositorio.count();
    }

    public Usuario buscarPorId(String id) {

        return usuarioRepositorio.getOne(id);
    }

    public List<Usuario> findAll() {

        return usuarioRepositorio.findAll();
    }

    @Transactional
    public void registrarUsuario(String usuario, String email, String password, String repetir, Rol rol) throws ErrorService {

        validar(usuario, email, password, repetir, rol);

        Usuario usu = new Usuario();

        usu.setUsuario(usuario);
        usu.setEmail(email);
        String encriptada = new BCryptPasswordEncoder().encode(password);
        usu.setPassword(encriptada);
        usu.setRol(rol);

        usuarioRepositorio.save(usu);

        notificacionMail.enviar("Bienvedidos a la Tu Almacen Se ha regitrardo exitosamente", "Tu Almacen", usu.getEmail());

    }

    @Transactional
    public void modificarUsuario(String id, String usuario, String email, String password, String repetir, Rol rol) throws ErrorService {

        validar(usuario, email, password, repetir, rol);

        Optional<Usuario> respuesta = usuarioRepositorio.findById(id);

        if (respuesta.isPresent()) {

            Usuario usu = respuesta.get();

            usu.setUsuario(usuario);
            usu.setEmail(email);
            String encriptada = new BCryptPasswordEncoder().encode(password);
            usu.setPassword(encriptada);
            usu.setRol(rol);

            usuarioRepositorio.save(usu);

        } else {
            throw new ErrorService("No se encontro el usuario solicitado");
        }

    }

    @Transactional
    public void modificarUsuario(Usuario u) throws ErrorService {

        validar(u.getUsuario(), u.getEmail(), u.getPassword(), u.getPassword(), u.getRol());

        Optional<Usuario> respuesta = usuarioRepositorio.findById(u.getId());

        if (respuesta.isPresent()) {

            Usuario usu = respuesta.get();

            if (!u.getPassword().equals(usu.getPassword())) {
                String encriptada = new BCryptPasswordEncoder().encode(u.getPassword());
                u.setPassword(encriptada);
            }

            usuarioRepositorio.save(u);

        } else {
            throw new ErrorService("No se encontro el usuario solicitado");
        }

    }

    @Transactional
    public void eliminarUsuario(String id) throws ErrorService {

        Optional<Usuario> respuesta = usuarioRepositorio.findById(id);

        if (respuesta.isPresent()) {

            Usuario usu = respuesta.get();

            Usuario eliminado = this.buscarPorRol(Rol.ELIMINADO);

            if (eliminado == null) {
                eliminado = new Usuario();
                eliminado.setRol(Rol.ELIMINADO);
                eliminado.setUsuario("Usuario Eliminado");
                usuarioRepositorio.save(eliminado);
            }

            List<Pedido> pedidos = pedidoRepositorio.mispedidos(id);
            
            for (Pedido pedido : pedidos) {
                pedido.setUsuario(eliminado);
                pedidoRepositorio.save(pedido);
            }
            
            List<Comentario> comentarios = comentarioService.listarComentariosPorUsuario(id);
            
            for (Comentario comentario : comentarios) {
                comentarioService.eliminarComentario(comentario.getId());
            }
            
            usuarioRepositorio.delete(usu);

        } else {

            throw new ErrorService("No se encontro el usuario solicitado");
        }

    }

    public void validar(String usuario, String email, String password, String repetir, Rol rol) throws ErrorService {

        if (usuario == null || usuario.isEmpty()) {

            throw new ErrorService("El usario no puede ser nulo");
        }

        if (email == null || email.isEmpty()) {

            throw new ErrorService("La clave no tiene que ser nula");
        }

        if (password == null || usuario.isEmpty() || password.length() <= 6) {

            throw new ErrorService("La clave tiene que tener como mínimo 6 dígitos");

        }

        if (!password.equals(repetir)) {

            throw new ErrorService("Las claves deben ser iguales");

        }

        if (rol == null) {

            throw new ErrorService("El rol no puede ser nulo");
        }

    }

    @Override
    public UserDetails loadUserByUsername(String usuario) throws UsernameNotFoundException {

        Usuario usu = usuarioRepositorio.buscarPorUsuario(usuario);

        if (usu != null) {

            List<GrantedAuthority> permisos = new ArrayList<>();

            GrantedAuthority p1 = new SimpleGrantedAuthority("ROLE_" + usu.getRol().toString());
            permisos.add(p1);

            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpSession session = attr.getRequest().getSession(true);
            session.setAttribute("usuariosession", usu);

            User user = new User(usu.getUsuario(), usu.getPassword(), permisos);

            return user;

        } else {

            return null;
        }

    }

    @Transactional
    public Usuario buscarPorRol(Rol rol) throws ErrorService {
        return usuarioRepositorio.buscarPorRol(rol);
    }
}
