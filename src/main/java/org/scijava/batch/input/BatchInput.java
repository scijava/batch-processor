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
