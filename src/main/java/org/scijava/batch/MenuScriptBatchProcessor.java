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
import org.scijava.module.ModuleService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.script.ScriptInfo;
import org.scijava.script.ScriptService;
import org.scijava.widget.ChoiceWidget;
import org.scijava.widget.FileWidget;

@Plugin(type = Command.class, label = "Process Folder", menuPath = "Process>Batch>Run Script from Menu")
public class MenuScriptBatchProcessor extends DynamicCommand {

	private final String WILDCARD = "Wildcard";
	private final String REGEX = "Regex";

	@Parameter
	private ModuleService modules;

	@Parameter
	private ScriptService scripts;

	@Parameter
	private CommandService commands;

	@Parameter(label = "Script to run", style = "java.io.File")
	private ScriptInfo scriptInfo; // ScriptInfoWidget

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

	@Parameter(label = "Output directory", style = FileWidget.DIRECTORY_STYLE, required = false)
	private File outputFolder;

	private FilenameFilter inputFilter;
	private List<File> fileList;

	@Override
	public void run() {
		HashMap<String, Object> inputMap = new HashMap<>();
		inputMap.put("moduleInfo", scriptInfo);
		File[] fileArray = fileList.toArray(new File[fileList.size()]);
		inputMap.put("inputFileList", fileArray);
		inputMap.put("outputFolder", outputFolder);
		commands.run(ModuleBatchProcessor.class, true, inputMap);
	}

	// -- Helper methods --

	private List<File> populateFileList(File folder, FilenameFilter filter,
			boolean rec) {
		List<File> list = new ArrayList<>();
		if (rec) {
			for (File file : folder
					.listFiles((FilenameFilter) DirectoryFileFilter.DIRECTORY)) {
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
		// get list of all applicable files
		try {
			switch (filterChoice) {
			case WILDCARD:
				inputFilter = new WildcardFileFilter(pattern);
				break;
			case REGEX:
			default:
				inputFilter = new RegexFileFilter(pattern);
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
}
