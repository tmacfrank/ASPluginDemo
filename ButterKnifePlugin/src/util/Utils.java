package util;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.psi.*;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.ui.JBColor;
import entity.Element;
import org.apache.http.util.TextUtils;

import java.awt.*;
import java.util.List;
import java.util.Locale;

public class Utils {

    public static String firstToUpperCase(String key) {
        return key.substring(0, 1).toUpperCase(Locale.CHINA) + key.substring(1);
    }

    public static void showPopupBalloon(Editor editor, String message, int time) {
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                JBPopupFactory factory = JBPopupFactory.getInstance();
                factory.createHtmlTextBalloonBuilder(message, null,
                        new JBColor(new Color(116, 214, 238), new Color(76, 112, 117)), null)
                        .setFadeoutTime(time * 1000)
                        .createBalloon()
                        .show(factory.guessBestPopupLocation(editor), Balloon.Position.below);
            }
        });
    }

    /**
     * 解析 psiFile 中添加了 id 的组件，将该组件信息封装到 Element 元素中，并存入
     * elements 集合
     */
    public static List<Element> getIDsFromLayout(PsiFile psiFile, List<Element> elements) {
        // 遍历一个文件的所有元素
        analyzeFromXml(psiFile, elements);
        return elements;
    }

    private static void analyzeFromXml(PsiFile psiFile, List<Element> elements) {
        psiFile.accept(new XmlRecursiveElementVisitor() {
            @Override
            public void visitElement(PsiElement element) {
                super.visitElement(element);
                if (element instanceof XmlTag) {
                    XmlTag xmlTag = (XmlTag) element;
                    String name = xmlTag.getName();
                    // 如果 xml 中有 include 标签，那就要递归去找 include 布局中的标签
                    if ("include".equalsIgnoreCase(name)) {
                        XmlAttribute layout = xmlTag.getAttribute("layout");
                        Project project = psiFile.getProject();
                        String layoutName = getLayoutName(layout.getValue()) + ".xml";
                        PsiFile[] psiFiles = FilenameIndex.getFilesByName(project, layoutName, GlobalSearchScope.allScope(project));
                        if (psiFiles.length > 0) {
                            // 开始递归
                            analyzeFromXml(psiFiles[0], elements);
                            return;
                        }
                    }

                    // 没有 include 的一般情况，去找 android:id 属性
                    XmlAttribute id = xmlTag.getAttribute("android:id", null);
                    if (id == null) return;

                    String value = id.getValue();
                    if (value == null) return;

                    XmlAttribute aClass = xmlTag.getAttribute("class");
                    if (aClass != null) {
                        name = aClass.getValue();
                    }

                    Element newElement = new Element(name, value, xmlTag);
                    elements.add(newElement);
                }
            }
        });
    }

    /**
     * @param layoutValue 布局文件中 include 标签对应的属性值，形如 @layout/layout_view，
     *                    从中截取出 layout 文件名 layout_view 返回
     */
    private static String getLayoutName(String layoutValue) {
        if (TextUtils.isEmpty(layoutValue) || !layoutValue.startsWith("@") || !layoutValue.contains("/"))
            return null;

        String[] segments = layoutValue.split("/");
        if (segments.length != 2) {
            return null;
        }

        return segments[1];
    }

    /**
     * 根据当前文件获取对应的 psiClass 文件
     */
    public static PsiClass getTargetClass(Editor editor, PsiFile psiFile) {
        int offset = editor.getCaretModel().getOffset();
        PsiElement element = psiFile.findElementAt(offset);
        if (element == null) {
            return null;
        } else {
            PsiClass target = PsiTreeUtil.getParentOfType(element, PsiClass.class);
            return target instanceof SyntheticElement ? null : target;
        }
    }
}
