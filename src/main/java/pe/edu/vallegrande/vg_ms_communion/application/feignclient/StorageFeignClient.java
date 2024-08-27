package pe.edu.vallegrande.vg_ms_communion.application.feignclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@Component
@FeignClient(name = "ms-storage", url = "${spring.client.ms-storage.url}", configuration = FeignConfig.class)
public interface    StorageFeignClient {

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    ResponseEntity<String> uploadFile(@RequestPart("files") MultipartFile[] files,
                                      @RequestPart("folderName") String folderName,
                                      @RequestPart("userCode") String userCode,
                                      @RequestPart("transactionCode") String transactionCode);
}