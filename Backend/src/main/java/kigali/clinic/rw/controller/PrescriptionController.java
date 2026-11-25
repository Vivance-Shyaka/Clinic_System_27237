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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import kigali.clinic.rw.domain.Prescription;
import kigali.clinic.rw.service.PrescriptionService;

@RestController
@RequestMapping("/api/prescriptions")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    public PrescriptionController(PrescriptionService prescriptionService) {
        this.prescriptionService = prescriptionService;
    }

    @PostMapping("/save")
    public ResponseEntity<String> savePrescription(
            @RequestBody Prescription prescription,
            @RequestParam UUID appointmentId) {
        String message = prescriptionService.savePrescription(prescription, appointmentId);
        if ("Prescription saved successfully".equals(message)) {
            return new ResponseEntity<>(message, HttpStatus.CREATED);
        }
        if (message.endsWith("does not exist")) {
            return new ResponseEntity<>(message, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<?> getPrescriptionsForAppointment(@PathVariable UUID appointmentId) {
        Optional<List<Prescription>> prescriptions =
                prescriptionService.getPrescriptionsForAppointment(appointmentId);
        if (prescriptions.isEmpty()) {
            return new ResponseEntity<>(
                    "The appointment with that id does not exist", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(prescriptions.get(), HttpStatus.OK);
    }

    @GetMapping("/patient/{ssNumber}")
    public ResponseEntity<List<Prescription>> getPatientHistory(@PathVariable String ssNumber) {
        return new ResponseEntity<>(prescriptionService.getPatientHistory(ssNumber), HttpStatus.OK);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<String> updatePrescription(
            @PathVariable UUID id,
            @RequestBody Prescription prescription) {
        String message = prescriptionService.updatePrescription(id, prescription);
        if ("Prescription updated successfully".equals(message)) {
            return new ResponseEntity<>(message, HttpStatus.OK);
        }
        return new ResponseEntity<>(message, HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deletePrescription(@PathVariable UUID id) {
        String message = prescriptionService.deletePrescription(id);
        if ("Prescription deleted successfully".equals(message)) {
            return new ResponseEntity<>(message, HttpStatus.OK);
        }
        return new ResponseEntity<>(message, HttpStatus.NOT_FOUND);
    }

    @GetMapping("/stats/top-medicines")
    public ResponseEntity<List<Object[]>> getTopMedicines() {
        return new ResponseEntity<>(prescriptionService.getTopMedicines(), HttpStatus.OK);
    }
}
