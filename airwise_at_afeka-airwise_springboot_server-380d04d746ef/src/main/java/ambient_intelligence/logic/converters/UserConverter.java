package ambient_intelligence.logic.converters;

import org.springframework.stereotype.Component;

import ambient_intelligence.data.UserEntity;
import ambient_intelligence.logic.boundaries.UserBoundary;
import ambient_intelligence.logic.boundaries.UserId;
import ambient_intelligence.utils.AirwiseConfig;

@Component
public class UserConverter {

	public UserBoundary toBoundary(UserEntity entity) {

		UserBoundary userBoundary = new UserBoundary();
		
		UserId userId = new UserId();
		
		String systemID = entity.getUserId().split(AirwiseConfig.getIdSeparator())[0];
		String email = entity.getUserId().split(AirwiseConfig.getIdSeparator())[1];
		
		userId.setSystemID(systemID);
		userId.setEmail(email);
		
		userBoundary.setUserId(userId);

		userBoundary.setRole(entity.getRole());

		userBoundary.setUsername(entity.getUsername());
		
		userBoundary.setAvatar(entity.getAvatar());
		
		return userBoundary;
	}
	
	
	public UserEntity toEntity(UserBoundary boundary) {

		UserEntity userEntity = new UserEntity();
		
		userEntity.setUserId(boundary.getUserId());
		
		userEntity.setRole(boundary.getRole());
		
		userEntity.setUsername(boundary.getUsername());
		
		userEntity.setAvatar(boundary.getAvatar());
		
		return userEntity;
	}


}
