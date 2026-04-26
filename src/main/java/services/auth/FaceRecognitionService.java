package services.auth;

import entities.user.User;
import services.user.UserService;

import java.util.Base64;
import java.util.Optional;
import java.util.List;

public class FaceRecognitionService {
    private final UserService userService;
    private final FaceCaptureService faceCaptureService;
    private static final double SIMILARITY_THRESHOLD = 0.70;

    public FaceRecognitionService() {
        this.userService = new UserService();
        this.faceCaptureService = new FaceCaptureService();
    }

    public boolean isFaceRecognitionAvailable() {
        return FaceCaptureService.isOpenCVLoaded() && faceCaptureService.isInitialized();
    }

    public Optional<User> authenticateWithFace() {
        if (!isFaceRecognitionAvailable()) {
            throw new RuntimeException("Face recognition is not available");
        }

        try {
            String capturedFaceEncoding = faceCaptureService.captureAndEncodeFace();
            if (capturedFaceEncoding == null) {
                return Optional.empty();
            }

            List<User> usersWithFaceAuth = userService.findUsersWithFaceAuthEnabled();
            
            for (User user : usersWithFaceAuth) {
                if (user.getFaceEncoding() != null && !user.getFaceEncoding().isEmpty()) {
                    double similarity = compareFaceEncodings(capturedFaceEncoding, user.getFaceEncoding());
                    System.out.println("Face similarity for " + user.getUsername() + ": " + String.format("%.2f%%", similarity * 100));
                    if (similarity >= SIMILARITY_THRESHOLD) {
                        System.out.println("✅ Face recognized! User: " + user.getUsername());
                        return Optional.of(user);
                    } else {
                        System.out.println("❌ Face not recognized (below threshold: " + String.format("%.2f%%", SIMILARITY_THRESHOLD * 100) + ")");
                    }
                }
            }

            return Optional.empty();
        } catch (Exception e) {
            System.err.println("Error during face authentication: " + e.getMessage());
            return Optional.empty();
        }
    }

    public boolean registerFaceForUser(User user) {
        if (!isFaceRecognitionAvailable()) {
            throw new RuntimeException("Face recognition is not available");
        }
        try {
            String faceEncoding = faceCaptureService.captureAndEncodeFace();
            if (faceEncoding == null) {
                return false;
            }

            user.setFaceEncoding(faceEncoding);
            user.setFaceAuthenticationEnabled(true);
            
            return userService.updateUserWithFaceData(user);
        } catch (Exception e) {
            System.err.println("Error registering face for user: " + e.getMessage());
            return false;
        }
    }

    public boolean updateFaceForUser(User user) {
        if (!isFaceRecognitionAvailable()) {
            throw new RuntimeException("Face recognition is not available");
        }
        
        try {
            String faceEncoding = faceCaptureService.captureAndEncodeFace();
            if (faceEncoding == null) {
                return false;
            }

            user.setFaceEncoding(faceEncoding);
            
            return userService.updateUserWithFaceData(user);
        } catch (Exception e) {
            System.err.println("Error updating face for user: " + e.getMessage());
            return false;
        }
    }

    public boolean enableFaceAuthForUser(User user) {
        if (user.getFaceEncoding() == null || user.getFaceEncoding().isEmpty()) {
            System.err.println("No face data found for user");
            return false;
        }

        try {
            user.setFaceAuthenticationEnabled(true);
            return userService.updateUserWithFaceData(user);
        } catch (Exception e) {
            System.err.println("Error enabling face auth for user: " + e.getMessage());
            return false;
        }
    }

    public boolean disableFaceAuthForUser(User user) {
        try {
            user.setFaceAuthenticationEnabled(false);
            return userService.updateUserWithFaceData(user);
        } catch (Exception e) {
            System.err.println("Error disabling face auth for user: " + e.getMessage());
            return false;
        }
    }

    private double compareFaceEncodings(String encoding1, String encoding2) {
        try {
            System.out.println("Comparing face encodings...");
            System.out.println("Encoding1 length: " + (encoding1 != null ? encoding1.length() : 0));
            System.out.println("Encoding2 length: " + (encoding2 != null ? encoding2.length() : 0));
            
            byte[] bytes1 = Base64.getDecoder().decode(encoding1);
            byte[] bytes2 = Base64.getDecoder().decode(encoding2);
            
            System.out.println("Bytes1 length: " + bytes1.length);
            System.out.println("Bytes2 length: " + bytes2.length);

            if (bytes1.length != bytes2.length) {
                System.out.println("Byte arrays have different lengths!");
                return 0.0;
            }

            // Convert back to float values (0.0 to 1.0 range)
            float[] features1 = new float[bytes1.length];
            float[] features2 = new float[bytes2.length];
            
            for (int i = 0; i < bytes1.length; i++) {
                features1[i] = (bytes1[i] & 0xFF) / 255.0f;
                features2[i] = (bytes2[i] & 0xFF) / 255.0f;
            }
            
            System.out.println("Features converted, length: " + features1.length);

            // Use simpler pixel comparison instead of histogram correlation
            double similarity = calculatePixelSimilarity(features1, features2);
            System.out.println("Calculated similarity: " + String.format("%.4f", similarity));
            return similarity;
        } catch (Exception e) {
            System.err.println("Error comparing face encodings: " + e.getMessage());
            return 0.0;
        }
    }

    private double calculatePixelSimilarity(float[] features1, float[] features2) {
        if (features1.length != features2.length) {
            System.out.println("Feature arrays have different lengths!");
            return 0.0;
        }

        System.out.println("Calculating pixel similarity...");
        
        double totalDifference = 0.0;
        for (int i = 0; i < features1.length; i++) {
            totalDifference += Math.abs(features1[i] - features2[i]);
        }
        
        double averageDifference = totalDifference / features1.length;
        double similarity = 1.0 - averageDifference; // Invert difference to get similarity
        
        System.out.println("Total difference: " + String.format("%.6f", totalDifference));
        System.out.println("Average difference: " + String.format("%.6f", averageDifference));
        System.out.println("Pixel similarity: " + String.format("%.6f", similarity));
        
        return Math.max(0.0, Math.min(1.0, similarity)); // Clamp to 0-1 range
    }

    public void release() {
        faceCaptureService.release();
    }
}
