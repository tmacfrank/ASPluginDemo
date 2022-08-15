package entity;

import com.intellij.psi.xml.XmlTag;
import util.Utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Element {

    /**
     * 从 layout 文件中截取 id 的正则表达式，如从 android:id="@+id/text1" 中
     * 截取出的是 @+id/text1 这部分
     */
    private static final Pattern idPattern = Pattern.compile("@\\+?(android:)?id/([^$]+)$", Pattern.CASE_INSENSITIVE);

    /**
     * fieldName 的命名有三种方式，分别对应 fieldNameType 的值为 1/2/3
     * 1:aa_bb_cc; 2:aaBbCc; 3:mAaBbCc
     */
    private int fieldNameTypes = 3;

    /**
     * layout 文件中组件的 id 字符串，如对于 android:id="@+id/text1" 而言，
     * id 应为 text1
     */
    private String id;

    /**
     * Java 文件中字段的类型名称
     */
    private String fieldTypeName;

    /**
     * Java 文件中字段的名称
     */
    private String fieldName;

    private XmlTag xmlTag;

    /**
     * 表示是否声明当前字段为成员，并且加上 @BindView 注解，形如：
     *
     * @BindView(R.id.button1) public Button mButton1;
     * <p>
     * 默认是会生成的，可以通过插件 UI 勾选 CheckBox 决定是否生成
     */
    private boolean createField = true;

    /**
     * 表示是否为当前字段生成一个方法并用 @OnClick 注解，形如：
     *
     * @OnClick(R.id.text1) public void text1Click(TextView text1) {
     * }
     * 默认不生成，可以通过插件 UI 勾选 CheckBox 决定是否生成
     */
    private boolean createClickMethod = false;

    /**
     * 解析 layout 文件时的有效数据，保存在 Element 中
     *
     * @param fieldTypeName 组件标签上的类名，可能是一个单独的单词，如 TextView、Button 等，
     *                      也可能是全类名，如 androidx.appcompat.widget.AppCompatTextView
     * @param id            属性 android:id 的值，形如 @+id/text
     * @param xmlTag        布局文件中 xml 标签对象
     */
    public Element(String fieldTypeName, String id, XmlTag xmlTag) {
        // 如果传入的类型名是全类名，则用 . 分开只要最后一段
        String[] segments = fieldTypeName.split("\\.");
        if (segments.length > 1) {
            this.fieldTypeName = segments[segments.length - 1];
        } else {
            this.fieldTypeName = fieldTypeName;
        }

        // 获取 id，根据 idPattern 中的正则表达式，group(2) 拿到的是 id 字符串
        Matcher matcher = idPattern.matcher(id);
        if (matcher.find() && matcher.groupCount() > 1) {
            this.id = matcher.group(2);
        }

        if (this.id == null) {
            throw new IllegalArgumentException("Invalid format of view id.");
        }
        this.xmlTag = xmlTag;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFieldTypeName() {
        return fieldTypeName;
    }

    public void setFieldTypeName(String typeName) {
        this.fieldTypeName = typeName;
    }

    public XmlTag getXmlTag() {
        return xmlTag;
    }

    public void setXmlTag(XmlTag xmlTag) {
        this.xmlTag = xmlTag;
    }

    public boolean isCreateField() {
        return createField;
    }

    public void setCreateField(boolean createField) {
        this.createField = createField;
    }

    public boolean isCreateClickMethod() {
        return createClickMethod;
    }

    public void setCreateClickMethod(boolean createClickMethod) {
        this.createClickMethod = createClickMethod;
    }

    /**
     * 获取组件的变量名，例如 private Button button; 中的 button，
     * 这个名字需要根据 id 字符串生成，并且当 fieldNameType 取值
     * 不同时，有不同的命名风格：
     * 1:aa_bb_cc; 2:aaBbCc; 3:mAaBbCc
     */
    public String getFieldName() {
        if (fieldName != null) {
            return fieldName;
        }

        if (fieldNameTypes == 1) {
            fieldName = id;
        } else {
            String[] names = id.split("_");
            StringBuilder sb = new StringBuilder();
            if (fieldNameTypes == 2) {
                // aaBbCc
                for (int i = 0; i < names.length; i++) {
                    if (i == 0) {
                        sb.append(names[i]);
                    } else {
                        sb.append(Utils.firstToUpperCase(names[i]));
                    }
                }
            } else if (fieldNameTypes == 3) {
                // mAaBbCc
                for (int i = 0; i < names.length; i++) {
                    if (i == 0) {
                        sb.append("m");
                    }
                    sb.append(Utils.firstToUpperCase(names[i]));
                }
            }
            fieldName = sb.toString();
        }

        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    /**
     * 返回一个完全的表示 id 的字符串，如：R.id.xxx
     */
    public String getFullIdString() {
        String prefix = "R.id.";
        return prefix + id;
    }
}
