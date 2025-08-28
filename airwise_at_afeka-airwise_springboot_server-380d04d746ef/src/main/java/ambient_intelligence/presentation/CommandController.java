package ambient_intelligence.presentation;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import ambient_intelligence.logic.CommandsService;
import ambient_intelligence.logic.boundaries.CommandBoundary;

import java.util.List;

@RestController
@RequestMapping("/ambient-intelligence/commands")
public class CommandController {

	private final CommandsService commandService;

	public CommandController(CommandsService commandService) {
		this.commandService = commandService;
	}

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public List<Object> invokeCommand(@RequestBody CommandBoundary commandBoundary) {

		return this.commandService.invokeCommand(commandBoundary);
		
	}
}
