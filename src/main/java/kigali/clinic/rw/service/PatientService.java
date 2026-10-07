package kigali.clinic.rw.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import kigali.clinic.rw.domain.Patient;
import kigali.clinic.rw.repository.AppointmentRepository;
import kigali.clinic.rw.repository.DoctorRepository;
import kigali.clinic.rw.repository.PatientRepository;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;

    public PatientService(
            PatientRepository patientRepository,
            DoctorRepository doctorRepository,
            AppointmentRepository appointmentRepository) {
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
    }

    public String savePatient(Patient patient) {
        if (patientRepository.existsByFirstNameAndLastNameAndDateOfBirth(
                patient.getFirstName(), patient.getLastName(), patient.getDateOfBirth())) {
            return "Patient already exists";
        }

        patientRepository.save(patient);
        return "Patient is saved successfully";
    }

    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    public Optional<Patient> getPatientById(UUID id) {
        return patientRepository.findById(id);
    }

    public List<Patient> getPatientsByLastName(String lastName) {
        return patientRepository.findByLastNameIgnoreCaseOrderByFirstNameAsc(lastName);
    }

    public Optional<List<Patient>> getPatientsOfDoctor(UUID doctorId) {
        return doctorRepository.findById(doctorId)
                .map(doctor -> patientRepository.findPatientsOfDoctor(doctorId));
    }

    public List<Patient> getFrequentPatients(long min) {
        return patientRepository.findFrequentPatients(min);
    }

    public String updatePatient(UUID id, Patient updatedPatient) {
        Optional<Patient> existingPatient = patientRepository.findById(id);
        if (existingPatient.isEmpty()) {
            return "Patient not found";
        }

        if (patientRepository.existsByFirstNameAndLastNameAndDateOfBirthAndIdNot(
                updatedPatient.getFirstName(),
                updatedPatient.getLastName(),
                updatedPatient.getDateOfBirth(),
                id)) {
            return "Patient already exists";
        }

        Patient patient = existingPatient.get();
        patient.setFirstName(updatedPatient.getFirstName());
        patient.setLastName(updatedPatient.getLastName());
        patient.setDateOfBirth(updatedPatient.getDateOfBirth());
        patientRepository.save(patient);

        return "Patient is updated successfully";
    }

    public String deletePatient(UUID id) {
        Optional<Patient> existingPatient = patientRepository.findById(id);
        if (existingPatient.isEmpty()) {
            return "Patient not found";
        }

        if (appointmentRepository.existsByPatient(existingPatient.get())) {
            return "Patient has appointments and cannot be deleted";
        }

        patientRepository.deleteById(id);
        return "Patient is deleted successfully";
    }
}
