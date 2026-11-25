package kigali.clinic.rw.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import kigali.clinic.rw.domain.Office;
import kigali.clinic.rw.repository.DoctorRepository;
import kigali.clinic.rw.repository.OfficeRepository;

@Service
public class OfficeService {

    private final OfficeRepository officeRepository;
    private final DoctorRepository doctorRepository;

    public OfficeService(OfficeRepository officeRepository, DoctorRepository doctorRepository) {
        this.officeRepository = officeRepository;
        this.doctorRepository = doctorRepository;
    }

    public String saveOffice(Office office) {
        if (officeRepository.existsByOfficeNumber(office.getOfficeNumber())) {
            return "Office with this office number already exists";
        }

        officeRepository.save(office);
        return "Office is saved successfully";
    }

    public List<Office> getAllOffices() {
        return officeRepository.findAll();
    }

    public Optional<Office> getOfficeById(UUID id) {
        return officeRepository.findById(id);
    }

    public List<Office> getFreeOffices() {
        return officeRepository.findFreeOffices();
    }

    public Optional<Object[]> getBusiestOffice() {
        List<Object[]> offices = officeRepository.findOfficesByAppointmentCount(PageRequest.of(0, 1));
        if (offices.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(offices.get(0));
    }

    public String updateOffice(UUID id, Office updatedOffice) {
        Optional<Office> existingOffice = officeRepository.findById(id);
        if (existingOffice.isEmpty()) {
            return "Office not found";
        }

        if (officeRepository.existsByOfficeNumberAndIdNot(updatedOffice.getOfficeNumber(), id)) {
            return "Office with this office number already exists";
        }

        Office office = existingOffice.get();
        office.setName(updatedOffice.getName());
        office.setOfficeNumber(updatedOffice.getOfficeNumber());
        officeRepository.save(office);

        return "Office is updated successfully";
    }

    public String deleteOffice(UUID id) {
        Optional<Office> existingOffice = officeRepository.findById(id);
        if (existingOffice.isEmpty()) {
            return "Office not found";
        }

        if (doctorRepository.existsByOffice(existingOffice.get())) {
            return "Office is assigned to a doctor and cannot be deleted";
        }

        officeRepository.deleteById(id);
        return "Office is deleted successfully";
    }
    public String deleteOfficeByOfficeNumber(int officeNumber){
        Optional<Office> existingOffice = officeRepository.findByOfficeNumber(officeNumber);

     if (existingOffice.isEmpty()) {
            return "Office not found";
        }
        officeRepository.delete(existingOffice.get());
        return "Office is deleted successfully";
    }
}
