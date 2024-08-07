/*
package com.mamedov.opencv;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.mamedov.opencv.Utils.MatToBufferedImage;
import static com.mamedov.opencv.Utils.imageToMat;

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

    @FXML
    private Button imageButton;

    // таймер получения видеопотока
    private ScheduledExecutorService timer;
    private final VideoCapture capture = new VideoCapture();
    private boolean cameraActive;

    // свойство для привязки объекта
    private ObjectProperty<String> hsvValuesProperty;

    @FXML
    private void startCamera() {

        */
/**
         * связываем текстовое свойство со строкой,
         * содержащей текущий диапазон значения HSV для обнаружения объекта
         *//*


        hsvValuesProperty = new SimpleObjectProperty<>();
        this.hsvCurrentValues.textProperty().bind(hsvValuesProperty);

        */
/**
         * устанавливаем фиксированную ширину для всего изображения,
         *  чтобы показать и сохранить соотношение изображения
         *//*


        this.imageViewProperties(this.originalFrame, 400);
        this.imageViewProperties(this.maskImage, 400);
//      this.imageViewProperties(this.morphImage, 200);

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

                    Scalar minValues = new Scalar(this.hueStart.getValue(), this.saturationStart.getValue(), this.valueStart.getValue());
                    Scalar maxValues = new Scalar(this.hueStop.getValue(), this.saturationStop.getValue(), this.valueStop.getValue());

                    String valuesToPrint = "Hue range: " + minValues.val[0] + "-" + maxValues.val[0] + "\tSaturation range: " + minValues.val[1] + "-" + maxValues.val[1] + "\tValue range: " + minValues.val[2] + "-" + maxValues.val[2];
                    Utils.onFXThread(this.hsvValuesProperty, valuesToPrint);

                    Core.inRange(hsvImage, minValues, maxValues, mask);
                    this.updateImageView(this.maskImage, Utils.matToImage(mask));

                    Mat dilateElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(24, 24));
                    Mat erodeElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(12, 12));

                    */
/**
                     * Размывает изображение, используя определенный структурирующий элемент.
                     * Функция размывает исходное изображение,
                     * используя указанный структурирующий элемент,
                     * определяющий форму окрестности пикселя, по которой берется минимум...
                     * *//*


                    Imgproc.erode(mask, morphOutput, erodeElement);
                    Imgproc.erode(morphOutput, morphOutput, erodeElement);

                    */
/**
                     * Расширяет изображение, используя определенный структурирующий элемент.
                     * *//*


                    Imgproc.dilate(morphOutput, morphOutput, dilateElement);
                    Imgproc.dilate(morphOutput, morphOutput, dilateElement);

//                  this.updateImageView(this.morphImage, Utils.matToImage(morphOutput));

                    this.findAndDrawBalls(morphOutput, frame);
                }
            } catch (Exception exception) {
                System.err.print("Exception while processing image...");
                exception.printStackTrace();
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
                Imgproc.drawContours(frame, contours, idx, new Scalar(0, 0, 255), 2);
            }
        }

        return frame;
    }

    private void imageViewProperties(ImageView image, int dimension) {
        image.setFitWidth(dimension);
        image.setPreserveRatio(true);
        */
/**
         * Указывает, сохранить ли формат изображения исходного изображения, масштабируясь,
         * чтобы соответствовать изображению в пределах подходящего ограничивающего прямоугольника.
         * *//*

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
        resetColor();
        black.setStyle("-fx-background-color: black");
        hueStart.setValue(0);
        hueStop.setValue(180);

        saturationStart.setValue(0);
        saturationStop.setValue(255);

        valueStart.setValue(0);
        valueStop.setValue(46);
    }

    @FXML
    private void onClickWhite() {
        resetColor();
        white.setStyle("-fx-background-color: white");
        hueStart.setValue(0);
        hueStop.setValue(180);

        saturationStart.setValue(0);
        saturationStop.setValue(43);

        valueStart.setValue(46);
        valueStop.setValue(220);
    }

    @FXML
    private void onClickRed() {
        resetColor();
        red.setStyle("-fx-background-color: red");
        hueStart.setValue(0);
        hueStop.setValue(10);

        saturationStart.setValue(43);
        saturationStop.setValue(255);

        valueStart.setValue(46);
        valueStop.setValue(255);
    }

    @FXML
    private void onClickBrown() {
        resetColor();
        brown.setStyle("-fx-background-color: #980000");
        hueStart.setValue(11);
        hueStop.setValue(25);

        saturationStart.setValue(43);
        saturationStop.setValue(255);

        valueStart.setValue(46);
        valueStop.setValue(255);
    }

    @FXML
    private void onClickYellow() {
        resetColor();
        yellow.setStyle("-fx-background-color: yellow");
        hueStart.setValue(26);
        hueStop.setValue(34);

        saturationStart.setValue(43);
        saturationStop.setValue(255);

        valueStart.setValue(46);
        valueStop.setValue(255);
    }

    @FXML
    private void onClickGreen() {
        resetColor();
        green.setStyle("-fx-background-color: green");
        hueStart.setValue(35);
        hueStop.setValue(77);

        saturationStart.setValue(43);
        saturationStop.setValue(255);

        valueStart.setValue(46);
        valueStop.setValue(255);
    }

    @FXML
    private void onClickBlue1() {
        resetColor();
        blue1.setStyle("-fx-background-color: #00b2ff");
        hueStart.setValue(78);
        hueStop.setValue(99);

        saturationStart.setValue(43);
        saturationStop.setValue(255);

        valueStart.setValue(46);
        valueStop.setValue(255);
    }

    @FXML
    private void onClickBlue2() {
        resetColor();
        blue2.setStyle("-fx-background-color: blue");
        hueStart.setValue(100);
        hueStop.setValue(124);

        saturationStart.setValue(43);
        saturationStop.setValue(255);

        valueStart.setValue(46);
        valueStop.setValue(255);
    }

    @FXML
    private void onClickViolet() {
        resetColor();
        violet.setStyle("-fx-background-color: violet");
        hueStart.setValue(125);
        hueStop.setValue(155);

        saturationStart.setValue(43);
        saturationStop.setValue(255);

        valueStart.setValue(46);
        valueStop.setValue(255);
    }

    private void resetColor() {
        black.setStyle("-fx-background-color: gray");
        white.setStyle("-fx-background-color: gray");
        red.setStyle("-fx-background-color: gray");
        brown.setStyle("-fx-background-color: gray");
        yellow.setStyle("-fx-background-color: gray");
        green.setStyle("-fx-background-color: gray");
        blue1.setStyle("-fx-background-color: gray");
        blue2.setStyle("-fx-background-color: gray");
        violet.setStyle("-fx-background-color: gray");

    }

    @FXML
    private void addImage() {
        ImageChooser chooser = new ImageChooser();
        chooser.setAvailableFormats("*.png", "*.gif", "*.jpg", "*.jpeg");
        Label imageNotSelected = new Label("Image not selected");
        BorderPane root = new BorderPane();
        root.setCenter(imageNotSelected);
        ImageView imageView = new ImageView();
        Button button = new Button("Set Image");
        BorderPane.setAlignment(button, Pos.CENTER);
        root.setBottom(button);
        button.setPadding(new Insets(10));
        button.setStyle("-fx-padding: 10");


        Image image = chooser.openImage();
        button.setOnAction((event) -> {
            if (image != null) {
                imageView.setImage(image);
                imageView.setFitWidth(500.0);
                imageView.setFitHeight(500.0);
                root.setCenter(imageView); // Добавление изображения в контейнер.
            } else root.setCenter(imageNotSelected);
        });

        Button bgrButton = new Button("BGR Image");
        Button grayButton = new Button("Gray Image");
        Button hsvButton = new Button("HSV Image");

        VBox box = new VBox(10, bgrButton, grayButton, hsvButton);
        box.setAlignment(Pos.CENTER);

        bgrButton.setOnAction((event) -> {
            Mat bgrImage = new Mat();
            Imgproc.cvtColor(imageToMat(image), bgrImage, Imgproc.COLOR_RGB2BGR);
            if (bgrImage.empty()) {
                System.out.println("Failed to load image");
                return;
            }
            showImage(bgrImage, "BGR Image");
        });

        grayButton.setOnAction((event) -> {
            Mat bgrImage = new Mat();
            Imgproc.cvtColor(imageToMat(image), bgrImage, Imgproc.COLOR_BGR2GRAY);
            if (bgrImage.empty()) {
                System.out.println("Failed to load image");
                return;
            }
            showImage(bgrImage, "BGR Image");
        });

        hsvButton.setOnAction((event) -> {
            Mat bgrImage = new Mat();
            Imgproc.cvtColor(imageToMat(image), bgrImage, Imgproc.COLOR_BGR2HSV);
            if (bgrImage.empty()) {
                System.out.println("Failed to load image");
                return;
            }
            showImage(bgrImage, "BGR Image");
        });

        root.setRight(box);
        newStage(root);
    }

    public static void showImage(Mat img, String title) {
        BufferedImage image = MatToBufferedImage(img);
        if (image == null) {
            return;
        }
        int w = 1000, h = 600;
        JFrame window = new JFrame(title);
        window.setSize(w, h);
        window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        ImageIcon imageIcon = new ImageIcon(image);
        JLabel label = new JLabel(imageIcon);
        JScrollPane pane = new JScrollPane(label);
        window.setContentPane(pane);
//        if (image.getWidth() < w && image.getHeight() < h) {
//            window.pack();
//        }
        window.setLocationRelativeTo(null);
        window.setVisible(true);
    }

    private void newStage(BorderPane root) {
        Stage primaryStage = new Stage();
        Scene scene = new Scene(root, 640.0, 480.0);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Add Image");
        primaryStage.show();
    }
}*/
