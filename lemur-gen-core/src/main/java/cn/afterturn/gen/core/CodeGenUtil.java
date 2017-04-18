/**
 * Copyright 2013-2017 JueYue (qrb.jueyue@gmail.com)
 *   
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.afterturn.gen.core;

import java.util.List;

import cn.afterturn.gen.core.db.read.ReadTableFactory;
import cn.afterturn.gen.core.parse.ParseFactory;
import cn.afterturn.gen.core.util.ConnectionUtil;
import cn.afterturn.gen.model.base.GenBeanEntity;

/**
 * 
 * @author JueYue
 * 2017年4月18日
 */
public class CodeGenUtil {

    /**
     * 生产代码
     * @param model
     * @return
     */
    public static List<String> codeGen(CodeGenModel model) {
        //Step 1 初始化数据库连接
        ConnectionUtil.init(model.getDbType(), model.getUrl(), model.getUsername(), model.getPasswd());
        //Step 2 读取数据源列表
        GenBeanEntity tableEntity = ReadTableFactory.getReadTable(model.getDbType()).read(model.getDbName(),
            model.getTableName());
        //Step 3 解析模板,生产代码
        return ParseFactory.getParse(model.getParseType()).parse(model.getGenerationEntity(), tableEntity,
            model.getFileList());
    }

}
