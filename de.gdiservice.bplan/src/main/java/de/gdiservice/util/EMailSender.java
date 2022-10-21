package de.gdiservice.util;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Properties;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

public class EMailSender {
    
    public static final String PARAM_EMAIL_FROM = "emailFrom";
    public static final String PARAM_EMAIL_SMTP_HOST = "emailSmtpHost";
    public static final String PARAM_EMAIL_SMTP_PORT = "emailPort";
    public static final String PARAM_EMAIL_USER = "emailUser";
    public static final String PARAM_EMAIL_PWD = "emailPwd";
	
    private String from;
    private String smtpHost;
    private String port;
	
    private String user;
    private String pwd;
	
    
    public EMailSender(Map<String, String> emailParams) {
        this.from = emailParams.get(PARAM_EMAIL_FROM);
        this.smtpHost = emailParams.get(PARAM_EMAIL_SMTP_HOST);
        this.port = emailParams.get(PARAM_EMAIL_SMTP_PORT);
        this.user = emailParams.get(PARAM_EMAIL_USER);
        this.pwd = emailParams.get(PARAM_EMAIL_PWD);
    }
	
	public EMailSender(String from, String smtpHost, String port, String user, String pwd) {
	    this.from = from;
	    this.smtpHost = smtpHost;
	    this.port = port;
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
		    String from = argList.get("from");
		    String smtpHost = argList.get("smtpHost");
		    String port = argList.get("port");
            String emailUser = argList.get("emailUser");
            String emailPwd = argList.get("emailPwd");
            
			(new EMailSender(from, smtpHost, port, emailUser, emailPwd)).sendEmail("Das ist eine erste Mail vom Radnetzplaner<br>das lsdfjasofj hkdshjafkafj", "Mail vom Radnetzplaner", "ralf.trier@outlook.de");
		} 
		catch (Exception e) {		
			e.printStackTrace();
		}
	}

}
