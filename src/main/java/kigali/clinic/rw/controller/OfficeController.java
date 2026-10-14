package kigali.clinic.rw.controller;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import kigali.clinic.rw.domain.Office;
import kigali.clinic.rw.service.OfficeService;

@RestController
@RequestMapping({"/api/offices", "/api/office"})
public class OfficeController {

    private final OfficeService officeService;

    public OfficeController(OfficeService officeService) {
        this.officeService = officeService;
    }

    @PostMapping("/save")
    public ResponseEntity<?> saveOffice(@RequestBody Office office) {
        String message = officeService.saveOffice(office);
        if ("Office is saved successfully".equals(message)) {
            return new ResponseEntity<>(message, HttpStatus.OK);
        }
        return new ResponseEntity<>(message, HttpStatus.CONFLICT);
    }

    @GetMapping("/all")
    public ResponseEntity<List<Office>> getAllOffices() {
        return new ResponseEntity<>(officeService.getAllOffices(), HttpStatus.OK);
    }

    @GetMapping("/free")
    public ResponseEntity<List<Office>> getFreeOffices() {
        return new ResponseEntity<>(officeService.getFreeOffices(), HttpStatus.OK);
    }

    @GetMapping("/busiest")
    public ResponseEntity<?> getBusiestOffice() {
        Optional<Object[]> busiestOffice = officeService.getBusiestOffice();
        if (busiestOffice.isPresent()) {
            return new ResponseEntity<>(busiestOffice.get(), HttpStatus.OK);
        }
        return new ResponseEntity<>("No appointments yet", HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOfficeById(@PathVariable UUID id) {
        Optional<Office> office = officeService.getOfficeById(id);
        if (office.isPresent()) {
            return new ResponseEntity<>(office.get(), HttpStatus.OK);
        }
        return new ResponseEntity<>("Office not found", HttpStatus.NOT_FOUND);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateOffice(@PathVariable UUID id, @RequestBody Office office) {
        String message = officeService.updateOffice(id, office);
        if ("Office is updated successfully".equals(message)) {
            return new ResponseEntity<>(message, HttpStatus.OK);
        }
        if ("Office not found".equals(message)) {
            return new ResponseEntity<>(message, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(message, HttpStatus.CONFLICT);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteOffice(@PathVariable UUID id) {
        String message = officeService.deleteOffice(id);
        if ("Office is deleted successfully".equals(message)) {
            return new ResponseEntity<>(message, HttpStatus.OK);
        }
        if ("Office not found".equals(message)) {
            return new ResponseEntity<>(message, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(message, HttpStatus.CONFLICT);
    }
}
