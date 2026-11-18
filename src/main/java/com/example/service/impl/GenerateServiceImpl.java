package com.example.service.impl;

import com.example.service.GenerateService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.config.*;
import org.mybatis.generator.exception.InvalidConfigurationException;
import org.mybatis.generator.internal.DefaultShellCallback;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class GenerateServiceImpl implements GenerateService {

    @Resource
    private DataSourceProperties dataSourceProperties;

    public static String toCamelCase(String underlineStr) {
        if (underlineStr == null || underlineStr.isEmpty()) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        boolean nextUpper = false;

        for (int i = 0; i < underlineStr.length(); i++) {
            char c = underlineStr.charAt(i);

            if (c == '_') {
                nextUpper = true;
            } else if (nextUpper) {
                result.append(Character.toUpperCase(c));
                nextUpper = false;
            } else {
                result.append(i == 0 ? Character.toUpperCase(c) : Character.toLowerCase(c));
            }
        }

        return result.toString();
    }

    @Override
    public void generate(List<String> tableNames) {
        Configuration config = new Configuration();

        JDBCConnectionConfiguration jdbcConfig = new JDBCConnectionConfiguration.Builder()
                .withDriverClass(dataSourceProperties.getDriverClassName())
                .withConnectionURL(dataSourceProperties.getUrl())
                .withUserId(dataSourceProperties.getUsername())
                .withPassword(dataSourceProperties.getPassword())
                .withProperties(Map.of("useInformationSchema", true))
                .build();

        ModelGeneratorConfiguration modelGeneratorConfiguration = new ModelGeneratorConfiguration.Builder()
                .withTargetPackage("com.example.model")
                .withTargetProject("src/main/java")
                .build();

        SqlMapGeneratorConfiguration sqlMapGeneratorConfiguration = new SqlMapGeneratorConfiguration.Builder()
                .withTargetPackage("mapper")
                .withTargetProject("src/main/resources")
                .build();

        CommentGeneratorConfiguration commentGeneratorConfiguration = new CommentGeneratorConfiguration.Builder()
                .withProperties(Map.of("suppressAllComments", "true",
                        "suppressGeneratedAnnotation", "true",
                        "suppressDate", "true"))
                .build();

        ClientGeneratorConfiguration clientGeneratorConfiguration = new ClientGeneratorConfiguration.Builder()
                .withTargetPackage("com.example.dao")
                .withTargetProject("src/main/java")
                .withLegacyClientType(ClientGeneratorConfiguration.LegacyClientType.XML_MAPPER)
                .build();

        JavaTypeResolverConfiguration javaTypeResolverConfiguration = new JavaTypeResolverConfiguration.Builder()
                .withProperties(Map.of("forceBigDecimals", "false"))
                .build();

        PluginConfiguration pluginConfiguration1 = new PluginConfiguration.Builder()
                .withConfigurationType("org.mybatis.generator.plugins.SerializablePlugin")
                .build();

        PluginConfiguration pluginConfiguration2 = new PluginConfiguration.Builder()
                .withConfigurationType("com.example.plugin.MyBatisPlugin")
                .withProperties(Map.of("hasLombok", "true"))
                .build();

        Context.Builder contextBuilder = new Context.Builder()
                .withDefaultModelType(ModelType.CONDITIONAL)
                .withId("postgresContext")
                .withTargetRuntime("MyBatis3")
                .withProperties(Map.of("javaFileEncoding", "UTF-8"))
                .withJdbcConnectionConfiguration(jdbcConfig)
                .withModelGeneratorConfiguration(modelGeneratorConfiguration)
                .withSqlMapGeneratorConfiguration(sqlMapGeneratorConfiguration)
                .withCommentGeneratorConfiguration(commentGeneratorConfiguration)
                .withClientGeneratorConfiguration(clientGeneratorConfiguration)
                .withJavaTypeResolverConfiguration(javaTypeResolverConfiguration)
                .withPluginConfiguration(pluginConfiguration1)
                .withPluginConfiguration(pluginConfiguration2);

        for (String tableName : tableNames) {
            TableConfiguration tableConfiguration = new TableConfiguration.Builder()
                    .withTableName(tableName)
                    .withDomainObjectName(toCamelCase(tableName))
                    .withDelimitIdentifiers(true)
                    .withAllColumnDelimitingEnabled(true)
                    .withCountByExampleStatementEnabled(false)
                    .withSelectByExampleStatementEnabled(false)
                    .withInsertStatementEnabled(true)
                    .withUpdateByExampleStatementEnabled(false)
                    .withDeleteByExampleStatementEnabled(false)
                    .build();
            contextBuilder.withTableConfiguration(tableConfiguration);
        }

        Context context = contextBuilder.build();

        config.addContext(context);

        DefaultShellCallback callback = new DefaultShellCallback();
        MyBatisGenerator myBatisGenerator = new MyBatisGenerator.Builder()
                .withConfiguration(config)
                .withShellCallback(callback)
                .build();
        try {
            List<String> warnings = myBatisGenerator.generateAndWrite();
            // 打印警告信息
            for (String warning : warnings) {
                System.out.println("Warning: " + warning);
            }
            System.out.println("代码生成成功！");
        } catch (SQLException | IOException | InterruptedException | InvalidConfigurationException e) {
            log.error("generate error", e);
        }
    }
}
