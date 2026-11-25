package kigali.clinic.rw.service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kigali.clinic.rw.domain.Doctor;
import kigali.clinic.rw.domain.Office;
import kigali.clinic.rw.domain.Specialization;
import kigali.clinic.rw.domain.Appointment;
import kigali.clinic.rw.domain.AppointmentStatus;
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
        newDoctor.setSpecializations(specializations);
        doctorRepository.save(newDoctor);

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

    @Transactional
    public String addSpecialization(UUID doctorId, UUID specializationId) {
        Optional<Doctor> requestedDoctor = doctorRepository.findById(doctorId);
        if (requestedDoctor.isEmpty()) {
            return "The doctor with that id does not exist";
        }

        Optional<Specialization> requestedSpecialization =
                specializationRepository.findById(specializationId);
        if (requestedSpecialization.isEmpty()) {
            return "That specialization does not exist";
        }

        Doctor doctor = requestedDoctor.get();
        Specialization specialization = requestedSpecialization.get();
        if (doctor.getSpecializations().contains(specialization)) {
            return "The doctor already has that specialization";
        }

        doctor.getSpecializations().add(specialization);
        doctorRepository.save(doctor);
        return "Specialization added to the doctor";
    }

    @Transactional
    public String removeSpecialization(UUID doctorId, UUID specializationId) {
        Optional<Doctor> requestedDoctor = doctorRepository.findById(doctorId);
        if (requestedDoctor.isEmpty()) {
            return "The doctor with that id does not exist";
        }

        Optional<Specialization> requestedSpecialization =
                specializationRepository.findById(specializationId);
        if (requestedSpecialization.isEmpty()) {
            return "That specialization does not exist";
        }

        Doctor doctor = requestedDoctor.get();
        if (!doctor.getSpecializations().remove(requestedSpecialization.get())) {
            return "The doctor does not have that specialization";
        }

        doctorRepository.save(doctor);
        return "Specialization removed from the doctor";
    }

    @Transactional
    public String changeOffice(UUID doctorId, int officeNumber) {
        Optional<Doctor> requestedDoctor = doctorRepository.findById(doctorId);
        if (requestedDoctor.isEmpty()) {
            return "The doctor with that id does not exist";
        }

        Optional<Office> requestedOffice = officeRepository.findByOfficeNumber(officeNumber);
        if (requestedOffice.isEmpty()) {
            return "The office with that office number does not exist";
        }

        Doctor doctor = requestedDoctor.get();
        Office office = requestedOffice.get();
        if (doctorRepository.existsByOfficeAndIdNot(office, doctorId)) {
            return "That office is taken by other doctor";
        }

        doctor.setOffice(office);
        doctorRepository.save(doctor);
        return "Office changed successfully";
    }

    public Optional<List<Appointment>> getAgenda(UUID doctorId, LocalDate date) {
        Optional<Doctor> doctor = doctorRepository.findById(doctorId);
        if (doctor.isEmpty()) {
            return Optional.empty();
        }
        List<Appointment> appointments = appointmentRepository
                .findByDoctorAndAppointmentDateAndStatusNotOrderByAppointmentDateAsc(
                        doctor.get(), date, AppointmentStatus.CANCELLED);
        return Optional.of(appointments);
    }

    public List<Doctor> searchDoctors(String name) {
        return doctorRepository.searchByName(name);
    }

    public List<Object[]> getWorkload(LocalDate start, LocalDate end) {
        return doctorRepository.findWorkloadByStatusAndDateBetween(
                AppointmentStatus.COMPLETED, start, end);
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

        doctor.getSpecializations().clear();
        doctorRepository.save(doctor);
        doctorRepository.delete(doctor);
        return "Doctor is deleted successfully";
    }
}
