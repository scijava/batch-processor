
package org.scijava.batch.input;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import org.scijava.module.Module;
import org.scijava.module.ModuleItem;
import org.scijava.plugin.AbstractHandlerPlugin;
import org.scijava.plugin.Plugin;
import org.scijava.widget.FileListWidget;
import org.scijava.widget.FileWidget;

@Plugin(type = BatchInputProvider.class)
public class FileBatchInputProvider extends AbstractHandlerPlugin<BatchInput> implements
	BatchInputProvider<File>
{
	@Override
	public boolean supports(BatchInput input) {
		return canProvide(input.moduleItem());
	}

	@Override
	public boolean canProvide(ModuleItem<?> item) {
		// we can't provide inputs for saving files
		return item.getType() == File.class && !hasStyle(item, FileWidget.SAVE_STYLE);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void populateInput(Module module, ModuleItem<?> moduleItem, File inputObject) {
		((ModuleItem<File>)moduleItem).setValue(module, inputObject);
	}

	@Override
	public String getTargetWidgetStyle(ModuleItem<?> item) {
		ArrayList<String> targetStyles = new ArrayList<>();

		if (hasStyle(item, FileWidget.DIRECTORY_STYLE)) {
			targetStyles.add(FileListWidget.DIRECTORIES_ONLY);
		} else {
			targetStyles.add(FileListWidget.FILES_ONLY);
		}

		// extensions?
		String widgetStyle = item.getWidgetStyle();
		if (widgetStyle != null) {
			String[] styles = widgetStyle.trim().split("\\s*,\\s*");
			for (String s : styles) {
				if (s.startsWith("extensions")) { // TODO: use new constant from FileListWidget
					targetStyles.add(s);
				}
			}			
		}
		return String.join(",", targetStyles);
	}

	private boolean hasStyle(ModuleItem<?> item, String style) {
		String widgetStyle = item.getWidgetStyle();
		if (widgetStyle == null) return false;
		return Arrays.asList(widgetStyle.trim().split("\\s*,\\s*"))
			.contains(style);
	}
}
