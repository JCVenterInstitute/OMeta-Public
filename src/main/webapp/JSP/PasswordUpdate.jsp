
<!doctype html>

  <%@ page contentType="text/html; charset=UTF-8" %>
  <%@ taglib uri="/struts-tags" prefix="s" %>
  <%@ page isELIgnored="false" %>

  <head>
    <link rel="stylesheet" href="style/version01.css" />

    <jsp:include page="header.jsp" />
    <style>
    </style>
  </head>

  <body class="smart-style-2">
    <div id="container">

    <jsp:include page="top.jsp" />

    
    <div id="main" class="container max-container">
      <div id="inner-content" class="">
        <div id="content" role="main">
            <div class="col-xs-12 page-header">
              <h1 class="page-title">Change Password</h1>
            </div>

            <div id="HeaderPane" class="row">
              <div id="errorMessagesPanel" class="container"></div>
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

              <div class="col-sm-12">
                <div class="form-instructions">
                  <p>Please use the form below to update your password.</p>
                  <div class="well well-sm">
                    <strong><small class="text-danger">*</small> Required Field</strong>
                    <div class="row" id=""></div>
                  </div>
                </div>

                <s:form id="passwordUpdatePage" name="passwordUpdatePage" namespace="/" action="passwordUpdate" method="post" theme="simple">

                  <div class="row">
                    <div class="col-sm-6">
                      <div class="form-group">
                        <label for="user_name" class="col-sm-3 col-lg-4 control-label text-left"><small class="text-danger">*</small> Current Password</label>
                        <div class="col-sm-8">
                          <input type="password" class="form-control" id="oldPass" name="oldPass" required>
                        </div>
                      </div>
                    </div>
                  </div>
                  <div class="row" style="margin-top:15px;">
                    <div class="col-sm-6">
                      <div class="form-group">
                        <label for="user_name" class="col-sm-3 col-lg-4 control-label text-left"><small class="text-danger">*</small> New Password</label>
                        <div class="col-sm-8">
                          <input type="password" class="form-control" id="newPass" name="newPass" required>
                        </div>
                      </div>
                    </div>
                    <div class="col-sm-6">
                      <div class="form-group">
                        <label for="user_name" class="col-sm-3 col-lg-4 control-label text-left"><small class="text-danger">*</small> Retype New Password</label>
                        <div class="col-sm-8">
                          <input type="password" class="form-control" id="newPassRe" name="newPassRe" required>
                        </div>
                      </div>
                    </div>
                  </div>
                  <div class="form-group">
                    <div class="col-sm-12">
                      <input type="button" class="btn btn-primary" onclick="formSubmit();" value="Update Password" />
                    </div>
                  </div>

                </s:form>

              </div>
            </div>
          </div>
      </div>
    </div>
  </div>

  <jsp:include page="../html/footer.html" />

  <script type="text/javascript">
    function formSubmit() {
      utils.error.remove();

      if(!$('#oldPass').val()) {
        utils.error.alert("'Current Password' field is empty!");
        return;  
      }
      if(!$('#newPass').val()) {
        utils.error.alert("'New Password' field is empty!");  
        return;
      }
      if(!$('#newPassRe').val()) {
        utils.error.alert("'Retype New Password' field is empty!");  
        return;
      }

      if($('#newPass').val() !== $('#newPassRe').val()) {
        utils.error.alert("'New Password' and 'Retype New Password' don't match! Please try it again.");
        return;
      }

      if(!checkPassword($('#newPassRe').val())) {
        utils.error.alert("<br/>Password requirements are:<br/> - minimum 8 characters<br/> - at least 1 number<br/> - at least 1 upper case character<br/> - at least 1 lower case character<br/>");
        return;
      }

      $('form').submit();
    }

    function checkPassword(pass) {
      var regex = /(?=.*\d)(?=.*[a-z])(?=.*[A-Z]).{8,}/;
      return regex.test(pass);
    }

    utils.error.check();
  </script>
  
</body>
</html>
