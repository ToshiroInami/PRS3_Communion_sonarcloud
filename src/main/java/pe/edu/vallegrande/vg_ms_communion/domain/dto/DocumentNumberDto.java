package pe.edu.vallegrande.vg_ms_communion.domain.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DocumentNumberDto {
    private String documentType;
    private String documentNumber;
}
