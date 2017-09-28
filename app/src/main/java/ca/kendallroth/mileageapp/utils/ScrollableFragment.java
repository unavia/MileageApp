package ca.kendallroth.mileageapp.utils;

/**
 * Interface to add methods for fragment scrolling
 */
public interface ScrollableFragment {
  /**
   * Scroll to a position within a fragment view
   * @param position Destination scroll position
   * @param smooth Whether smooth scrolling should be used
   */
  //void scrollToPosition(int position, boolean smooth);

  /**
   * Scroll to the top of a fragment view
   */
  void scrollToTop();

  /**
   * Scroll to the top of a fragment view
   * @param smooth Whether smooth scrolling should be used
   */
  void scrollToTop(boolean smooth);
}
