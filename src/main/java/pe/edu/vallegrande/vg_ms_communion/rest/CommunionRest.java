package pe.edu.vallegrande.vg_ms_communion.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pe.edu.vallegrande.vg_ms_communion.application.service.impl.AdminCommunionServiceImpl;
import pe.edu.vallegrande.vg_ms_communion.application.service.impl.UserCommunionServiceImpl;
import pe.edu.vallegrande.vg_ms_communion.domain.dto.*;
import pe.edu.vallegrande.vg_ms_communion.domain.model.Communion;
import pe.edu.vallegrande.vg_ms_communion.domain.repository.CommunionRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/communions")
public class CommunionRest {
    private final AdminCommunionServiceImpl communionService;
    private final UserCommunionServiceImpl userCommunionService;


    @Autowired
    public CommunionRest(AdminCommunionServiceImpl communionService, UserCommunionServiceImpl userCommunionService, CommunionRepository communionRepository) {
        this.communionService = communionService;
        this.userCommunionService = userCommunionService;
    }

    @GetMapping
    public Flux<CommunionResponseDto> listAllRequest(@RequestParam(required = false) String requestStatus) {
        return communionService.listAllRequest(requestStatus);
    }

    @PostMapping("/create")
    public Mono<ResponseEntity<?>> addCommunion(
            @ModelAttribute UserCommunionDto userCommunionDto,
            @RequestPart(name = "fileIncome", required = false) MultipartFile[] incomeFiles,
            @RequestPart(name = "storage_files", required = false) MultipartFile[] storageFiles,
            @RequestPart(name = "categoryId", required = false) String categoryId) {
        MultipartFile[] finalIncomeFiles = incomeFiles != null ? incomeFiles : new MultipartFile[0];
        MultipartFile[] finalStorageFiles = storageFiles != null ? storageFiles : new MultipartFile[0];
        String finalCategoryId = (categoryId != null && !categoryId.trim().isEmpty()) ? categoryId
                : "default-category-id";

        return userCommunionService.addCommunion(userCommunionDto, finalIncomeFiles, finalStorageFiles, finalCategoryId);
    }


    @PatchMapping("/update/{id}")
    public Mono<Communion> updateCommunion(@PathVariable String id, @RequestBody AdminCommunionDto adminCommunionDto) {
        return communionService.updateCommunion(id, adminCommunionDto);
    }

    @PatchMapping("/updatePayment/{id}")
    public Mono<Communion> updatePaymentStatus(@PathVariable String id, @RequestBody PaymentStatusDto paymentStatusDto) {
        return communionService.updatePaymentStatus(id, paymentStatusDto);
    }


    @GetMapping("/findPerson")
    public Mono<ResponseEntity<PersonDto>> findPersonByDocumentTypeAndNumber(
            @RequestParam("documentType") String documentType,
            @RequestParam("documentNumber") String documentNumber) {

        return userCommunionService.findPersonByDocumentTypeAndNumber(documentType, documentNumber);
    }
}
