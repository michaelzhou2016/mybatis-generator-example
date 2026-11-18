package com.example.plugin;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.*;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * https://www.jianshu.com/p/58ee7e09fc3f
 */
public class MyBatisPlugin extends PluginAdapter {

    @Override
    public boolean validate(List<String> list) {
        return true;
    }

    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        if ("true".equals(properties.getProperty("hasLombok", "false"))) {
            topLevelClass.addImportedType("lombok.Data");

            //添加domain的注解
            topLevelClass.addAnnotation("@Data");
        }

        // 可选：添加 @Builder
        if ("true".equals(properties.getProperty("builder", "false"))) {
            topLevelClass.addImportedType("lombok.Builder");
            topLevelClass.addAnnotation("@Builder");
        }

        // 可选：添加 @NoArgsConstructor 和 @AllArgsConstructor
        if ("true".equals(properties.getProperty("allArgsConstructor", "false"))) {
            topLevelClass.addImportedType("lombok.NoArgsConstructor");
            topLevelClass.addAnnotation("@NoArgsConstructor");
            topLevelClass.addImportedType("lombok.AllArgsConstructor");
            topLevelClass.addAnnotation("@AllArgsConstructor");
        }

        topLevelClass.addJavaDocLine("/**");

        Optional<String> remarksOptional = introspectedTable.getRemarks();
        if (remarksOptional.isPresent()) {
            String[] remarkLines = remarksOptional.get().split(System.getProperty("line.separator"));
            for (String remarkLine : remarkLines) {
                topLevelClass.addJavaDocLine(" * " + remarkLine);
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append(" * ").append(introspectedTable.getFullyQualifiedTable());
        topLevelClass.addJavaDocLine(sb.toString());
        sb.setLength(0);
        sb.append(" * @author ").append(System.getProperties().getProperty("user.name"));
        topLevelClass.addJavaDocLine(sb.toString());
        sb.setLength(0);
        sb.append(" * @since ");
        sb.append(getDateString());
        topLevelClass.addJavaDocLine(sb.toString());
        topLevelClass.addJavaDocLine(" */");
        return true;
    }

    @Override
    public boolean modelFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn,
                                       IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        field.addJavaDocLine("/**");
        Optional<String> remarksOptional = introspectedColumn.getRemarks();
        if (remarksOptional.isPresent()) {
            String[] remarkLines = remarksOptional.get().split(System.getProperty("line.separator"));
            for (String remarkLine : remarkLines) {
                field.addJavaDocLine(" * " + remarkLine);
            }
        }
        field.addJavaDocLine(" */");
        return true;
    }

    @Override
    public boolean clientGenerated(Interface interfaze, IntrospectedTable introspectedTable) {
        //添加Mapper的import
        interfaze.addImportedType(new FullyQualifiedJavaType("org.apache.ibatis.annotations.Mapper"));
        //添加Mapper的注解
        interfaze.addAnnotation("@Mapper");
        return true;
    }

    @Override
    public boolean modelSetterMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        //不生成getter
        return false;
    }

    @Override
    public boolean modelGetterMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        //不生成setter
        return false;
    }

    protected String getDateString() {
        return DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss");
    }
}