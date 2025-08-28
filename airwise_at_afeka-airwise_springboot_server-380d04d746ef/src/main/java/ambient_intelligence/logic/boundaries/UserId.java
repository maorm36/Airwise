package ambient_intelligence.logic.boundaries;

public class UserId {

	private String email;
	private String systemID;

	public UserId() {
	}

	public UserId(String systemID, String email) {
		this.systemID = systemID;
		this.email = email;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getSystemID() {
		return systemID;
	}

	public void setSystemID(String systemID) {
		this.systemID = systemID;
	}

}
