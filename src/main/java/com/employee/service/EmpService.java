package com.employee.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.employee.entity.AppliedLeave;
import com.employee.entity.Attendance;
import com.employee.entity.EmployeeLeave;
import com.employee.entity.EmployeeRegister;
import com.employee.entity.LeaveRequestDTO;
import com.employee.entity.LeaveStatus;
import com.employee.entity.Status;
import com.employee.repo.AppliedLeaveDAO;
import com.employee.repo.AttendanceRepository;
import com.employee.repo.EmployeeLeaveDAO;
import com.employee.repo.EmployeeRegisterDAO;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
@Component
public class EmpService {

	@Autowired
	private EmployeeRegisterDAO employeeRegisterDAO;
	@Autowired
	private EmployeeLeaveDAO employeeLeaveDAO;
	@Autowired
	private AppliedLeaveDAO appliedLeaveDAO;

	@Autowired
	private EmailService emailService;

	 @Autowired
	    private AttendanceRepository attendanceRepository;
	public EmployeeRegister findByEmail(String email) {
		return employeeRegisterDAO.findByEmail(email);
	}

	public ResponseEntity<Map<String, Object>> loginUser(String email, String password) {
		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		EmployeeRegister user = employeeRegisterDAO.findByEmail(email);
System.out.println("ebntesdgbvg[=======");
		Map<String, Object> response = new HashMap<>();
		if (user == null) {
			response.put("status", "email_not_found");
		} else if ("unconfirmed".equals(user.getStatus())) {
			response.put("status", "Please check your email to confirm your registration before logging in.");
		} else if ("confirmed".equals(user.getStatus()) && passwordEncoder.matches(password, user.getPassword())) {
			// This is where you'd generate your token
			String jwtToken = generateToken(user); // Implement this function to generate the JWT token
			response.put("status", "Logged in successfully.");
			response.put("token", jwtToken); // Return the token
			response.put("email", user.getEmail());
			response.put("role", user.getRole());
			response.put("empcode", user.getEmpCode());
		} else {
			response.put("status", "invalid_credentials");
		}
		return ResponseEntity.ok(response);
	}

	public String generateToken(EmployeeRegister user) {
		String secretKey = "YOUR_SECRET_KEY_HERE"; // Make this a strong, unique key and consider storing it outside the
													// code.

		String jwtToken = Jwts.builder().setSubject(user.getEmail()).setIssuedAt(new Date())
				.setExpiration(new Date(System.currentTimeMillis() + 86400000)) // Token validity 1 day. Adjust as
																				// needed.
				.signWith(SignatureAlgorithm.HS256, secretKey).compact();

		return jwtToken;
	}

	public static final String USER_ROLE = "USER";
	public static final String ADMIN_ROLE = "ADMIN";

	@Transactional
	public EmployeeRegister registerEmployee(EmployeeRegister emp) {
		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		String rawPassword = emp.getPassword();
		String hashedPassword = passwordEncoder.encode(rawPassword);
		emp.setPassword(hashedPassword);
		emp.setConfirmationToken(UUID.randomUUID().toString());
		emp.setConfirmationTokenCreationDate(LocalDateTime.now());

		long userCount = employeeRegisterDAO.count();

		if (userCount == 0) {
			emp.setRole(ADMIN_ROLE);
		} else if (emp.getRole() == null || emp.getRole().trim().isEmpty()) {
			emp.setRole(USER_ROLE);
		}

		if (emp.getSubdesignation() != null && emp.getSubdesignation().isEmpty()) {
			emp.setSubdesignation(null);
		}

		try {
			emp = employeeRegisterDAO.save(emp); // Save the entity first, so it gets an ID
			emp.setEmpCode(emp.generateCode()); // Now generate the code using the ID
			emp = employeeRegisterDAO.saveAndFlush(emp); // Save the entity again with the generated code

			String loginOption = emp.getLoginOption();
			System.out.println(loginOption);
			
			if ("whatsapp".equals(loginOption)) {
				
				// Send WhatsApp message
				emailService.sendWhatsAppMessage(emp.getPhoneno().toString(),emp.getConfirmationToken(),emp.getEmail());
				
			} else if ("email".equals(loginOption)) {
				emailService.sendEmail(emp.getEmail(), emp.getConfirmationToken(), emp);
			}

			EmployeeRegister employeeRegister = employeeRegisterDAO.findByEmail(emp.getEmail());

			if (employeeRegister != null) {
				EmployeeLeave l = new EmployeeLeave();
				l.setTotalSickLeave(10);
				l.setTotalCasualLeave(15);
				l.setRemainingCasualLeaves(15);
				l.setRemainingSickLeaves(10);
				l.setUsedCasualLeaves(0);
				l.setUsedSickLeaves(0);
				l.setEmpCode(emp.getEmpCode());
				employeeLeaveDAO.save(l);
			}

		} catch (DataIntegrityViolationException e) {
			e.printStackTrace();
			throw new DataIntegrityViolationException("Email already exists! Please Login");
		}
		// Once the employee is registered, save their leave details

		return emp;
	}

	public EmployeeRegister updateEmployeePassword(String email, String newPassword) {
		// Retrieve the employee record from the database by email
		EmployeeRegister emp = employeeRegisterDAO.findByEmail(email);
		
		if (emp == null) {
			throw new EntityNotFoundException("No employee found with the given email.");
		}

		// Encode the new password
		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		String hashedPassword = passwordEncoder.encode(newPassword);
		emp.setPassword(hashedPassword);

		// Update the record in the database
		try {			
			emp = employeeRegisterDAO.saveAndFlush(emp);
		} catch (DataAccessException e) {
			e.printStackTrace();
			throw new RuntimeException("Error updating the password.");
		}

		return emp;
	}

	public List<EmployeeRegister> getAllEmployees() {
		return employeeRegisterDAO.findAll();
	}

	@Transactional
	public void deleteByEmail(String email) {
		employeeRegisterDAO.deleteByEmail(email);
	}

	@Transactional
	public boolean deleteEmployeeByEmail(String email) {
		try {

			EmployeeRegister emp = employeeRegisterDAO.findByEmail(email);
			String s = emp.getEmpCode();
			employeeRegisterDAO.deleteByEmail(email);
			employeeLeaveDAO.deleteByEmpCode(s);
			appliedLeaveDAO.deleteByEmpCode(s);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

//	@Transactional
//	public boolean otpToEmail(String email) {
//		try {
//			EmployeeRegister emp = employeeRegisterDAO.findByEmail(email);
//			if (emp == null) {
//				return false;
//
//			} else {
//				Random random = new Random();
//				int otp = 100000 + random.nextInt(900000);
//				emailService.sendEmail(emp.getEmail(), otp);
//				return true;
//			}
//
//		} catch (Exception e) {
//			e.printStackTrace();
//			return false;
//		}
//	}

	public List<EmployeeRegister> searchByFirstname(String firstName) {

		return employeeRegisterDAO.findByFirstnameIgnoreCase(firstName);
	}

	public EmployeeRegister getUserProfile(String email) {
		EmployeeRegister emp = employeeRegisterDAO.findByEmail(email);

		return emp;

	}

	public void saveProfileImage(String email, MultipartFile file) throws Exception {
		try {
			EmployeeRegister emp = employeeRegisterDAO.findByEmail(email);
			if (emp == null) {
				throw new Exception("User not found!");
			}
			emp.setProfileImage(file.getBytes());
			employeeRegisterDAO.save(emp);
		} catch (IOException e) {
			throw new Exception("Error saving image!");
		}
	}

	public void validateRoleAsAdmin(EmployeeRegister emp) throws Exception {
		if (!"ADMIN".equals(emp.getRole())) {
			throw new Exception("Only admins can perform this action!");
		}
	}

	public EmployeeRegister updateEmployeeByEmail(String email, EmployeeRegister updatedEmployee) {
		EmployeeRegister existingEmployee = employeeRegisterDAO.findByEmail(email);

		existingEmployee.setFirstname(updatedEmployee.getFirstname());
		existingEmployee.setLastname(updatedEmployee.getLastname());
		existingEmployee.setFathername(updatedEmployee.getFathername());
		existingEmployee.setPhoneno(updatedEmployee.getPhoneno());
		existingEmployee.setDesignation(updatedEmployee.getDesignation());
		existingEmployee.setSubdesignation(updatedEmployee.getSubdesignation());

		return employeeRegisterDAO.save(existingEmployee);
	}

	public EmployeeLeave getLeaveByEmpCode(String emp_code) {

		return employeeLeaveDAO.findByEmpCode(emp_code);
	}

	public void applyLeave(LeaveRequestDTO leaveRequest) throws Exception {

		EmployeeLeave leave = employeeLeaveDAO.findByEmpCode(leaveRequest.getEmpCode());

		if (leave == null) {
			throw new Exception("Employee leave data not found for the provided empCode");
		}
		leave.setStartDate(leaveRequest.getStartDate());
		leave.setEndDate(leaveRequest.getEndDate());

		if (leaveRequest.getStartDate().isAfter(leaveRequest.getEndDate())) {
			throw new Exception("Start date cannot be after end date.");
		}

		// Optional: check if the date range corresponds to the number of days applied
		// for
		long daysBetween = ChronoUnit.DAYS.between(leaveRequest.getStartDate(), leaveRequest.getEndDate()) + 1;
		if (daysBetween != leaveRequest.getDays()) {
			throw new Exception("Date range does not match the number of days applied for.");
		}

		if ("sick".equalsIgnoreCase(leaveRequest.getType())) {
			if (leave.getRemainingSickLeaves() >= leaveRequest.getDays()) {
				leave.setRemainingSickLeaves(leave.getRemainingSickLeaves() - leaveRequest.getDays());
				leave.setUsedSickLeaves(leave.getTotalSickLeave() - leave.getRemainingSickLeaves());
			} else {
				throw new Exception("Not enough sick leaves available");
			}

		} else if ("casual".equalsIgnoreCase(leaveRequest.getType())) {
			if (leave.getRemainingCasualLeaves() >= leaveRequest.getDays()) {
				leave.setRemainingCasualLeaves(leave.getRemainingCasualLeaves() - leaveRequest.getDays());
				leave.setUsedCasualLeaves(leave.getTotalCasualLeave() - leave.getRemainingCasualLeaves());
			} else {
				throw new Exception("Not enough casual leaves available");
			}
		}
		leave.setStatus(LeaveStatus.PENDING);

		// Add logic to save the reason for leave or any other operations

		employeeLeaveDAO.save(leave); // Save the modified leave count for the employee

		// Now, store the applied leave in the AppliedLeaves table
		AppliedLeave appliedLeave = new AppliedLeave();
		appliedLeave.setEmpCode(leaveRequest.getEmpCode());
		appliedLeave.setType(leaveRequest.getType());
		appliedLeave.setStartDate(leaveRequest.getStartDate());
		appliedLeave.setEndDate(leaveRequest.getEndDate());
		appliedLeave.setDays(leaveRequest.getDays());
		appliedLeave.setReason(leaveRequest.getReason());
		// Set the status for the AppliedLeave entity here:
		appliedLeave.setStatus(LeaveStatus.PENDING);

		appliedLeaveDAO.save(appliedLeave); // Assuming you have a DAO or repository for the AppliedLeaves table

	}

	public void approveLeave(LeaveRequestDTO leaveRequest) throws Exception {
		EmployeeLeave leave = employeeLeaveDAO.findByEmpCode(leaveRequest.getEmpCode());
		if (leave == null) {
			throw new Exception("Leave application not found.");
		}

		appliedLeaveDAO.findById(leaveRequest.getId()).ifPresent(appliedLeave -> {
			appliedLeave.setStatus(LeaveStatus.APPROVED);
			appliedLeaveDAO.save(appliedLeave);
		});

	}

	public void rejectLeave(LeaveRequestDTO leaveRequest) throws Exception {
		EmployeeLeave leave = employeeLeaveDAO.findByEmpCode(leaveRequest.getEmpCode());
		EmployeeLeave employeeLeave = employeeLeaveDAO.findByEmpCode(leaveRequest.getEmpCode());
		if (employeeLeave == null) {
			throw new Exception("Leave application not found.");
		}
		if (leave == null) {
			throw new Exception("Leave application not found.");
		}
		appliedLeaveDAO.findById(leaveRequest.getId()).ifPresent(appliedLeave -> {
			appliedLeave.setStatus(LeaveStatus.REJECTED);
			appliedLeaveDAO.save(appliedLeave);
			// Check the type of the leave and add the days back to the respective leave
			// type
			if (appliedLeave.getType().equalsIgnoreCase("SICK")) {
				employeeLeave.setRemainingSickLeaves(employeeLeave.getRemainingSickLeaves() + appliedLeave.getDays());
			} else if (appliedLeave.getType().equalsIgnoreCase("CASUAL")) {
				employeeLeave
						.setRemainingCasualLeaves(employeeLeave.getRemainingCasualLeaves() + appliedLeave.getDays());
			}

			employeeLeaveDAO.save(employeeLeave);
		});

	}

	public ResponseEntity<Map<String, String>> changePassword(String email, String password) {
		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		EmployeeRegister user = employeeRegisterDAO.findByEmail(email);
		Map<String, String> response = new HashMap<>();
		if (passwordEncoder.matches(password, user.getPassword())) {
			response.put("status", "Password is correct");
		} else {
			response.put("status", "Password is in_corrrect.");
		}

		return ResponseEntity.ok(response);
	}
	
	public Attendance markAttendance(Date date, Status status, String email, Date login, Date logout) {
	    
	    // First, let's check if there's already an attendance record for this date and user.
	    Attendance existingAttendance = attendanceRepository.findByDateAndEmployee_Email(date,email);
	    
	    if(existingAttendance != null) {
	    	System.out.println( existingAttendance.getEmployee());
	        // If there's an existing attendance, update its status and timestamps.
	        existingAttendance.setStatus(status);
	        if(status == Status.IN) {
	            existingAttendance.setLogin(login); // Set the login timestamp
	        } else if(status == Status.OUT) {
	            existingAttendance.setLogout(logout); // Set the logout timestamp
	        }
	        return attendanceRepository.save(existingAttendance);
	    } else {
	        // If there's no existing attendance for this date and user, create a new one.
	        Attendance newAttendance = new Attendance();
	        newAttendance.setDate(date);
	        newAttendance.setStatus(status);
	      newAttendance.setEmployee(null);	    
	      if(status == Status.IN) {
	            newAttendance.setLogin(login);
	        } else if(status == Status.OUT) {
	            newAttendance.setLogout(logout);
	        }
	        return attendanceRepository.save(newAttendance);
	    }
	}


	 public List<Attendance> getAttendanceHistory(String userEmail) {
	        // Fetch the attendance records from the database
	        return attendanceRepository.findByEmployee_Email(userEmail);
	    }
}
