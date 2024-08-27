package pe.edu.vallegrande.vg_ms_communion.application.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pe.edu.vallegrande.vg_ms_communion.application.feignclient.IncomeFeignClient;
import pe.edu.vallegrande.vg_ms_communion.application.feignclient.PersonFeignClient;
import pe.edu.vallegrande.vg_ms_communion.application.feignclient.StorageFeignClient;
import pe.edu.vallegrande.vg_ms_communion.application.service.UserCommunionService;
import pe.edu.vallegrande.vg_ms_communion.domain.dto.*;
import pe.edu.vallegrande.vg_ms_communion.domain.model.Communion;
import pe.edu.vallegrande.vg_ms_communion.domain.repository.CommunionRepository;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

import static pe.edu.vallegrande.vg_ms_communion.util.Communion.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserCommunionServiceImpl implements UserCommunionService {

    private final CommunionRepository communionRepository;
    private final StorageFeignClient storageFeignClient;
    private final PersonFeignClient personFeignClient;
    private final IncomeFeignClient incomeFeignClient;
    private final ObjectMapper mapper;

    public Mono<ResponseEntity<PersonDto>> findPersonByDocumentTypeAndNumber(String documentType, String documentNumber) {
        return Mono.fromCallable(() -> personFeignClient.findByDocumentTypeAndNumber(documentType, documentNumber))
                .onErrorResume(e -> {
                    log.error("Error calling PersonFeignClient", e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null));
                });
    }

    private Mono<List<String>> extractFileUrls(String responseBody) {
        try {
            StorageResponseDto storageResponse = mapper.readValue(responseBody, StorageResponseDto.class);
            return Mono.just(storageResponse.getFilesUrl());
        } catch (Exception e) {
            return Mono.error(e);
        }
    }

    private Mono<List<String>> extractIncomeFileUrls(String responseBody) {
        try {
            IncomeDto incomeResponse = mapper.readValue(responseBody, IncomeDto.class);
            return Mono.just(incomeResponse.getFileUrls());
        } catch (Exception e) {
            return Mono.error(e);
        }
    }


    @Override
    public Mono<ResponseEntity<?>> addCommunion(UserCommunionDto userCommunionDto, MultipartFile[] incomeFiles, MultipartFile[] storageFiles, String categoryId) {
        return findPersonByDocumentTypeAndNumber(userCommunionDto.getDocumentType(), userCommunionDto.getDocumentNumber())
                .flatMap(personResponse -> {
                    if (personResponse.getStatusCode().is2xxSuccessful() && personResponse.getBody() != null) {
                        // Crear una nueva entidad de Communion con los datos proporcionados
                        Communion newCommunion = createNewCommunionEntity(userCommunionDto);

                        // Buscar en el repositorio de communion las solicitudes con estatus PENDING, ACCEPTED o REJECTED
                        return communionRepository.findFirstByDocumentNumberAndRequestStatus(userCommunionDto.getDocumentNumber(), PENDING)
                                .switchIfEmpty(communionRepository.findFirstByDocumentNumberAndRequestStatus(userCommunionDto.getDocumentNumber(), ACCEPTED))
                                .switchIfEmpty(communionRepository.findFirstByDocumentNumberAndRequestStatus(userCommunionDto.getDocumentNumber(), REJECTED))
                                .flatMap(existingCommunion -> handleExistingCommunion(existingCommunion, newCommunion, storageFiles, incomeFiles, categoryId))
                                .switchIfEmpty(handleNewCommunion(newCommunion, storageFiles, incomeFiles, categoryId))
                                .onErrorResume(IncorrectResultSizeDataAccessException.class, ex -> {
                                    String errorMessage = "Error al insertar el registro";
                                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMessage));
                                });
                    } else {
                        String errorMessage = "Este DNI no está registrado";
                        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage));
                    }
                });
    }


    private Communion createNewCommunionEntity(UserCommunionDto userCommunionDto) {
        Communion newCommunion = new Communion();
        newCommunion.setIdCommunion(UUID.randomUUID().toString());
        newCommunion.setApplicantId(userCommunionDto.getApplicantId());
        newCommunion.setDocumentType(userCommunionDto.getDocumentType());
        newCommunion.setDocumentNumber(userCommunionDto.getDocumentNumber());
        newCommunion.setPlaceCommunion(userCommunionDto.getPlaceCommunion());
        newCommunion.setCatechesis(userCommunionDto.getCatechesis());
        newCommunion.setCommunionDate(null);
        newCommunion.setRequestStatus(PENDING);
        newCommunion.setPaymentStatus(PENDING);
        return newCommunion;
    }

    private Mono<ResponseEntity<?>> handleExistingCommunion(Communion existingCommunion, Communion newCommunion, MultipartFile[] storageFiles, MultipartFile[] incomeFiles, String categoryId) {
        if (REJECTED.equals(existingCommunion.getRequestStatus())) {
            return createCommunionAndUploadFiles(newCommunion, storageFiles, incomeFiles, categoryId)
                    .map(savedCommunion -> ResponseEntity.status(HttpStatus.CREATED).body(savedCommunion));
        } else if (ACCEPTED.equals(existingCommunion.getRequestStatus()) || PENDING.equals(existingCommunion.getRequestStatus())) {
            String errorMessage = "Ya existe una solicitud en curso";
            return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).body(errorMessage));
        } else {
            String errorMessage = "Estado de solicitud no válido";
            return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage));
        }
    }

    private Mono<ResponseEntity<?>> handleNewCommunion(Communion newCommunion, MultipartFile[] storageFiles, MultipartFile[] incomeFiles, String categoryId) {
        return createCommunionAndUploadFiles(newCommunion, storageFiles, incomeFiles, categoryId)
                .map(savedCommunion -> ResponseEntity.status(HttpStatus.CREATED).body(savedCommunion));
    }

    private Mono<Communion> createCommunionAndUploadFiles(Communion newCommunion, MultipartFile[] storageFiles, MultipartFile[] incomeFiles, String categoryId) {
        return Mono.zip(
                uploadToStorage(newCommunion, storageFiles),
                uploadToIncome(newCommunion, incomeFiles, categoryId)
        ).flatMap(tuple -> communionRepository.save(newCommunion));
    }

    private Mono<Communion> uploadToStorage(Communion newCommunion, MultipartFile[] files) {
        return Mono.fromCallable(() -> storageFeignClient.uploadFile(files, "communion", newCommunion.getApplicantId(), newCommunion.getIdCommunion()))
                .flatMap(responseEntity -> {
                    if (responseEntity.getStatusCode().is2xxSuccessful()) {
                        return extractFileUrls(responseEntity.getBody())
                                .doOnNext(newCommunion::setStorageId)
                                .thenReturn(newCommunion);
                    } else {
                        return Mono.error(new RuntimeException("No se pudieron cargar archivos al servicio de almacenamiento"));
                    }
                });
    }

    private Mono<Communion> uploadToIncome(Communion newCommunion, MultipartFile[] files, String categoryId) {
        String nameProof = "ms-communion";
        return Mono.fromCallable(() -> incomeFeignClient.uploadIncome(files, newCommunion.getApplicantId(), categoryId, newCommunion.getIdCommunion(), nameProof))
                .flatMap(responseEntity -> {
                    if (responseEntity.getStatusCode().is2xxSuccessful()) {
                        return extractIncomeFileUrls(responseEntity.getBody())
                                .doOnNext(newCommunion::setIncomeFileIds)
                                .thenReturn(newCommunion);
                    } else {
                        return Mono.error(new RuntimeException("No se pudieron cargar archivos al servicio de ingresos"));
                    }
                });
    }
}
