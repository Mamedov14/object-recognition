package com.mamedov.opencv;

import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
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

// JAVAFX LOGIC APPLICATION!
public class Controller extends Application {
    private final VideoCapture capture = new VideoCapture();
    private ScheduledExecutorService timer;

    private final ImageView originalFrame = new ImageView();
    private final ImageView maskImage = new ImageView();

    private ObjectProperty<String> hsvValuesProperty;
    private final Label hsvCurrentValues = new Label();

    private final Button cameraButton = new Button("Start camera");
    private final Button imageButton = new Button("Add Image");
    private boolean cameraActive;

    private final Slider idHueStart = new Slider(0, 180, 0);
    private final Slider idHueStop = new Slider(0, 180, 180);
    private final Slider idSaturationStart = new Slider(0, 255, 0);
    private final Slider idSaturationStop = new Slider(0, 255, 255);
    private final Slider idValueStart = new Slider(0, 255, 0);
    private final Slider idValueStop = new Slider(0, 255, 46);

    private final Button black = new Button("Black");
    private final Button white = new Button("White");
    private final Button red = new Button("Red");
    private final Button brown = new Button("Brown");
    private final Button yellow = new Button("Yellow");
    private final Button green = new Button("Green");
    private final Button blue1 = new Button("Blue1");
    private final Button blue2 = new Button("Blue2");
    private final Button violet = new Button("Violet");

    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #9a9a9a;");

        // RIGHT
        // названия меток
        Label hueStart = new Label("Hue Start");
        Label hueStop = new Label("Hue Stop");
        Label saturationStart = new Label("Saturation Start");
        Label saturationStop = new Label("Saturation Stop");
        Label valueStart = new Label("Value Start");
        Label valueStop = new Label("Value Stop");

        // ползунки
        idHueStart.setBlockIncrement(1);
        idHueStop.setBlockIncrement(1);
        idSaturationStart.setBlockIncrement(1);
        idSaturationStop.setBlockIncrement(1);
        idValueStart.setBlockIncrement(1);
        idValueStop.setBlockIncrement(1);

        // активация кнопок для выбора цвета
        buttonColor();

        // box в котором лежат цветные кнопки
        HBox hBox = new HBox(black, white, red, brown, yellow, green, blue1, blue2, violet);

        // box в котором лежат метки и ползунки
        VBox vBox = new VBox(10,
                hueStart, idHueStart,
                hueStop, idHueStop,
                saturationStart, idSaturationStart,
                saturationStop, idSaturationStop,
                valueStart, idValueStart,
                valueStop, idValueStop, hBox);

        vBox.setAlignment(Pos.CENTER);
        vBox.setSpacing(10);
        vBox.setPadding(new Insets(0, 10, 0, 10));
        root.setRight(vBox);

        // CENTER

        HBox hBoxMask = new HBox(5, maskImage);
        hBoxMask.setAlignment(Pos.CENTER_RIGHT);
        HBox hBoxCenter = new HBox(5, originalFrame, hBoxMask);
        hBoxCenter.setAlignment(Pos.CENTER_LEFT);
        hBoxCenter.setPadding(new Insets(0, 10, 0, 10));
        root.setCenter(hBoxCenter);


        //BOTTOM
        cameraButton.setAlignment(Pos.CENTER);
        imageButton.setAlignment(Pos.CENTER);

        // активация захвата камеры
        cameraButton.setOnAction(event -> startCamera());

        // активация добавления изображения
        imageButton.setOnAction(event -> addImage());

        Separator separator = new Separator();

        VBox vBoxBottom = new VBox(15, cameraButton, imageButton, separator, hsvCurrentValues);
        vBoxBottom.setAlignment(Pos.CENTER);
        vBoxBottom.setPadding(new Insets(25, 25, 25, 25));
        root.setBottom(vBoxBottom);

/////////////////////////////////////////////////////////////////////////////////////

        // закидываем в сцену root контейнер
        Scene scene = new Scene(root, 1300, 600);
        stage.setTitle("Object Recognition");
        stage.getIcons().add(new Image("img/opencv.png"));
        stage.setScene(scene);

        // показ
        stage.show();
    }

    private void buttonColor() {
        black.setOnAction(event -> {
            resetColor();
            black.setStyle("-fx-background-color: black");
            idHueStart.setValue(0);
            idHueStop.setValue(180);

            idSaturationStart.setValue(0);
            idSaturationStop.setValue(255);

            idValueStart.setValue(0);
            idValueStop.setValue(46);
        });

        white.setOnAction(event -> {
            resetColor();
            white.setStyle("-fx-background-color: white");
            idHueStart.setValue(0);
            idHueStop.setValue(180);

            idSaturationStart.setValue(0);
            idSaturationStop.setValue(43);

            idValueStart.setValue(46);
            idValueStop.setValue(220);
        });

        red.setOnAction(event -> {
            resetColor();
            red.setStyle("-fx-background-color: red");
            idHueStart.setValue(0);
            idHueStop.setValue(10);

            idSaturationStart.setValue(43);
            idSaturationStop.setValue(255);

            idValueStart.setValue(46);
            idValueStop.setValue(255);
        });

        brown.setOnAction(event -> {
            resetColor();
            brown.setStyle("-fx-background-color: brown");
            idHueStart.setValue(11);
            idHueStop.setValue(25);

            idSaturationStart.setValue(43);
            idSaturationStop.setValue(255);

            idValueStart.setValue(46);
            idValueStop.setValue(255);
        });

        yellow.setOnAction(event -> {
            resetColor();
            yellow.setStyle("-fx-background-color: yellow");
            idHueStart.setValue(26);
            idHueStop.setValue(34);

            idSaturationStart.setValue(43);
            idSaturationStop.setValue(255);

            idValueStart.setValue(46);
            idValueStop.setValue(255);
        });

        green.setOnAction(event -> {
            Controller.this.resetColor();
            green.setStyle("-fx-background-color: green");
            idHueStart.setValue(35);
            idHueStop.setValue(77);

            idSaturationStart.setValue(43);
            idSaturationStop.setValue(255);

            idValueStart.setValue(46);
            idValueStop.setValue(255);
        });

        blue1.setOnAction(event -> {
            resetColor();
            blue1.setStyle("-fx-background-color: #00b2ff");
            idHueStart.setValue(78);
            idHueStop.setValue(99);

            idSaturationStart.setValue(43);
            idSaturationStop.setValue(255);

            idValueStart.setValue(46);
            idValueStop.setValue(255);
        });

        blue2.setOnAction(event -> {
            resetColor();
            blue2.setStyle("-fx-background-color: blue");
            idHueStart.setValue(100);
            idHueStop.setValue(124);

            idSaturationStart.setValue(43);
            idSaturationStop.setValue(255);

            idValueStart.setValue(46);
            idValueStop.setValue(255);
        });

        violet.setOnAction(event -> {
            resetColor();
            violet.setStyle("-fx-background-color: violet");
            idHueStart.setValue(125);
            idHueStop.setValue(155);

            idSaturationStart.setValue(43);
            idSaturationStop.setValue(255);

            idValueStart.setValue(46);
            idValueStop.setValue(255);
        });
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


    private void startCamera() {

        /**
         * связываем текстовое свойство со строкой,
         * содержащей текущий диапазон значения HSV для обнаружения объекта
         */

        hsvValuesProperty = new SimpleObjectProperty<>();
        this.hsvCurrentValues.textProperty().bind(hsvValuesProperty);

        /**
         * устанавливаем фиксированную ширину для всего изображения,
         *  чтобы показать и сохранить соотношение изображения
         */

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
            this.stopTimer();
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

                    Scalar minValues = new Scalar(this.idHueStart.getValue(), this.idSaturationStart.getValue(), this.idValueStart.getValue());
                    Scalar maxValues = new Scalar(this.idHueStop.getValue(), this.idSaturationStop.getValue(), this.idSaturationStop.getValue());

                    String valuesToPrint = "Hue range: " + minValues.val[0] + "-" + maxValues.val[0] + "\tSaturation range: " + minValues.val[1] + "-" + maxValues.val[1] + "\tValue range: " + minValues.val[2] + "-" + maxValues.val[2];
                    Utils.onFXThread(this.hsvValuesProperty, valuesToPrint);

                    Core.inRange(hsvImage, minValues, maxValues, mask);
                    this.updateImageView(this.maskImage, Utils.matToImage(mask));

                    Mat dilateElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(24, 24));
                    Mat erodeElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(12, 12));

                    /**
                     * Размывает изображение, используя определенный структурирующий элемент.
                     * Функция размывает исходное изображение,
                     * используя указанный структурирующий элемент,
                     * определяющий форму окрестности пикселя, по которой берется минимум...
                     * */

                    Imgproc.erode(mask, morphOutput, erodeElement);
                    Imgproc.erode(morphOutput, morphOutput, erodeElement);

                    /**
                     * Расширяет изображение, используя определенный структурирующий элемент.
                     * */

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
        /**
         * Указывает, сохранить ли формат изображения исходного изображения, масштабируясь,
         * чтобы соответствовать изображению в пределах подходящего ограничивающего прямоугольника.
         * */
    }

    private void stopTimer() {
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
        this.stopTimer();
    }

    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        launch(args);
    }
}