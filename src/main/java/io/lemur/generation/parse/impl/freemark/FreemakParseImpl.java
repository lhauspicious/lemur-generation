package io.lemur.generation.parse.impl.freemark;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import freemarker.template.Configuration;
import freemarker.template.Template;
import io.lemur.generation.entity.enmu.TypeEnmu;
import io.lemur.generation.entity.field.DataBaseFieldEntity;
import io.lemur.generation.entity.generation.GenerationEntity;
import io.lemur.generation.entity.table.DataBaseTableEntity;
import io.lemur.generation.parse.IParse;
import io.lemur.generation.util.CodeFromatter;
import io.lemur.generation.util.PropertiesUtil;

/**
 * FREEMARK 解析
 * @author JueYue
 * @date 2014年12月25日
 */
public class FreemakParseImpl implements IParse {

    private static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public Configuration getConfiguration() throws Exception {
        Configuration cfg = new Configuration();
        cfg.setDirectoryForTemplateLoading(new File(FreemakParseImpl.class.getResource("/")
            .getFile() + "/ftl"));
        cfg.setLocale(Locale.CHINA);
        cfg.setDefaultEncoding("UTF-8");
        return cfg;
    }

    @Override
    public void parse(GenerationEntity generationEntity, DataBaseTableEntity tableEntity) {

        Map<String, Object> rootMap = new HashMap<String, Object>();
        rootMap.put("params", generationEntity);
        rootMap.put("table", tableEntity);
        rootMap.put("isNeedDate", judgeNeedDate(tableEntity.getFields()));
        rootMap.put("createTime", format.format(new Date()));
        if (StringUtils.isEmpty(generationEntity.getEntityName())) {
            generationEntity.setEntityName(tableEntity.getName());
        }
        if (StringUtils.isEmpty(generationEntity.getPackageName())) {
            generationEntity.setPackageName(tableEntity.getName());
        }
        for (int i = 0; i < generationEntity.getTypes().length; i++) {
            try {
                //创建文件目录
                String pathOfSrc = getSrcPath(generationEntity, tableEntity,
                    generationEntity.getTypes()[i]);
                File file = new File(pathOfSrc);
                if (!file.exists()) {
                    file.mkdirs();
                }
                // 模板解析
                Template template = getConfiguration().getTemplate(
                    generationEntity.getTypes()[i].getValue());
                Writer outWriter = new OutputStreamWriter(new FileOutputStream(
                    pathOfSrc
                            + String.format(generationEntity.getTypes()[i].getSuffix(),
                                generationEntity.getEntityName())), "UTF-8");
                Writer out = new StringWriter();
                template.process(rootMap, out);
                //格式化文件
                String sourceCode = CodeFromatter.format(out.toString());
                //输出
                outWriter.write(sourceCode);
                outWriter.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String getSrcPath(GenerationEntity generationEntity, DataBaseTableEntity tableEntity,
                              TypeEnmu type) {
        return PropertiesUtil.getValue(PropertiesUtil.GENERATION_PATH)
               + (generationEntity.getJavaPackage() + type.getSrcPackage()).replace(".", "/") + "/"
               + generationEntity.getPackageName();
    }

    /**
     * 判断是否存在Date类型
     * @param fields
     * @return
     */
    private boolean judgeNeedDate(List<DataBaseFieldEntity> fields) {
        for (int i = 0; i < fields.size(); i++) {
            if (fields.get(i).getType().equals("Date")) {
                return true;
            }
        }
        return false;
    }

}
