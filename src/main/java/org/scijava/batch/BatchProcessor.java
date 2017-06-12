package org.scijava.batch;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import net.imagej.ImageJ;
import net.imagej.table.Column;
import net.imagej.table.DefaultGenericTable;
import net.imagej.table.Table;

import org.scijava.ItemIO;
import org.scijava.MenuPath;
import org.scijava.command.Command;
import org.scijava.command.DynamicCommand;
import org.scijava.log.LogService;
import org.scijava.module.Module;
import org.scijava.module.ModuleItem;
import org.scijava.module.ModuleService;
import org.scijava.module.MutableModuleItem;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.script.ScriptInfo;
import org.scijava.script.ScriptService;

/**
 * Batch process a list of files in a folder by running a given script,
 * automatically providing the {@link File} input
 * 
 * @author Jan Eglinger
 */
@Plugin(type = Command.class, label = "Batch Processor", initializer = "initScriptChoice", menuPath = "Process>Batch>Process Files with Script")
public class BatchProcessor extends DynamicCommand {

	// -- Parameters --

	@Parameter
	private LogService log;

	@Parameter
	private ModuleService modules;

	@Parameter
	private ScriptService scripts;

	@Parameter(label = "Input directory", style = "directory")
	private File inputFolder;

	@Parameter(label = "File extension")
	private String extension;

	@Parameter(label = "Script to run")
	private String scriptChoice;

	@SuppressWarnings("rawtypes")
	@Parameter(type = ItemIO.OUTPUT)
	private Table outputTable;
	// -- Other fields --

	private Map<String, Module> scriptMap = new HashMap<>();

	// -- Initializer methods --

	/**
	 * Generate a list of all {@link ScriptInfo} objects that take a
	 * {@link File} input, and populate the parameter choices with their
	 * identifiers.
	 */
	protected void initScriptChoice() {
		MutableModuleItem<String> input = getInfo().getMutableInput(
				"scriptChoice", String.class);
		List<String> choices = new ArrayList<>();
		for (ScriptInfo script : scripts.getScripts()) {
			Module scriptModule = modules.createModule(script);
			if (modules.getSingleInput(scriptModule, File.class) != null) {
				String scriptName = getNiceName(script);
				choices.add(scriptName);
				scriptMap.put(scriptName, scriptModule);
			}
		}
		Collections.sort(choices);
		input.setChoices(choices);
	}

	// -- Helper methods --

	private String getNiceName(ScriptInfo script) {
		MenuPath menu = script.getMenuPath();
		return menu.getLeaf().getName() + " (in " + menu.getMenuString(false)
				+ ")";
	}

	// -- Main method --

	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		Module module = scriptMap.get(scriptChoice);
		ModuleItem<File> fileInput = modules.getSingleInput(module, File.class);

		/* Mark File input as resolved */
		module.resolveInput(fileInput.getName());

		/* Create output Table and mark all outputs as resolved */
		outputTable = new DefaultGenericTable();
		@SuppressWarnings("rawtypes")
		List<Column> columns = new ArrayList<>();

		for (String outputKey : module.getOutputs().keySet()) {
			columns.add(outputTable.appendColumn(outputKey));
			module.resolveOutput(outputKey);
		}

		FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				if (name.toLowerCase().endsWith(extension.toLowerCase()))
					return true;
				return false;
			}
		};

		int i = 0;
		for (File file : inputFolder.listFiles(filter)) {
			fileInput.setValue(module, file);
			outputTable.appendRow(file.getName());

			Future<Module> instance = modules.run(module, true);
			try {
				Map<String, Object> outputs = instance.get().getOutputs();
				for (Entry<String, Object> output : outputs.entrySet()) {
					outputTable.set(output.getKey(), i++, output.getValue());
				}
			} catch (InterruptedException exc) {
				log.error("Error: interrupted module execution", exc);
				return;
			} catch (ExecutionException exc) {
				log.error("Error during module execution", exc);
				// continue loop
			}
		}
	}

	/** Test the batch processor. */
	public static void main(final String... args) throws Exception {
		// Launch ImageJ as usual.
		final ImageJ ij = net.imagej.Main.launch(args);

		ij.command().run(BatchProcessor.class, true);
	}
}
