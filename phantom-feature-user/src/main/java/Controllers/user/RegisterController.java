package Controllers.user;

import entities.user.User;
import services.user.UserService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

public class RegisterController {

    private final UserService userService = new UserService();

    @FXML
    private TextField emailField;
    @FXML
    private Label emailError;

    @FXML
    private TextField usernameField;
    @FXML
    private Label usernameError;

    @FXML
    private TextField fullNameField;
    @FXML
    private Label fullNameError;

    @FXML
    private PasswordField passwordField;
    @FXML
    private Label passwordError;

    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Label confirmPasswordError;

    @FXML
    private TextField countryField;
    @FXML
    private Label countryError;

    @FXML
    private TextField birthDateField;
    @FXML
    private Label birthDateError;

    @FXML
    private ComboBox<String> roleComboBox;

    @FXML
    private Button registerButton;

    @FXML
    private Hyperlink loginLink;

    @FXML
    private Label successLabel;

    @FXML
    public void initialize() {
        clearAllErrors();
        clearSuccessLabel();
        
        // Initialize role combo box
        roleComboBox.getItems().addAll("PLAYER", "COACH", "ORGANIZER", "ADMIN");
        roleComboBox.getSelectionModel().selectFirst(); // Default to PLAYER
    }

    @FXML
    private void handleRegister() {
        clearAllErrors();
        clearSuccessLabel();

        // Get input values
        String email = emailField.getText().trim();
        String username = usernameField.getText().trim();
        String fullName = fullNameField.getText().trim();
        String password = passwordField.getText().trim();
        String confirmPassword = confirmPasswordField.getText().trim();
        String country = countryField.getText().trim();
        String birthDateStr = birthDateField.getText().trim();
        String role = roleComboBox.getValue();

        // Validate all fields
        boolean hasErrors = false;

        // Email validation
        List<String> emailErrors = validateEmail(email);
        if (!emailErrors.isEmpty()) {
            emailError.setText(String.join(", ", emailErrors));
            hasErrors = true;
        }

        // Username validation
        List<String> usernameErrors = validateUsername(username, null);
        if (!usernameErrors.isEmpty()) {
            usernameError.setText(String.join(", ", usernameErrors));
            hasErrors = true;
        }

        // Full Name validation
        List<String> fullNameErrors = validateFullName(fullName);
        if (!fullNameErrors.isEmpty()) {
            fullNameError.setText(String.join(", ", fullNameErrors));
            hasErrors = true;
        }

        // Password validation
        List<String> passwordErrors = validatePassword(password);
        if (!passwordErrors.isEmpty()) {
            passwordError.setText(String.join(", ", passwordErrors));
            hasErrors = true;
        }

        // Confirm password validation
        if (!password.equals(confirmPassword)) {
            confirmPasswordError.setText("Les mots de passe ne correspondent pas");
            hasErrors = true;
        }

        // Country validation
        List<String> countryErrors = validateCountry(country);
        if (!countryErrors.isEmpty()) {
            countryError.setText(String.join(", ", countryErrors));
            hasErrors = true;
        }

        // Birth Date validation
        List<String> birthDateErrors = validateBirthDate(birthDateStr);
        if (!birthDateErrors.isEmpty()) {
            birthDateError.setText(String.join(", ", birthDateErrors));
            hasErrors = true;
        }

        // Role validation
        List<String> roleErrors = validateRole(role);
        if (!roleErrors.isEmpty()) {
            hasErrors = true;
        }

        if (hasErrors) {
            return;
        }

        try {
            // Check if email already exists
            if (userService.emailExists(email)) {
                emailError.setText("Cet email est déjà enregistré");
                return;
            }

            // Check if username already exists
            if (userService.usernameExists(username)) {
                usernameError.setText("Ce nom d'utilisateur est déjà pris");
                return;
            }

            // Parse birth date
            LocalDate birthDate = LocalDate.parse(birthDateStr);

            // Create user
            String roles = buildRolesJson(role);
            User user = new User(email, roles, password, username, fullName, country, birthDate, role);
            
            userService.createUser(user);
            
            showSuccess("Inscription réussie ! Veuillez vous connecter avec votre nouveau compte.");
            clearFields();

            // Auto-redirect to login after 2 seconds
            new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        javafx.application.Platform.runLater(() -> handleLogin());
                    }
                },
                2000
            );

        } catch (Exception e) {
            showError("Échec de l'inscription : " + e.getMessage());
        }
    }

    @FXML
    private void handleLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/user/fxml/login.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) loginLink.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Login");
            stage.show();
            
        } catch (Exception e) {
            showError("Cannot open login screen: " + e.getMessage());
        }
    }

    // Validation methods (copied from UserController)
    private List<String> validateEmail(String email) {
        List<String> errors = new ArrayList<>();
        if (email == null || email.isBlank()) {
            errors.add("L'adresse email est obligatoire");
            return errors;
        }
        if (email.length() > 180)
            errors.add("L'email ne doit pas dépasser 180 caractères");
        if (!email.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$"))
            errors.add("Format d'email invalide (ex: nom@domaine.com)");
        return errors;
    }

    private List<String> validateUsername(String username, Integer excludeId) {
        List<String> errors = new ArrayList<>();
        if (username == null || username.isBlank()) {
            errors.add("Le nom d'utilisateur est obligatoire");
            return errors;
        }
        if (username.length() < 3)
            errors.add("Le nom d'utilisateur doit contenir au moins 3 caractères");
        if (username.length() > 50)
            errors.add("Le nom d'utilisateur ne doit pas dépasser 50 caractères");
        if (!username.matches("^[a-zA-Z0-9_]+$"))
            errors.add("Le nom d'utilisateur ne peut contenir que des lettres, chiffres et underscores");
        return errors;
    }

    private List<String> validateFullName(String fullName) {
        List<String> errors = new ArrayList<>();
        if (fullName == null || fullName.isBlank()) {
            errors.add("Le nom complet est obligatoire");
            return errors;
        }
        if (fullName.length() < 2)
            errors.add("Le nom complet doit contenir au moins 2 caractères");
        if (fullName.length() > 100)
            errors.add("Le nom complet ne doit pas dépasser 100 caractères");
        if (!fullName.matches("^[a-zA-ZÀ-ÿ\\s'\\-]+$"))
            errors.add("Le nom complet ne peut contenir que des lettres, espaces, tirets et apostrophes");
        return errors;
    }

    private List<String> validatePassword(String password) {
        List<String> errors = new ArrayList<>();
        if (password == null || password.isBlank()) {
            errors.add("Le mot de passe est obligatoire");
            return errors;
        }
        if (password.length() < 8)
            errors.add("Le mot de passe doit contenir au moins 8 caractères");
        if (!password.matches(".*[A-Z].*"))
            errors.add("Le mot de passe doit contenir au moins une lettre majuscule");
        if (!password.matches(".*[a-z].*"))
            errors.add("Le mot de passe doit contenir au moins une lettre minuscule");
        if (!password.matches(".*[0-9].*"))
            errors.add("Le mot de passe doit contenir au moins un chiffre");
        if (!password.matches(".*[^A-Za-z0-9].*"))
            errors.add("Le mot de passe doit contenir au moins un caractère spécial");
        return errors;
    }

    private List<String> validateCountry(String country) {
        List<String> errors = new ArrayList<>();
        if (country == null || country.isBlank()) {
            errors.add("Le pays est obligatoire");
            return errors;
        }
        if (country.length() > 50)
            errors.add("Le pays ne doit pas dépasser 50 caractères");
        if (!country.matches("^[a-zA-ZÀ-ÿ\\s\\-]+$"))
            errors.add("Le pays ne peut contenir que des lettres, espaces et tirets");
        return errors;
    }

    private List<String> validateBirthDate(String dateStr) {
        List<String> errors = new ArrayList<>();
        if (dateStr == null || dateStr.isBlank()) {
            errors.add("La date de naissance est obligatoire");
            return errors;
        }
        LocalDate birthDate;
        try {
            birthDate = LocalDate.parse(dateStr);
        } catch (Exception e) {
            errors.add("Format de date invalide (utilisez AAAA-MM-JJ)");
            return errors;
        }
        int age = Period.between(birthDate, LocalDate.now()).getYears();
        if (age < 13)
            errors.add("L'utilisateur doit avoir au moins 13 ans");
        if (age > 100)
            errors.add("Date de naissance invalide (plus de 100 ans)");
        return errors;
    }

    private List<String> validateRole(String role) {
        List<String> errors = new ArrayList<>();
        if (role == null || role.isBlank()) {
            errors.add("Le rôle est obligatoire");
            return errors;
        }
        if (!List.of("PLAYER", "COACH", "ORGANIZER", "ADMIN").contains(role.toUpperCase()))
            errors.add("Rôle invalide. Choisissez: PLAYER, COACH, ORGANIZER, ADMIN");
        return errors;
    }

    private String buildRolesJson(String role) {
        return switch (role) {
            case "ADMIN" -> "[\"ROLE_ADMIN\"]";
            case "COACH" -> "[\"ROLE_USER\",\"ROLE_COACH\"]";
            case "ORGANIZER" -> "[\"ROLE_USER\",\"ROLE_ORGANIZER\"]";
            default -> "[\"ROLE_USER\",\"ROLE_PLAYER\"]";
        };
    }

    private void clearAllErrors() {
        emailError.setText("");
        usernameError.setText("");
        fullNameError.setText("");
        passwordError.setText("");
        confirmPasswordError.setText("");
        countryError.setText("");
        birthDateError.setText("");
    }

    private void clearSuccessLabel() {
        successLabel.setText("");
    }

    private void showSuccess(String message) {
        successLabel.setText(message);
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur d'inscription");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void clearFields() {
        emailField.clear();
        usernameField.clear();
        fullNameField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        countryField.clear();
        birthDateField.clear();
        roleComboBox.getSelectionModel().selectFirst();
    }
}
