package Controllers.team;

import entities.user.User;

public interface IDashboardTeamController {
    void refreshTeams();
    void showSuccess(String message);
    void showWarning(String message);
    void showError(String message);
    void setTeamController(IDashboardTeamController teamController);
    void setCurrentUser(User user);
}
