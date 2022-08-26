/*-
 * #%L
 * A Batch Processor for SciJava Modules and Scripts
 * %%
 * Copyright (C) 2017 - 2022 Friedrich Miescher Institute for Biomedical Research, Basel (Switzerland)
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
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.scijava.ItemVisibility;
import org.scijava.command.Command;
import org.scijava.command.CommandService;
import org.scijava.command.DynamicCommand;
import org.scijava.convert.ConvertService;
import org.scijava.module.ModuleInfo;
import org.scijava.module.ModuleItem;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.script.ScriptService;
import org.scijava.widget.ChoiceWidget;
import org.scijava.widget.FileWidget;

@Plugin(type = Command.class, label = "Process Folder", menuPath = "Process>Batch>Run Script from File")
public class FileScriptBatchProcessor extends DynamicCommand {

	private final String WILDCARD = "Wildcard";
	private final String REGEX = "Regex";

	@Parameter
	private ScriptService scripts;

	@Parameter
	private CommandService commands;
	
	@Parameter
	private ConvertService convert;

	@Parameter(label = "Script file to run", callback = "scriptFileCallback")
	private File scriptFile;
	
	@Parameter(visibility=ItemVisibility.MESSAGE, persist = false)
	private String scriptMessage = "";

	@Parameter(style = FileWidget.DIRECTORY_STYLE, callback = "directoryCallback")
	private File inputFolder;

	@Parameter(label = "Recursive (with subfolders)", callback = "directoryCallback")
	private boolean recursive;

	@Parameter(label = "File selection method", choices = { WILDCARD, REGEX }, style = ChoiceWidget.RADIO_BUTTON_HORIZONTAL_STYLE, callback = "directoryCallback")
	private String filterChoice = WILDCARD;

	@Parameter(label = "File name pattern", callback = "directoryCallback")
	private String pattern = "*.tif";

	@Parameter(visibility = ItemVisibility.MESSAGE, persist = false)
	private String message = " "; // Placeholder message

//	@Parameter(label = "Output directory", style = FileWidget.DIRECTORY_STYLE, required = false)
//	private File outputFolder;

	private List<File> fileList;
	private ModuleInfo moduleInfo;

	@Override
	public void run() {
		// Get list of files from input folder
		if (fileList == null) {
			directoryCallback();
		}
		
		if (moduleInfo == null) {
			scriptFileCallback();
		}

		HashMap<String, Object> inputMap = new HashMap<>();
		inputMap.put("moduleInfo", moduleInfo);
		File[] fileArray = fileList.toArray(new File[fileList.size()]);
		inputMap.put("inputFileList", fileArray);
//		inputMap.put("outputFolder", outputFolder);
		commands.run(ModuleBatchProcessor.class, true, inputMap);
	}

	// -- Helper methods --

	private List<File> populateFileList(File folder, FilenameFilter filter, boolean rec) {
		List<File> list = new ArrayList<>();
		if (rec) {
			for (File file : folder.listFiles((FilenameFilter) DirectoryFileFilter.DIRECTORY)) {
				list.addAll(populateFileList(file, filter, rec));
			}
		}
		for (File file : folder.listFiles(filter)) {
			list.add(file);
		}
		return list;
	}
	
	// -- Callback methods --
	
	/**
	 * Count all applicable files and display them in the dialog message
	 */
	protected void directoryCallback() {
		if (inputFolder == null || !inputFolder.exists()) return;

		// get list of all applicable files
		FilenameFilter inputFilter;
		try {
			switch (filterChoice) {
			case WILDCARD:
				inputFilter = new WildcardFileFilter(pattern);
				break;
			case REGEX:
			default:
				inputFilter = new RegexFileFilter(pattern);
				break;
			}
			fileList = populateFileList(inputFolder, inputFilter, recursive);
		} catch (PatternSyntaxException e) {
			fileList = null;
			message = "Syntax error in regex.";
		}
		if (fileList != null) {
			message = "Found " + fileList.size() + " files.";
		}
	}

	protected void scriptFileCallback() {
		if (scriptFile != null) {
			moduleInfo = scripts.getScript(scriptFile);
			int nCompatibleInputs = 0;
			for (ModuleItem<?> input : moduleInfo.inputs()) {
				if (convert.supports(new File(""), input.getType())) {
					nCompatibleInputs++;
				}
			}
			scriptMessage = "This script contains " + nCompatibleInputs + " compatible inputs.";
		}
	}
}
