package 江西宁新.NX20220825抽取.结算单;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {
    public static void main(String[] args) {
        Pattern p = Pattern.compile("No:([A-Z]+\\d{7})");
        Matcher m = p.matcher("销售结算单 No:XS0002199");
        if(m.find())
            System.out.println(m.group(1));
    }
}
