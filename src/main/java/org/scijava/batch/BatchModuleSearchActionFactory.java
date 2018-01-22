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
				&& batchService.supports(((ModuleSearchResult) result).info());
	}

	@Override
	public SearchAction create(final SearchResult result) {
		return new DefaultSearchAction("Batch", true, () -> {
			batchService.run(((ModuleSearchResult) result).info());
		});
	}
}
