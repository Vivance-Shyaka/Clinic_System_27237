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
import kigali.clinic.rw.repository.PrescriptionRepository;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final PrescriptionRepository prescriptionRepository;

    public AppointmentService(
            AppointmentRepository appointmentRepository,
            DoctorRepository doctorRepository,
            PatientRepository patientRepository,
            PrescriptionRepository prescriptionRepository) {
        this.appointmentRepository = appointmentRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.prescriptionRepository = prescriptionRepository;
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

        if (appointment.getAppointmentDate().isBefore(LocalDate.now())) {
            return "Appointment date cannot be in the past";
        }

        if (appointmentRepository.existsByDoctorAndAppointmentDateAndStatusNot(
                requestedDoctor.get(), appointment.getAppointmentDate(), AppointmentStatus.CANCELLED)) {
            return "Doctor is already booked on that date";
        }

        Appointment newAppointment = new Appointment();
        newAppointment.setDoctor(requestedDoctor.get());
        newAppointment.setPatient(requestedPatient.get());
        newAppointment.setAppointmentDate(appointment.getAppointmentDate());
        newAppointment.setReason(appointment.getReason());
        newAppointment.setStatus(AppointmentStatus.SCHEDULED);
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
        Optional<Doctor> doctor = doctorRepository.findById(doctorId);
        if (doctor.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(appointmentRepository.findByDoctor(doctor.get()));
    }

    public Optional<List<Appointment>> getAppointmentsByPatient(UUID patientId) {
        Optional<Patient> patient = patientRepository.findById(patientId);
        if (patient.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(appointmentRepository.findByPatient(patient.get()));
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
        int cancelled = appointmentRepository.cancelDoctorAppointmentsOnDate(
                doctorId, date, AppointmentStatus.CANCELLED, AppointmentStatus.COMPLETED);
        return cancelled + " appointments cancelled";
    }

    @Transactional
    public String deleteCancelledBefore(LocalDate date) {
        int deleted = appointmentRepository.deleteAppointmentsWithStatusBefore(
                AppointmentStatus.CANCELLED, date);
        return deleted + " appointments deleted";
    }

    public String updateAppointmentStatus(UUID id, AppointmentStatus newStatus) {
        Optional<Appointment> existingAppointment = appointmentRepository.findById(id);
        if (existingAppointment.isEmpty()) {
            return "The appointment with that id does not exist";
        }

        Appointment appointment = existingAppointment.get();
        AppointmentStatus currentStatus = appointment.getStatus();
        boolean allowed = (currentStatus == AppointmentStatus.SCHEDULED
                && (newStatus == AppointmentStatus.CONFIRMED || newStatus == AppointmentStatus.CANCELLED))
                || (currentStatus == AppointmentStatus.CONFIRMED
                && (newStatus == AppointmentStatus.COMPLETED || newStatus == AppointmentStatus.CANCELLED));
        if (!allowed) {
            return "Cannot change status from " + currentStatus + " to " + newStatus;
        }

        appointment.setStatus(newStatus);
        appointmentRepository.save(appointment);
        return "Appointment status updated successfully";
    }

    public String rescheduleAppointment(UUID id, LocalDate newDate) {
        Optional<Appointment> existingAppointment = appointmentRepository.findById(id);
        if (existingAppointment.isEmpty()) {
            return "The appointment with that id does not exist";
        }

        Appointment appointment = existingAppointment.get();
        if (appointment.getStatus() == AppointmentStatus.COMPLETED
                || appointment.getStatus() == AppointmentStatus.CANCELLED) {
            return "A " + appointment.getStatus() + " appointment cannot be rescheduled";
        }
        if (newDate.isBefore(LocalDate.now())) {
            return "Appointment date cannot be in the past";
        }
        if (appointmentRepository.existsByDoctorAndAppointmentDateAndStatusNotAndIdNot(
                appointment.getDoctor(), newDate, AppointmentStatus.CANCELLED, id)) {
            return "Doctor is already booked on that date";
        }

        appointment.setAppointmentDate(newDate);
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointmentRepository.save(appointment);
        return "Appointment rescheduled successfully";
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

        if (appointmentRepository.existsByDoctorAndPatientAndAppointmentDateAndIdNot(
                doctor, patient, updated.getAppointmentDate(), id)) {
            return "Appointment already exists";
        }

        appointment.setDoctor(doctor);
        appointment.setPatient(patient);
        appointment.setAppointmentDate(updated.getAppointmentDate());
        appointment.setReason(updated.getReason());
        appointmentRepository.save(appointment);

        return "Appointment is updated successfully";
    }

    public String deleteAppointment(UUID id) {
        Optional<Appointment> existingAppointment = appointmentRepository.findById(id);
        if (existingAppointment.isEmpty()) {
            return "The appointment with that id does not exist";
        }
        if (prescriptionRepository.existsByAppointment(existingAppointment.get())) {
            return "That appointment has prescriptions, delete them first";
        }

        appointmentRepository.delete(existingAppointment.get());
        return "Appointment is deleted successfully";
    }
}
