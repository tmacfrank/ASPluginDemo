package utils;

import com.intellij.openapi.project.Project;
import org.apache.http.util.TextUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class CommonUtils {

    /**
     * 解析 AndroidManifest 中的 packageName 并返回
     */
    public static String getPackageName(Project project) {
        String packageName;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
            Document document = documentBuilder.parse(project.getBasePath() + "/app/src/main/AndroidManifest.xml");
            // 拿到 manifest 标签对应的 Element，然后遍历其中的节点，直到找到 package
            NodeList nodeList = document.getElementsByTagName("manifest");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                Element element = (Element) node;
                packageName = element.getAttribute("package");
                if (packageName != null) {
                    return packageName;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String capitalize(String string) {
        if (TextUtils.isEmpty(string)) {
            throw new NullPointerException("String is null!");
        }

        String firstChar = String.valueOf(string.charAt(0)).toUpperCase();
        String others = string.substring(1);
        return firstChar + others;
    }
}
