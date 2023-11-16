package com.employee.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.employee.entity.EmployeeLeave;

@Repository
public interface EmployeeLeaveDAO extends JpaRepository<EmployeeLeave, Long> {
	  EmployeeLeave findByEmpCode(String emp_code);

	void deleteByEmpCode(String s);

}
