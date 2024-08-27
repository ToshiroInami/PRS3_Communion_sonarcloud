package pe.edu.vallegrande.vg_ms_communion.application.service;

import pe.edu.vallegrande.vg_ms_communion.domain.dto.AdminCommunionDto;
import pe.edu.vallegrande.vg_ms_communion.domain.dto.CommunionResponseDto;
import pe.edu.vallegrande.vg_ms_communion.domain.dto.PaymentStatusDto;
import pe.edu.vallegrande.vg_ms_communion.domain.model.Communion;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AdminCommunionService {
    Flux<CommunionResponseDto> listAllRequest(String requestStatus);
    Mono<Communion> updateCommunion(String idCommunion, AdminCommunionDto adminCommunionDto);
    Mono<Communion> updatePaymentStatus(String id, PaymentStatusDto paymentStatusDto);
}