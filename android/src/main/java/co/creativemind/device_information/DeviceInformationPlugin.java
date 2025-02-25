package android.src.main.java.co.creativemind.device_information;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaDrm;
import android.os.Build;
import android.telephony.TelephonyManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import java.util.Arrays;
import java.util.UUID;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

/** DeviceInformationPlugin */
public class DeviceInformationPlugin implements FlutterPlugin, MethodCallHandler, ActivityAware {
  private MethodChannel channel;
  private Activity activity;

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "device_information");
    channel.setMethodCallHandler(this);
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    switch (call.method) {
      case "getPlatformVersion":
        result.success("Android " + Build.VERSION.RELEASE);
        break;

      case "getIMEINumber":
        if (activity == null) {
          result.error("NO_ACTIVITY", "Activity is not attached", null);
          return;
        }

        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
          result.error("PERMISSION_DENIED", "READ_PHONE_STATE permission is required", null);
        } else {
          result.success(getIMEINo());
        }
        break;

      case "getAPILevel":
        result.success(Build.VERSION.SDK_INT);
        break;

      case "getModel":
        result.success(Build.MODEL);
        break;

      case "getManufacturer":
        result.success(Build.MANUFACTURER);
        break;

      case "getDevice":
        result.success(Build.DEVICE);
        break;

      case "getProduct":
        result.success(Build.PRODUCT);
        break;

      case "getCPUType":
        result.success(Build.CPU_ABI);
        break;

      case "getHardware":
        result.success(Build.HARDWARE);
        break;

      default:
        result.notImplemented();
        break;
    }
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    channel.setMethodCallHandler(null);
  }

  @SuppressLint({"HardwareIds", "MissingPermission"})
  public String getIMEINo() {
    if (activity == null) return "";

    TelephonyManager telephonyManager =
            (TelephonyManager) activity.getSystemService(Context.TELEPHONY_SERVICE);

    if (telephonyManager == null) return "";

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
      return getDeviceUniqueID();
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      return telephonyManager.getImei() != null ? telephonyManager.getImei() : "";
    } else {
      return telephonyManager.getDeviceId() != null ? telephonyManager.getDeviceId() : "";
    }
  }

  @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
  @Nullable
  public String getDeviceUniqueID() {
    UUID wideVineUuid = new UUID(-0x121074568629b532L, -0x5c37d8232ae2de13L);
    try {
      MediaDrm wvDrm = new MediaDrm(wideVineUuid);
      byte[] wideVineId = wvDrm.getPropertyByteArray(MediaDrm.PROPERTY_DEVICE_UNIQUE_ID);
      return Arrays.toString(wideVineId)
              .replaceAll("[\\[\\], -]", "")  // Remove brackets, commas, hyphens, and spaces
              .substring(0, 15);
    } catch (Exception e) {
      return "";
    }
  }

  @Override
  public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
    this.activity = binding.getActivity();
  }

  @Override
  public void onDetachedFromActivityForConfigChanges() {
    this.activity = null;
  }

  @Override
  public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
    this.activity = binding.getActivity();
  }

  @Override
  public void onDetachedFromActivity() {
    this.activity = null;
  }
}
