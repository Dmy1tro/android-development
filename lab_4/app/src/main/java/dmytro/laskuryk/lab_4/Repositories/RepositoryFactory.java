package dmytro.laskuryk.lab_4.Repositories;

import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;

import dmytro.laskuryk.lab_4.Interfaces.INoteRepository;

@RequiresApi(api = Build.VERSION_CODES.R)
public class RepositoryFactory {
    public static INoteRepository getRepository(Context context) {
        // return new FileRepository(context.getExternalFilesDir(null));
        return new SQLiteRepository(context);
    }

    public static ImageRepository getImageRepository(Context context) {
        return new ImageRepository(context.getExternalFilesDir(null));
    }
}
