package com.metacodestudio.hotsuploader.scene.control;

import com.metacodestudio.hotsuploader.files.FileHandler;
import com.metacodestudio.hotsuploader.models.ReplayFile;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.util.Callback;

import java.net.URL;

/**
 * @author Eivind Vegsundvåg
 */
public class CustomListCellFactory implements Callback<ListView<ReplayFile>, ListCell<ReplayFile>> {

    private final Image updateImage;
    private final Image deleteImage;
    private final Image failedImage;
    private final FileHandler fileHandler;

    public CustomListCellFactory(FileHandler fileHandler) {
        this.fileHandler = fileHandler;
        URL updateResource = getClass().getResource("update.png");
        URL deleteResource = getClass().getResource("delete.png");
        URL failedResource = getClass().getResource("failed.png");
        String updateUrl = updateResource.toExternalForm();
        String deleteUrl = deleteResource.toExternalForm();
        String failedUrl = failedResource.toExternalForm();
        updateImage = new Image(updateUrl);
        deleteImage = new Image(deleteUrl);
        failedImage = new Image(failedUrl);
        ///svgPath
    }
    @Override
    public ListCell<ReplayFile> call(ListView<ReplayFile> param) {
        return new CustomListCell(updateImage, deleteImage, failedImage, fileHandler);
    }
}
