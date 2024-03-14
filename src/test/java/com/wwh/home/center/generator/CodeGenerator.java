package com.wwh.home.center.generator;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * <pre>
 * 代码生成
 * 参考：
 * https://www.baomidou.com/pages/981406/#%E5%8F%AF%E9%80%89%E9%85%8D%E7%BD%AE
 * </pre>
 *
 * @author wangwh
 * @date 2023/05/09
 */
public class CodeGenerator {
    public static void main(String[] args) {
        FastAutoGenerator.create("jdbc:mysql://localhost:3306/home_center?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf8&useSSL=false",
                "home", "123456")
//        FastAutoGenerator.create("jdbc:mysql://localhost:3306/sunshine?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai",
//                "example-user", "change-me")
                // 全局配置
                .globalConfig(builder -> builder.author("wangwh").enableSwagger().outputDir("D:\\temp\\MybatisPlus").fileOverride())
                //.globalConfig((scanner, builder) -> builder.author(scanner.apply("请输入作者名称？")).fileOverride())
                // 包配置
                //.packageConfig((scanner, builder) -> builder.parent(scanner.apply("请输入包名？")))
                .packageConfig(builder -> builder.parent("com.wwh.home.center")
                        .entity("model.entity")
                        .mapper("dao.mapper")
                        .xml("xml")
                )
                // 策略配置
                .strategyConfig((scanner, builder) -> builder.addInclude(getTables(scanner.apply("请输入表名，多个英文逗号分隔？所有输入 all")))
                        .controllerBuilder().enableRestStyle().enableHyphenStyle()
                        .entityBuilder().enableLombok()
                        //.addTableFills(new Column("create_time", FieldFill.INSERT))

                        .mapperBuilder().enableBaseResultMap().enableBaseColumnList()
                        .build())
                /*
                    模板引擎配置，默认 Velocity 可选模板引擎 Beetl 或 Freemarker
                   .templateEngine(new BeetlTemplateEngine())
                   .templateEngine(new FreemarkerTemplateEngine())
                 */
                .execute();

    }

    // 处理 all 情况
    protected static List<String> getTables(String tables) {
        return "all".equals(tables) ? Collections.emptyList() : Arrays.asList(tables.split(","));
    }

}
