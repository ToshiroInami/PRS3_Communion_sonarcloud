package pe.edu.vallegrande.vg_ms_communion.application.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import pe.edu.vallegrande.vg_ms_communion.domain.dto.UserCommunionDto;
import reactor.core.publisher.Mono;

public interface UserCommunionService {
    Mono<ResponseEntity<?>> addCommunion(UserCommunionDto userCommunionDto, MultipartFile[] incomeFiles, MultipartFile[] storageFiles, String categoryId);
}
