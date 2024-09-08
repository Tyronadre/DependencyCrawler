package converter;

import java.util.List;
import java.util.stream.Collectors;

public interface Converter<Original, Converted> {

    Converted convert(Original original);

    default List<Converted> convertList(List<Original> originalList) {
        if (originalList == null) return null;
        return originalList.stream().map(this::convert).collect(Collectors.toList());
    }
}
