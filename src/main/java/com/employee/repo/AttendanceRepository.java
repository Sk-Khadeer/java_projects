package com.employee.repo;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.employee.entity.Attendance;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
//	Attendance findByDate(String date);
	 List<Attendance> findByEmployee_Email(String userEmail);
	Attendance findByDateAndEmployee_Email(Date date, String email);
}
