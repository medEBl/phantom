package Controllers.matchy;

import entities.user.User;

public interface IDashboardMatchyController {
    void loadMatches();
    void showSuccess(String message);
    void showWarning(String message);
    void showError(String message);
    void setMatchyController(IDashboardMatchyController matchyController);
    void setCurrentUser(User user);
}
