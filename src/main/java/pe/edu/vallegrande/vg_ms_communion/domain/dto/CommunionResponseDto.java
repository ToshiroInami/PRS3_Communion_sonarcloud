package pe.edu.vallegrande.vg_ms_communion.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Data
public class CommunionResponseDto {
    private String idCommunion;
    private String applicantId;
    private PersonDto applicant; // Este campo contendrá el objeto PersonDto
    private List<String> storageId;
    private List<String> incomeFileIds;
    private String placeCommunion;
    private String catechesis;
    private String priest;
    private Date communionDate;
    private String comment;
    private String requestStatus;
    private String paymentStatus;
}