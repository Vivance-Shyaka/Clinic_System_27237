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

import kigali.clinic.rw.domain.Appointment;
import kigali.clinic.rw.domain.Patient;
import kigali.clinic.rw.service.PatientService;

@RestController
@RequestMapping({"/api/patients", "/api/patient"})
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @PostMapping("/save")
    public ResponseEntity<?> savePatient(@RequestBody Patient patient) {
        String message = patientService.savePatient(patient);
        if ("Patient is saved successfully".equals(message)) {
            return new ResponseEntity<>(message, HttpStatus.OK);
        }
        return new ResponseEntity<>(message, HttpStatus.CONFLICT);
    }

    @GetMapping("/all")
    public ResponseEntity<List<Patient>> getAllPatients() {
        return new ResponseEntity<>(patientService.getAllPatients(), HttpStatus.OK);
    }

    @GetMapping("/by-last-name")
    public ResponseEntity<List<Patient>> getPatientsByLastName(@RequestParam String lastName) {
        return new ResponseEntity<>(patientService.getPatientsByLastName(lastName), HttpStatus.OK);
    }

    @GetMapping("/of-doctor/{doctorId}")
    public ResponseEntity<?> getPatientsOfDoctor(@PathVariable UUID doctorId) {
        Optional<List<Patient>> patients = patientService.getPatientsOfDoctor(doctorId);
        if (patients.isPresent()) {
            return new ResponseEntity<>(patients.get(), HttpStatus.OK);
        }
        return new ResponseEntity<>("The doctor with that id does not exist", HttpStatus.NOT_FOUND);
    }

    @GetMapping("/frequent")
    public ResponseEntity<List<Patient>> getFrequentPatients(@RequestParam long min) {
        return new ResponseEntity<>(patientService.getFrequentPatients(min), HttpStatus.OK);
    }

    @GetMapping("/{ssNumber}/upcoming")
    public ResponseEntity<?> getUpcomingAppointments(@PathVariable String ssNumber) {
        Optional<List<Appointment>> appointments = patientService.getUpcomingAppointments(ssNumber);
        if (appointments.isEmpty()) {
            return new ResponseEntity<>("That patient with that ssnumber does not exist", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(appointments.get(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPatientById(@PathVariable UUID id) {
        Optional<Patient> patient = patientService.getPatientById(id);
        if (patient.isPresent()) {
            return new ResponseEntity<>(patient.get(), HttpStatus.OK);
        }
        return new ResponseEntity<>("Patient not found", HttpStatus.NOT_FOUND);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updatePatient(@PathVariable UUID id, @RequestBody Patient patient) {
        String message = patientService.updatePatient(id, patient);
        if ("Patient is updated successfully".equals(message)) {
            return new ResponseEntity<>(message, HttpStatus.OK);
        }
        if ("Patient not found".equals(message)) {
            return new ResponseEntity<>(message, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(message, HttpStatus.CONFLICT);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deletePatient(@PathVariable UUID id) {
        String message = patientService.deletePatient(id);
        if ("Patient is deleted successfully".equals(message)) {
            return new ResponseEntity<>(message, HttpStatus.OK);
        }
        if ("Patient not found".equals(message)) {
            return new ResponseEntity<>(message, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(message, HttpStatus.CONFLICT);
    }
}
