package garin.artemiy.compassview.library.widget;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.GeomagneticField;
import android.location.Location;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import garin.artemiy.compassview.library.CompassSensorManager;

/**
 * @author Artemiy Garin
 * @since 26.11.13
 */
public class CompassView
    extends ImageView
    implements Animation.AnimationListener
{

  private static final int FAST_ANIMATION_DURATION = 200;

  private static final int DEGREES_360 = 360;

  private static final float CENTER = 0.5f;

  private Context context;

  private Location userLocation;

  private Location objectLocation;

  private Bitmap directionBitmap;

  private int drawableResource;

  private float lastRotation;

  private CompassSensorManager compassSensorManager;

  @SuppressWarnings("unused")
  public CompassView(Context context)
  {
    super(context);
    init(context);
  }

  @SuppressWarnings("unused")
  public CompassView(Context context, AttributeSet attrs)
  {
    super(context, attrs);
    init(context);
  }

  private void init(Context context)
  {
    this.context = context;

    if (isDeviceCompatible(context) == false)
    {
      setVisibility(View.GONE);
    }
  }

  public void initializeCompass(CompassSensorManager compassSensorManager, Location userLocation,
      Location objectLocation, int drawableResource)
  {
    if (isDeviceCompatible(context) == true)
    {
      this.compassSensorManager = compassSensorManager;
      this.userLocation = userLocation;
      this.objectLocation = objectLocation;
      this.drawableResource = drawableResource;
      startRotation();
    }
  }

  private void startRotation()
  {
    final GeomagneticField geomagneticField = new GeomagneticField((float) userLocation.getLatitude(), (float) userLocation.getLongitude(), (float) userLocation.getAltitude(), System.currentTimeMillis());

    float azimuth = compassSensorManager.getAzimuth();
    azimuth -= geomagneticField.getDeclination();

    float bearTo = userLocation.bearingTo(objectLocation);
    if (bearTo < 0)
    {
      bearTo = bearTo + DEGREES_360;
    }

    float rotation = bearTo - azimuth;
    if (rotation < 0)
    {
      rotation = rotation + DEGREES_360;
    }

    rotateImageView(rotation);
  }

  @SuppressWarnings("ConstantConditions")
  private void rotateImageView(float currentRotate)
  {
    if (directionBitmap == null)
    {
      directionBitmap = BitmapFactory.decodeResource(getResources(), drawableResource);

      final Animation fadeInAnimation = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);
      fadeInAnimation.setAnimationListener(this);
      startAnimation(fadeInAnimation);
      setImageDrawable(new BitmapDrawable(getResources(), directionBitmap));
      setScaleType(ScaleType.CENTER);
    }
    else
    {
      currentRotate = currentRotate % DEGREES_360;

      final RotateAnimation rotateAnimation = new RotateAnimation(lastRotation, currentRotate, Animation.RELATIVE_TO_SELF, CompassView.CENTER, Animation.RELATIVE_TO_SELF, CompassView.CENTER);
      rotateAnimation.setInterpolator(new LinearInterpolator());
      rotateAnimation.setDuration(CompassView.FAST_ANIMATION_DURATION);
      rotateAnimation.setFillAfter(true);
      rotateAnimation.setAnimationListener(this);

      lastRotation = currentRotate;

      startAnimation(rotateAnimation);
    }
  }

  @Override
  public void onAnimationStart(Animation animation)
  {

  }

  @Override
  public void onAnimationEnd(Animation animation)
  {
    startRotation();
  }

  @Override
  public void onAnimationRepeat(Animation animation)
  {

  }

  public static boolean isDeviceCompatible(Context context)
  {
    return context.getPackageManager() != null && context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_SENSOR_ACCELEROMETER) == true && context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_SENSOR_COMPASS) == true;
  }

}
