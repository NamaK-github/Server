package filters;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by NamaK on 07.03.2017.
 */
public class TestFilter implements ChatFilter {
    private List<String> censoredList;

    public TestFilter() {
        censoredList = new LinkedList<String>();
        censoredList.add("водка");
        censoredList.add("коньяк");
        censoredList.add("пиво");
        censoredList.add("самогон");
    }

    @Override
    public String filter(String message) {
        for (String word : censoredList) {
            message = message.replaceAll(word, "лимонад");
        }
        return message;
    }
}
