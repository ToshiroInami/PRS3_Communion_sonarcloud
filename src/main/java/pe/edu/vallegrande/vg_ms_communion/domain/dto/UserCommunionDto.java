package pe.edu.vallegrande.vg_ms_communion.domain.dto;

import lombok.Data;

import java.util.List;

@Data
public class    UserCommunionDto {
    private String applicantId;
    private String documentType;
    private String documentNumber;
    private String placeCommunion;
    private String catechesis;
    private List<String> storageId;
}