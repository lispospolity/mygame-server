public class CredentialsRules {
    public static UserLogin.ServerResponse credsLegal(String name, String password) {
        UserLogin.ServerResponse nameExit = nameLegal(name);
        UserLogin.ServerResponse passwordExit = passwordLegal(password);
        if (!nameExit.success()) return nameExit;
        if (!passwordExit.success()) return passwordExit;
        return new UserLogin.ServerResponse(true, "", 200);
    }
    private static UserLogin.ServerResponse passwordLegal(String password) {
        if (password == null) return new UserLogin.ServerResponse(false, "Password cannot be empty.", 200);
        if (password.length()<8) return new UserLogin.ServerResponse(false, "Password cannot be shorter than 8 letters.", 200);;
        return new UserLogin.ServerResponse(true, "", 200);
    }
    private static UserLogin.ServerResponse nameLegal(String name) {
        if (name == null) return new UserLogin.ServerResponse(false, "Name cannot be empty", 200);
        if (name.length()<3) return new UserLogin.ServerResponse(false, "Name cannot be shorter than 3 characters", 200);
        if (name.contains("@")||name.contains(".")) return new UserLogin.ServerResponse(false, "Name cannot include characters - '@', '.' ", 200);
        return new UserLogin.ServerResponse(true, "", 200);
    }
}
