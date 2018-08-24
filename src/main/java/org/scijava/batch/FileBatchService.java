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
