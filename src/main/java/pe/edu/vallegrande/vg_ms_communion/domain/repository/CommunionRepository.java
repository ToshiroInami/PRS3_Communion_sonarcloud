package pe.edu.vallegrande.vg_ms_communion.domain.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import pe.edu.vallegrande.vg_ms_communion.domain.model.Communion;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CommunionRepository extends ReactiveMongoRepository<Communion, String> {
    Flux<Communion> findByRequestStatus(String requestStatus);
    Mono<Communion> findFirstByDocumentNumberAndRequestStatus(String documentNumber, String requestStatus);
}