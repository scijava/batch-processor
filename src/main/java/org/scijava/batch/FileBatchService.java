package org.scijava.batch;

import java.io.File;
import java.util.HashMap;

import org.scijava.Priority;
import org.scijava.command.CommandService;
import org.scijava.log.LogService;
import org.scijava.module.ModuleInfo;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

@Plugin(type = Service.class, priority = Priority.LOW)
public final class FileBatchService extends AbstractService implements
		BatchService {

	@Parameter
	private LogService log;
	
	@Parameter
	private CommandService commandService;

	/**
	 * Returns true if {@code type} is a {@link File}.
	 */
	@Override
	public boolean supports(Class<?> type) {
		return type.isAssignableFrom(File.class);
	}

	@Override
	public void run(ModuleInfo moduleInfo) {
		// check if moduleInfo has batchable inputs
		if (batchableInputs(moduleInfo).isEmpty()) {
			log.error("No compatible inputs (of type File) found.");
			return;
		}
		// Call ModuleBatchProcessor with input moduleInfo
		HashMap<String, Object> inputMap = new HashMap<>();
		inputMap.put("moduleInfo", moduleInfo);
		commandService.run(ModuleBatchProcessor.class, true, inputMap);
	}

}
