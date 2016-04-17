package android.example.com.boguscode;

/**
 * Created by Rohit on 4/12/2016.
 */
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

public class ImagesCache
{
    private  LruCache<String, Bitmap> imagesTable;

    private static ImagesCache cache;

    public static ImagesCache getInstance()
    {
        if(cache == null)
        {
            cache = new ImagesCache();
        }

        return cache;
    }

    public void initializeCache()
    {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() /1024);

        final int cacheSize = maxMemory / 8;

        System.out.println("cache size = "+cacheSize);

        imagesTable = new LruCache<String, Bitmap>(cacheSize)
        {
            protected int sizeOf(String key, Bitmap value)
            {
                // The cache size will be measured in kilobytes rather than number of items.

                int bitmapByteCount = value.getRowBytes() * value.getHeight();

                return bitmapByteCount / 1024;
              }
        };
    }

    public void addImageToTable(String key, Bitmap value)
    {
        if(imagesTable != null && imagesTable.get(key) == null)
        {
            imagesTable.put(key, value);
        }
    }

    public Bitmap getImageFromTable(String key)
    {
        if(key != null)
        {
            return imagesTable.get(key);
        }
        else
        {
            return null;
        }
    }
}

