
package org.scijava.batch.input;

import org.scijava.module.Module;
import org.scijava.module.ModuleItem;
import org.scijava.plugin.HandlerPlugin;

public interface BatchInputProvider<I> extends HandlerPlugin<BatchInput> {

	@Override
	default public Class<BatchInput> getType() {
		return BatchInput.class;
	}

	/**
	 * Check if a given {@code ModuleItem} can be populated by this input provider.
	 * Implementations should make sure to not only match the type of objects, but
	 * also respect possible widget style settings, such as files, directories,
	 * image files only, etc.
	 *
	 * @param moduleItem
	 *            the input item that needs to be populated
	 * @return true if this input provider can provide suitable objects
	 */
	public boolean canProvide(ModuleItem<?> moduleItem);
	
	public void populateInput(Module module, ModuleItem<?> moduleItem, I inputObject);

	public String getTargetWidgetStyle(ModuleItem<?> moduleItem);

	// public Iterable<O> iterate(Collection<I> fileList);
}
