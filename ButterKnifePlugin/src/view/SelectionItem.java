package view;

import entity.Element;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

/**
 * 插件 UI 显示时，一行数据中所涉及的 UI 结构被封装到 SelectionItem 中
 */
public class SelectionItem extends JPanel {

    private JCheckBox enableCheckBox;
    private JLabel idLabel;
    private JCheckBox clickCheckBox;
    private JTextField fieldTextField;

    /**
     * widgetCheckBox 的勾选接口
     */
    public interface EnableActionListener {
        void onSetEnable(JCheckBox enableCheckBox, Element element);
    }

    private EnableActionListener enableActionListener;

    public void setEnableActionListener(EnableActionListener enableActionListener) {
        this.enableActionListener = enableActionListener;
    }

    /**
     * clickCheckBox 的勾选接口
     */
    public interface ClickActionListener {
        void onSetClick(JCheckBox clickCheckBox);
    }

    private ClickActionListener clickActionListener;

    public void setClickActionListener(ClickActionListener clickActionListener) {
        this.clickActionListener = clickActionListener;
    }

    /**
     * fieldTextField 接口
     */
    public interface FieldFocusListener {
        void onSetFieldName(JTextField fieldJTextField);
    }

    private FieldFocusListener fieldFocusListener;

    public void setFieldFocusListener(FieldFocusListener fieldFocusListener) {
        this.fieldFocusListener = fieldFocusListener;
    }

    public SelectionItem(LayoutManager layout, EmptyBorder emptyBorder, JCheckBox enableCheckBox, JLabel idLabel,
                         JCheckBox clickCheckBox, JTextField fieldTextField, Element element) {
        super(layout);
        initLayout(layout, emptyBorder);
        this.enableCheckBox = enableCheckBox;
        this.idLabel = idLabel;
        this.clickCheckBox = clickCheckBox;
        this.fieldTextField = fieldTextField;
        initComponent(element);
        addComponent();
    }

    private void initLayout(LayoutManager layout, EmptyBorder emptyBorder) {
        this.setLayout(layout);
        this.setBorder(emptyBorder);
    }

    /**
     * 初始化各个组件，并且为它们设置监听
     */
    private void initComponent(Element element) {
        // 初始状态
        clickCheckBox.setEnabled(true);
        idLabel.setEnabled(element.isCreateField());
        fieldTextField.setEnabled(element.isCreateField());
        enableCheckBox.setSelected(element.isCreateField());
        clickCheckBox.setSelected(element.isCreateClickMethod());

        // 左对齐
        enableCheckBox.setHorizontalAlignment(JLabel.LEFT);
        idLabel.setHorizontalAlignment(JLabel.LEFT);
        fieldTextField.setHorizontalAlignment(JTextField.LEFT);

        // 监听
        enableCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (enableActionListener != null) {
                    enableActionListener.onSetEnable(enableCheckBox, element);
                    idLabel.setEnabled(enableCheckBox.isSelected());
                    fieldTextField.setEnabled(enableCheckBox.isSelected());
                }
            }
        });

        clickCheckBox.addActionListener(e -> {
            if (clickActionListener != null) {
                clickActionListener.onSetClick(clickCheckBox);
            }
        });

        fieldTextField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (fieldFocusListener != null) {
                    fieldFocusListener.onSetFieldName(fieldTextField);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (fieldFocusListener != null) {
                    fieldFocusListener.onSetFieldName(fieldTextField);
                }
            }
        });
    }

    private void addComponent() {
        this.add(enableCheckBox);
        this.add(idLabel);
        this.add(clickCheckBox);
        this.add(fieldTextField);
    }
}
