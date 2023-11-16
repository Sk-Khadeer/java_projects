package com.employee.entity;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;

@Entity
@Table(name = "employee_register")
@Data
public class EmployeeRegister {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(unique = true)
	private String empCode;
	private String firstname;
	private String lastname;
	private String fathername;
	private Long phoneno;
	private String password;
	@Column(unique = true)
	private String email;
	private String designation;
	private String subdesignation;
	private Date dob;
	private String gender;
	private String state;
	private String city;
	private Integer pincode;
	@Column(name = "status", nullable = false, columnDefinition = "VARCHAR(255) default 'unconfirmed'")
	private String status = "unconfirmed";
	private String confirmationToken;
	private LocalDateTime confirmationTokenCreationDate;
	@Column(name = "role")
	private String role; // Can be 'ADMIN' or 'USER'
	@Transient
	private String loginOption;

	@Column(name = "profile_image", columnDefinition = "MEDIUMBLOB")
	private byte[] profileImage;

	@OneToMany(mappedBy = "employee", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private List<Attendance> attendances;

	public String generateCode() {
		return this.empCode = "AK@" + id;
	}

}
