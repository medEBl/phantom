package Controllers.team;

public interface IDashboardTeamController {
    void refreshTeams();
    void showSuccess(String message);
    void showWarning(String message);
    void showError(String message);
    void setTeamController(IDashboardTeamController teamController);
}
