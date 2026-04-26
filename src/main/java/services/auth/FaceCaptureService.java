package services.auth;

import javafx.scene.image.Image;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

public class FaceCaptureService {
    private static final String CASCADE_FILE;
    private CascadeClassifier faceCascade;
    private VideoCapture camera;
    private boolean openCVLoaded = false;
    private boolean isInitialized = false;

    static {
        // Try to find OpenCV cascade file
        String opencvDir = System.getenv("OPENCV_DIR");
        if (opencvDir != null) {
            CASCADE_FILE = opencvDir + "/etc/haarcascades/haarcascade_frontalface_default.xml";
        } else {
            CASCADE_FILE = "haarcascade_frontalface_default.xml";
        }
    }

    static {
        try {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
            openCVLoaded = true;
            System.out.println("OpenCV native library loaded successfully");
        } catch (UnsatisfiedLinkError e) {
            System.err.println("OpenCV native library not found: " + e.getMessage());
            openCVLoaded = false;
        }
    }

    public FaceCaptureService() {
        if (openCVLoaded) {
            initialize();
        }
    }

    private void initialize() {
        try {
            System.out.println("Loading cascade file from: " + CASCADE_FILE);
            faceCascade = new CascadeClassifier(CASCADE_FILE);
            
            if (faceCascade.empty()) {
                System.err.println("Failed to load cascade classifier");
                return;
            }

            camera = new VideoCapture(0);
            if (!camera.isOpened()) {
                System.err.println("Failed to open camera");
                return;
            }

            isInitialized = true;
            System.out.println("Face capture service initialized successfully");
        } catch (Exception e) {
            System.err.println("Error initializing face capture service: " + e.getMessage());
            isInitialized = false;
        }
    }

    public boolean isInitialized() {
        return isInitialized && openCVLoaded;
    }

    public static boolean isOpenCVLoaded() {
        return openCVLoaded;
    }

    public byte[] captureFaceImage() {
        if (!isInitialized || !openCVLoaded) {
            System.err.println("Face capture service not initialized or OpenCV not available");
            return null;
        }

        try {
            Mat frame = new Mat();
            if (!camera.read(frame)) {
                System.err.println("Failed to capture frame from camera");
                return null;
            }

            Mat grayFrame = new Mat();
            Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);

            MatOfRect faces = new MatOfRect();
            faceCascade.detectMultiScale(grayFrame, faces);

            Rect[] facesArray = faces.toArray();
            if (facesArray.length == 0) {
                System.err.println("No face detected");
                return null;
            }

            Rect faceRect = facesArray[0];
            Mat faceMat = new Mat(grayFrame, faceRect);

            BufferedImage bufferedImage = matToBufferedImage(faceMat);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "jpg", baos);

            frame.release();
            grayFrame.release();
            faceMat.release();

            return baos.toByteArray();
        } catch (Exception e) {
            System.err.println("Error capturing face: " + e.getMessage());
            return null;
        }
    }

    public Image captureFaceImageFX() {
        byte[] faceData = captureFaceImage();
        if (faceData != null) {
            ByteArrayInputStream bis = new ByteArrayInputStream(faceData);
            return new Image(bis);
        }
        return null;
    }

    public String captureAndEncodeFace() {
        byte[] faceData = captureFaceImage();
        if (faceData != null) {
            try {
                System.out.println("Face captured, size: " + faceData.length + " bytes");
                
                // Convert image to OpenCV Mat
                Mat faceMat = Imgcodecs.imdecode(new MatOfByte(faceData), Imgcodecs.IMREAD_GRAYSCALE);
                System.out.println("Face Mat dimensions: " + faceMat.rows() + "x" + faceMat.cols());
                
                // Resize to standard size for consistent comparison
                Mat resizedFace = new Mat();
                Size standardSize = new Size(100, 100);
                Imgproc.resize(faceMat, resizedFace, standardSize);
                System.out.println("Resized Face Mat dimensions: " + resizedFace.rows() + "x" + resizedFace.cols());
                
                // Extract simple face features (histogram-based)
                MatOfFloat features = extractFaceFeatures(resizedFace);
                System.out.println("Features extracted, size: " + features.toArray().length);
                
                // Convert features to Base64 string
                String encoded = featuresToBase64(features);
                System.out.println("Face encoded successfully, length: " + (encoded != null ? encoded.length() : 0));
                return encoded;
            } catch (Exception e) {
                System.err.println("Error encoding face: " + e.getMessage());
                e.printStackTrace();
                return null;
            }
        }
        System.err.println("No face data captured");
        return null;
    }
    
    private MatOfFloat extractFaceFeatures(Mat face) {
        // Simple histogram-based feature extraction
        MatOfInt histSize = new MatOfInt(256);
        MatOfFloat ranges = new MatOfFloat(0f, 256f);
        Mat histogram = new Mat();
        
        Imgproc.calcHist(java.util.Collections.singletonList(face), 
                        new MatOfInt(0), 
                        new Mat(), 
                        histogram, 
                        histSize, 
                        ranges);
        
        // Normalize histogram
        Core.normalize(histogram, histogram, 0, 1, Core.NORM_L1);
        
        // Convert to float array
        float[] histData = new float[(int) histogram.total()];
        histogram.get(0, 0, histData);
        
        return new MatOfFloat(histData);
    }
    
    private String featuresToBase64(MatOfFloat features) {
        try {
            float[] data = features.toArray();
            byte[] bytes = new byte[data.length];
            for (int i = 0; i < data.length; i++) {
                bytes[i] = (byte) (data[i] * 255); // Convert back to 0-255 range
            }
            return Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            System.err.println("Error converting features to Base64: " + e.getMessage());
            return null;
        }
    }

    public void release() {
        if (camera != null && camera.isOpened()) {
            camera.release();
        }
    }

    public boolean detectFaceInImage(byte[] imageData) {
        if (!isInitialized || !openCVLoaded) {
            return false;
        }

        try {
            Mat image = Imgcodecs.imdecode(new MatOfByte(imageData), Imgcodecs.IMREAD_COLOR);
            if (image.empty()) {
                return false;
            }

            MatOfRect faces = new MatOfRect();
            faceCascade.detectMultiScale(image, faces);

            return faces.toArray().length > 0;
        } catch (Exception e) {
            System.err.println("Error detecting face in image: " + e.getMessage());
            return false;
        }
    }

    private BufferedImage matToBufferedImage(Mat mat) {
        int width = mat.cols();
        int height = mat.rows();
        int channels = mat.channels();
        byte[] data = new byte[width * height * channels];

        mat.get(0, 0, data);

        int type;
        if (channels == 1) {
            type = BufferedImage.TYPE_BYTE_GRAY;
        } else {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }

        BufferedImage image = new BufferedImage(width, height, type);
        image.getRaster().setDataElements(0, 0, width, height, data);

        return image;
    }
}
