package org.scijava.batch;

import java.io.File;
import java.util.HashMap;

import org.scijava.Priority;
import org.scijava.batch.input.BatchInput;
import org.scijava.batch.input.BatchInputProvider;
import org.scijava.command.CommandService;
import org.scijava.log.LogService;
import org.scijava.module.Module;
import org.scijava.module.ModuleInfo;
import org.scijava.module.ModuleItem;
import org.scijava.plugin.AbstractHandlerService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.Service;

@Plugin(type = Service.class, priority = Priority.LOW)
public final class FileBatchService extends AbstractHandlerService<BatchInput, BatchInputProvider<?>> implements BatchService {

	@Parameter
	private LogService log;

	@Parameter
	private CommandService commandService;

	/**
	 * Returns true if {@code type} is a {@link File}.
	 */
	@Override
	public boolean supportsItem(ModuleItem<?> moduleItem) {
		BatchInputProvider<?> handler = getHandler(new BatchInput(File.class, moduleItem));
		if (handler == null) {
			return false;
		}
		return handler.canProvide(moduleItem);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <I> void fillInput(Module module, ModuleItem<?> moduleItem, I inputObject) {
		BatchInputProvider<File> handler = (BatchInputProvider<File>) getHandler(new BatchInput(File.class, moduleItem));
		if (handler == null) {
			log.error("No handler found for input: " + moduleItem.getName());
			return;
		}
		handler.populateInput(module, moduleItem, (File) inputObject);
	}

	@Override
	public void run(ModuleInfo moduleInfo) {
		// check if moduleInfo has batchable inputs
		if (batchableInputs(moduleInfo).isEmpty()) {
			log.error("No compatible inputs (of type File) found.");
			return;
		}
		
		// 1) get input ModuleItems
		// 2) choose ModuleItem
		// 3) get suitable BatchInputProvider
		// 4) get Iterator for ModuleItem

		//for (getHandler(new BatchInput(File.class, moduleItem)).iterate(fileList)) {
		//}
		
		// Call ModuleBatchProcessor with input moduleInfo
		HashMap<String, Object> inputMap = new HashMap<>();
		inputMap.put("moduleInfo", moduleInfo);
		commandService.run(ModuleBatchProcessor.class, true, inputMap);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Class<BatchInputProvider<?>> getPluginType() {
		return (Class) BatchInputProvider.class;
	}

	@Override
	public Class<BatchInput> getType() {
		return BatchInput.class;
	}

}
