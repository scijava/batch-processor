package org.scijava.batch;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.scijava.module.ModuleInfo;
import org.scijava.module.ModuleItem;
import org.scijava.service.SciJavaService;

public interface BatchService extends SciJavaService {
	/**
	 * Returns true if {@code moduleInfo} has at least one input item whose type
	 * is supported by this service
	 */
	default public boolean supports(ModuleInfo moduleInfo) {
		for (ModuleItem<?> input : moduleInfo.inputs()) {
			if (supports(input.getType()))
				return true;
		}
		return false;
	}

	/**
	 * Returns true if {@code type} can be populated with batch inputs provided
	 * by this service
	 */
	public boolean supports(Class<?> type);

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
				.filter(item -> supports(item.getType()))
				.collect(Collectors.toList());
	}
}
