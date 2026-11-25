package kigali.clinic.rw.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import kigali.clinic.rw.domain.Appointment;
import kigali.clinic.rw.domain.AppointmentStatus;
import kigali.clinic.rw.domain.Prescription;
import kigali.clinic.rw.repository.AppointmentRepository;
import kigali.clinic.rw.repository.PrescriptionRepository;

@Service
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final AppointmentRepository appointmentRepository;

    public PrescriptionService(
            PrescriptionRepository prescriptionRepository,
            AppointmentRepository appointmentRepository) {
        this.prescriptionRepository = prescriptionRepository;
        this.appointmentRepository = appointmentRepository;
    }

    public String savePrescription(Prescription request, UUID appointmentId) {
        Optional<Appointment> requestedAppointment = appointmentRepository.findById(appointmentId);
        if (requestedAppointment.isEmpty()) {
            return "The appointment with that id does not exist";
        }
        if (requestedAppointment.get().getStatus() != AppointmentStatus.COMPLETED) {
            return "Prescriptions are only for COMPLETED appointments";
        }

        Prescription prescription = new Prescription();
        prescription.setMedicineName(request.getMedicineName());
        prescription.setDosage(request.getDosage());
        prescription.setDurationDays(request.getDurationDays());
        prescription.setIssuedDate(LocalDate.now());
        prescription.setAppointment(requestedAppointment.get());
        prescriptionRepository.save(prescription);
        return "Prescription saved successfully";
    }

    public Optional<List<Prescription>> getPrescriptionsForAppointment(UUID appointmentId) {
        Optional<Appointment> appointment = appointmentRepository.findById(appointmentId);
        if (appointment.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(prescriptionRepository.findByAppointment(appointment.get()));
    }

    public List<Prescription> getPatientHistory(String ssNumber) {
        return prescriptionRepository.findHistoryByPatientSsNumber(ssNumber);
    }

    public String updatePrescription(UUID id, Prescription updated) {
        Optional<Prescription> existingPrescription = prescriptionRepository.findById(id);
        if (existingPrescription.isEmpty()) {
            return "The prescription with that id does not exist";
        }

        Prescription prescription = existingPrescription.get();
        prescription.setMedicineName(updated.getMedicineName());
        prescription.setDosage(updated.getDosage());
        prescription.setDurationDays(updated.getDurationDays());
        prescriptionRepository.save(prescription);
        return "Prescription updated successfully";
    }

    public String deletePrescription(UUID id) {
        Optional<Prescription> existingPrescription = prescriptionRepository.findById(id);
        if (existingPrescription.isEmpty()) {
            return "The prescription with that id does not exist";
        }
        prescriptionRepository.delete(existingPrescription.get());
        return "Prescription deleted successfully";
    }

    public List<Object[]> getTopMedicines() {
        return prescriptionRepository.findTopMedicines();
    }
}
