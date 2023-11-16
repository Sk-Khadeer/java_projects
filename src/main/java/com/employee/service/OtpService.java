package com.employee.service;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.employee.entity.EmployeeRegister;
import com.employee.repo.EmployeeRegisterDAO;

import jakarta.transaction.Transactional;
import redis.clients.jedis.Jedis;

@Service
public class OtpService {
	@Autowired
	private EmployeeRegisterDAO employeeRegisterDAO;

	@Autowired
	private EmailService emailService;
	private static final int OTP_EXPIRATION = 300; // 300 seconds = 5 minutes

	@Autowired
	private Jedis jedis;

	@SuppressWarnings("deprecation")
	@Transactional
	public boolean otpToEmail(String email) {
		try {
			EmployeeRegister emp = employeeRegisterDAO.findByEmail(email);
			if (emp == null) {
				return false;
			} else {
				Random random = new Random();
				int otp = 100000 + random.nextInt(900000);

				// Store the OTP in Redis with a TTL
				jedis.setex(email, OTP_EXPIRATION, String.valueOf(otp));

				emailService.sendEmail(emp.getEmail(), otp);
				return true;
			}

		} catch (Exception e) {
			e.printStackTrace();	
			return false;
		}
	}

	public boolean validateOtp(String email, int userOtp) {
		String storedOtp = jedis.get(email);
		if (storedOtp != null && Integer.parseInt(storedOtp) == userOtp) {
			// Remove the OTP once validated to prevent reuse
			System.out.println(storedOtp+"   "+userOtp);
			jedis.del(email);
			return true;
		}
		return false;
	}
}
