package com.employee.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.employee.entity.EmployeeRegister;
import com.twilio.Twilio;
import com.twilio.exception.ApiException;
import com.twilio.type.PhoneNumber;

import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.activation.FileDataSource;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.Multipart;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

@Service
public class EmailService {
	@Autowired
	private Environment environment;
	public String sendEmail(String to, String token, EmployeeRegister message) {
		String subject = "Login Link";
		String from = "944shaikkhadeer@gmail.com";
		String host = "smtp.gmail.com";

		Properties properties = System.getProperties();

		// setting important information to properties object

		// host set
		properties.put("mail.smtp.host", host);
		properties.put("mail.smtp.port", "465");
		properties.put("mail.smtp.ssl.enable", "true");
		properties.put("mail.smtp.auth", "true");
//		hhmnhtfcskczulik -------------- MAIL PWD
		// Step 1: to get the session object..
		Session session = Session.getInstance(properties, new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(from, "hhmnhtfcskczulik");
			}

		});

		session.setDebug(true);

		// Step 2 : compose the message [text,multi media]
		MimeMessage m = new MimeMessage(session);
		// Create a MimeMultipart object to hold both text and image

		try {
			// from email
			m.setFrom(from);

			// adding recipient to message
			m.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

			// adding subject to message
			m.setSubject(subject);

			String confirmationUrl = "http://127.0.0.1:5500/Project/login/login.html?token=" + token;

			Multipart multipart = new MimeMultipart("related");
			// Create the text part of the email
			MimeBodyPart textPart = new MimeBodyPart();

			String bodyContent = "<div style='font-family: Arial, sans-serif; padding: 20px; background-color: #f7f7f7;'>";
			bodyContent += "<h2 style='color: #2C3E50;'>Welcome to AK Family Private Limited!</h2>";
			bodyContent += "<p style='line-height: 1.5;'>We are delighted to have you on board as a valued member of our dynamic team. At AK Family Private Limited, we pride ourselves on driving innovation in the tech industry, delivering unparalleled IT solutions, and creating a collaborative work environment for our employees.</p>";

			bodyContent += "<div style='margin-top: 20px; margin-bottom: 20px; text-align: center;'>";
			bodyContent += "<img src='cid:image' alt='Company Image' style='max-width: 100%; border-radius: 10px; box-shadow: 0 4px 8px rgba(0,0,0,0.1);'/>";
			bodyContent += "<div style='margin-top: 10px;'>AK Family Private Limited Headquarters</div>"; // Caption
																											// below the
																											// image
			bodyContent += "</div>";

			bodyContent += "<h2 style='color: #2C3E50;'>Click on this link for login : </h2>" + confirmationUrl;
			bodyContent += "<h3 style='color: #34495E;'>Our Services:</h3>";
			bodyContent += "<ul style='list-style-type: circle; padding-left: 20px;'>";
			bodyContent += "<li style='margin-bottom: 10px;'>Cloud Solutions & Architecture</li>";
			bodyContent += "<li style='margin-bottom: 10px;'>Database Management & Security</li>";
			bodyContent += "<li style='margin-bottom: 10px;'>Web & Mobile Application Development</li>";
			bodyContent += "<li>AI & Machine Learning Innovations</li>";
			bodyContent += "</ul>";

			bodyContent += "<h3 style='color: #34495E; margin-top: 20px;'>Contact & Address:</h3>";
			bodyContent += "<p><strong>Email:</strong><a href='mailto:" + from + "'>" + from + "</a><br/>";
			bodyContent += "<strong>Address:</strong> 123 Tech Street, Silicon Valley, CA 94000<br/>";
			bodyContent += "<strong>Location:</strong> Nestled in the heart of Silicon Valley, a hub for tech innovation.</p>";

			bodyContent += "<p>We value your feedback and insights. If you have any questions, suggestions, or feedback about our services or your onboarding process, kindly reply to this email. We're always eager to improve and serve you better.</p>";

			bodyContent += "<h3 style='color: #34495E; margin-top: 20px;'>Explore Further:</h3>";
			bodyContent += "<p>For more details about our products, services, and the latest updates, visit our <a href='http://127.0.0.1:5500//Project/index.html' style='color: #2980B9; text-decoration: none;'>Official Website</a>.</p>";
			bodyContent += "</div>";

			textPart.setContent(bodyContent, "text/html");
			multipart.addBodyPart(textPart);

			// Create the image part of the email
			MimeBodyPart imagePart = new MimeBodyPart();
			DataSource source = new FileDataSource("C://Users//user181//Documents//AK.png"); // Replace with the actual
																								// image path
			imagePart.setDataHandler(new DataHandler(source));
			imagePart.setHeader("Content-ID", "<image>"); // The CID we use to refer to in the HTML
			imagePart.setDisposition(MimeBodyPart.INLINE); // Ensure it's treated as inline and not as attachment
			multipart.addBodyPart(imagePart);

			// Set the content of the message to be the multipart
			m.setContent(multipart);
			Transport.send(m);
			System.out.println("Sent success...................");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return to;
	}

//	TWILIO SID :AC89e114bb3ba654cb78d5bba23c921532  token : 24ee22f9ebb41cbffd59e41f95e3c44d
//	ACCOUNT NO :AK@1722@AK@1722@ 944shaikhadeer@gmail.com
//	MSpS_n6qkZ3IdPBipPQT5NPmrJYWsYXZCFWvwTwx

	public void sendWhatsAppMessage(String to, String messageBody,String email) {
		// Initialize Twilio
		Twilio.init("AC4a930c6f051de6fe2d2b6ab33907f589", "204b1c9d0f9506b21691225decbf6759");
		
		try {
			
//			String encodedToken = URLEncoder.encode(messageBody, StandardCharsets.UTF_8.toString());
//			String confirmationUrl = "http://localhost/Project/login/login.html?token=" + encodedToken;
			String confirmationUrl = "https://789d-14-140-84-6.ngrok-free.app/Project/login/login.html?email=" + email;

//			String confirmationUrl ="http://www.facebook.com";
//			String confirmationUrl = "http://192.168.252.133:5500/Project/login/login.html?token=" + messageBody;
			com.twilio.rest.api.v2010.account.Message message = com.twilio.rest.api.v2010.account.Message
					.creator(new PhoneNumber("whatsapp:+91" + to), new PhoneNumber("whatsapp:+14155238886"),
							confirmationUrl)
					.create();
			System.out.println("enetred 2");
		} catch (ApiException e) {
			// This is thrown when there's an issue making the API call
			e.printStackTrace();
			// Maybe log the error or notify admin
		} catch (Exception e) {
			// This is to catch any other general exception that might occur
			e.printStackTrace();
			// Maybe log the error or notify admin
		}

	}

	public String sendEmail(String to, int otp) {
		String subject = "OTP for forgot password";
		String from = "944shaikkhadeer@gmail.com";
		String host = "smtp.gmail.com";

		Properties properties = System.getProperties();
		// setting important information to properties object

		// host set
		properties.put("mail.smtp.host", host);
		properties.put("mail.smtp.port", "465");
		properties.put("mail.smtp.ssl.enable", "true");
		properties.put("mail.smtp.auth", "true");
//		hhmnhtfcskczulik -------------- MAIL PWD
		// Step 1: to get the session object..
		Session session = Session.getInstance(properties, new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(from, "hhmnhtfcskczulik");
			}

		});

		session.setDebug(true);

		// Step 2 : compose the message [text,multi media]
		MimeMessage m = new MimeMessage(session);

		try {

			// from email
			m.setFrom(from);

			// adding recipient to message
			m.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

			// adding subject to message
			m.setSubject(subject);

			// Update the message to contain the new URL
			String bodyContent = " This is the otp don't share with anyone " + otp;
			m.setContent(bodyContent, "text/html");

//			 adding text to message
//			m.setText(message);

			// send

			// Step 3 : send the message using Transport class
			Transport.send(m);
			System.out.println("Sent success...................");

		} catch (Exception e) {
			e.printStackTrace();
		}
		return to;

	}

}