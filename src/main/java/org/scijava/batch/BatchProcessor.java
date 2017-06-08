package org.scijava.batch;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import net.imagej.ImageJ;

import org.scijava.command.Command;
import org.scijava.command.DynamicCommand;
import org.scijava.log.LogService;
import org.scijava.module.Module;
import org.scijava.module.ModuleException;
import org.scijava.module.ModuleInfo;
import org.scijava.module.ModuleItem;
import org.scijava.module.ModuleService;
import org.scijava.module.MutableModuleItem;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.script.ScriptInfo;
import org.scijava.script.ScriptModule;
import org.scijava.script.ScriptService;

/**
 * Batch process a list of files in a folder by running a given script,
 * automatically providing the {@link File} input
 * 
 * @author Jan Eglinger
 */
@Plugin(type = Command.class, label = "Batch Processor", initializer = "initScriptChoice", menuPath = "Process>Batch>SciJava Batch Processor")
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

	@Parameter(label = "Script to run")
	private String scriptChoice;

	// -- Initializer methods --

	/**
	 * Generate a list of all {@link ScriptInfo} objects that take a
	 * {@link File} input, and populate the parameter choices with their
	 * identifiers.
	 */
	protected void initScriptChoice() {
		MutableModuleItem<String> input = getInfo().getMutableInput("scriptChoice", String.class);
		List<String> choices = new ArrayList<>();
		for (ScriptInfo script : scripts.getScripts()) {
			try {
				if (modules.getSingleInput(script.createModule(), File.class) != null) {
					choices.add(script.getIdentifier());
					// choices.add(script.getPath());
					// TODO create map with readable names and modules
				}
			} catch (ModuleException exc) {
				log.warn("Could not create module for script: ", exc);
			}
		}
		input.setChoices(choices);
	}

	// -- Main method --

	@Override
	public void run() {
		ModuleInfo moduleInfo = modules.getModuleById(scriptChoice);
		ScriptModule module;
		try {
			module = (ScriptModule) moduleInfo.createModule();
		} catch (ModuleException exc) {
			log.error("Error during module creation", exc);
			return;
		}
		module.setContext(getContext());
		ModuleItem<File> fileInput = modules.getSingleInput(module, File.class);
		for (File file : inputFolder.listFiles()) {
			fileInput.setValue(module, file);
			module.resolveInput(fileInput.getName());

			Future<Module> instance = modules.run(module, true);
			try {
				log.info(instance.get());
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
