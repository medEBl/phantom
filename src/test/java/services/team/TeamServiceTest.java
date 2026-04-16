package services.team;

import entities.team.Team;
import Iservices.team.ITeamService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 Test Class for TeamService
 * Tests ONLY CRUD operations
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TeamServiceTest {

    private static ITeamService teamService;
    private static Team testTeam;
    private static int testTeamId;

    @BeforeAll
    static void setUp() {
        System.out.println("=== Setting up TeamService Tests ===");
        teamService = new TeamService();

        testTeam = new Team();
        testTeam.setName("TestTeam_" + System.currentTimeMillis());
        testTeam.setGame("Test Game");
        testTeam.setCreationDate(LocalDateTime.now());
        testTeam.setCoachId(6); // Only coach ID 6 exists in database
    }

    // ========== CREATE ==========

    @Test
    @Order(1)
    void testCreateTeam() {
        System.out.println("\n=== TEST CREATE: createTeam() ===");

        teamService.createTeam(testTeam);
        testTeamId = testTeam.getId();

        Optional<Team> created = teamService.getTeamById(testTeamId);
        assertTrue(created.isPresent());

        // FIX: Remove .toUpperCase() - TeamService keeps original case
        assertEquals(testTeam.getGame(), created.get().getGame());
        assertEquals(testTeam.getCoachId(), created.get().getCoachId());

        System.out.println("✅ Équipe créée avec ID: " + testTeamId);
        System.out.println("   Game in DB: " + created.get().getGame());
    }

    // ========== READ ==========

    @Test
    @Order(2)
    void testGetTeamById() {
        System.out.println("\n=== TEST READ: getTeamById() ===");

        // Create team if not already created
        if (testTeamId == 0) {
            teamService.createTeam(testTeam);
            testTeamId = testTeam.getId();
        }

        Optional<Team> found = teamService.getTeamById(testTeamId);
        assertTrue(found.isPresent(), "Team with ID " + testTeamId + " should exist");
        assertEquals(testTeamId, found.get().getId());

        System.out.println("✅ READ: Team found with ID: " + testTeamId);
    }

    @Test
    @Order(3)
    void testGetAllTeams() {
        System.out.println("\n=== TEST READ: getAllTeams() ===");

        if (testTeamId == 0) {
            teamService.createTeam(testTeam);
            testTeamId = testTeam.getId();
        }

        List<Team> allTeams = teamService.getAllTeams();
        assertNotNull(allTeams);
        assertFalse(allTeams.isEmpty());

        boolean found = allTeams.stream().anyMatch(t -> t.getId() == testTeamId);
        assertTrue(found, "Test team should be in list");

        System.out.println("✅ READ: Retrieved " + allTeams.size() + " teams");
    }

    // ========== UPDATE ==========

    @Test
    @Order(4)
    void testUpdateTeam() {
        System.out.println("\n=== TEST UPDATE: updateTeam() ===");

        if (testTeamId == 0) {
            teamService.createTeam(testTeam);
            testTeamId = testTeam.getId();
        }

        // FIX: Remove .toUpperCase() - TeamService keeps original case
        String updatedGame = testTeam.getGame() + "_Updated";
        testTeam.setGame(updatedGame);

        teamService.updateTeam(testTeam);

        Optional<Team> updated = teamService.getTeamById(testTeamId);
        assertTrue(updated.isPresent());
        assertEquals(updatedGame, updated.get().getGame());

        System.out.println("✅ UPDATE: Team updated - Game: " + updatedGame);
    }

    // ========== DELETE ==========

    @Test
    @Order(5)
    void testDeleteTeam() {
        System.out.println("\n=== TEST DELETE: deleteTeam() ===");

        if (testTeamId == 0) {
            teamService.createTeam(testTeam);
            testTeamId = testTeam.getId();
        }

        teamService.deleteTeam(testTeamId);

        Optional<Team> deleted = teamService.getTeamById(testTeamId);
        assertFalse(deleted.isPresent());

        System.out.println("✅ DELETE: Team deleted with ID: " + testTeamId);

        testTeamId = 0;
    }

    // Optional: Clean up at VERY END only
    @AfterAll
    static void finalCleanup() {
        System.out.println("\n=== Final cleanup ===");
        try {
            if (testTeamId > 0) {
                Optional<Team> checkTeam = teamService.getTeamById(testTeamId);
                if (checkTeam.isPresent()) {
                    teamService.deleteTeam(testTeamId);
                    System.out.println("Final cleanup deleted team ID: " + testTeamId);
                }
            }
        } catch (Exception e) {
            System.out.println("Error in final cleanup: " + e.getMessage());
        }
    }
}