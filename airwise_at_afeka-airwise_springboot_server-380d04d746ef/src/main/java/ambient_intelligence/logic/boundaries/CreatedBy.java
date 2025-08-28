package ambient_intelligence.logic.boundaries;

public class CreatedBy {
	
	private UserId userId;

	public CreatedBy() {
	}

	public CreatedBy(String systemId, String email) {
		this.userId = new UserId(systemId, email);
	}

	public UserId getUserId() {
		return userId;
	}

	public void setUserId(UserId userId) {
		this.userId = userId;
	}

	@Override
	public String toString() {
		return "CreatedBy [userId=" + userId + "]";
	}
}
