package pe.edu.vallegrande.vg_ms_communion.application.feignclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@Component
@FeignClient(name = "ms-income", url = "${spring.client.ms-income.url}", configuration = FeignConfig.class)
public interface    IncomeFeignClient {
    @PostMapping(value = "/create", consumes = "multipart/form-data")
    ResponseEntity<String> uploadIncome(
        @RequestPart("files") MultipartFile[] files,
        @RequestPart("personId") String personId,
        @RequestPart("categoryId") String categoryId,
        @RequestPart("proofId") String proofId,
        @RequestPart("nameProof") String nameProof
    );
}
