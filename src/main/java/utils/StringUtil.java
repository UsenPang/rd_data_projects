package utils;

public class StringUtil {
    public static boolean isEmpty(String str) {
        return str == null || str.equals("");
    }

    public static String rmBlank(String str) {
        if (isEmpty(str)) return "";
        return str.replaceAll("\\p{Z}", "");
    }

    public static String concat(String separator,String... strings) {
        if (strings == null || strings.length <= 0)
            return null;

        StringBuilder sb = new StringBuilder();
        sb.append(strings[0]);
        for (int i = 1; i < strings.length; i++) {
            sb.append(separator).append(strings[i]);
        }
        return sb.toString();
    }

    public static boolean containsAll(String content, String... keyWords) {
        if (keyWords == null || keyWords.length <= 0) return false;
        for (String keyWord : keyWords) {
            if (!content.contains(keyWord)) return false;
        }
        return true;
    }

    public static boolean containsAny(String content, String... keyWords) {
        if (keyWords == null || keyWords.length <= 0 || isEmpty(content)) return false;
        for (String keyWord : keyWords) {
            if (content.contains(keyWord)) return true;
        }
        return false;
    }

    public static String anyWordOf(String content,String... keyWords){
        if (keyWords == null || keyWords.length <= 0) return null;
        for (String keyWord : keyWords) {
            if (content.contains(keyWord)) return keyWord;
        }
        return null;
    }
}
