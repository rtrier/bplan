package de.gdiservice.util;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

public class EMailSender {
	
	static String from = "ralf.trier@gdi-service.de";
	static String smtpHost = "smtp.gmail.com";
	static String port = "465";
	
    private String user;
    private String pwd;
	
	
	public EMailSender(String user, String pwd) {
	    this.user = user;
	    this.pwd = pwd;
	}

	public void sendEmail(String body, String subject, String recipient) throws MessagingException,	UnsupportedEncodingException {
		Properties mailProps = new Properties();
		mailProps.put("mail.smtp.from", from);
		mailProps.put("mail.smtp.host", smtpHost);
		mailProps.put("mail.smtp.port", port);
		mailProps.put("mail.smtp.auth", true);
		mailProps.put("mail.smtp.socketFactory.port", port);
		mailProps.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		mailProps.put("mail.smtp.socketFactory.fallback", "false");
		mailProps.put("mail.smtp.starttls.enable", "true");

		Session mailSession = Session.getDefaultInstance(mailProps, new Authenticator() {

			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(user, pwd);
			}

		});

		MimeMessage message = new MimeMessage(mailSession);
		message.setFrom(new InternetAddress(from));
		message.setReplyTo(new InternetAddress[] {new InternetAddress(from)});
		System.err.println(message.getFrom()[0]);
		String[] emails = { recipient };
		InternetAddress dests[] = new InternetAddress[emails.length];
		for (int i = 0; i < emails.length; i++) {
			dests[i] = new InternetAddress(emails[i].trim().toLowerCase());
		}
		message.setRecipients(Message.RecipientType.TO, dests);
		message.setSubject(subject, "UTF-8");
		Multipart mp = new MimeMultipart();
		MimeBodyPart mbp = new MimeBodyPart();
		mbp.setContent(body, "text/html;charset=utf-8");
		mp.addBodyPart(mbp);
		message.setContent(mp);
		message.setSentDate(new java.util.Date());

		Transport.send(message);
	}   

	public static void main(String[] args) {
		try {
		    ArgList argList = new ArgList(args);

            String emailUser = argList.get("emailUser");
            String emailPwd = argList.get("emailPwd");
            
			(new EMailSender(emailUser, emailPwd)).sendEmail("Das ist eine erste Mail vom Radnetzplaner<br>das lsdfjasofj hkdshjafkafj", "Mail vom Radnetzplaner", "ralf.trier@outlook.de");
		} 
		catch (Exception e) {		
			e.printStackTrace();
		}
	}

}
