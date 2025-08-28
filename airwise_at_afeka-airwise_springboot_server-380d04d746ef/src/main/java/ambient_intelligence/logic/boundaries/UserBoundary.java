package ambient_intelligence.logic.boundaries;

import ambient_intelligence.data.UserRole;

public class UserBoundary {

	private UserId userId;
	private UserRole role;
	private String username;
	private String avatar;

	public UserBoundary() {
	};
	
	public UserBoundary(String systemID, String email, UserRole role, String username, String avatar) {
		this.userId = new UserId(systemID, email);
		this.role = role;
		this.username = username;
		this.avatar = avatar;
	}
	
	public UserBoundary(NewUserBoundary newUser, String systemID) {
		this.userId = new UserId(systemID, newUser.getEmail());
		this.role = newUser.getRole();
		this.username = newUser.getUsername();
		this.avatar = newUser.getAvatar();
	}
	
	public UserId getUserId() {
		return userId;
	}

	public void setUserId(UserId userId) {
		this.userId = userId;
	}

	public UserRole getRole() {
		return role;
	}

	public void setRole(UserRole role) {
		this.role = role;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getAvatar() {
		return avatar;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}

}
