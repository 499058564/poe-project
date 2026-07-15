package com.poe.provider;

import com.poe.provider.tool.SeedDbBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author yuziyang
 * @since 2026/7/15
 **/
public class SeedDbBuilderMain {
    private static final Logger log = LoggerFactory.getLogger(SeedDbBuilderMain.class);

    private static final String DEFAULT_OUTPUT_PATH = "data-provider/src/main/resources/seed.db";
    public static void main(String[] args) {
        try {
            //TODO yzy 数据同步
            String[] runArgs = {"--output", DEFAULT_OUTPUT_PATH};
            SeedDbBuilder builder = SeedDbBuilder.parse(runArgs);
            builder.build();
        } catch (Exception e) {
            log.error("SeedDbBuilder failed: {}", e.getMessage(), e);
            System.exit(1);
        }
    }
}
