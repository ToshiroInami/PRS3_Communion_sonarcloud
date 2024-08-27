package pe.edu.vallegrande.vg_ms_communion.application.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vg_ms_communion.domain.dto.AdminCommunionDto;
import pe.edu.vallegrande.vg_ms_communion.domain.dto.CommunionResponseDto;
import pe.edu.vallegrande.vg_ms_communion.domain.dto.PaymentStatusDto;
import pe.edu.vallegrande.vg_ms_communion.domain.dto.PersonDto;
import pe.edu.vallegrande.vg_ms_communion.domain.model.Communion;
import pe.edu.vallegrande.vg_ms_communion.domain.repository.CommunionRepository;
import pe.edu.vallegrande.vg_ms_communion.application.feignclient.PersonFeignClient;
import pe.edu.vallegrande.vg_ms_communion.application.service.AdminCommunionService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

import static pe.edu.vallegrande.vg_ms_communion.util.Communion.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminCommunionServiceImpl implements AdminCommunionService {

    private final CommunionRepository communionRepository;
    private final PersonFeignClient personFeignClient;

    public Mono<ResponseEntity<PersonDto>> findPersonById(
            String documentType,
            String documentNumber
    ) {
        return Mono.fromCallable(() ->
                        personFeignClient.findByDocumentTypeAndNumber(
                                documentType,
                                documentNumber
                        )
                )
                .onErrorResume(e -> {
                    log.error("Error calling PersonFeignClient with Document Type: {} and Document Number: {}", documentType, documentNumber, e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null));
                });
    }

    @Override
    public Flux<CommunionResponseDto> listAllRequest(String requestStatus) {
        Flux<Communion> communions;

        if (requestStatus == null || requestStatus.isEmpty()) {
            communions = communionRepository.findAll();
        } else {
            communions = communionRepository.findByRequestStatus(requestStatus);
        }

        return communions.flatMap(communion ->
                findPersonById(
                        communion.getDocumentType(),
                        communion.getDocumentNumber()
                ) // Llamada al método que obtiene PersonDto por tipo y número de documento
                        .flatMap(response -> {
                            PersonDto personDto = response.getBody();

                            // Crear el DTO de respuesta y copiar los datos de Communion
                            CommunionResponseDto responseDto = new CommunionResponseDto();
                            responseDto.setIdCommunion(communion.getIdCommunion());
                            responseDto.setPlaceCommunion(communion.getPlaceCommunion());
                            responseDto.setCatechesis(communion.getCatechesis());
                            responseDto.setPriest(communion.getPriest());
                            responseDto.setCommunionDate(communion.getCommunionDate());
                            responseDto.setApplicantId(communion.getApplicantId());
                            responseDto.setStorageId(communion.getStorageId());
                            responseDto.setIncomeFileIds(communion.getIncomeFileIds());
                            responseDto.setComment(communion.getComment());
                            responseDto.setPaymentStatus(communion.getPaymentStatus());
                            responseDto.setRequestStatus(communion.getRequestStatus());
                            // Asignar el objeto PersonDto al DTO de respuesta
                            responseDto.setApplicant(personDto);

                            return Mono.just(responseDto);
                        }));
    }

    @Override
    public Mono<Communion> updateCommunion(String idCommunion, AdminCommunionDto adminCommunionDto) {
        return communionRepository.findById(idCommunion)
                .flatMap(communion -> {
                    if (communion.getRequestStatus().equals(PENDING) &&
                            adminCommunionDto.getRequestStatus() != null &&
                            (adminCommunionDto.getRequestStatus().equals(ACCEPTED)
                                    || adminCommunionDto.getRequestStatus().equals(REJECTED))) {
                        communion.setRequestStatus(adminCommunionDto.getRequestStatus());
                        return communionRepository.save(communion);
                    } else if (communion.getRequestStatus().equals(ACCEPTED)
                            || communion.getRequestStatus().equals(REJECTED)) {
                        log.info("No se hace ninguna acción porque ya está en un estado final {}",
                                communion.getRequestStatus());
                        return Mono.just(communion);
                    } else {
                        if (adminCommunionDto.getPriest() != null) {
                            communion.setPriest(adminCommunionDto.getPriest());
                        }
                        if (adminCommunionDto.getCommunionDate() != null) {
                            communion.setCommunionDate(adminCommunionDto.getCommunionDate());
                        }
                        if (adminCommunionDto.getComment() != null) {
                            communion.setComment(adminCommunionDto.getComment());
                        }
                        if (adminCommunionDto.getPaymentStatus() != null) {
                            communion.setPaymentStatus(adminCommunionDto.getPaymentStatus());
                        }
                        if (adminCommunionDto.getRequestStatus() != null) {
                            communion.setRequestStatus(adminCommunionDto.getRequestStatus());
                        }

                        return communionRepository.save(communion);
                    }
                });
    }

    @Override
    public Mono<Communion> updatePaymentStatus(String id, PaymentStatusDto paymentStatusDto) {
        return communionRepository.findById(id)
                .flatMap(communion -> {
                    // Verificar si el RequestStatus ya está en un estado final
                    if (communion.getRequestStatus().equals(ACCEPTED) || communion.getRequestStatus().equals(REJECTED)) {
                        log.info("No se hace ninguna acción porque ya está en un estado final: {}",
                                communion.getRequestStatus());
                        return Mono.just(communion);
                    }

                    // Verificar si el PaymentStatus actual ya está en un estado final
                    if (communion.getPaymentStatus().equals(ACCEPTED) || communion.getPaymentStatus().equals(REJECTED)) {
                        log.info("No se puede cambiar el PaymentStatus porque ya está en un estado final: {}",
                                communion.getPaymentStatus());
                        return Mono.just(communion);
                    }

                    // Validar y actualizar el PaymentStatus si es válido
                    if (paymentStatusDto.getPaymentStatus() != null &&
                            (paymentStatusDto.getPaymentStatus().equals(ACCEPTED)
                                    || paymentStatusDto.getPaymentStatus().equals(REJECTED))) {
                        communion.setPaymentStatus(paymentStatusDto.getPaymentStatus());
                        return communionRepository.save(communion);
                    } else {
                        return Mono.error(new IllegalArgumentException("El PaymentStatus debe ser 'A' o 'R'"));
                    }
                });
    }

}