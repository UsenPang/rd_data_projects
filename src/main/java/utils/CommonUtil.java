package utils;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class CommonUtil {


    public static String cleanBlank(String target){
        if(isEmpty(target))
            return "";
        return target.replaceAll("\\s+","");
    }

    public static boolean isEmpty(String target){
        return target==null || "".equals(target);
    }

    public List<Elements> tableSplit(Element document){
        Elements elements = document.select("p,table");
        List<Elements> elList = new ArrayList<>();
        Elements els = new Elements();
        for (Element element : elements) {
            els.add(element);
            if("table".equals(element.tagName())){
                elList.add(els);
                els = new Elements();
            }
        }
        return elList;
    }
}
