package pe.edu.vallegrande.vg_ms_communion.domain.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Data
@Document(collection = "communion")
public class Communion {

    @Id
    private String idCommunion;
    private List<String> storageId;
    private List<String> incomeFileIds;
    private String documentNumber;
    private String placeCommunion;
    private String catechesis;
    private String priest;
    private Date communionDate;
    private String documentType;
    private String applicantId;
    private String comment;
    private String paymentStatus;
    private String requestStatus;
}