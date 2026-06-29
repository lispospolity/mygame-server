public class CredentialsRules {
    public static Boolean credsLegal(String name, String password) {
        if (passwordLegal(password)&&nameLegal(name)) return true;
        return false;
    }
    private static Boolean passwordLegal(String password) {
        if (password == null) return false;
        if (password.length()<8) return false;
        return true;
    }
    private static Boolean nameLegal(String name) {
        if (name == null) return false;
        if (name.length()<3) return false;
        if (name.contains("@")) return false;
        return true;
    }
}
