package com.egg.TuAlmacen.controlador;

import com.egg.TuAlmacen.entidad.Pedido;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.egg.TuAlmacen.entidad.Producto;
import com.egg.TuAlmacen.enums.Estado;
import com.egg.TuAlmacen.enums.Rubro;
import com.egg.TuAlmacen.error.ErrorService;
import com.egg.TuAlmacen.service.PedidoService;
import com.egg.TuAlmacen.service.ProductoService;

import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.stereotype.Controller;

@Controller
public class PedidoAdminController {

    @Autowired
    private ProductoService productoService;
    @Autowired
    private PedidoService pedidoService;

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/pedido")
    public String pedido(ModelMap modelo, @RequestParam(required = false) String estado) throws ErrorService {
        List<Pedido> pedidos = new ArrayList<Pedido>();
        if (estado != null) {
            modelo.put("estadop", estado);
            pedidos = pedidoService.listarPedidosPorEstado(Estado.valueOf(estado));
        } else {
            modelo.put("estadop", "adsfadfa");
            pedidos = pedidoService.listarPedidosPorEstado(Estado.PENDIENTE);
        }
        modelo.put("pedidos", pedidos);
        Set<Estado> estados = EnumSet.allOf(Estado.class);
        modelo.put("estados", estados);
        
        return "pedido.html";

    }

    @PostMapping("/modificarpedido")
    public String pedidoModificar(ModelMap modelo, @RequestParam String id,
            @RequestParam List<String> idproducto, @RequestParam List<Integer> cantidad, @RequestParam String estado
    ) throws ErrorService {

        try {
            List<Producto> producto = new ArrayList<>();
            for (String i : idproducto) {
                producto.add(productoService.buscarPorId(i));

            }
            pedidoService.modificarPedido(id, producto, cantidad, Estado.valueOf(estado));

        } catch (Exception e) {
            List<Producto> productos = productoService.listarProducto();

            Set<Estado> estadoo = EnumSet.allOf(Estado.class);
            modelo.put("productos", productos);
            modelo.put("estados", estadoo);

            modelo.put("cantidad", cantidad);
            modelo.put("error", e.getMessage());

            return this.pedido(modelo, null);
        }

        modelo.put("mensaje", "Has modificado el pedido exitosamente :D");

        return this.pedido(modelo, null);
    }

    @PostMapping("/eliminarpedido")
    public String bajaproducto(ModelMap modelo,
            @RequestParam String id) throws ErrorService {

        try {
            pedidoService.eliminarPedido(id);

            modelo.put("mensaje", "Se ha eliminado el pedido exitosamente");

        } catch (ErrorService e) {
            modelo.addAttribute("error", e.getMessage());
            return this.pedido(modelo, null);
        }
        return this.pedido(modelo, null);

    }

    @PostMapping("/confirmarpedido")
    public String confirmarpedido(ModelMap modelo,
            @RequestParam String id) throws ErrorService {

        Pedido p = pedidoService.buscarPorId(id);

        try {
            pedidoService.verificar(p);

            modelo.put("mensaje", "Se ha eliminado el pedido exitosamente");

        } catch (Exception e) {
            modelo.addAttribute("error", e.getMessage());
            return this.pedido(modelo, null);
        }
        return this.pedido(modelo, null);

    }
    
    @PostMapping("/anularpedidoadmin")
    public String anularPedido(ModelMap modelo, @RequestParam String id) throws ErrorService{
        
        Pedido p = pedidoService.buscarPorId(id);
        
        try {
            pedidoService.anular(p);
        } catch (Exception ex) {
            Logger.getLogger(PedidoUsuarioController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return this.pedido(modelo, null);
    } 

}
