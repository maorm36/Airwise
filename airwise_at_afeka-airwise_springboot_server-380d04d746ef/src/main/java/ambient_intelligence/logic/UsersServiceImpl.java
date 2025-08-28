package ambient_intelligence.logic;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ambient_intelligence.dal.UserCrud;
import ambient_intelligence.data.UserEntity;
import ambient_intelligence.data.UserRole;
import ambient_intelligence.logic.boundaries.UserBoundary;
import ambient_intelligence.logic.boundaries.UserId;
import ambient_intelligence.logic.converters.UserConverter;
import ambient_intelligence.logic.exceptions.InvalidRequestInputException;
import ambient_intelligence.logic.exceptions.ObjectNotFoundException;
import ambient_intelligence.logic.exceptions.UnauthorizedException;
import ambient_intelligence.logic.security.AuthorizationService;
import ambient_intelligence.utils.AirWiseValidator;
import ambient_intelligence.utils.AirwiseConfig;

import org.springframework.transaction.annotation.Transactional;

@Service
public class UsersServiceImpl implements UsersServiceWithPagination {

	private final UserCrud userCrud;
	private final UserConverter userConverter;
	private final AirWiseValidator validator;
	private final AuthorizationService authz;

	public UsersServiceImpl(UserCrud userCrud, UserConverter userConverter, AirWiseValidator validator, AuthorizationService authz) {
		this.userCrud = userCrud;
		this.userConverter = userConverter;
		this.validator = validator;
		this.authz = authz;
	}

	@Override
	@Transactional(readOnly = false)
	public UserBoundary createUser(UserBoundary newUser) {

		if (newUser == null || newUser.getUserId() == null)
			throw new InvalidRequestInputException("Invalid input - user is not initialized");

		if (!this.validator.isValidRole(newUser.getRole().toString())) {
			throw new InvalidRequestInputException("Invalid Role");
		}

		if (!this.validator.isValidEmail(newUser.getUserId().getEmail())) {
			throw new InvalidRequestInputException("Invalid Email");
		}

		if (newUser.getUsername() == null || newUser.getUsername().isBlank()) {
			throw new InvalidRequestInputException("Invalid UserName");
		}
		
		if (newUser.getAvatar() == null || newUser.getAvatar().isBlank()) {
			throw new InvalidRequestInputException("Invalid Avatar");
		}

		newUser.setUserId(new UserId(AirwiseConfig.getSystemID(), newUser.getUserId().getEmail()));

		UserEntity userEntity = this.userConverter.toEntity(newUser);

		return this.userConverter.toBoundary(this.userCrud.save(userEntity));
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<UserBoundary> login(String systemID, String userEmail) {

		if (!this.validator.isValidSystemId(systemID)) {
			throw new InvalidRequestInputException("Invalid input - systemID is invalid.");
		}

		if (!this.validator.isValidEmail(userEmail)) {
			throw new InvalidRequestInputException("Invalid input - Email is invalid.");
		}

		return this.userCrud.findById(systemID + AirwiseConfig.getIdSeparator() + userEmail)
				.map(this.userConverter::toBoundary);
	}

	@Override
	@Transactional(readOnly = false)
	public void updateUser(String systemID, String userEmail, UserBoundary update) {

		if (update == null) {
			throw new InvalidRequestInputException("Invalid input - update is not initialized");
		}

		if (!this.validator.isValidSystemId(systemID)) {
			throw new InvalidRequestInputException("Invalid input - systemID is invalid.");
		}

		if (!this.validator.isValidEmail(userEmail)) {
			throw new InvalidRequestInputException("Invalid input - Email is invalid.");
		}

		String userId = systemID + AirwiseConfig.getIdSeparator() + userEmail;
		Optional<UserEntity> existing = this.userCrud.findById(userId);

		if (!existing.isPresent()) {
			throw new ObjectNotFoundException("Could not find user");
		}

		UserEntity existingUser = existing.get();

		if (this.validator.isValidRole(update.getRole().toString())) {
			existingUser.setRole(update.getRole());
		}

		if (update.getUsername() != null && !update.getUsername().isBlank()) {
			existingUser.setUsername(update.getUsername());
		}

		if (update.getAvatar() != null && !update.getAvatar().isBlank()) {
			existingUser.setAvatar(update.getAvatar());
		}

		this.userCrud.save(existingUser);

	}
	
	@Override
	@Transactional(readOnly = true)
	public List<UserBoundary> getAllUsers(String userSystemID, String userEmail, int size, int page) {
		
		this.validator.isValidPaginationInputs(size, page);
		
		// validate the user requesting the data and his Role.
		if(!this.authz.ensureRole(userSystemID, userEmail, UserRole.ADMIN)) {
			throw new UnauthorizedException("Unauthorized action.");
		}
		
		return this.userCrud.findAll(
				PageRequest.of(page, size)
				).stream().map(this.userConverter::toBoundary).toList();
	}
	
	@Override
	@Transactional(readOnly = false)
	public void deleteAllUsers(String userSystemID, String userEmail) {
		
		// validate the user making the action and his Role.
		if(!this.authz.ensureRole(userSystemID, userEmail, UserRole.ADMIN)) {
			throw new UnauthorizedException("Unauthorized action.");
		}
		
		this.userCrud.deleteAll();
		
	}
	
	
	// DEPRECATED METHODS BELOW:
	

	@Override
	@Deprecated
	public List<UserBoundary> getAllUsers() {
		throw new InvalidRequestInputException("This operation is deprecated and will be removed in the near future.");
	}
	

	@Override
	@Deprecated
	public void deleteAllUsers() {
		throw new InvalidRequestInputException("This operation is deprecated and will be removed in the near future.");
	}



}
