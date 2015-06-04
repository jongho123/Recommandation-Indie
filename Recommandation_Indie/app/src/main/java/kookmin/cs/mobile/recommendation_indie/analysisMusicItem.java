package kookmin.cs.mobile.recommendation_indie;

/**
 * Created by sloth on 2015-06-03.
 */
public class analysisMusicItem {

  private String[] mData;

  public analysisMusicItem(String[] obj) {
    mData = obj;
  }

  public analysisMusicItem(String obj01, String obj02, String obj03, String obj04) {
    mData = new String[4];

    mData[0] = obj01;
    mData[1] = obj02;
    mData[2] = obj03;
    mData[3] = obj04;
  }

  public String[] getData() {
    return mData;
  }

  public String getData(int index) {
    if ( mData == null || index >= mData.length ) {
      return null;
    }

    return mData[index];
  }

  public void setData(String obj[]) {
    mData = obj;
  }
}
