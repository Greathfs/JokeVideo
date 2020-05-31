package com.mooc.ppjoke.ui.publish;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Rational;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraInfo;
import androidx.camera.core.CameraInfoUnavailableException;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.core.UseCase;
import androidx.camera.core.VideoCapture;
import androidx.camera.core.impl.VideoCaptureConfig;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentActivity;

import com.google.common.util.concurrent.ListenableFuture;
import com.mooc.ppjoke.R;
import com.mooc.ppjoke.databinding.ActivityLayoutCaptureBinding;
import com.mooc.ppjoke.view.RecordView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

public class CaptureActivity extends AppCompatActivity {
    public static final int REQ_CAPTURE = 10001;
    private ActivityLayoutCaptureBinding mBinding;
    private static final String[] PERMISSIONS = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO};
    private static final int PERMISSION_CODE = 1000;
    private ArrayList<String> deniedPermission = new ArrayList<>();
    private int rotation = Surface.ROTATION_0;
    private Size resolution = new Size(1280, 720);
    private Rational rational = new Rational(9, 16);
    private Preview preview;
    private ImageCapture imageCapture;
    private VideoCapture videoCapture;
    private boolean takingPicture;
    private String outputFilePath;
    public static final String RESULT_FILE_PATH = "file_path";
    public static final String RESULT_FILE_WIDTH = "file_width";
    public static final String RESULT_FILE_HEIGHT = "file_height";
    public static final String RESULT_FILE_TYPE = "file_type";
    private Executor executor;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private CameraSelector cameraSelector;

    public static void startActivityForResult(Activity activity) {
        Intent intent = new Intent(activity, CaptureActivity.class);
        activity.startActivityForResult(intent, REQ_CAPTURE);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_layout_capture);
        ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_CODE);
        mBinding.recordView.setOnRecordListener(new RecordView.onRecordListener() {
            @Override
            public void onClick() {
                takingPicture = true;
                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), System.currentTimeMillis() + ".jpeg");
                mBinding.captureTips.setVisibility(View.INVISIBLE);
                ImageCapture.OutputFileOptions outputFileOptions =
                        new ImageCapture.OutputFileOptions.Builder(file).build();
                imageCapture.takePicture(outputFileOptions,executor, new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        onFileSaved(file);
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        showErrorToast(Objects.requireNonNull(exception.getMessage()));
                    }

                });
            }

            @SuppressLint("RestrictedApi")
            @Override
            public void onLongClick() {
                takingPicture = false;
                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), System.currentTimeMillis() + ".mp4");
                videoCapture.startRecording(file, executor, new VideoCapture.OnVideoSavedCallback() {
                    @Override
                    public void onVideoSaved(@NonNull File file) {
                        onFileSaved(file);
                    }

                    @Override
                    public void onError(int videoCaptureError, @NonNull String message, @Nullable Throwable cause) {
                        showErrorToast(message);
                    }
                });
            }

            @SuppressLint("RestrictedApi")
            @Override
            public void onFinish() {
                videoCapture.stopRecording();
            }
        });
    }

    private void showErrorToast(@NonNull String message) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            Toast.makeText(CaptureActivity.this, message, Toast.LENGTH_SHORT).show();
        } else {
            runOnUiThread(() -> Toast.makeText(CaptureActivity.this, message, Toast.LENGTH_SHORT).show());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PreviewActivity.REQ_PREVIEW && resultCode == RESULT_OK) {
            Intent intent = new Intent();
            intent.putExtra(RESULT_FILE_PATH, outputFilePath);
            //当设备处于竖屏情况时，宽高的值 需要互换，横屏不需要
            intent.putExtra(RESULT_FILE_WIDTH, resolution.getHeight());
            intent.putExtra(RESULT_FILE_HEIGHT, resolution.getWidth());
            intent.putExtra(RESULT_FILE_TYPE, !takingPicture);
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    private void onFileSaved(File file) {
        outputFilePath = file.getAbsolutePath();
        String mimeType = takingPicture ? "image/jpeg" : "video/mp4";
        MediaScannerConnection.scanFile(this, new String[]{outputFilePath}, new String[]{mimeType}, null);
        PreviewActivity.startActivityForResult(this, outputFilePath, !takingPicture, "完成");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CODE) {
            deniedPermission.clear();
            for (int i = 0; i < permissions.length; i++) {
                String permission = permissions[i];
                int result = grantResults[i];
                if (result != PackageManager.PERMISSION_GRANTED) {
                    deniedPermission.add(permission);
                }
            }

            if (deniedPermission.isEmpty()) {
                bindCameraX();
            } else {
                new AlertDialog.Builder(this)
                        .setMessage(getString(R.string.capture_permission_message))
                        .setNegativeButton(getString(R.string.capture_permission_no), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                CaptureActivity.this.finish();
                            }
                        })
                        .setPositiveButton(getString(R.string.capture_permission_ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String[] denied = new String[deniedPermission.size()];
                                ActivityCompat.requestPermissions(CaptureActivity.this, deniedPermission.toArray(denied), PERMISSION_CODE);
                            }
                        }).create().show();
            }
        }
    }

    @SuppressLint("RestrictedApi")
    private void bindCameraX() {

        executor = ContextCompat.getMainExecutor(this);

        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this Future.
                // This should never be reached.
            }
        }, executor);

    }

    @SuppressLint("RestrictedApi")
    private void bindPreview(ProcessCameraProvider cameraProvider) {
        CameraX.unbindAll();

        cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        //查询一下当前要使用的设备摄像头(比如后置摄像头)是否存在
        boolean hasAvailableCameraId = false;
        hasAvailableCameraId = CameraX.hasCamera(cameraSelector);

        if (!hasAvailableCameraId) {
            showErrorToast("无可用的设备cameraId!,请检查设备的相机是否被占用");
            finish();
            return;
        }
//
        //查询一下是否存在可用的cameraId.形式如：后置："0"，前置："1"
        String cameraIdForLensFacing = null;
        try {
            cameraIdForLensFacing = CameraX.getCameraFactory().cameraIdForLensFacing(CameraSelector.LENS_FACING_BACK);
        } catch (CameraInfoUnavailableException e) {
            e.printStackTrace();
        }
        if (TextUtils.isEmpty(cameraIdForLensFacing)) {
            showErrorToast("无可用的设备cameraId!,请检查设备的相机是否被占用");
            finish();
            return;
        }

        Preview preview = new Preview.Builder()
                .setCameraSelector(cameraSelector) //前后摄像头
                .setTargetAspectRatio(AspectRatio.RATIO_16_9) //宽高比
                .setTargetRotation(rotation) //旋转角度
                //.setTargetResolution(resolution) //分辨率
                .build();

        imageCapture = new ImageCapture.Builder()
                .setCameraSelector(cameraSelector)
                .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                .setTargetRotation(rotation)
                //.setTargetResolution(resolution)
                .build();

        videoCapture = new VideoCaptureConfig.Builder()
                .setCameraSelector(cameraSelector)
                .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                .setTargetRotation(rotation)
                //.setTargetResolution(resolution)
                //视频帧率
                .setVideoFrameRate(25)
                //bit率
                .setBitRate(10440).build();

        //Caused by: java.lang.IllegalArgumentException: No supported surface combination is found for camera device - Id : 0.  May be attempting to bind too many use cases.
        //cameraSelector与videoCapture不能同时绑定
//        Camera camera = cameraProvider.bindToLifecycle(this, cameraSelector, imageCapture, videoCapture, preview);

//        Camera camera = cameraProvider.bindToLifecycle(this, cameraSelector, imageCapture, preview);
        Camera camera = cameraProvider.bindToLifecycle(this, cameraSelector, videoCapture, preview);

        CameraInfo cameraInfo = camera.getCameraInfo();

        preview.setSurfaceProvider(mBinding.previewView.createSurfaceProvider(cameraInfo));
    }
}

