package mapper;


import lombok.Data;
import lombok.NoArgsConstructor;
import util.StringUt;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class Table implements Serializable {
    String name;
    Set<String> pkColumns;
    Set<String> otherColumns;

    public List<String> getAllColumns() {
        List<String> columns = new ArrayList<>();
        columns.addAll(getPkColumns());
        columns.addAll(getOtherColumns());
        return columns;
    }


    public Table(String name, Collection<String> pkColumns, Collection<String> subColumns) {
        this.name = StringUt.lowerTrim(name);
        this.pkColumns = pkColumns.stream().map(StringUt::lowerTrim).collect(Collectors.toSet());
        this.otherColumns = subColumns.stream().map(StringUt::lowerTrim).collect(Collectors.toSet());
    }

    public void addColumns(Set<String> columns) {
        if (columns != null) {
            Set<String> current = new HashSet<>(otherColumns);
            current.addAll(columns);
            otherColumns = current;
        }
    }

    public boolean hasCol(String colName) {
        return StringUt.anyContainsFirst(colName, pkColumns, otherColumns);
    }
}
