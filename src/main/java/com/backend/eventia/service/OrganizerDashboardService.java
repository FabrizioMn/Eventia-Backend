package com.backend.eventia.service;

import com.backend.eventia.dto.organizer.OrganizerDashboardResponseDto;
import com.backend.eventia.dto.organizer.OrganizerLiveSaleNotificationDto;

/**
 * Servicio para el Dashboard comercial y métricas en tiempo real del
 * organizador
 *
 * Incluye la infraestructura de difusión por WebSocket (STOMP) para notificar
 * inmediatamente las ventas registradas hacia la interfaz del organizador.
 */
public interface OrganizerDashboardService {

    /**
     * Obtiene el consolidado de métricas (KPIs, gráfico diario, distribución por
     * zonas,
     * eventos recientes y turno activo) para el organizador autenticado.
     *
     * @param organizerEmail Email del usuario con rol ORGANIZER
     * @return DTO consolidado con todas las métricas del dashboard
     */
    OrganizerDashboardResponseDto getDashboardData(String organizerEmail);

    /**
     * =========================================================================
     * INTEGRACIÓN EN TIEMPO REAL CON CHECKOUT / COMPRAS (WEBSOCKET STOMP)
     * =========================================================================
     * Invoque este método desde el servicio de compras u órdenes (ej. OrderService
     * / PurchaseService)
     * una vez confirmada y pagada la orden para que el Dashboard del Organizador
     * se actualice en vivo sin que el usuario tenga que recargar la página.
     *
     * Destino STOMP donde se publica el mensaje:
     * /topic/organizers/{organizerId}/dashboard
     *
     * Ejemplo de uso:
     * 
     * <pre>{@code
     * organizerDashboardService.broadcastSaleNotification(
     *         event.getOrganizer().getEmail(),
     *         event.getTitle(),
     *         ticketType.getName(),
     *         orderDetail.getQuantity(),
     *         orderDetail.getUnitPrice(),
     *         buyer.getFirstName() + " " + buyer.getLastName());
     * }</pre>
     *
     * @param organizerEmail Email del organizador dueño del evento
     * @param eventTitle     Título del evento comprado
     * @param ticketTypeName Nombre de la tarifa / zona (ej. "Campo VIP Platinum")
     * @param quantity       Cantidad de entradas compradas
     * @param unitPrice      Precio unitario por entrada (PEN)
     * @param buyerName      Nombre del comprador para la notificación
     * @return DTO de la notificación transmitida con los KPIs recalculados
     */
    OrganizerLiveSaleNotificationDto broadcastSaleNotification(
            String organizerEmail,
            String eventTitle,
            String ticketTypeName,
            Integer quantity,
            Double unitPrice,
            String buyerName);
}
