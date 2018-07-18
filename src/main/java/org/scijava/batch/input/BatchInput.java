package org.scijava.batch.input;

import java.lang.reflect.Type;

import org.scijava.module.ModuleItem;

/**
 * Currency for use with {@link BatchInputProvider} methods
 * 
 * 
 * 
 * @author Jan Eglinger
 *
 */
public class BatchInput {
	private final Type srcType;
	private final ModuleItem<?> destItem;

	public BatchInput(final Class<?> srcClass, final ModuleItem<?> destItem) {
		this.srcType = srcClass;
		this.destItem = destItem;
	}

	public BatchInput(final Type srcType, final ModuleItem<?> destItem) {
		this.srcType = srcType;
		this.destItem = destItem;
	}

	public Type sourceType() {
		return srcType;
	}

	public ModuleItem<?> moduleItem() {
		return destItem;
	}
}
