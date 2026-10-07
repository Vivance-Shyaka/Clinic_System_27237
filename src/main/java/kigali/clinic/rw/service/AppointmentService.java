package kigali.clinic.rw.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kigali.clinic.rw.domain.Appointment;
import kigali.clinic.rw.domain.AppointmentStatus;
import kigali.clinic.rw.domain.Doctor;
import kigali.clinic.rw.domain.Patient;
import kigali.clinic.rw.repository.AppointmentRepository;
import kigali.clinic.rw.repository.DoctorRepository;
import kigali.clinic.rw.repository.PatientRepository;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    public AppointmentService(
            AppointmentRepository appointmentRepository,
            DoctorRepository doctorRepository,
            PatientRepository patientRepository) {
        this.appointmentRepository = appointmentRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
    }

    public String saveAppointment(Appointment appointment, UUID doctorId, UUID patientId) {
        Optional<Doctor> requestedDoctor = doctorRepository.findById(doctorId);
        if (requestedDoctor.isEmpty()) {
            return "Doctor not found";
        }

        Optional<Patient> requestedPatient = patientRepository.findById(patientId);
        if (requestedPatient.isEmpty()) {
            return "Patient not found";
        }

        if (appointment.getAppointmentDate() == null) {
            return "appointmentDate is required";
        }

        AppointmentStatus status = appointment.getStatus() != null
                ? appointment.getStatus()
                : AppointmentStatus.SCHEDULED;

        if (status != AppointmentStatus.CANCELLED
                && appointmentRepository.existsByDoctorAndAppointmentDateAndStatusNot(
                        requestedDoctor.get(), appointment.getAppointmentDate(), AppointmentStatus.CANCELLED)) {
            return "Doctor is already booked on that date";
        }

        Appointment newAppointment = new Appointment();
        newAppointment.setDoctor(requestedDoctor.get());
        newAppointment.setPatient(requestedPatient.get());
        newAppointment.setAppointmentDate(appointment.getAppointmentDate());
        newAppointment.setReason(appointment.getReason());
        newAppointment.setStatus(status);
        appointmentRepository.save(newAppointment);

        return "Appointment is saved successfully";
    }

    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    public Optional<Appointment> getAppointmentById(UUID id) {
        return appointmentRepository.findById(id);
    }

    public Optional<List<Appointment>> getAppointmentsByDoctor(UUID doctorId) {
        return doctorRepository.findById(doctorId)
                .map(appointmentRepository::findByDoctor);
    }

    public Optional<List<Appointment>> getAppointmentsByPatient(UUID patientId) {
        return patientRepository.findById(patientId)
                .map(appointmentRepository::findByPatient);
    }

    public List<Appointment> getAppointmentsByStatus(AppointmentStatus status) {
        return appointmentRepository.findByStatusOrderByAppointmentDateAsc(status);
    }

    public List<Appointment> getAppointmentsBetween(LocalDate start, LocalDate end) {
        return appointmentRepository.findByAppointmentDateBetweenOrderByAppointmentDateAsc(start, end);
    }

    public List<Object[]> getAppointmentCountByStatus() {
        return appointmentRepository.countAppointmentsByStatus();
    }

    public Page<Appointment> getAppointmentsPage(Pageable pageable) {
        return appointmentRepository.findAll(pageable);
    }

    @Transactional
    public String cancelDoctorDay(UUID doctorId, LocalDate date) {
        int cancelled = appointmentRepository.cancelDoctorAppointmentsOnDate(doctorId, date);
        return cancelled + " appointments cancelled";
    }

    @Transactional
    public String deleteCancelledBefore(LocalDate date) {
        int deleted = appointmentRepository.deleteCancelledBefore(date);
        return deleted + " appointments deleted";
    }

    public String updateAppointment(UUID id, Appointment updated, UUID doctorId, UUID patientId) {
        Optional<Appointment> existingAppointment = appointmentRepository.findById(id);
        if (existingAppointment.isEmpty()) {
            return "Appointment not found";
        }

        Appointment appointment = existingAppointment.get();
        Doctor doctor = appointment.getDoctor();
        if (doctorId != null) {
            Optional<Doctor> requestedDoctor = doctorRepository.findById(doctorId);
            if (requestedDoctor.isEmpty()) {
                return "Doctor not found";
            }
            doctor = requestedDoctor.get();
        }

        Patient patient = appointment.getPatient();
        if (patientId != null) {
            Optional<Patient> requestedPatient = patientRepository.findById(patientId);
            if (requestedPatient.isEmpty()) {
                return "Patient not found";
            }
            patient = requestedPatient.get();
        }

        if (updated.getAppointmentDate() == null) {
            return "appointmentDate is required";
        }

        AppointmentStatus status = updated.getStatus() != null
                ? updated.getStatus()
                : appointment.getStatus();

        if (appointmentRepository.existsByDoctorAndPatientAndAppointmentDateAndIdNot(
                doctor, patient, updated.getAppointmentDate(), id)) {
            return "Appointment already exists";
        }

        appointment.setDoctor(doctor);
        appointment.setPatient(patient);
        appointment.setAppointmentDate(updated.getAppointmentDate());
        appointment.setReason(updated.getReason());
        appointment.setStatus(status);
        appointmentRepository.save(appointment);

        return "Appointment is updated successfully";
    }

    public String deleteAppointment(UUID id) {
        if (!appointmentRepository.existsById(id)) {
            return "Appointment not found";
        }

        appointmentRepository.deleteById(id);
        return "Appointment is deleted successfully";
    }
}
