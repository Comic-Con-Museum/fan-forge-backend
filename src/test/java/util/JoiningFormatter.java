package util;

import com.tngtech.jgiven.format.ArgumentFormatter;

public class JoiningFormatter implements ArgumentFormatter<Iterable<? extends CharSequence>> {
    public static class Array implements ArgumentFormatter<String[]> {
        @Override
        public String format(String[] s, String... args) {
            return String.join(args.length == 0 ? ", " : args[0], s);
        }
    }
    
    @Override
    public String format(Iterable<? extends CharSequence> s, String... args) {
        return String.join(args.length == 0 ? ", " : args[0], s);
    }
}
