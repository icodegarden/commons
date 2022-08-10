package io.github.icodegarden.commons.lang.query;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public abstract class AbstractTableDataCountCollector implements TableDataCountCollector {

	/**
	 * null时不起作用
	 */
	private final Set<String> whiteListTables;
//			private Set<String> blackListTables = new HashSet<String>(Arrays.asList("table_data_count", "id_sequence"));

	private List<String> listTablesCache;

	public AbstractTableDataCountCollector() {
		this(null);
	}

	/**
	 * 
	 * @param whiteListTables 只关心对应的表
	 */
	public AbstractTableDataCountCollector(Set<String> whiteListTables) {
		this.whiteListTables = whiteListTables;
	}

	public List<String> listTables() {
		if (listTablesCache == null) {
			List<String> list = doListTables();
			if (whiteListTables != null) {
				list = doListTables().stream().filter(table -> {
//					return !blackListTables.contains(table);
					return whiteListTables.contains(table);
				}).collect(Collectors.toList());
			}

			listTablesCache = list;
		}
		return listTablesCache;
	}

	protected abstract List<String> doListTables();
}