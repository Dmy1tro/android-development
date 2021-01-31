package dmytro.laskuryk.lab_4.Repositories;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

public class ImageRepository {
    private final File imagesRoot;

    public ImageRepository(File root) {
        imagesRoot = new File(root, "Images");

        if (!imagesRoot.exists()) {
            imagesRoot.mkdirs();
        }
    }

    public String save(Bitmap bitmapImage) {
        String name = UUID.randomUUID().toString() + ".png";
        File imageFile = initializeFile(name);

        FileOutputStream fos = null;

        try {
            fos = new FileOutputStream(imageFile);
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return name;
    }

    public Bitmap get(String name) {
        Bitmap imageBitmap = null;

        try {
            File imageFile = initializeFile(name);
            FileInputStream fs = new FileInputStream(imageFile);
            imageBitmap = BitmapFactory.decodeStream(fs);
            fs.close();
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        return imageBitmap;
    }

    public void remove(String name) {
        initializeFile(name).delete();
    }

    private File initializeFile(String name) { return new File(imagesRoot, name); }
}
