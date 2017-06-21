package org.scijava.batch;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import net.imagej.table.Column;
import net.imagej.table.DefaultGenericTable;
import net.imagej.table.Table;

import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.command.DynamicCommand;
import org.scijava.convert.ConvertService;
import org.scijava.log.LogService;
import org.scijava.module.Module;
import org.scijava.module.ModuleInfo;
import org.scijava.module.ModuleItem;
import org.scijava.module.ModuleService;
import org.scijava.module.MutableModuleItem;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.widget.FileWidget;

@Plugin(type = Command.class, label = "Choose batch processing parameters", initializer = "initInputChoice")
public class ModuleBatchProcessor extends DynamicCommand {
	@Parameter
	private ModuleService modules;

	@Parameter
	private ConvertService convert;
	
	@Parameter
	private LogService log;
	
	@Parameter
	private ModuleInfo moduleInfo; // to be provided at runtime!
	
	@Parameter(label = "Which input parameter to batch?", persist = false)
	private String inputChoice;

	@Parameter(label = "Input files")
	private File[] inputFileList;

	@Parameter(label = "Output directory", style = FileWidget.DIRECTORY_STYLE, required = false)
	private File outputFolder;

	@SuppressWarnings("rawtypes")
	@Parameter(type = ItemIO.OUTPUT)
	private Table outputTable;

	// -- Initializer --

	protected void initInputChoice() {
		MutableModuleItem<String> choiceInput = getInfo().getMutableInput("inputChoice", String.class);
		// get compatible inputs from module
		ArrayList<String> compatibleInputs = new ArrayList<>();
		for (ModuleItem<?> input : moduleInfo.inputs()) {
			// TODO consider replacing by isAssignableFrom
			if (convert.supports(new File(""), input.getType())) {
				// if we can convert a File to the given input,
				// add it to the list of compatible inputs
				compatibleInputs.add(input.getName());
			}
		}
		// if only single input left, fill module input and resolve 'inputChoice'
		if (compatibleInputs.size() == 1) {
			choiceInput.setValue(this, compatibleInputs.get(0));
			resolveInput("inputChoice");
		}
		else if (compatibleInputs.size() > 1) {
			choiceInput.setChoices(compatibleInputs);
		}
	}

	// -- Main method --

	@Override
	public void run() {
		// mark inputChoice as resolved, then harvest script parameters (i.e. run)
		ModuleItem<File> fileInput = moduleInfo.getInput(inputChoice, File.class);
		// TODO check if conversion needed?
		Module scriptModule = modules.createModule(moduleInfo);
		scriptModule.resolveInput(inputChoice);

		/* Create output Table and mark all outputs as resolved */
		outputTable = new DefaultGenericTable();
		@SuppressWarnings("rawtypes")
		List<Column> columns = new ArrayList<>();

		for (String outputKey : scriptModule.getOutputs().keySet()) {
			columns.add(outputTable.appendColumn(outputKey));
			scriptModule.resolveOutput(outputKey);
		}

		for (File file: inputFileList) {
			if(!processFile(scriptModule, fileInput, file)) {
				log.warn("Terminating batch process.");
				break; // end for loop
			}
		}

		// case File
		//   feed files into input
		// case Image (not needed if conversion works
		//   open each file as image (warn on errors) and feed image into input
	}
	
	// -- Helper methods --
	
	@SuppressWarnings("unchecked")
	private boolean processFile(Module module, ModuleItem<File> fileInput, File file) {
		fileInput.setValue(module, file);
		outputTable.appendRow(file.getName());

		Future<Module> instance = modules.run(module, true);
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
