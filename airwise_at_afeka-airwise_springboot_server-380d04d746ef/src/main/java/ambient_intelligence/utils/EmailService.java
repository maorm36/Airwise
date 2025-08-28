package ambient_intelligence.utils;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

	private JavaMailSender mailSender;
	
	public EmailService(JavaMailSender mailSender) {
		this.mailSender = mailSender;
	}

	public void sendEmail(EmailRequest request) {
		SimpleMailMessage message = new SimpleMailMessage();

		message.setFrom(request.getEmailSender());
		message.setTo(request.getEmailReceiver());
		message.setSubject(request.getEmailSubject());
		message.setText(request.getEmailBody());

		mailSender.send(message);
	}
	
	/* how to use this service:
	 * 
	 * 1) init the properties in application properties (steps are written there)
	 * 
	 * 2) init the service: (example by using constructor)
		  private final EmailService emailService;
		  public ObjectController(ObjectsServicePagination objectsService, EmailService emailService) {
			  this.objectsService = objectsService;
			  this.emailService = emailService;
		  }
	*	  
	*  3) insert the following code for sending the email where needed:
	*     emailService.sendEmail(new EmailRequest("senderEmail", "rcvrEmail", "subject", "body"));
	* 
	*/
}
