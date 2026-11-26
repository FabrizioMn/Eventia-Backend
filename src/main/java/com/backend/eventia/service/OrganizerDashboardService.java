package com.backend.eventia.service;

import com.backend.eventia.dto.organizer.OrganizerDashboardResponseDto;

/**
 * Servicio con las métricas consolidadas del dashboard del organizador.
 */
public interface OrganizerDashboardService {

    /**
     * Obtiene el consolidado de métricas (KPIs, gráfico diario, distribución por
     * zonas, eventos recientes y turno activo) para el organizador autenticado.
     *
     * @param organizerEmail Email del usuario con rol ORGANIZER
     * @return DTO consolidado con todas las métricas del dashboard
     */
    OrganizerDashboardResponseDto getDashboardData(String organizerEmail);
}
