package com.borjaglez.dddlab.ordering.application.port.out;

import com.borjaglez.dddlab.ordering.application.port.in.OrderView;
import com.borjaglez.dddlab.ordering.domain.OrderId;
import java.util.Optional;

/**
 * [Hexagonal · puerto de salida · lectura] El lado de lectura: devuelve vistas, no agregados.
 *
 * <p>Su adaptador puede leer como quiera (SQL directo, una proyección, una caché) porque leer no
 * cambia nada y no hay invariantes que proteger.
 */
public interface OrderViews {

  Optional<OrderView> findById(OrderId orderId);
}
