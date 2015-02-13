
<!doctype html>

<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="/struts-tags" prefix="s" %>
<%@ page isELIgnored="false" %>

<head>
    <jsp:include page="header.jsp" />
    <link rel="stylesheet" href="style/cupertino/jquery-ui-1.8.18.custom.css" type='text/css' media='all' />
    <link rel="stylesheet" href="style/chosen.css" />
    <%--<link rel="stylesheet" href="style/version01.css" />--%>
    <style>
        tr.spacer > td {
            padding-top: 1em;
        }
        ul {
            margin: 0 !important;
            padding: 0 !important;
        }
    </style>
</head>

<body class="smart-style-2">
<div id="container">

    <jsp:include page="top.jsp" />

    <div id="main" class="">
        <div id="inner-content" class="">
            <div id="content" class="container max-container" role="main">
                <div id="ribbon">
                    <ol class="breadcrumb">
                        <li>
                            <a href="/ometa/secureIndex.action">Dashboard</a>
                        </li>
                        <li>Admin</li>
                        <li>User Management</li>
                    </ol>
                </div>

                <s:form id="actorRolePage" name="actorRolePage" namespace="/" action="actorRole" method="post" theme="simple">
                    <s:hidden name="type" id="type" />

                    <div class="page-header">
                        <h1>User Management</h1>
                    </div>

                    <div id="HeaderPane">
                        <div id="errorMessagesPanel" style="margin-top:15px;"></div>
                        <s:if test="hasActionErrors()">
                            <input type="hidden" id="error_messages" value="<s:iterator value='actionErrors'><s:property/><br/></s:iterator>"/>
                        </s:if>
                        <s:if test="hasActionMessages()">
                            <div class="alert_info" onclick="$('.alert_info').remove();">
                                <strong><s:iterator value='actionMessages'><s:property/><br/></s:iterator></strong>
                            </div>
                        </s:if>
                    </div>
                    <div id="mainContent">
                        <div id="mainDiv">
                            <div id="tableTop">
                                <div class="row row_spacer">
                                    <div class="col-md-1">Actor</div>
                                    <div class="col-md-11 combobox">
                                        <s:select id="actorSelect"
                                                  list="actors" name="actorId" headerKey="0" headerValue="None"
                                                  listValue="username + ' - ' + firstName + ' ' +lastName" listKey="loginId" required="true" />
                                    </div>
                                </div>
                                <div class="row row_spacer">
                                    <div class="col-md-1">Groups</div>
                                    <div class="col-md-11">
                                        <s:select id="groupSelect"
                                                  list="groups" name="groupIds"
                                                  listValue="groupNameLookupValue.name" listKey="groupId"
                                                  multiple="true" required="true" style="width:400px;height:19px;
                                        "/>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div id="buttonDiv" style="margin:15px 10px 5px 0;width:100%;">
                            <input type="button" class="btn btn-success" onclick="submit();" id="setup" value="Setup Role"/>
                            <input type="button" class="btn btn-info" onclick="popup();" id="addRole" value="Add Actor Role"/>
                            <input type="button" class="btn btn-warning" onclick="passReset();" id="resetPass" value="Reset Password"/>
                            <input type="button" class="btn btn-default" onclick="doClear();" value="Clear" />
                        </div>
                    </div>

                </s:form>

            </div>
        </div>
    </div>
</div>

<jsp:include page="../html/footer.html" />

<script src="scripts/jquery/chosen.jquery.min.js"></script>

<script type="text/javascript">
    (function() {
        utils.error.check();
        $('#actorSelect').combobox();
        // $('#actorSelect').chosen().change(function() {
        //   $("#groupSelect option:selected").removeAttr("selected");
        //   var selectedUser = $(this).find("option:selected");
        //   makeAjax(selectedUser.val());
        // });
        $('#groupSelect').chosen();
    })();

    function doClear() {
        $("#actorSelect, #groupSelect").val('');
        $('#groupSelect > option').removeAttr('selected');
        $("#groupSelect").trigger("chosen:updated");
    }

    function makeAjax(actorId, cb) {
        $.ajax({
            url: 'actorRoleAjax.action',
            cache: false,
            async: true,
            data: 'actorId='+parseInt(actorId),
            success: function(res){
                if(res.errorMsg) {
                    utils.error.add(res.errorMsg);
                } else {
                    if(cb) {
                        cb(res);
                    } else {
                        $('#groupSelect > option').removeAttr('selected');
                        var actorGroups = res.actorGroups;
                        if(actorGroups) {
                            var $groupOptions = $('#groupSelect option');
                            $.each(actorGroups, function(i, ag) {
                                $('#groupSelect option[value="' + ag.groupId + '"]').attr('selected', 'selected');
                            })
                        }

                        $("#groupSelect").trigger("chosen:updated");
                    }
                }
            },
            fail: function(html) {
                utils.error.add("Ajax Process has Failed.");
            }
        });
    }

    function comboBoxChanged(option, id) {
        if(id === 'actorSelect') {
            if(option && option.value !== 0) {
                makeAjax(option.value);
            }
        }
    }

    function popup() {
        $.openPopupLayer({
            name: 'LPopupAddLookupValue',
            width: 450,
            url: 'addLookupValue.action?type=gr'
        });
    }

    function passReset() {
        $("#type").val("reset");
        submit();
    }

    function submit() {
        var actorId = $('#actorSelect').val();
        if(actorId && actorId != 0) {
            $('form').submit();
        }
    }
</script>
</body>
</html>
