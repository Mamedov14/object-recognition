package com.mamedov.opencv;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ObjectRecognitionController {
    @FXML
    private Button cameraButton;
    // область FXML для отображения текущего кадра
    @FXML
    private ImageView originalFrame;
    // область FXML для отображения маски
    @FXML
    private ImageView maskImage;
    // область FXML для отображения результатов морфологических операций
//    @FXML
//    private ImageView morphImage;
    // Ползунок FXML для установки диапазонов HSV
    @FXML
    private Slider hueStart;
    @FXML
    private Slider hueStop;
    @FXML
    private Slider saturationStart;
    @FXML
    private Slider saturationStop;
    @FXML
    private Slider valueStart;
    @FXML
    private Slider valueStop;
    // Метка FXML для отображения текущих значений, установленных с помощью ползунков
    @FXML
    private Label hsvCurrentValues;

    @FXML
    private Button black;
    @FXML
    private Button white;
    @FXML
    private Button red;
    @FXML
    private Button brown;
    @FXML
    private Button yellow;
    @FXML
    private Button green;
    @FXML
    private Button blue1;
    @FXML
    private Button blue2;
    @FXML
    private Button violet;

    // таймер получения видеопотока
    private ScheduledExecutorService timer;
    private final VideoCapture capture = new VideoCapture();
    private boolean cameraActive;

    // свойство для привязки объекта
    private ObjectProperty<String> hsvValuesProperty;

    @FXML
    private void startCamera() {
        hsvValuesProperty = new SimpleObjectProperty<>();
        this.hsvCurrentValues.textProperty().bind(hsvValuesProperty);

        this.imageViewProperties(this.originalFrame, 400);
        this.imageViewProperties(this.maskImage, 400);
//        this.imageViewProperties(this.morphImage, 200);

        if (!this.cameraActive) {
            this.capture.open(0);

            if (this.capture.isOpened()) {
                this.cameraActive = true;

                Runnable grabber = () -> {
                    Mat frame = grabFrame();
                    Image imageToShow = Utils.matToImage(frame);
                    updateImageView(originalFrame, imageToShow);
                };

                this.timer = Executors.newSingleThreadScheduledExecutor();
                this.timer.scheduleAtFixedRate(grabber, 0, 33, TimeUnit.MILLISECONDS);

                this.cameraButton.setText("Stop Camera");
            } else {
                System.err.println("Failed to open the camera connection...");
            }
        } else {
            this.cameraActive = false;
            this.cameraButton.setText("Start Camera");
            this.stop();
        }
    }

    private Mat grabFrame() {
        Mat frame = new Mat();

        if (this.capture.isOpened()) {
            try {
                this.capture.read(frame);
                if (!frame.empty()) {
                    Mat blurredImage = new Mat();
                    Mat hsvImage = new Mat();
                    Mat mask = new Mat();
                    Mat morphOutput = new Mat();

                    Imgproc.blur(frame, blurredImage, new Size(7, 7));

                    Imgproc.cvtColor(blurredImage, hsvImage, Imgproc.COLOR_BGR2HSV);

                    Scalar minValues = new Scalar(this.hueStart.getValue(), this.saturationStart.getValue(),
                            this.valueStart.getValue());
                    Scalar maxValues = new Scalar(this.hueStop.getValue(), this.saturationStop.getValue(),
                            this.valueStop.getValue());

                    String valuesToPrint = "Hue range: " + minValues.val[0] + "-" + maxValues.val[0]
                            + "\tSaturation range: " + minValues.val[1] + "-" + maxValues.val[1] + "\tValue range: "
                            + minValues.val[2] + "-" + maxValues.val[2];
                    Utils.onFXThread(this.hsvValuesProperty, valuesToPrint);

                    Core.inRange(hsvImage, minValues, maxValues, mask);
                    this.updateImageView(this.maskImage, Utils.matToImage(mask));

                    Mat dilateElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(24, 24));
                    Mat erodeElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(12, 12));

                    Imgproc.erode(mask, morphOutput, erodeElement);
                    Imgproc.erode(morphOutput, morphOutput, erodeElement);

                    Imgproc.dilate(morphOutput, morphOutput, dilateElement);
                    Imgproc.dilate(morphOutput, morphOutput, dilateElement);

//                    this.updateImageView(this.morphImage, Utils.matToImage(morphOutput));

                    this.findAndDrawBalls(morphOutput, frame);

                }

            } catch (Exception e) {
                System.err.print("Exception during the image elaboration...");
                e.printStackTrace();
            }
        }

        return frame;
    }

    private Mat findAndDrawBalls(Mat maskedImage, Mat frame) {
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();

        Imgproc.findContours(maskedImage, contours, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);

        if (hierarchy.size().height > 0 && hierarchy.size().width > 0) {
            for (int idx = 0; idx >= 0; idx = (int) hierarchy.get(0, idx)[0]) {
                Imgproc.drawContours(frame, contours, idx, new Scalar(250, 0, 0));
            }
        }

        return frame;
    }

    private void imageViewProperties(ImageView image, int dimension) {
        image.setFitWidth(dimension);
        image.setPreserveRatio(true);
    }

    private void stop() {
        if (this.timer != null && !this.timer.isShutdown()) {
            try {
                this.timer.shutdown();
                this.timer.awaitTermination(33, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                System.err.println("Exception in stopping the frame capture, trying to release the camera now... " + e);
            }
        }
        if (this.capture.isOpened()) {
            this.capture.release();
        }
    }

    private void updateImageView(ImageView view, Image image) {
        Utils.onFXThread(view.imageProperty(), image);
    }

    protected void setClosed() {
        this.stop();
    }

    @FXML
    private void onClickBlack() {
        red.
        hueStart.setValue(0);
        hueStop.setValue(180);

        saturationStart.setValue(0);
        saturationStop.setValue(25);

        valueStart.setValue(0);
        valueStop.setValue(46);
    }

    @FXML
    private void onClickWhite() {
        hueStart.setValue(0);
        hueStop.setValue(180);

        saturationStart.setValue(0);
        saturationStop.setValue(43);

        valueStart.setValue(46);
        valueStop.setValue(220);
    }

    @FXML
    private void onClickRed() {
        hueStart.setValue(0);
        hueStop.setValue(10);

        saturationStart.setValue(43);
        saturationStop.setValue(255);

        valueStart.setValue(46);
        valueStop.setValue(255);
    }

    @FXML
    private void onClickBrown() {
        hueStart.setValue(11);
        hueStop.setValue(25);

        saturationStart.setValue(43);
        saturationStop.setValue(255);

        valueStart.setValue(46);
        valueStop.setValue(255);
    }

    @FXML
    private void onClickYellow() {
        hueStart.setValue(26);
        hueStop.setValue(34);

        saturationStart.setValue(43);
        saturationStop.setValue(255);

        valueStart.setValue(46);
        valueStop.setValue(255);
    }

    @FXML
    private void onClickGreen() {
        hueStart.setValue(35);
        hueStop.setValue(77);

        saturationStart.setValue(43);
        saturationStop.setValue(255);

        valueStart.setValue(46);
        valueStop.setValue(255);
    }

    @FXML
    private void onClickBlue1() {
        hueStart.setValue(78);
        hueStop.setValue(99);

        saturationStart.setValue(43);
        saturationStop.setValue(255);

        valueStart.setValue(46);
        valueStop.setValue(255);
    }

    @FXML
    private void onClickBlue2() {
        hueStart.setValue(100);
        hueStop.setValue(124);

        saturationStart.setValue(43);
        saturationStop.setValue(255);

        valueStart.setValue(46);
        valueStop.setValue(255);
    }

    @FXML
    private void onClickViolet() {
        hueStart.setValue(125);
        hueStop.setValue(155);

        saturationStart.setValue(43);
        saturationStop.setValue(255);

        valueStart.setValue(46);
        valueStop.setValue(255);
    }
}