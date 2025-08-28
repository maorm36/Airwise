package ambient_intelligence.data;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import ambient_intelligence.logic.boundaries.UserId;
import ambient_intelligence.utils.AirwiseConfig;

@Document(collection = "USERS")
public class UserEntity {

	@Id
	private String userId;
	private UserRole role;
	private String username;
	private String avatar;

	public UserEntity() {
	};

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public void setUserId(UserId userId) {
		this.userId = AirwiseConfig.getSystemID() + AirwiseConfig.getIdSeparator() + userId.getEmail();
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
