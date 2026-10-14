package kigali.clinic.rw.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import kigali.clinic.rw.domain.Appointment;
import kigali.clinic.rw.domain.AppointmentStatus;
import kigali.clinic.rw.service.AppointmentService;

@RestController
@RequestMapping({"/api/appointments", "/api/appointment"})
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping("/save")
    public ResponseEntity<String> saveAppointment(
            @RequestBody Appointment appointment,
            @RequestParam UUID doctorId,
            @RequestParam UUID patientId) {
        String message = appointmentService.saveAppointment(appointment, doctorId, patientId);
        if ("Appointment is saved successfully".equals(message)) {
            return ResponseEntity.status(HttpStatus.CREATED).body(message);
        }
        return ResponseEntity.status(errorStatus(message)).body(message);
    }

    @GetMapping("/all")
    public ResponseEntity<List<Appointment>> getAllAppointments() {
        return ResponseEntity.ok(appointmentService.getAllAppointments());
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<String> updateAppointmentStatus(
            @PathVariable UUID id,
            @RequestParam AppointmentStatus status) {
        String message = appointmentService.updateAppointmentStatus(id, status);
        if ("Appointment status updated successfully".equals(message)) {
            return ResponseEntity.ok(message);
        }
        if (message.endsWith("does not exist")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
    }

    @PatchMapping("/{id}/reschedule")
    public ResponseEntity<String> rescheduleAppointment(
            @PathVariable UUID id,
            @RequestParam String newDate) {
        String message = appointmentService.rescheduleAppointment(id, LocalDate.parse(newDate));
        if ("Appointment rescheduled successfully".equals(message)) {
            return ResponseEntity.ok(message);
        }
        if (message.endsWith("does not exist")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
        }
        if ("Doctor is already booked on that date".equals(message)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(message);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getAppointmentById(@PathVariable UUID id) {
        Optional<Appointment> appointment = appointmentService.getAppointmentById(id);
        if (appointment.isPresent()) {
            return ResponseEntity.ok(appointment.get());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Appointment not found");
    }

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<?> getAppointmentsByDoctor(@PathVariable UUID doctorId) {
        Optional<List<Appointment>> appointments = appointmentService.getAppointmentsByDoctor(doctorId);
        if (appointments.isPresent()) {
            return ResponseEntity.ok(appointments.get());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Doctor not found");
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<?> getAppointmentsByPatient(@PathVariable UUID patientId) {
        Optional<List<Appointment>> appointments = appointmentService.getAppointmentsByPatient(patientId);
        if (appointments.isPresent()) {
            return ResponseEntity.ok(appointments.get());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Patient not found");
    }

    @GetMapping("/by-status")
    public ResponseEntity<List<Appointment>> getAppointmentsByStatus(@RequestParam AppointmentStatus status) {
        return ResponseEntity.ok(appointmentService.getAppointmentsByStatus(status));
    }

    @GetMapping("/between")
    public ResponseEntity<List<Appointment>> getAppointmentsBetween(
            @RequestParam String start,
            @RequestParam String end) {
        LocalDate startDate = LocalDate.parse(start);
        LocalDate endDate = LocalDate.parse(end);
        return ResponseEntity.ok(appointmentService.getAppointmentsBetween(startDate, endDate));
    }

    @GetMapping("/stats/by-status")
    public ResponseEntity<List<Object[]>> getAppointmentCountByStatus() {
        return ResponseEntity.ok(appointmentService.getAppointmentCountByStatus());
    }

    @GetMapping("/page")
    public ResponseEntity<Page<Appointment>> getAppointmentsPage(
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam(defaultValue = "appointmentDate,asc") String sort) {
        String[] sortParts = sort.split(",");
        Sort.Direction direction = sortParts.length > 1
                ? Sort.Direction.fromString(sortParts[1])
                : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortParts[0]));
        return ResponseEntity.ok(appointmentService.getAppointmentsPage(pageable));
    }

    @PatchMapping("/cancel-day")
    public ResponseEntity<String> cancelDoctorDay(@RequestParam UUID doctorId, @RequestParam String date) {
        return ResponseEntity.ok(appointmentService.cancelDoctorDay(doctorId, LocalDate.parse(date)));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<String> updateAppointment(
            @PathVariable UUID id,
            @RequestBody Appointment appointment,
            @RequestParam(required = false) UUID doctorId,
            @RequestParam(required = false) UUID patientId) {
        String message = appointmentService.updateAppointment(id, appointment, doctorId, patientId);
        if ("Appointment is updated successfully".equals(message)) {
            return ResponseEntity.ok(message);
        }
        return ResponseEntity.status(errorStatus(message)).body(message);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteAppointment(@PathVariable UUID id) {
        String message = appointmentService.deleteAppointment(id);
        if ("Appointment is deleted successfully".equals(message)) {
            return ResponseEntity.ok(message);
        }
        if (message.endsWith("does not exist")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
        }
        return ResponseEntity.status(HttpStatus.CONFLICT).body(message);
    }

    @DeleteMapping("/cancelled-before")
    public ResponseEntity<String> deleteCancelledBefore(@RequestParam String date) {
        return ResponseEntity.ok(appointmentService.deleteCancelledBefore(LocalDate.parse(date)));
    }

    private HttpStatus errorStatus(String message) {
        if (message.endsWith("not found") || message.endsWith("does not exist")) {
            return HttpStatus.NOT_FOUND;
        }
        if (message.endsWith("is required") || message.equals("Appointment date cannot be in the past")) {
            return HttpStatus.BAD_REQUEST;
        }
        return HttpStatus.CONFLICT;
    }
}
