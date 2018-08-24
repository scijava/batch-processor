/*-
 * #%L
 * A Batch Processor for SciJava Modules and Scripts
 * %%
 * Copyright (C) 2017 - 2018 Friedrich Miescher Institute for Biomedical Research, Basel (Switzerland)
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
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
