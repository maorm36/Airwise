package ambient_intelligence.presentation;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import ambient_intelligence.logic.CommandsServiceWithPagination;
import ambient_intelligence.logic.ObjectsService;
import ambient_intelligence.logic.UsersServiceWithPagination;
import ambient_intelligence.logic.boundaries.CommandBoundary;
import ambient_intelligence.logic.boundaries.UserBoundary;

@RestController
@RequestMapping(path = { "/ambient-intelligence/admin" })
public class AdminController {

	private final UsersServiceWithPagination userService;
	private final CommandsServiceWithPagination commandService;
	private final ObjectsService objectService;

	public AdminController(UsersServiceWithPagination userService, CommandsServiceWithPagination commandService,
			ObjectsService objectService) {
		this.userService = userService;
		this.commandService = commandService;
		this.objectService = objectService;
	}

	@GetMapping(path = { "/users" }, produces = { MediaType.APPLICATION_JSON_VALUE })
	public List<UserBoundary> exportAllUsers(@RequestParam(name = "userSystemID", required = true) String userSystemID,
			@RequestParam(name = "userEmail", required = true) String userEmail,
			@RequestParam(name = "size", required = false, defaultValue = "10") int size,
			@RequestParam(name = "page", required = false, defaultValue = "0") int page) {

		return this.userService.getAllUsers(userSystemID, userEmail, size, page);

	}

	@GetMapping(path = { "/commands" }, produces = { MediaType.APPLICATION_JSON_VALUE })
	public List<CommandBoundary> exportAllCommands(
			@RequestParam(name = "userSystemID", required = true) String userSystemID,
			@RequestParam(name = "userEmail", required = true) String userEmail,
			@RequestParam(name = "size", required = false, defaultValue = "10") int size,
			@RequestParam(name = "page", required = false, defaultValue = "0") int page) {

		return this.commandService.getAllCommandsHistory(userSystemID, userEmail, size, page);
	}

	@DeleteMapping(path = { "/users" })
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	public void deleteAllUsers(@RequestParam(name = "userSystemID", required = true) String userSystemID,
			@RequestParam(name = "userEmail", required = true) String userEmail) {

		this.userService.deleteAllUsers(userSystemID, userEmail);

	}

	@DeleteMapping(path = { "/commands" })
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	public void deleteAllCommands(@RequestParam(name = "userSystemID", required = true) String userSystemID,
			@RequestParam(name = "userEmail", required = true) String userEmail) {

		this.commandService.deleteAllCommands(userSystemID, userEmail);

	}

	@DeleteMapping(path = { "/objects" })
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	public void deleteAllObjects(@RequestParam(name = "userSystemID", required = true) String userSystemID,
			@RequestParam(name = "userEmail", required = true) String userEmail) {

		this.objectService.deleteAllObjects(userSystemID, userEmail);

	}

}
