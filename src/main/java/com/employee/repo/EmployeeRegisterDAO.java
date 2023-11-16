package com.employee.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.employee.entity.EmployeeRegister;

@Repository
public interface EmployeeRegisterDAO extends JpaRepository<EmployeeRegister, Long> {
	EmployeeRegister findByEmail(String email);

	void deleteByEmail(String email);
	EmployeeRegister findByConfirmationToken(String token);

	EmployeeRegister getEmployeeByEmail(String email);
	@Query("SELECT e FROM EmployeeRegister e WHERE LOWER(e.firstname) LIKE LOWER(concat(?1, '%'))")
    List<EmployeeRegister> findByFirstnameIgnoreCase(String firstname);

}
