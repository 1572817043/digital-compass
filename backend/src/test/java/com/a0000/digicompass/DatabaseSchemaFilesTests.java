package com.a0000.digicompass;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class DatabaseSchemaFilesTests {

    @Test
    void schemaSqlContainsCoreTables() throws Exception {
        String schema = Files.readString(Path.of("src/main/resources/db/schema.sql"));

        assertThat(schema)
                .contains("CREATE TABLE IF NOT EXISTS dc_category")
                .contains("CREATE TABLE IF NOT EXISTS dc_brand")
                .contains("CREATE TABLE IF NOT EXISTS dc_product")
                .contains("brand_id BIGINT UNSIGNED NULL")
                .contains("CREATE TABLE IF NOT EXISTS dc_product_image")
                .contains("CREATE TABLE IF NOT EXISTS dc_product_spec")
                .contains("CREATE TABLE IF NOT EXISTS dc_price_reference")
                .contains("CREATE TABLE IF NOT EXISTS dc_purchase_link")
                .contains("CREATE TABLE IF NOT EXISTS dc_product_tag")
                .contains("CREATE TABLE IF NOT EXISTS dc_product_metric")
                .contains("CREATE TABLE IF NOT EXISTS dc_ai_knowledge")
                .contains("CREATE TABLE IF NOT EXISTS dc_ai_knowledge_chunk")
                .contains("CREATE TABLE IF NOT EXISTS dc_ai_knowledge_embedding")
                .contains("CREATE TABLE IF NOT EXISTS dc_ai_provider_config")
                .contains("CREATE TABLE IF NOT EXISTS dc_ai_workflow_log")
                .contains("CREATE TABLE IF NOT EXISTS dc_user")
                .contains("CREATE TABLE IF NOT EXISTS dc_user_favorite")
                .contains("CREATE TABLE IF NOT EXISTS dc_user_view_history")
                .contains("CREATE TABLE IF NOT EXISTS dc_price_alert")
                .contains("CREATE TABLE IF NOT EXISTS dc_user_preference");
    }

    @Test
    void applicationYmlContainsMysqlDatasource() throws Exception {
        String yml = Files.readString(Path.of("src/main/resources/application.yml"));

        assertThat(yml)
                .contains("jdbc:mysql://localhost:3306/digital_compass")
                .contains("username: root")
                .contains("password: 123456");
    }
}
