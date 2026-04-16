package services.matchy;

import entities.matchy.Matchy;
import Iservices.matchy.IMatchyService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MatchyServiceTest {

    private static IMatchyService matchyService;
    private static Matchy testMatch;
    private static int testMatchId;
    private static final int TEST_TEAM1_ID = 3;
    private static final int TEST_TEAM2_ID = 2;

    @BeforeAll
    static void setUp() {
        System.out.println("=== Setting up MatchyService Tests ===");
        matchyService = new MatchyService();

        testMatch = new Matchy();
        testMatch.setGame("TestGame_" + System.currentTimeMillis());
        testMatch.setMatchDate(LocalDateTime.now().plusDays(1));
        testMatch.setTeam1Id(TEST_TEAM1_ID);
        testMatch.setTeam2Id(TEST_TEAM2_ID);
        testMatch.setStatus("planned");
    }

    // REMOVED @AfterEach - don't delete after every test

    // ========== CREATE ==========

    @Test
    @Order(1)
    void testCreateMatch() {
        System.out.println("\n=== TEST CREATE: createMatch() ===");

        matchyService.createMatch(testMatch);
        testMatchId = testMatch.getId();

        Optional<Matchy> created = matchyService.getMatchById(testMatchId);
        assertTrue(created.isPresent());

        String expectedGame = testMatch.getGame().toUpperCase();
        assertEquals(expectedGame, created.get().getGame());
        assertEquals(testMatch.getTeam1Id(), created.get().getTeam1Id());
        assertEquals(testMatch.getTeam2Id(), created.get().getTeam2Id());

        System.out.println("✅ Match créé avec ID: " + testMatchId);
        System.out.println("   Game in DB: " + created.get().getGame());
    }

    // ========== READ ==========

    @Test
    @Order(2)
    void testGetMatchById() {
        System.out.println("\n=== TEST READ: getMatchById() ===");

        // Create match if not already created
        if (testMatchId == 0) {
            matchyService.createMatch(testMatch);
            testMatchId = testMatch.getId();
        }

        Optional<Matchy> found = matchyService.getMatchById(testMatchId);
        assertTrue(found.isPresent(), "Match with ID " + testMatchId + " should exist");
        assertEquals(testMatchId, found.get().getId());

        System.out.println("✅ READ: Match found with ID: " + testMatchId);
    }

    @Test
    @Order(3)
    void testGetAllMatches() {
        System.out.println("\n=== TEST READ: getAllMatches() ===");

        if (testMatchId == 0) {
            matchyService.createMatch(testMatch);
            testMatchId = testMatch.getId();
        }

        List<Matchy> allMatches = matchyService.getAllMatches();
        assertNotNull(allMatches);
        assertFalse(allMatches.isEmpty());

        boolean found = allMatches.stream().anyMatch(m -> m.getId() == testMatchId);
        assertTrue(found, "Test match should be in the list");

        System.out.println("✅ READ: Retrieved " + allMatches.size() + " matches");
    }

    // ========== UPDATE ==========

    @Test
    @Order(4)
    void testUpdateMatch() {
        System.out.println("\n=== TEST UPDATE: updateMatch() ===");

        if (testMatchId == 0) {
            matchyService.createMatch(testMatch);
            testMatchId = testMatch.getId();
        }

        String updatedGame = (testMatch.getGame() + "_Updated").toUpperCase();
        testMatch.setGame(updatedGame);
        testMatch.setStatus("ongoing");

        matchyService.updateMatch(testMatch);

        Optional<Matchy> updated = matchyService.getMatchById(testMatchId);
        assertTrue(updated.isPresent());
        assertEquals(updatedGame, updated.get().getGame());
        assertEquals("ongoing", updated.get().getStatus());

        System.out.println("✅ UPDATE: Match updated - Game: " + updatedGame + ", Status: ongoing");
    }

    // ========== DELETE ==========

    @Test
    @Order(5)
    void testDeleteMatch() {
        System.out.println("\n=== TEST DELETE: deleteMatch() ===");

        if (testMatchId == 0) {
            matchyService.createMatch(testMatch);
            testMatchId = testMatch.getId();
        }

        matchyService.deleteMatch(testMatchId);

        Optional<Matchy> deleted = matchyService.getMatchById(testMatchId);
        assertFalse(deleted.isPresent());

        System.out.println("✅ DELETE: Match deleted with ID: " + testMatchId);

        testMatchId = 0;
    }

    // Optional: Clean up at the VERY END only
    @AfterAll
    static void finalCleanup() {
        System.out.println("\n=== Final cleanup ===");
        try {
            if (testMatchId > 0) {
                Optional<Matchy> checkMatch = matchyService.getMatchById(testMatchId);
                if (checkMatch.isPresent()) {
                    matchyService.deleteMatch(testMatchId);
                    System.out.println("Final cleanup deleted match ID: " + testMatchId);
                }
            }
        } catch (Exception e) {
            System.out.println("Error in final cleanup: " + e.getMessage());
        }
    }
}