package pe.edu.vallegrande.vg_ms_communion.domain.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PersonDto {
    private String id;
    private String firstName;
    private String lastName;
    private String documentType;
    private String documentNumber;
}

