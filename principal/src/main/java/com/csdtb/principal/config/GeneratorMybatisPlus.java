package com.csdtb.principal.config;


import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.exceptions.MybatisPlusException;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.InjectionConfig;
import com.baomidou.mybatisplus.generator.config.*;
import com.baomidou.mybatisplus.generator.config.builder.ConfigBuilder;
import com.baomidou.mybatisplus.generator.config.po.TableInfo;
import com.baomidou.mybatisplus.generator.config.rules.FileType;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * @Author zhoujiacheng
 * @Date 2022-10-31
 * @Description 自动生成模板配置
 **/
public class GeneratorMybatisPlus {

    private static String DRIVER_CLASS_NAME = "com.mysql.cj.jdbc.Driver";
    private static String URL = "jdbc:mysql://192.168.110.128:3306/control-simulation-dual-task-back?useUnicode=true&characterEncoding=utf-8&useSSL=false&allowMultiQueries=true";
    private static String USERNAME = "root";
    private static String PASSWORD = "Sdbc1234@1";

    /**
     * 父包名路径(文件输出路径,也是导包的路径)
     */
    private static String PARENT_PACKAGE_PATH = "/com/csdtb/";
    // 各层路径
    private static String ENTITY_PATH = "database/entity/";
    private static String MAPPER_PATH = "database/mapper/";
    private static String SERVICE_PATH = "principal/service/";
    private static String SERVICE_IMPL_PATH = "principal/service/impl/";
    private static String CONTROLLER_PATH = "principal/controller/";

    /**
     * entity输出模板
     */
    private static String ENTITY_TEMPLATE = "templates/entity.java.ftl";
    private static String ENTITY_OUTPUT_PATH = "/database" + "/src/main/java" + PARENT_PACKAGE_PATH + ENTITY_PATH;
    /**
     * mapper.xml输出模板
     */
    private static String XML_TEMPLATE = "templates/mapper.xml.ftl";
    private static String XML_OUTPUT_PATH = "/database" + "/src/main/resources/mapper/";
    /**
     * mapper.java输出模板
     */
    private static String MAPPER_TEMPLATE = "templates/mapper.java.ftl";
    private static String MAPPER_OUTPUT_PATH = "/database" + "/src/main/java" + PARENT_PACKAGE_PATH + MAPPER_PATH;
    /**
     * service输出模板
     */
    private static String SERVICE_TEMPLATE = "templates/service.java.ftl";
    private static String SERVICE_OUTPUT_PATH = "/principal" + "/src/main/java" + PARENT_PACKAGE_PATH + SERVICE_PATH;
    /**
     * serviceImpl输出模板
     */
    private static String SERVICE_IMPL_TEMPLATE = "templates/serviceImpl.java.ftl";
    private static String SERVICE_IMPL_OUTPUT_PATH = "/principal" + "/src/main/java" + PARENT_PACKAGE_PATH + SERVICE_IMPL_PATH;
    /**
     * controller输出模板
     */
    private static String CONTROLLER_TEMPLATE = "templates/controller.java.ftl";
    private static String CONTROLLER_OUTPUT_PATH = "/principal" + "/src/main/java" + PARENT_PACKAGE_PATH + CONTROLLER_PATH;

    public static void main(String[] args) {

        //全局配置
        GlobalConfig globalConfig = globalConfig();
        //数据库配置
        DataSourceConfig dataSourceConfig = dataSourceConfig();
        //策略配置
        StrategyConfig strategyConfig = strategyConfig();
        // 模板配置
        TemplateConfig templateConfig = templateConfig();
        // 自定义配置
        InjectionConfig injectionConfig = injectionConfig();
        // 包配置
        PackageConfig packageConfig = packageConfig();

        new AutoGenerator().setGlobalConfig(globalConfig)
                .setDataSource(dataSourceConfig)
                .setStrategy(strategyConfig)
                .setPackageInfo(packageConfig)
                .setTemplate(templateConfig)
                .setTemplateEngine(new FreemarkerTemplateEngine())
                .setCfg(injectionConfig)
                .execute();
    }

    private static InjectionConfig injectionConfig() {
        return new InjectionConfig() {
            @Override
            public void initMap() {
                //注入配置
            }
        }
                //判断是否创建文件
                .setFileCreate(new IFileCreate() {
                    @Override
                    public boolean isCreate(ConfigBuilder configBuilder, FileType fileType, String filePath) {
                        //检查文件目录，不存在自动递归创建
                        checkDir(filePath);

                        // 指定需要覆盖的文件
                        // 文件结尾名字参照 全局配置 中对各层文件的命名,未修改为默认值
                        if (isExists(filePath) && !filePath.endsWith("Mapper.xml") && !filePath.endsWith("Entity.java")) {
                            return false;
                        }
                        return true;
                    }
                })
                //自定义输入文件
                .setFileOutConfigList(fileOutConfigList());
    }

    private static List<FileOutConfig> fileOutConfigList() {
        List<FileOutConfig> list = new ArrayList<>();
        //当前项目路径
        String PROJECT_PATH = System.getProperty("user.dir");

        //实体类文件输出
        list.add(new FileOutConfig(ENTITY_TEMPLATE) {
            @Override
            public String outputFile(TableInfo tableInfo) {
                return PROJECT_PATH + ENTITY_OUTPUT_PATH + tableInfo.getEntityName() + "Entity" + StringPool.DOT_JAVA;
            }
        });
        // mapper xml文件输出
        list.add(new FileOutConfig(XML_TEMPLATE) {
            @Override
            public String outputFile(TableInfo tableInfo) {
                return PROJECT_PATH + XML_OUTPUT_PATH + tableInfo.getMapperName() + StringPool.DOT_XML;
            }
        });
        // mapper文件输出
        list.add(new FileOutConfig(MAPPER_TEMPLATE) {
            @Override
            public String outputFile(TableInfo tableInfo) {
                return PROJECT_PATH + MAPPER_OUTPUT_PATH + tableInfo.getMapperName() + StringPool.DOT_JAVA;
            }
        });
        // service文件输出
        list.add(new FileOutConfig(SERVICE_TEMPLATE) {
            @Override
            public String outputFile(TableInfo tableInfo) {
                return PROJECT_PATH + SERVICE_OUTPUT_PATH + (tableInfo.getServiceName().startsWith("I") ? tableInfo.getServiceName().substring(1) : tableInfo.getServiceName()) + StringPool.DOT_JAVA;
            }
        });
        // service impl文件输出
        list.add(new FileOutConfig(SERVICE_IMPL_TEMPLATE) {
            @Override
            public String outputFile(TableInfo tableInfo) {
                return PROJECT_PATH + SERVICE_IMPL_OUTPUT_PATH + tableInfo.getServiceImplName() + StringPool.DOT_JAVA;
            }
        });
        // controller文件输出
        list.add(new FileOutConfig(CONTROLLER_TEMPLATE) {
            @Override
            public String outputFile(TableInfo tableInfo) {
                return PROJECT_PATH + CONTROLLER_OUTPUT_PATH + tableInfo.getControllerName() + StringPool.DOT_JAVA;
            }
        });

        return list;
    }

    private static boolean isExists(String path) {
        File file = new File(path);
        return file.exists();
    }

    private static TemplateConfig templateConfig() {
        return new TemplateConfig()
                // 置空后方便使用自定义输出位置
                .setEntity(null)
                .setXml(null)
                .setMapper(null)
                .setService(null)
                .setServiceImpl(null)
                .setController(null);
    }

    private static PackageConfig packageConfig() {
        return new PackageConfig()
                .setParent("com.csdtb")
                .setEntity("database.entity")
                .setMapper("database.mapper")
                .setService("principal.service")
                .setServiceImpl("principal.service.impl")
                .setController("principal.controller")
                .setXml("resources.mapper");
    }

    private static StrategyConfig strategyConfig() {
        return new StrategyConfig()
                //表名生成策略：下划线连转驼峰
                .setNaming(NamingStrategy.underline_to_camel)
                //表字段生成策略：下划线连转驼峰
                .setColumnNaming(NamingStrategy.underline_to_camel)
                //需要生成的表
                .setInclude(scanner("表名，多个英文逗号分割").split(","))
                //生成RestController
                .setRestControllerStyle(true)
//                //去除表前缀
                .setTablePrefix("tb_")
                // controller映射地址：驼峰转连字符
                .setControllerMappingHyphenStyle(true)
                // 是否为lombok模型; 需要lombok依赖
                .setEntityLombokModel(true)
                // 生成实体类字段注解
                .setEntityTableFieldAnnotationEnable(true);
    }

    private static DataSourceConfig dataSourceConfig() {
        // 数据源配置
        DataSourceConfig dataSourceConfig = new DataSourceConfig();
        dataSourceConfig.setDbType(DbType.MYSQL);
        dataSourceConfig.setUrl(URL);
        dataSourceConfig.setDriverName(DRIVER_CLASS_NAME);
        dataSourceConfig.setUsername(USERNAME);
        dataSourceConfig.setPassword(PASSWORD);
        return dataSourceConfig;
    }

    private static GlobalConfig globalConfig() {
        return new GlobalConfig()
                // 打开文件
                .setOpen(false)
                // 文件覆盖
                .setFileOverride(true)
                // 开启activeRecord模式
                .setActiveRecord(true)
                // XML ResultMap: mapper.xml生成查询映射结果
                .setBaseResultMap(true)
                // XML ColumnList: mapper.xml生成查询结果列
                .setBaseColumnList(true)
                // swagger注解; 须添加swagger依赖
                .setSwagger2(false)
                // 作者
                .setAuthor(scanner("作者名称"));
    }

    public static String scanner(String tip) {
        Scanner scanner = new Scanner(System.in);
        StringBuilder help = new StringBuilder();
        help.append("请输入" + tip + "：");
        System.out.println(help.toString());
        if (scanner.hasNext()) {
            String ipt = scanner.next();
            if (StringUtils.isNotBlank(ipt)) {
                return ipt;
            }
        }
        throw new MybatisPlusException("请输入正确的" + tip + "！");
    }
}
