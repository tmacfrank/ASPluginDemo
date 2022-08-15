package test;

import com.intellij.psi.xml.XmlTag;
import entity.Element;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {
    public static void main(String[] args) {
        Element element = new Element("Button", "@+id/text1", null);
        String fieldName = element.getFieldName();
        System.out.println(fieldName);

        /*String string = "android:id=\"@+id/text1\"";

        Pattern idPattern = Pattern.compile("@\\+?(android:)?id/([^$])+$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = idPattern.matcher(string);
        while (matcher.find()) {
            System.out.println(matcher.group());
        }*/
    }
}
