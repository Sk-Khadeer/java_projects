package com.employee.repo;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.employee.entity.AppliedLeave;

public interface AppliedLeaveDAO extends JpaRepository<AppliedLeave, Long> {

Optional<AppliedLeave> findById(Long id);
	List<AppliedLeave> findByEmpCode(String emp_code);

	AppliedLeave findByEmpCodeAndStartDate(String empCode, LocalDate startDate);
	void deleteByEmpCode(String s);

}
