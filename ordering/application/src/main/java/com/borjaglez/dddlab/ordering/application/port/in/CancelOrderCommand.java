package com.borjaglez.dddlab.ordering.application.port.in;

import static java.util.Objects.requireNonNull;

import com.borjaglez.dddlab.ordering.domain.CancellationReason;
import com.borjaglez.dddlab.ordering.domain.OrderId;

public record CancelOrderCommand(OrderId orderId, CancellationReason reason) {

  public CancelOrderCommand {
    requireNonNull(orderId, "orderId");
    requireNonNull(reason, "reason");
  }
}
