package com.mamedov.opencv;

import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

import java.io.File;
import java.net.URI;
import java.util.List;

public class ImageChooser {
    private final FileChooser chooser;
    private final List<ExtensionFilter> filters; // Фильтры файлов по их расширениям.

    public ImageChooser() {
        chooser = new FileChooser();
        filters = chooser.getExtensionFilters();
    }

    // Метод для выбора изображения.
    public Image openImage() {
        File file = chooser.showOpenDialog(null); // Открываем файл.
        if (file != null) {
            URI uri = file.toURI(); // Преобразуем файл в URI.
            return new Image(uri.toString());
        }

        return null; // Если изображение не выбрано, тогда возвращаем null.
    }

    // Метод для утановки форматов.
    public void setAvailableFormats(String... formats) {
        filters.clear(); // Удаляем все прошлые форматы.
        if (formats != null && formats.length > 0) { // Если есть что добавить.
            ExtensionFilter filter = new ExtensionFilter(String.join(", ", formats), formats);
            filters.add(filter);
        }
    }
}