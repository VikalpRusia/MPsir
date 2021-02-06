<%--
  Created by IntelliJ IDEA.
  User: vikalp rusia
  Date: 06-02-2021
  Time: 09:10
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title>Forgot Password</title>
    <link rel="icon" href="applicationIcon.PNG" type="image/x-icon">
    <script>
        function closeWindow() {
            close();
        }
    </script>
</head>
<body>
<div style="width:30%;margin-left: auto;margin-right:auto;margin-top: 10%">
    <form>
        <label>Find Your Account</label>
        <hr/>
        <label for="search">Please enter your email address or phone number to search for your account.</label><br>
        <input id="search" type="text" name="search" placeholder="Mobile number">
        <br>
        <div>
            <button type="submit" value="Search" style="background: lightskyblue">Search</button>
            <button type="button" value="Cancel" onclick="closeWindow()">Cancel</button>
        </div>
    </form>
</div>
</body>
</html>
