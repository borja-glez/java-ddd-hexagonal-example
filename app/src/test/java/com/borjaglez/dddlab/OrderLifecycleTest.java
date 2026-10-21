package com.borjaglez.dddlab;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import com.borjaglez.dddlab.notifications.EmailSender;
import com.jayway.jsonpath.JsonPath;
import com.legacypay.sdk.LegacyPayClient;
import com.legacypay.sdk.LpTransactionRequest;
import java.io.UnsupportedEncodingException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

/**
 * El sistema entero, de HTTP a la base de datos y de vuelta: los tres contextos, los adaptadores
 * reales (JPA, LegacyPay, eventos) y la transacción. Son pocos tests y lentos a propósito: las
 * reglas ya están probadas en milisegundos en OrderTest y en los tests de los casos de uso; aquí se
 * prueba que las piezas encajan.
 */
@SpringBootTest
@AutoConfigureMockMvc
class OrderLifecycleTest {

  @Autowired MockMvcTester mvc;
  @Autowired EmailSender emailSender;
  @Autowired LegacyPayClient legacyPay;

  @Test
  void aCustomerPlacesPaysAndReceivesAnOrder() {
    String orderId = placeOrder("ana", "keyboard", 1, "mouse", 2);

    assertThat(mvc.get().uri("/orders/{id}", orderId))
        .hasStatusOk()
        .bodyJson()
        .satisfies(
            body -> {
              body.assertThat().extractingPath("$.status").isEqualTo("PLACED");
              body.assertThat().extractingPath("$.total").isEqualTo(149.80);
              body.assertThat().extractingPath("$.lines.length()").isEqualTo(2);
            });

    assertThat(mvc.post().uri("/orders/{id}/payment", orderId)).hasStatus(HttpStatus.NO_CONTENT);
    assertThat(mvc.post().uri("/orders/{id}/shipment", orderId)).hasStatus(HttpStatus.NO_CONTENT);

    assertThat(mvc.get().uri("/orders/{id}", orderId))
        .bodyJson()
        .extractingPath("$.status")
        .isEqualTo("SHIPPED");
    assertThat(emailsFor(orderId)).containsExactly("Pago recibido", "Pedido enviado");
  }

  @Test
  void aShippedOrderCannotBeCancelledAndTheApiSaysWhyInTheUbiquitousLanguage() {
    String orderId = placeOrder("bea", "keyboard", 1);
    mvc.post().uri("/orders/{id}/payment", orderId).exchange();
    mvc.post().uri("/orders/{id}/shipment", orderId).exchange();

    assertThat(mvc.post().uri("/orders/{id}/cancellation", orderId))
        .hasStatus(HttpStatus.CONFLICT)
        .bodyJson()
        .extractingPath("$.title")
        .isEqualTo("OrderAlreadyShipped");
  }

  @Test
  void cancellingAPaidOrderRefundsItThroughTheAntiCorruptionLayer() {
    String orderId = placeOrder("carla", "monitor", 1);
    mvc.post().uri("/orders/{id}/payment", orderId).exchange();

    assertThat(mvc.post().uri("/orders/{id}/cancellation", orderId))
        .hasStatus(HttpStatus.NO_CONTENT);

    String paymentId =
        JsonPath.read(body(mvc.get().uri("/orders/{id}", orderId).exchange()), "$.paymentId");
    assertThat(legacyPay.getSandboxLedger())
        .filteredOn(tx -> "RFND".equals(tx.getTxType()))
        .extracting(LpTransactionRequest::getOriginalTxnId, LpTransactionRequest::getAmountInCents)
        .containsExactly(tuple(paymentId, 34900L));
    assertThat(emailsFor(orderId)).containsExactly("Pago recibido", "Pedido cancelado");
  }

  @Test
  void aRejectedPaymentLeavesTheOrderWaitingForPayment() {
    String orderId = placeOrder("dani", "monitor", 3); // 1047,00 EUR: LegacyPay responde "51"

    assertThat(mvc.post().uri("/orders/{id}/payment", orderId))
        .hasStatus(HttpStatus.PAYMENT_REQUIRED)
        .bodyJson()
        .extractingPath("$.detail")
        .asString()
        .contains("fondos insuficientes");
    assertThat(mvc.get().uri("/orders/{id}", orderId))
        .bodyJson()
        .extractingPath("$.status")
        .isEqualTo("PLACED");
  }

  @Test
  void invalidInputIsRejectedByTheValueObjects() {
    assertThat(
            mvc.post()
                .uri("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"customerId": "eva", "items": [{"productId": "keyboard", "quantity": 0}]}
                    """))
        .hasStatus(HttpStatus.BAD_REQUEST)
        .bodyJson()
        .extractingPath("$.detail")
        .asString()
        .contains("La cantidad debe estar entre 1 y 99");
  }

  @Test
  void anUnknownProductIsNotFound() {
    assertThat(
            mvc.post()
                .uri("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"customerId": "fran", "items": [{"productId": "toaster", "quantity": 1}]}
                    """))
        .hasStatus(HttpStatus.NOT_FOUND)
        .bodyJson()
        .extractingPath("$.title")
        .isEqualTo("ProductNotFound");
  }

  private String placeOrder(String customerId, Object... productsAndQuantities) {
    StringBuilder items = new StringBuilder();
    for (int i = 0; i < productsAndQuantities.length; i += 2) {
      if (!items.isEmpty()) {
        items.append(',');
      }
      items.append(
          """
          {"productId": "%s", "quantity": %s}"""
              .formatted(productsAndQuantities[i], productsAndQuantities[i + 1]));
    }
    MvcTestResult result =
        mvc.post()
            .uri("/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .content(
                """
                {"customerId": "%s", "items": [%s]}"""
                    .formatted(customerId, items))
            .exchange();
    assertThat(result).hasStatus(HttpStatus.CREATED);
    return JsonPath.read(body(result), "$.orderId");
  }

  private static String body(MvcTestResult result) {
    try {
      return result.getResponse().getContentAsString();
    } catch (UnsupportedEncodingException e) {
      throw new IllegalStateException(e);
    }
  }

  private List<String> emailsFor(String orderId) {
    return emailSender.sentEmails().stream()
        .filter(email -> email.body().contains(orderId))
        .map(EmailSender.Email::subject)
        .toList();
  }
}
