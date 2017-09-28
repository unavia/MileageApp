package ca.kendallroth.mileageapp.utils;

import android.content.res.Resources;
import android.graphics.Point;
import android.util.DisplayMetrics;

/**
 * Various Screen utility functions
 *   Taken from: https://alvinalexander.com/android/how-to-determine-android-screen-size-dimensions-orientation
 */
public abstract class ScreenUtils {
  /**
   * Get the current display metrics
   * @return Current display metrics
   */
  public static DisplayMetrics getDisplayMetrics() {
    DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();

    return displayMetrics;
  }

  /**
   * Get the current display size
   * @return Current display size
   */
  public static Point getDisplaySize() {
    DisplayMetrics displayMetrics = ScreenUtils.getDisplayMetrics();

    return new Point(displayMetrics.widthPixels, displayMetrics.heightPixels);
  }

  /**
   * Get the current display orientation
   * @return Current display orientation
   */
  public static Orientation getDisplayOrientation() {
    Point displaySize = ScreenUtils.getDisplaySize();

    return displaySize.x > displaySize.y ? Orientation.LANDSCAPE : Orientation.PORTRAIT;
  }
}
