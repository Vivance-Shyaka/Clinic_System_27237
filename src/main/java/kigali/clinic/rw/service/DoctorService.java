package kigali.clinic.rw.service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kigali.clinic.rw.domain.Doctor;
import kigali.clinic.rw.domain.Office;
import kigali.clinic.rw.domain.Specialization;
import kigali.clinic.rw.repository.AppointmentRepository;
import kigali.clinic.rw.repository.DoctorRepository;
import kigali.clinic.rw.repository.OfficeRepository;
import kigali.clinic.rw.repository.SpecializationRepository;

@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final OfficeRepository officeRepository;
    private final SpecializationRepository specializationRepository;
    private final AppointmentRepository appointmentRepository;

    public DoctorService(
            DoctorRepository doctorRepository,
            OfficeRepository officeRepository,
            SpecializationRepository specializationRepository,
            AppointmentRepository appointmentRepository) {
        this.doctorRepository = doctorRepository;
        this.officeRepository = officeRepository;
        this.specializationRepository = specializationRepository;
        this.appointmentRepository = appointmentRepository;
    }

    @Transactional
    public String saveDoctor(Doctor doctor, UUID officeId, List<UUID> specializationIds) {
        if (officeId == null) {
            return "officeId is required";
        }

        if (doctor.getFirstName() == null || doctor.getFirstName().isBlank()
                || doctor.getLastName() == null || doctor.getLastName().isBlank()
                || doctor.getDateOfBirth() == null) {
            return "firstName, lastName and dateOfBirth are required";
        }

        if (doctorRepository.existsByFirstNameAndLastNameAndDateOfBirth(
                doctor.getFirstName(), doctor.getLastName(), doctor.getDateOfBirth())) {
            return "Doctor already exists";
        }

        Optional<Office> requestedOffice = officeRepository.findById(officeId);
        if (requestedOffice.isEmpty()) {
            return "Office not found";
        }

        Office office = requestedOffice.get();
        if (doctorRepository.existsByOffice(office)) {
            return "Office is already assigned to another doctor";
        }

        List<Specialization> specializations = new ArrayList<>();
        if (specializationIds != null) {
            Set<UUID> uniqueIds = new LinkedHashSet<>(specializationIds);
            for (UUID specializationId : uniqueIds) {
                Optional<Specialization> specialization = specializationRepository.findById(specializationId);
                if (specialization.isEmpty()) {
                    return "Specialization not found";
                }
                specializations.add(specialization.get());
            }
        }

        Doctor newDoctor = new Doctor();
        newDoctor.setFirstName(doctor.getFirstName());
        newDoctor.setLastName(doctor.getLastName());
        newDoctor.setDateOfBirth(doctor.getDateOfBirth());
        newDoctor.setOffice(office);
        Doctor savedDoctor = doctorRepository.save(newDoctor);

        for (Specialization specialization : specializations) {
            specialization.getDoctors().add(savedDoctor);
            specializationRepository.save(specialization);
        }

        return "Doctor is saved successfully";
    }

    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }

    public Optional<Doctor> getDoctorById(UUID id) {
        return doctorRepository.findById(id);
    }

    public List<Doctor> getDoctorsBySpecializationName(String name) {
        return doctorRepository.findBySpecializationName(name);
    }

    public List<Doctor> getDoctorsWithoutOffice() {
        return doctorRepository.findDoctorsWithoutOffice();
    }

    public String updateDoctor(UUID id, Doctor updatedDoctor, UUID officeId) {
        Optional<Doctor> existingDoctor = doctorRepository.findById(id);
        if (existingDoctor.isEmpty()) {
            return "Doctor not found";
        }

        if (updatedDoctor.getFirstName() == null || updatedDoctor.getFirstName().isBlank()
                || updatedDoctor.getLastName() == null || updatedDoctor.getLastName().isBlank()
                || updatedDoctor.getDateOfBirth() == null) {
            return "firstName, lastName and dateOfBirth are required";
        }

        if (doctorRepository.existsByFirstNameAndLastNameAndDateOfBirthAndIdNot(
                updatedDoctor.getFirstName(),
                updatedDoctor.getLastName(),
                updatedDoctor.getDateOfBirth(),
                id)) {
            return "Doctor already exists";
        }

        Doctor doctor = existingDoctor.get();
        if (officeId != null) {
            Optional<Office> requestedOffice = officeRepository.findById(officeId);
            if (requestedOffice.isEmpty()) {
                return "Office not found";
            }
            if (doctorRepository.existsByOfficeAndIdNot(requestedOffice.get(), id)) {
                return "Office is already assigned to another doctor";
            }
            doctor.setOffice(requestedOffice.get());
        }

        doctor.setFirstName(updatedDoctor.getFirstName());
        doctor.setLastName(updatedDoctor.getLastName());
        doctor.setDateOfBirth(updatedDoctor.getDateOfBirth());
        doctorRepository.save(doctor);

        return "Doctor is updated successfully";
    }

    @Transactional
    public String deleteDoctor(UUID id) {
        Optional<Doctor> existingDoctor = doctorRepository.findById(id);
        if (existingDoctor.isEmpty()) {
            return "Doctor not found";
        }

        Doctor doctor = existingDoctor.get();
        if (appointmentRepository.existsByDoctor(doctor)) {
            return "Doctor has appointments and cannot be deleted";
        }

        for (Specialization specialization : new ArrayList<>(doctor.getSpecializations())) {
            specialization.getDoctors().remove(doctor);
        }

        doctorRepository.delete(doctor);
        return "Doctor is deleted successfully";
    }
}
