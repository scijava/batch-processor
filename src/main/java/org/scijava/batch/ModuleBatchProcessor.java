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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.command.DynamicCommand;
import org.scijava.log.Logger;
import org.scijava.module.Module;
import org.scijava.module.ModuleInfo;
import org.scijava.module.ModuleItem;
import org.scijava.module.MutableModuleItem;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.table.Column;
import org.scijava.table.DefaultGenericTable;
import org.scijava.table.Table;
import org.scijava.task.Task;
import org.scijava.task.TaskService;
//import org.scijava.widget.FileWidget;

@Plugin(type = Command.class, label = "Choose batch processing parameters", initializer = "initInputChoice")
public class ModuleBatchProcessor<T> extends DynamicCommand {
	@Parameter
	private BatchService batchService;

	@Parameter
	private Logger log;

	@Parameter
	private ModuleInfo moduleInfo; // to be provided at runtime!

	@Parameter(label = "Which input parameter to batch?", persist = false)
	private String inputChoice;

	@Parameter(label = "Input files")
	private File[] inputFileList;

//	@Parameter(label = "Output directory", style = FileWidget.DIRECTORY_STYLE, required = false)
//	private File outputFolder;

	@SuppressWarnings("rawtypes")
	@Parameter(type = ItemIO.OUTPUT)
	private Table outputTable;

	@Parameter
	private TaskService taskService;

	// -- Initializer --

	protected void initInputChoice() {
		MutableModuleItem<String> choiceInput = getInfo().getMutableInput("inputChoice", String.class);
		// Get compatible inputs for moduleInfo
		List<ModuleItem<?>> compatibleInputs = batchService
				.batchableInputs(moduleInfo);
		if (compatibleInputs.size() == 1) {
			choiceInput.setValue(this, compatibleInputs.get(0).getName());
			resolveInput("inputChoice");
		} else if (compatibleInputs.size() > 1) {
			choiceInput.setChoices(compatibleInputs.stream()
					.map(ModuleItem::getName).collect(Collectors.toList()));
		} else {
			log.error("No compatible inputs found. Unable to initialize input choice.");
		}
	}

	// -- Main method --

	@Override
	public void run() {
		// mark inputChoice as resolved, then harvest script parameters (i.e. run)
		ModuleItem<?> inputModuleItem = moduleInfo.getInput(inputChoice);
		// TODO check if conversion needed?
		Module scriptModule = moduleService.createModule(moduleInfo);
		scriptModule.resolveInput(inputChoice);

		/* Create output Table and mark all outputs as resolved */
		outputTable = new DefaultGenericTable();
		@SuppressWarnings("rawtypes")
		List<Column> columns = new ArrayList<>();

		for (String outputKey : scriptModule.getOutputs().keySet()) {
			columns.add(outputTable.appendColumn(outputKey));
			scriptModule.resolveOutput(outputKey);
		}

		String taskName = "Batch:";
		if (scriptModule.getInfo()!=null) {
			if (scriptModule.getInfo().getName()!=null)
				taskName = scriptModule.getInfo().getName();
		}
		Task batchTask = taskService.createTask(taskName);
		batchTask.setProgressMaximum(inputFileList.length);
		batchTask.setCancelCallBack(() -> batchTask.setStatusMessage("Cancelling batch task..."));
		for (File file : inputFileList) {
			batchTask.setStatusMessage("process "+file.getName());
			if (!(processFile(scriptModule, inputModuleItem, file))) {
				log.warn("Terminating batch process.");
				break; // end for loop
			}
			if (batchTask.isCanceled()) {
				log.warn("Terminating batch process.");
				break; // end for loop
			}
			batchTask.setProgressValue(batchTask.getProgressValue() + 1);
		}
		batchTask.finish();
		// case File
		//   feed files into input
		// case Image (not needed if conversion works
		//   open each file as image (warn on errors) and feed image into input
	}
	
	// -- Helper methods --
	
	@SuppressWarnings("unchecked")
	private boolean processFile(Module module, ModuleItem<?> inputModuleItem, File file) {
		batchService.fillInput(module, inputModuleItem, file);
		//fileInput.setValue(module, file);
		outputTable.appendRow(file.getName());

		Future<Module> instance = moduleService.run(module, true);
		try {
			// run the script
			Map<String, Object> outputs = instance.get().getOutputs();
			for (Entry<String, Object> output : outputs.entrySet()) {
				outputTable.set(output.getKey(), outputTable.getRowCount() - 1,
						output.getValue());
			}
			return true;
		} catch (InterruptedException exc) {
			log.error("Error: interrupted module execution", exc);
			return false;
		} catch (ExecutionException exc) {
			log.error("Error during module execution", exc);
			return true; // continue loop
		}
	}

}
