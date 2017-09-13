/**
 * Copyright 2013-2017 JueYue (qrb.jueyue@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package cn.afterturn.gen.modular.code.controller;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.afterturn.gen.core.CodeGenModel;
import cn.afterturn.gen.core.CodeGenUtil;
import cn.afterturn.gen.core.GenCoreConstant;
import cn.afterturn.gen.core.db.read.IReadTable;
import cn.afterturn.gen.core.db.read.ReadTableFactory;
import cn.afterturn.gen.core.model.BaseModel;
import cn.afterturn.gen.core.model.GenerationEntity;
import cn.afterturn.gen.core.model.RequestModel;
import cn.afterturn.gen.core.model.ResponseModel;
import cn.afterturn.gen.core.shiro.ShiroKit;
import cn.afterturn.gen.core.util.ConnectionUtil;
import cn.afterturn.gen.core.util.NameUtil;
import cn.afterturn.gen.modular.code.model.DbInfoModel;
import cn.afterturn.gen.modular.code.model.GenParamModel;
import cn.afterturn.gen.modular.code.model.TemplateGroupModel;
import cn.afterturn.gen.modular.code.model.TemplateModel;
import cn.afterturn.gen.modular.code.service.IDbInfoService;
import cn.afterturn.gen.modular.code.service.IGenParamService;
import cn.afterturn.gen.modular.code.service.IGenService;
import cn.afterturn.gen.modular.code.service.ITemplateGroupService;
import cn.afterturn.gen.modular.code.service.ITemplateService;

/**
 * @author JueYue 2017年4月22日
 */
@Controller
@RequestMapping("code")
public class GenController {

    private static final Logger LOGGER = LoggerFactory.getLogger(GenController.class);

    private String PREFIX = "/code/gen/";

    @Autowired
    private IGenService genService;
    @Autowired
    private ITemplateService templateService;
    @Autowired
    private IDbInfoService dbInfoService;
    @Autowired
    private ITemplateGroupService templateGroupService;
    @Autowired
    private IGenParamService genParamService;

    /**
     * 跳转到首页
     */
    @RequestMapping("")
    public String index(Model modelMap) {
        TemplateGroupModel model = new TemplateGroupModel();
        model.setUserId(ShiroKit.getUser().getId());
        modelMap.addAttribute("groups", templateGroupService.selectList(model));
        GenParamModel params = new GenParamModel();
        model.setUserId(ShiroKit.getUser().getId());
        modelMap.addAttribute("params", genParamService.selectList(params));
        return PREFIX + "index.html";
    }

    @RequestMapping(value = "queryDatabses")
    @ResponseBody
    public ResponseModel queryDatabses(DbInfoModel entity, RequestModel form) {
        try {
            entity = dbInfoService.selectOne(entity);
            ConnectionUtil.init(entity.getDbDriver(), entity.getDbUrl(), entity.getDbUserName(),
                    entity.getDbPassword());
            IReadTable readTable = ReadTableFactory.getReadTable(entity.getDbType());
            List<String> list = readTable.getAllDB();
            List<BaseModel> dblist = new ArrayList<BaseModel>();
            BaseModel info;
            for (String db : list) {
                info = new BaseModel(db);
                dblist.add(info);
            }
            return ResponseModel.ins(dblist);
        } finally {
            ConnectionUtil.close();
        }
    }

    @RequestMapping(value = "queryTables")
    @ResponseBody
    public ResponseModel queryTables(DbInfoModel entity, String dbName, RequestModel form) {
        try {
            entity = dbInfoService.selectOne(entity);
            ConnectionUtil.init(entity.getDbDriver(), entity.getDbUrl(), entity.getDbUserName(),
                    entity.getDbPassword());
            IReadTable readTable = ReadTableFactory.getReadTable(entity.getDbType());
            return ResponseModel.ins(readTable.getAllTable(dbName));
        } finally {
            ConnectionUtil.close();
        }
    }

    @RequestMapping(value = "genCode")
    public void genCode(DbInfoModel entity, String dbName, String tableName, GenerationEntity ge,
                        HttpServletRequest req, HttpServletResponse res) {
        entity = dbInfoService.selectOne(entity);
        String[] templates = req.getParameterValues("templates[]");
        CodeGenModel model = new CodeGenModel();
        model.setDbType(GenCoreConstant.MYSQL);
        model.setTableName(tableName);
        model.setDbName(dbName);
        model.setUrl(entity.getDbUrl());
        model.setPasswd(entity.getDbPassword());
        model.setUsername(entity.getDbUserName());
        if (StringUtils.isEmpty(ge.getEntityName())) {
            ge.setEntityName(NameUtil.getEntityHumpName(dbName));
        }
        model.setGenerationEntity(ge);
        List<TemplateModel> templateList = templateService.getTemplateByIds(templates);
        List<String> templateFileList = genService.loadTemplateFile(templateList);
        List<String> fileList = new ArrayList<>();
        for (int i = 0; i < templateList.size(); i++) {
            model.setParseType(templateList.get(i).getTemplateType());
            model.setFile(templateFileList.get(i));
            fileList.addAll(CodeGenUtil.codeGen(model));
        }
        downThisFileList(res, fileList, templateList, ge);
    }

    private void downThisFileList(HttpServletResponse res, List<String> fileList, List<TemplateModel> templateList, GenerationEntity ge) {
        ZipOutputStream out = null;
        try {
            out = new ZipOutputStream(res.getOutputStream());
            for (int i = 0; i < fileList.size(); i++) {
                if (templateList.get(i).getFileName().endsWith("js") || templateList.get(i).getFileName().endsWith("html")) {
                    out.putNextEntry(new ZipEntry(String.format(templateList.get(i).getFileName(), ge.getEntityName().toLowerCase())));
                } else {
                    out.putNextEntry(new ZipEntry(String.format(templateList.get(i).getFileName(), ge.getEntityName())));
                }
                out.write(fileList.get(i).getBytes(), 0, fileList.get(i).getBytes().length);
                out.closeEntry();
            }
            res.setContentType("application/octet-stream");
            res.setHeader("Content-Disposition", "attachment;filename=" + "code.zip");
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            try {
                out.close();
                res.getOutputStream().flush();
                res.getOutputStream().close();
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }


}
