package ambient_intelligence.utils;

public class EmailRequest {
	private String emailSender;
	private String emailReceiver;
	private String emailSubject;
	private String emailBody;

	public EmailRequest() {
	}

	public EmailRequest(String emailSender, String emailReceiver, String emailSubject, String emailBody) {
		super();
		this.emailSender = emailSender;
		this.emailReceiver = emailReceiver;
		this.emailSubject = emailSubject;
		this.emailBody = emailBody;
	}

	// Getters and Setters
	public String getEmailSender() {
		return emailSender;
	}

	public void setEmailSender(String emailSender) {
		this.emailSender = emailSender;
	}

	public String getEmailReceiver() {
		return emailReceiver;
	}

	public void setEmailReceiver(String emailReceiver) {
		this.emailReceiver = emailReceiver;
	}

	public String getEmailSubject() {
		return emailSubject;
	}

	public void setEmailSubject(String emailSubject) {
		this.emailSubject = emailSubject;
	}

	public String getEmailBody() {
		return emailBody;
	}

	public void setEmailBody(String emailBody) {
		this.emailBody = emailBody;
	}

	@Override
	public String toString() {
		return "EmailRequest [emailSender=" + emailSender + ", emailReceiver=" + emailReceiver + ", emailSubject="
				+ emailSubject + ", emailBody=" + emailBody + "]";
	}
}
