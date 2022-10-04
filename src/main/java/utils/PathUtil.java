package utils;

import java.io.File;

public class PathUtil {

    public static String trim(String str) {
        char[] chars = str.toCharArray();
        int len = chars.length;
        int st = 0;
        char[] val = chars;

        while ((st < len) && (val[st] <= ' '
                || val[st] == '/'
                || val[st] == '\\')) {
            st++;
        }
        while ((st < len) && (val[len - 1] <= ' '
                || val[st] == '/'
                || val[st] == '\\')) {
            len--;
        }
        return ((st > 0) || (len < chars.length)) ? str.substring(st, len) : str;
    }

    public static String getCanonicalPath(String path) {
        if (StringUtil.isEmpty(path)) {
            return null;
        }

        path = path.replaceAll("[<>|:*?]", "");
        String[] split = path.split("[/\\\\]");
        int len = split.length;
        split[len-1] = getCanonicalFileName(split[len-1]);
        return StringUtil.concat("/",split);
    }

    public static String getCanonicalFileName(String fileName) {
        if (StringUtil.isEmpty(fileName)) {
            return null;
        }

        fileName = fileName.replaceAll("[<>|:*?/\\\\]", "");
        String[] split = fileName.split(".");
        int len = split.length;
        if (len <= 0) return fileName;
        split[len - 1] = split[len - 1].trim();
        return StringUtil.concat("", split);
    }


    public static  PathBuilder newPathBuilder(){
        return new PathBuilder();
    }


   public static class PathBuilder {
        private static final String SEPARATOR = File.separator;
        StringBuilder sb = new StringBuilder();

        public PathBuilder appendChild(String childPath) {
            sb.append(SEPARATOR).append(trim(childPath));
            return this;
        }

        public PathBuilder append(String str) {
            sb.append(str);
            return this;
        }


        public String build() {
            return sb.toString();
        }
    }
}
