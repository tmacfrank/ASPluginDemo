package action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiUtilBase;
import com.intellij.psi.xml.XmlFile;
import entity.Element;
import org.apache.http.util.TextUtils;
import util.Utils;
import view.FindViewByIdDialog;

import java.util.ArrayList;
import java.util.List;

public class ButterKnifePlugin extends AnAction {

    private FindViewByIdDialog dialog;
    private String xmlFileName;

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        // 得到编辑区对象
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        if (editor == null) {
            return;
        }

        // 获取用户选择的字符（用户应该选择 xml 文件名）
        SelectionModel model = editor.getSelectionModel();
        xmlFileName = model.getSelectedText();

        // 如果用户没有选择任何内容，则先检查光标所在的那一行代码是否有布局文件名字
        if (TextUtils.isEmpty(xmlFileName)) {
            // 获取光标所在位置那一行中的布局文件名
            xmlFileName = getCurrentLayout(editor);
            if (TextUtils.isEmpty(xmlFileName)) {
                // 如果还没有就弹对话框让用户自己输入
                xmlFileName = Messages.showInputDialog(project, "输入layout名称", "未输入", Messages.getInformationIcon());
                if (TextUtils.isEmpty(xmlFileName)) {
                    Utils.showPopupBalloon(editor, "用户没有输入layout", 5);
                    return;
                }
            }
        }

        // 找到 xmlFileName 对应的 xml 文件，获取所有 id 并保存到 elements 集合中
        PsiFile[] psiFiles = FilenameIndex.getFilesByName(project, xmlFileName + ".xml", GlobalSearchScope.allScope(project));
        if (psiFiles.length == 0) {
            Utils.showPopupBalloon(editor, "未找到选中的布局文件" + xmlFileName, 5);
            return;
        }

        XmlFile xmlFile = (XmlFile) psiFiles[0];
        List<Element> elements = new ArrayList<>();
        Utils.getIDsFromLayout(xmlFile, elements);

        // 生成选择对话框，并根据用户的选择生成代码
        if (elements.size() != 0) {
            // 获取 editor 所在文件的 PsiFile 对象
            PsiFile psiFile = PsiUtilBase.getPsiFileInEditor(editor, project);
            PsiClass psiClass = Utils.getTargetClass(editor, psiFile);
            // 生成 UI
            dialog = new FindViewByIdDialog(editor, project, psiFile, psiClass, elements, xmlFileName);
            dialog.showDialog();
        }
    }

    /**
     * 检查光标所在的那一行是否有 layout 文件名，有则返回
     */
    private String getCurrentLayout(Editor editor) {
        Document document = editor.getDocument();
        // 获取光标对象并得到其位置（相对于左上角编辑区起始点的偏移量）
        CaretModel caretModel = editor.getCaretModel();
        int caretOffset = caretModel.getOffset();
        // 得到一行开始和结束的位置
        int lineNumber = document.getLineNumber(caretOffset);
        int lineStartOffset = document.getLineStartOffset(lineNumber);
        int lineEndOffset = document.getLineEndOffset(lineNumber);
        // 得到一行的所有字符串
        String lineContent = document.getText(new TextRange(lineStartOffset, lineEndOffset));
        String layoutMatcher = "R.layout.";

        System.out.println("lineContent:" + lineContent + ",contain:" + lineContent.contains(layoutMatcher));
        if (!TextUtils.isEmpty(lineContent) && lineContent.contains(layoutMatcher)) {
            // 获取 layout 文件名的字符串
            int startPosition = lineContent.indexOf(layoutMatcher) + layoutMatcher.length();
            int endPosition = lineContent.indexOf(")", startPosition);
            return lineContent.substring(startPosition, endPosition);
        }
        return null;
    }
}
