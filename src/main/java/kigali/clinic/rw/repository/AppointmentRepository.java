package kigali.clinic.rw.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import kigali.clinic.rw.domain.Appointment;
import kigali.clinic.rw.domain.AppointmentStatus;
import kigali.clinic.rw.domain.Doctor;
import kigali.clinic.rw.domain.Patient;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    List<Appointment> findByDoctor(Doctor doctor);

    List<Appointment> findByPatient(Patient patient);

    boolean existsByDoctor(Doctor doctor);

    boolean existsByPatient(Patient patient);

    boolean existsByDoctorAndPatientAndAppointmentDateAndIdNot(
            Doctor doctor,
            Patient patient,
            LocalDate appointmentDate,
            UUID id);

    List<Appointment> findByStatusOrderByAppointmentDateAsc(AppointmentStatus status);

    List<Appointment> findByAppointmentDateBetweenOrderByAppointmentDateAsc(
            LocalDate start,
            LocalDate end);

    boolean existsByDoctorAndAppointmentDateAndStatusNot(
            Doctor doctor,
            LocalDate appointmentDate,
            AppointmentStatus status);

    @Query("SELECT a.status, COUNT(a) FROM Appointment a GROUP BY a.status")
    List<Object[]> countAppointmentsByStatus();

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Appointment a SET a.status = kigali.clinic.rw.domain.AppointmentStatus.CANCELLED "
            + "WHERE a.doctor.id = :doctorId AND a.appointmentDate = :date "
            + "AND a.status <> kigali.clinic.rw.domain.AppointmentStatus.COMPLETED "
            + "AND a.status <> kigali.clinic.rw.domain.AppointmentStatus.CANCELLED")
    int cancelDoctorAppointmentsOnDate(@Param("doctorId") UUID doctorId, @Param("date") LocalDate date);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Appointment a "
            + "WHERE a.status = kigali.clinic.rw.domain.AppointmentStatus.CANCELLED "
            + "AND a.appointmentDate < :date")
    int deleteCancelledBefore(@Param("date") LocalDate date);
}
