public interface UserManager {
    boolean createAccount(String username, String password);
    boolean login(String username, String password);
    boolean deleteAccount(String username, String password);
}
