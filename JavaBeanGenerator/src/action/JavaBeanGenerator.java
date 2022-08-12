package action;

import com.intellij.ide.IdeView;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import object.FieldElement;
import org.apache.commons.collections.map.HashedMap;
import utils.CommonUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JavaBeanGenerator extends AnAction {

    // 定义要生成的 JavaBean 包含的字段
    private String fieldStr = "name String\n" +
            "age int\n" + "id Integer\n";

    // 字段的访问限定
    private String accessController = "public";
    private List<FieldElement> fieldElements;

    @Override
    public void actionPerformed(AnActionEvent e) {
        // 生成一个名字为 User 的 JavaBean 文件
        generateFile(e, "User", fieldStr);
    }

    /**
     * 根据 fieldStr 字符串中定义的字段，生成名字为 fileName 的 JavaBean 文件
     */
    private void generateFile(AnActionEvent actionEvent, String fileName, String fieldStr) {
        // 获取当前工程对象
        Project project = actionEvent.getProject();
        // 得到目录服务
        JavaDirectoryService directoryService = JavaDirectoryService.getInstance();
        // 得到当前菜单选项的相对路径，会在该路径下生成 JavaBean 文件
        IdeView ideView = actionEvent.getRequiredData(LangDataKeys.IDE_VIEW);
        PsiDirectory directory = ideView.getOrChooseDirectory();

        // 将模板文件中需要填写的参数放入 Map 中
        Map<String, String> map = new HashedMap();
        map.put("NAME", fileName);
        map.put("INTERFACES", "implements Serializable");
        map.put("PACKAGE_NAME", CommonUtils.getPackageName(project));

        // 开始生成文件，createClass() 的第三个参数必须和模板文件的文件名保持一致，不用写扩展名
        // Psi:Program Structure Interface，即程序结构接口
        PsiClass psiClass = directoryService.createClass(directory, fileName, "GenerateFileByString", false, map);
        WriteCommandAction.runWriteCommandAction(project,
                new Runnable() {
                    @Override
                    public void run() {
                        // 加入字段
                        generateModelField(project, psiClass, fieldStr);
                        // 加入 getter&setter 方法
                        generateModelMethod(project, psiClass, fieldElements);
                    }
                });
    }

    /**
     * 生成字段的声明语句
     */
    private void generateModelField(Project project, PsiClass psiClass, String fieldStr) {
        if (psiClass == null) {
            return;
        }

        PsiElementFactory factory = JavaPsiFacade.getInstance(project).getElementFactory();
        // fieldStr 是以 \n 分隔每个字段的，这里区分开，得到每个字段的字符串
        String[] lineString = fieldStr.split("\n");

        if (lineString.length < 0) {
            return;
        }

        fieldElements = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lineString.length; i++) {
            String[] temp = lineString[i].split(" ");
            String fieldName = temp[0];
            String fieldType = temp[1];
            // 记录下所有字段的类型和名字，下一步生成方法时要用
            FieldElement fieldElement = new FieldElement(fieldType, fieldName);
            fieldElements.add(fieldElement);
            // 拼接出 public fieldType fieldName;
            sb.append(accessController + " " + fieldType + " " + fieldName + ";");
            PsiField psiField = factory.createFieldFromText(sb.toString(), psiClass);
            psiClass.add(psiField);
            sb.delete(0, sb.length());
        }
    }

    /**
     * 生成字段的 getter 和 setter 方法
     */
    private void generateModelMethod(Project project, PsiClass psiClass, List<FieldElement> elements) {
        if (psiClass == null || elements.size() == 0) {
            return;
        }

        PsiElementFactory factory = JavaPsiFacade.getInstance(project).getElementFactory();
        StringBuilder sb = new StringBuilder();
        // 遍历所有字段生成 getter 和 setter 方法
        for (FieldElement element : elements) {
            // 拼接 getter 方法
            sb.append(accessController + " " + element.getFieldType() + " get" + CommonUtils.capitalize(element.getFieldName()) + "() {" + "\n");
            sb.append("return " + element.getFieldName() + ";\n");
            sb.append("}");
            PsiMethod psiMethod = factory.createMethodFromText(sb.toString(), psiClass);
            psiClass.add(psiMethod);
            sb.delete(0, sb.length());
            // 拼接 setter 方法
            sb.append(accessController + " void" + " set" + CommonUtils.capitalize(element.getFieldName()) + "(" +
                    element.getFieldType() + " " + element.getFieldName() + ") {" + "\n");
            sb.append("this." + element.getFieldName() + " = " + element.getFieldName() + ";\n");
            sb.append("}");
            psiMethod = factory.createMethodFromText(sb.toString(), psiClass);
            psiClass.add(psiMethod);
            sb.delete(0, sb.length());
        }
    }
}
