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
package org.scijava.batch;

import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.search.DefaultSearchAction;
import org.scijava.search.SearchAction;
import org.scijava.search.SearchActionFactory;
import org.scijava.search.SearchResult;
import org.scijava.search.module.ModuleSearchResult;

/**
 * Search action for executing a SciJava module in batch mode.
 *
 * @author Jan Eglinger
 */
@Plugin(type = SearchActionFactory.class)
public class BatchModuleSearchActionFactory implements SearchActionFactory {

	@Parameter
	private BatchService batchService;

	@Override
	public boolean supports(final SearchResult result) {
		return (result instanceof ModuleSearchResult)
				&& batchService.supportsModule(((ModuleSearchResult) result).info());
	}

	@Override
	public SearchAction create(final SearchResult result) {
		return new DefaultSearchAction("Batch", () -> {
			batchService.run(((ModuleSearchResult) result).info());
		});
	}
}
