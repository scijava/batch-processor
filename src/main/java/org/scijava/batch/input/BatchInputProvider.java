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
