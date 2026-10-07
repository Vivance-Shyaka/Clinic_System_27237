package kigali.clinic.rw.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kigali.clinic.rw.domain.Doctor;
import kigali.clinic.rw.domain.Specialization;
import kigali.clinic.rw.repository.DoctorRepository;
import kigali.clinic.rw.repository.SpecializationRepository;

@Service
public class SpecializationService {

    private final SpecializationRepository specializationRepository;
    private final DoctorRepository doctorRepository;

    public SpecializationService(
            SpecializationRepository specializationRepository,
            DoctorRepository doctorRepository) {
        this.specializationRepository = specializationRepository;
        this.doctorRepository = doctorRepository;
    }

    public String saveSpecialization(Specialization specialization) {
        if (specialization.getName() == null || specialization.getName().isBlank()) {
            return "Specialization name is required";
        }

        String name = specialization.getName().trim();
        if (specializationRepository.existsByNameIgnoreCase(name)) {
            return "Specialization already exists";
        }

        Specialization newSpecialization = new Specialization();
        newSpecialization.setName(name);
        specializationRepository.save(newSpecialization);

        return "Specialization is saved successfully";
    }

    public List<Specialization> getAllSpecializations() {
        return specializationRepository.findAll();
    }

    public Optional<Specialization> getSpecializationById(UUID id) {
        return specializationRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<List<Doctor>> getDoctorsBySpecialization(UUID id) {
        return specializationRepository.findById(id)
                .map(specialization -> new ArrayList<>(specialization.getDoctors()));
    }

    public List<Specialization> getUnusedSpecializations() {
        return specializationRepository.findUnusedSpecializations();
    }

    public String updateSpecialization(UUID id, Specialization updatedSpecialization) {
        Optional<Specialization> existingSpecialization = specializationRepository.findById(id);
        if (existingSpecialization.isEmpty()) {
            return "Specialization not found";
        }

        if (updatedSpecialization.getName() == null || updatedSpecialization.getName().isBlank()) {
            return "Specialization name is required";
        }

        String name = updatedSpecialization.getName().trim();
        if (specializationRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            return "Specialization already exists";
        }

        Specialization specialization = existingSpecialization.get();
        specialization.setName(name);
        specializationRepository.save(specialization);

        return "Specialization is updated successfully";
    }

    public String deleteSpecialization(UUID id) {
        if (!specializationRepository.existsById(id)) {
            return "Specialization not found";
        }

        specializationRepository.deleteById(id);
        return "Specialization is deleted successfully";
    }

    @Transactional
    public String addDoctor(UUID specializationId, UUID doctorId) {
        Optional<Specialization> requestedSpecialization = specializationRepository.findById(specializationId);
        if (requestedSpecialization.isEmpty()) {
            return "Specialization not found";
        }

        Optional<Doctor> requestedDoctor = doctorRepository.findById(doctorId);
        if (requestedDoctor.isEmpty()) {
            return "Doctor not found";
        }

        Specialization specialization = requestedSpecialization.get();
        Doctor doctor = requestedDoctor.get();
        if (specialization.getDoctors().contains(doctor)) {
            return "Doctor already has this specialization";
        }

        specialization.getDoctors().add(doctor);
        specializationRepository.save(specialization);
        return "Doctor is added to specialization successfully";
    }

    @Transactional
    public String removeDoctor(UUID specializationId, UUID doctorId) {
        Optional<Specialization> requestedSpecialization = specializationRepository.findById(specializationId);
        if (requestedSpecialization.isEmpty()) {
            return "Specialization not found";
        }

        Optional<Doctor> requestedDoctor = doctorRepository.findById(doctorId);
        if (requestedDoctor.isEmpty()) {
            return "Doctor not found";
        }

        Specialization specialization = requestedSpecialization.get();
        if (!specialization.getDoctors().remove(requestedDoctor.get())) {
            return "Doctor does not have this specialization";
        }

        specializationRepository.save(specialization);
        return "Doctor is removed from specialization successfully";
    }
}
