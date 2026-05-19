package org.myfx.controls.aione.AiService.common.Interceptor;

import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.plugin.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Intercepts({
        @Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})
})
@Component
public class SqlBackupInterceptor implements Interceptor {

    @Value("${spring.application.name:unknown-service}")
    private String serviceName;

    // 备份根目录
    private static final String BACKUP_DIR = "src/main/resources/sql_backup/";

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
        BoundSql boundSql = statementHandler.getBoundSql();
        String sql = boundSql.getSql();

        // 仅记录增删改操作（排除查询，否则文件会爆炸）
        String upperSql = sql.trim().toUpperCase();
        if (upperSql.startsWith("INSERT") || upperSql.startsWith("UPDATE") || upperSql.startsWith("DELETE")) {
            saveSqlToFile(sql);
        }

        return invocation.proceed();
    }

    private synchronized void saveSqlToFile(String sql) {
        // 1. 整理 SQL 格式（去掉多余换行）
        String formattedSql = sql.replaceAll("\\s+", " ").trim() + ";";
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // 2. 确定文件名：sql_backup/user-service-backup.sql
        String fileName = serviceName + "-backup.sql";
        File dir = new File(BACKUP_DIR);
        if (!dir.exists()) dir.mkdirs();

        File file = new File(dir, fileName);

        // 3. 写入文件（追加模式）
        try (FileWriter writer = new FileWriter(file, true)) {
            writer.write("-- [" + time + "]\n");
            writer.write(formattedSql + "\n\n");
        } catch (IOException e) {
            System.err.println("SQL 备份失败: " + e.getMessage());
        }
    }
}