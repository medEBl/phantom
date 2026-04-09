package Iservices.tournament;

import entities.tournament.Registration;
import java.util.List;
import java.util.Optional;

public interface IRegistrationService {
    void createRegistration(Registration registration);
    void deleteRegistration(int id);
    Optional<Registration> getRegistrationById(int id);
    List<Registration> getAllRegistrations();
    List<Registration> getRegistrationsByTournament(int tournamentId);
}
