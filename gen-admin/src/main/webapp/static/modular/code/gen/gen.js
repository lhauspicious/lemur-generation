var GEN = {
    dbbtn: '<button type="button" class="list-group-item" value="dbId" onclick="GEN.getDataBases(\'dbId\')">dbName</button>',
    tablesBtn: '<button type="button" class="list-group-item" value="tableName" onclick="GEN.selectTables(\'tableName\',\'chinaName\')">tableName-chinaName</button>',
    databasebtn: '<button type="button" class="list-group-item" value="name" onclick="GEN.getTables(\'name\')">name</button>',
    templatebtn: '<button type="button" class="list-group-item" value="id" onclick="GEN.selectTemplates(\'id\')">templateName</button>',
    param: {},
    validateFields: {
        className: {
            validators: {
                notEmpty: {
                    message: '类名不能为空'
                },
                stringLength: {/*长度提示*/
                    min: 3,
                    max: 20,
                    message: '类名长度必须在3到20之间'
                }
            }
        },
        name: {
            validators: {
                notEmpty: {
                    message: '功能不能为空'
                },
                stringLength: {/*长度提示*/
                    min: 3,
                    max: 20,
                    message: '功能长度必须在3到20之间'
                }
            }
        },
        author: {
            validators: {
                notEmpty: {
                    message: '作者不能为空'
                },
                stringLength: {/*长度提示*/
                    min: 3,
                    max: 20,
                    message: '用户名长度必须在3到20之间'
                }
            }
        },
        codePackage: {
            validators: {
                notEmpty: {
                    message: '代码目录不能为空'
                },
                regexp: {/* 只需加此键值对，包含正则表达式，和提示 */
                    regexp: /^[a-zA-Z0-9_\-\.]+$/,
                    message: '只能是数字和字母和-_.'
                },
                stringLength: {/*长度提示*/
                    min: 1,
                    max: 100,
                    message: '代码目录长度必须在1到100之间'
                }
            }
        },
        htmlPackage: {
            validators: {
                regexp: {/* 只需加此键值对，包含正则表达式，和提示 */
                    regexp: /^[a-zA-Z0-9_\-\.]+$/,
                    message: '只能是数字和字母和-_.'
                },
                stringLength: {/*长度提示*/
                    min: 1,
                    max: 100,
                    message: 'Html目录长度必须在1到100之间'
                }
            }
        },
        jsPackage: {
            validators: {
                regexp: {/* 只需加此键值对，包含正则表达式，和提示 */
                    regexp: /^[a-zA-Z0-9_\-\.]+$/,
                    message: '只能是数字和字母和-_.'
                },
                stringLength: {/*长度提示*/
                    min: 1,
                    max: 100,
                    message: 'Js目录长度必须在1到100之间'
                }
            }
        }
    }
}
// 查询按钮
GEN.getData = function (url, p) {
    var success = function (data) {
        if ('dbconnects' == p) {
            var html = '';
            $(data.rows).each(function (i, d) {
                html +=
                    GEN.dbbtn.replace("dbName", d.alias).replace("dbId", d.id)
                        .replace("dbId", d.id);
            });
            $("#dblinklist").html(html);
        } else if ('databases' == p) {
            var html = '';
            $(data.result).each(function (i, d) {
                html +=
                    GEN.databasebtn.replace("name", d.name).replace("name", d.name)
                        .replace("name", d.name);
            });
            $("#databaseslist").html(html);
        } else if ('tables' == p) {
            var html = '';
            $(data.result).each(function (i, d) {
                html +=
                    GEN.tablesBtn.replace("tableName", d.tableName)
                        .replace("tableName", d.tableName)
                        .replace("tableName", d.tableName)
                        .replace("chinaName", d.chinaName)
                        .replace("chinaName", d.chinaName);
            });
            $("#tableslist").html(html);
        } else if ('templates' == p) {
            var html = '';
            $(data.rows).each(function (i, d) {
                html += GEN.templatebtn.replace("id", d.id).replace("id", d.id)
                    .replace("templateName", d.templateName);
            });
            $("#templatesList").html(html);
        }
    };
    var ajax = new $ax(Feng.ctxPath + url, success);
    ajax.setData(GEN.param);
    ajax.start();
};

GEN.queryData = function () {
    GEN.getData();
};

GEN.getDataBases = function (id) {
    GEN.param.id = id;
    GEN.selectChange(id, 'dblinklist');
    $("#databaseslist").html('');
    $("#tableslist").html('');
    GEN.getData('/code/queryDatabses', 'databases');
}

GEN.getTables = function (name) {
    GEN.param.dbName = name;
    GEN.selectChange(name, 'databaseslist');
    $("#tableslist").html('');
    GEN.getData('/code/queryTables', 'tables');
}

GEN.selectTables = function (tableName,chinaName) {
    GEN.param.tableName = tableName;
    GEN.selectChange(tableName, 'tableslist');

    $("#className").val(tableName);
    $("#name").val(chinaName);
}
GEN.selectTemplates = function (id) {
    if (!GEN.param.templates) {
        GEN.param.templates = [];
    }
    if ($.inArray(id, GEN.param.templates) > -1) {
        GEN.param.templates.splice($.inArray(id, GEN.param.templates), 1);
        $("#templatesList").find("button").each(function (i, d) {
            if ($(this).val() == id) {
                $(this).removeClass('active');
            }
        });
    } else {
        $("#templatesList").find("button").each(function (i, d) {
            if ($(this).val() == id) {
                $(this).addClass('active');
            }
        });
        GEN.param.templates.push(id);
    }

}

/**
 * 验证数据
 */
GEN.validate = function () {
    $('#genForm').data("bootstrapValidator").resetForm();
    $('#genForm').bootstrapValidator('validate');
    return $("#genForm").data('bootstrapValidator').isValid();
};

GEN.selectChange = function (data, eleName) {
    $("#" + eleName).find("button").each(function (i, d) {
        if ($(this).val() == data) {
            $(this).addClass('active');
        } else {
            $(this).removeClass('active');
        }
    });
}

GEN.genCode = function () {
    if (!this.validate()) {
        return;
    }
    GEN.param.author = $("#author").val();
    GEN.param.codePackage = $("#codePackage").val();
    GEN.param.htmlPackage = $("#htmlPackage").val();
    GEN.param.jsPackage = $("#jsPackage").val();
    GEN.param.className = $("#className").val();
    GEN.param.name = $("#name").val();
    GEN.param.encoded = $("#encoded").val();
    window.location.href = '/code/genCode?' + $.param(GEN.param);
}

// 页面初始化
$(function () {
    GEN.param.limit = 100;
    GEN.param.offset = 0;
    GEN.getData('/dbinfo/queryAll', 'dbconnects');
    GEN.getData('/template/list', 'templates');
    Feng.initValidator("genForm", GEN.validateFields);
});

$("#params").change(function(){
    var ajax = new $ax(Feng.ctxPath + 'genparam/detail', function (data) {
        $("#author").val(data.author);
        $("#codePackage").val(data.codePackage);
        $("#htmlPackage").val(data.htmlPackage);
        $("#jsPackage").val(data.jsPackage);
    });
    ajax.set('id',$("#params").val());
    ajax.start();
});

$("#groupId").change(function(){
    GEN.getData('/template/list?groupId='+$("#groupId").val(), 'templates');
});