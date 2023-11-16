package com.employee.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.employee.entity.AppliedLeave;
import com.employee.entity.Attendance;
import com.employee.entity.AttendanceRequest;
import com.employee.entity.EmployeeLeave;
import com.employee.entity.EmployeeRegister;
import com.employee.entity.LeaveRequestDTO;
import com.employee.entity.OtpRequest;
import com.employee.entity.Response;
import com.employee.entity.Status;
import com.employee.repo.AppliedLeaveDAO;
import com.employee.repo.EmployeeRegisterDAO;
import com.employee.service.EmpService;
import com.employee.service.OtpService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.persistence.EntityNotFoundException;

@RestController
//@CrossOrigin(origins = "http://127.0.0.1:5500")
@CrossOrigin(origins = { "http://127.0.0.1:5500", "https://789d-14-140-84-6.ngrok-free.app" })

public class EmpController {
	@Autowired
	private EmpService empService;
	@Autowired
	private EmployeeRegisterDAO employeeRegisterDAO;
	@Autowired
	private AppliedLeaveDAO appliedLeaveDAO;

	@Autowired
	private OtpService otpService;

	@PostMapping("/login")
	public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> body) {
		String email = body.get("email");
		String password = body.get("password");

		// Directly return the ResponseEntity you get from the service
		return empService.loginUser(email, password);
	}

	@PostMapping("/changePassword")
	public ResponseEntity<Map<String, String>> changePassword(@RequestBody Map<String, String> body) {
		String email = body.get("email");
		String password = body.get("password");
		System.out.println(email + " " + password);
		return empService.changePassword(email, password);
	}

	@GetMapping("/history")
	public ResponseEntity<List<Attendance>> getAttendanceHistory(@RequestHeader String userEmail) {
		List<Attendance> records = empService.getAttendanceHistory(userEmail);
		return ResponseEntity.ok(records);
	}

	@GetMapping("/confirm")
	public ResponseEntity<Map<String, String>> confirmEmail(@RequestParam("token") String token) {
		EmployeeRegister user = employeeRegisterDAO.findByConfirmationToken(token);

		Map<String, String> response = new HashMap<>();
		if (user == null) {
			response.put("status", "Invalid confirmation link.");
			return ResponseEntity.badRequest().body(response);
		}
		if (user.getConfirmationTokenCreationDate().plusHours(24).isBefore(LocalDateTime.now())) {
			response.put("status", "The confirmation link has expired. Please request a new one.");
			user.setConfirmationToken(null);
			return ResponseEntity.badRequest().body(response);
		}

		if ("confirmed".equals(user.getStatus())) {
			response.put("status", "Your email is already confirmed. Please log in with your credentials.");
			return ResponseEntity.ok(response);
		}
		user.setStatus("confirmed");
//		user.setConfirmationToken(null);
		employeeRegisterDAO.save(user);

		response.put("status", "Email confirmed successfully!");
		return ResponseEntity.ok(response);
	}

	@GetMapping("/confirmViaWhatsApp")
	public ResponseEntity<Map<String, String>> confirmViaWhatsApp(@RequestParam String email) {
		// Use the token to identify the user
		Map<String, String> response = new HashMap<>();
		System.out.println("emtered");
		EmployeeRegister user = employeeRegisterDAO.findByEmail(email);
		System.out.println("got it");
		if (user == null) {
			response.put("status", "Invalid email.");
			return ResponseEntity.badRequest().body(response);
		}
		// Update the user's status to "confirmed"
		user.setStatus("confirmed");
		System.out.println("checked");
		employeeRegisterDAO.save(user);
		response.put("status", "Account confirmed successfully!");
		return ResponseEntity.ok(response);
	}

	@PostMapping("/saveEmployee")
	public ResponseEntity<Map<String, Object>> registerEmployee(@RequestBody EmployeeRegister emp) {
		Map<String, Object> res = new HashMap<>();
		try {
			empService.registerEmployee(emp);
			res.put("status", "Registration Successful");
			res.put("employee", emp);
			return ResponseEntity.ok(res);
		} catch (DataIntegrityViolationException ex) {
			res.put("status", "Failed");
			res.put("error", ex.getMessage()); // This will display "Email already exists!"
			return ResponseEntity.status(HttpStatus.CONFLICT).body(res); // 409 Conflict status code
		}
	}

	@GetMapping("/getEmployees")
	public List<EmployeeRegister> getAllEmployees() {
		return empService.getAllEmployees();
	}

	@GetMapping("/getEmployee")
	public EmployeeRegister getEmployeeByEmail(@RequestParam String email) {
		return empService.findByEmail(email);
	}

	@PostMapping("/deleteEmployee")
	public ResponseEntity<?> deleteEmployee(@RequestBody Map<String, String> json) {

		String email = json.get("email");
		try {
			boolean isDeleted = empService.deleteEmployeeByEmail(email);
			if (isDeleted) {
				return ResponseEntity.ok().body(new Response("success", "Employee deleted successfully"));
			} else {
				return ResponseEntity.status(400).body(new Response("error", "Error deleting employee"));
			}
		} catch (Exception e) {
			return ResponseEntity.status(500).body(new Response("error", "Server error"));
		}
	}

	@DeleteMapping("/deleteEmployee/{email}")
	public ResponseEntity<?> deleteEmployee(@PathVariable String email) {
		try {
			empService.deleteByEmail(email);
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace(); // This will print the error details in the server logs.
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/search")
	public List<EmployeeRegister> searchByFirstname(@RequestParam String firstname) {
		return empService.searchByFirstname(firstname);
	}

	@GetMapping("/getUserProfile")
	public ResponseEntity<?> getUserProfile(@RequestParam(required = false) String email) {
		if (email == null || email.isEmpty()) {
			return ResponseEntity.badRequest().body("Email parameter is missing");
		}
		EmployeeRegister e = empService.getUserProfile(email);
		if (e == null) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(e);
	}

	@PutMapping("/updateByEmail")
	public ResponseEntity<EmployeeRegister> updateEmployeeByEmail(@RequestBody EmployeeRegister employee) {
		try {
			EmployeeRegister updatedEmployee = empService.updateEmployeeByEmail(employee.getEmail(), employee);
			return new ResponseEntity<>(updatedEmployee, HttpStatus.OK);
		} catch (RuntimeException e) {
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping("/uploadImage")
	public ResponseEntity<String> uploadProfileImage(@RequestParam("email") String email,
			@RequestParam("file") MultipartFile file) {
		try {
			empService.saveProfileImage(email, file);
			return ResponseEntity.ok("Profile image uploaded successfully");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
	}

	@GetMapping("/getImage")
	public ResponseEntity<byte[]> getProfileImage(@RequestParam("email") String email) {
		EmployeeRegister userProfile = employeeRegisterDAO.findByEmail(email);
		if (userProfile != null && userProfile.getProfileImage() != null) {
			return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(userProfile.getProfileImage());
		}
		return ResponseEntity.notFound().build();
	}

	@GetMapping("/getUserLeave")
	public EmployeeLeave getUserLeave(@RequestParam String emp_code) {
		return empService.getLeaveByEmpCode(emp_code);

	}

	@GetMapping("/getAppliedLeaves")
	public ResponseEntity<?> getAppliedLeaves(@RequestParam String emp_code) {
		try {
			List<AppliedLeave> appliedLeaves = appliedLeaveDAO.findByEmpCode(emp_code); // Fetch leaves by employee code
			return ResponseEntity.ok(appliedLeaves);
		} catch (Exception ex) {
			return ResponseEntity.badRequest().body(ex.getMessage());
		}
	}

	@PostMapping("/applyLeave")
	public ResponseEntity<?> applyForLeave(@RequestBody LeaveRequestDTO leaveRequest) {
		try {
			empService.applyLeave(leaveRequest);
			return ResponseEntity.ok("Leave applied successfully");
		} catch (Exception ex) {
			return ResponseEntity.badRequest().body(ex.getMessage());
		}
	}

	@PostMapping("/approveLeave")
	public ResponseEntity<?> approveLeave(@RequestBody LeaveRequestDTO leaveRequest) {
		try {
			empService.approveLeave(leaveRequest);
			return ResponseEntity.ok("Leave approved successfully");
		} catch (Exception ex) {
			return ResponseEntity.badRequest().body(ex.getMessage());
		}
	}

	@PostMapping("/rejectLeave")
	public ResponseEntity<?> rejectLeave(@RequestBody LeaveRequestDTO leaveRequest) {
		try {
			empService.rejectLeave(leaveRequest);
			return ResponseEntity.ok("Leave rejected successfully");
		} catch (Exception ex) {
			return ResponseEntity.badRequest().body(ex.getMessage());
		}
	}

	@PostMapping("/otpToEmail")
	public ResponseEntity<?> otpToEmail(@RequestBody Map<String, String> json) {

		String email = json.get("email");
		try {
			boolean isDeleted = otpService.otpToEmail(email);
			if (isDeleted) {
				return ResponseEntity.ok().body(new Response("success", "otp send successfully"));
			} else {
				return ResponseEntity.status(400)
						.body(new Response("error", "Error while sending, check email address is correct ?"));
			}
		} catch (Exception e) {
			return ResponseEntity.status(500).body(new Response("error", "Server error"));
		}
	}

	@PostMapping("/verifyOtp")
	public ResponseEntity<?> verifyOtp(@RequestBody OtpRequest otpRequest) {
		if (otpService.validateOtp(otpRequest.getEmail(), otpRequest.getOtp())) {
			return ResponseEntity.ok("OTP validated successfully.");
		}
		return ResponseEntity.badRequest().body("Invalid OTP.");
	}

	@PostMapping("/updatePassword")
	public ResponseEntity<?> updatePassword(@RequestBody PasswordUpdateRequest request) {
		System.out.println(request);
		try {
			EmployeeRegister updatedEmployee = empService.updateEmployeePassword(request.getEmail(),
					request.getNewPassword());
			System.out.println(updatedEmployee);
			return ResponseEntity.ok(new HashMap<String, String>() {
				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

				{
					put("status", "success");
					put("message", "Password updated successfully!");
				}
			});

		} catch (EntityNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
		} catch (RuntimeException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
	}

	public String extractEmailFromToken(String token) {
		// Extract the claims from the token
		Claims claims = Jwts.parser().setSigningKey("") // SECRET_KEY is the key you've used to sign your JWTs
				.parseClaimsJws(token.replace("Bearer ", "")) // Remove the Bearer prefix
				.getBody();

		return claims.get("email").toString(); // Assuming the claim for the email is "email"
	}

//    @PostMapping("/markAttendance")
//    public ResponseEntity<String> markAttendance(@RequestBody AttendanceRequest request) {
//        
//        Attendance markedAttendance = empService.markAttendance(request.date, request.status, request.userEmail, request.login, request.logout);
//
//        if(markedAttendance != null) {
//            return ResponseEntity.ok("Attendance marked successfully!");
//        } else {
//            return ResponseEntity.status(500).body("Failed to mark attendance.");
//        }
//    }
	@PostMapping("/markAttendance")
	public ResponseEntity<String> markAttendance(@RequestBody AttendanceRequest attendanceRequestDto) {
		try {
			Date parsedDate = new SimpleDateFormat("yyyy-MM-dd").parse(attendanceRequestDto.getDate());
			Date parsedLogin = (attendanceRequestDto.getLogin() != null && !attendanceRequestDto.getLogin().isEmpty())
					? new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").parse(attendanceRequestDto.getLogin())
					: null;
			Date parsedLogout = (attendanceRequestDto.getLogout() != null
					&& !attendanceRequestDto.getLogout().isEmpty())
							? new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").parse(
									attendanceRequestDto.getLogout())
							: null;
			System.out.println(attendanceRequestDto.getEmployee_email());
			Attendance markedAttendance = empService.markAttendance(parsedDate,
					Status.valueOf(attendanceRequestDto.getStatus().toUpperCase()),
					attendanceRequestDto.getEmployee_email(), parsedLogin, parsedLogout);

			if (markedAttendance != null) {
				return ResponseEntity.ok("Attendance marked successfully!");
			} else {
				return ResponseEntity.status(500).body("Failed to mark attendance.");
			}
		} catch (ParseException e) {
			e.printStackTrace(); // This will give more details on what went wrong with the date format
			return ResponseEntity.status(400).body("Invalid date format provided.");
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(400).body("Invalid status provided.");
		}
	}

	static class PasswordUpdateRequest {
		private String email;
		private String newPassword;

		public String getEmail() {
			return email;
		}

		public void setEmail(String email) {
			this.email = email;
		}

		public String getNewPassword() {
			return newPassword;
		}

		public void setNewPassword(String newPassword) {
			this.newPassword = newPassword;
		}

		// Getters, setters, constructors, etc.
	}
}
