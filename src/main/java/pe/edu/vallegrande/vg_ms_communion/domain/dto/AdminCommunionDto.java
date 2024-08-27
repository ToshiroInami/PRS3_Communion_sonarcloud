package pe.edu.vallegrande.vg_ms_communion.domain.dto;

import lombok.Data;

import java.util.Date;

@Data
public class AdminCommunionDto {
    private String priest;
    private Date communionDate;
    private String comment;
    private String requestStatus;
    private String paymentStatus;
}