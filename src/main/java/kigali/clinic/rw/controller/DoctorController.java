package kigali.clinic.rw.controller;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.time.LocalDate;

import org.springframework.web.bind.annotation.PatchMapping;
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

import kigali.clinic.rw.domain.Doctor;
import kigali.clinic.rw.domain.Appointment;
import kigali.clinic.rw.service.DoctorService;

@RestController
@RequestMapping({"/api/doctors", "/api/doctor"})
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @PostMapping("/save")
    public ResponseEntity<?> saveDoctor(
            @RequestBody Doctor doctor,
            @RequestParam(required = false) UUID officeId,
            @RequestParam(required = false) List<UUID> specializationIds) {
        String message = doctorService.saveDoctor(doctor, officeId, specializationIds);
        if ("Doctor is saved successfully".equals(message)) {
            return new ResponseEntity<>(message, HttpStatus.OK);
        }
        return new ResponseEntity<>(message, errorStatus(message));
    }

    @GetMapping("/all")
    public ResponseEntity<List<Doctor>> getAllDoctors() {
        return new ResponseEntity<>(doctorService.getAllDoctors(), HttpStatus.OK);
    }

    @GetMapping("/by-specialization")
    public ResponseEntity<List<Doctor>> getDoctorsBySpecialization(@RequestParam String name) {
        return new ResponseEntity<>(doctorService.getDoctorsBySpecializationName(name), HttpStatus.OK);
    }

    @GetMapping("/without-office")
    public ResponseEntity<List<Doctor>> getDoctorsWithoutOffice() {
        return new ResponseEntity<>(doctorService.getDoctorsWithoutOffice(), HttpStatus.OK);
    }

    @PutMapping("/{doctorId}/specializations/{specializationId}")
    public ResponseEntity<String> addSpecialization(
            @PathVariable UUID doctorId,
            @PathVariable UUID specializationId) {
        String message = doctorService.addSpecialization(doctorId, specializationId);
        if ("Specialization added to the doctor".equals(message)) {
            return new ResponseEntity<>(message, HttpStatus.OK);
        }
        return new ResponseEntity<>(message, errorStatus(message));
    }

    @DeleteMapping("/{doctorId}/specializations/{specializationId}")
    public ResponseEntity<String> removeSpecialization(
            @PathVariable UUID doctorId,
            @PathVariable UUID specializationId) {
        String message = doctorService.removeSpecialization(doctorId, specializationId);
        if ("Specialization removed from the doctor".equals(message)) {
            return new ResponseEntity<>(message, HttpStatus.OK);
        }
        return new ResponseEntity<>(message, errorStatus(message));
    }

    @PatchMapping("/{doctorId}/office")
    public ResponseEntity<String> changeOffice(
            @PathVariable UUID doctorId,
            @RequestParam int officeNumber) {
        String message = doctorService.changeOffice(doctorId, officeNumber);
        if ("Office changed successfully".equals(message)) {
            return new ResponseEntity<>(message, HttpStatus.OK);
        }
        return new ResponseEntity<>(message, errorStatus(message));
    }

    @GetMapping("/{doctorId}/agenda")
    public ResponseEntity<?> getAgenda(
            @PathVariable UUID doctorId,
            @RequestParam String date) {
        Optional<List<Appointment>> appointments =
                doctorService.getAgenda(doctorId, LocalDate.parse(date));
        if (appointments.isEmpty()) {
            return new ResponseEntity<>("The doctor with that id does not exist", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(appointments.get(), HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Doctor>> searchDoctors(@RequestParam String name) {
        return new ResponseEntity<>(doctorService.searchDoctors(name), HttpStatus.OK);
    }

    @GetMapping("/workload")
    public ResponseEntity<List<Object[]>> getWorkload(
            @RequestParam String start,
            @RequestParam String end) {
        return new ResponseEntity<>(
                doctorService.getWorkload(LocalDate.parse(start), LocalDate.parse(end)), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDoctorById(@PathVariable UUID id) {
        Optional<Doctor> doctor = doctorService.getDoctorById(id);
        if (doctor.isPresent()) {
            return new ResponseEntity<>(doctor.get(), HttpStatus.OK);
        }
        return new ResponseEntity<>("Doctor not found", HttpStatus.NOT_FOUND);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateDoctor(
            @PathVariable UUID id,
            @RequestBody Doctor doctor,
            @RequestParam(required = false) UUID officeId) {
        String message = doctorService.updateDoctor(id, doctor, officeId);
        if ("Doctor is updated successfully".equals(message)) {
            return new ResponseEntity<>(message, HttpStatus.OK);
        }
        return new ResponseEntity<>(message, errorStatus(message));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteDoctor(@PathVariable UUID id) {
        String message = doctorService.deleteDoctor(id);
        if ("Doctor is deleted successfully".equals(message)) {
            return new ResponseEntity<>(message, HttpStatus.OK);
        }
        return new ResponseEntity<>(message, errorStatus(message));
    }

    private HttpStatus errorStatus(String message) {
        if (message.endsWith("not found") || message.endsWith("does not exist")) {
            return HttpStatus.NOT_FOUND;
        }
        if (message.endsWith("are required") || message.endsWith("is required")) {
            return HttpStatus.BAD_REQUEST;
        }
        return HttpStatus.CONFLICT;
    }
}
