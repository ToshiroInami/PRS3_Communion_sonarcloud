package pe.edu.vallegrande.vg_ms_communion.application.feignclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import pe.edu.vallegrande.vg_ms_communion.domain.dto.DocumentNumberDto;
import pe.edu.vallegrande.vg_ms_communion.domain.dto.PersonDto;

@Component
@FeignClient(name = "ms-users", url = "${spring.client.ms-users.url}", configuration = FeignConfig.class)
public interface    PersonFeignClient {

    @GetMapping("/document")
                ResponseEntity<PersonDto> findByDocumentTypeAndNumber(
            @RequestParam("documentType") String documentType,
            @RequestParam("documentNumber") String documentNumber);


    @GetMapping("/{id}")
    ResponseEntity<PersonDto> findById(@PathVariable("id") String id);
}
