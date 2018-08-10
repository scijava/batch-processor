package org.scijava.batch;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.scijava.batch.input.BatchInput;
import org.scijava.batch.input.BatchInputProvider;
import org.scijava.module.Module;
import org.scijava.module.ModuleInfo;
import org.scijava.module.ModuleItem;
import org.scijava.plugin.HandlerService;
import org.scijava.service.SciJavaService;

public interface BatchService extends HandlerService<BatchInput, BatchInputProvider<?>>, SciJavaService {
	/**
	 * Returns true if {@code moduleInfo} has at least one input item whose type
	 * is supported by this service
	 */
	default public boolean supportsModule(ModuleInfo moduleInfo) {
		for (ModuleItem<?> input : moduleInfo.inputs()) {
			if (supportsItem(input))
				return true;
		}
		return false;
	}
	
	//default public getHandler(ModuleItem)

	/**
	 * Returns true if {@code type} can be populated with batch inputs provided
	 * by this service
	 */
	public boolean supportsItem(ModuleItem<?> moduleItem);

	/**
	 * Run the module described by {@link ModuleInfo} in batch.
	 */
	public void run(ModuleInfo moduleInfo);

	/**
	 * A collection of input {@link ModuleItem}s of the given {@link ModuleInfo}
	 * that are supported (i.e. can be batch-processed) by this service
	 */
	default public List<ModuleItem<?>> batchableInputs(ModuleInfo moduleInfo) {
		return StreamSupport.stream(moduleInfo.inputs().spliterator(), false)
				.filter(item -> supportsItem(item))
				.collect(Collectors.toList());
	}
	
	/**
	 * Fill a provided ModuleItem with a given input object
	 * @param <I>
	 */
	public <I> void fillInput(Module module, ModuleItem<?> moduleItem, I inputObject);

}
